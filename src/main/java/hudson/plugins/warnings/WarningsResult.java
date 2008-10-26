package hudson.plugins.warnings; // NOPMD

import hudson.model.AbstractBuild;
import hudson.plugins.warnings.parser.Warning;
import hudson.plugins.warnings.util.AnnotationsBuildResult;
import hudson.plugins.warnings.util.ParserResult;
import hudson.plugins.warnings.util.model.JavaProject;

/**
 * Represents the results of the warning analysis. One instance of this class is persisted for
 * each build via an XML file.
 *
 * @author Ulli Hafner
 */
public class WarningsResult extends AnnotationsBuildResult {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -137460587767210579L;

    static {
        XSTREAM.alias("warning", Warning.class);
    }

    /**
     * Creates a new instance of <code>WarningsResult</code>.
     *
     * @param build
     *            the current build as owner of this action
     * @param result
     *            the parsed result with all annotations
     */
    public WarningsResult(final AbstractBuild<?, ?> build, final ParserResult result) {
        super(build, result);
    }

    /**
     * Creates a new instance of <code>WarningsResult</code>.
     *
     * @param build
     *            the current build as owner of this action
     * @param result
     *            the parsed result with all annotations
     * @param previous
     *            the result of the previous build
     */
    public WarningsResult(final AbstractBuild<?, ?> build, final ParserResult result, final WarningsResult previous) {
        super(build, result, previous);
    }

    /**
     * Returns a summary message for the summary.jelly file.
     *
     * @return the summary message
     */
    public String getSummary() {
        return ResultSummary.createSummary(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getDetails() {
        String message = ResultSummary.createDeltaMessage(this);
        if (getNumberOfAnnotations() == 0 && getDelta() == 0) {
            message += "<li>" + Messages.Warnings_ResultAction_NoWarningsSince(getZeroWarningsSinceBuild()) + "</li>";
            message += createHighScoreMessage();
        }
        return message;
    }


    /**
     * Creates a highscore message.
     *
     * @return a highscore message
     */
    private String createHighScoreMessage() {
        if (isNewZeroWarningsHighScore()) {
            long days = getDays(getZeroWarningsHighScore());
            if (days == 1) {
                return "<li>" + Messages.Warnings_ResultAction_OneHighScore() + "</li>";
            }
            else {
                return "<li>" + Messages.Warnings_ResultAction_MultipleHighScore(days) + "</li>";
            }
        }
        else {
            long days = getDays(getHighScoreGap());
            if (days == 1) {
                return "<li>" + Messages.Warnings_ResultAction_OneNoHighScore() + "</li>";
            }
            else {
                return "<li>" + Messages.Warnings_ResultAction_MultipleNoHighScore(days) + "</li>";
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected String getSerializationFileName() {
        return "compiler-warnings.xml";
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.Warnings_ProjectAction_Name();
    }

    /**
     * Returns the results of the previous build.
     *
     * @return the result of the previous build, or <code>null</code> if no
     *         such build exists
     */
    @Override
    public JavaProject getPreviousResult() {
        WarningsResultAction action = getOwner().getAction(WarningsResultAction.class);
        if (action.hasPreviousResultAction()) {
            return action.getPreviousResultAction().getResult().getProject();
        }
        else {
            return null;
        }
    }

    /**
     * Returns whether a previous build result exists.
     *
     * @return <code>true</code> if a previous build result exists.
     */
    @Override
    public boolean hasPreviousResult() {
        return getOwner().getAction(WarningsResultAction.class).hasPreviousResultAction();
    }
}