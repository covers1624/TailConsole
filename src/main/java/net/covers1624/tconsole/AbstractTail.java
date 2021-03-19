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
