#!/bin/bash

DATA=../data

ATTCK_URL=https://raw.githubusercontent.com/mitre-attack/attack-stix-data/master/enterprise-attack/enterprise-attack.json
ATTCK_FILE=${DATA}/enterprise-attack.json

wget -c -O ${ATTCK_FILE} ${ATTCK_URL}
