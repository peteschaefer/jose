<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format" 
	exclude-result-prefixes="fo">
	
	<xsl:import href="css.xsl"/>
	<xsl:import href="html_header.xsl"/>
	<xsl:import href="fig.xsl"/>
	<xsl:import href="html_body.xsl"/>
	<xsl:output method="html" version="4.0" encoding="UTF-8" indent="yes"/>

	<!-- the following info is evalulated by the jose application (not the XSLT processor) -->
	<jose:export xmlns:jose="http://jose-chess.sourceforge.net/xsl-info">
		<jose:title>xsl.dhtml</jose:title>
		
		<!-- accepted input -->
		<jose:input>list-of-games</jose:input>
		<!-- creates output: -->
		<jose:output>html</jose:output>

		<!-- include FENs for every move -->
		<jose:param>
			<jose:key>move-fen</jose:key>
			<jose:value>true</jose:value>
		</jose:param>
		<!-- create large icons -->
		<jose:param>
			<jose:key>large-icons</jose:key>
			<jose:value>true</jose:value>
		</jose:param>
		<!-- copy navigation icons -->
		<jose:param>
			<jose:key>nav-icons</jose:key>
			<jose:value>true</jose:value>
		</jose:param>
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
		<!-- name of linke JavaScript file (possible many) -->
		<jose:param>
			<jose:key>js-file</jose:key>
			<jose:value>games.js</jose:value>
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
			<style>
			A { text-decoration: none}
			img { border: 0; behavior: url(pngHack.htc); }
			div.scroll-body { width: 100%; overflow:auto; border: thin; }
			</style>
		</head>
		
		<body>		
		
		<script language="JavaScript">
			<xsl:text>
            imgurl = "</xsl:text>
            <xsl:if test="string-length($imgurl)&gt;0">
                <xsl:value-of select="$imgurl"/><xsl:text>/</xsl:text>
            </xsl:if>
            <xsl:value-of select="//figurines/dia/font"/><xsl:text>/</xsl:text>
            <xsl:value-of select="//figurines/dia/px-size"/><xsl:text>";
            </xsl:text>
		</script>
		
		<script language="JavaScript">
			<xsl:attribute name="src">
                <xsl:if test="string-length($imgurl)&gt;0">
                    <xsl:value-of select="$imgurl"/><xsl:text>/</xsl:text>
                </xsl:if>
                <xsl:text>games.js</xsl:text>
            </xsl:attribute>
		</script>

		<xsl:apply-templates select="game"/>

			<center class="body_comment_4">created with <a href="http://hrimfaxi.bitbucket.io/jose">jose Chess</a></center>
		</body>
	</html>
		
	</xsl:template>
	
	
	<xsl:template match="game">
		<xsl:param name="gidx" select="count(preceding::game)"/>
		<xsl:param name="fen" select="head/tag[key='FEN']/value"/>
        <xsl:param name="hasMoves" select="count(descendant::m) &gt; 0"/>
        <xsl:param name="hasFen" select="string-length($fen) &gt; 0"/>

		<script language="JavaScript">
			g = games[games.length] = new Object();
			var j = pos.length;

			g.start = j;
			g.current = j;

			<xsl:choose>
				<xsl:when test="$hasFen">g.init="<xsl:value-of select="$fen"/>";</xsl:when>
				<xsl:otherwise>g.init=null;</xsl:otherwise>
			</xsl:choose>

<xsl:for-each select="descendant::m">	pos[j] = "<xsl:value-of select="fen"/>"; dep[j++] = <xsl:value-of select="../depth"/>;
</xsl:for-each>

			g.end = j;
		</script>

		<table id="toptable">
			<tr>
			<td rowspan="2">
                <xsl:if test="$hasFen or $hasMoves">
                    <xsl:call-template name="create-diagram">
                        <xsl:with-param name="gidx" select="$gidx"/>
                    </xsl:call-template>
                </xsl:if>
            </td>
			<td width="80" rowspan="2">

			</td>
			<td valign="top">
				<xsl:apply-templates select="head"/>
			</td>
			</tr>

