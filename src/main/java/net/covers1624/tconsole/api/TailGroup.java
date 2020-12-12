package net.covers1624.tconsole.api;

/**
 * Created by covers1624 on 10/8/19.
 */
public interface TailGroup {

    /**
     * Adds a {@link Tail} to the start of this Group.
     *
     * @param toAdd The Tail to add.
     * @return The added tail.
     */
    <T extends Tail> T addFirst(T toAdd);

    /**
     * Adds a {@link Tail} after the specified {@link Tail}.
     *
     * @param existing The tail to add before.
     * @param toAdd    The Tail to add.
     * @return The added tail.
     */
    <T extends Tail> T addBefore(Tail existing, T toAdd);

    /**
     * Adds a {@link Tail} after the specified {@link Tail}.
     *
     * @param existing The tail to add after.
     * @param toAdd    The Tail to add.
     * @return The added tail.
     */
    <T extends Tail> T addAfter(Tail existing, T toAdd);

    /**
     * Adds a {@link Tail} to the end of this Group.
     *
     * @param toAdd The Tail to add.
     * @return The added tail.
     */
    <T extends Tail> T add(T toAdd);

}
