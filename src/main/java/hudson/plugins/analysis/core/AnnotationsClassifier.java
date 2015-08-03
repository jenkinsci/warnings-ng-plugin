package hudson.plugins.analysis.core;

import java.io.File;
import java.io.IOException;

import jenkins.MasterToSlaveFileCallable;

import hudson.plugins.analysis.util.ContextHashCode;
import hudson.plugins.analysis.util.model.FileAnnotation;

import hudson.remoting.VirtualChannel;

/**
 * Reads the content of each file with warnings and creates a unique hash code
 * of the warning to enable a more flexible new and fixed warnings detection.
 *
 * @author Ulli Hafner
 */
public class AnnotationsClassifier extends MasterToSlaveFileCallable<ParserResult> {
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

    @Override
    public ParserResult invoke(final File workspace, final VirtualChannel channel) throws IOException {
        ContextHashCode contextHashCode = new ContextHashCode();
        for (FileAnnotation annotation : result.getAnnotations()) {
            try {
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