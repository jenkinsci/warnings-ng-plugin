package io.jenkins.plugins.analysis.warnings.benchmark;

import org.openjdk.jmh.annotations.Benchmark;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import jenkins.benchmark.jmh.JmhBenchmark;
import jenkins.benchmark.jmh.JmhBenchmarkState;

/**
 * Provides a checkstyle and pmd analysis benchmark test.
 *
 * @author Kevin Richter
 * @author Simon Schönwiese
 */
@JmhBenchmark
public class StaticAnalysisBenchmarkITest {
    public static class JenkinsState extends JmhBenchmarkState {
        private WorkflowJob job;

        public WorkflowJob getJob() {
            return job;
        }

        @Override
        public void setup() throws Exception {
            job = getJenkins().createProject(WorkflowJob.class, "Benchmark Job");
            configureScanner();
        }

        private void configureScanner() {
            String checkstyleFileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<checkstyle version=\"4.1\">"
                    + "<file name=\"package.html\">"
                    + "<error line=\"0\" severity=\"error\" message=\"Fehlende Package-Dokumentation.\" source=\"com.puppycrawl.tools.checkstyle.checks.javadoc.PackageHtmlCheck\"/>"
                    + "</file>"
                    + "<file name=\"CsharpNamespaceDetector.java\">"
                    + "<error line=\"17\" column=\"5\" severity=\"error\" message=\"Die Methode accepts&apos; ist nicht für Vererbung entworfen - muss abstract, final oder leer sein.\" source=\"com.puppycrawl.tools.checkstyle.checks.design.DesignForExtensionCheck\"/>"
                    + "<error line=\"42\" severity=\"error\" message=\"Zeile länger als 80 Zeichen\" source=\"com.puppycrawl.tools.checkstyle.checks.sizes.LineLengthCheck\"/>"
                    + "<error line=\"22\" column=\"5\" severity=\"error\" message=\"Die Methode detectPackageName&apos; ist nicht fr Vererbung entworfen - muss abstract, final oder leer sein.\" source=\"com.puppycrawl.tools.checkstyle.checks.design.DesignForExtensionCheck\"/>"
                    + "</file>"
                    + "</checkstyle>";

            String pmdFileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<pmd version=\"4.1\" timestamp=\"2008-03-18T22:17:01.031\">"
                    + "<file name=\"CopyToClipboard.java\">"
                    + "<violation beginline=\"54\" endline=\"61\" begincolumn=\"21\" endcolumn=\"21\" rule=\"CollapsibleIfStatements\" ruleset=\"Basic\" package=\"com.avaloq.adt.env.internal.ui.actions\" class=\"CopyToClipboard\" method=\"execute\" externalInfoUrl=\"http://pmd.sourceforge.net/rules/basic.html#CollapsibleIfStatements\" priority=\"3\">"
                    + "                    These nested if statements could be combined"
                    + "</violation>"
                    + "</file>"
                    + "<file name=\"ChangeSelectionAction.java\">"
                    + "<violation beginline=\"14\" endline=\"14\" begincolumn=\"1\" endcolumn=\"37\" rule=\"UnusedImports\" ruleset=\"Import Statement Rules\" package=\"com.avaloq.adt.env.internal.ui.actions.change\" externalInfoUrl=\"http://pmd.sourceforge.net/rules/imports.html#UnusedImports\" priority=\"4\">"
                    + "                    Avoid unused imports such as org.eclipse.ui.IWorkbenchPart"
                    + "                    </violation>"
                    + "</file>"
                    + "<file name=\"SelectSourceDialog.java\">"
                    + "<violation beginline=\"938\" endline=\"940\" begincolumn=\"13\" endcolumn=\"13\" rule=\"EmptyCatchBlock\" ruleset=\"Basic Rules\" package=\"com.avaloq.adt.env.internal.ui.dialogs\" class=\"SourceRunnable\" method=\"run\" externalInfoUrl=\"http://pmd.sourceforge.net/rules/basic.html#EmptyCatchBlock\" priority=\"5\">"
                    + "                    Avoid empty catch blocks"
                    + "                    </violation>"
                    + "<violation beginline=\"980\" endline=\"982\" begincolumn=\"13\" endcolumn=\"13\" rule=\"EmptyCatchBlock\" ruleset=\"Basic Rules\" package=\"com.avaloq.adt.env.internal.ui.dialogs\" class=\"HistRunnable\" method=\"run\" externalInfoUrl=\"http://pmd.sourceforge.net/rules/basic.html#EmptyCatchBlock\" priority=\"2\">"
                    + "                    Avoid empty catch blocks"
                    + "                    </violation>"
                    + "</file>"
                    + "</pmd>";
            job.setDefinition(new CpsFlowDefinition("node {\n"
                    + "  stage ('Integration Test') {\n"
                    + "         writeFile file: 'checkstyle.txt', text: '" + checkstyleFileContent + "'\n"
                    + "         writeFile file: 'pmd.txt', text: '" + pmdFileContent + "'\n"
                    + "         recordIssues tools: [checkStyle(pattern: '**/checkstyle*'),\n"
                    + "             pmdParser(pattern: '**/pmd*')]\n"
                    + "  }\n"
                    + "}", true));
        }

    }

    @Benchmark
    public void benchmark(final JenkinsState state) {
        state.getJob().scheduleBuild2(0);
    }
}