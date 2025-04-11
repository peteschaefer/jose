<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:fo="http://www.w3.org/1999/XSL/Format" 
	exclude-result-prefixes="fo">
	
	<xsl:import href="text_header.xsl"/>
	<xsl:import href="text_body.xsl"/>
	<xsl:output method="text" encoding="UTF-8" indent="yes"/>
	
	<!-- the following info is evalulated by the jose application (not the XSLT processor) -->
	<jose:export xmlns:jose="http://jose-chess.sourceforge.net/xsl-info">
		<jose:title>xsl.text</jose:title>
		
		<!-- accepted input -->
		<jose:input>list-of-games</jose:input>
		<!-- creates output: -->
		<jose:output>text</jose:output>
	</jose:export>
	
	<!-- - - - - - - - - - - - - - -->
	<!-- Main Template     -->
	<!-- - - - - - - - - - - - - - -->
	<xsl:template match="jose-export">
			<xsl:for-each select="game">
				<xsl:apply-templates select="head"/>

				<!-- initial FEN ? -->
				<xsl:apply-templates select="diagram"/>

				<xsl:text>
</xsl:text>

				<xsl:apply-templates select="body"/>

				<xsl:text>

</xsl:text>
			</xsl:for-each>
			<xsl:text>
			(created with jose Chess: peteschaefer.github.io/jose)
			</xsl:text>
	</xsl:template>
	

	<!-- Move -->
	<xsl:template match="m">
		<!--<xsl:text> </xsl:text>-->
		<xsl:apply-templates/>
		<xsl:text> </xsl:text>
	</xsl:template>

	<!-- Figurines -->
	<xsl:template match="n">
		<xsl:call-template name="fig">
			<xsl:with-param name="f" select="'n'"/>
		</xsl:call-template>
	</xsl:template>
	<xsl:template match="b">
		<xsl:call-template name="fig">
			<xsl:with-param name="f" select="'b'"/>
		</xsl:call-template>
	</xsl:template>
	<xsl:template match="r">
		<xsl:call-template name="fig">
			<xsl:with-param name="f" select="'r'"/>
		</xsl:call-template>
	</xsl:template>
	<xsl:template match="q">
		<xsl:call-template name="fig">
			<xsl:with-param name="f" select="'q'"/>
		</xsl:call-template>
	</xsl:template>
	<xsl:template match="k">
		<xsl:call-template name="fig">
			<xsl:with-param name="f" select="'k'"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="fig">
		<xsl:param name="f"/>
		<xsl:param name="fig" select="//figurines/text"/>
		<!-- text figurine -->
		<xsl:value-of select="$fig/string[key=$f]/value"/>
	</xsl:template>

	<xsl:template match="fen"><xsl:text></xsl:text></xsl:template>
</xsl:stylesheet>
