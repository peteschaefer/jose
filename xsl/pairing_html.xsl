<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:fo="http://www.w3.org/1999/XSL/Format" 
	exclude-result-prefixes="fo">
	
	<xsl:import href="css.xsl"/>
	<xsl:output method="html" version="4.0" encoding="UTF-8" indent="yes" />

	<!-- - - - - - - - - - - - - - -->
	<!-- Main Template     -->
	<!-- - - - - - - - - - - - - - -->
	<xsl:template match="jose-export">
	
	<xsl:variable name="rounds" select="pairing-list/count-rounds"/>
		
	<html>
		<head>
			<title><xsl:value-of select="pairing-list/event"/></title>
<!--			<xsl:call-template name="include_css"/> -->
		</head>
		
		<style>
		
		
			body {
				font-family: Arial,sans-serif;
			}

			.round-header {
				font-weight: bold;
				font-size: 12pt;

				padding: 4 4 4 4;
				background-color: #eeeeee;
			}
			td.white, td.black, td.bye, td.result {
				padding: 4 4 4 4;
				border-style: solid;
				border-width: 2pt;
				border-color: #eeeeee;
				font-size: 10pt;
			}
			.white, .black {
				width: 3.5cm;
			}
			.result {
				text-align: center;
				width: 1.5cm;
			}
			.bye {
				font-style: italic;
			}
		
		</style>
		
		<body>

			<h2 class="page-header"><xsl:value-of select="pairing-list/event"/></h2>

			<table border="0">
				<tr>
					<td valign="top">
					<table border="0">
						<tbody>
							<xsl:apply-templates select="pairing-list/game-list/round[number(description) &lt;= (($rounds+1) div 2)]"/>
						</tbody>
					</table>
					</td>
					<td valign="top">
					<table border="0">
						<tbody>
							<xsl:apply-templates select="pairing-list/game-list/round[number(description) &gt; (($rounds+1) div 2)]"/>
						</tbody>
					</table>
					</td>
				</tr>
			</table>
			
<!--			<center class="body_comment_4">created with <a href="http://jose-chess.sourceforge.net">jose-chess.sourceforge.net</a></center> -->

		</body>
	</html>
		
	</xsl:template>
	
	<xsl:template match="round">
		<tr>
			<td colspan="3" height="16pt">&#160; </td>
		</tr>
		<tr class="round-header">
			<td colspan="3" class="round-header">
				Runde <xsl:value-of select="description"/>
			</td>
		</tr>
		<xsl:apply-templates select="game"/>
		<xsl:apply-templates select="bye"/>
	</xsl:template>
	
	<xsl:template match="game">
		<tr class="game">
			<td class="white"><xsl:value-of select="white-name"/></td>
			<td class="black"><xsl:value-of select="black-name"/></td>
			<td class="result">
			<xsl:choose>
				<xsl:when test="result='*'">-</xsl:when>
				<xsl:when test="result='1/2'">&#189;-&#189;</xsl:when>
				<xsl:otherwise><xsl:value-of select="result"/></xsl:otherwise>
			</xsl:choose>			
			</td>
		</tr>
	</xsl:template>
	
	<xsl:template match="bye">
		<tr class="game">
			<td class="white"><xsl:value-of select="name"/></td>
			<td class="bye">Freilos</td>
			<td class="result"></td>
		</tr>
	</xsl:template>
	
</xsl:stylesheet>
