<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="text" encoding="UTF-8" indent="no" />

	<!-- Text Body -->
	<xsl:template match="body">
			<xsl:apply-templates/>
	</xsl:template>
	
	<!-- Variation Line -->
	<xsl:template match="v">
		<xsl:param name="style" select="concat('body.line.',depth)"/>
		<xsl:param name="prefix" select="/descendant::style[name=$style]/a[key='variation.prefix']/value"/>
		<xsl:param name="suffix" select="/descendant::style[name=$style]/a[key='variation.suffix']/value"/>
		<xsl:param name="newline" select="/descendant::style[name=$style]/a[key='variation.newline']/value"/>
	
		<xsl:choose>
			<xsl:when test="$newline='true'">
				<xsl:text>
</xsl:text><xsl:value-of select="$prefix"/>
				<xsl:apply-templates/>
				<xsl:value-of select="$suffix"/>
				<xsl:text>
</xsl:text>
			</xsl:when>

			<xsl:otherwise>
				<xsl:value-of select="$prefix"/>
				<xsl:apply-templates/>
				<xsl:value-of select="$suffix"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- hide "depth" -->
	<xsl:template match="depth"></xsl:template>


	<!-- Annotation -->
	<xsl:template match="a">
		<xsl:value-of select="text"/>
		<xsl:text> </xsl:text>
	</xsl:template>
	
	<!-- Comment -->
	<xsl:template match="c">
		<xsl:text> { </xsl:text>
		<xsl:apply-templates/>
		<xsl:text> } </xsl:text>
	</xsl:template>
	
	<xsl:template match="bold">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="italic">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="underline">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="center">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="right">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="font">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="br"><xsl:text>
</xsl:text></xsl:template>

	<!-- Result -->
	<xsl:template match="result">
		<xsl:if test=".!='*'">
			<xsl:text>
</xsl:text>
			<xsl:value-of select="."/>
		</xsl:if>
	</xsl:template>
		
	<!-- Diagram -->
	<xsl:template match="diagram">	
		<!--TODO?-->
		<xsl:text>
		[]
</xsl:text>
	</xsl:template>

</xsl:stylesheet>
