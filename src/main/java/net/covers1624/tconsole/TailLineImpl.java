/*
 * This file is part of TailConsole and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.tconsole;

import net.covers1624.tconsole.api.TailLine;

/**
 * Created by covers1624 on 7/12/20.
 */
class TailLineImpl implements TailLine {

    String text = "";
    boolean dirty = false;

    @Override
    public boolean set(String text) {
        if (text.equals(this.text)) {
            return false;
        }
        this.text = text;
        dirty = true;
        return true;
    }
}
