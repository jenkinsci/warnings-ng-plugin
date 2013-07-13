package hudson.plugins.warnings.parser.jcreport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * File-Class.
 * Stores field to create a warning.
 * It represents the File-Tags within the report.xml.
 * The Java-Bean-Conformity was chosen due to the digesters style of assigning.
 * @author Johann Vierthaler, johann.vierthaler@web.de
 */
public class File {
    private String name;
    private String packageName;
    private String srcdir;
    private final transient List<Item> items = new ArrayList<Item>();

    /**
     * These properties are not used to create Warnings.
     * It was decided to keep them
     * available when Jenkins is modified and needs
     * access to these fields;
     */
    private String level;
    private String loc;
    private String classname;


    /**
     * Getter for the Item-Collection.
     * @return  unmodifiable collection of Item-Objects
     */
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Adds an Item-Object to the collection items.
     * @param item -> add this item.
     */
    public void addItem(final Item item) {
        items.add(item);
    }


    /**
     * Getter for className-Field.
     * @return String className.
     */
    public String getClassname() {
        return classname;
    }

    /**
     * Setter for className-Field.
     * @param classname -> classNamesetter
     */
    public void setClassname(final String classname) {
        this.classname = classname;
    }

    /**
     * Getter for level-Field.
     * @return level
     */
    public String getLevel() {
        return level;
    }


    /**
     * Setter for level-Field.
     * @param level -> set level
     */
    public void setLevel(final String level) {
        this.level = level;
    }



    /**
     * Getter for loc-Field.
     * @return loc -> loc
     */
    public String getLoc() {
        return loc;
    }

    /**
     * Setter for loc-Field.
     * @param loc -> locsetter
     */
    public void setLoc(final String loc) {
        this.loc = loc;
    }


    /**
     * Getter for name-Field.
     * @return name -> name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for Name-Field.
     * @param name -> name
     */
    public void setName(final String name) {
        this.name = name;
    }


    /**
     * Getter for packageName-Field.
     * @return packageName -> packageName.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Setter for packageName-Field.
     * @param packageName -> packageName Setter
     */
    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    /**
     * Getter for srcdir-Field.
     * @return srcdir -> srcdir.
     */
    public String getSrcdir() {
        return srcdir;
    }

    /**
     * Setter for srcdir-Field.
     * @param srcdir -> srcdir
     */
    public void setSrcdir(final String srcdir) {
        this.srcdir = srcdir;
    }

}
