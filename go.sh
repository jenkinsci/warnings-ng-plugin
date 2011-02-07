rm -rf $HUDSON_HOME/plugins/analysis-core*

mvn install
cp -f target/analysis-core.hpi $HUDSON_HOME/plugins/

cd $HUDSON_HOME
java -jar hudson.war
