/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hudson.plugins.warnings.parser;
import java.util.regex.Matcher;
import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * A parser for Cadence Incisive Enterprise Simulator
 * @author Andrew 'Necromant' Andrianov
 */

@Extension
public class CadenceIncisiveParser extends RegexpLineParser {
    private static final String SLASH = "/";
    private static final String CADENCE_MESSAGE_PATTERN = "("
            + "(^[a-zA-Z]+): \\*([a-zA-Z]),([a-zA-Z]+): (.*) \\[File:(.*), Line:(.*)\\]." //ncelab vhdl warning
             + ")|("
            + "(^[a-zA-Z]+): \\*([a-zA-Z]),([a-zA-Z]+) \\((.*),([0-9]+)\\|([0-9]+)\\): (.*)$" //Warning/error with filename
            + ")|("
            + "(^g?make\\[.*\\]: Entering directory)\\s*(['`]((.*))\\')" // make: entering directory
            + ")|("
            + "(^[a-zA-Z]+): \\*([a-zA-Z]),([a-zA-Z]+): (.*)$" //Single generic warning
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

    
    private Warning handleDirectory(final Matcher matcher, int offset) {
        directory = matcher.group(offset) + SLASH; //17
        return FALSE_POSITIVE;
    }

    
    @Override
    protected Warning createWarning(final Matcher matcher) {

        String tool, type, category, message, fileName; 
        int lineNumber = 0;
        int column = 0;


        List<String> arr = new ArrayList<String>();
        int n = matcher.groupCount();
        for (int i = 0; i<= n; i++)
            arr.add(i,matcher.group(i));
        
        Priority priority = Priority.LOW;
        
        if (matcher.group(1) != null)
        { 
            /* vhdl warning from ncelab */
            tool = matcher.group(2);
            type = matcher.group(3);
            category = matcher.group(4);
            fileName = matcher.group(6);
            lineNumber = getLineNumber(matcher.group(7));
            message = matcher.group(5);            
        } else if (matcher.group(16) != null)
        {
            /* Set current directory */
            return handleDirectory(matcher, 20);            
        } else if (matcher.group(8) != null) {
            tool = matcher.group(9);
            type = matcher.group(10);
            category = matcher.group(11);
            fileName = matcher.group(12);
            lineNumber = getLineNumber(matcher.group(13));
            priority = Priority.NORMAL;
            /* column = matcher.group(14); */
            message = matcher.group(15);
        } else if (matcher.group(21) != null) {
            tool = matcher.group(22);
            type = matcher.group(23);
            category = matcher.group(24);
            message = matcher.group(25);            
            fileName = "/NotFileRelated";
        } else {
            return FALSE_POSITIVE; /* Some shit happened ! */
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
    

}
