<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:import href="tex_header.xsl"/>
	<xsl:output method="text" version="1.0" encoding="ISO-8859-1" indent="no"/>
		
	<!-- the following info is evalulated by the jose application (not the XSLT processor) -->
	<jose:export xmlns:jose="http://jose-chess.sourceforge.net/xsl-info">
		<jose:title>xsl.tex</jose:title>
		
		<!-- accepted input -->
		<jose:input>list-of-games</jose:input>
		<!-- creates output: -->
		<jose:output>tex</jose:output>
	</jose:export>

<!-- - - - - - - - - - - - - - -->
<!-- Main Template     -->
<!-- - - - - - - - - - - - - - -->
<xsl:template match="jose-export">

\documentclass[10pt,twocolumn]{article}
% You must have the "chess12" package installed to typeset this file.

\usepackage{times}
\usepackage{a4wide}
\usepackage{chess}
\usepackage{german}
\usepackage[latin1]{inputenc}
\usepackage[T1]{fontenc}

\setlength{\columnsep}{7mm}
\setlength{\parindent}{0pt}
			
% Macros for variations and diagrams:
\newenvironment{variation}{\begin{quote}}{\end{quote}}
\newenvironment{diagram}{\begin{nochess}}{$$\showboard$$\end{nochess}}


\begin{document}

<xsl:for-each select="game">
	<xsl:apply-templates select="head"/>
	<xsl:text>
	</xsl:text>

	<!-- initial FEN ? -->
	<xsl:apply-templates select="diagram"/>

	<xsl:apply-templates select="body"/>

\vspace{0.5cm}
{\center\hrule}
\vspace{0.5cm}

</xsl:for-each>

\begin{center} created with jose Chess: peteschaefer.github.io/jose \end{center}

\end{document}

</xsl:template>
	
<!-- - - - - - - - - - - - - - -->
<!-- Main Line                 -->
<!-- - - - - - - - - - - - - - -->
<xsl:template match="v[depth=0]">
	<xsl:text> \begin{chess} {\bf </xsl:text>
	<xsl:apply-templates/>
	<xsl:text> } \end{chess} </xsl:text>
</xsl:template>

