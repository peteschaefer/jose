#!/bin/sh

cd `dirname "$0"`

#
# if jre-linux/bin/java is present, use it
# otherwise use java from path
#

localJava=jre-linux/bin/java

if test -x $localJava;
then $localJava -jar jose.jar $*;
else java -jar jose.jar $*;
fi
