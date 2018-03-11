#!/bin/sh 

if [[ -z "$JENKINS_HOME" ]]; then
    echo "JENKINS_HOME is not defined" 1>&2
    exit 1
fi

cd $JENKINS_HOME
./go.sh
cd -
