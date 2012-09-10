rm -rf $JENKINS_HOME/plugins/analysis-core*

mvn install || { echo "Build failed"; exit 1; }

cp -f target/analysis-core.hpi $JENKINS_HOME/plugins/

cd $JENKINS_HOME
./go.sh
