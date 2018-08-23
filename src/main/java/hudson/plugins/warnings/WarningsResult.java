package hudson.plugins.warnings; // NOPMD

import java.io.File;

import com.thoughtworks.xstream.XStream;

import hudson.model.Run;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.warnings.parser.ParserRegistry;
import hudson.plugins.warnings.parser.Warning;

/**
 * Represents the results of the warning analysis. One instance of this class is persisted for
 * each build via an XML file.
 *
 * @author Ullrich Hafner
 * @deprecated replaced by classes of io.jenkins.plugins.analysis package
 */
@Deprecated
public class WarningsResult extends BuildResult {
    private static final String FILENAME_SUFFIX = "-warnings.xml";
    /** Version < 4.0 file name of warnings. */
    static final String ORIGINAL_COMPILER_WARNINGS_XML = "compiler-warnings.xml";
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -137460587767210579L;
    /** The group of the parser. @since 4.0 */
    private final String group;

    /**
     * Creates a new instance of {@link WarningsResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param history
     *            the build history
     * @param result
     *            the parsed result with all annotations
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param group
     *            the parser group this result belongs to
     */
    public WarningsResult(final Run<?, ?> build, final BuildHistory history,
                          final ParserResult result, final String defaultEncoding, final String group) {
        this(build, history, result, defaultEncoding, group, group == null ? false : true);
    }

    WarningsResult(final Run<?, ?> build, final BuildHistory history,
                   final ParserResult result, final String defaultEncoding,
                   final String group, final boolean canSerialize) {
        super(build, history, result, defaultEncoding);

        this.group = group;
        if (canSerialize) {
            serializeAnnotations(result.getAnnotations());
        }
    }

    @Override
    protected BuildHistory createHistory(final Run<?, ?> build) {
        return new WarningsBuildHistory(build, group, false, false);
    }

    @Override
    protected void configure(final XStream xstream) {
        xstream.alias("warning", Warning.class);
    }

    @Override
    public String getHeader() {
        return ParserRegistry.getParser(group).getLinkName().toString();
    }

    @Override
    public String getSummary() {
        return ParserRegistry.getParser(group).getLinkName() + ": "
                + createDefaultSummary(getUrl(), getNumberOfAnnotations(), getNumberOfModules());
    }

    @Override
    protected String createDeltaMessage() {
        return createDefaultDeltaMessage(getUrl(), getNumberOfNewWarnings(), getNumberOfFixedWarnings());
    }

    private String getUrl() {
        return WarningsDescriptor.getResultUrl(group);
    }

    @Override
    protected String getSerializationFileName() {
        FileChecker fileChecker = new FileChecker(getOwner().getRootDir());

        return getFileName(fileChecker, group == null ? 0 : ParserRegistry.getUrl(group));
    }

    String getFileName(final FileChecker fileChecker, final int groupUrl) {
        String fileName = ORIGINAL_COMPILER_WARNINGS_XML;
        if (fileChecker.canRead(fileName)) {
            return fileName;
        }

        fileName = createFileName(groupUrl);
        if (fileChecker.canRead(fileName)) {
            return fileName;
        }

        return group.replaceAll("\\W+", "") + FILENAME_SUFFIX;
    }

    String createFileName(final int groupUrl) {
        return "compiler-" + groupUrl + FILENAME_SUFFIX;
    }

    @Override
    public String getDisplayName() {
        if (group == null) {
            return Messages.Warnings_ProjectAction_Name();
        }
        else {
            return ParserRegistry.getParser(group).getLinkName().toString();
        }
    }

    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType() {
        return WarningsResultAction.class;
    }

    /**
     * Provides a way to hide file system access during testing.
     *
     * @author Ullrich Hafner
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