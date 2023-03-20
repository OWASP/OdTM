#!/bin/bash

POST="${1}.rez"
FLD="rez"
LINES=`ls ../clear/*.ttl`
LG="${FLD}/0.log"

rm -f ${FLD}/*.${POST}

for LINE in $LINES
do 
  echo "::: file: $LINE"
  NAME=`basename $LINE`
  SAVETO="${FLD}/${NAME}.${POST}"

  ./arqcsv $1 $LINE > $SAVETO

  COUNT=`wc -l < $SAVETO`
  if [ $COUNT -eq 1 ];
  then
    rm -f $SAVETO
  fi

done


FOUND=`ls ${FLD}/*.${POST} | wc -l`
DT=`date "+%D %T"`
echo "$DT $POST $FOUND " >> $LG