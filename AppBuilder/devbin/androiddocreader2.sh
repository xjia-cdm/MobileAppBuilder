#!/bin/sh

source $(dirname $0)/setenv.sh

java -classpath $CLASSPATH1 xj.mobile.tool.AndroidDocReader2 -T $* 

exit 0