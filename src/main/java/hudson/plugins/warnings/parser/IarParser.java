package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;
import java.util.*;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the IAR C/C++ compiler warnings. Note, that since release 4.1
 * this parser requires that IAR compilers are started with option
 * '----no_wrap_diagnostics'. Then the IAR compilers will create single-line
 * warnings.
 *
 * @author Claus Klein
 * @author Ulli Hafner
 * @author Kay van der Zander
 */
@Extension
public class IarParser extends RegexpLineParser {
    private static final long serialVersionUID = 7695540852439013425L;
    private static int GROUP_NUMBER = 5;
    
    // search for: Fatal Error[Pe1696]: cannot open source file "c:\filename.c"
    // search for: c:\filename.h(17) : Fatal Error[Pe1696]: cannot open source file "System/ProcDef_LPC17xx.h"
    private static final String IAR_WARNING_PATTERN = 
"((\\[exec\\] )?(.*)\\((\\d+)\\)?.*)?(Fatal [Ee]rror|Remark|Warning)\\[(\\w+)\\]: (.*(\\\".*(c|h)\\\")|.*)";
    /**
     * Creates a new instance of {@link IarParser}.
     */
    public IarParser() {
        super(Messages._Warnings_iar_ParserName(),
                Messages._Warnings_iar_LinkName(),
                Messages._Warnings_iar_TrendName(),
                IAR_WARNING_PATTERN);
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("Warning") || line.contains("rror") || line.contains("Remark") || line.contains("[");
    }

    @Override
    protected String getId() {
        return "IAR compiler (C/C++)";
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority;
           
        priority = determinePriority(matcher.group(GROUP_NUMBER));
        return composeWarning(matcher, priority);
    }
           
    private Warning composeWarning(final Matcher matcher, final Priority priority) {
        String message = matcher.group(7);
        
        if( matcher.group(3) == null ) {
            // createWarning( filename, line number, error number (Pe177), message, priority )
            return createWarning(matcher.group(8), 0, matcher.group(6), message, priority);
        }
        // createWarning( filename, line number, error number (Pe177), message, priority )
        return createWarning(matcher.group(3), getLineNumber(matcher.group(4)), matcher.group(6), message, priority);
    }
          
    private Priority determinePriority(final String message) {
        // for "Fatal error", "Fatal Error", "Error" and "error" and "warning"
        if (message.toLowerCase().contains("error")) {
            return Priority.HIGH;
        } else if (message.toLowerCase().contains("warning")) {
            return Priority.NORMAL;
        } else {
            return Priority.LOW;
        }
    }
}
