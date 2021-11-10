/*
 * This file is part of TailConsole and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.tconsole;

import net.covers1624.tconsole.api.TailConsole;
import net.covers1624.tconsole.api.TailGroup;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.internal.CLibrary;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by covers1624 on 11/9/19.
 */
@SuppressWarnings ("SuspiciousMethodCalls")
public class TailConsoleImpl implements TailConsole {

    private final List<TailLineImpl> tailLines = new ArrayList<>();
    private final List<TailGroupImpl> groups = Collections.synchronizedList(new ArrayList<>());

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("TailConsole Executor");
        thread.setDaemon(true);
        return thread;
    });

    private final List<String> stdoutLines = Collections.synchronizedList(new ArrayList<>(32));

    private final PrintStream stdOut;
    private final PrintStream stdErr;

    private final PrintStream ansiOut;
    private final PrintStream ansiErr;

    private final boolean[] isatty = new boolean[2];
    final Terminal terminal;

    private boolean shutdown;
    private ScheduledFuture<?> updateIntervalFuture = null;
    final Object lock = new Object();
    boolean linesChanged = false;
    int lastWindowWidth = -1;

    public TailConsoleImpl() {
        try {
            terminal = TerminalBuilder.builder().build();
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialise Terminal", e);
        }
        for (Output output : Output.values()) {
            isatty[output.ordinal()] = CLibrary.isatty(output.getFileno()) != 0;
        }
        ansiOut = AnsiConsole.out();
        ansiErr = AnsiConsole.err();
        stdOut = AnsiConsole.sysOut();
        stdErr = AnsiConsole.sysErr();
    }

    @Override
    public PrintStream getAnsiOut() {
        if (shutdown) throw new IllegalStateException();
        return ansiOut;
    }

    @Override
    public PrintStream getAnsiErr() {
        if (shutdown) throw new IllegalStateException();
        return ansiErr;
    }

    @Override
    public PrintStream getStdOut() {
        return stdOut;
    }

    @Override
    public PrintStream getStdErr() {
        return stdErr;
    }

    @Override
    public boolean isSupported(Output output) {
        if (shutdown) throw new IllegalStateException();
        return isatty[output.ordinal()];
    }

    @Override
    public void clearTails() {
        if (shutdown) throw new IllegalStateException();
        if (!isSupported(Output.STDOUT)) {
            return;
        }
        Ansi reset = Ansi.ansi();
        for (int i = 0; i < tailLines.size(); i++) {
            if (i != 0) {
                reset.cursorUpLine();
            }
            reset.eraseLine(Ansi.Erase.ALL);
        }
        ansiOut.print(reset);
        ansiOut.flush();
    }

    @Override
    public void drawTails() {
        if (shutdown) throw new IllegalStateException();
        if (!isSupported(Output.STDOUT)) {
            return;
        }
        synchronized (groups) {
            for (int j = 0, groupsSize = groups.size(); j < groupsSize; j++) {
                TailGroupImpl group = groups.get(j);
                group.tick();
            }
        }
        rebuild();
        Ansi a = Ansi.ansi();
        a.cursorToColumn(0);
        for (int i = 0; i < tailLines.size(); i++) {
            TailLineImpl line = tailLines.get(i);
            if (i != 0) {
                a.newline();
            }
            line.dirty = false;
            a.a(line.text).reset();
        }
        a.cursorToColumn(0);
        ansiOut.print(a);
        ansiOut.flush();
    }

    @Override
    public void scheduleStdout(String line) {
        if (shutdown) throw new IllegalStateException();
        synchronized (stdoutLines) {
            stdoutLines.add(line);
        }
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(3, TimeUnit.HOURS);
        } catch (InterruptedException ignored) {
        }
        clearTails();
        shutdown = true;
        synchronized (stdoutLines) {
            for (String line : stdoutLines) {
                if (!line.endsWith("\n")) {
                    line += "\n";
                }
                stdOut.print(line);
            }
        }
        stdOut.flush();
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public List<TailGroup> getTailGroups() {
        if (shutdown) throw new IllegalStateException();
        return Collections.unmodifiableList(groups);
    }

    @Override
    public TailGroup newGroupFirst() {
        if (shutdown) throw new IllegalStateException();
        return addGroup(0);
    }

    @Override
    public TailGroup newGroupBefore(TailGroup other) {
        if (shutdown) throw new IllegalStateException();
        return addGroup(getIndex(other));
    }

    @Override
    public TailGroup newGroupAfter(TailGroup other) {
        if (shutdown) throw new IllegalStateException();
        return addGroup(getIndex(other) + 1);
    }

    @Override
    public TailGroup newGroup() {
        if (shutdown) throw new IllegalStateException();
        return addGroup(-1);
    }

    @Override
    public void removeGroup(TailGroup group) {
        if (shutdown) throw new IllegalStateException();
        groups.remove(group);
        linesChanged = true;
    }

    @Override
    public void schedule(Runnable task) {
        if (shutdown) throw new IllegalStateException();
        if (!isSupported(Output.STDOUT)) {
            return;
        }
        executor.submit(task);
    }

    @Override
    public void setRefreshRate(long interval, TimeUnit unit) {
        if (shutdown) throw new IllegalStateException();
        if (!isSupported(Output.STDOUT)) {
            return;
        }
        if (updateIntervalFuture != null) {
            updateIntervalFuture.cancel(false);
            try {
                updateIntervalFuture.get();
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }
        updateIntervalFuture = executor.scheduleAtFixedRate(this::redraw, 0, interval, unit);
    }

    void redraw() {
        if (shutdown) throw new IllegalStateException();
        boolean forceRedraw = false;
        if (terminal != null) {
            int width = terminal.getWidth();
            forceRedraw = width != -1 && lastWindowWidth != width;
            lastWindowWidth = width;
        }
        synchronized (stdoutLines) {
            if (forceRedraw || linesChanged || !stdoutLines.isEmpty()) {
                clearTails();
                if (!stdoutLines.isEmpty()) {
                    for (int i = 0; i < stdoutLines.size(); i++) {
                        String stdoutLine = stdoutLines.get(i);
                        stdOut.print(stdoutLine);
                    }
                    stdoutLines.clear();
                }
                drawTails();
                return;
            }
        }
        Ansi a = Ansi.ansi();
        int last = 0;
        boolean dirtyLines = false;
        synchronized (groups) {
            for (int j = 0, groupsSize = groups.size(); j < groupsSize; j++) {
                TailGroupImpl group = groups.get(j);
                group.tick();
            }
        }
        for (int i = tailLines.size() - 1; i >= 0; i--) {
            TailLineImpl tailLine = tailLines.get(i);
            int idx = tailLines.size() - 1 - i;
            if (tailLine.dirty) {
                int up = Math.abs(last - idx);
                if (up > 0) {
                    a.cursorUpLine(up);
                }
                a.eraseLine(Ansi.Erase.ALL).cursorToColumn(0).a(tailLine.text);
                tailLine.dirty = false;
                last = idx;
                dirtyLines = true;
            }
        }
        if (last != 0) {
            a.cursorDown(last);
        }
        if (dirtyLines) {
            a.cursorToColumn(0);
            ansiOut.print(a);
            ansiOut.flush();
        }
    }

    void rebuild() {
        if (shutdown) throw new IllegalStateException();
        if (linesChanged) {
            synchronized (lock) {
                tailLines.clear();
                synchronized (groups) {
                    for (int i = 0; i < groups.size(); i++) {
                        TailGroupImpl group = groups.get(i);
                        synchronized (group.tails) {
                            List<AbstractTail> tails = group.tails;
                            for (int j = 0; j < tails.size(); j++) {
                                AbstractTail tail = tails.get(j);
                                List<TailLineImpl> lines = tail.lines;
                                for (int k = 0; k < lines.size(); k++) {
                                    TailLineImpl line = lines.get(k);
                                    tailLines.add(line);
                                }
                            }
                        }
                    }
                }
                linesChanged = false;
            }
        }
    }

    private int getIndex(TailGroup g) {
        if (shutdown) throw new IllegalStateException();
        int idx = groups.indexOf(g);
        if (idx == -1) {
            throw new RuntimeException("Group does not exist.");
        }
        return idx;
    }

    private TailGroup addGroup(int idx) {
        if (shutdown) throw new IllegalStateException();
        TailGroupImpl group = new TailGroupImpl(this);
        synchronized (groups) {
            if (idx == -1 || (idx != 0 && idx == groups.size() - 1)) {
                groups.add(group);
            } else {
                groups.add(idx, group);
            }
        }
        return group;
    }
}
