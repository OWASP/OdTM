#!/bin/bash

# Use a properties file as an argument

CONF="../../guide/models/${1}"

cd ../../applications/OdTMServer

mvn -e exec:java -q -Dexec.mainClass="ab.run.consoleApplication" -Dexec.args="$CONF"

