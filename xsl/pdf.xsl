<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
	xmlns:fox="http://xml.apache.org/fop/extensions"
	exclude-result-prefixes="fo">
	
	<xsl:import href="pdf_header.xsl"/>
	<xsl:output method="xml" encoding="UTF-8" indent="no"/>

	<jose:export xmlns:jose="http://jose-chess.sourceforge.net/xsl-info">
		<jose:title>xsl.pdf</jose:title>
		<jose:input>list-of-games</jose:input>
		<jose:output>xsl-fo</jose:output>
	</jose:export>
		
	<xsl:param name="styles" select="//styles"/>
	<xsl:param name="inline" select="//figurines/inline"/>
	
	<!-- - - - - - - - - - - - - -->
	<!-- Global Layout   -->
	<!-- - - - - - - - - - - - - -->
	<xsl:template match="/">
		<xsl:param name="pgno_style" select="//style[name='body.line.0']"/>
	
		<xsl:param name="header_extent"><xsl:text>0.5cm</xsl:text></xsl:param>
		<xsl:param name="footer_extent"><xsl:text>1.1cm</xsl:text></xsl:param>

		<xsl:param name="column-count">2</xsl:param>

		<xsl:param name="language">de</xsl:param>

		<fo:root>
			<fo:layout-master-set>
				<fo:simple-page-master master-name="content-page">
					<xsl:attribute name="page-width"><xsl:value-of select="//page/width"/>pt</xsl:attribute>
					<xsl:attribute name="page-height"><xsl:value-of select="//page/height"/>pt</xsl:attribute>
					<xsl:attribute name="margin-top"><xsl:value-of select="//page/margin-top"/>pt</xsl:attribute>
					<xsl:attribute name="margin-bottom"><xsl:value-of select="//page/margin-bottom"/>pt</xsl:attribute>
					<xsl:attribute name="margin-left"><xsl:value-of select="//page/margin-left"/>pt</xsl:attribute>
					<xsl:attribute name="margin-right"><xsl:value-of select="//page/margin-right"/>pt</xsl:attribute>
					<fo:region-before>
						<xsl:attribute name="extent"><xsl:value-of select="$header_extent"/></xsl:attribute>
					</fo:region-before>
					<fo:region-after>
						<xsl:attribute name="extent"><xsl:value-of select="$footer_extent"/></xsl:attribute>
					</fo:region-after>
					
					<fo:region-body 
						column-gap="10pt">
						<xsl:attribute name="column-count"><xsl:value-of select="$column-count"/></xsl:attribute>
						<xsl:attribute name="margin-top"><xsl:value-of select="$header_extent"/></xsl:attribute>
						<xsl:attribute name="margin-bottom"><xsl:value-of select="$footer_extent"/></xsl:attribute>
					</fo:region-body>
						
				</fo:simple-page-master>
				<fo:page-sequence-master master-name="content-sequence">
					<fo:repeatable-page-master-reference master-reference="content-page"/>
				</fo:page-sequence-master>
			</fo:layout-master-set>

			<!-- Index -->
			<xsl:if test="//bookmarks='true'">
				<xsl:for-each select="//game">
				<fox:outline>
					<xsl:attribute name="internal-destination">game-<xsl:value-of select="position()"/></xsl:attribute>
					<fox:label>
						<xsl:value-of select="head/tag[key='White']/value"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="head/tag[key='Black']/value"/>
						<xsl:text>, </xsl:text>
						<xsl:value-of select="head/tag[key='Site']/value"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="head/tag[key='Date']/value"/>
					</fox:label>
				</fox:outline>
				</xsl:for-each>
			</xsl:if>

			<fo:page-sequence master-reference="content-sequence">
				<!-- Page Header  -->
				<fo:static-content flow-name="xsl-region-before">
					<fo:block text-align="center" padding-top="-4pt" line-height="4pt">
						<fo:leader leader-pattern="rule"
								rule-thickness="0.5pt"
								leader-alignment="page"
								leader-length="100%"/>
					</fo:block>
				</fo:static-content>
				
				<!-- Page Footer -->
				<fo:static-content flow-name="xsl-region-after">
					<fo:block>
						<fo:leader leader-pattern="rule" rule-thickness="0.5pt"
							leader-alignment="page">
							<xsl:attribute name="leader-length">100%</xsl:attribute>
						</fo:leader>
					</fo:block>

					<fo:block text-align="end">
						<xsl:attribute name="font-family"><xsl:value-of select="$pgno_style/a[key='family']/value"/></xsl:attribute>
						<xsl:attribute name="font-size"><xsl:value-of select="$pgno_style/a[key='size']/value"/>pt</xsl:attribute>

						<fo:page-number/>
					</fo:block>
				</fo:static-content>
				
				<!-- Page Body -->
				<fo:flow flow-name="xsl-region-body" 
					language="de"
					hyphenate="true"
					hyphenation-push-character-count="2"
					hyphenation-remain-character-count="2"
					overflow="visible">
					<xsl:attribute name="language"><xsl:value-of select="$language"/></xsl:attribute>

					<xsl:apply-templates select="//game"/>

					<fo:block text-align="center" vertical-align="bottom">
						<xsl:attribute name="font-family"><xsl:value-of select="$styles//style[name='body.comment.4']/a[key='family']/value"/></xsl:attribute>
						<xsl:attribute name="font-size"><xsl:value-of select="$styles//style[name='body.comment.4']/a[key='size']/value"/>pt</xsl:attribute>
						<xsl:attribute name="color"><xsl:value-of select="$styles//style[name='body.comment.4']/a[key='foreground']/value"/></xsl:attribute>
			
			                  			created with jose-chess.sourceforge.net
					</fo:block>

				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>
	
	<xsl:template match="game">
		<fo:block>
			<xsl:attribute name="id">game-<xsl:value-of select="position()"/></xsl:attribute>

			<xsl:apply-templates select="head"/>
			<xsl:apply-templates select="diagram"/>
			<xsl:apply-templates select="body"/>
		</fo:block>
	</xsl:template>

	<!-- - - - - - - - - - - - - - - - - -->
	<!--   Game Body             -->
	<!-- - - - - - - - - - - - - - - - - -->

	<xsl:template match="body">
		<fo:block text-align="justify" padding-after="24pt">
			<xsl:apply-templates/>
		</fo:block>

		<!--fo:block text-align="center"
			padding-after="12pt">
			<fo:leader leader-pattern="rule" 
					leader-alignment="reference-area" 
					text-align="center"
					leader-length.optimum="30%"/>
		</fo:block-->
	</xsl:template>

	<!-- Variation Line -->
	<xsl:template match="v">
		<xsl:param name="style_name" select="concat('body.line.', depth)"/>
		<xsl:param name="style" select="$styles//style[name=$style_name]"/>
		<xsl:param name="color" select="$style/a[key='foreground']/value"/>

		<xsl:param name="prefix" select="$style/a[key='variation.prefix']/value"/>
		<xsl:param name="suffix" select="$style/a[key='variation.suffix']/value"/>


		<xsl:choose>
			<xsl:when test="depth = 0">
				<!-- variation on new line -->
				<fo:block
				 	padding-top="10pt"
				 	padding-bottom="10pt">
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
		
					<xsl:if test="string-length($prefix) > 0">
						<fo:inline> <xsl:value-of select="$prefix"/> </fo:inline>
						<!-- keep-with-next.within-line="always" not understood by FOP -->
					</xsl:if>
					
					<xsl:apply-templates/>
					
					<xsl:if test="string-length($suffix) > 0">
						<fo:inline> <xsl:value-of select="$suffix"/> </fo:inline>
						<!-- keep-with-next.within-line="always" not understood by FOP -->
					</xsl:if>
				</fo:block>
			</xsl:when>
			
			<xsl:when test="depth = 1">
				<!-- variation on new line -->
				<fo:block
					margin-left="12pt"
				 	padding-top="10pt"
				 	padding-bottom="10pt">
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
		
					<xsl:if test="string-length($prefix) > 0">
						<fo:inline> <xsl:value-of select="$prefix"/> </fo:inline>
						<!--  keep-with-next.within-line="always" not understood by FOP -->
					</xsl:if>
					
					<xsl:apply-templates/>
					
					<xsl:if test="string-length($suffix) > 0">
						<fo:inline> <xsl:value-of select="$suffix"/> </fo:inline>
						<!--  keep-with-next.within-line="always" not understood by FOP -->
					</xsl:if>
				</fo:block>
			</xsl:when>


			<xsl:otherwise>
				<!-- variation in line -->
				<fo:inline>
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
		
					<xsl:if test="string-length($prefix) > 0">
						<fo:inline> <xsl:value-of select="$prefix"/> </fo:inline>
					</xsl:if>
					
					<xsl:apply-templates/>
					
					<xsl:if test="string-length($suffix) > 0">
						<fo:inline> <xsl:value-of select="$suffix"/> </fo:inline>
					</xsl:if>
				</fo:inline>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- hide depth -->
	<xsl:template match="depth"></xsl:template>

	<!-- Move -->
	<xsl:template match="m">
		<fo:inline> <!-- hyphenate="false" keep-together.within-line="always"  ignored by FOP -->
			<xsl:text> </xsl:text><xsl:apply-templates/><xsl:text> </xsl:text>
		</fo:inline>
	</xsl:template>

	<!-- - - - - - - - - - - - - - - - - -->
	<!--   Comment                -->
	<!-- - - - - - - - - - - - - - - - - -->
	<xsl:template match="c">
		<xsl:param name="style_name" select="concat('body.comment.',../depth)"/>
		<xsl:param name="style" select="$styles//style[name=$style_name]"/>
		<xsl:param name="color" select="$style/a[key='foreground']/value"/>

		<fo:inline>
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

			<xsl:text> </xsl:text><xsl:apply-templates/><xsl:text> </xsl:text>
		</fo:inline>
	</xsl:template>

	
	<xsl:template match="bold">
		<fo:inline font-weight="bold"><xsl:apply-templates/></fo:inline>
	</xsl:template>
	<xsl:template match="italic">
		<fo:inline font-style="italic"><xsl:apply-templates/></fo:inline>
	</xsl:template>
	<xsl:template match="underline">
		<fo:inline text-decoration="underline"><xsl:apply-templates/></fo:inline>
	</xsl:template>
	<xsl:template match="center">
		<fo:block text-align="center"><xsl:apply-templates/></fo:block>
	</xsl:template>
	<xsl:template match="right">
		<fo:block text-align="right"><xsl:apply-templates/></fo:block>
	</xsl:template>
	<xsl:template match="font">
		<fo:inline>
			<xsl:if test="@size">
				<xsl:variable name="percent" select="number(substring(@size,1,string-length(@size)-1))"/>
				<xsl:attribute name="font-size"><xsl:value-of select="round(100+$percent)"/>%</xsl:attribute>
			</xsl:if>
			<xsl:if test="@color">
				<xsl:attribute name="color"><xsl:value-of select="@color"/></xsl:attribute>
			</xsl:if>
	            <xsl:if test="@face">
	                <xsl:attribute name="font-family"><xsl:value-of select="@face"/></xsl:attribute>
	            </xsl:if>
			<xsl:apply-templates/>
		</fo:inline>	
	</xsl:template>
	
	<xsl:template match="br">
		<fo:block> </fo:block>
		<!-- unfortunately, this doesn't work for /consecutive/ linebreaks -->
		<!-- adding 'space-after' doesn't work either, cause it messes up /single/ line breaks -->
		<!-- bugger .... -->
	</xsl:template>



	<!-- - - - - - - - - - - - - - - - - -->
	<!-- Annotation                      -->
	<!-- - - - - - - - - - - - - - - - - -->
	<xsl:template match="a">
		<xsl:choose>
			<xsl:when test="count(sym)&gt;0">
				<xsl:call-template name="sym-annotation">
					<xsl:with-param name="sym" select="sym"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<!-- plain text -->
				<xsl:value-of select="text"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="sym-annotation">
		<xsl:param name="sym"/>

		<xsl:param name="style" select="$styles//style[name='body.symbol']"/>
		<xsl:param name="color" select="$style/a[key='foreground']/value"/>

		<fo:inline>
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

			<xsl:value-of select="$sym"/>
		</fo:inline>

	</xsl:template>

	<!-- - - - - - - - - - - - - - - - - -->
	<!-- Figurines                   -->
	<!-- - - - - - - - - - - - - - - - - -->
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

		<xsl:param name="style_name" select="concat('body.figurine.',$d)"/>
		<xsl:param name="style" select="$styles//style[name=$style_name]"/>
		<xsl:param name="color" select="$style/a[key='foreground']/value"/>

		<!-- text figurine -->
		<fo:inline>
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

			<xsl:value-of select="$fig/string[key=$f]/value"/>
		</fo:inline>
	</xsl:template>
	
	<!-- Annotation -->
	<xsl:template match="a">
		<!-- inherit style from v -->
		<xsl:value-of select="text"/>
	</xsl:template>

	<!-- Result -->
	<xsl:template match="result">
		<xsl:param name="style" select="$styles//style[name='body.result']"/>
		<xsl:param name="color" select="$style/a[key='foreground']/value"/>

		<xsl:if test=".!='*'">
			<fo:inline>
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

				<xsl:value-of select="."/>
			</fo:inline>
		</xsl:if>
	</xsl:template>

	<!-- hide fen -->
	<xsl:template match="fen"></xsl:template>
	<xsl:template match="depth"></xsl:template>

	<!-- - - - - - - - - - - - - - - - - -->
	<!-- Diagram                    -->
	<!-- - - - - - - - - - - - - - - - - -->
	<xsl:template match="diagram">
		<xsl:param name="style" select="$styles//style[name='body.inline']"/>
		<xsl:param name="color" select="$style/a[key='foreground']/value"/>

	<!-- FOP workaround: keep-together works only in table rows !!!! -->
	<!-- that's why another table is wrapped around -->
		<fo:table table-layout="fixed" width="100%">
		<fo:table-column/>
		<fo:table-body> <fo:table-row keep-together="always"><fo:table-cell>
		
		<fo:block space-before="-1em" space-after="-0.0em" text-align="center">
			<!-- font attributes -->
			<xsl:attribute name="font-family"><xsl:value-of select="$style/a[key='family']/value"/></xsl:attribute>
			<xsl:attribute name="font-size"><xsl:value-of select="$style/a[key='size']/value"/>pt</xsl:attribute>
			<xsl:choose>
				<xsl:when test="string-length($color)=0"><xsl:attribute name="color">black</xsl:attribute></xsl:when>
				<xsl:otherwise><xsl:attribute name="color"><xsl:value-of select="$color"/></xsl:attribute></xsl:otherwise>
			</xsl:choose>
			<xsl:attribute name="font-weight">normal</xsl:attribute>
			<xsl:attribute name="font-style">normal</xsl:attribute>
			<!-- note that bold or italic would mess up character alignment;
				 that's why it is turned off completely (doesn't make much sense, anyway) -->

			<xsl:apply-templates select="table/tr"/>
		</fo:block>

		</fo:table-cell></fo:table-row></fo:table-body></fo:table>
	</xsl:template>

	<xsl:template match="tr">
		<fo:block line-height="1em">
			<!--xsl:attribute name="line-height"><xsl:value-of select="$inline/pt-size"/>pt</xsl:attribute-->
			<xsl:apply-templates select="td"/>
		</fo:block>
	</xsl:template>

	<xsl:template match="td">
		<xsl:choose>
			<xsl:when test="char=' '">
				<!-- TODO -->
				<!-- white space is not printed with exact width; that sucks ! -->
				<!--fo:character character=" " width="1em" /-->
				<!--fo:external-graphic src="url(file:///D:/jose/output/empty.gif)"
					width="1em" height="1em" alignment-baseline="baseline"/-->
				<fo:leader leader-length="1em">
                    <!--xsl:attribute name="leader-length"><xsl:value-of select="$inline/pt-size"/>pt</xsl:attribute-->
                </fo:leader>
			</xsl:when>
			<xsl:otherwise><xsl:value-of select="char"/></xsl:otherwise>
		</xsl:choose>		
	</xsl:template>

</xsl:stylesheet>
