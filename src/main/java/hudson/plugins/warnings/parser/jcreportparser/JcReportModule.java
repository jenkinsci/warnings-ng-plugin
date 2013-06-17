package hudson.plugins.warnings.parser.jcreportparser;

import org.apache.commons.digester3.binder.AbstractRulesModule;

/**
 * This class is the 'module' that decides WHAT the Digester has to parse
 * and to WHICH property the value given.
 * @author Johann Vierthaler, johann.vierthaler@web.de
 */
public class JcReportModule extends AbstractRulesModule {
    @Override
    protected void configure() {

        // Creates a Report-Object
        forPattern("report").createObject().ofType(Report.class.getCanonicalName()).then().setProperties();

        /*
         * Calls the Report-Objects method addFile() to add a new File-Object to the list.
         * Since fields are not allowed to have names like "package" or "src-dir" an alias has to be added
         * to let the digester assign values to those rewritten fields.
         */
        forPattern("report/file").createObject().ofType(File.class.getCanonicalName()).then().setNext("addFile").then().setProperties()
        .then().setProperties().addAlias("package").forProperty("packageName")
        .then().setProperties().addAlias("src-dir").forProperty("srcdir");

        /*
         * The Digester assigns here the values in the report.xml to the fields in the File-Object
         */
        forPattern("report/file/classname").setBeanProperty();
        forPattern("report/file/level").setBeanProperty();
        forPattern("report/file/loc").setBeanProperty();
        forPattern("report/file/name").setBeanProperty();
        forPattern("report/file/package").setBeanProperty();
        forPattern("report/file/src-dir").setBeanProperty();

        /*
         * The File-Object has also a collection called Items.
         * Like in the File-Object some properties need aliases.
         */
        forPattern("report/file/item").createObject().ofType(Item.class.getCanonicalName()).then().setNext("addItem")
                .then().setProperties().addAlias("finding-type").forProperty("findingtype")
                .then().setProperties().addAlias("end-line").forProperty("endline")
                .then().setProperties().addAlias("end-column").forProperty("endcolumn");

        /*
         * Here assigns the digester to the values in the Item-Object.
         */
        forPattern("report/file/item/column").setBeanProperty();
        forPattern("report/file/item/line").setBeanProperty();
        forPattern("report/file/item/message").setBeanProperty();
        forPattern("report/file/item/origin").setBeanProperty();
        forPattern("report/file/item/severity").setBeanProperty();
        forPattern("report/file/item/findingtype").setBeanProperty();
        forPattern("report/file/item/endline").setBeanProperty();
        forPattern("report/file/item/endcolumn").setBeanProperty();

    }

}
