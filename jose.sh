#!/bin/bash

cd `dirname "$0"`

localJava="jre/bin/java"

#
#	Hi-DPI scaling
#
#	depends on the installed JRE
#
#	(1) Jetbrains JBR 21 can do scaling out of the box. Great !!
#		unfortunately, this JRE has a problem with touch events.
#		as soon as the touch screen bug is fixed, we shall use it.
#
#	(2) Oracle Java SE 21, Azul Zulu 21
#		need some extra configuration to find the appropritate scaling factor.
#		We try our best (following instructions at 
#		https://intellij-support.jetbrains.com/hc/en-us/articles/360007994999-HiDPI-configuration).
#
#		if it doesn't work, feel free toset the scaling factor manually, below.
#
# 	and yes, uiscale accepts only integers.
#   no fractional scaling, yet...
#

dpi=`xrdb -q | grep -i dpi | awk '{print $2}'`
echo "dpi="$dpi

uiscale=1
if [[ dpi -ge 96 ]]; then
	uiscale=$((dpi/96))
fi

echo "uiscale="$uiscale

# hi-dpi scaling:
scale_args="-Dsun.java2d.uiScale.enabled=true -Dsun.java2d.uiScale=$uiscale"

# path to native libs:
lp="-Djava.library.path=lib/Linux_amd64"

# exports needed for Java3D integration:
vmargs="--add-exports=java.desktop/sun.awt=ALL-UNNAMED $lp $scale_args" 

$localJava $vmargs -jar jose.jar $*
