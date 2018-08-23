package hudson.plugins.warnings.tokens;

import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.tokens.AbstractTokenMacro;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * Provides a token that evaluates detailed informations to the plug-in
 * build result.
 *
 * @author Benedikt Spranger
 * @deprecated replaced by classes of io.jenkins.plugins.analysis package
 */
@Deprecated
public abstract class AbstractDetailedTokenMacro extends AbstractTokenMacro {

    public AbstractDetailedTokenMacro(String tokenName, Class<? extends ResultAction<? extends BuildResult>>... resultActions) {
        super(tokenName, resultActions);
    }

    private final List<String> modules = new ArrayList<String>();
    private boolean verbose;
    private boolean showLow = true;
    private boolean showNormal = true;
    private boolean showHigh = true;
    private String linechar = "-";
    private int indent;

    @Parameter(alias = "indent")
    public void setIndent(int indentation) {
        indent = indentation;
    }

    @Parameter(alias = "modules")
    public void setModules(String module) {
        modules.add(module);
    }

    @Parameter(alias = "verbose")
    public void setVerbosity(boolean verbosity) {
        verbose = verbosity;
    }

    @Parameter(alias = "low")
    public void setLow(boolean show) {
        showLow = show;
    }

    @Parameter(alias = "normal")
    public void setNormal(boolean show) {
        showNormal = show;
    }

    @Parameter(alias = "high")
    public void setHigh(boolean show) {
        showHigh = show;
    }

    @Parameter(alias = "linechar")
    public void setLineChar(String c) {
        linechar = c.substring(0, 1);
    }

    protected String evalWarnings(final BuildResult result, Collection<FileAnnotation> warnings) {
        StringBuilder message = new StringBuilder();

        if (modules.isEmpty()) {
            modules.add("all");
        }

        for (String module : modules) {
            boolean allWarn = "all".equals(module);
            String heading = module + " annotations:";
            String tmp = "";

            for (FileAnnotation annotation : warnings) {
                Priority prio = annotation.getPriority();
                if (prio == Priority.LOW && !showLow) {
                    continue;
                }

                if (prio == Priority.NORMAL && !showNormal) {
                    continue;
                }

                if (prio == Priority.HIGH && !showHigh) {
                    continue;
                }

                if (allWarn || annotation.getType().equals(module)) {
                    if (allWarn && verbose) {
                        tmp += annotation.getType() + ": ";
                    }
                    tmp += createMessage(annotation);
                }
            }

            if (tmp.length() > 0) {
                String ind = (indent > 0) ? StringUtils.repeat(" ", indent) : "";

                message.append(ind);
                message.append(heading);
                message.append("\n");

                message.append(ind);
                message.append(StringUtils.repeat(linechar, heading.length()));
                message.append("\n");

                message.append(tmp);
                message.append("\n");
            }
        }

        return message.toString();
    }

    private String createMessage(FileAnnotation annotation) {
        String ind = (indent > 0) ? StringUtils.repeat(" ", indent) : "";
        StringBuilder message = new StringBuilder();
        message.append(ind);

        if (annotation.getPrimaryLineNumber() > 0) {
            message.append(annotation.getFileName().replaceAll("^.*workspace/", ""));
            message.append(":");
            message.append(annotation.getPrimaryLineNumber());
            message.append(" ");
        }

        message.append(annotation.getMessage().replace("<br>", "\n" + ind));
        message.append("\n");

        String toolTip;
        toolTip = annotation.getToolTip().replace("<br>", "\n");

        if (toolTip != null) {
            toolTip = ind + toolTip.replace("\n", "\n" + ind).trim();
            message.append(toolTip);
            message.append("\n");
        }

        return message.toString();
    }
}
