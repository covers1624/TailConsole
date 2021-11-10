/*
 * This file is part of TailConsole and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.tconsole.log4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by covers1624 on 7/12/20.
 */
public class Log4jUtils {

    public static void redirectStreams() {
        Logger stdout = LogManager.getLogger("STDOUT");
        Logger stderr = LogManager.getLogger("STDERR");
        System.setOut(new TracingPrintStream(stdout));
        System.setErr(new TracingPrintStream(stderr));
    }

}
