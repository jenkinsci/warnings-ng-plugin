package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests the class {@link hudson.plugins.warnings.parser.AjcParser}.
 */
public class AjcParserTest extends ParserTester {
    private static final String WARNING_TYPE = Messages._Warnings_AjcParser_ParserName().toString(Locale.ENGLISH);

    /**
     * Parses a file with various warnings:
     *   - message not found / unknown source
     *   - deprecation (class / method)
     *   - advice not applied
     *
     * Both unix and windows file paths.
     *
     * @throws java.io.IOException
     *      if the file could not be read
     */
    @Test
    public void parseDeprecation() throws IOException {
        Collection<FileAnnotation> warnings = new AjcParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 9, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "incorrect classpath: /home/hudson/.m2/repository/org/apache/cxf/cxf/2.6.1/cxf-2.6.1.pom",
                "<unknown source file>",
                WARNING_TYPE, "", Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                12,
                "The type SimpleFormController is deprecated",
                "/home/hudson/workspace/project/project-ejb/src/main/java/com/product/foo/pro/controllers/BarController.java",
                WARNING_TYPE, RegexpParser.DEPRECATION, Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                19,
                "The type SimpleFormController is deprecated",
                "/home/hudson/workspace/project/project-ejb/src/main/java/com/product/foo/pro/controllers/BarController.java",
                WARNING_TYPE, RegexpParser.DEPRECATION, Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                32,
                "The method BarController.initBinder(HttpServletRequest, ServletRequestDataBinder) overrides a deprecated method from BaseCommandController",
                "/home/hudson/workspace/project/project-ejb/src/main/java/com/product/foo/pro/controllers/BarController.java",
                WARNING_TYPE, RegexpParser.DEPRECATION, Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                33,
                "The method initBinder(HttpServletRequest, ServletRequestDataBinder) from the type BaseCommandController is deprecated",
                "/home/hudson/workspace/project/project-ejb/src/main/java/com/product/foo/pro/controllers/BarController.java",
                WARNING_TYPE, RegexpParser.DEPRECATION, Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                31,
                "The method NewBarController.initBinder(HttpServletRequest, ServletRequestDataBinder) overrides a deprecated method from BaseCommandController",
                "/home/hudson/workspace/project/project-ejb/src/main/java/com/product/foo/pro/controllers/NewBarController.java",
                WARNING_TYPE, RegexpParser.DEPRECATION, Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                28,
                "The method NewFooController.onSubmit(HttpServletRequest, HttpServletResponse, Object, BindException) overrides a deprecated method from SimpleFormController",
                "C:/Users/hudson/workspace/project/project-ejb/src/main/java/com/product/foo/pro/controllers/NewFooController.java",
                WARNING_TYPE, RegexpParser.DEPRECATION, Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                38,
                "advice defined in com.company.foo.common.security.aspect.FooBarAspect has not been applied [Xlint:adviceDidNotMatch]",
                "/home/hudson/workspace/project/project-ejb/src/main/java/com/product/foo/common/security/aspect/FooBarAspect.java",
                WARNING_TYPE, AjcParser.ADVICE, Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                32,
                "advice defined in org.springframework.orm.jpa.aspectj.JpaExceptionTranslatorAspect has not been applied [Xlint:adviceDidNotMatch]",
                "/home/hudson/.m2/repository/org/springframework/spring-aspects/3.2.8.RELEASE/spring-aspects-3.2.8.RELEASE.jar!org/springframework/orm/jpa/aspectj/JpaExceptionTranslatorAspect.class",
                WARNING_TYPE, AjcParser.ADVICE, Priority.NORMAL);

        assertFalse(iterator.hasNext());
    }

    @Override
    protected String getWarningsFile() {
        return "ajc.txt";
    }
}
