<?xml version="1.0"?> 
<!-- copies the input 1:1 to the output, 
 i.e. you get an exact XML description of the DOM document 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml" encoding="UTF-8"/>

	<jose:export xmlns:jose="http://jose-chess.sourceforge.net/xsl-info">
		<jose:title>xsl.debug</jose:title>
		<jose:output>xml</jose:output>

		<!-- include FENs for every move -->
		<jose:param>
			<key>move-fen</key>
			<value>true</value>
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

		<!-- experimental -->
		<jose:param>
			<jose:key>pairing-list</jose:key>
			<jose:value>true</jose:value>
		</jose:param>
		<jose:param>
			<jose:key>cross-table</jose:key>
			<jose:value>true</jose:value>
		</jose:param>
	</jose:export>

	<xsl:template match="/">
		<xsl:copy-of select="."/>
	</xsl:template>
</xsl:stylesheet>
