#!/bin/sh

git pull
git push
mvn -B release:prepare release:perform


