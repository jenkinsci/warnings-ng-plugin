package hudson.plugins.warnings;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;
import hudson.model.BuildListener;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.ParserResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Aggregates {@link WarningsResultAction}s of {@link MatrixRun}s into
 * {@link MatrixBuild}.
 *
 * @author Ulli Hafner
 */
public class WarningsAnnotationsAggregator extends MatrixAggregator {
    private final HealthDescriptor healthDescriptor;
    private final String defaultEncoding;
    private final Map<String, ParserResult> totalsPerParser = Maps.newHashMap();

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
     */
    public WarningsAnnotationsAggregator(final MatrixBuild build, final Launcher launcher, final BuildListener listener,
            final HealthDescriptor healthDescriptor, final String defaultEncoding) {
        super(build, launcher, listener);

        this.healthDescriptor = healthDescriptor;
        this.defaultEncoding = defaultEncoding;
    }

    @Override
    public boolean endRun(final MatrixRun run) throws InterruptedException, IOException {
        List<WarningsResultAction> actions = run.getActions(WarningsResultAction.class);
        if (!actions.isEmpty()) {
            for (WarningsResultAction action : actions) {
                if (!totalsPerParser.containsKey(action.getParser())) {
                    WarningsResult result = action.getResult();
                    ParserResult aggregation = new ParserResult();
                    aggregation.addAnnotations(result.getAnnotations());
                    aggregation.addModules(result.getModules());
                    totalsPerParser.put(action.getParser(), aggregation);
                }
            }
        }
        return true;
    }

    @Override
    public boolean endBuild() throws InterruptedException, IOException {
        for (String parser : totalsPerParser.keySet()) {
            WarningsBuildHistory history = new WarningsBuildHistory(build, parser);
            WarningsResult result = new WarningsResult(build, history, totalsPerParser.get(parser), defaultEncoding, parser);
            build.addAction(new WarningsResultAction(build, healthDescriptor, result, parser));
        }

        return true;
    }
}

