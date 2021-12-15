package io.jenkins.plugins.analysis.core.model;

import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.groovy.GroovyParser;
import io.jenkins.plugins.analysis.warnings.groovy.GroovyScript;
import io.jenkins.plugins.analysis.warnings.groovy.ParserConfiguration;

public class ToolITest extends IntegrationTestWithJenkinsPerSuite {

    @Test
    @Issue("SECURITY-2090")
    public void setIdShouldThrowExceptionIfCustomIdHasInvalidPattern() {
        ParserConfiguration configuration = ParserConfiguration.getInstance();
        configuration.setParsers(Collections.singletonList(new GroovyParser("groovy", "", "", "", "")));
        Tool groovyScript = new GroovyScript("groovy");

        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> groovyScript.setId("../../invalid-id"));
    }
}