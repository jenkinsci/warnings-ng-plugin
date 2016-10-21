#!/bin/sh

git pull
git push
mvn -B -Djava.net.id=drulli release:prepare release:perform


