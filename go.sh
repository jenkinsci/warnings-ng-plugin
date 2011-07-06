rm -rf $HUDSON_HOME/plugins/analysis-core*

mvn install || { echo "Build failed"; exit 1; }

cp -f target/analysis-core.hpi $HUDSON_HOME/plugins/

cd $HUDSON_HOME
java -jar jenkins.war
