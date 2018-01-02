package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.util.NoSuchElementException;
import jenkins.model.Jenkins;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.console.ConsoleNote;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * Describes a static analysis tool that reports issues.
 *
 * @author Ullrich Hafner
 */
public abstract class StaticAnalysisTool extends AbstractDescribableImpl<StaticAnalysisTool>
        implements IssueParser, ExtensionPoint {
    /**
     * Finds the static analysis tool with the specified ID.
     *
     * @param id
     *         the ID of the tool to find
     *
     * @return the static analysis tool
     * @throws NoSuchElementException
     *         if the tool could not be found
     */
    private static StaticAnalysisLabelProvider find(final String id) {
        if (DefaultLabelProvider.STATIC_ANALYSIS_ID.equals(id)) {
            return new DefaultLabelProvider();
        }
        for (StaticAnalysisToolDescriptor toolDescriptor : all()) {
            if (toolDescriptor.getId().equals(id)) {
                return toolDescriptor.getLabelProvider();
            }
        }
        throw new NoSuchElementException("No static analysis tool found with ID %s.", id);
    }

    /**
     * Finds the static analysis tool with the specified ID.
     *
     * @param id
     *         the ID of the tool to find
     * @param name
     *         the name of the tool (might be empty or null)
     *
     * @return the static analysis tool
     * @throws NoSuchElementException
     *         if the tool could not be found
     */
    public static StaticAnalysisLabelProvider find(final String id, @CheckForNull final String name) {
        if (StringUtils.isBlank(name)) {
            return find(id);
        }
        else {
            return new DefaultLabelProvider(id, name);
        }
    }

    private static DescriptorExtensionList<StaticAnalysisTool, StaticAnalysisToolDescriptor> all() {
        return Jenkins.getInstance().getDescriptorList(StaticAnalysisTool.class);
    }

    private String defaultEncoding;

    @CheckForNull
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Sets the default encoding used to read files (warnings, source code, etc.).
     *
     * @param defaultEncoding
     *         the encoding, e.g. "ISO-8859-1"
     */
    @DataBoundSetter
    public void setDefaultEncoding(final String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    @Override
    public String toString() {
        return String.format("[%s] Encoding: %s", getName(), defaultEncoding);
    }

    /**
     * Returns the name of this tool.
     *
     * @return the name of this tool
     */
    public String getName() {
        return getDescriptor().getDisplayName();
    }

    @Override
    public String getId() {
        return getDescriptor().getId();
    }

    /**
     * Parses the specified file for issues.
     *
     * @param parser
     *         the parser to use
     * @param file
     *         the file to parse
     * @param charset
     *         the encoding to use when reading files
     * @param builder
     *         the issue builder to use
     *
     * @return the parsed issues
     * @throws ParsingException
     *         Signals that during parsing a non recoverable error has been occurred
     * @throws ParsingCanceledException
     *         Signals that the parsing has been aborted by the user
     */
    public Issues<Issue> parse(final AbstractParser parser, final File file, final Charset charset, final IssueBuilder builder)
            throws ParsingException, ParsingCanceledException {
        try (Reader input = createReader(new FileInputStream(file), charset)) {
            parser.setTransformer(line -> ConsoleNote.removeNotes(line));
            Issues<Issue> issues = parser.parse(input, builder);
            issues.log("Successfully parsed '%s': found %d issues (tool ID = %s)",
                    file.getAbsolutePath(), issues.getSize(), getId());
            if (issues.getDuplicatesSize() == 1) {
                issues.log("Note: one issue has been dropped since it is a duplicate");
            }
            else if (issues.getDuplicatesSize() > 1) {
                issues.log("Note: %d issues have been dropped since they are duplicates",
                        issues.getDuplicatesSize());
            }
            return issues;
        }
        catch (FileNotFoundException exception) {
            throw new ParsingException(exception, "Can't find file: " + file.getAbsolutePath());
        }
        catch (IOException exception) {
            throw new ParsingException(exception, "Can't scan file for issues: " + file.getAbsolutePath());
        }
    }

    private Reader createReader(final InputStream inputStream, final Charset charset) {
        return new InputStreamReader(new BOMInputStream(inputStream), charset);
    }

    /** Descriptor for {@link StaticAnalysisTool}. **/
    public abstract static class StaticAnalysisToolDescriptor extends Descriptor<StaticAnalysisTool> {
        private final StaticAnalysisLabelProvider labelProvider;

        /**
         * Creates a new {@link StaticAnalysisToolDescriptor} with the specified label provider.
         *
         * @param labelProvider
         *         the label provider to use
         */
        protected StaticAnalysisToolDescriptor(final StaticAnalysisLabelProvider labelProvider) {
            this.labelProvider = labelProvider;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return labelProvider.getName();
        }

        @Override
        public String getId() {
            return labelProvider.getId();
        }

        /**
         * Returns the associated label provider for this tool.
         *
         * @return the label provider
         */
        public StaticAnalysisLabelProvider getLabelProvider() {
            return labelProvider;
        }
    }
}
