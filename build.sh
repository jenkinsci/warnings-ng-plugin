rm -rf $JENKINS_HOME/plugins/analysis-core*

mvn install
cp -f target/analysis-core.hpi $JENKINS_HOME/plugins/
