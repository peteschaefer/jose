<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	<xsl:template name="DownloadText">
		<xsl:param name="key"/>
		<xsl:param name="text"/>
		<xsl:param name="size" select="//file[@key=$key]/size"/>
		<xsl:param name="size-mb" select="$size div (1024*1024)"/>
		<xsl:param name="size-mb-rounded" select="floor($size-mb*10 + 0.5) div 10"/>
		<xsl:param name="size-kb" select="floor($size div 1024 + 0.5)"/>
		<a>
			<xsl:attribute name="href"><xsl:value-of select="//files/host"/><xsl:value-of select="//file[@key=$key]/path"/><xsl:value-of select="//files/suffix"/></xsl:attribute>
			<xsl:value-of select="$text"/>
		</a>
		<xsl:choose>
			<xsl:when test="$size-mb &gt;= 1.0">
			(<xsl:value-of select="$size-mb-rounded"/> MB)		
		</xsl:when>
			<xsl:otherwise>
			(<xsl:value-of select="$size-kb"/> kB)		
		</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="Download">
		<xsl:param name="key"/>
		<xsl:call-template name="DownloadText">
			<xsl:with-param name="key" select="$key"/>
			<xsl:with-param name="text">Download</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<xsl:template match="/">
		<html>
			<head>
				<title>
        jose Downloads
    </title>
				<META http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
				<meta http-equiv="content-type" content="text/html;charset=iso-8859-1"/>
				<meta name="description" content="jose Chess Database"/>
				<meta name="keywords" content="chess, database, XBoard, Java, MySQL, Open Source"/>
				<link rel="StyleSheet" type="text/css" href="styles.css"/>
			</head>
			<body>
				<table>
					<tr>
						<td colspan="3" align="right">
							<font size="-1">
								<a href="#download-windows">Windows</a>
								<xsl:text> - </xsl:text>
								<a href="#download-linux">Linux</a>
								<xsl:text> - </xsl:text>
								<a href="#download-macosx">Mac OS X</a>
								<xsl:text> - </xsl:text>
								<a href="#download-more">All Platforms</a>
								<xsl:text> - </xsl:text>
								<a href="#download-engines">Engines</a>
							</font>
							<br/>
							<br/>
						</td>
					</tr>
					<tr>
						<td class="download" colspan="3">
							<a name="download-windows">
							<h3 class="download">Windows Downloads</h3>
							</a>
						</td>
					</tr>
					<tr>
						<td class="download" colspan="3">
jose requires <b>Windows 98</b> or later and <b>Sun Java Runtime Environment 1.4 or 1.5</b>
							<br/>
If you don't have a Sun JRE, download a package that includes it, or download it from java.sun.com
</td>
					</tr>
					<tr>
						<td colspan="3">
							<hr/>
						</td>
					</tr>

						<tr>
							<td class="download">
								<b class="download">jose <xsl:value-of select="//current-version"/> for Windows</b>
								<br/>
requires Windows 98 or later and Sun JRE 1.4 or 1.5
</td>
							<td class="download">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">jose-windows</xsl:with-param>
								</xsl:call-template>
							</td>
							<td class="download">
								<a href="install.html#win-install">View Instructions</a>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
						<tr>
							<td class="download">
								<b class="download">jose <xsl:value-of select="//current-version"/> for Windows, includes JRE 1.5.0</b>
								<br/>
requires Windows 98 or later </td>
							<td class="download">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">jose-windows-jre</xsl:with-param>
								</xsl:call-template>
							</td>
							<td class="download">
								<a href="install.html#win-install">View Instructions</a>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
						<tr>
							<td class="download">
								<b class="download">Sun Java Runtime Environment 1.5.0</b>
							</td>
							<td class="download2" colspan="2">
								<a href="http://java.sun.com/j2se/1.5.0/download.html">Download</a> from java.sun.com</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
