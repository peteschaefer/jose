<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="text" encoding="UTF-8" indent="no" />

<xsl:template match="jose-export">
	<xsl:text>
	</xsl:text>
	<xsl:apply-templates select="game/head"/>
	<xsl:text>
	</xsl:text>
</xsl:template>

	<xsl:template match="head">
			<xsl:call-template name="first-header-line">
				<xsl:with-param name="head" select="."/>
			</xsl:call-template>
			<xsl:call-template name="second-header-line">
				<xsl:with-param name="head" select="."/>
			</xsl:call-template>
			<xsl:call-template name="third-header-line">
				<xsl:with-param name="head" select="."/>
			</xsl:call-template>
	</xsl:template>

	<!-- - - - - - - - - - - - - - - - - - - - - - -->
	<!-- First Header Line: Event, Site, Date, ... -->
	<!-- - - - - - - - - - - - - - - - - - - - - - -->
	<xsl:template name="first-header-line">
		<xsl:param name="head"/>

		<xsl:param name="Event" select="$head/tag[key='Event']/value"/>
		<xsl:param name="EventDate" select="$head/tag[key='EventDate']/value"/>
		<xsl:param name="Site"	select="$head/tag[key='Site']/value"/>
		<xsl:param name="Date" select="$head/tag[key='Date']/value"/>
		<xsl:param name="Round" select="$head/tag[key='Round']/value"/>
		<xsl:param name="Board" select="$head/tag[key='Board']/value"/>

		<xsl:param name="isEvent" select="string-length($Event)&gt;0 and $Event!='-'"/>
		<xsl:param name="isEventDate" select="string-length($EventDate)&gt;0 and $EventDate!='-'"/>
		<xsl:param name="isSite" select="string-length($Site)&gt;0 and $Site!='-'"/>
        <xsl:param name="isDate" select="string-length($Date)&gt;0 and $Date!='-'"/>
        <xsl:param name="isRound" select="string-length($Round)&gt;0 and $Round!='-'"/>
		<xsl:param name="isBoard" select="string-length($Board)&gt;0 and $Board!='-'"/>


		<xsl:if test="$isEvent or $isEventDate or $isSite or $isDate or $isRound or $isBoard">

			<xsl:if test="$isEvent">
					<xsl:value-of select="$Event"/>
					<xsl:if test="$isEventDate"> (<xsl:value-of select="$EventDate"/>) </xsl:if>
					<xsl:if test="$isSite or $isDate or $isRound or $isBoard">, </xsl:if>
			</xsl:if>

			<xsl:if test="$isSite">
					<xsl:value-of select="$Site"/>
					<xsl:choose>
						<xsl:when test="$isDate"><xsl:text> </xsl:text></xsl:when>
						<xsl:when test="$isRound or $isBoard">, </xsl:when>
					</xsl:choose>
			</xsl:if>

			<xsl:if test="$isDate">
					<xsl:value-of select="$Date"/>
					<xsl:if test="$isRound or $isBoard">, </xsl:if>
			</xsl:if>

			<xsl:if test="$isRound or $isBoard">
				<xsl:if test="$isRound">
						<xsl:value-of select="$head/tag[key='Round']/text"/><xsl:text>: </xsl:text>
						<xsl:value-of select="$Round"/>
						<xsl:if test="$isBoard">, </xsl:if>
				</xsl:if>
				<xsl:if test="$isBoard">
					<xsl:value-of select="$head/tag[key='Board']/text"/><xsl:text>: </xsl:text>
					<xsl:value-of select="$Board"/>
				</xsl:if>
			</xsl:if>

			<xsl:text>
