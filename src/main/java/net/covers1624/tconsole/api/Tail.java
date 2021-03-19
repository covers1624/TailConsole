/*
 * MIT License
 *
 * Copyright (c) 2020-2021 covers1624
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
