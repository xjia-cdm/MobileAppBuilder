#!/bin/sh

source $(dirname $0)/setenv.sh

java -classpath $CLASSPATH:$HOME/scripts/ xj.mobile.EditorInfo $* 

exit 0