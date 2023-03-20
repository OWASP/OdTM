#!/bin/bash

LINES=`ls ../clear/*.ttl`

for LINE in $LINES
do
  echo $LINE
  ./arqtext $1 $LINE
done



