#!/bin/bash

cd `dirname "$0"`

#
# if jre-linux/bin/java is present, use it
# otherwise use java from path
#

localJava="jre/bin/java"

vmargs="--add-exports=java.desktop/sun.awt=ALL-UNNAMED -Djava.library.path=lib/Linux_x64"

$localJava $vmargs -jar jose.jar $*
