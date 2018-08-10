package hudson.plugins.warnings;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Aggregates {@link WarningsResultAction}s of {@link MatrixRun}s into
 * {@link MatrixBuild}.
 *
 * @author Ullrich Hafner
 */
public class WarningsAnnotationsAggregator extends MatrixAggregator {
    private final HealthDescriptor healthDescriptor;
    private final String defaultEncoding;
    private final Map<String, ParserResult> totalsPerParser = Maps.newHashMap();
    private final boolean usePreviousBuildAsReference;
    private final boolean useStableBuildAsReference;

    /**
     * Creates a new instance of {@link WarningsAnnotationsAggregator}.
     *
     * @param build
     *            the matrix build
     * @param launcher
     *            the launcher
     * @param listener
     *            the build listener
     * @param healthDescriptor
     *            health descriptor
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param usePreviousBuildAsReference
     *            determines whether the previous build should be used as the
     *            reference build
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as
     *            reference builds or not
     */
    public WarningsAnnotationsAggregator(final MatrixBuild build, final Launcher launcher, final BuildListener listener,
            final HealthDescriptor healthDescriptor, final String defaultEncoding,
            final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference) {
        super(build, launcher, listener);

        this.healthDescriptor = healthDescriptor;
        this.defaultEncoding = defaultEncoding;
        this.usePreviousBuildAsReference = usePreviousBuildAsReference;
        this.useStableBuildAsReference = useStableBuildAsReference;
    }

    @Override
    public boolean endRun(final MatrixRun run) throws InterruptedException, IOException {
        List<WarningsResultAction> actions = run.getActions(WarningsResultAction.class);
        if (!actions.isEmpty()) {
            for (WarningsResultAction action : actions) {
                initializeParser(action.getParser());

                ParserResult aggregation = totalsPerParser.get(action.getParser());
                String configurationName = run.getParent().getName();
                WarningsResult result = action.getResult();
                addAllWarnings(aggregation, result, configurationName);
                aggregation.addModules(appendConfigurationNameToModule(result, configurationName));
            }
        }
        return true;
    }

    private List<String> appendConfigurationNameToModule(final WarningsResult result, final String configurationName) {
        List<String> modulesByConfiguration = Lists.newArrayList();
        Collection<String> modules = result.getModules();
        if (modules.isEmpty()) {
            modulesByConfiguration.add(configurationName);
        }
        else {
            for (String module : modules) {
                modulesByConfiguration.add(configurationName + " - " + module);
            }
        }
        return modulesByConfiguration;
    }

    private void addAllWarnings(final ParserResult aggregation, final WarningsResult result, final String configurationName) {
        Set<FileAnnotation> annotations = result.getAnnotations();
        for (FileAnnotation annotation : annotations) {
            annotation.setModuleName(configurationName);
        }
        aggregation.addAnnotations(annotations);
    }

    private void initializeParser(final String parserName) {
        if (!totalsPerParser.containsKey(parserName)) {
            totalsPerParser.put(parserName, new ParserResult());
        }
    }

    private void createTotalsAction() {
        ParserResult totals = new ParserResult();
        for (ParserResult result : totalsPerParser.values()) {
            totals.addProject(result);
        }
        BuildHistory history = new BuildHistory((Run<?, ?>)build, AggregatedWarningsResultAction.class,
                usePreviousBuildAsReference, useStableBuildAsReference);
        AggregatedWarningsResult result = new AggregatedWarningsResult(build, history, totals, defaultEncoding);
        build.addAction(new AggregatedWarningsResultAction(build, result));
    }

    @Override
    public boolean endBuild() throws InterruptedException, IOException {
        for (String parser : totalsPerParser.keySet()) {
            WarningsBuildHistory history = new WarningsBuildHistory(build, parser,
                    usePreviousBuildAsReference, useStableBuildAsReference);
            WarningsResult result = new WarningsResult(build, history, totalsPerParser.get(parser), defaultEncoding, parser);
            build.addAction(new WarningsResultAction(build, healthDescriptor, result, parser));
        }
        createTotalsAction();

        return true;
    }
}

