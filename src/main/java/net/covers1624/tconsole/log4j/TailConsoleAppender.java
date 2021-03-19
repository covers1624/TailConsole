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

package net.covers1624.tconsole.log4j;

import net.covers1624.tconsole.api.TailConsole;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.PrintStream;
import java.io.Serializable;

/**
 * Created by covers1624 on 9/8/19.
 */
@Plugin (name = "TailConsoleAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class TailConsoleAppender extends AbstractAppender {

    private static final PrintStream sysOut = System.out;
    private static boolean initialized;
    private static TailConsole tailConsole;

    protected TailConsoleAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
        if (!initialized) {
            initialized = true;
            tailConsole = TailConsole.create();
        }
    }

    public static TailConsole getTailConsole() {
        return tailConsole;
    }

    @Override
    public void append(LogEvent event) {
        String str = getLayout().toSerializable(event).toString();
        if (tailConsole != null && !tailConsole.isShutdown() && tailConsole.isSupported(TailConsole.Output.STDOUT)) {
            tailConsole.scheduleStdout(str);
        } else {
            sysOut.print(str);
        }
    }

    @PluginFactory
    public static TailConsoleAppender createAppender(
            @Required (message = "No name provided for AnsiTailConsoleAppender")
            @PluginAttribute ("name") String name,
            @PluginElement ("Filter") Filter filter,
            @PluginElement ("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute (value = "ignoreExceptions", defaultBoolean = true) boolean ignoreExceptions) {
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        return new TailConsoleAppender(name, filter, layout, ignoreExceptions);
    }
}
