<?xml version="1.0"?>
<!-- 

This is a basic XSLT that transforms the XSL
output by interpro into a basic RDF that can 
be used by HAMAP as SPARQL and other tools.

Important is that InterProScan needs to be run
with the -dp option. Other wise the alignment 
string is not available.



 -->
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:in="http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5" 
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"  
    xmlns:faldo="http://biohackathon.org/resource/faldo#" 
    xmlns:fn="http://www.w3.org/2005/xpath-functions" 
    xmlns:up="http://purl.uniprot.org/core/" 
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">
  <xsl:output method="text"/>
 
  <xsl:template match="/">
   
PREFIX ys:<http://example.org/yoursequence/>
PREFIX yr:<http://example.org/yourrecord/>
PREFIX up:&lt;http://purl.uniprot.org/core/&gt;
PREFIX rdf:&lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt;
PREFIX rdfs:&lt;http://www.w3.org/2000/01/rdf-schema#&gt;
PREFIX faldo:&lt;http://biohackathon.org/resource/faldo#&gt;
PREFIX signature:&lt;http://purl.uniprot.org/hamap/&gt;
PREFIX edam: &lt;http://edamontology.org/&gt;

    <xsl:apply-templates />
  </xsl:template>
    
  <xsl:template match="in:protein-matches">
    <xsl:for-each select="in:protein">
      <xsl:variable name="sequenceid" select="translate(in:xref/@id, '|', '%7C')"/>
      <xsl:variable name="sequencemd5" select="translate(in:sequence/@md5,'acbdef','ABCDEF')"/>

    <xsl:for-each select="in:matches/in:profilescan-match">
      <xsl:variable name="signatureid" select="in:signature/@ac"/>
      <xsl:variable name="start" select="in:locations/in:profilescan-location/@start"/>
      <xsl:variable name="end" select="in:locations/in:profilescan-location/@end"/>
yr:<xsl:value-of select="$sequenceid"/>;
  up:sequence ys:<xsl:value-of select="$sequenceid"/>-sequence; ;
  rdfs:seeAlso signature:<xsl:value-of select="$signatureid"/> .
  </xsl:for-each>
ys:<xsl:value-of select="$sequenceid"/>-sequence   
  rdf:hasValue "<xsl:value-of select="in:sequence"/>" .
  
    <xsl:for-each select="in:matches/in:profilescan-match">
      <xsl:variable name="signatureid" select="in:signature/@ac"/>
      <xsl:variable name="start" select="in:locations/in:profilescan-location/@start"/>
      <xsl:variable name="alignment" select="in:locations/in:profilescan-location/in:alignment/text()"/>
      <xsl:variable name="end" select="in:locations/in:profilescan-location/@end"/>
[] a edam:data_0869 ;
  <xsl:if test="$alignment != 'Not available'">
  rdf:value "<xsl:value-of select="$alignment" />" ;
  </xsl:if>
  edam:is_output_of [ 
    a edam:operation_0300 ;
    edam:has_input signature:<xsl:value-of select="$signatureid"/>
  ] ;
  faldo:begin [ faldo:position <xsl:value-of select="$start" />] ;
  faldo:end   [ faldo:position <xsl:value-of select="$end" /> ] .
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