<xsl:if test="$hasMoves">
			<tr>
				<td>
					<form>
						<xsl:attribute name="name">game-<xsl:value-of select="$gidx"/></xsl:attribute>

						<image width="36" height="32" src="backward-stop.png">
							<xsl:attribute name="name">button-<xsl:value-of select="$gidx"/>-1</xsl:attribute>
							<xsl:attribute name="onclick">first(<xsl:value-of select="$gidx"/>)</xsl:attribute>
							<xsl:attribute name="onmouseover">hover(<xsl:value-of select="$gidx"/>,1,1)</xsl:attribute>
							<xsl:attribute name="onmouseout">hover(<xsl:value-of select="$gidx"/>,1,2)</xsl:attribute>
							<xsl:attribute name="onmousedown">hover(<xsl:value-of select="$gidx"/>,1,3)</xsl:attribute>
							<xsl:attribute name="onmouseup">hover(<xsl:value-of select="$gidx"/>,1,4)</xsl:attribute>
						</image>
						<image width="36" height="32" src="left.png">
							<xsl:attribute name="name">button-<xsl:value-of select="$gidx"/>-2</xsl:attribute>
							<xsl:attribute name="onclick">previous(<xsl:value-of select="$gidx"/>)</xsl:attribute>
							<xsl:attribute name="onmouseover">hover(<xsl:value-of select="$gidx"/>,2,1)</xsl:attribute>
							<xsl:attribute name="onmouseout">hover(<xsl:value-of select="$gidx"/>,2,2)</xsl:attribute>
							<xsl:attribute name="onmousedown">hover(<xsl:value-of select="$gidx"/>,2,3)</xsl:attribute>
							<xsl:attribute name="onmouseup">hover(<xsl:value-of select="$gidx"/>,2,4)</xsl:attribute>
						</image>
						<image width="36" height="32" src="right.png">
							<xsl:attribute name="name">button-<xsl:value-of select="$gidx"/>-3</xsl:attribute>
							<xsl:attribute name="onclick">next(<xsl:value-of select="$gidx"/>)</xsl:attribute>
							<xsl:attribute name="onmouseover">hover(<xsl:value-of select="$gidx"/>,3,1)</xsl:attribute>
							<xsl:attribute name="onmouseout">hover(<xsl:value-of select="$gidx"/>,3,2)</xsl:attribute>
							<xsl:attribute name="onmousedown">hover(<xsl:value-of select="$gidx"/>,3,3)</xsl:attribute>
							<xsl:attribute name="onmouseup">hover(<xsl:value-of select="$gidx"/>,3,4)</xsl:attribute>
						</image>
						<image width="36" height="32" src="forward-stop.png">
							<xsl:attribute name="name">button-<xsl:value-of select="$gidx"/>-4</xsl:attribute>
							<xsl:attribute name="onclick">last(<xsl:value-of select="$gidx"/>)</xsl:attribute>
							<xsl:attribute name="onmouseover">hover(<xsl:value-of select="$gidx"/>,4,1)</xsl:attribute>
							<xsl:attribute name="onmouseout">hover(<xsl:value-of select="$gidx"/>,4,2)</xsl:attribute>
							<xsl:attribute name="onmousedown">hover(<xsl:value-of select="$gidx"/>,4,3)</xsl:attribute>
							<xsl:attribute name="onmouseup">hover(<xsl:value-of select="$gidx"/>,4,4)</xsl:attribute>
						</image>

						<image width="36" height="32" src="fast-forward.png">
							<xsl:attribute name="name">button-<xsl:value-of select="$gidx"/>-5</xsl:attribute>
							<xsl:attribute name="onclick">animate(<xsl:value-of select="$gidx"/>, 0.5, +1)</xsl:attribute>
							<xsl:attribute name="onmouseover">hover(<xsl:value-of select="$gidx"/>,5,1)</xsl:attribute>
							<xsl:attribute name="onmouseout">hover(<xsl:value-of select="$gidx"/>,5,2)</xsl:attribute>
							<xsl:attribute name="onmousedown">hover(<xsl:value-of select="$gidx"/>,5,3)</xsl:attribute>
							<xsl:attribute name="onmouseup">hover(<xsl:value-of select="$gidx"/>,5,4)</xsl:attribute>
						</image>
						<image width="36" height="32" src="stop.png">
							<xsl:attribute name="name">button-<xsl:value-of select="$gidx"/>-6</xsl:attribute>
							<xsl:attribute name="onclick">stop_animation()</xsl:attribute>
							<xsl:attribute name="onmouseover">hover(<xsl:value-of select="$gidx"/>,6,1)</xsl:attribute>
							<xsl:attribute name="onmouseout">hover(<xsl:value-of select="$gidx"/>,6,2)</xsl:attribute>
							<xsl:attribute name="onmousedown">hover(<xsl:value-of select="$gidx"/>,6,3)</xsl:attribute>
							<xsl:attribute name="onmouseup">hover(<xsl:value-of select="$gidx"/>,6,4)</xsl:attribute>
						</image>
					</form>
				</td>
			</tr>
