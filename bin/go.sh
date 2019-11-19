#!/bin/bash

JENKINS_HOME=../docker/volumes/jenkins-home

mvn install -DskipITs || { echo "Build failed"; exit 1; }

echo "Installing plugin in $JENKINS_HOME"
rm -rf $JENKINS_HOME/plugins/warnings-ng*
cp -fv target/warnings-ng.hpi $JENKINS_HOME/plugins/warnings-ng.jpi

CURRENT_UID="$(id -u):$(id -g)"
export CURRENT_UID
IS_RUNNING=$(docker-compose ps -q jenkins-master)
if [[ "$IS_RUNNING" != "" ]]; then
    docker-compose restart
    echo "Restarting Jenkins (docker compose with user ID ${CURRENT_UID}) ..."
fi

