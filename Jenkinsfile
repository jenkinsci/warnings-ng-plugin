    Map params = [platform: "docker && highmem", jdk: "8"]

    // Faster build and reduces IO needs
    properties([
        durabilityHint('PERFORMANCE_OPTIMIZED'),
        buildDiscarder(logRotator(numToKeepStr: '5')),
    ])

    def repo = params.containsKey('repo') ? params.repo : null
    def failFast = params.containsKey('failFast') ? params.failFast : true
    def timeoutValue = params.containsKey('timeout') ? params.timeout : 60
    def useAci = params.containsKey('useAci') ? params.useAci : false
    def forceAci = params.containsKey('forceAci') ? params.forceAci : false
    if(timeoutValue > 180) {
      echo "Timeout value requested was $timeoutValue, lowering to 180 to avoid Jenkins project's resource abusive consumption"
      timeoutValue = 180
    }

    boolean publishingIncrementals = false
    boolean archivedArtifacts = false
    Map tasks = [failFast: failFast]
    getConfigurations(params).each { config ->
        String label = config.platform
        String jdk = config.jdk
        String jenkinsVersion = config.jenkins
        String javaLevel = config.javaLevel

        String stageIdentifier = "${label}-${jdk}${jenkinsVersion ? '-' + jenkinsVersion : ''}"
        boolean first = tasks.size() == 1
        boolean skipTests = params?.tests?.skip
        boolean reallyUseAci = (useAci && label == 'linux') || forceAci
        boolean addToolEnv = !reallyUseAci

        if(reallyUseAci) {
            String aciLabel = jdk == '8' ? 'maven' : 'maven-11'
            if(label == 'windows') {
                aciLabel += "-windows"
            }
            label = aciLabel
        }

        tasks[stageIdentifier] = {
            node(label) {
                try {
                    timeout(timeoutValue) {
                        boolean isMaven
                        // Archive artifacts once with pom declared baseline
                        boolean doArchiveArtifacts = !jenkinsVersion && !archivedArtifacts
                        if (doArchiveArtifacts) {
                            archivedArtifacts = true
                        }
                        boolean incrementals // cf. JEP-305

                        stage("Checkout (${stageIdentifier})") {
                            infra.checkout(repo)
                            isMaven = fileExists('pom.xml')
                            incrementals = fileExists('.mvn/extensions.xml') &&
                                    readFile('.mvn/extensions.xml').contains('git-changelist-maven-extension')
                        }

                        String changelistF
                        String m2repo

                        stage("Build (${stageIdentifier})") {
                            String command
                            if (isMaven) {
                                m2repo = "${pwd tmp: true}/m2repo"
                                List<String> mavenOptions = [
                                        '--update-snapshots',
                                        "-Dmaven.repo.local=$m2repo",
                                        '-Dmaven.test.failure.ignore',
                                ]
                                if (incrementals) { // set changelist and activate produce-incrementals profile
                                    mavenOptions += '-Dset.changelist'
                                    if (doArchiveArtifacts) { // ask Maven for the value of -rc999.abc123def456
                                        changelistF = "${pwd tmp: true}/changelist"
                                        mavenOptions += "help:evaluate -Dexpression=changelist -Doutput=$changelistF"
                                    }
                                }
                                if (jenkinsVersion) {
                                    mavenOptions += "-Djenkins.version=${jenkinsVersion} -Daccess-modifier-checker.failOnError=false"
                                }
                                if (javaLevel) {
                                    mavenOptions += "-Djava.level=${javaLevel}"
                                }
                                if (skipTests) {
                                    mavenOptions += "-DskipTests -DskipITs"
                                }
                                mavenOptions += "clean install -npu -DskipITs -DElasticTime.factor=2 -Dsurefire.rerunFailingTestsCount=2"
                                infra.runMaven(mavenOptions, jdk, ["BROWSER=firefox-container"], null, addToolEnv)
                            } else {
                                echo "WARNING: Gradle mode for buildPlugin() is deprecated, please use buildPluginWithGradle()"
                                List<String> gradleOptions = [
                                        '--no-daemon',
                                        'cleanTest',
                                        'build',
                                ]
                                command = "gradlew ${gradleOptions.join(' ')}"
                                if (isUnix()) {
                                    command = "./" + command
                                }
                                infra.runWithJava(command, jdk, null, addToolEnv)
                            }
                        }

                        stage("Archive (${stageIdentifier})") {
                            if (!skipTests) {
                                String testReports
                                if (isMaven) {
                                    testReports = '**/target/surefire-reports/**/*.xml,**/target/failsafe-reports/**/*.xml'
                                } else {
                                    testReports = '**/build/test-results/**/*.xml'
                                }
                                junit testReports
                                // TODO do this in a finally-block so we capture all test results even if one branch aborts early
                            }
                            if (failFast && currentBuild.result == 'UNSTABLE') {
                                error 'There were test failures; halting early'
                            }

                            if (first) {
                                recordIssues enabledForFailure: true, tool: mavenConsole()
                                recordIssues enabledForFailure: true, tool: errorProne()
                                recordIssues enabledForFailure: true, tools: [java(), javaDoc()], sourceCodeEncoding: 'UTF-8'
                                recordIssues tools: [spotBugs(), checkStyle(), pmdParser(), cpd()], sourceCodeEncoding: 'UTF-8'
                                recordIssues enabledForFailure: true, tool: taskScanner(
                                        includePattern:'**/*.java',
                                        excludePattern:'target/**',
                                        highTags:'FIXME',
                                        normalTags:'TODO'), sourceCodeEncoding: 'UTF-8'
                                if (failFast && currentBuild.result == 'UNSTABLE') {
                                    error 'There were static analysis warnings; halting early'
                                }
                                jacoco()
                            }
                            if (doArchiveArtifacts) {
                                if (incrementals) {
                                    String changelist = readFile(changelistF)
                                    dir(m2repo) {
                                        fingerprint '**/*-rc*.*/*-rc*.*' // includes any incrementals consumed
                                        archiveArtifacts artifacts: "**/*$changelist/*$changelist*",
                                                excludes: '**/*.lastUpdated',
                                                allowEmptyArchive: true // in case we forgot to reincrementalify
                                    }
                                    publishingIncrementals = true
                                } else {
                                    String artifacts
                                    if (isMaven) {
                                        artifacts = '**/target/*.hpi,**/target/*.jpi,**/target/*.jar'
                                    } else {
                                        artifacts = '**/build/libs/*.hpi,**/build/libs/*.jpi'
                                    }
                                    archiveArtifacts artifacts: artifacts, fingerprint: true
                                }
                            }
                        }
                    }
                } finally {
                    if (hasDockerLabel()) {
                        if(isUnix()) {
                            sh 'docker system prune --force --all || echo "Failed to cleanup docker images"'
                        } else {
                            bat 'docker system prune --force --all || echo "Failed to cleanup docker images"'
                        }
                    }
                }
            }
        }
    }

    parallel(tasks)
    if (publishingIncrementals) {
        infra.maybePublishIncrementals()
    }

