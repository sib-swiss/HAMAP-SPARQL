# InterPro2GO as SPARQL rules

[InterPro2GO](https://www.ebi.ac.uk/GOA/InterPro2GO) is a mapping of InterPro to GO terms, that can be retrieved from [the go consortium website](http://current.geneontology.org/ontology/external2go/interpro2go)

The rules are if InterPro:A matches add GO:B .

```
shared_prefixes='PREFIX InterPro:<http://purl.uniprot.org/interpro/> PREFIX GO:<http://purl.obolibrary.org/obo/GO_> PREFIX up:<http://purl.uniprot.org/core/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>'
wget "http://www.geneontology.org/external2go/interpro2go" -O - | \
  grep -P "$Interpro:" | \
    awk "{print \"$shared_prefixes CONSTRUCT {?this up:classifiedWith \"\$(NF)\" } WHERE {?this rdfs:seeAlso \"\$1\"}\" }" | \
  split -l 1 /dev/stdin interpro2go_sparql_
```

This generates thousands of sparql queries one per file. That look like

```sparql
PREFIX InterPro:<http://purl.uniprot.org/interpro/>
PREFIX GO:<http://purl.obolibrary.org/obo/GO_>
PREFIX up:<http://purl.uniprot.org/core/>
PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>

CONSTRUCT {
  ?this up:classifiedWith GO:0035102
} WHERE {
  ?this rdfs:seeAlso InterPro:IPR043000
}
```

To improve the construct queries to include evidence tags use the following awk instead

```awk
#!/usr/bin/awk -f
{
  if ( $1 ~ /^InterPro:/ ) {
    print "PREFIX InterPro:<http://purl.uniprot.org/interpro/>" > $1"_"$(NF)".sparql"
    print "PREFIX GO:<http://purl.obolibrary.org/obo/GO_>" >> $1"_"$(NF)".sparql"
    print "PREFIX up:<http://purl.uniprot.org/core/>" >> $1"_"$(NF)".sparql"
    print "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" >> $1"_"$(NF)".sparql"
    print "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" >> $1"_"$(NF)".sparql"
    print "PREFIX ECO:<http://purl.obolibrary.org/obo/ECO_>" >> $1"_"$(NF)".sparql"
    print "CONSTRUCT {?this up:classifiedWith "$(NF)" . " >> $1"_"$(NF)".sparql"
    print "[ rdf:subject ?this ; rdf:predicate up:classifiedWith ;"  >> $1"_"$(NF)".sparql"
    print "  rdf:object "$(NF)" ;" >> $1"_"$(NF)".sparql"
    print "  up:attribution [ " >> $1"_"$(NF)".sparql"
    print "    up:source <http://www.geneontology.org/external2go/interpro2go> ;"  >> $1"_"$(NF)".sparql"
    print "    up:eco ECO:0000501 ;"  >> $1"_"$(NF)".sparql"
    print "  ]]" >> $1"_"$(NF)".sparql"
    print "} WHERE {?this rdfs:seeAlso "$1"}" >> $1"_"$(NF)".sparql"
  }
}
```
If you save the file as './convertInterpro2Go.aw' and make it executable then this will generate one file per
interpro2go combination.
```bash
 wget "http://www.geneontology.org/external2go/interpro2go" -O - | ./convertInterpro2Go.awk 
```
