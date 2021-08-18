#! /bin/sh


if [ $# -eq 0 ]
then
	ALL_LANG="en"
else
    ALL_LANG=$@
fi

RELEASES="../releases" 
DEPLOYED="../deployed"
VERSION="11.3"

mkdir $RELEASES

for LANG_FILE in $ALL_LANG
do
	echo "$LANG_FILE"
	RESPATH="${RELEASES}/${LANG_FILE}"
	echo "Building $RESPATH"
	rm -rf $RESPATH
	mkdir $RESPATH
	mkdir $RESPATH/base
	mkdir $RESPATH/compression
	mkdir $RESPATH/TLS

	ant deployedSuite -propertyfile eclipseme-build-compression.properties -Dlang=$LANG_FILE build
	cp ${DEPLOYED}/lampiro.ja* $RESPATH/compression/
	zip -j $RESPATH/compression/lampiro-${LANG_FILE}-${VERSION}-compression.zip $RESPATH/compression/lampiro.jad $RESPATH/compression/lampiro.jar
	
	ant deployedSuite -Dlang=$LANG_FILE build
	cp ${DEPLOYED}/lampiro.ja* $RESPATH/base/
	zip -j $RESPATH/base/lampiro-${LANG_FILE}-${VERSION}.zip $RESPATH/base/lampiro.jad $RESPATH/base/lampiro.jar
	
	ant deployedSuite -propertyfile eclipseme-build-TLS.properties -Dlang=$LANG_FILE build
	cp ${DEPLOYED}/lampiro.ja* $RESPATH/TLS/
	zip -j $RESPATH/TLS/lampiro-${LANG_FILE}-${VERSION}-TLS.zip $RESPATH/TLS/lampiro.jad $RESPATH/TLS/lampiro.jar


done
