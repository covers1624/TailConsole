package net.covers1624.tconsole.api;

import org.fusesource.jansi.Ansi;

/**
 * Created by covers1624 on 11/9/19.
 */
public interface TailLine {

    /**
     * Sets this tail line's content.
     *
     * @param ansi The {@link Ansi} content.
     */
    //TODO, this should take a custom Ansi class to ensure each line stays within the screen width.
    default boolean set(Ansi ansi) {
        return set(ansi.toString());
    }

    boolean set(String text);
}
