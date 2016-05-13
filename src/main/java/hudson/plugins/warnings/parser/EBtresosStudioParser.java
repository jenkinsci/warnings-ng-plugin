package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for EB tresos Studio warnings.
 *
 * @author Sven Lübke
 */
@Extension
public class EBtresosStudioParser extends RegexpLineParser {
    /** Pattern of EB tresos warnings. */
    private static final String TRESOS_WARNING_PATTERN = "^(INFO|WARNING|ERROR)\\s\\d*-\\d*-\\d*,\\d*:\\d*:\\d*\\s\\(((\\p{Alnum}|_)*)\\)\\s(.*)$";
    private static final String[] TRESOS_INFO_MSGS_IGNORE = {
        "13014", //Generated "B:\Camera_comp\src\workspace\ECU_Camera\output\generated\include\FiM_Trace.h"
        "13030", //Running generator "CanTp_TS_TxDxM6I5R0_GeneratorId" in mode "generate" for module "CanTp_TS_TxDxM6I5R0"
        "13015", //Generated "B:\Camera_comp\src\workspace\ECU_Camera\output\generated\make\Make_cfg.mak"
        "160001", //Generated file Com_SymbolicNames_PBcfg.h
        "170001", //Generated file src/PduR_Lcfg.c
        "20053", //Generating file B:\Camera_comp\src\workspace\ECU_Camera\output\generated\output\Dio.epc
        "2113", //Creating directory "B:\Camera_comp\src\workspace\ECU_Camera\output\generated\output"
        "2501", //Generated "B:\Camera_comp\src\workspace\ECU_Camera\output\generated\include\Common_MemMap.h"
        "2506", //Creating directory "B:\Camera_comp\src\workspace\ECU_Camera\output\generated\orti"
        "OS_1", //*** AutosarOS 4.5.38 Build 20130705 (PA/XPC56XXL) ***
        "OS_3", //OS-Generation succeeded for project Os_TS_T2D17M4I5R0_AS403
        "SWDUPDATER_10", //Successfully completed the update of the Basic Software Module Descriptions and Service Component Descriptions.
        "SWDUPDATER_14", //Imported BSW module description CanIf.
        "SWDUPDATER_24", //Imported service component type Dem.
        "SWDUPDATER_7", //Generated description file PduR_Bswmd.arxml.
        "SWDUPDATER_8" //Imported all description files successfully.
        };

    /**
     * Creates a new instance of <code>EBtresosStudioParser</code>.
     */
    public EBtresosStudioParser() {
        super(Messages._Warnings_EBtresosStudio_ParserName(),
                Messages._Warnings_EBtresosStudio_LinkName(),
                Messages._Warnings_EBtresosStudio_TrendName(),
                TRESOS_WARNING_PATTERN);
    }

    @Override
    protected String getId() {
        return "EB tresos Studio";
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = "*.xdm";
        int lineNumber  = 0;
        String type     = matcher.group(1);
        String category = matcher.group(2);
        String message  = matcher.group(4);
        Priority priority;


        if (type.equals("ERROR")) {
            priority = Priority.HIGH;
        }
        if (type.equals("INFO")) {
            priority = Priority.NORMAL;

            for (int index = 0; index < TRESOS_INFO_MSGS_IGNORE.length; index++) {
              if (category.equals(TRESOS_INFO_MSGS_IGNORE[index])) {
                //type = "";
                priority = Priority.LOW;
                message = "really unimportant tresos message";
                fileName = "unimportant.file";
                lineNumber = 1;
                break;
              }
            }
        }
		else {
            priority = Priority.HIGH;
        }
        Warning warning = createWarning(fileName, lineNumber, category, message, priority);
        return warning;
    }
}

