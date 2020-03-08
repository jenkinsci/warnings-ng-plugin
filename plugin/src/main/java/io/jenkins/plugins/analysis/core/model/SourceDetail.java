package io.jenkins.plugins.analysis.core.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.lang3.exception.ExceptionUtils;

import edu.hm.hafner.analysis.Issue;

import hudson.model.ModelObject;
import hudson.model.Run;

/**
 * Renders a source file containing an issue for the whole file or a specific line number.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class SourceDetail implements ModelObject {
    private final Run<?, ?> owner;
    private final String baseName;
    private final String sourceCode;

    /**
     * Creates a new instance of this source code object.
     *
     * @param owner
     *         the current build as owner of this view
     * @param affectedFile
     *         the file to show
     * @param issue
     *         the issue to show in the source file
     * @param description
     *         a detailed description of the specified issue
     * @param iconUrl
     *         absolute URL to the small icon of the static analysis tool
     */
    public SourceDetail(final Run<?, ?> owner, final Reader affectedFile, final Issue issue, final String description,
            final String iconUrl) {
        this.owner = owner;

        baseName = issue.getBaseName();
        sourceCode = render(affectedFile, issue, description, iconUrl);
    }

    private String render(final Reader affectedFile, final Issue issue, final String description,
            final String iconUrl) {
        try (BufferedReader reader = new BufferedReader(affectedFile)) {
            SourcePrinter sourcePrinter = new SourcePrinter();
            return sourcePrinter.render(reader.lines(), issue, description, iconUrl);
        }
        catch (IOException e) {
            return String.format("%s%n%s", ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public String getDisplayName() {
        return baseName;
    }

    /**
     * Returns the build as owner of this view.
     *
     * @return the build
     */
    @SuppressWarnings("unused") // Called by jelly view to show the side panel
    public Run<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the colorized source code.
     *
     * @return the source code
     */
    public String getSourceCode() {
        return sourceCode;
    }
}

