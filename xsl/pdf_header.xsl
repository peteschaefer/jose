<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>


	<xsl:param name="styles" select="../../styles"/>

	<!-- - - - - - - - - - - - - - - - - -->
	<!--   Game Header          -->
	<!-- - - - - - - - - - - - - - - - - -->
	<xsl:template match="head">
		<xsl:param name="style" select="$styles//style[name='header']"/>
		<xsl:param name="color" select="$style/a[key='foreground']/value"/>

		<!-- FOP workaround: keep-together works only within table-row !! -->
		<fo:table table-layout="fixed" width="100%">
			<fo:table-column/>
			<fo:table-body> <fo:table-row keep-together="always"><fo:table-cell> 
			
			<fo:block><!-- header block -->
				<!-- font attributes -->
				<xsl:attribute name="font-family"><xsl:value-of select="$style/a[key='family']/value"/></xsl:attribute>
				<xsl:attribute name="font-size"><xsl:value-of select="$style/a[key='size']/value"/>pt</xsl:attribute>
				<xsl:choose>
					<xsl:when test="string-length($color)=0"><xsl:attribute name="color">black</xsl:attribute></xsl:when>
					<xsl:otherwise><xsl:attribute name="color"><xsl:value-of select="$color"/></xsl:attribute></xsl:otherwise>
				</xsl:choose>
				<xsl:choose>
					<xsl:when test="$style/a[key='bold']/value='true'"><xsl:attribute name="font-weight">bold</xsl:attribute></xsl:when>
					<xsl:otherwise><xsl:attribute name="font-weight">normal</xsl:attribute></xsl:otherwise>
				</xsl:choose>
				<xsl:choose>
					<xsl:when test="$style/a[key='italic']/value='true'"><xsl:attribute name="font-style">italic</xsl:attribute></xsl:when>
					<xsl:otherwise><xsl:attribute name="font-style">normal</xsl:attribute></xsl:otherwise>
				</xsl:choose>
	
				<xsl:call-template name="first-header-line">
					<xsl:with-param name="head" select="."/>
				</xsl:call-template>
	
				<xsl:call-template name="second-header-line">
					<xsl:with-param name="head" select="."/>
				</xsl:call-template>
	
				<xsl:call-template name="third-header-line">
					<xsl:with-param name="head" select="."/>
				</xsl:call-template>
	
			</fo:block>
		</fo:table-cell> </fo:table-row> </fo:table-body> </fo:table>
			
	</xsl:template>


	<!-- - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- First Line: Event, Date ...          -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - -->
	<xsl:template name="first-header-line">
		<xsl:param name="head"/>
	
		<xsl:param name="Event" select="$head/tag[key='Event']/value"/>
		<xsl:param name="EventDate" select="$head/tag[key='EventDate']/value"/>
		<xsl:param name="Site" select="$head/tag[key='Site']/value"/>
		<xsl:param name="Date" select="$head/tag[key='Date']/value"/>
		<xsl:param name="Round" select="$head/tag[key='Round']/value"/>
		<xsl:param name="Board" select="$head/tag[key='Board']/value"/>

		<xsl:param name="event_style" select="$styles//style[name='header.event']"/>
		<xsl:param name="site_style" select="$styles//style[name='header.site']"/>
		<xsl:param name="date_style" select="$styles//style[name='header.date']"/>
		<xsl:param name="round_style" select="$styles//style[name='header.round']"/>
		<!-- other elements inherit style from header -->
		<xsl:param name="event_color" select="$event_style/a[key='foreground']/value"/>
		<xsl:param name="site_color" select="$site_style/a[key='foreground']/value"/>
		<xsl:param name="date_color" select="$date_style/a[key='foreground']/value"/>
		<xsl:param name="round_color" select="$round_style/a[key='foreground']/value"/>

		<xsl:param name="isEvent" select="string-length($Event)&gt;0 and $Event!='-'"/>
		<xsl:param name="isEventDate" select="string-length($EventDate)&gt;0 and $EventDate!='-'"/>
		<xsl:param name="isSite" select="string-length($Site)&gt;0 and $Site!='-'"/>
        <xsl:param name="isDate" select="string-length($Date)&gt;0 and $Date!='-'"/>
        <xsl:param name="isRound" select="string-length($Round)&gt;0 and $Round!='-'"/>
		<xsl:param name="isBoard" select="string-length($Board)&gt;0 and $Board!='-'"/>

		
		<xsl:if test="$isEvent or $isEventDate or $isSite or $isDate or $isRound or $isBoard">
			<fo:block>
				<!-- Event, EventDate -->
				<xsl:if test="$isEvent">
					<fo:inline>
						<!-- font attributes -->
						<xsl:attribute name="font-family"><xsl:value-of select="$event_style/a[key='family']/value"/></xsl:attribute>
						<xsl:attribute name="font-size"><xsl:value-of select="$event_style/a[key='size']/value"/>pt</xsl:attribute>
						<xsl:choose>
							<xsl:when test="string-length($event_color)=0"><xsl:attribute name="color">black</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="color"><xsl:value-of select="$event_color"/></xsl:attribute></xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$event_style/a[key='bold']/value='true'"><xsl:attribute name="font-weight">bold</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="font-weight">normal</xsl:attribute></xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$event_style/a[key='italic']/value='true'"><xsl:attribute name="font-style">italic</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="font-style">normal</xsl:attribute></xsl:otherwise>
						</xsl:choose>
											
						<xsl:value-of select="$Event"/>
						
						<xsl:if test="$isEventDate">
							<xsl:text> (</xsl:text><xsl:value-of select="$EventDate"/><xsl:text>)</xsl:text>
						</xsl:if>

						<xsl:if test="$isSite or $isDate or $isRound or $isBoard">
							<xsl:text>, </xsl:text>
						</xsl:if>
					</fo:inline>
				</xsl:if>
							
				<!-- Site -->
				<xsl:if test="$isSite">
					<fo:inline>
						<!-- font attributes -->
						<xsl:attribute name="font-family"><xsl:value-of select="$site_style/a[key='family']/value"/></xsl:attribute>
						<xsl:attribute name="font-size"><xsl:value-of select="$site_style/a[key='size']/value"/>pt</xsl:attribute>
						<xsl:choose>
							<xsl:when test="string-length($site_color)=0"><xsl:attribute name="color">black</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="color"><xsl:value-of select="$site_color"/></xsl:attribute></xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$site_style/a[key='bold']/value='true'"><xsl:attribute name="font-weight">bold</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="font-weight">normal</xsl:attribute></xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$site_style/a[key='italic']/value='true'"><xsl:attribute name="font-style">italic</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="font-style">normal</xsl:attribute></xsl:otherwise>
						</xsl:choose>
					
						<xsl:value-of select="$Site"/>

						<xsl:choose>
							<xsl:when test="$isDate"><xsl:text> </xsl:text></xsl:when>
							<xsl:when test="$isRound or $isBoard">, </xsl:when>
						</xsl:choose>
					</fo:inline>
				</xsl:if>							
							
				<!-- Date -->
				<xsl:if test="$isDate">
					<fo:inline>
						<!-- font attributes -->
						<xsl:attribute name="font-family"><xsl:value-of select="$date_style/a[key='family']/value"/></xsl:attribute>
						<xsl:attribute name="font-size"><xsl:value-of select="$date_style/a[key='size']/value"/>pt</xsl:attribute>
						<xsl:choose>
							<xsl:when test="string-length($date_color)=0"><xsl:attribute name="color">black</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="color"><xsl:value-of select="$date_color"/></xsl:attribute></xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$date_style/a[key='bold']/value='true'"><xsl:attribute name="font-weight">bold</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="font-weight">normal</xsl:attribute></xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$date_style/a[key='italic']/value='true'"><xsl:attribute name="font-style">italic</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="font-style">normal</xsl:attribute></xsl:otherwise>
						</xsl:choose>
					
						<xsl:value-of select="$Date"/>
					
						<xsl:if test="$isRound or $isBoard">
							<xsl:text>, </xsl:text>
						</xsl:if>
					</fo:inline>				
				</xsl:if>							
							
				<!-- Round, Board -->
				<xsl:if test="$isRound or $isBoard">
					<fo:inline>
						<!-- font attributes -->
						<xsl:attribute name="font-family"><xsl:value-of select="$round_style/a[key='family']/value"/></xsl:attribute>
						<xsl:attribute name="font-size"><xsl:value-of select="$round_style/a[key='size']/value"/>pt</xsl:attribute>
						<xsl:choose>
							<xsl:when test="string-length($round_color)=0"><xsl:attribute name="color">black</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="color"><xsl:value-of select="$round_color"/></xsl:attribute></xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$round_style/a[key='bold']/value='true'"><xsl:attribute name="font-weight">bold</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="font-weight">normal</xsl:attribute></xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$round_style/a[key='italic']/value='true'"><xsl:attribute name="font-style">italic</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="font-style">normal</xsl:attribute></xsl:otherwise>
						</xsl:choose>
					
						<xsl:if test="$isRound">
							<fo:inline keep-together.within-line="always">
								<!-- note: keep-together ignored by FOP -->
								<xsl:value-of select="$head/tag[key='Round']/text"/>
								<xsl:text> </xsl:text>
								<xsl:value-of select="$Round"/>
								
								<xsl:if test="$isBoard">
									<xsl:text>, </xsl:text>
								</xsl:if>
							</fo:inline>
						</xsl:if>

						<xsl:if test="$isBoard">
							<fo:inline keep-together.within-line="always">
								<!-- note: keep-together ignored by FOP -->
								<xsl:value-of select="$head/tag[key='Board']/text"/>
								<xsl:text> </xsl:text>
								<xsl:value-of select="$Board"/>
							</fo:inline>
						</xsl:if>
						
					</fo:inline>				
				</xsl:if>							
							
			</fo:block>		
		</xsl:if>
	</xsl:template>

	<!-- - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- Second Line: Players and Result ...          -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - -->
	<xsl:template name="second-header-line">
		<xsl:param name="head"/>
		
		<xsl:param name="White" select="$head/tag[key='White']/value"/>
		<xsl:param name="Black" select="$head/tag[key='Black']/value"/>							
		<xsl:param name="WhiteTitle" select="$head/tag[key='WhiteTitle']/value"/>
		<xsl:param name="BlackTitle" select="$head/tag[key='BlackTitle']/value"/>			
		<xsl:param name="WhiteELO" select="$head/tag[key='WhiteELO']/value"/>
		<xsl:param name="BlackELO" select="$head/tag[key='BlackELO']/value"/>			
		<xsl:param name="Result" select="$head/tag[key='Result']/value"/>

		<xsl:param name="isWhite" select="string-length($White)&gt;0 and $White!='-'"/>
		<xsl:param name="isWhiteTitle" select="string-length($WhiteTitle)&gt;0 and $WhiteTitle!='-'"/>
		<xsl:param name="isWhiteELO" select="string-length($WhiteELO)&gt;0 and $WhiteELO!='-'"/>
		<xsl:param name="isBlack" select="string-length($Black)&gt;0 and $Black!='-'"/>
		<xsl:param name="isBlackTitle" select="string-length($BlackTitle)&gt;0 and $BlackTitle!='-'"/>
		<xsl:param name="isBlackELO" select="string-length($BlackELO)&gt;0 and $BlackELO!='-'"/>
		<xsl:param name="isResult" select="string-length($Result)&gt;0 and $Result!='-' and $Result!='*'"/>

		<xsl:param name="white_style" select="$styles//style[name='header.white']"/>
		<xsl:param name="black_style" select="$styles//style[name='header.black']"/>
		<xsl:param name="result_style" select="$styles//style[name='header.result']"/>
		<!-- other elements inherit style from header -->
		<xsl:param name="white_color" select="$white_style/a[key='foreground']/value"/>
		<xsl:param name="black_color" select="$black_style/a[key='foreground']/value"/>
		<xsl:param name="result_color" select="$result_style/a[key='foreground']/value"/>

		<xsl:if test="$isWhite or $isBlack or $isResult">
			<fo:block>
				<!-- White -->
				<xsl:if test="$isWhite">
					<fo:inline>
						<!-- font attributes -->
						<xsl:attribute name="font-family"><xsl:value-of select="$white_style/a[key='family']/value"/></xsl:attribute>
						<xsl:attribute name="font-size"><xsl:value-of select="$white_style/a[key='size']/value"/>pt</xsl:attribute>
						<xsl:choose>
							<xsl:when test="string-length($white_color)=0"><xsl:attribute name="color">black</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="color"><xsl:value-of select="$white_color"/></xsl:attribute></xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$white_style/a[key='bold']/value='true'"><xsl:attribute name="font-weight">bold</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="font-weight">normal</xsl:attribute></xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$white_style/a[key='italic']/value='true'"><xsl:attribute name="font-style">italic</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="font-style">normal</xsl:attribute></xsl:otherwise>
						</xsl:choose>
					
						<xsl:value-of select="$White"/>
						
						<xsl:choose>
							<xsl:when test="$isBlack">
								<xsl:text> - </xsl:text>
							</xsl:when>
							<xsl:when test="$isResult">
								<xsl:text> </xsl:text>
							</xsl:when>
						</xsl:choose>
					</fo:inline>
				</xsl:if>
				
				<!-- Black -->
				<xsl:if test="$isBlack">
					<fo:inline keep-with-next.within-line="always">
						<!-- font attributes -->
						<xsl:attribute name="font-family"><xsl:value-of select="$black_style/a[key='family']/value"/></xsl:attribute>
						<xsl:attribute name="font-size"><xsl:value-of select="$black_style/a[key='size']/value"/>pt</xsl:attribute>
						<xsl:choose>
							<xsl:when test="string-length($black_color)=0"><xsl:attribute name="color">black</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="color"><xsl:value-of select="$black_color"/></xsl:attribute></xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$black_style/a[key='bold']/value='true'"><xsl:attribute name="font-weight">bold</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="font-weight">normal</xsl:attribute></xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$black_style/a[key='italic']/value='true'"><xsl:attribute name="font-style">italic</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="font-style">normal</xsl:attribute></xsl:otherwise>
						</xsl:choose>
					
						<xsl:value-of select="$Black"/>
						
						<xsl:if test="$isResult">
							<xsl:text> </xsl:text>
						</xsl:if>
					</fo:inline>
				</xsl:if>
				
				<!-- Result -->
				<xsl:if test="$isResult">
					<fo:inline>
						<!-- font attributes -->
						<xsl:attribute name="font-family"><xsl:value-of select="$result_style/a[key='family']/value"/></xsl:attribute>
						<xsl:attribute name="font-size"><xsl:value-of select="$result_style/a[key='size']/value"/>pt</xsl:attribute>
						<xsl:choose>
							<xsl:when test="string-length($result_color)=0"><xsl:attribute name="color">black</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="color"><xsl:value-of select="$result_color"/></xsl:attribute></xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$result_style/a[key='bold']/value='true'"><xsl:attribute name="font-weight">bold</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="font-weight">normal</xsl:attribute></xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$result_style/a[key='italic']/value='true'"><xsl:attribute name="font-style">italic</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="font-style">normal</xsl:attribute></xsl:otherwise>
						</xsl:choose>
					
						<xsl:value-of select="$Result"/>
					</fo:inline>
				</xsl:if>
			
			</fo:block>
		</xsl:if>		
	</xsl:template>


	<!-- - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- Third and Fourth Line: Opening, Annotator ...          -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - -->
	<xsl:template name="third-header-line">
		<xsl:param name="head"/>
	
		<xsl:param name="ECO" select="$head/tag[key='ECO']/value"/>
		<xsl:param name="Opening" select="$head/tag[key='Opening']/value"/>
		<xsl:param name="Annotator" select="$head/tag[key='Annotator']/value"/>

		<xsl:param name="isECO" select="string-length($ECO)&gt;0 and $ECO!='-'"/>
		<xsl:param name="isOpening" select="string-length($Opening)&gt;0 and $Opening!='-'"/>
		<xsl:param name="isAnnotator" select="string-length($Annotator)&gt;0 and $Annotator!='-'"/>

		<xsl:if test="$isECO or $isOpening">
			<fo:block>
				<!-- style inherited -->
				
				<xsl:value-of select="$ECO"/>
				<xsl:if test="$isECO and $isOpening">
					<xsl:text> </xsl:text>
				</xsl:if>
				<xsl:value-of select="$Opening"/>
			</fo:block>			
		</xsl:if>
		
		<xsl:if test="$isAnnotator">
			<fo:block>
				<xsl:value-of select="$head/tag[key='Annotator']/text"/>
				<xsl:text> </xsl:text>
				
				<xsl:value-of select="$Annotator"/>
			</fo:block>
		</xsl:if>
		
	</xsl:template>

</xsl:stylesheet>
