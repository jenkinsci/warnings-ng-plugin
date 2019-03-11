#!/bin/bash

if [[ -z "$JENKINS_HOME" ]]; then
    JENKINS_HOME=../docker/volumes/jenkins-home
    echo "JENKINS_HOME is not defined, using $JENKINS_HOME"
fi

mvn install -DskipTests || { echo "Build failed"; exit 1; }

echo "Installing plugin in $JENKINS_HOME"
rm -rf $JENKINS_HOME/plugins/warnings-ng*
cp -fv target/warnings-ng.hpi $JENKINS_HOME/plugins/warnings-ng.jpi

IS_RUNNING=`docker-compose ps -q jenkins-master`
if [[ "$IS_RUNNING" != "" ]]; then
    echo "Restarting Jenkins..."
    docker-compose restart
fi

