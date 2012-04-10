/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link FlexSDKParser}.
 */
public class FlexSDKParserTest extends ParserTester {
    private static final String TYPE = new FlexSDKParser().getGroup();

    /**
     * Parses a file with two deprecation warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseDeprecation() throws IOException {
        Collection<FileAnnotation> warnings = new FlexSDKParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 5, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();

        FileAnnotation firstCompcAnnotation = iterator.next();
        checkWarning(firstCompcAnnotation,
                34,
                "class 'FeedStructureHelper' will be scoped to the default namespace: com.company.flex.feed internal.  It will not be visible outside of this package.",
                "D:/workspaces/flexcompo_trunkdev_nightly/src/flexcompo/uicomponents/ugv_component/src/main/com/company/flex/feed/FeedStructureHelper.as",
                TYPE, "", Priority.NORMAL);

        FileAnnotation secondCompcAnnotation = iterator.next();
        checkWarning(secondCompcAnnotation,
                122,
                "Duplicate variable definition.",
                "D:/workspaces/flexcompo_trunkdev_nightly/src/flexcompo/uicomponents/ugv_component/src/main/com/company/flex/component/chart/lasso/DefaultLassoObjectsHandler.as",
                TYPE, "", Priority.NORMAL);

        FileAnnotation firstASMxmlcAnnotation = iterator.next();
        checkWarning(firstASMxmlcAnnotation,
                115,
                "return value for function 'cx' has no type declaration.",
                "D:/workspaces/flexcompo_trunkdev_nightly/src/flexcompo/samples/ugv_helloExtensibility_flex/src/main/extensibility/wordpress/Tag.as",
                TYPE, "", Priority.NORMAL);

        FileAnnotation firstMXMLMxmlcAnnotation = iterator.next();
        checkWarning(firstMXMLMxmlcAnnotation,
                157,
                "var 'cacheList' will be scoped to the default namespace: HelloExtensibleWorld: internal.  It will not be visible outside of this package.",
                "D:/workspaces/flexcompo_trunkdev_nightly/src/flexcompo/samples/ugv_helloExtensibility_flex/src/main/HelloExtensibleWorld.mxml",
                TYPE, "", Priority.NORMAL);

        FileAnnotation secondMXMLMxmlcAnnotation = iterator.next();
        checkWarning(secondMXMLMxmlcAnnotation,
                148,
                "The CSS type selector 'Book' was not processed, because the type was not used in the application.",
                "D:/workspaces/flexcompo_trunkdev_nightly/src/flexcompo/samples/ugv_helloExtensibility_flex/src/main/HelloExtensibleWorld.mxml",
                TYPE, "", Priority.NORMAL);
    }

    @Override
    protected String getWarningsFile() {
        return "flexsdk.txt";
    }
}