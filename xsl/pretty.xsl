<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="text" indent="yes"/>
	<xsl:strip-space elements="*"/>
	
	<xsl:template match="*">
   <!-- element transform rules -->
   <!-- o The document element should not be preceded by a newline or indented.
        o Any element not preceded by a text node should start on a new line, and should
          be indented.
        o Any element that IS a preceded by a text node should appear on the same line as the text.
        o The end tag should not be followed by a newline.  Following text content will appear
          on the same line as the end tag, and following elements will handle breaking to a new
          line themselves.
   -->
   <!-- should the opening tag be preceded by a newline?  Not if it's the document element, and
        not if it's preceded by a text node -->
   <xsl:if test="parent::* and not(preceding-sibling::node()[1][self::text()])">
      <xsl:text>&#10;</xsl:text>
   </xsl:if>
   <!-- should the opening tag be indented?  Only if it's not preceded by a text node. -->
   <xsl:if test="not(preceding-sibling::node()[1][self::text()])">
      <xsl:for-each select="ancestor::*">
	      <!-- insert one tab for each ancestor of the current element -->
			   <xsl:text>&#09;</xsl:text>
      </xsl:for-each>
   </xsl:if>
   <!-- insert the opening of the element's tag followed by any attributes -->
   <xsl:text>&lt;</xsl:text>
   <xsl:value-of select="name()"/>
   <xsl:apply-templates select="@*"/>
   <!-- now decide how to handle child nodes:
      o If there are no child nodes, just close the tag, e.g. "<foo/>".
      o If child nodes exist, apply templates to them all.  The template rules will handle
        their formatting.  
      o Once they're done insert the end tag.
   -->
   <xsl:choose>
      <xsl:when test="not(node())">
         <!-- this is the simplest case -->
         <xsl:text>/&gt;</xsl:text>
      </xsl:when>
      <xsl:otherwise>
         <!-- element has children, so close the tag and use templates to transform them -->
         <xsl:text>&gt;</xsl:text>
         <xsl:apply-templates select="node()"/>
         <!-- put in the closing tag for the element.  If the LAST child node is not text, 
              insert a newline and indent before putting in the closing tag.				
         -->
         <xsl:if test="not(child::node()[last()][self::text()])">
            <xsl:text>&#10;</xsl:text>
            <xsl:for-each select="ancestor::*">
               <!-- insert a tab for each ancestor of the current element -->
               <xsl:text>&#09;</xsl:text>
            </xsl:for-each>
         </xsl:if>
         <!-- here's the closing tag -->
         <xsl:text>&lt;/</xsl:text>
         <xsl:value-of select="name()"/>
         <xsl:text>&gt;</xsl:text>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>
	
</xsl:stylesheet>