</xsl:text>
		</xsl:if>

	</xsl:template>


	<!-- - - - - - - - - - - - - - - - - - - - - - -->
	<!-- Second Header Line: Players, Result, ... -->
	<!-- - - - - - - - - - - - - - - - - - - - - - -->
	<xsl:template name="second-header-line">
		<xsl:param name="head"/>

		<xsl:param name="White" select="$head/tag[key='White']/value"/>
		<xsl:param name="WhiteTitle" select="$head/tag[key='WhiteTitle']/value"/>
		<xsl:param name="WhiteELO" select="$head/tag[key='WhiteELO']/value"/>
		<xsl:param name="Black" select="$head/tag[key='Black']/value"/>
		<xsl:param name="BlackTitle" select="$head/tag[key='BlackTitle']/value"/>
		<xsl:param name="BlackELO" select="$head/tag[key='BlackELO']/value"/>
		<xsl:param name="Result" select="$head/tag[key='Result']/value"/>

		<xsl:param name="isWhite" select="string-length($White)&gt;0 and $White!='-'"/>
		<xsl:param name="isWhiteTitle" select="string-length($WhiteTitle)&gt;0 and $WhiteTitle!='-'"/>
		<xsl:param name="isWhiteELO" select="string-length($WhiteELO)&gt;0 and $WhiteELO!='-'"/>
		<xsl:param name="isBlack" select="string-length($Black)&gt;0 and $Black!='-'"/>
		<xsl:param name="isBlackTitle" select="string-length($BlackTitle)&gt;0 and $BlackTitle!='-'"/>
		<xsl:param name="isBlackELO" select="string-length($BlackELO)&gt;0 and $BlackELO!='-'"/>
		<xsl:param name="isResult" select="string-length($Result)&gt;0 and $Result!='-' and $Result!='*'"/>

		<xsl:if test="$isWhiteTitle or $isWhite or $isWhiteELO or $isBlackTitle or $isBlack or $isBlackELO or $isResult">

			<xsl:if test="$isWhiteTitle or $isWhite or $isWhiteELO">
					<xsl:value-of select="$WhiteTitle"/>
					<xsl:value-of select="$White"/>
				<xsl:if test="$isWhiteELO"> (<xsl:value-of select="$WhiteELO"/>) </xsl:if>
				<xsl:if test="$isBlackTitle or $isBlack or $isBlackELO"> - </xsl:if>
			</xsl:if>

			<xsl:if test="$isBlackTitle or $isBlack or $isBlackELO">
					<xsl:value-of select="$BlackTitle"/>
					<xsl:text> </xsl:text>
					<xsl:value-of select="$Black"/>
				<xsl:if test="$isBlackELO"> (<xsl:value-of select="$BlackELO"/>) </xsl:if>
			</xsl:if>

			<xsl:if test="$isResult">
					<xsl:text> </xsl:text>
					<xsl:value-of select="$Result"/>
			</xsl:if>

			<xsl:text>
</xsl:text>
		</xsl:if>

	</xsl:template>

	<!-- - - - - - - - - - - - - - - - - - - - - - -->
	<!-- Third and Fourth Header Line: Opening, Annotator ... -->
	<!-- - - - - - - - - - - - - - - - - - - - - - -->
	<xsl:template name="third-header-line">
		<xsl:param name="head"/>

		<xsl:param name="ECO" select="$head/tag[key='ECO']/value"/>
		<xsl:param name="Opening" select="$head/tag[key='Opening']/value"/>
		<xsl:param name="Annotator" select="$head/tag[key='Annotator']/value"/>

		<xsl:param name="isECO" select="string-length($ECO)&gt;0 and $ECO!='-'"/>
		<xsl:param name="isOpening" select="string-length($Opening)&gt;0 and $Opening!='-'"/>
		<xsl:param name="isAnnotator" select="string-length($Annotator)&gt;0 and $Annotator!='-'"/>

		<xsl:if test="$isECO or $isOpening">
			<xsl:value-of select="$ECO"/>
			<xsl:text> </xsl:text>
			<xsl:value-of select="$Opening"/>
			<br/>
		</xsl:if>

		<!-- Fourth Header Line -->
		<xsl:if test="$isAnnotator">
			<xsl:text>
</xsl:text>
			<xsl:value-of select="$head/tag[key='Annotator']/text"/><xsl:text>: </xsl:text>
			<xsl:value-of select="$Annotator"/>
			<xsl:text>
</xsl:text>
		</xsl:if>

		<xsl:text>
</xsl:text>
	</xsl:template>

</xsl:stylesheet>
