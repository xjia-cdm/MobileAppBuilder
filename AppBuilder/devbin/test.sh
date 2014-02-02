#!/bin/sh

source $(dirname $0)/setenv.sh

#java -classpath $CLASSPATH xj.mobile.test.Test0 $* 
#java -classpath $CLASSPATH xj.mobile.test.EvaluatorTest $* 
java -classpath $CLASSPATH xj.mobile.test.EditorInfoTest $* 

exit 0