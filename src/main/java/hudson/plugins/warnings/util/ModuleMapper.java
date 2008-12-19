package hudson.plugins.warnings.util;

import hudson.FilePath.FileCallable;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;

/**
 * Scans the workspace for maven pom.xml files and ant build.xml files and maps
 * all annotations to corresponding modules.
 *
 * @author Ulli Hafner
 */
public class ModuleMapper implements FileCallable<ParserResult> {
    /** Generated ID. */
    private static final long serialVersionUID = 5152042155205600031L;
    /** All annotations. */
    private final ParserResult result;

    /**
     * Creates a new instance of <code>CheckstyleCollector</code>.
     *
     * @param result
     *            the annotations to assign a module for
     */
    public ModuleMapper(final ParserResult result) {
        this.result = result;
    }

    /** {@inheritDoc} */
    public ParserResult invoke(final File workspace, final VirtualChannel channel) throws IOException {
        ModuleDetector detector = new ModuleDetector(workspace);
        for (FileAnnotation annotation : result.getAnnotations()) {
            annotation.setModuleName(detector.guessModuleName(annotation.getFileName()));
        }
        return result;
    }

}