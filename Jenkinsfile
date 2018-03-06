node ('linux') {
    timeout(60) {
        stage ('Checkout') {
            checkout scm
        }

        stage ('Build') {
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
            mavenOptions += "clean verify checkstyle:checkstyle pmd:pmd findbugs:findbugs jacoco:prepare-agent test jacoco:report"
            command = "mvn ${mavenOptions.join(' ')}"
            env << "PATH+MAVEN=${tool 'mvn'}/bin"

            withEnv(env) {
                sh command
            }

            junit testResults: '**/target/surefire-reports/TEST-*.xml'
            warnings consoleParsers: [[parserName: 'Java Compiler (javac)'], [parserName: 'JavaDoc']]
            checkstyle pattern: '**/target/checkstyle-result.xml'
            findbugs pattern: '**/target/*Xml.xml'
            pmd pattern: '**/target/pmd.xml'
            jacoco()
        }
    }

}