package hudson.plugins.warnings;

import hudson.FilePath.FileCallable;
import hudson.plugins.warnings.util.ContextHashCode;
import hudson.plugins.warnings.util.ModuleDetector;
import hudson.plugins.warnings.util.ParserResult;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

/**
 * Scans the workspace for maven pom.xml files and ant build.xml files and maps
 * all annotations to corresponding modules. Additionally, the content of each
 * file with warnings is read and a hash code of the warning is created to
 * enable a more flexible new and fixed warnings detection.
 *
 * @author Ulli Hafner
 */
public class AnnotationsClassifier implements FileCallable<ParserResult> {
    /** Generated ID. */
    private static final long serialVersionUID = 5152042155205600031L;
    /** All annotations. */
    private final ParserResult result;
    /** The default encoding to be used when reading and parsing files. */
    private final String defaultEncoding;

    /**
     * Creates a new instance of {@link AnnotationsClassifier}.
     *
     * @param result
     *            the annotations to assign a module for
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    public AnnotationsClassifier(final ParserResult result, final String defaultEncoding) {
        this.result = result;
        this.defaultEncoding = defaultEncoding;
    }

    /** {@inheritDoc} */
    public ParserResult invoke(final File workspace, final VirtualChannel channel) throws IOException {
        ModuleDetector detector = new ModuleDetector(workspace);
        ContextHashCode contextHashCode = new ContextHashCode();
        for (FileAnnotation annotation : result.getAnnotations()) {
            try {
                if (StringUtils.isBlank(annotation.getModuleName())) {
                    annotation.setModuleName(detector.guessModuleName(annotation.getFileName()));
                }
                annotation.setContextHashCode(contextHashCode.create(
                        annotation.getFileName(), annotation.getPrimaryLineNumber(), defaultEncoding));
            }
            catch (IOException exception) {
                // ignore and continue
            }
        }
        return result;
    }

}