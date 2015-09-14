package hudson.plugins.warnings.parser;

import javax.annotation.CheckForNull;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Sets;

import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Filters warnings by exclude and include patterns.
 *
 * @author Ullrich Hafner
 */
public class WarningsFilter {
    private final Set<Pattern> includePatterns = Sets.newHashSet();
    private final Set<Pattern> excludePatterns = Sets.newHashSet();

    private Set<Pattern> addPatterns(final @CheckForNull String pattern) {
        Set<Pattern> patterns = Sets.newHashSet();
        if (StringUtils.isNotBlank(pattern)) {
            String[] split = StringUtils.split(pattern, ',');
            for (String singlePattern : split) {
                String trimmed = StringUtils.trim(singlePattern);
                String directoriesReplaced = StringUtils.replace(trimmed, "**", "*"); // NOCHECKSTYLE
                patterns.add(Pattern.compile(StringUtils.replace(directoriesReplaced, "*", ".*"))); // NOCHECKSTYLE
            }
        }
        return patterns;
    }

    /**
     *  Filters te specified warnings by exclude and include patterns.
     *
     * @param allAnnotations
     *            all annotations
     * @param includePattern
     *            regexp pattern of files to include in report,
     *            <code>null</code> or an empty string do not filter the output
     * @param excludePattern
     *            regexp pattern of files to exclude from report,
     *            <code>null</code> or an empty string do not filter the output
     * @param messagesPattern
     *            regexp pattern of warning messages to exclude from report,
     *            <code>null</code> or an empty string do not filter the output
     * @return the filtered annotations if there is a filter defined
     */
    public Collection<FileAnnotation> apply(final Collection<FileAnnotation> allAnnotations,
                                     final @CheckForNull String includePattern,
                                     final @CheckForNull String excludePattern,
                                     final @CheckForNull String messagesPattern,
                                     final PluginLogger logger) {
        Collection<Pattern> includePatterns = addPatterns(includePattern);
        Collection<Pattern> excludePatterns = addPatterns(excludePattern);
        Collection<Pattern> messagesPatterns = addPatterns(messagesPattern);

        Collection<FileAnnotation> includedAnnotations;
        if (includePatterns.isEmpty()) {
            includedAnnotations = allAnnotations;
        }
        else {
            includedAnnotations = Sets.newHashSet();
            for (FileAnnotation annotation : allAnnotations) {
                for (Pattern include : includePatterns) {
                    if (include.matcher(annotation.getFileName()).matches()) {
                        includedAnnotations.add(annotation);
                    }
                }
            }
        }
        if (excludePatterns.isEmpty() && messagesPatterns.isEmpty()) {
            return includedAnnotations;
        }
        else {
            Set<FileAnnotation> excludedAnnotations = Sets.newHashSet(includedAnnotations);
            for (FileAnnotation annotation : includedAnnotations) {
                for (Pattern exclude : excludePatterns) {
                    if (exclude.matcher(annotation.getFileName()).matches()) {
                        excludedAnnotations.remove(annotation);
                    }
                }
                for (Pattern exclude : messagesPatterns) {
                    if (exclude.matcher(annotation.getMessage()).matches()) {
                        excludedAnnotations.remove(annotation);
                    }
                }
            }
            logger.log(String.format("Found %d warnings after exclusion.", excludedAnnotations.size()));
            return excludedAnnotations;
        }
    }

    public boolean isActive(final String includePattern, final String excludePattern, final String messagesPattern) {
        return StringUtils.isNotBlank(includePattern) || StringUtils.isNotBlank(excludePattern) || StringUtils.isNotBlank(messagesPattern);
    }
}
