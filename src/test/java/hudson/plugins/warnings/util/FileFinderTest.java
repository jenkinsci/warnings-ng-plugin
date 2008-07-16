package hudson.plugins.warnings.util;

import junit.framework.Assert;

import org.apache.commons.lang.SystemUtils;
import org.junit.Test;

/**
 * Tests the class {@link FileFinder}.
 *
 * @author Ulli Hafner
 */
public class FileFinderTest {
    /**
     * Tests whether we always return an empty array even if the pattern is
     * wrong and ANT throws an exception (see issue #1813).
     **/
    @Test
    public void catchException() {
        FileFinder fileFinder = new FileFinder("**/non-existing-directory/*.xml");
        String[] files = fileFinder.find(SystemUtils.getUserHome());

        Assert.assertEquals(0, files.length);
    }
}

