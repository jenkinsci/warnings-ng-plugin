package io.jenkins.plugins.analysis.warnings.axivion;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.UsernamePasswordCredentials;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.util.EnvironmentResolver;
import io.jenkins.plugins.analysis.core.util.JenkinsFacade;
import io.jenkins.plugins.analysis.core.util.LogHandler;

/** Provides a parser and customized messages for the Axivion Suite. */
public class AxivionSuite extends Tool {

    private static final long serialVersionUID = 967222727302169818L;
    static final String ID = "axivion-suite";

    private String projectUrl = StringUtils.EMPTY;
    private String credentialsId = StringUtils.EMPTY;
    private String basedir = "$";

    @VisibleForTesting
    AxivionSuite(final String projectUrl, final String credentialsId, final String basedir) {
        super();
        setBasedir(basedir);
        setCredentialsId(credentialsId);
        setProjectUrl(projectUrl);
    }

    /** Creates a new instance of {@link AxivionSuite}. */
    @DataBoundConstructor
    public AxivionSuite() {
        super();
        // empty constructor required for stapler
    }

    public String getBasedir() {
        return basedir;
    }

    @DataBoundSetter
    public void setBasedir(final String basedir) {
        this.basedir = basedir;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    @DataBoundSetter
    public void setProjectUrl(final String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(final String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @Override
    public Report scan(final Run<?, ?> run, final FilePath workspace, final Charset sourceCodeEncoding,
            final LogHandler logger)
            throws ParsingException, ParsingCanceledException {

        AxivionDashboard dashboard = new RemoteAxivionDashboard(projectUrl, withValidCredentials());
        AxivionParser parser = new AxivionParser(projectUrl, expandBaseDir(run, this.basedir));

        Report report = new Report();
        report.logInfo("Axivion webservice: " + this.projectUrl);
        report.logInfo("Local basedir: " + this.basedir);

        for (AxIssueKind kind : AxIssueKind.values()) {
            JSONObject payload = dashboard.getIssues(kind);
            parser.parse(report, kind, payload);
        }

        return report;
    }

    private UsernamePasswordCredentials withValidCredentials() {
        final List<StandardUsernamePasswordCredentials> all =
                CredentialsProvider.lookupCredentials(
                        StandardUsernamePasswordCredentials.class,
                        (Item) null,
                        ACL.SYSTEM,
                        Collections.emptyList());

        StandardUsernamePasswordCredentials jenkinsCredentials =
                CredentialsMatchers.firstOrNull(all,
                        CredentialsMatchers.withId(credentialsId));

        if (jenkinsCredentials == null) {
            throw new ParsingException("Could not find the credentials for " + credentialsId);
        }

        return new UsernamePasswordCredentials(
                jenkinsCredentials.getUsername(),
                Secret.toString(jenkinsCredentials.getPassword()));
    }

    private static String expandBaseDir(final Run<?, ?> run, final String baseDir) {
        String expandedBasedir;
        try {
            EnvironmentResolver environmentResolver = new EnvironmentResolver();

            expandedBasedir =
                    environmentResolver.expandEnvironmentVariables(
                            run.getEnvironment(TaskListener.NULL), baseDir);
        }
        catch (IOException | InterruptedException ignore) {
            expandedBasedir = baseDir;
        }
        return expandedBasedir;
    }

    /** Descriptor for {@link AxivionSuite}. * */
    @Symbol({"axivionSuite", "axivion"})
    @Extension
    public static class AxivionSuiteToolDescriptor extends ToolDescriptor {

        /** Creates the descriptor instance. */
        public AxivionSuiteToolDescriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Axivion Suite";
        }

        @Override
        public String getHelp() {
            return "For using Axivion Suite, set up your analysis project and the web service. Provide the URL and credentials.";
        }

        /**
         * Dashboard project url must be a valid url.
         *
         * @param projectUrl
         *         url to a project inside an Axivion dashboard
         *
         * @return {@link FormValidation#ok()} is a valid url
         */
        public FormValidation doCheckProjectUrl(@QueryParameter final String projectUrl) {
            try {
                new URL(projectUrl).toURI();
            }
            catch (URISyntaxException | MalformedURLException ex) {
                return FormValidation.error("This is not a valid URL.");
            }
            return FormValidation.ok();
        }

        /**
         * Checks whether the given path is a correct os path.
         *
         * @param basedir
         *         path to check
         *
         * @return {@link FormValidation#ok()} is a valid url
         */
        public FormValidation doCheckBasedir(@QueryParameter final String basedir) {
            try {
                if (!basedir.contains("$")) {
                    // path with a variable cannot be checked at this point
                    Paths.get(basedir);
                }
            }
            catch (InvalidPathException e) {
                return FormValidation.error("You have to provide a valid path.");
            }
            return FormValidation.ok();
        }

        /**
         * Checks whether valid credentials are given.
         *
         * @param item
         *         jenkins configuration
         * @param credentialsId
         *         id of the stored credentials pair
         *
         * @return {@link FormValidation#ok()} if credentials exist and are valid
         */
        public FormValidation doCheckCredentialsId(
                @AncestorInPath Item item, @QueryParameter String credentialsId) {
            if (StringUtils.isBlank(credentialsId)) {
                return FormValidation.error("You have to provide credentials.");
            }
            if (item == null) {
                if (!new JenkinsFacade().hasPermission(Jenkins.ADMINISTER)) {
                    return FormValidation.ok();
                }
            }
            else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                        && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return FormValidation.ok();
                }
            }

            if (CredentialsMatchers.firstOrNull(
                    CredentialsProvider.lookupCredentials(
                            StandardUsernamePasswordCredentials.class,
                            item,
                            ACL.SYSTEM,
                            Collections.emptyList()),
                    CredentialsMatchers.withId(credentialsId))
                    == null) {
                return FormValidation.error("Cannot find currently selected credentials.");
            }
            return FormValidation.ok();
        }

        /**
         * Shows the user all available credential id items.
         *
         * @param item
         *         jenkins configuration
         * @param credentialsId
         *         current used credentials
         *
         * @return a list view of all credential ids
         */
        public ListBoxModel doFillCredentialsIdItems(
                @AncestorInPath Item item, @QueryParameter String credentialsId) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!new JenkinsFacade().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(credentialsId);
                }
            }
            else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                        && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(credentialsId);
                }
            }
            return result.includeAs(
                    ACL.SYSTEM,
                    item,
                    StandardUsernamePasswordCredentials.class,
                    Collections.emptyList())
                    .includeCurrentValue(credentialsId);
        }
    }
}
