<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:copyall="http://edu.mayo.mir/xst/copyall"
		version="1.0">
		
	
<xsl:template name="copyall:copy-content">
	<xsl:apply-templates select="@*|processing-instruction()|comment()|*|text()"/>
</xsl:template>

<xsl:template match="/|processing-instruction()|comment()|*|@*|text()">
	<xsl:copy>
		<xsl:call-template name="copyall:copy-content"/>
	</xsl:copy>
</xsl:template>

</xsl:stylesheet>