<!--
						<tr>
							<td class="download">
								<b class="download">jose 3D package for Windows</b>
								</td>
							<td class="download">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">j3d-windows</xsl:with-param>
								</xsl:call-template>
							</td>
							<td class="download">
								<a href="install.html#win-3d">View Instructions</a>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
-->						
						<tr>
							<td class="download">
								<b class="download">jose <xsl:value-of select="//current-version"/> Upgrade</b>
								<br/>
upgrade from a previously installed version
</td>
							<td class="download">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">jose-upgrade-windows</xsl:with-param>
								</xsl:call-template>
							</td>
							<td class="download">
								<a href="install.html#upgrade">View Instructions</a>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
								<br/>
								<br/>
							</td>
						</tr>
					
					
						<tr>
							<td class="download" colspan="3">
								<a name="download-linux">
								<h3 class="download">Linux Downloads</h3>
								</a>
							</td>
						</tr>
						<tr>
							<td class="download" colspan="3">
jose requires <b>Linux</b> with an <b>Intel 32 bit</b> architecture and <b>Sun Java Runtime Environment 1.4 or 1.5</b>
								<br/>
If you don't have a Sun JRE, download a package that includes it, or download it from java.sun.com<br/>
Alternatively, you can use <b>Blackdown JRE</b>.
</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
						<tr>
							<td class="download">
								<b class="download">jose <xsl:value-of select="//current-version"/> for Linux</b>
								<br/>
requires Linux IA and Sun JRE 1.4 or 1.5
</td>
							<td class="download">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">jose-linux</xsl:with-param>
								</xsl:call-template>
							</td>
							<td class="download">
								<a href="install.html#linux-install">View Instructions</a>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
						<tr>
							<td class="download">
								<b class="download">jose <xsl:value-of select="//current-version"/> for Linux, includes JRE 1.5.0</b>
								<br/>
requires Linux with Intel 32 bit architectur </td>
							<td class="download">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">jose-linux-jre</xsl:with-param>
								</xsl:call-template>
							</td>
							<td class="download">
								<a href="install.html#linux-install">View Instructions</a>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
						<tr>
							<td class="download">
								<b class="download">Sun Java Runtime Environment 1.5.0</b>
							</td>
							<td class="download2" colspan="2">
								<a href="http://java.sun.com/j2se/1.5.0/download.html">Download</a> from java.sun.com</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
						<tr>
							<td class="download">
								<b class="download">Blackdown Java Runtime Environment 1.4.2</b>
							</td>
							<td class="download2" colspan="2">
								<a href="http://www.blackdown.org/java-linux/java2-status/jdk1.4-status.html">Download</a> from www.blackdown.org</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
<!--
						<tr>
							<td class="download">
								<b class="download">jose 3D package for Linux</b>
								<br/>
requires an OpenGL enabled graphics card</td>
							<td class="download">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">j3d-linux</xsl:with-param>
								</xsl:call-template>
							</td>
							<td class="download">
								<a href="install.html#linux-3d">View Instructions</a>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
-->						
						<tr>
							<td class="download">
								<b class="download">jose <xsl:value-of select="//current-version"/> Upgrade</b>
								<br/>
upgrade from a previously installed version
</td>
							<td class="download">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">jose-upgrade-linux</xsl:with-param>
								</xsl:call-template>
							</td>
							<td class="download">
								<a href="install.html#upgrade">View Instructions</a>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
								<br/>
								<br/>
							</td>
						</tr>


						<tr>
							<td class="download" colspan="3">
								<a name="download-macosx">
								<h3 class="download">Mac OS X Downloads</h3>
								</a>
							</td>
						</tr>
						<tr>
							<td class="download" colspan="3">
jose requires <b>Mac OS X 10.3</b> or later, with PowerPC architecture, and <b>Java Runtime Environment 1.4</b>
								<br/>
To enable jose's 3D features, you need to download <b>Java3D</b> from www.apple.com.
</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
						<tr>
							<td class="download">
								<b class="download">jose <xsl:value-of select="//current-version"/> for Mac OS X</b>
								<br/>
