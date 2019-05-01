node ('linux') {
    timeout(200) {
        stage ('Linux Checkout') {
            checkout scm
        }

        stage ('Linux Build') {
            String jdk = '8'
            String jdkTool = "jdk${jdk}"
            List<String> env = [
                    "JAVA_HOME=${tool jdkTool}",
                    'PATH+JAVA=${JAVA_HOME}/bin',
            ]
            String command
            List<String> mavenOptions = [
                    '--batch-mode',
                    '--errors',
                    '--update-snapshots',
                    '-Dmaven.test.failure.ignore',
            ]
            if (jdk.toInteger() > 7 && infra.isRunningOnJenkinsInfra()) {
                /* Azure mirror only works for sufficiently new versions of the JDK due to Letsencrypt cert */
                def settingsXml = "${pwd tmp: true}/settings-azure.xml"
                writeFile file: settingsXml, text: libraryResource('settings-azure.xml')
                mavenOptions += "-s $settingsXml"
            }
            mavenOptions += "clean verify jacoco:prepare-agent test jacoco:report -Djenkins.test.timeout=240 -Djenkins.version=2.150.1"
            command = "mvn ${mavenOptions.join(' ')}"
            env << "PATH+MAVEN=${tool 'mvn'}/bin"

            withEnv(env) {
                sh command
            }

            archiveArtifacts artifacts: '**/target/*.hpi', fingerprint: true
            junit testResults: '**/target/*-reports/TEST-*.xml'
            recordIssues enabledForFailure: true, tool: mavenConsole(), referenceJobName: 'Plugins/warnings-ng-plugin/master'
            recordIssues enabledForFailure: true, tools: [java(), javaDoc()], sourceCodeEncoding: 'UTF-8', referenceJobName: 'Plugins/warnings-ng-plugin/master'
            recordIssues enabledForFailure: true, tool: checkStyle(pattern: 'target/checkstyle.xml'), sourceCodeEncoding: 'UTF-8', referenceJobName: 'Plugins/warnings-ng-plugin/master'
            recordIssues enabledForFailure: true, tool: cpd(pattern: 'target/cpd.xml'), sourceCodeEncoding: 'UTF-8', referenceJobName: 'Plugins/warnings-ng-plugin/master'
            recordIssues enabledForFailure: true, tool: pmdParser(pattern: 'target/pmd.xml'), sourceCodeEncoding: 'UTF-8', referenceJobName: 'Plugins/warnings-ng-plugin/master'
            recordIssues enabledForFailure: true, tool: spotBugs(pattern: 'target/spotbugsXml.xml'), sourceCodeEncoding: 'UTF-8', referenceJobName: 'Plugins/warnings-ng-plugin/master'
            recordIssues enabledForFailure: true, tool: taskScanner(includePattern:'**/*.java', excludePattern:'target/**/*', highTags:'FIXME', normalTags:'TODO'), sourceCodeEncoding: 'UTF-8', referenceJobName: 'Plugins/warnings-ng-plugin/master'
            jacoco()
        }
    }
}

node ('windows') {
    timeout(200) {
        stage ('Windows Checkout') {
            checkout scm
        }

        stage ('Windows Build') {
            String jdk = '8'
            String jdkTool = "jdk${jdk}"
            List<String> env = [
                    "JAVA_HOME=${tool jdkTool}",
                    'PATH+JAVA=${JAVA_HOME}/bin',
            ]
            String command
            List<String> mavenOptions = [
                    '--batch-mode',
                    '--errors',
                    '--update-snapshots',
                    '-Dmaven.test.failure.ignore',
            ]
            if (jdk.toInteger() > 7 && infra.isRunningOnJenkinsInfra()) {
                /* Azure mirror only works for sufficiently new versions of the JDK due to Letsencrypt cert */
                def settingsXml = "${pwd tmp: true}/settings-azure.xml"
                writeFile file: settingsXml, text: libraryResource('settings-azure.xml')
                mavenOptions += "-s $settingsXml"
            }
            mavenOptions += "clean verify -Djenkins.test.timeout=240 -Djenkins.version=2.150.1"
            command = "mvn ${mavenOptions.join(' ')}"
            env << "PATH+MAVEN=${tool 'mvn'}/bin"

            withEnv(env) {
                bat command
            }

            junit testResults: '**/target/*-reports/TEST-*.xml'
        }
    }
}