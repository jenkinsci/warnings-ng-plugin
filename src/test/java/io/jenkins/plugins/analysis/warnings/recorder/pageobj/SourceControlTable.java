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

import static org.assertj.core.api.Assertions.*;

/**
 * Page Object for a table with source control information.
 *
 * @author Andreas Pabst
 * @author Fabian Janker
 */
public class SourceControlTable {
    private DomElement scmInfo = null;
    private DomElement scmPaginate = null;
    private DomElement scmFilter = null;

    private List<SourceControlRow> rows = new ArrayList<>();
    private List<String> columnNames = new ArrayList<>();

    private final HtmlPage page;

    /**
     * Create a page object for the source control table in the details view.
     *
     * @param page
     *         the whole build details HtmlPage
     */
    public SourceControlTable(final HtmlPage page) {
        this.page = page;
        load();
    }

    private void load() {
        rows = new ArrayList<>();

        HtmlAnchor content = page.getAnchorByHref("#scmContent");
        clickOnLink(content);

        scmInfo = page.getElementById("scm_info");
        scmPaginate = page.getElementById("scm_paginate");
        scmFilter = page.getElementById("scm_filter");

        DomElement scm = page.getElementById("scm");
        assertThat(scm).isInstanceOf(HtmlTable.class);

        HtmlTable table = (HtmlTable) scm;
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
            clickOnLink(pages.get(pageNumber - 1));
            load();
        }
    }

    /**
     * Clicks a link.
     *
     * @param element
     *         a {@link DomElement} which will trigger the redirection to a new page.
     */
    private void clickOnLink(final DomElement element) {
        try {
            element.click();
        }
        catch (IOException e) {
            throw new AssertionError(e);
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
