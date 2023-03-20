#!/bin/bash


LINES=`ls ../clear/*.ttl`

for LINE in $LINES
do 
  echo "::: file: $LINE"
  ./arqcsv $1 $LINE
done



