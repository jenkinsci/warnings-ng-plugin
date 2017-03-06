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

    private Set<Pattern> addFilePatterns(final @CheckForNull String pattern) {
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

    private Set<Pattern> addStringPatterns(final @CheckForNull String pattern) {
        Set<Pattern> patterns = Sets.newHashSet();
        if (StringUtils.isNotBlank(pattern)) {
            String[] split = StringUtils.split(pattern, '\n');
            for (String singlePattern : split) {
                String trimmed = StringUtils.trim(singlePattern);
                patterns.add(Pattern.compile(trimmed)); // NOCHECKSTYLE
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
                                     final @CheckForNull String categoriesPattern,
                                     final PluginLogger logger) {
        Collection<Pattern> includePatterns = addFilePatterns(includePattern);
        Collection<Pattern> excludePatterns = addFilePatterns(excludePattern);
        Collection<Pattern> messagesPatterns = addStringPatterns(messagesPattern);
        Collection<Pattern> categoriesPatterns = addStringPatterns(categoriesPattern);

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
        if (excludePatterns.isEmpty() && messagesPatterns.isEmpty() && categoriesPatterns.isEmpty()) {
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
                for (Pattern exclude : categoriesPatterns) {
                    if (exclude.matcher(annotation.getCategory()).matches()) {
                        excludedAnnotations.remove(annotation);
                    }
                }
            }
            logger.log(String.format("Found %d warnings after exclusion.", excludedAnnotations.size()));
            return excludedAnnotations;
        }
    }

    public boolean isActive(final String includePattern, final String excludePattern, final String messagesPattern, final String categoriesPattern) {
        return StringUtils.isNotBlank(includePattern) || StringUtils.isNotBlank(excludePattern) || StringUtils.isNotBlank(messagesPattern) || StringUtils.isNotBlank(categoriesPattern);
    }
}
