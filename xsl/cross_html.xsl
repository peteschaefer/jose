<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [ 
    <!ENTITY nbsp "&#160;">
    <!ENTITY frac12 "&#189;">  ]>  
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:fo="http://www.w3.org/1999/XSL/Format" 
	exclude-result-prefixes="fo">
	
	<xsl:output method="html" version="4.0" encoding="UTF-8" indent="yes" />

	<!-- - - - - - - - - - - - - - -->
	<!-- Main Template     -->
	<!-- - - - - - - - - - - - - - -->
	<xsl:template match="jose-export">
	
	<html>
		<head>
			<title><xsl:value-of select="cross-table/event"/></title>
			<style>
			body {
				font-family: Arial, sans-serif;
				font-size: 12pt;
			}
			TD.sub-title {
				font-weight: bold;
				font-size: 12pt;

				margin-bottom: 6pt;

				padding: 4 4 4 4;
				background-color: #eeeeee;
			}

			TD {
				border-style: solid;
				border-width: 2pt;
				border-color: #eeeeee;
			}
			TD.empty-cell {
				border-style: none;
			}
			TD.header-cell {
				padding: 4 4 4 4;
				text-align: center;
			}
			TD.rank-cell,TD.name-cell,TD.total-cell,TD.gamecount-cell,TD.grid-cell {
				padding: 4 4 4 4;
			}
			TD.grid-cell {
				width: 8mm;
				height: 8mm;
				text-align: center;
			}
			TD.rank-cell {
				padding-left: 16;
				padding-right: 16;
				text-align: right;
			}
			TD.gamecount-cell {
				padding-left: 16;
				padding-right: 16;
				text-align: right;
			}
			TD.total-cell {
				padding-left: 16;
				padding-right: 16;
				font-weight: bold;
				text-align: right;
			}
			</style>
		</head>
		
		<body>

			<h2 class="title"><xsl:value-of select="cross-table/event"/></h2>			

			<table>
				<thead>
					<tr>
						<td class="sub-title">
							<xsl:attribute name="colspan"><xsl:value-of select="4 + number(cross-table/count-players)"/></xsl:attribute>
							<xsl:choose>
								<xsl:when test="cross-table/complete-rounds=''">&nbsp;</xsl:when>
								<xsl:when test="cross-table/complete-rounds='0'">&nbsp;</xsl:when>
								<xsl:when test="cross-table/complete-rounds='all'">Endstand</xsl:when>
								<xsl:otherwise>Stand nach der <xsl:value-of select="cross-table/complete-rounds"/>. Runde</xsl:otherwise>
							</xsl:choose>
						</td>
					</tr>
					<xsl:apply-templates select="cross-table/table/head"/>
				</thead>
				
				<tbody>
					<xsl:apply-templates select="cross-table/table/body"/>
				</tbody>
			</table>			

		</body>
	</html>
		
	</xsl:template>
	
	<xsl:template match="head">
		<tr>
			<td class="empty-cell"></td>	<!-- rank -->
			<td class="empty-cell"></td><!-- player name -->
			
			<xsl:for-each select="player">
				<td class="header-cell"><xsl:value-of select="rank"/></td>
			</xsl:for-each>
			
			<td class="header-cell">Summe</td>
			<td class="header-cell">aus</td>
			
		</tr>
	</xsl:template>
	
	<xsl:template match="body">
		<xsl:for-each select="row">
			<tr>
				<td class="rank-cell"><xsl:value-of select="player/rank"/></td>
				<td class="name-cell"><xsl:value-of select="player/name"/></td>
				
				<xsl:for-each select="c">
					<xsl:choose>
						<xsl:when test=".='x'"><td class="empty-cell"></td></xsl:when>
						<xsl:when test=".=''"><td class="grid-cell">&nbsp;</td></xsl:when>
						<xsl:otherwise>
							<td class="grid-cell">
							<xsl:call-template name="chess-result">
							<xsl:with-param name="value" select="."/>
							</xsl:call-template>
							</td>
						</xsl:otherwise>
					</xsl:choose>				
				</xsl:for-each>
				
				<td class="total-cell">
					<xsl:call-template name="chess-result">
						<xsl:with-param name="value" select="player/total"/>
					</xsl:call-template>
				</td>
				<td class="gamecount-cell">
					<xsl:value-of select="player/gameCount"/>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="chess-result">
		<xsl:param name="value"/>

		<xsl:choose>
			<xsl:when test="$value=0">0</xsl:when>
			<xsl:when test="$value=0.5"></xsl:when>
			<xsl:otherwise><xsl:value-of select="floor($value)"/></xsl:otherwise>
		</xsl:choose>
		
		<xsl:if test="$value &gt; floor($value)">&frac12;</xsl:if>
	</xsl:template>
	
</xsl:stylesheet>
