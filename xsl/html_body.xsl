<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" version="4.0" encoding="UTF-8" indent="yes" />

	<!-- use TT figurines, or images ? -->
	<xsl:param name="imgfig" select="//option[key='xsl.html.figs']/value='img'"/>
	<!-- directory that contains figurine images -->
	<xsl:param name="imgurl" select="//option[key='xsl.html.img.dir']/value"/>

	<xsl:param name="inlineurl">
        <xsl:if test="string-length($imgurl)&gt;0">
            <xsl:value-of select="$imgurl"/><xsl:text>/</xsl:text>
        </xsl:if>
        <xsl:value-of select="//figurines/inline/font"/><xsl:text>/</xsl:text>
        <xsl:value-of select="//figurines/inline/px-size"/>
    </xsl:param>

	<!-- HTML Body -->
	<xsl:template match="body">
		<font class="body">
			<font class="body_line">
				<xsl:apply-templates/>
			</font>
		</font>
	</xsl:template>
	
	<!-- Variation Line -->
	<xsl:template match="v">
		<xsl:param name="style" select="concat('body.line.',depth)"/>
	
		<xsl:param name="prefix" select="/descendant::style[name=$style]/a[key='variation.prefix']/value"/>
		<xsl:param name="suffix" select="/descendant::style[name=$style]/a[key='variation.suffix']/value"/>
		<xsl:param name="newline" select="/descendant::style[name=$style]/a[key='variation.newline']/value"/>
	
		<font>
		<xsl:attribute name="class"><xsl:value-of select="translate($style,'.','_')"/></xsl:attribute>
			<xsl:choose>
				<xsl:when test="$newline='true'">
					<p><xsl:value-of select="$prefix"/> 
					<xsl:apply-templates/>
					<xsl:value-of select="$suffix"/></p>
				</xsl:when>
				
				<xsl:otherwise>
					<xsl:value-of select="$prefix"/> 
					<xsl:apply-templates/>
					<xsl:value-of select="$suffix"/>
				</xsl:otherwise>
			</xsl:choose>	
		</font>
	</xsl:template>
	<!-- hide "depth" -->
	<xsl:template match="depth"></xsl:template>


	<!-- Annotation -->
	<xsl:template match="a">
		<xsl:value-of select="text"/>
	</xsl:template>
	
	<!-- Comment -->
	<xsl:template match="c">
		<xsl:param name="style" select="concat('body.comment.',../depth)"/>

		<font>
		<xsl:attribute name="class"><xsl:value-of select="translate($style,'.','_')"/></xsl:attribute>
		<xsl:text> </xsl:text>		
		<xsl:apply-templates/>
		<xsl:text> </xsl:text>
		</font>
	</xsl:template>
	
	<xsl:template match="bold">
		<b><xsl:apply-templates/></b>
	</xsl:template>
	<xsl:template match="italic">
		<i><xsl:apply-templates/></i>
	</xsl:template>
	<xsl:template match="underline">
		<u><xsl:apply-templates/></u>
	</xsl:template>
	<xsl:template match="center">
		<center><xsl:apply-templates/></center>
	</xsl:template>
	<xsl:template match="right">
		<div align="right"><xsl:apply-templates/></div>
	</xsl:template>
	<xsl:template match="font">
		<font>
		<xsl:attribute name="style">
			<xsl:if test="@size">
				<xsl:variable name="percent" select="number(substring(@size,1,string-length(@size)-1))"/>
				<xsl:text>font-size: </xsl:text><xsl:value-of select="12.0 * (1.0 + $percent div 100)"/><xsl:text>pt;</xsl:text>
			</xsl:if>
			<xsl:if test="@color">color: <xsl:value-of select="@color"/>;</xsl:if>
			<xsl:if test="@face">font-family: '<xsl:value-of select="@face"/>';</xsl:if>
		</xsl:attribute>
		<xsl:apply-templates/>
		</font>
	</xsl:template>
	<xsl:template match="br"><br/></xsl:template>

	<!-- Result -->
	<xsl:template match="result">
		<xsl:if test=".!='*'">
			<br/>
			<font class="body.result">
			<xsl:value-of select="."/>
			</font>
		</xsl:if>
	</xsl:template>
		
	<!-- Diagram -->
	<xsl:template match="diagram">	
		<a>
		<xsl:attribute name="href">javascript:go(<xsl:value-of select="count(preceding::m)-1"/>)</xsl:attribute>
		<table cellspacing="0" cellpadding="0" border="0" class="body_inline">
			<xsl:apply-templates select="table"/>
		</table>
		</a>
	</xsl:template>
	
	<xsl:template match="tr">
		<tr>
		<xsl:apply-templates select="td"/>
		</tr>
	</xsl:template>
	
	<xsl:template match="td">
		<td>
			<xsl:choose>
				<xsl:when test="$imgfig">
					<!-- image figurines -->
					<img border="0">
						<xsl:attribute name="src">
                            <xsl:value-of select="$inlineurl"/><xsl:text>/</xsl:text>
                            <xsl:value-of select="img"/><xsl:text>.png</xsl:text>
						</xsl:attribute>
					</img>
				</xsl:when>
				<xsl:otherwise>
					<!-- TT figurines -->
					<xsl:value-of select="char"/>
				</xsl:otherwise>
			</xsl:choose>
		</td>
	</xsl:template>
	

</xsl:stylesheet>
