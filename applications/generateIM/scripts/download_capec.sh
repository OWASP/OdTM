#!/bin/bash

DATA=../data

CAPEC_URL=https://capec.mitre.org/data/xml/capec_latest.xml
CAPEC_ARCH=${DATA}/capec_latest.xml

wget -c -O ${CAPEC_ARCH} ${CAPEC_URL}
