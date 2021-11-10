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
public interface LineAllocator {

    TailLine allocLine();
}
