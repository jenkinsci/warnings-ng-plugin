package io.jenkins.plugins.analysis.warnings.axivion;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.utils.URIBuilder;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.io.Serial;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.warnings.axivion.AxivionParser.Config;
import io.jenkins.plugins.util.EnvironmentResolver;
import io.jenkins.plugins.util.JenkinsFacade;
import io.jenkins.plugins.util.LogHandler;

/** Provides a parser and customized messages for the Axivion Suite. */
@SuppressWarnings({"PMD.DataClass", "ClassFanOutComplexity"})
public final class AxivionSuite extends Tool {
    @Serial
    private static final long serialVersionUID = 967222727302169818L;
    private static final String ID = "axivion-suite";
    private static final String NAME = "Axivion Suite";

    private String projectUrl = StringUtils.EMPTY;
    private String credentialsId = StringUtils.EMPTY;
    private String basedir = "$";

    // field was added in 9.1.0
    private String namedFilter = StringUtils.EMPTY;

    // field was added in 9.23.0
    private boolean ignoreSuppressedOrJustified = true;

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

    /**
     * Stapler setter for the projectUrl field. Verifies the url and encodes the path part e.g. whitespaces in project
     * names.
     *
     * @param projectUrl
     *         url to an Axivion dashboard project
     */
    @DataBoundSetter
    public void setProjectUrl(final String projectUrl) {
        try {
            final var url = new URL(projectUrl);
            this.projectUrl = new URIBuilder()
                    .setCharset(StandardCharsets.UTF_8)
                    .setHost(url.getHost())
                    .setPort(url.getPort())
                    .setPath(url.getPath())
                    .setScheme(url.getProtocol())
                    .build()
                    .toString();
        }
        catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalArgumentException("Not a valid project url.", e);
        }
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(final String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getNamedFilter() {
        return this.namedFilter;
    }

    @DataBoundSetter
    public void setNamedFilter(final String namedFilter) {
        this.namedFilter = namedFilter;
    }

    public boolean isIgnoreSuppressedOrJustified() {
        return ignoreSuppressedOrJustified;
    }

    @DataBoundSetter
    public void setIgnoreSuppressedOrJustified(final boolean ignoreSuppressedOrJustified) {
        this.ignoreSuppressedOrJustified = ignoreSuppressedOrJustified;
    }

    /**
     * Called after deserialization to retain backward compatibility.
     *
     * @return this
     */
    @Override
    protected Object readResolve() {
        // field was added in 9.1.0
        if (namedFilter == null) {
            namedFilter = StringUtils.EMPTY;
        }
        return super.readResolve();
    }

    @Override
    public Report scan(final Run<?, ?> run, final FilePath workspace, final Charset sourceCodeEncoding,
            final LogHandler logger) throws ParsingException, ParsingCanceledException {
        final var httpClientCredentials = withValidCredentials(run.getParent());
        final var dashboard = new RemoteAxivionDashboard(projectUrl, httpClientCredentials, namedFilter);
        final var config = new Config(projectUrl, expandBaseDir(run, basedir),
                ignoreSuppressedOrJustified);
        final var parser = new AxivionParser(config);

        final var report = new Report(ID, NAME);
        report.logInfo("Axivion webservice: %s", projectUrl);
        report.logInfo("Local basedir: %s", basedir);
        report.logInfo("Named Filter: %s", namedFilter);
        report.logInfo("Ignore suppressed or justified: %s", ignoreSuppressedOrJustified);

        for (AxIssueKind kind : AxIssueKind.values()) {
            final var payload = dashboard.getIssues(kind);
            parser.parse(report, kind, payload);
        }

        return report;
    }

    private UsernamePasswordCredentials withValidCredentials(final Item context) {
        final List<StandardUsernamePasswordCredentials> all =
                CredentialsProvider.lookupCredentials(
                        StandardUsernamePasswordCredentials.class,
                        context,
                        null,
                        Collections.emptyList());

        final StandardUsernamePasswordCredentials jenkinsCredentials =
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
            var environmentResolver = new EnvironmentResolver();

            expandedBasedir =
                    environmentResolver.expandEnvironmentVariables(
                            run.getEnvironment(TaskListener.NULL), baseDir);
        }
        catch (IOException | InterruptedException ignore) {
            expandedBasedir = baseDir;
        }
        return expandedBasedir;
    }

    /** Provides the Axivion icons. */
    private static class LabelProvider extends IconLabelProvider {
        LabelProvider() {
            super(ID, "Axivion Suite", EMPTY_DESCRIPTION, "axivion");
        }
    }

    /** Descriptor for {@link AxivionSuite}. * */
    @Symbol({"axivionSuite", "axivion"})
    @Extension
    public static class AxivionSuiteToolDescriptor extends ToolDescriptor {
        private static final JenkinsFacade JENKINS = new JenkinsFacade();

        /** Creates the descriptor instance. */
        public AxivionSuiteToolDescriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return NAME;
        }

        @Override
        public String getHelp() {
            return "For using Axivion Suite, set up your analysis project and the web service. Provide the URL and credentials.";
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider();
        }

        /**
         * Dashboard project url must be a valid url.
         *
         * @param project
         *         the project that is configured
         * @param projectUrl
         *         url to a project inside an Axivion dashboard
         *
         * @return {@link FormValidation#ok()} is a valid url
         */
        @POST
        public FormValidation doCheckProjectUrl(@AncestorInPath final BuildableItem project,
                @QueryParameter final String projectUrl) {
            if (!JENKINS.hasPermission(Item.CONFIGURE, project)) {
                return FormValidation.ok();
            }

            try {
                new URL(projectUrl).toURI();

                return FormValidation.ok();
            }
            catch (URISyntaxException | MalformedURLException ex) {
                return FormValidation.error("This is not a valid URL.");
            }
        }

        /**
         * Checks whether the given path is a correct os path.
         *
         * @param project
         *         the project that is configured
         * @param basedir
         *         path to check
         *
         * @return {@link FormValidation#ok()} is a valid url
         */
        @SuppressFBWarnings("PATH_TRAVERSAL_IN")
        @POST
        public FormValidation doCheckBasedir(@AncestorInPath final BuildableItem project,
                @QueryParameter final String basedir) {
            if (!JENKINS.hasPermission(Item.CONFIGURE, project)) {
                return FormValidation.ok();
            }

            try {
                if (!basedir.contains("$")) {
                    // path with a variable cannot be checked at this point
                    Path.of(basedir);
                }
                return FormValidation.ok();
            }
            catch (InvalidPathException e) {
                return FormValidation.error("You have to provide a valid path.");
            }
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
        @POST
        public FormValidation doCheckCredentialsId(
                @AncestorInPath final Item item, @QueryParameter final String credentialsId) {
            if (StringUtils.isBlank(credentialsId)) {
                return FormValidation.error("You have to provide credentials.");
            }
            if (item == null) {
                if (!JENKINS.hasPermission(Jenkins.ADMINISTER)) {
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
                            null,
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
        @POST
        public ListBoxModel doFillCredentialsIdItems(
                @AncestorInPath final Item item, @QueryParameter final String credentialsId) {
            final var result = new StandardListBoxModel();
            if (item == null) {
                if (!JENKINS.hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(credentialsId);
                }
            }
            else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                        && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(credentialsId);
                }
            }

            final ListBoxModel credentials = CredentialsProvider.listCredentials(
                    StandardUsernamePasswordCredentials.class,
                    item,
                    null,
                    Collections.emptyList(),
                    CredentialsMatchers.always()
            );

            result.addMissing(credentials);
            return result.includeCurrentValue(credentialsId);
        }
    }
}