boolean hasDockerLabel() {
    env.NODE_LABELS?.contains("docker")
}

List<Map<String, String>> getConfigurations(Map params) {
    boolean explicit = params.containsKey("configurations")
    boolean implicit = params.containsKey('platforms') || params.containsKey('jdkVersions') || params.containsKey('jenkinsVersions')

    if (explicit && implicit) {
        error '"configurations" option can not be used with either "platforms", "jdkVersions" or "jenkinsVersions"'
    }


    def configs = params.configurations
    configs.each { c ->
        if (!c.platform) {
            error("Configuration field \"platform\" must be specified: $c")
        }
        if (!c.jdk) {
            error("Configuration filed \"jdk\" must be specified: $c")
        }
    }

    if (explicit) return params.configurations

    def platforms = params.containsKey('platforms') ? params.platforms : ['linux', 'windows']
    def jdkVersions = params.containsKey('jdkVersions') ? params.jdkVersions : [8]
    def jenkinsVersions = params.containsKey('jenkinsVersions') ? params.jenkinsVersions : [null]

    def ret = []
    for (p in platforms) {
        for (jdk in jdkVersions) {
            for (jenkins in jenkinsVersions) {
                ret << [
                        "platform": p,
                        "jdk": jdk,
                        "jenkins": jenkins,
                        "javaLevel": null   // not supported in the old format
                ]
            }
        }
    }
    return ret
}

/**
 * Get recommended configurations for testing.
 * Includes testing Java 8 and 11 on the newest LTS.
 */
static List<Map<String, String>> recommendedConfigurations() {
    def recentLTS = "2.164.1"
    def configurations = [
        [ platform: "linux", jdk: "8", jenkins: null ],
        [ platform: "windows", jdk: "8", jenkins: null ],
        [ platform: "linux", jdk: "8", jenkins: recentLTS, javaLevel: "8" ],
        [ platform: "windows", jdk: "8", jenkins: recentLTS, javaLevel: "8" ],
        [ platform: "linux", jdk: "11", jenkins: recentLTS, javaLevel: "8" ],
        [ platform: "windows", jdk: "11", jenkins: recentLTS, javaLevel: "8" ]
    ]
    return configurations
}
