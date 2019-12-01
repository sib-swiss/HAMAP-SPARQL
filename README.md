# What is HAMAP?

[HAMAP](https://hamap.expasy.org) is a system for the classification and annotation of protein sequences. 
It consists of a collection of expert-curated protein family signatures and rules that specify annotations that apply to family members.
HAMAP was originally developed by the [Swiss-Prot group](https://www.sib.swiss/alan-bridge-group) to help curators annotate UniProtKB/Swiss-Prot records and was subsequently made available to external users via the [HAMAP-Scan web service](https://hamap.expasy.org/hamap_scan.html). HAMAP signatures are also integrated into [InterPro](http://www.ebi.ac.uk/interpro) and the HAMAP rules into [UniRule](https://www.uniprot.org/help/unirule), the core part of the [UniProt automatic annotation pipeline](https://www.uniprot.org/help/automatic_annotation).

# Why SPARQL?

Our internal implementation of HAMAP uses a custom rule format and annotation engine that are not easy to integrate into other groups' pipelines. The HAMAP-Scan web service is an option for small research projects, but not all projects can depend on external web services to process their data.

Our goal was to develop a generic HAMAP rule format and annotation engine that is easily portable, using standard technologies that developers of other genome annotation pipelines could also adopt. To achieve this we have developed a representation of HAMAP annotation rules using the World Wide Web Consortium (W3C) standard SPARQL 1.1 syntax. [SPARQL](https://en.wikipedia.org/wiki/SPARQL) is a query language for [RDF](https://en.wikipedia.org/wiki/Resource_Description_Framework), a core Semantic Web technology from the W3C. There are many freely available SPARQL engines available that can be used to annotate protein sequences expressed as RDF with HAMAP rules in SPARQL syntax.

# How can I use it?

Please try this [tutorial](Tutorial.md) to learn how to use a SPARQL engine to annotate protein sequences with HAMAP rules in SPARQL syntax.


