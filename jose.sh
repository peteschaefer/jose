#!/bin/sh

cd `dirname "$0"`

#
# if jre-linux/bin/java is present, use it
# otherwise use java from path
#

localJava=jre-linux/bin/java

vmargs = -Xmx1200M -Djava.library.path=lib/Linux_i386

$localJava $vmargs -jar jose.jar $*
