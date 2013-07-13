package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.warnings.parser.jcreport.File;
import hudson.plugins.warnings.parser.jcreport.Item;
import hudson.plugins.warnings.parser.jcreport.JcReportParser;
import hudson.plugins.warnings.parser.jcreport.Report;

import hudson.util.IOException2;

/**
 * Tests the JcReportParser-Class.
 * @author Johann Vierthaler, johann.vierthaler@web.de
 */
public class JcReportParserTest {
    /**
     * Parses Report with 5 Warnings.
     * @author Johann Vierthaler, johann.vierthaler@web.de
     * @throws ParsingCanceledException -> thrown by jcrp.parse();
     * @throws IOException -> thrown by jcrp.parse();
     */
    @Test
    public void testParserWithValidFile() throws ParsingCanceledException, IOException {
       JcReportParser jcrp = new JcReportParser();
        ArrayList<FileAnnotation> warnings = new ArrayList<FileAnnotation>();
        InputStreamReader readCorrectXml = new InputStreamReader(new FileInputStream("src/test/resources/hudson/plugins/warnings/parser/jcreport/testCorrect.xml"), "UTF-8");
        warnings.addAll(jcrp.parse(readCorrectXml));
        assertEquals("Should be 7: ", 7, warnings.size());
        assertEquals("Wrong Parse FileName: ", "SomeDirectory/SomeClass.java", warnings.get(0).getFileName());
        assertEquals("Wrong Parse Origin: ", "Checkstyle", warnings.get(0).getOrigin());
        assertEquals("Wrong Parse Priority: ", Priority.HIGH, warnings.get(0).getPriority());
        assertEquals("Wrong Parse Message: ", "SomeMessage", warnings.get(0).getMessage());
        assertEquals("Wrong Parse PackageName: ", "SomePackage", warnings.get(0).getPackageName());
        assertEquals("Wrong Parse LineNumberParse: ", 50, warnings.get(0).getPrimaryLineNumber());
        assertEquals("Wrong Parse Pathname: ", "SomeDirectory", warnings.get(0).getPathName());
    }

    /**
     * Gets Collection with size of 5.
     * @author Johann Vierthaler, johann.vierthaler@web.de
     * @throws ParsingCanceledException -> thrown by jcrp.parse();
     * @throws IOException -> thrown by jcrp.parse();
     */
    @Test
    public void testGetWarningList() throws ParsingCanceledException, IOException {

        JcReportParser jcrp = new JcReportParser();
        ArrayList<FileAnnotation> warnings = new ArrayList<FileAnnotation>();
        InputStreamReader readCorrectXml = new InputStreamReader(new FileInputStream("src/test/resources/hudson/plugins/warnings/parser/jcreport/testCorrect.xml"), "UTF-8");
        warnings.addAll(jcrp.parse(readCorrectXml));
        assertEquals("Size is 7: ", 7, warnings.size());
    }


    /**
     * This test assures that all properties within Report-, File- and Item-Objects are parsed correctly.
     * Not all properties are needed to create a warning.
     * So it was decided to keep them anyway in case Jenkins is modified to contain more
     * information in the Warning-Objects.
     * For reasons of simplicity only a Report with 1 file and 1 item was created.
     * @author Johann Vierthaler, johann.vierthaler@web.de
     * @throws IOException -> createReport can cause an IOException.
     */
    @Test
    public void testReportParserProperties() throws IOException {
        InputStreamReader readCorrectXml = new InputStreamReader(new FileInputStream("src/test/resources/hudson/plugins/warnings/parser/jcreport/testReportProps.xml"), "UTF-8");
        Report testReportProps = new JcReportParser().createReport(readCorrectXml);

        assertEquals("Should be 1: ", 1, testReportProps.getFiles().size());

        File file = testReportProps.getFiles().get(0);
        assertEquals("Should be 'SomeClass'", "SomeClass", file.getClassname());
        assertEquals("Should be 'SomeLevel'", "SomeLevel", file.getLevel());
        assertEquals("Should be '173'", "173", file.getLoc());
        assertEquals("Should be 'SomeDirectory/SomeClass.java'", "SomeDirectory/SomeClass.java", file.getName());
        assertEquals("Should be 'SomePackage'", "SomePackage", file.getPackageName());
        assertEquals("Should be 'SomeDirectory'", "SomeDirectory", file.getSrcdir());

        Item item = file.getItems().get(0);
        assertEquals("Should be '0'", "0", item.getColumn());
        assertEquals("Should be '3'", "3", item.getEndcolumn());
        assertEquals("Should be 'SomeType'", "SomeType", item.getFindingtype());
        assertEquals("Should be '50'", "50", item.getLine());
        assertEquals("Should be '70'", "70", item.getEndline());
        assertEquals("Should be 'SomeMessage'", "SomeMessage", item.getMessage());
        assertEquals("Should be 'CriticalError'", "CriticalError", item.getSeverity());
    }


    /**
     * Test the SAXException when file is corrupted.
     * When a SAXException is triggered a new IOException is thrown.
     * This explains the expected = IOException.class.
     * @author Johann Vierthaler, johann.vierthaler@web.de
     * @throws ParsingCanceledException -> thrown by jcrp.parse();
     * @throws IOException -> thrown by jcrp.parse();
     * @Coverage The missing coverage is a known issue with ECLEMMA.
     *           For further Information: http://www.eclemma.org/faq.html#trouble05
     */
    @Test(expected = IOException2.class)
    public void testSAXEception() throws ParsingCanceledException, IOException {
        new JcReportParser().parse(new InputStreamReader(new FileInputStream("src/test/resources/hudson/plugins/warnings/parser/jcreport/testCorrupt.xml") , "UTF-8"));
    }
}