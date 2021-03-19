package net.covers1624.tconsole.api;

/**
 * Created by covers1624 on 10/8/19.
 */
public interface Tail {

    /**
     * @return Gets the group this tail belongs to.
     */
    TailGroup getGroup();

    /**
     * Adds a new tail before this tail.
     *
     * @param toAdd The tail to add.
     * @return The added tail.
     */
    <T extends Tail> T addBefore(T toAdd);

    /**
     * Adds a new tail after this tail.
     *
     * @param toAdd The tail to add.
     * @return The added tail.
     */
    <T extends Tail> T addAfter(T toAdd);

    /**
     * Called on the {@link TailConsole} execution thread, before this tails lines are re-drawn.
     */
    default void tick() { }

    /**
     * Called when this Tail is initialized and all Lines have been allocated.
     * <p>
     * This can be used for tails that have static lines, or defaults of some kind.
     */
    default void onInitialized() { }
}
