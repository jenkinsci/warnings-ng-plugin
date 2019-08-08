package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.assertj.core.api.Assertions.*;

/**
 * Page Object for a table with source control information.
 *
 * @author Andreas Pabst
 * @author Fabian Janker
 */
public class SourceControlTable extends PageObject {
    private static final String SCM_BLAMES_ID = "blames";

    private DomElement scmInfo = null;
    private DomElement scmPaginate = null;
    private DomElement scmFilter = null;

    private List<SourceControlRow> rows = new ArrayList<>();
    private List<String> columnNames = new ArrayList<>();

    /**
     * Create a page object for the source control table in the details view.
     *
     * @param page
     *         the whole build details HtmlPage
     */
    public SourceControlTable(final HtmlPage page) {
        super(page);
        load();
    }

    private void load() {
        rows = new ArrayList<>();

        HtmlPage page = getPage();
        HtmlAnchor content = page.getAnchorByHref("#" + SCM_BLAMES_ID + "Content");
        clickOnElement(content);

        scmInfo = page.getElementById(SCM_BLAMES_ID + "_info");
        scmPaginate = page.getElementById(SCM_BLAMES_ID + "_paginate");
        scmFilter = page.getElementById(SCM_BLAMES_ID + "_filter");

        HtmlTable table = getHtmlTable(page);
        List<HtmlTableRow> tableHeaderRows = table.getHeader().getRows();
        assertThat(tableHeaderRows).hasSize(1);

        HtmlTableRow header = tableHeaderRows.get(0);
        columnNames = header
                .getCells()
                .stream()
                .map(HtmlTableCell::getTextContent)
                .collect(Collectors.toList());

        List<HtmlTableBody> bodies = table.getBodies();
        assertThat(bodies).hasSize(1);

        HtmlTableBody mainBody = bodies.get(0);
        waitForAjaxCall(mainBody);
        List<HtmlTableRow> contentRows = mainBody.getRows();

        for (HtmlTableRow row : contentRows) {
            List<HtmlTableCell> rowCells = row.getCells();
            rows.add(new SourceControlRow(rowCells, columnNames));
        }
    }

    @SuppressFBWarnings("BC")
    private HtmlTable getHtmlTable(final HtmlPage page) {
        DomElement scm = page.getElementById(SCM_BLAMES_ID);
        assertThat(scm).isInstanceOf(HtmlTable.class);

        return (HtmlTable) scm;
    }

    public String getInfo() {
        return scmInfo.getTextContent();
    }

    public List<SourceControlRow> getRows() {
        return rows;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    /**
     * Filter the table to show only issues containing a specific term.
     *
     * @param term
     *         The term to filter for
     */
    public void filter(final String term) {
        try {
            HtmlTextInput search = (HtmlTextInput) scmFilter.getElementsByTagName("input").get(0);
            search.type(term);

            load();
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    /**
     * Go to a page number in the table.
     *
     * @param pageNumber
     *         The page number to go to. 1-based.
     */
    public void goToPage(final int pageNumber) {
        DomNodeList<HtmlElement> pages = scmPaginate.getElementsByTagName("a");

        if (pages.size() >= pageNumber) {
            HtmlElement pageButton = pages.get(pageNumber - 1);
            clickOnElement(pageButton);
            pageButton.getPage().getEnclosingWindow().getJobManager().waitForJobs(1000);

            load();
        }
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private static void waitForAjaxCall(final HtmlTableBody body) {
        while ("Loading - please wait ...".equals(
                body.getRows().get(0).getCells().get(0).getFirstChild().getTextContent())) {
            System.out.println("Waiting for Ajax call to populate issues table ...");
            body.getPage().getEnclosingWindow().getJobManager().waitForJobs(1000);
        }
    }
}
