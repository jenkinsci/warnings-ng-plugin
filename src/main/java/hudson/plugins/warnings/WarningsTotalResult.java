package hudson.plugins.warnings;

import java.io.File;

import com.thoughtworks.xstream.XStream;

import hudson.model.AbstractBuild;

import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.warnings.parser.Warning;

/**
 * Represents the results of the whole warning analysis. One instance of this class is persisted for
 * each build via an XML file.

 *
 * @author Marvin Schütz
 * @author Sebastian Hansbauer
 */
public class WarningsTotalResult extends BuildResult{

     /** Unique identifier of this class. */
     private static final long serialVersionUID = 4572019928324067680L;

    /**
     * Creates a new instance of {@link WarningsTotalResult}.
     * @param build
     * @param history
     * @param result
     * @param defaultEncoding
     */
    public WarningsTotalResult(final AbstractBuild<?, ?> build, final BuildHistory history, final ParserResult result,
            final String defaultEncoding) {
        super(build, history, result, defaultEncoding);
    }

    @Override
    protected void configure(final XStream xstream) {
        xstream.alias("warning", Warning.class);
    }

    @Override
    public String getHeader() {
        return "warningsTotal";
    }

    @Override
    public String getSummary() {
        return "warningsTotal: " + createDefaultSummary(getUrl(), getNumberOfAnnotations(), getNumberOfModules());
    }

    private String getUrl() {
        //return "warningsTotal";
        return "warningsResult";
    }

    @Override
    protected String createDeltaMessage() {
        return createDefaultDeltaMessage(getUrl(), getNumberOfNewWarnings(), getNumberOfFixedWarnings());
    }

    @Override
    protected String getSerializationFileName() {
        return "warningsTotal.xml";
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.Warnings_ProjectAction_Name();
    }

    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType() {
        return WarningsTotalResultAction.class;
    }

    //übernommen aus WarningsResult

    private static final String FILENAME_SUFFIX = "-totalWarnings.xml";
    static final String ORIGINAL_COMPILER_WARNINGS_XML = "totalCompiler-warnings.xml";


    String getFileName(final FileChecker fileChecker, final int groupUrl) {
        String fileName = ORIGINAL_COMPILER_WARNINGS_XML;
        if (fileChecker.canRead(fileName)) {
            return fileName;
        }

        fileName = createFileName(groupUrl);
        if (fileChecker.canRead(fileName)) {
            return fileName;
        }

        //return group.replaceAll("\\W+", "") + FILENAME_SUFFIX;
        return "totalWarningsGrp";
    }

    String createFileName(final int groupUrl) {
        return "compiler-" + groupUrl + FILENAME_SUFFIX;
    }

    /**
     * Provides a way to hide file system access during testing.
     *
     * @author Ulli Hafner
     */
    static class FileChecker {
        private final File root;

        FileChecker(final File root) {
            this.root = root;
        }

        boolean canRead(final String fileName) {
            return new File(root, fileName).canRead();
        }
    }
}

