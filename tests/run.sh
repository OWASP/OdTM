#!/bin/bash

# give folder name as an argument

FOLDER="${1}"
APP="../applications/OdTMServer"
CONF="../../tests/${FOLDER}/test.properties"

cd $APP

mvn -e exec:java -q -Dexec.mainClass="ab.run.checkApplication" -Dexec.args="$CONF"