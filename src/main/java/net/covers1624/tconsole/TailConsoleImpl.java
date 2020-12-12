package net.covers1624.tconsole;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.covers1624.tconsole.api.TailConsole;
import net.covers1624.tconsole.api.TailGroup;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiPrintStream;
import org.fusesource.jansi.FilterPrintStream;
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

    private static final ThreadFactory factory = new ThreadFactoryBuilder()//
            .setDaemon(true)//
            .setNameFormat("TailConsole Executor")//
            .build();

    private final List<TailLineImpl> tailLines = new ArrayList<>();
    private final List<TailGroupImpl> groups = Collections.synchronizedList(new ArrayList<>());

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(factory);

    private final PrintStream stdOut;
    private final PrintStream stdErr;

    private final PrintStream ansiOut;
    private final PrintStream ansiErr;

    private final boolean[] isatty = new boolean[2];
    final Terminal terminal;

    private ScheduledFuture<?> updateIntervalFuture = null;
    final Object lock = new Object();
    boolean linesChanged = false;
    long lastFullRedraw = 0;

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
        stdOut = AnsiConsole.system_out;
        stdErr = AnsiConsole.system_err;
    }

    @Override
    public PrintStream getAnsiOut() {
        return ansiOut;
    }

    @Override
    public PrintStream getAnsiErr() {
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
        return isatty[output.ordinal()];
    }

    @Override
    public void clearTails() {
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
        ansiOut.print(a);
        ansiOut.flush();
    }

    @Override
    public List<TailGroup> getTailGroups() {
        return Collections.unmodifiableList(groups);
    }

    @Override
    public TailGroup newGroupFirst() {
        return addGroup(0);
    }

    @Override
    public TailGroup newGroupBefore(TailGroup other) {
        return addGroup(getIndex(other));
    }

    @Override
    public TailGroup newGroupAfter(TailGroup other) {
        return addGroup(getIndex(other) + 1);
    }

    @Override
    public TailGroup newGroup() {
        return addGroup(-1);
    }

    @Override
    public void removeGroup(TailGroup group) {
        groups.remove(group);
        linesChanged = true;
    }

    @Override
    public void schedule(Runnable task) {
        if (!isSupported(Output.STDOUT)) {
            return;
        }
        executor.submit(task);
    }

    @Override
    public void setRefreshRate(long interval, TimeUnit unit) {
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
        long now = System.currentTimeMillis();
        if (linesChanged /*|| lastFullRedraw + 1000 <= now*/) {
            clearTails();
            drawTails();
            lastFullRedraw = now;
            return;
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
        int idx = groups.indexOf(g);
        if (idx == -1) {
            throw new RuntimeException("Group does not exist.");
        }
        return idx;
    }

    private TailGroup addGroup(int idx) {
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
