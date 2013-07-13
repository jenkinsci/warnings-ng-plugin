package hudson.plugins.warnings.parser.jcreport;

/**
 * This the Item-Class
 * The Java-Bean-Conformity was chosen due to the digesters style of assigning.
 * It represents the Item-Tags within the report.xml.
 * Items have properties, that are mandatory to create a warning.
 * @author Johann Vierthaler, johann.vierthaler@web.de
 */
public class Item {

    private String column;
    private String findingtype;
    private String line;
    private String message;
    private String origin;
    private String severity;
    private String endcolumn;

    /**
     * Although this property is not used.
     * It was decided to keep it
     * available when Jenkins is modified and needs
     * access to this field;
     */
        private String endline;

    /**
     * Getter for column-Field.
     * @return column -> string
     */
    public String getColumn() {
        return column;
    }

    /**
     * Setter for Column-Field.
     * @param column -> setter
     */
    public void setColumn(final String column) {
        this.column = column;
    }

    /**
     * Getter for findingtype-Field.
     * @return findingtype -> getter
     */
    public String getFindingtype() {
        return findingtype;
    }

    /**
     * Setter for findingtype-Field.
     * @param findingtype -> setter
     */
    public void setFindingtype(final String findingtype) {
        this.findingtype = findingtype;
    }

    /**
     * Getter for line-Field.
     * @return line -> getter
     */
    public String getLine() {
        return line;
    }

    /**
     * Setter for line-Field.
     * @param line -> setter
     */
    public void setLine(final String line) {
        this.line = line;
    }

    /**
     * Getter for message-Field.
     * @return message -> getter
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter for message-Field.
     * @param message -> setter
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * Getter for origin-Field.
     * @return origin -> getter
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Setter for origin-Field.
     * @param origin -> setter
     */
    public void setOrigin(final String origin) {
        this.origin = origin;
    }

    /**
     * Getter for severity-Field.
     * @return severity -> getter
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * Setter for severtiy-Field.
     * @param severity -> setter
     */
    public void setSeverity(final String severity) {
        this.severity = severity;
    }


    /**
     * Getter for endline-Field.
     * @return endline -> getter
     */
    public String getEndline() {
        return endline;
    }


    /**
     * Setter for endline-Field.
     * @param endline -> setter
     */
    public void setEndline(final String endline) {
        this.endline = endline;
    }

    /**
     * Getter for endcolumn-Field.
     * @return endcolumn -> getter
     */
    public String getEndcolumn() {
        return endcolumn;
    }

    /**
     * Setter for endcolumn-Field.
     * @param endcolumn -> setter
     */
    public void setEndcolumn(final String endcolumn) {
        this.endcolumn = endcolumn;
    }

}
