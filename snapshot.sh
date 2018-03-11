#!/bin/sh

mvn install deploy:deploy -Durl=https://repo.jenkins-ci.org/snapshots/ -Did=maven.jenkins-ci.org
