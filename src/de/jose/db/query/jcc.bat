@echo off
set MBACKUP=%CLASSPATH%
set CLASSPATH=D:\Programme\javacc2.0\bin\lib\JavaCC.zip
java COM.sun.labs.jjtree.Main -STATIC=false -MULTI=TRUE -NODE_SCOPE_HOOK=true query.jjt
java COM.sun.labs.javacc.Main query.jj
set CLASSPATH=%MBACKUP%
