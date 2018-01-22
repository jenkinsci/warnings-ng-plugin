package io.jenkins.plugins.analysis.core.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;

import edu.hm.hafner.util.Ensure;
import edu.hm.hafner.util.NoSuchElementException;
import edu.hm.hafner.util.VisibleForTesting;
import io.jenkins.plugins.analysis.core.JenkinsFacade;

import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.util.CopyOnWriteMap.Hash;

/**
 * Registry for static analysis tools that conform to the interface {@link StaticAnalysisTool}. Tools are registered and
 * created automatically by extending the class {@link StaticAnalysisTool} and marking the class with the {@link
 * Extension} annotation. Additionally, tools could be registered dynamically by factory classes that implement the the
 * extension point {@link StaticAnalysisToolFactory}.
 *
 * @author Ullrich Hafner
 */
public final class ToolRegistry {
    private final Map<String, StaticAnalysisTool> tools = new Hash<>();

    /**
     * Creates a new instance of {@link ToolRegistry}.
     */
    public ToolRegistry() {
        this(new JenkinsFacade());
    }

    @VisibleForTesting
    ToolRegistry(final JenkinsFacade jenkins) {
        List<StaticAnalysisTool> extensions = jenkins.getExtensionsFor(StaticAnalysisTool.class);
        for (StaticAnalysisTool extension : extensions) {
            register(extension);
        }
        List<StaticAnalysisToolFactory> factories = jenkins.getExtensionsFor(StaticAnalysisToolFactory.class);
        for (StaticAnalysisToolFactory factory : factories) {
            registerAll(factory.getTools());
        }
    }

    private void registerAll(final Collection<StaticAnalysisTool> tools) {
        for (StaticAnalysisTool tool : tools) {
            register(tool);
        }
    }

    /**
     * Registers the specified static analysis tool.
     *
     * @param tool
     *         the tool to register
     *
     * @throws AssertionError
     *         if the ID of the tool has been used already
     */
    private void register(final StaticAnalysisTool tool) {
        String key = tool.getId();

        Ensure.that(tools.containsKey(key)).isFalse("Tool with key '%s' already registered.", key);

        tools.put(key, tool);
    }

    /**
     * Returns the IDs of the registered static analysis tools.
     *
     * @return the IDs
     */
    public ImmutableSet<String> getIds() {
        return Sets.immutable.ofAll(tools.keySet());
    }

    /**
     * Returns the IDs of the registered static analysis tools.
     *
     * @return the IDs
     */
    public ImmutableList<StaticAnalysisTool> getAll() {
        return Lists.immutable.ofAll(tools.values());
    }

    /**
     * Finds the static analysis tool with the specified ID.
     *
     * @param id
     *         the ID of the tool to find
     *
     * @return the static analysis tool
     * @throws java.util.NoSuchElementException
     *         if the tool could not be found
     */
    public StaticAnalysisTool find(final String id) {
        if (contains(id)) {
            return tools.get(id);
        }
        throw new NoSuchElementException("No such tool registered with id '%s'.", id);
    }

    /**
     * Returns whether a static analysis tool with the specified ID exists.
     *
     * @param id
     *         the ID of the tool to find
     *
     * @return {@code true} if a tool with the specified ID exists, {@code false} otherwise
     */
    public boolean contains(final String id) {
        return tools.containsKey(id);
    }

    /**
     * Provides additional {@link StaticAnalysisTool static analysis tool} instances that are created dynamically.
     */
    public interface StaticAnalysisToolFactory extends ExtensionPoint {
        /**
         * Returns the additional static analysis tools.
         *
         * @return the tools
         */
        List<StaticAnalysisTool> getTools();
    }
}
