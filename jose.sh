#!/bin/sh

cd `dirname "$0"`

#
# if jre-linux/bin/java is present, use it
# otherwise use java from path
#

localJava=jre-linux/bin/java

switches=-Xmx2G -Djava.library.path=lib/Linux_i386

if test -x $localJava;
then $localJava $switches -jar jose.jar $*;
else java $switches -jar jose.jar $*;
fi
