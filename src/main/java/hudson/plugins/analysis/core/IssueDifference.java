package hudson.plugins.analysis.core;

import javax.annotation.CheckForNull;
import java.util.HashSet;
import java.util.Set;

import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Computes old, new, and fixed issues based on two set of issues.
 *
 * @author Ulli Hafner
 */
public class IssueDifference {
    private final Set<FileAnnotation> newIssues;
    private final Set<FileAnnotation> fixedIssues;

    public IssueDifference(final Set<FileAnnotation> currentIssues, final Set<FileAnnotation> referenceIssues) {
        newIssues = new HashSet<>(currentIssues);
        fixedIssues = new HashSet<>(referenceIssues);
        
        Set<FileAnnotation> outstandingIssues = new HashSet<>();

        for (FileAnnotation current : currentIssues) {
            FileAnnotation referenceToRemove = findReferenceByEquals(current);

            if (referenceToRemove == null) {
                referenceToRemove = findReferenceByContext(current);
            }

            if (referenceToRemove != null) {
                outstandingIssues.add(current);
                newIssues.remove(current);
                fixedIssues.remove(referenceToRemove);

                current.setBuild(referenceToRemove.getBuild());
            }
        }
    }

    @CheckForNull
    private FileAnnotation findReferenceByContext(final FileAnnotation current) {
        for (FileAnnotation reference : fixedIssues) {
            if (current.getContextHashCode() == reference.getContextHashCode()) {
                return reference;
            }
        }
        return null;
    }

    @CheckForNull
    private FileAnnotation findReferenceByEquals(final FileAnnotation current) {
        for (FileAnnotation reference : fixedIssues) {
            if (current.equals(reference)) {
                return reference;
            }
        }
        return null;
    }

    public Set<FileAnnotation> getNewIssues() {
        return newIssues;   
    }

    public Set<FileAnnotation> getFixedIssues() {
        return fixedIssues;
    }
}

