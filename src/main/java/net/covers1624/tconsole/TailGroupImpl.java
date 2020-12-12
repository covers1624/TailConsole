package net.covers1624.tconsole;

import net.covers1624.tconsole.api.Tail;
import net.covers1624.tconsole.api.TailGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by covers1624 on 10/8/19.
 */
class TailGroupImpl implements TailGroup {

    final TailConsoleImpl console;
    final List<AbstractTail> tails = Collections.synchronizedList(new ArrayList<>());

    public TailGroupImpl(TailConsoleImpl console) {
        this.console = console;
    }

    @Override
    public <T extends Tail> T addFirst(T toAdd) {
        return addTail(0, toAdd);
    }

    @Override
    public <T extends Tail> T addBefore(Tail existing, T toAdd) {
        return addTail(indexOf((AbstractTail) existing), toAdd);
    }

    @Override
    public <T extends Tail> T addAfter(Tail existing, T toAdd) {
        return addTail(indexOf((AbstractTail) existing) + 1, toAdd);
    }

    @Override
    public <T extends Tail> T add(T toAdd) {
        return addTail(-1, toAdd);
    }

    void tick() {
        for (int i = 0, tailsSize = tails.size(); i < tailsSize; i++) {
            AbstractTail tail = tails.get(i);
            tail.tick();
        }
    }

    private int indexOf(AbstractTail t) {
        int idx = tails.indexOf(t);
        if (idx == -1) {
            throw new RuntimeException("Tail doesn't exist in group.");
        }
        return idx;
    }

    private <T extends Tail> T addTail(int idx, T t) {
        AbstractTail tail = (AbstractTail) t;
        synchronized (console.lock) {
            synchronized (tails) {
                if (tail.group != null) {
                    throw new IllegalArgumentException("Tail is already a member of a group.");
                }
                if (idx == -1 || idx == tails.size() - 1) {
                    tails.add(tail);
                } else {
                    tails.add(idx, tail);
                }
                tail.initialize(() -> {
                    console.linesChanged = true;
                    return new TailLineImpl();
                });
                tail.group = this;
            }
        }
        return t;
    }
}
