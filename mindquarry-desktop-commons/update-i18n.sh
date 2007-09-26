#!/bin/sh

echo ""
echo "This script will update the i18n files. It requires the project 'mindquarry-i18n' to be checked out two directories above"
echo ""

EXTRACTOR_DIR=mindquarry-i18n/mindquarry-i18n-ts-extractor
UPDATER_DIR=mindquarry-i18n/mindquarry-i18n-ts-updater
DIR=mindquarry-desktop-tools/mindquarry-desktop-commons/
OLD=../../mindquarry-desktop-tools/mindquarry-desktop-commons/src/main/resources/com/mindquarry/desktop/messages_de.xml
TMP=../../mindquarry-desktop-tools/mindquarry-desktop-commons/src/main/resources/com/mindquarry/desktop/messages_de_tmp.xml
NEW=../../mindquarry-desktop-tools/mindquarry-desktop-commons/src/main/resources/com/mindquarry/desktop/messages_de_new.xml

#
# Extract
#

cd ../..
cd $EXTRACTOR_DIR
mvn assembly:assembly
cd ../..
cd $DIR
# extractor gets confused with relative paths containing '..', so change 
# to the directory it is supposed to work in:
java -jar ../../$EXTRACTOR_DIR/target/mindquarry-i18n-ts-extractor-1.0-SNAPSHOT-jar-with-dependencies.jar src/main > $NEW

#
# Update
#

cd ../..
cd $UPDATER_DIR
mvn assembly:assembly
java -jar target/mindquarry-i18n-ts-updater-1.0-SNAPSHOT-jar-with-dependencies.jar $OLD $NEW $TMP
mv $TMP $OLD
rm $NEW

echo ""
echo "DONE -- now translate $OLD using Qt 'linguist'"
echo ""
