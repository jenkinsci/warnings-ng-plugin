export MAVEN_OPTS="-Xmx1024m -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"
rm -rf $HUDSON_HOME/plugins/warnings*
mvn clean hpi:run
