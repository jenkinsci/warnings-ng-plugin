package io.jenkins.plugins.analysis.core.util;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool.StaticAnalysisToolDescriptor;

/**
 * A simple tool to derive a warning parser Symbol name
 *
 * @author Ullrich Hafner
 */
public class SymbolNameGenerator {
    /**
     * Get the Symbol name for a parser
     *
     * @author Jeremy Marshall
     */
    public static String getSymbolName(Class<? extends StaticAnalysisTool> tool) {
        String name = tool.getSimpleName();
        char c[] = name.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }
}
