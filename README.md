# hamapAsSparqlSupplements

These are the supllements for the Hamap as SPARQL preprint in code form.

There is a function for use with Apache Jena that is more efficient 
than using the REGEX approach.

There is also an XSLT used to convert the output from InterPro Scan
to RDF so that it can be used on your own data with HAMAP as SPARQL.

## basic annotation system


Get a command line sparql engine using [Apache Jena](https://jena.apache.org) runing on java.


```bash
wget "http://www.pirbot.com/mirrors/apache/jena/binaries/apache-jena-3.12.0.tar.gz"
tar xzvf apache-jena-3.12.0.tar.gz
export JENA_HOME=$(pwd)/apache-jena-3.12.0
export PATH=$JENA_HOME/bin/:$PATH
```

Make sure to turn the interpro results into RDF

```bash
./interproscan.sh -dp -appl hamap "$YOUR_SEQ"
xsltproc to_rdf.xslt “$IP_OUT” > "$INPUT_FOR_HAMAP"
```


Get the latest rules

```bash
wget "ftp://ftp.expasy.org/databases/hamap/hamap_sparql.tar.gz"
tar -xzvf hamap_sparql.tar.gz
```

```
while read -r rule in
do
  ./bin/sparql --data "$INPUT_FOR_HAMAP" --query $rule
done < sparql/hamap.simple
```