/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hudson.plugins.warnings.parser;
import java.util.regex.Matcher;
import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;

/**
 *
 * A parser for Cadence Incisive Enterprise Simulator
 * @author Andrew 'Necromant' Andrianov
 */

@Extension
public class CadenceIncisiveParser extends RegexpLineParser {
    private static final String SLASH = "/";
    private static final String CADENCE_MESSAGE_PATTERN = "("
            + "(^[a-zA-Z]+): \\*([a-zA-Z]),([a-zA-Z]+): (.*)$" //Single warning
            + ")|("
            + "(^[a-zA-Z]+): \\*([a-zA-Z]),([a-zA-Z]+) \\((.*),([0-9]+)\\|([0-9]+)\\): (.*)$" //Warning/error with filename
            + ")|("
            + "(^g?make\\[.*\\]: Entering directory)\\s*(['`]((.*))\\')" // make: entering directory
            + ")";
    private String directory = "";
    
    /**
     * Creates a new instance of {@link CadenceIncisiveParser}.
     */
    public CadenceIncisiveParser() {
        super(Messages._Warnings_CadenceIncisive_ParserName(),
                Messages._Warnings_CadenceIncisive_LinkName(),
                Messages._Warnings_CadenceIncisive_TrendName(),
                CADENCE_MESSAGE_PATTERN);
    }

    @Override
    protected String getId() {
        return "Cadence Incisive Enterprise Simulator";
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {

        String tool, type, category, message, fileName; 
        int lineNumber = 0;
        int column = 0;
        Priority priority = Priority.LOW;
        
        if (matcher.group(14) != null)
        {
            /* Set current directory */
            return handleDirectory(matcher);            
        } else if (matcher.group(6) != null) {
            tool = matcher.group(7);
            type = matcher.group(8);
            category = matcher.group(9);
            fileName = matcher.group(10);
            lineNumber = getLineNumber(matcher.group(11));
            /* column = matcher.group(12); */
            message = matcher.group(13);
        } else {
            tool = matcher.group(2);
            type = matcher.group(3);
            category = matcher.group(4);
            message = matcher.group(5);            
            fileName = "/NotFileRelated";
        }
         

        if (category.equalsIgnoreCase("E")) {
            priority = Priority.HIGH;
            category = "Error (" + tool + "): " + category;
        } else {
            priority = Priority.LOW;
            category = "Warning (" + tool + "): " + category;
        }

        if (fileName.startsWith(SLASH)) {
            return createWarning(fileName, lineNumber, category, message, priority);
        } else {
            return createWarning(directory + fileName, lineNumber, category, message, priority);
        }
    }
    
private Warning handleDirectory(final Matcher matcher) {
        directory = matcher.group(17) + SLASH;
        return FALSE_POSITIVE;
    }
}
