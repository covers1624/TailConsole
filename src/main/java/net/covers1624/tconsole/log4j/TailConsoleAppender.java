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
import org.checkerframework.checker.nullness.qual.Nullable;

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
    public static TailConsoleAppender createAppender(//
            @Required (message = "No name provided for AnsiTailConsoleAppender")//
            @PluginAttribute ("name")//
                    String name,//
            @PluginElement ("Filter")//
                    Filter filter,//l
            @PluginElement ("Layout")//
            @Nullable//
                    Layout<? extends Serializable> layout,//
            @PluginAttribute (value = "ignoreExceptions", defaultBoolean = true)//
                    boolean ignoreExceptions) {
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        return new TailConsoleAppender(name, filter, layout, ignoreExceptions);
    }
}
