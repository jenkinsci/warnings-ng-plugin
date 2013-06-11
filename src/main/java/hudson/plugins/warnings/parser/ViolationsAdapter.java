package hudson.plugins.warnings.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jvnet.localizer.Localizable;

import com.google.common.collect.Lists;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.violations.ViolationsParser;
import hudson.plugins.violations.model.FullBuildModel;
import hudson.plugins.violations.model.FullFileModel;
import hudson.plugins.violations.model.Severity;
import hudson.plugins.violations.model.Violation;

/**
 * Adapter for warnings parsers of the violations plug-in.
 *
 * @author Ulli Hafner
 */
public class ViolationsAdapter extends AbstractWarningsParser {
    private static final long serialVersionUID = -4655802222866500913L;

    // FIXME: this will not work if the file to parse is on the slave
    private final ViolationsParser parser;

    /**
     * Creates a new instance of {@link ViolationsAdapter}.
     *
     * @param parser
     *            violations parser
     * @param parserName
     *            name of the parser
     * @param linkName
     *            name of the project action link
     * @param trendName
     *            name of the trend graph
     */
    public ViolationsAdapter(final ViolationsParser parser, final Localizable parserName, final Localizable linkName, final Localizable trendName) {
        super(parserName, linkName, trendName);

        this.parser = parser;
    }

    @Override
    public Collection<FileAnnotation> parse(final Reader reader) throws IOException, ParsingCanceledException {
        File temp = copyContentToTemporaryFile(reader);

        FullBuildModel model = new FullBuildModel();
        parser.parse(model, temp.getParentFile(), temp.getName(), null);

        return convertToWarnings(model);
    }

    private List<FileAnnotation> convertToWarnings(final FullBuildModel model) {
        List<FileAnnotation> warnings = Lists.newArrayList();
        for (FullFileModel fileModel : model.getFileModelMap().values()) {
            for (TreeSet<Violation> violations : fileModel.getTypeMap().values()) {
                for (Violation violation : violations) {
                    warnings.add(new Warning(getPath(fileModel),
                            violation.getLine(), getGroup(), StringUtils.EMPTY,
                            violation.getMessage(), convertSeverity(violation.getSeverity())));
                }
            }
        }
        return warnings;
    }

    private String getPath(final FullFileModel fileModel) {
        File sourceFile = fileModel.getSourceFile();
        if (sourceFile != null) {
            return sourceFile.getPath();
        }
        return fileModel.getDisplayName();
    }

    private Priority convertSeverity(final String severity) {
        int level = Severity.getSeverityLevel(severity);
        if (level < 2) {
            return Priority.HIGH;
        }
        if (level < 4) {
            return Priority.NORMAL;
        }
        return Priority.LOW;
    }

    private File copyContentToTemporaryFile(final Reader reader) throws IOException, FileNotFoundException {
        File temp = File.createTempFile("warnings", "log");
        temp.deleteOnExit();
        FileOutputStream output = new FileOutputStream(temp);
        try {
            IOUtils.copy(reader, output, "UTF-8");
        }
        finally {
            IOUtils.closeQuietly(output);
        }
        return temp;
    }
}

