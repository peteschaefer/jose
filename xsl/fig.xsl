<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:fo="http://www.w3.org/1999/XSL/Format" 
	exclude-result-prefixes="fo">
	
	<xsl:output method="html" version="1.0" encoding="ISO-8859-1" indent="yes"/>

	<!-- use TT figurines, or images ? -->
	<xsl:param name="imgfig" select="//option[key='xsl.html.figs']/value='img'"/>
	<!-- directory that contains figurine images -->
	<xsl:param name="imgurl" select="//option[key='xsl.html.img.dir']/value"/>

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
				
		<xsl:param name="d" select="number(../../depth)"/>		
		<xsl:param name="fig" select="//figurines/fig[position()=(1+$d) or position()=5]"/>		
		<xsl:choose>
			<xsl:when test="$imgfig">
				<!-- image figurine -->
				<img>
				<xsl:attribute name="alt"><xsl:value-of select="$f"/></xsl:attribute>
				<xsl:attribute name="src">
                    <xsl:if test="string-length($imgurl)&gt;0">
                        <xsl:value-of select="$imgurl"/><xsl:text>/</xsl:text>
                    </xsl:if>
                    <xsl:value-of select="$fig/font"/>/<xsl:value-of select="$fig/px-size"/>/<xsl:value-of select="$f"/>.png</xsl:attribute>
				<xsl:attribute name="width"><xsl:value-of select="$fig/string[key=$f]/width"/></xsl:attribute>
				<xsl:attribute name="height"><xsl:value-of select="$fig/string[key=$f]/height"/></xsl:attribute>
				</img>
			</xsl:when>
			
			<xsl:otherwise>
				<!-- text figurine -->						
				<span>
				<xsl:attribute name="class">body_figurine_<xsl:value-of select="$d"/></xsl:attribute>
				<xsl:value-of select="$fig/string[key=$f]/value"/>
				</span>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
			
</xsl:stylesheet>
