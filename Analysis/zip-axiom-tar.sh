#!/bin/bash

# the location of the MADL files
MADL_SRC_DIR="test"

# the location iOS and Android generated source
ANDROID_SRC_DIR="gen/Platform.Android"
IOS_SRC_DIR="gen/Platform.iOS"

# the name of the reports folder
ZIP_DIR="zips"

# prepare the zips folder
if [ ! -d $ZIP_DIR ]
then
  echo "ZIP folder [$ZIP_DIR] does not exist...creating"
  mkdir $ZIP_DIR
  mkdir $ZIP_DIR/ios
  mkdir $ZIP_DIR/android
  mkdir $ZIP_DIR/madl
fi

# we're going to write a summary file
echo -e "sample\tmadl-raw\tmadl-gzip\tmadl-ratio\tandroid-raw\tandroid-gzip\tandroid-ratio\tios-raw\tios-gzip\tios-ratio\tmadl-to-android\tmadl-to-ios" > $ZIP_DIR/summary-zips

# visit each file in the "test" folder
#for madl_src_file in $MADL_SRC_DIR/*.madl.stripped
for madl_src_file in $MADL_SRC_DIR/app01.madl.stripped
do
  # get the name of the file without the extension or the leading path
  madl_file_name=`basename $madl_src_file .madl.stripped`

  # zip the source file. at the moment there is just one MADL file for each
  # project although that could change in the future.
  echo "ZIPPING file $madl_file_name...." 1>&2;
  gzip --best -c $madl_src_file > $ZIP_DIR/madl/$madl_file_name.gz
  MADL_SIZE_RAW=$(stat -c%s "$madl_src_file")
  MADL_SIZE_GZIP=$(stat -c%s "$ZIP_DIR/madl/$madl_file_name.tar.gz")
  MADL_RATIO=$(echo "scale=4;$MADL_SIZE_RAW/$MADL_SIZE_GZIP" | bc)

  # zip the android files. then unzip them into a temp folder so that we can
  # calculate the raw file sizes
  echo "ZIPPING android project $madl_file_name...." 1>&2;
  #tar czf $ZIP_DIR/android/$madl_file_name.tar.gz $ANDROID_SRC_DIR/$madl_file_name --exclude=build.xml --exclude=ant.properties --exclude=*.png --exclude=*.jpg --exclude=*.gif
  tar cf $ZIP_DIR/android/$madl_file_name.tar --files-from /dev/null
  find $ANDROID_SRC_DIR/$madl_file_name -type f -name '*.stripped' -print0 | xargs -0 tar -uf $ZIP_DIR/android/$madl_file_name.tar
  gzip --best $ZIP_DIR/android/$madl_file_name.tar #$ZIP_DIR/android/$madl_file_name.tar.gz
  mkdir /tmp/$madl_file_name
  tar xzf $ZIP_DIR/android/$madl_file_name.tar.gz --directory=/tmp/$madl_file_name
  ANDROID_SIZE_RAW=$(du -s --block-size=1 /tmp/$madl_file_name | cut -f 1)
  ANDROID_SIZE_GZIP=$(stat -c%s "$ZIP_DIR/android/$madl_file_name.tar.gz")
  ANDROID_RATIO=$(echo "scale=4;$ANDROID_SIZE_RAW/$ANDROID_SIZE_GZIP" | bc)
  MADL_TO_ANDROID=$(echo "scale=4;$ANDROID_RATIO/$MADL_RATIO" | bc)
  rm -rf /tmp/$madl_file_name

  # zip the ios files
  echo "ZIPPING ios project $madl_file_name...." 1>&2;
  #tar czf $ZIP_DIR/ios/$madl_file_name.tar.gz $IOS_SRC_DIR/$madl_file_name --exclude=*.png --exclude=*xcodeproj* --exclude=*.jpg --exclude=*.gif
  tar cf $ZIP_DIR/ios/$madl_file_name.tar --files-from /dev/null
  find $IOS_SRC_DIR/$madl_file_name -type f -name '*.stripped' -print0 | xargs -0 tar -uf $ZIP_DIR/ios/$madl_file_name.tar
  gzip --best $ZIP_DIR/ios/$madl_file_name.tar #$ZIP_DIR/ios/$madl_file_name.tar.gz
  mkdir /tmp/$madl_file_name
  tar xzf $ZIP_DIR/ios/$madl_file_name.tar.gz --directory=/tmp/$madl_file_name
  IOS_SIZE_RAW=$(du -s --block-size=1 /tmp/$madl_file_name | cut -f 1)
  IOS_SIZE_GZIP=$(stat -c%s "$ZIP_DIR/ios/$madl_file_name.tar.gz")
  IOS_RATIO=$(echo "scale=4;$IOS_SIZE_RAW/$IOS_SIZE_GZIP" | bc)
  MADL_TO_IOS=$(echo "scale=4;$IOS_RATIO/$MADL_RATIO" | bc)
  rm -rf /tmp/$madl_file_name

  # add the content to the summary
  echo -e "$madl_file_name\t$MADL_SIZE_RAW\t$MADL_SIZE_GZIP\t$MADL_RATIO\t$ANDROID_SIZE_RAW\t$ANDROID_SIZE_GZIP\t$ANDROID_RATIO\t$IOS_SIZE_RAW\t$IOS_SIZE_GZIP\t$IOS_RATIO\t$MADL_TO_ANDROID\t$MADL_TO_IOS" >> $ZIP_DIR/summary-zips
done

