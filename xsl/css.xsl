<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="text" version="1.0" encoding="ISO-8859-1" indent="yes"/>

<xsl:template match="styles">
	<xsl:apply-templates select="//style"/>
</xsl:template>

<xsl:template match="style">

<xsl:param name="family" select="a[key='family']/value"/>
<xsl:param name="size" select="a[key='size']/value"/>
<xsl:param name="bold" select="a[key='bold']/value"/>
<xsl:param name="italic" select="a[key='italic']/value"/>
<xsl:param name="color" select="a[key='foreground']/value"/>
<xsl:param name="indent" select="a[key='FirstLineIndent']/value"/>

<xsl:if test="boolean($family) or boolean($size) or $bold='true' or $italic='true' or boolean($color)">
	.<xsl:value-of select="translate(name,'.','_')"/>
<!--	
	<xsl:for-each select="descendant::style">, SPAN.<xsl:value-of select="translate(name,'.','_')"/></xsl:for-each>
-->	
	<xsl:text> {
	</xsl:text>
	
	<xsl:if test="boolean($family)">	font-family: <xsl:choose>
		<xsl:when test="contains($family,' ')">"<xsl:value-of select="$family"/>"</xsl:when>
		<xsl:otherwise><xsl:value-of select="$family"/></xsl:otherwise>
	</xsl:choose>;
	</xsl:if>
	
	<xsl:if test="boolean($size)">	font-size: <xsl:value-of select="$size"/>pt;
	</xsl:if> 
	
	<xsl:text>	font-weight: </xsl:text>
	<xsl:choose>
		<xsl:when test="$bold='true'">bold</xsl:when>
		<xsl:otherwise>normal</xsl:otherwise>
	</xsl:choose>
	<xsl:text>;
	</xsl:text>

	<xsl:text>	font-style: </xsl:text>
	<xsl:choose>
		<xsl:when test="$italic='true'">italic</xsl:when>
		<xsl:otherwise>normal</xsl:otherwise>
	</xsl:choose>
	<xsl:text>;
	</xsl:text>
	
	<xsl:text>	color: </xsl:text>
	<xsl:choose>
		<xsl:when test="boolean($color)">
			<xsl:value-of select="$color"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:text>black</xsl:text>
		</xsl:otherwise>
	</xsl:choose>
	<xsl:text>;
	</xsl:text>
	
	<xsl:if test="boolean($indent)">	text-indent: <xsl:value-of select="round($indent)"/>pt;
	</xsl:if>
			
	<xsl:text>}
	
	</xsl:text>		
</xsl:if>

</xsl:template>
	
<xsl:template name="include_css">
			<!-- directory that contains figurine images -->
			<xsl:param name="cssurl" select="//option[key='xsl.html.img.dir']/value"/>
			<!-- location of CSS -->
			<xsl:param name="css_standalone" select="//option[key='xsl.css.standalone']/value='true'"/>

			<xsl:choose>
				<xsl:when test="$css_standalone">
						<!-- link to CSS -->
						<link rel="StyleSheet" type="text/css">
						<xsl:attribute name="href"><xsl:value-of select="$cssurl"/>games.css</xsl:attribute>
						</link>
				</xsl:when>
				<xsl:otherwise>
						<!-- create CSS -->
						<style>
							<xsl:apply-templates select="styles"/>

							p {
								margin-top: 6pt;
								margin-bottom: 6pt;
							}
							
						</style>
				</xsl:otherwise>
			</xsl:choose>
</xsl:template>

</xsl:stylesheet>
