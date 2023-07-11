#!/bin/bash

POST="${1}.rez"
FLD="rez"
LINES=`ls ../clear/*.ttl`
LG="${FLD}/00.log"

rm -f ${FLD}/*.${POST}

DT=`date "+%D %T"`
echo "$DT" > $LG

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
  else
    BM=`grep OdTMBaseThreatModel $SAVETO | wc -l`
    DM=`grep ACCTP $SAVETO | wc -l`
#    RM=`echo "scale=4; $DM / $BM" | bc`
    RM=`python3 -c "print( round ($DM / float($BM),4) )"`
    echo "$NAME $BM $DM $RM" >> $LG
  fi

done


#FOUND=`ls ${FLD}/*.${POST} | wc -l`
#DT=`date "+%D %T"`
#echo "$DT $POST $FOUND " >> $LG