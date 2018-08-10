package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import static org.junit.Assert.*;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link IarParser}.
 *
 * @author Ullrich Hafner
 */
public class IarParserTest extends ParserTester {
    private static final String TYPE = new IarParser().getGroup();

    /**
     * Parses a file with warnings/errors in all styles. it check the amount of error/warnings found
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-46131">Issue 46131</a>
     */
    @Test
    public void issue46131() throws IOException {
        Collection<FileAnnotation> warnings = new IarParser().parse(openFile("issue46131.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 2, warnings.size());
    }

    /**
     * Parses a file with warnings/errors in all styles. it check the amount of error/warnings found
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-8823">Issue 8823</a>
     */
    @Test
    public void IAR_error_size() throws IOException {
        Collection<FileAnnotation> warnings = new IarParser().parse(openFile("issue8823.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 6, warnings.size());
    }

    /**
     * Parses a file and check error number 1
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-8823">Issue 8823</a>
     */
   @Test
    public void IAR_error1() throws IOException {
        FileAnnotation annotation = getErrorNumber(1);
       checkWarning(annotation, 3767, "enumerated type mixed with another type",
                "D:/continuousIntegration/modifiedcomps/forcedproduct/MHSM-Cascade/Cascade-Config/config/src/RDR_Config.c",
                "Pe188", Priority.NORMAL);
    }
    
     /**
     * Parses a file and check error number 2
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-8823">Issue 8823</a>
     */
   @Test
    public void IAR_error2() throws IOException {
        FileAnnotation annotation = getErrorNumber(2);
        checkWarning(annotation, 3767, "enumerated type mixed with another type",
                "D:/continuousIntegration/modifiedcomps/forcedproduct/MHSM-Cascade/Cascade-Config/config/src/RDR_Config.c",
                "Pe188", Priority.NORMAL);
    }
    
     /**
     * Parses a file and check error number 3
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-8823">Issue 8823</a>
     */
   @Test
    public void IAR_error3() throws IOException {
        FileAnnotation annotation = getErrorNumber(3);
        checkWarning(annotation, 3918, "enumerated type mixed with another type",
                "D:/continuousIntegration/modifiedcomps/forcedproduct/MHSM-Cascade/Cascade-Config/config/src/RDR_Config.c",
                "Pe188", Priority.NORMAL);
    }
    
     /**
     * Parses a file and check error number 4
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-8823">Issue 8823</a>
     */
   @Test
    public void IAR_error4() throws IOException {
        FileAnnotation annotation = getErrorNumber(4);
        checkWarning(annotation, 17, "cannot open source file \"System/ProcDef_LPC17xx.h\"",
                "c:/JenkinsJobs/900ZH/Workspace/Product.900ZH/Src/System/AdditionalResources.h",
                "Pe1696", Priority.HIGH);
    }
    
    /**
     * Parses a file and check error number 5
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-8823">Issue 8823</a>
     */
   @Test
    public void IAR_error5() throws IOException {
        FileAnnotation annotation = getErrorNumber(5);
        checkWarning(annotation, 43, "variable \"pgMsgEnv\" was declared but never referenced",
                "C:/dev/bsc/daqtask.c",
                "Pe177", Priority.NORMAL);
    }
    
     /**
     * Parses a file and check error number 6
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-8823">Issue 8823</a>
     */
   @Test
    public void IAR_error6() throws IOException {
        FileAnnotation annotation = getErrorNumber(6);
        // the \" is needed
        checkWarning(annotation, 0, "cannot open source file \"c:\\JenkinsJobs\\900ZH\\Workspace\\Lib\\Drivers\\_Obsolete\\Uart\\UartInterface.c\"",
                "\"c:/JenkinsJobs/900ZH/Workspace/Lib/Drivers/_Obsolete/Uart/UartInterface.c\"",
                "Pe1696", Priority.HIGH);
    }

    @Override
    protected String getWarningsFile() {
        return "iar-nowrap.log";
    }

    protected FileAnnotation getErrorNumber( final int number) throws IOException {
        Collection<FileAnnotation> warnings = new IarParser().parse(openFile("issue8823.txt"));
        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();

        for(int i = 1; i < number; i++)
        {
            annotation = iterator.next();
        }

        return annotation;
    }
    
}