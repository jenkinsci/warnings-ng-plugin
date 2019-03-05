package io.jenkins.plugins.analysis.warnings;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.PyLintParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for Pylint.
 *
 * @author Ullrich Hafner
 */
public class PyLint extends ReportScanningTool {
    private static final long serialVersionUID = 4578376477574960381L;
    static final String ID = "pylint";

    /** Creates a new instance of {@link PyLint}. */
    @DataBoundConstructor
    public PyLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public PyLintParser createParser() {
        return new PyLintParser();
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends IconLabelProvider {
        private final PyLintDescriptions descriptions;

        LabelProvider(final PyLintDescriptions descriptions) {
            super(ID, Messages.Warnings_PyLint_ParserName());

            this.descriptions = descriptions;
        }

        @Override
        public String getDescription(final Issue issue) {
            return descriptions.getDescription(issue.getType());
        }
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("pyLint")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        private final PyLintDescriptions descriptions;

        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);

            descriptions = new PyLintDescriptions();
            descriptions.initialize();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider(descriptions);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_PyLint_ParserName();
        }

        @Override
        public String getHelp() {
            return "<p>Create a ./pylintrc that contains:" 
                    + "<p><code>msg-template={path}:{module}:{line}: [{msg_id}({symbol}), {obj}] {msg}</code></p>"
                    + "</p>" 
                    + "<p>Start pylint using the command:" 
                    + "<p><code>pylint --rcfile=./pylintrc CODE > pylint.log</code></p>" 
                    + "</p>";
        }
    }

    /**
     * Provides descriptions for all pylint rules.
     */
    static class PyLintDescriptions {
        static final String NO_DESCRIPTION_FOUND = "no description found";
        private final Map<String, String> descriptionByName = new HashMap<>();
        private final Map<String, String> descriptionById = new HashMap<>();

        int initialize() {
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);

            try {
                JSONArray elements = (JSONArray) parser.parse(PyLint.class.getResourceAsStream("pylint-descriptions.json"));
                for (Object element : elements) {
                    JSONObject object = (JSONObject) element;
                    String description = object.getAsString("description");
                    descriptionByName.put(object.getAsString("name"), description);
                    descriptionById.put(object.getAsString("code"), description);
                }
            }
            catch (ParseException | UnsupportedEncodingException ignored) {
                // ignore all exceptions
            }

            return descriptionByName.size();
        }

        /**
         * Returns the description of PyLint rule with the specified name.
         *
         * @param name
         *         the name of the rule, like 'missing-docstring'
         *
         * @return the description for the specified rule
         */
        String getDescription(final String name) {
            return descriptionByName.getOrDefault(name, descriptionById.getOrDefault(name, NO_DESCRIPTION_FOUND));
        }
    }
}