requires Mac OS X 10.3 or later and JRE 1.4
</td>
							<td class="download">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">jose-macosx</xsl:with-param>
								</xsl:call-template>
							</td>
							<td class="download">
								<a href="install.html#macosx-install">View Instructions</a>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
						<tr>
							<td class="download">
								<b class="download">Java Runtime Environment 1.4.2</b>
							</td>
							<td class="download2" colspan="2">
								<a href="http://www.apple.com/downloads/macosx/apple/javaupdate142.html">Download</a> from www.apple.com</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
						<tr>
							<td class="download">
								<b class="download">Java 3D 1.3.1</b>
							</td>
							<td class="download2" colspan="2">
								<a href="http://www.apple.com/downloads/macosx/apple/java3dandjavaadvancedimagingupdate.html">Download</a> from www.apple.com</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
						<tr>
							<td class="download">
								<b class="download">jose <xsl:value-of select="//current-version"/> Upgrade</b>
								<br/>
upgrade from a previously installed version
</td>
							<td class="download">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">jose-upgrade-macosx</xsl:with-param>
								</xsl:call-template>
							</td>
							<td class="download">
								<a href="install.html#upgrade">View Instructions</a>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
								<br/>
								<br/>
							</td>
						</tr>

					
						<tr>
							<td class="download" colspan="3">
								<a name="download-more">
								<h3 class="download">Other Downloads - All Platforms</h3>
								</a>
							</td>
						</tr>
						<tr>
							<td class="download" colspan="3">
								<b class="download">Sample Databases</b>								
							</td>
						</tr>
						<tr>
							<td class="download">
10.000 selected grandmaster games
</td>
							<td class="download" colspan="2">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">db-10k</xsl:with-param>
								</xsl:call-template>
							</td>							
						</tr>
						<tr>
							<td class="download">
500.000 games from more than two centuries
</td>
							<td class="download">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">db-500k</xsl:with-param>
								</xsl:call-template>
							</td>
							<td class="download">
								<a href="install.html#database">View Instructions</a>
							</td>
						</tr>
						<tr>
							<td class="download">
1.5 million games
</td>
							<td class="download" colspan="2">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">db-1700k</xsl:with-param>
								</xsl:call-template>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
						<tr>
							<td class="download" colspan="2">
								<b class="download">Opening Books</b>
							</td>
						</tr>
						<tr>
							<td class="download">
Small Book
<font class="download-comment">compiled by Dann Corbit</font>
</td>
							<td class="download" colspan="2">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">book-small-bin</xsl:with-param>
								</xsl:call-template>
							</td>							
						</tr>
						<tr>
							<td class="download">
Standard Book
<font class="download-comment">compiled by Dann Corbit</font>
</td>
							<td class="download" colspan="2">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">book-bin</xsl:with-param>
								</xsl:call-template>
							</td>
						</tr>
						<tr>
							<td class="download">
Large Book
<font class="download-comment">compiled by Dann Corbit</font>
</td>
							<td class="download">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">dcbook-large-bin</xsl:with-param>
								</xsl:call-template>
							</td>
							<td class="download">
								<a href="install.html#book">View Instructions</a>
							</td>
						</tr>
						<tr>
							<td class="download">
Engine Book 
<font class="download-comment">compiled by Marc Lacrosse, and originally used for the very successful "Fruit" engine</font>
</td>
							<td class="download" colspan="2">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">performance-bin</xsl:with-param>
								</xsl:call-template>
							</td>							
						</tr>
						<tr>
							<td class="download">
Varied Book 
<font class="download-comment">another book by Marc Lacrosse with a wider range of opening lines</font>
</td>
							<td class="download" colspan="2">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">varied-bin</xsl:with-param>
								</xsl:call-template>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>

						<tr>
							<td class="download">
								<b class="download">Bonus Textures</b>
								<br/>
