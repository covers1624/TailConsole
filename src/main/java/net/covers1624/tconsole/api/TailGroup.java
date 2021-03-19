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
