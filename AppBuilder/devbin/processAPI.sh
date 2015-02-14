#!/bin/sh

source $(dirname $0)/setenv.sh

#rm -Rf $(dirname $0)/../output/api
#rm -Rf $(dirname $0)/../lib/api

java -classpath $CLASSPATH1 xj.mobile.tool.IOSDocReader -lib
java -classpath $CLASSPATH1 xj.mobile.tool.IOSDocReader -fetch
java -classpath $CLASSPATH1 xj.mobile.tool.IOSDocReader -analyze

java -classpath $CLASSPATH1 xj.mobile.tool.AndroidDocReader -lib
java -classpath $CLASSPATH1 xj.mobile.tool.AndroidDocReader -fetch
java -classpath $CLASSPATH1 xj.mobile.tool.AndroidDocReader -analyze

exit 0