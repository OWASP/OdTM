#!/bin/bash

DATA=../data

CAPEC_URL=https://cwe.mitre.org/data/xml/cwec_latest.xml.zip
CAPEC_ARCH=${DATA}/cwec_latest.xml.zip

wget -c -O ${CAPEC_ARCH} ${CAPEC_URL}

unzip $CAPEC_ARCH -d $DATA
