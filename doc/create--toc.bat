cd man
set LIB=C:\jh1.1.3\javahelp\lib
set JAVA=C:\jdk1.4\jre\bin\java

rem create TOC
%JAVA% -cp D:/jose/work/classes;%LIB%/jhall.jar de.jose.help.TOCGenerator . -notoc img -notoc JavaHelpSearch -notoc macros.html

rem create fulltext index
%JAVA% -cp %LIB%/jhall.jar;%LIB%/jhsearch.jar com.sun.java.help.search.Indexer index.html 01-install 03-panel 04-menu 05-dialog 10-reference

cd ..
