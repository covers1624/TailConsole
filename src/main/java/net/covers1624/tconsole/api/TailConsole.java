/*
 * This file is part of TailConsole and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.tconsole.api;

import net.covers1624.tconsole.ConsumingOutputStream;
import net.covers1624.tconsole.TailConsoleImpl;
import org.fusesource.jansi.internal.CLibrary;

import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by covers1624 on 10/9/19.
 */
public interface TailConsole {

    static TailConsole create() {
        TailConsoleImpl impl = new TailConsoleImpl();
        System.setOut(new PrintStream(new ConsumingOutputStream(l -> impl.schedule(() -> {
            impl.clearTails();
            impl.getStdOut().println(l);
            impl.drawTails();
        }))));
        System.setErr(new PrintStream(new ConsumingOutputStream(l -> impl.schedule(() -> {
            impl.clearTails();
            impl.getStdErr().println(l);
            impl.drawTails();
        }))));

        return impl;
    }

    /**
     * The STDOUT PrintStream capable of handling ANSI escape codes.
     *
     * @return The PrintStream.
     */
    PrintStream getAnsiOut();

    /**
     * The STDERR PrintStream capable of handling ANSI escape codes.
     *
     * @return The PrintStream.
     */
    PrintStream getAnsiErr();

    /**
     * The original STDOUT PrintStream {@link TailConsole} delegates to.
     *
     * @return The PrintStream.
     */
    PrintStream getStdOut();

    /**
     * The original STDERR PrintStream {@link TailConsole} delegates to.
     *
     * @return The PrintStream.
     */
    PrintStream getStdErr();

    /**
     * Checks if the given {@link Output} supports ANSI escape codes.
     *
     * @param output The output to check.
     * @return If ANSI escape codes are supported.
     */
    boolean isSupported(Output output);

    /**
     * Clears all the tails currently printed on the screen.
     * WARN: This method is not synchronised to the TailConsole's Execution thread,
     * please use with {@link #schedule}
     */
    void clearTails();

    /**
     * Re draws all the tails to the screen.
     * WARN: This method is not synchronised to the TailConsole's Execution thread,
     * please use with {@link #schedule}
     */
    void drawTails();

    /**
     * Schedules a line to be printed to STDOUT.
     *
     * @param line The line.
     */
    void scheduleStdout(String line);

    /**
     * Stops everything and uninstalls TailConsole.
     */
    void shutdown();

    /**
     * Stops everything and uninstalls TailConsole.
     */
    boolean isShutdown();

    /**
     * Gets an unmodifiable list of all {@link TailGroup}'s currently registered.
     *
     * @return The groups.
     */
    List<TailGroup> getTailGroups();

    /**
     * Creates a new {@link TailGroup} at the start of the {@link TailConsole}.
     *
     * @return The new group.
     */
    TailGroup newGroupFirst();

    /**
     * Creates a new {@link TailGroup} directly before the given {@link TailGroup}.
     *
     * @param other The group to add the new one before.
     * @return The new group.
     */
    TailGroup newGroupBefore(TailGroup other);

    /**
     * Creates a new {@link TailGroup} directly after the given {@link TailGroup}.
     *
     * @param other The group to add the new one after.
     * @return The new group.
     */
    TailGroup newGroupAfter(TailGroup other);

    /**
     * Creates a new {@link TailGroup} at the end of the {@link TailConsole}.
     *
     * @return The new group.
     */
    TailGroup newGroup();

    /**
     * Removes the given group from the {@link TailConsole}.
     *
     * @param group The group to remove.
     */
    void removeGroup(TailGroup group);

    /**
     * Schedules a task to be run on stdout IO Executor.
     *
     * @param task The task to run.
     */
    void schedule(Runnable task);

    /**
     * Sets the rate at which the console will attempt to re-draw changed Tail lines.
     *
     * @param interval The interval between 'frames'.
     * @param unit     The {@link TimeUnit} of the interval.
     */
    void setRefreshRate(long interval, TimeUnit unit);

    enum Output {
        STDOUT(CLibrary.STDOUT_FILENO),
        STDERR(CLibrary.STDERR_FILENO);

        private final int fileno;

        Output(int fileno) {
            this.fileno = fileno;
        }

        public int getFileno() {
            return fileno;
        }
    }

}
