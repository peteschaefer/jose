<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:fo="http://www.w3.org/1999/XSL/Format" 
	exclude-result-prefixes="fo">
	
	<xsl:import href="css.xsl"/>
	<xsl:import href="html_header.xsl"/>
	<xsl:import href="fig.xsl"/>
	<xsl:import href="html_body.xsl"/>
	<xsl:output method="html" version="4.0" encoding="UTF-8" indent="yes" />

	<!-- the following info is evalulated by the jose application (not the XSLT processor) -->
	<jose:export xmlns:jose="http://jose-chess.sourceforge.net/xsl-info">
		<jose:title>xsl.html</jose:title>
		
		<!-- accepted input -->
		<jose:input>list-of-games</jose:input>
		<!-- creates output: -->
		<jose:output>html</jose:output>

		<!-- name of linked CSS -->
		<jose:param>
			<jose:key>css-file</jose:key>
			<jose:value>games.css</jose:value>
		</jose:param>
		<!-- xsl sheet for generating CSS -->
		<jose:param>
			<jose:key>css-xsl</jose:key>
			<jose:value>css.xsl</jose:value>
		</jose:param>
	</jose:export>
	
	<!-- - - - - - - - - - - - - - -->
	<!-- Main Template     -->
	<!-- - - - - - - - - - - - - - -->
	<xsl:template match="jose-export">

	<html>
		<head>
			<title>jose Games</title>
			<xsl:call-template name="include_css"/>
			<!--hack for IE: display transparent PNGs correctly-->
			<style>
				img { behavior: url(pngHack.htc);}
			</style>
		</head>
		
		<body>

			<xsl:for-each select="game">
				<xsl:apply-templates select="head"/>

				<!-- initial FEN ? -->
				<xsl:apply-templates select="diagram"/>

				<xsl:apply-templates select="body"/>

				<br/><hr/><br/>
			</xsl:for-each>

			<center class="body_comment_4">created with <a href="http://hrimfaxi.bitbucket.io/jose">jose Chess</a></center>

		</body>
	</html>
		
	</xsl:template>
	

	<!-- Move -->
	<xsl:template match="m">
		<xsl:text> </xsl:text>
		<xsl:apply-templates/>
		<xsl:text> </xsl:text>
	</xsl:template>
	
	<xsl:template match="fen"><xsl:text></xsl:text></xsl:template>
</xsl:stylesheet>