</xsl:if>
		</table>

		<div class="scroll-body" id="gamebody">
		<xsl:apply-templates select="body"/>
		</div>

		<script language="JavaScript">
			<xsl:attribute name="defer"/>
			resizeDiv();
			window.onresize = resizeDiv;
			initial(<xsl:value-of select="$gidx"/>)
		</script>
		<br/>

	</xsl:template>
	
	<!-- Move -->
	<xsl:template match="m">
		<xsl:param name="style" select="concat('body.line.',../depth)"/>
		<xsl:param name="i" select="count(preceding::m)"/>
		
		<xsl:text> </xsl:text>
		<a>
		<xsl:attribute name="name"><xsl:value-of select="$i"/></xsl:attribute>
		<xsl:attribute name="href">javascript:go(<xsl:value-of select="$i"/>)</xsl:attribute>
		<xsl:attribute name="class"><xsl:value-of select="translate($style,'.','_')"/></xsl:attribute>	
		<xsl:apply-templates/></a>
		<xsl:text> </xsl:text>
	</xsl:template>
	
	<xsl:template match="fen"><xsl:text></xsl:text></xsl:template>
	
	
	<!-- create diagram images -->
	<xsl:template name="create-diagram">
		<xsl:param name="gidx"/>
		
		<table border="1" cellpadding="0" cellspacing="0"><tr><td>
		<table cellpadding="0" cellspacing="0">
			<xsl:call-template name="create-diagram-row"><xsl:with-param name="gidx" select="$gidx"/><xsl:with-param name="row" select="0"/></xsl:call-template>
			<xsl:call-template name="create-diagram-row"><xsl:with-param name="gidx" select="$gidx"/><xsl:with-param name="row" select="1"/></xsl:call-template>
			<xsl:call-template name="create-diagram-row"><xsl:with-param name="gidx" select="$gidx"/><xsl:with-param name="row" select="2"/></xsl:call-template>
			<xsl:call-template name="create-diagram-row"><xsl:with-param name="gidx" select="$gidx"/><xsl:with-param name="row" select="3"/></xsl:call-template>
			<xsl:call-template name="create-diagram-row"><xsl:with-param name="gidx" select="$gidx"/><xsl:with-param name="row" select="4"/></xsl:call-template>
			<xsl:call-template name="create-diagram-row"><xsl:with-param name="gidx" select="$gidx"/><xsl:with-param name="row" select="5"/></xsl:call-template>
			<xsl:call-template name="create-diagram-row"><xsl:with-param name="gidx" select="$gidx"/><xsl:with-param name="row" select="6"/></xsl:call-template>
			<xsl:call-template name="create-diagram-row"><xsl:with-param name="gidx" select="$gidx"/><xsl:with-param name="row" select="7"/></xsl:call-template>
		</table>
		</td></tr></table>
	</xsl:template>
	
	<xsl:template name="create-diagram-row">
		<xsl:param name="gidx"/>
		<xsl:param name="row"/>
		
		<tr>
		<xsl:call-template name="create-diagram-cell"><xsl:with-param name="gidx" select="$gidx"/><xsl:with-param name="row" select="$row"/><xsl:with-param name="col" select="0"/></xsl:call-template>
		<xsl:call-template name="create-diagram-cell"><xsl:with-param name="gidx" select="$gidx"/><xsl:with-param name="row" select="$row"/><xsl:with-param name="col" select="1"/></xsl:call-template>
		<xsl:call-template name="create-diagram-cell"><xsl:with-param name="gidx" select="$gidx"/><xsl:with-param name="row" select="$row"/><xsl:with-param name="col" select="2"/></xsl:call-template>
		<xsl:call-template name="create-diagram-cell"><xsl:with-param name="gidx" select="$gidx"/><xsl:with-param name="row" select="$row"/><xsl:with-param name="col" select="3"/></xsl:call-template>
		<xsl:call-template name="create-diagram-cell"><xsl:with-param name="gidx" select="$gidx"/><xsl:with-param name="row" select="$row"/><xsl:with-param name="col" select="4"/></xsl:call-template>
		<xsl:call-template name="create-diagram-cell"><xsl:with-param name="gidx" select="$gidx"/><xsl:with-param name="row" select="$row"/><xsl:with-param name="col" select="5"/></xsl:call-template>
		<xsl:call-template name="create-diagram-cell"><xsl:with-param name="gidx" select="$gidx"/><xsl:with-param name="row" select="$row"/><xsl:with-param name="col" select="6"/></xsl:call-template>
		<xsl:call-template name="create-diagram-cell"><xsl:with-param name="gidx" select="$gidx"/><xsl:with-param name="row" select="$row"/><xsl:with-param name="col" select="7"/></xsl:call-template>
		<!--<br clear="all"/>-->
		</tr>
	</xsl:template>
	
	<xsl:template name="create-diagram-cell">
		<xsl:param name="gidx"/>
		<xsl:param name="row"/>
		<xsl:param name="col"/>
		
		<td><img>
			<xsl:attribute name="name">i-<xsl:value-of select="$gidx"/>-<xsl:value-of select="8*$row+$col"/></xsl:attribute>
			<xsl:attribute name="width"><xsl:value-of select="/jose-export/figurines/dia/px-size"/></xsl:attribute>
			<xsl:attribute name="height"><xsl:value-of select="/jose-export/figurines/dia/px-size"/></xsl:attribute>
		</img></td>
	</xsl:template>
	
</xsl:stylesheet>
