#!/bin/sh

git pull
git push
mvn -B release:prepare release:perform -Djenkins.test.timeout=1000


