#!/bin/bash

set -e

JENKINS_HOME=../docker/volumes/jenkins-home

echo "Installing plugin ${1} in $JENKINS_HOME"
rm -rf $JENKINS_HOME/plugins/${1}*
cp -fv plugin/target/${1}.hpi $JENKINS_HOME/plugins/${1}.jpi

CURRENT_UID="$(id -u):$(id -g)"
export CURRENT_UID
IS_RUNNING=$(docker compose ps -q devenv-jenkins)
if [[ "$IS_RUNNING" != "" ]]; then
    docker compose restart
    echo "Restarting Jenkins (docker compose with user ID ${CURRENT_UID}) ..."
fi

