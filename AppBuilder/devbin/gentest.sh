#!/bin/sh

source $(dirname $0)/setenv.sh

java -classpath $CLASSPATH xj.mobile.tool.TestGenerator $* 

exit 0
