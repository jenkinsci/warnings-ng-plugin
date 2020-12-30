#!/bin/sh

git pull
git push
mvn -B clean build-helper:parse-version release:prepare release:perform -DdevelopmentVersion=\${parsedVersion.majorVersion}.\${parsedVersion.nextMinorVersion}.0-SNAPSHOT