<!-- - - - - - - - - - - - - - -->
<!-- Variation Line            -->
<!-- - - - - - - - - - - - - - -->
<xsl:template match="v[depth=1]">
	<xsl:text>
	} \\ \begin{variation} [</xsl:text>
	<xsl:apply-templates/>
	<xsl:text>] \end{variation} \\ {\bf
	</xsl:text>
</xsl:template>


<!-- - - - - - - - - - - - - - -->
<!-- Subvariation Line         -->
<!-- - - - - - - - - - - - - - -->
<xsl:template match="v[depth &gt; 1]">
	<xsl:text>{\small (</xsl:text>
	<xsl:apply-templates/>
	<xsl:text>) }</xsl:text>
</xsl:template>

<!-- hide "depth" -->
<xsl:template match="depth"></xsl:template>
<!-- hide "fen" -->
<xsl:template match="fen"></xsl:template>


<!-- Move -->
<xsl:template match="m">
	<xsl:text> </xsl:text>
	<xsl:apply-templates/>
	<xsl:text> </xsl:text>
</xsl:template>

<!-- Annotation -->
<xsl:template match="a">
	<xsl:choose>
		<xsl:when test="nag=14"> \wbetter </xsl:when>
		<xsl:when test="nag=15"> \bbetter </xsl:when>
		<xsl:when test="nag=16"> \wupperhand </xsl:when>
		<xsl:when test="nag=17"> \bupperhand </xsl:when>
		<xsl:when test="nag=18"> \wdecisive </xsl:when>
		<xsl:when test="nag=19"> \bdecisive </xsl:when>
		<xsl:when test="nag=10 or nag=11 or nag=144"> \equal </xsl:when>
		<xsl:when test="nag=13"> \unclear </xsl:when>
		<xsl:when test="nag=44 or nag=45"> \compensation </xsl:when>
		<xsl:when test="nag &gt;= 30 and nag &lt;= 35"> \devadvantage </xsl:when>
		<xsl:when test="nag &gt;= 24 and nag &lt;= 29"> \moreroom </xsl:when>
		<xsl:when test="nag=40 or nag=41"> \withattack </xsl:when>
		<xsl:when test="nag &gt;= 36 and nag &lt;= 39"> \withinit </xsl:when>
		<xsl:when test="nag &gt;= 130 and nag &lt;= 135"> \counterplay </xsl:when>
		<xsl:when test="nag=22 or nag=23"> \zugzwang </xsl:when>
		<xsl:when test="nag=140"> \withidea </xsl:when>
		<xsl:when test="nag=7 or nag=8"> \onlymove </xsl:when>
		<xsl:when test="nag=142"> \betteris </xsl:when>
		<xsl:when test="nag=149"> \file </xsl:when>
		<xsl:when test="nag=150"> \diagonal </xsl:when>
		<xsl:when test="nag &gt;= 48 and nag &lt;= 53"> \centre </xsl:when>
		<xsl:when test="nag &gt;= 54 and nag &lt;= 59"> \kside </xsl:when>
		<xsl:when test="nag &gt;= 60 and nag &lt;= 65"> \qside </xsl:when>
		<xsl:when test="nag=147"> \weakpt </xsl:when>
		<xsl:when test="nag=148"> \ending </xsl:when>
		<xsl:when test="nag=151 or nag=152"> \bishoppair </xsl:when>
		<xsl:when test="nag=153"> \opposbishops </xsl:when>
		<xsl:when test="nag=154"> \samebishops </xsl:when>
		<xsl:when test="nag=193"> \unitedpawns </xsl:when>
		<xsl:when test="nag=192"> \seppawns </xsl:when>		
		<xsl:when test="nag=191"> \doublepawns </xsl:when>
		<xsl:when test="nag=156"> \passedpawns </xsl:when>
		<xsl:when test="nag=157"> \morepawns</xsl:when>
		<xsl:when test="nag &gt;= 136 and nag &lt;= 139"> \timelimit </xsl:when>		
		<xsl:when test="nag=146"> \novelty </xsl:when>
		<xsl:when test="nag=145"> \comment </xsl:when>
		<xsl:when test="nag=190"> \various </xsl:when>
		<xsl:when test="nag=158"> \with </xsl:when>
		<xsl:when test="nag=159"> \without </xsl:when>
		<xsl:when test="nag=190"> \etc </xsl:when>
		<xsl:when test="nag=161"> \see </xsl:when>
		
		<xsl:otherwise> <xsl:value-of select="text"/> </xsl:otherwise>
	</xsl:choose>
</xsl:template>
	
<!-- Comment -->
<xsl:template match="c">
		<xsl:text>
\begin{nochess}{\rm </xsl:text>
		<xsl:apply-templates/>
		<xsl:text> }\end{nochess} 
</xsl:text>
</xsl:template>

<xsl:template match="bold"> {\bf <xsl:apply-templates/> } </xsl:template>
<xsl:template match="italic"> {\it <xsl:apply-templates/> } </xsl:template>
<xsl:template match="underline"> \underbar{ <xsl:apply-templates/> } </xsl:template>

<xsl:template match="center"> {\centering <xsl:apply-templates/> } </xsl:template>
<xsl:template match="right"> {\raggedleft <xsl:apply-templates/> } </xsl:template>

<xsl:template match="font">
<xsl:if test="@size"></xsl:if>
<xsl:if test="@color"></xsl:if>
	<xsl:apply-templates/>
<xsl:if test="@size"></xsl:if>
<xsl:if test="@color"></xsl:if>
</xsl:template>

<xsl:template match="br"> \\ </xsl:template>



<!-- Figurines -->
<xsl:template match="p">P</xsl:template>
<xsl:template match="n">N</xsl:template>
<xsl:template match="b">B</xsl:template>
<xsl:template match="r">R</xsl:template>
<xsl:template match="q">Q</xsl:template>
<xsl:template match="k">K</xsl:template>
	
<!-- Diagram -->
<xsl:template match="diagram">
\begin{diagram}
\board	
<xsl:for-each select="texboard/row">
<xsl:text>{</xsl:text><xsl:value-of select="."/><xsl:text>}
</xsl:text>
</xsl:for-each>
\end{diagram}
</xsl:template>
		
<!-- Result -->
<xsl:template match="result">
	{\bf <xsl:value-of select="."/>}		
</xsl:template>


</xsl:stylesheet>
