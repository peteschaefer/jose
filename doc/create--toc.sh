cd man
java -cp /windows/D/jose/work/classes:/windows/C/h1.1.3/javahelp/lib/jhall.jar de.jose.help.TOCGenerator . -notoc img -notoc JavaHelpSearch -notoc macros.html
java -cp /windows/C/jh1.1.3/javahelp/lib/jhall.jar:/windows/C/h1.1.3/javahelp/lib/jhsearch.jar com.sun.java.help.search.Indexer index.html 01-install 03-panel 04-menu 05-dialog 10-reference
