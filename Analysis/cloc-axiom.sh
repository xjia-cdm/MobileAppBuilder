#!/bin/bash

# define the project run
CLOC_SQL_PROJECT="201305221415";

# define some flags
CLOC_LANG_DEF="--force-lang-def=cloc-axiom.def"
#CLOC_LANG_DEF="--read-lang-def=cloc-axiom.def"
CLOC_EXCLUDES="--exclude-list-file=axiom-exclude-file"
CLOC_CSV_OPT=""
#CLOC_CSV_OPT="--csv"
CLOC_OTHER_OPTS="--quiet --by-file-by-lang --original-dir --strip-comments=stripped $CLOC_CSV_OPT"
CLOC_REPORT_OPTS="--quiet --sum-reports $CLOC_CSV_OPT"

# the location of the MADL files
MADL_SRC_DIR="test"

# the location iOS and Android generated source
ANDROID_SRC_DIR="gen/Platform.Android"
IOS_SRC_DIR="gen/Platform.iOS"

# the name of the reports folder
REPORTS_DIR="reports"

# prepare the reports folder
if [ ! -d $REPORTS_DIR ]
then
  echo "Reports folder [$REPORTS_DIR] does not exist...creating"
  mkdir $REPORTS_DIR
fi

# the list of files to be aggregated in the final step
android_summary_files=""
ios_summary_files=""
madl_summary_files=""

# visit each file in the "test" folder
for madl_src_file in $MADL_SRC_DIR/*.madl
#for madl_src_file in $MADL_SRC_DIR/app01.madl
do
  # demonstrate progress
  echo "Cloc-ing file $madl_src_file...." 1>&2;

  # get the name of the file without the extension or the leading path
  madl_file_name=`basename $madl_src_file .madl`

  # cloc the source file. at the moment there is just one MADL file for each
  # project although that could change in the future.
  ./cloc-1.58.pl $CLOC_OTHER_OPTS $CLOC_LANG_DEF $CLOC_EXCLUDES --report-file=$REPORTS_DIR/$madl_file_name/madl $madl_src_file

  # now take the MADL file name and use that to perform the analysis based of
  # the Android and iOS content. that data will go into a report in the "reports"
  # folder.
  ./cloc-1.58.pl $CLOC_OTHER_OPTS $CLOC_LANG_DEF $CLOC_EXCLUDES --report-file=$REPORTS_DIR/$madl_file_name/ios --ignored=$REPORTS_DIR/$madl_file_name/ignored $IOS_SRC_DIR/$madl_file_name/
  ./cloc-1.58.pl $CLOC_OTHER_OPTS $CLOC_LANG_DEF $CLOC_EXCLUDES --report-file=$REPORTS_DIR/$madl_file_name/android --ignored=$REPORTS_DIR/$madl_file_name/ignored $ANDROID_SRC_DIR/$madl_file_name/

  # once the reports have been created, aggregate them by language and file.
  ./cloc-1.58.pl $CLOC_REPORT_OPTS $CLOC_LANG_DEF --report-file=$REPORTS_DIR/$madl_file_name/summary $REPORTS_DIR/$madl_file_name/ios $REPORTS_DIR/$madl_file_name/android $REPORTS_DIR/$madl_file_name/madl

  # add the ios and android folders to the list of those required during final
  # summarization below
  #summary_files="$summary_files $REPORTS_DIR/$madl_file_name/summary.lang $REPORTS_DIR/$madl_file_name/summary.file "
  android_summary_files="$android_summary_files $REPORTS_DIR/$madl_file_name/android "
  ios_summary_files="$ios_summary_files $REPORTS_DIR/$madl_file_name/ios "
  madl_summary_files="$madl_summary_files $REPORTS_DIR/$madl_file_name/madl "
done

# once the analysis of everything is done, aggregate the reports to provide
# overall totals.
#./cloc-1.58.pl $CLOC_REPORT_OPTS $CLOC_LANG_DEF --report-file=$REPORTS_DIR/summary-all $summary_files
./cloc-1.58.pl $CLOC_REPORT_OPTS $CLOC_LANG_DEF --report-file=$REPORTS_DIR/summary-android $android_summary_files
./cloc-1.58.pl $CLOC_REPORT_OPTS $CLOC_LANG_DEF --report-file=$REPORTS_DIR/summary-ios $ios_summary_files
./cloc-1.58.pl $CLOC_REPORT_OPTS $CLOC_LANG_DEF --report-file=$REPORTS_DIR/summary-madl $madl_summary_files
