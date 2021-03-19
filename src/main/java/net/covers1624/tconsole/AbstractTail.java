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

package net.covers1624.tconsole;

import net.covers1624.tconsole.api.Tail;
import net.covers1624.tconsole.api.TailGroup;
import org.fusesource.jansi.Ansi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by covers1624 on 7/12/20.
 */
public abstract class AbstractTail implements Tail {

    final List<TailLineImpl> lines = new ArrayList<>();
    protected final int numLines;
    TailGroupImpl group;

    protected AbstractTail(int numLines) {
        this.numLines = numLines;
    }

    final void initialize(LineAllocator allocator) {
        for (int i = 0; i < numLines; i++) {
            lines.add((TailLineImpl) allocator.allocLine());
        }
        onInitialized();
    }

    public boolean setLine(int line, String text) {
        return lines.get(line).set(text);
    }

    public boolean setLine(int line, Ansi ansi) {
        return lines.get(line).set(ansi);
    }

    public int getTerminalWidth() {
        return group.console.terminal.getWidth();
    }

    @Override
    public TailGroup getGroup() {
        return group;
    }

    @Override
    public final <T extends Tail> T addBefore(T toAdd) {
        return group.addBefore(this, toAdd);
    }

    @Override
    public final <T extends Tail> T addAfter(T toAdd) {
        return group.addAfter(this, toAdd);
    }
}