more textures for board and pieces
</td>
							<td class="download">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">bonus-textures</xsl:with-param>
								</xsl:call-template>
							</td>
							<td class="download">
								<a href="install.html#textures">View Instructions</a>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
						<tr>
							<td class="download">
								<b class="download">Sound Files</b>
								<br/>
for move announcements
</td>
							<td class="download">
								<xsl:call-template name="Download">
									<xsl:with-param name="key">jose-sounds</xsl:with-param>
								</xsl:call-template>
							</td>
							<td class="download">
								<a href="install.html#sound">View Instructions</a>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
							</td>
						</tr>
						<tr>
							<td class="download">
								<b class="download">All Available Files</b>
								<br/>
including older version
</td>
							<td class="download2" colspan="2">
								<a href="http://sourceforge.net/project/showfiles.php?group_id=60120">View List</a>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<hr/>
								<br/>
								<br/>
							</td>
						</tr>


					
						<tr>
							<td class="download" colspan="3">
								<a name="download-engines">
								<h3 class="download">Chess Engines</h3>
								</a>
							</td>
						</tr>
					<tr>
						<td class="download" colspan="3">
jose is packaged with the <a href="http://uciengines.de/UCI-Engines/TogaII/togaii.html">Toga</a> and <a href="http://spike.lazypics.de/index_en.html">Spike</a> chess engines. <br/>
However, there is a huge number of freely available -and commercial- engines that you can also use with jose:
</td>
					</tr>
						<tr>
							<td class="download">
								&#160;&#160;&#160; <a href="http://wbec-ridderkerk.nl/">WBEC Ridderkerk</a>
							</td>
							<td colspan="2">
								<font size="-1">Best site for "Winboard" chess engines.</font>
							</td>
						</tr>
						<tr>
							<td class="download">
								&#160;&#160;&#160; <a href="http://uciengines.de/">UCI Engines.de</a>
							</td>
							<td colspan="2"><font size="-1">A very up-to-date size for "UCI" engines.</font>
							</td>
						</tr>
					<tr>
						<td class="download" colspan="3">
The "market" for Linux and Mac OS X engines is not quite as large, but growing steadily.
Here are some interesting links for Linux and OS X users:
</td>
					</tr>

					<tr>
						<td colspan="3" class="download">
							&#160;&#160;&#160; <a href="http://www.shredderchess.com">Shredder</a><br/>
							&#160;&#160;&#160; <a href="http://www.fruitchess.com">Fruit</a><br/>
							&#160;&#160;&#160; <a href="http://www.hiarcs.com/mac_chess_hiarcs.htm">Hiarcs</a><br/>
							&#160;&#160;&#160; <a href="http://www.lokasoft.nl/ruffian.htm">Ruffian</a><br/>
							&#160;&#160;&#160; <a href="http://www.math.uio.no/~romstad/gothmog/gothmog.html">Gothmog</a><br/>
							&#160;&#160;&#160; <a href="http://www.lokasoft.nl/deepsjengintro.htm">Deep Sjeng</a><br/>
							&#160;&#160;&#160; <a href="http://home1.stofanet.dk/moq/">Yace</a>
						</td>
					</tr>

					<tr>
						<td class="download" colspan="3">
<i>Please note that I don't provide support for the above mentioned software. <br/>

</i>
</td>
					</tr>

					<tr>
						<td colspan="3">
							<hr/>
						</td>
					</tr>

					<tr>
						<td colspan="3" align="right">
							<br/>
							<a href="#download-windows">Windows</a>
							<xsl:text> - </xsl:text>
							<a href="#download-linux">Linux</a>
							<xsl:text> - </xsl:text>
							<a href="#download-macosx">Mac OS X</a>
							<xsl:text> - </xsl:text>
							<a href="#download-more">All Platforms</a>
							<xsl:text> - </xsl:text>
							<a href="#download-engines">Engines</a>
						</td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
