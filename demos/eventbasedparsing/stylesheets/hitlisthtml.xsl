<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:biojava="http://www.biojava.org"
		version="1.0">
<xsl:output method="html"/>
		
    <xsl:template match="/">
	Results of Parsing
		<xsl:for-each select="//biojava:HitSummary/biojava:HitId">
		<xsl:value-of select="@id"/>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="*"/>

</xsl:stylesheet>