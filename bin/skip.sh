#!/bin/bash

(cd plugin; mvn clean install -Pskip || { echo "Build failed"; exit 1; })

$(dirname "$0")/deploy.sh warnings-ng

