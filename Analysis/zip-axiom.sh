#!/bin/bash

# the location of the MADL files
MADL_SRC_DIR="test"

# the location iOS and Android generated source
ANDROID_SRC_DIR="gen/Platform.Android"
IOS_SRC_DIR="gen/Platform.iOS"

# here's where we put the reports
REPORTS_DIR="reports"
REPORTS_FILE="$REPORTS_DIR/axiom-zip-data.dat"

# we're going to write a summary file
echo -e "sample\tmadl-raw\tmadl-gzip\tmadl-ratio\tandroid-raw\tandroid-gzip\tandroid-ratio\tios-raw\tios-gzip\tios-ratio\tmadl-to-android\tmadl-to-ios" > $REPORTS_FILE

# file names can have spaces in them, so we need to account for that
OIFS="$IFS"
IFS=$'\n'

# visit each file in the "test" folder
for madl_src_file in $MADL_SRC_DIR/*.madl.stripped
#for madl_src_file in $MADL_SRC_DIR/app01.madl.stripped
do
  # get the name of the file without the extension or the leading path
  madl_file_name=`basename $madl_src_file .madl.stripped`

  # zip the source file. at the moment there is just one MADL file for each
  # project although that could change in the future.
  echo "ZIPPING file $madl_file_name...." 1>&2;
  MADL_RAW=$(stat -c%s "$madl_src_file")
  MADL_GZIP=$(gzip -c --best "$madl_src_file" | wc -c)
  MADL_RATIO=$(echo "scale=4;$MADL_RAW/$MADL_GZIP" | bc)

  # zip the CLOC-ed android files so that we can determine how big they are
  # in their raw and compressed forms
  echo "ZIPPING android project $madl_file_name...." 1>&2;
  ANDROID_RAW=0
  ANDROID_GZIP=0
  for stripped_file in `find $ANDROID_SRC_DIR/$madl_file_name -name "*.stripped" -type f`
  do
    # add to the raw size
    RAW=$(du -s --block-size=1 $stripped_file | cut -f 1)
    ANDROID_RAW=$(echo "$ANDROID_RAW + $RAW" | bc)

    # add to the zipped size
    GZIP=$(gzip -c --best $stripped_file | wc -c)
    ANDROID_GZIP=$(echo "$ANDROID_GZIP + $GZIP" | bc)
  done
  ANDROID_RATIO=$(echo "scale=4;$ANDROID_RAW/$ANDROID_GZIP" | bc)
  MADL_TO_ANDROID=$(echo "scale=4;$ANDROID_RATIO/$MADL_RATIO" | bc)

  # zip the CLOC-ed android files so that we can determine how big they are
  # in their raw and compressed forms
  echo "ZIPPING ios project $madl_file_name...." 1>&2;
  IOS_RAW=0
  IOS_GZIP=0
  for stripped_file in `find $IOS_SRC_DIR/$madl_file_name -name "*.stripped" -type f`
  do
    # add to the raw size
    RAW=$(du -s --block-size=1 "$stripped_file" | cut -f 1)
    IOS_RAW=$(echo "$IOS_RAW + $RAW" | bc)

    # add to the zipped size
    GZIP=$(gzip -c --best "$stripped_file" | wc -c)
    IOS_GZIP=$(echo "$IOS_GZIP + $GZIP" | bc)
  done
  IOS_RATIO=$(echo "scale=4;$IOS_RAW/$IOS_GZIP" | bc)
  MADL_TO_IOS=$(echo "scale=4;$IOS_RATIO/$MADL_RATIO" | bc)

  # add the content to the summary
  echo -e "$madl_file_name\t$MADL_RAW\t$MADL_GZIP\t$MADL_RATIO\t$ANDROID_RAW\t$ANDROID_GZIP\t$ANDROID_RATIO\t$IOS_RAW\t$IOS_GZIP\t$IOS_RATIO\t$MADL_TO_ANDROID\t$MADL_TO_IOS" >> $REPORTS_FILE
done

# reset the IFS
IFS="$OIFS"
