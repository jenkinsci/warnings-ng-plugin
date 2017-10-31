#!/bin/sh

rm -rf $JENKINS_HOME/plugins/analysis-core*

mvn clean install
cp -f target/analysis-core.hpi $JENKINS_HOME/plugins/

cd $JENKINS_HOME
./go.sh
