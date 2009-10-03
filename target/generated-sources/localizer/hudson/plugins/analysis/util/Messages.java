// CHECKSTYLE:OFF

package hudson.plugins.analysis.util;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;

@SuppressWarnings({
    "",
    "PMD"
})
public class Messages {

    private final static ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);

    /**
     * Namespace
     * 
     */
    public static String NamespaceDetail_header() {
        return holder.format("NamespaceDetail.header");
    }

    /**
     * Namespace
     * 
     */
    public static Localizable _NamespaceDetail_header() {
        return new Localizable(holder, "NamespaceDetail.header");
    }

    /**
     * High
     * 
     */
    public static String Priority_High() {
        return holder.format("Priority.High");
    }

    /**
     * High
     * 
     */
    public static Localizable _Priority_High() {
        return new Localizable(holder, "Priority.High");
    }

    /**
     * File encoding has not been set in pom.xml, using platform encoding {0}, i.e. build is platform dependent (see &lt;a href="http://docs.codehaus.org/display/MAVENUSER/POM+Element+for+Source+File+Encoding">Maven FAQ&lt;/a>).
     * 
     */
    public static String Reporter_Error_NoEncoding(Object arg1) {
        return holder.format("Reporter.Error.NoEncoding", arg1);
    }

    /**
     * File encoding has not been set in pom.xml, using platform encoding {0}, i.e. build is platform dependent (see &lt;a href="http://docs.codehaus.org/display/MAVENUSER/POM+Element+for+Source+File+Encoding">Maven FAQ&lt;/a>).
     * 
     */
    public static Localizable _Reporter_Error_NoEncoding(Object arg1) {
        return new Localizable(holder, "Reporter.Error.NoEncoding", arg1);
    }

    /**
     * (normal priority)
     * 
     */
    public static String Trend_PriorityNormal() {
        return holder.format("Trend.PriorityNormal");
    }

    /**
     * (normal priority)
     * 
     */
    public static Localizable _Trend_PriorityNormal() {
        return new Localizable(holder, "Trend.PriorityNormal");
    }

    /**
     * (low priority)
     * 
     */
    public static String Trend_PriorityLow() {
        return holder.format("Trend.PriorityLow");
    }

    /**
     * (low priority)
     * 
     */
    public static Localizable _Trend_PriorityLow() {
        return new Localizable(holder, "Trend.PriorityLow");
    }

    /**
     * Configure the trend graph of this plug-in. This default configuration can be overwritten by each user.
     * 
     */
    public static String DefaultGraphConfiguration_Description() {
        return holder.format("DefaultGraphConfiguration.Description");
    }

    /**
     * Configure the trend graph of this plug-in. This default configuration can be overwritten by each user.
     * 
     */
    public static Localizable _DefaultGraphConfiguration_Description() {
        return new Localizable(holder, "DefaultGraphConfiguration.Description");
    }

    /**
     * Skipping file {0} of module {1} because Hudson has no permission to read the file.
     * 
     */
    public static String FilesParser_Error_NoPermission(Object arg1, Object arg2) {
        return holder.format("FilesParser.Error.NoPermission", arg1, arg2);
    }

    /**
     * Skipping file {0} of module {1} because Hudson has no permission to read the file.
     * 
     */
    public static Localizable _FilesParser_Error_NoPermission(Object arg1, Object arg2) {
        return new Localizable(holder, "FilesParser.Error.NoPermission", arg1, arg2);
    }

    /**
     * Threshold must be an integer value greater or equal 0.
     * 
     */
    public static String FieldValidator_Error_Threshold() {
        return holder.format("FieldValidator.Error.Threshold");
    }

    /**
     * Threshold must be an integer value greater or equal 0.
     * 
     */
    public static Localizable _FieldValidator_Error_Threshold() {
        return new Localizable(holder, "FieldValidator.Error.Threshold");
    }

    /**
     * Low
     * 
     */
    public static String Priority_Low() {
        return holder.format("Priority.Low");
    }

    /**
     * Low
     * 
     */
    public static Localizable _Priority_Low() {
        return new Localizable(holder, "Priority.Low");
    }

    /**
     * High Priority
     * 
     */
    public static String HighPriority() {
        return holder.format("HighPriority");
    }

    /**
     * High Priority
     * 
     */
    public static Localizable _HighPriority() {
        return new Localizable(holder, "HighPriority");
    }

    /**
     * (fixed)
     * 
     */
    public static String Trend_Fixed() {
        return holder.format("Trend.Fixed");
    }

    /**
     * (fixed)
     * 
     */
    public static Localizable _Trend_Fixed() {
        return new Localizable(holder, "Trend.Fixed");
    }

    /**
     * Encoding must be a supported encoding of the Java platform (see java.nio.charset.Charset).
     * 
     */
    public static String FieldValidator_Error_DefaultEncoding() {
        return holder.format("FieldValidator.Error.DefaultEncoding");
    }

    /**
     * Encoding must be a supported encoding of the Java platform (see java.nio.charset.Charset).
     * 
     */
    public static Localizable _FieldValidator_Error_DefaultEncoding() {
        return new Localizable(holder, "FieldValidator.Error.DefaultEncoding");
    }

    /**
     * Normal
     * 
     */
    public static String Priority_Normal() {
        return holder.format("Priority.Normal");
    }

    /**
     * Normal
     * 
     */
    public static Localizable _Priority_Normal() {
        return new Localizable(holder, "Priority.Normal");
    }

    /**
     * Configure Default Trend Graph
     * 
     */
    public static String DefaultGraphConfiguration_Name() {
        return holder.format("DefaultGraphConfiguration.Name");
    }

    /**
     * Configure Default Trend Graph
     * 
     */
    public static Localizable _DefaultGraphConfiguration_Name() {
        return new Localizable(holder, "DefaultGraphConfiguration.Name");
    }

    /**
     * Skipping file {0} of module {1} because it''s empty.
     * 
     */
    public static String FilesParser_Error_EmptyFile(Object arg1, Object arg2) {
        return holder.format("FilesParser.Error.EmptyFile", arg1, arg2);
    }

    /**
     * Skipping file {0} of module {1} because it''s empty.
     * 
     */
    public static Localizable _FilesParser_Error_EmptyFile(Object arg1, Object arg2) {
        return new Localizable(holder, "FilesParser.Error.EmptyFile", arg1, arg2);
    }

    /**
     * Parsing of file {0} failed due to an exception:
     * 
     */
    public static String FilesParser_Error_Exception(Object arg1) {
        return holder.format("FilesParser.Error.Exception", arg1);
    }

    /**
     * Parsing of file {0} failed due to an exception:
     * 
     */
    public static Localizable _FilesParser_Error_Exception(Object arg1) {
        return new Localizable(holder, "FilesParser.Error.Exception", arg1);
    }

    /**
     * Configure User Trend Graph
     * 
     */
    public static String UserGraphConfiguration_Name() {
        return holder.format("UserGraphConfiguration.Name");
    }

    /**
     * Configure User Trend Graph
     * 
     */
    public static Localizable _UserGraphConfiguration_Name() {
        return new Localizable(holder, "UserGraphConfiguration.Name");
    }

    /**
     * Package
     * 
     */
    public static String PackageDetail_header() {
        return holder.format("PackageDetail.header");
    }

    /**
     * Package
     * 
     */
    public static Localizable _PackageDetail_header() {
        return new Localizable(holder, "PackageDetail.header");
    }

    /**
     * No report files were found. Configuration error?
     * 
     */
    public static String FilesParser_Error_NoFiles() {
        return holder.format("FilesParser.Error.NoFiles");
    }

    /**
     * No report files were found. Configuration error?
     * 
     */
    public static Localizable _FilesParser_Error_NoFiles() {
        return new Localizable(holder, "FilesParser.Error.NoFiles");
    }

    /**
     * Configure the trend graph of this plug-in for the current job and user. These values are persisted in a cookie, so please make sure that cookies are enabled in your browser.
     * 
     */
    public static String UserGraphConfiguration_Description() {
        return holder.format("UserGraphConfiguration.Description");
    }

    /**
     * Configure the trend graph of this plug-in for the current job and user. These values are persisted in a cookie, so please make sure that cookies are enabled in your browser.
     * 
     */
    public static Localizable _UserGraphConfiguration_Description() {
        return new Localizable(holder, "UserGraphConfiguration.Description");
    }

    /**
     * Category
     * 
     */
    public static String CategoryDetail_header() {
        return holder.format("CategoryDetail.header");
    }

    /**
     * Category
     * 
     */
    public static Localizable _CategoryDetail_header() {
        return new Localizable(holder, "CategoryDetail.header");
    }

    /**
     * Fixed Warnings
     * 
     */
    public static String FixedWarningsDetail_Name() {
        return holder.format("FixedWarningsDetail.Name");
    }

    /**
     * Fixed Warnings
     * 
     */
    public static Localizable _FixedWarningsDetail_Name() {
        return new Localizable(holder, "FixedWarningsDetail.Name");
    }

    /**
     * (new)
     * 
     */
    public static String Trend_New() {
        return holder.format("Trend.New");
    }

    /**
     * (new)
     * 
     */
    public static Localizable _Trend_New() {
        return new Localizable(holder, "Trend.New");
    }

    /**
     * Trend graph height must be an integer value greater or equal {0}.
     * 
     */
    public static String FieldValidator_Error_TrendHeight(Object arg1) {
        return holder.format("FieldValidator.Error.TrendHeight", arg1);
    }

    /**
     * Trend graph height must be an integer value greater or equal {0}.
     * 
     */
    public static Localizable _FieldValidator_Error_TrendHeight(Object arg1) {
        return new Localizable(holder, "FieldValidator.Error.TrendHeight", arg1);
    }

    /**
     * Normal Priority
     * 
     */
    public static String NormalPriority() {
        return holder.format("NormalPriority");
    }

    /**
     * Normal Priority
     * 
     */
    public static Localizable _NormalPriority() {
        return new Localizable(holder, "NormalPriority");
    }

    /**
     * New Warnings
     * 
     */
    public static String NewWarningsDetail_Name() {
        return holder.format("NewWarningsDetail.Name");
    }

    /**
     * New Warnings
     * 
     */
    public static Localizable _NewWarningsDetail_Name() {
        return new Localizable(holder, "NewWarningsDetail.Name");
    }

    /**
     * Errors
     * 
     */
    public static String Errors() {
        return holder.format("Errors");
    }

    /**
     * Errors
     * 
     */
    public static Localizable _Errors() {
        return new Localizable(holder, "Errors");
    }

    /**
     * Module
     * 
     */
    public static String ModuleDetail_header() {
        return holder.format("ModuleDetail.header");
    }

    /**
     * Module
     * 
     */
    public static Localizable _ModuleDetail_header() {
        return new Localizable(holder, "ModuleDetail.header");
    }

    /**
     * Low Priority
     * 
     */
    public static String LowPriority() {
        return holder.format("LowPriority");
    }

    /**
     * Low Priority
     * 
     */
    public static Localizable _LowPriority() {
        return new Localizable(holder, "LowPriority");
    }

    /**
     * Type
     * 
     */
    public static String TypeDetail_header() {
        return holder.format("TypeDetail.header");
    }

    /**
     * Type
     * 
     */
    public static Localizable _TypeDetail_header() {
        return new Localizable(holder, "TypeDetail.header");
    }

    /**
     * Module {0}: {1}
     * 
     */
    public static String Result_Error_ModuleErrorMessage(Object arg1, Object arg2) {
        return holder.format("Result.Error.ModuleErrorMessage", arg1, arg2);
    }

    /**
     * Module {0}: {1}
     * 
     */
    public static Localizable _Result_Error_ModuleErrorMessage(Object arg1, Object arg2) {
        return new Localizable(holder, "Result.Error.ModuleErrorMessage", arg1, arg2);
    }

    /**
     * (high priority)
     * 
     */
    public static String Trend_PriorityHigh() {
        return holder.format("Trend.PriorityHigh");
    }

    /**
     * (high priority)
     * 
     */
    public static Localizable _Trend_PriorityHigh() {
        return new Localizable(holder, "Trend.PriorityHigh");
    }

}
