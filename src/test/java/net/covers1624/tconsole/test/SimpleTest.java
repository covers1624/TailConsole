/*
 * This file is part of TailConsole and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.tconsole.test;

import net.covers1624.tconsole.api.TailConsole;
import net.covers1624.tconsole.api.TailGroup;
import net.covers1624.tconsole.tails.TextTail;

/**
 * Created by covers1624 on 10/11/21.
 */
public class SimpleTest {

    public static void main(String[] args) throws Throwable {
        TailConsole console = TailConsole.create();
        TailGroup group = console.newGroup();
        TextTail tail = group.add(new TextTail(1));
        tail.setLine(0, "This is text!");
        int i = 0;
        while (true) {
            Thread.sleep(1000);
            System.out.println("Message: " + i++);
        }
    }
}
