#!/bin/bash

if [[ -z "$JENKINS_HOME" ]]; then
    echo "JENKINS_HOME is not defined" 1>&2
    exit 1
fi

mvn verify || { echo "Build failed"; exit 1; }

rm -rf $JENKINS_HOME/plugins/warnings*

cp -fv target/*.hpi $JENKINS_HOME/plugins



