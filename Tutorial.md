# HAMAP as SPARQL tutorial

This tutorial was tested on CentOS 7 and should also work on recent Fedora.


## Get the protein sequences and HAMAP signatures

The [HAMAP](https://hamap.expasy.org) system classifies and annotates protein sequences using a collection of expert-curated protein family signatures and annotation rules. We describe here two methods to scan protein sequences with the HAMAP signatures and both require the sequences in [FASTA format](https://en.wikipedia.org/wiki/FASTA_format). 

For this tutorial we will use the sequences of the _E. coli_ proteome [UP000069664](https://www.uniprot.org/proteomes/UP000069664) that you can download in FASTA format from [UniParc](https://www.uniprot.org/uniparc/):

```
wget "https://www.uniprot.org/uniparc/?query=proteome:UP000069664&format=fasta" \
  -O TEST_SEQUENCES.fasta
```

Now download the HAMAP signatures:

```
wget "https://ftp.expasy.org/databases/hamap/hamap.prf.gz"
gunzip hamap.prf.gz
```

## Scan the protein sequences with the HAMAP signatures

The [Swiss-Prot group](https://www.sib.swiss/alan-bridge-group) uses the program `pfscanV3` to scan its sequences with the HAMAP signatures, but the more widely used `InterProScan` will give you nearly identical results. Choose one of the two methods below.

### Method 1: PfTools v3.2

* Download the [PfTools v3.2](https://github.com/sib-swiss/pftools3/blob/master/README.md) code from GitHub and follow the instructions in the [INSTALL](https://github.com/sib-swiss/pftools3/blob/master/INSTALL) file to compile the code, e.g.:
  ```
  git clone https://github.com/sib-swiss/pftools3.git
  cd pftools3
  mkdir build
  cd build
  cmake -DUSE_32BIT_INTEGER=ON -DC_ONLY=ON -DUSE_PCRE=OFF ..
  make
  cd ../..
  pftools3/build/src/C/pfscanV3 --help
   ...
   --fasta                            [-f] : FASTA file database as input
   ...
   --output-method <uint>     [-o] : printing output method (default 5)
   ...
                                     == 10 Turtle/RDF output
  ```

* Scan the FASTA file with the HAMAP signatures, using the -o10 option for the Turtle output format:
  ```
  pftools3/build/src/C/pfscanV3 hamap.prf -f TEST_SEQUENCES.fasta -o10 > TEST_SCAN.ttl
  ```

### Method 2: InterProScan

* Download the [InterProScan](https://github.com/ebi-pf-team/interproscan/wiki) package.
  This will take a while because the package is very big!
  ```
  wget "ftp://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/5.39-77.0/interproscan-5.39-77.0-64-bit.tar.gz"
  tar -xvzf interproscan-5.39-77.0-64-bit.tar.gz
  ```

* Scan the FASTA file with the HAMAP signatures:
  ```
  interproscan-5.39-77.0/interproscan.sh --disable-precalc --applications hamap \
  --formats XML --input TEST_SEQUENCES.fasta --outfile TEST_SCAN.xml
  ```

* Download the XSLT stylesheet [`interproToRdf.xslt`](src/main/xlst/interproToRdf.xslt) and use it to convert the InterPro XML result file to Turtle format:
  ```
  xsltproc interproToRdf.xslt TEST_SCAN.xml > TEST_SCAN.ttl
  ```

## Convert the FASTA files to RDF Turtle format

You need to convert each FASTA record to one RDF statement of the following format:

```
ys:IDENTIFIER rdf:value "SEQUENCE" .
```

First define the namespaces that we will use in the RDF statements:

```
printf "PREFIX ys:<http://example.org/yoursequence/>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" \
> TEST_SEQUENCES.ttl
```

Now execute the following command to convert the FASTA file to RDF turtle format:
```
cat TEST_SEQUENCES.fasta \
  | awk '/^>/ {printf("\" . \nys:%s rdf:value \"", substr($1,2)); next; } \
              {printf("%s", $0)} END {printf("\" .\n")}' \
  | grep -v -P '^"' >> TEST_SEQUENCES.ttl
```

The result file `TEST_SEQUENCES.ttl` should now look similar to this:

```
PREFIX ys:<http://example.org/yoursequence/>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
ys:UPI0000000053 rdf:value "MTNLKAVIPV...KGIEKLLSE" . 
ys:UPI0000000054 rdf:value "MAVTNVAELN...AEKKAKKSA" . 
...
```

Note:
The FASTA header of our test data has this simple format:
```
>IDENTIFIER some_optional_stuff
```
If you want to process FASTA files that have a more complex header,
you must adapt the conversion command above to extract your sequence identifier correctly.
Pay special attention to '|' and ':' characters.
Please check also that the sequence strings do not contain any whitespace characters.


## Generate organism information

HAMAP needs to know the [NCBI taxonomy identifier](https://www.ncbi.nlm.nih.gov/taxonomy)
of the organism from which the sequences come.
Our _E. coli_ proteome [UP000069664](https://www.uniprot.org/proteomes/UP000069664) has the taxonomy identifier [511145](https://www.uniprot.org/taxonomy/511145).
If you try this tutorial with sequences from a different organism, please [lookup the NCBI taxonomy identifier](https://www.uniprot.org/taxonomy/) of that organism.
If you cannot find an identifier for your species, use instead the identfier of its genus or a closely related species. Then replace the number '511145' in the code examples below with the NCBI taxonomy identifier you found.

* First define the namespaces that we will use in the RDF statements:
  ```
  printf "PREFIX up:<http://purl.uniprot.org/core/>
  PREFIX taxon:<http://purl.uniprot.org/taxonomy/>
  PREFIX yr:<http://example.org/yourrecord/>\n" \
  > TEST_ORGANISM.ttl
  ```

* Then append the full taxonomic lineage for the organism in RDF.
  You can retrieve this data from the [UniProt](https://www.uniprot.org/taxonomy/) website:
  ```
  wget "http://www.uniprot.org/taxonomy/?query=id:511145&include=yes&format=ttl" -O - >> TEST_ORGANISM.ttl
  ```

* Now you must add one RDF statement of the following format for each of the sequence records:
  ```
  yr:IDENTIFIER up:organism taxon:511145 .
  ```
  You can use the previously generate `TEST_SEQUENCES.ttl` file to do this:
  ```
  grep -oP "^ys:\w+" TEST_SEQUENCES.ttl | cut -c 4- \
    | awk '{print "yr:"$1" up:organism taxon:511145 ."}' >> TEST_ORGANISM.ttl
  ```

The result file `TEST_ORGANISM.ttl` should now look similar to this:

```
PREFIX up:<http://purl.uniprot.org/core/>
PREFIX taxon:<http://purl.uniprot.org/taxonomy/>
PREFIX yr:<http://example.org/yourrecord/>
... taxonomy data retrieved from uniprot.org here ...
yr:UPI0000000053 up:organism taxon:511145 .
yr:UPI0000000054 up:organism taxon:511145 .
...
```

## Get the SPARQL representation of the HAMAP rules

```
wget "https://ftp.expasy.org/databases/hamap/hamap_sparql.tar.gz"
tar -xzvf hamap_sparql.tar.gz
```

This distribution contains several types of SPARQL rules:

* Rules that generate all annotation types in [UniProt RDF format](https://sparql.uniprot.org/uniprot):

  * hamap.sparql  
    SPARQL syntax with CONSTRUCT clause to generate RDF statements.
  * hamap.sparul  
    SPARQL syntax with INSERT clause for writing results to a triplestore.
  * hamap.visql  
    SPARQL syntax with INSERT clause tuned for writing results to [Virtuoso](http://vos.openlinksw.com/owiki/wiki/VOS) v7.2.
  * hamap.ttl  
    [SPIN](https://spinrdf.org/) syntax.

* Rules that generate only a limited set of key annotation types
  ([GO terms](http://geneontology.org/),
  [EC numbers](https://en.wikipedia.org/wiki/Enzyme_Commission_number),
  [UniProt keywords](https://www.uniprot.org/keywords/))
  in tabular format:

  * hamap.simple  
    SPARQL syntax with SELECT clause.
  * hamap.orsql  
    For use with [Oracle Spatial and Graph](https://www.oracle.com/database/technologies/spatialandgraph.html).

It also includes the file `template_matches.ttl` that stores the result of a scan of 
the Swiss-Prot entries that are used to curate the HAMAP rules (so called 'template entries')
with the HAMAP signatures.
The alignment of these Swiss-Prot sequences to HAMAP signatures is required
to 'transfer' sequence features like active sites, etc.
from the Swiss-Prot template entries to the query sequences that match a HAMAP signature.


## Combine all data into one file

Now you have all the data that you need in RDF Turtle format.

Concatenate the results of the scan, the sequences and the organism information, as well as the HAMAP template matches.

```
cat TEST_SCAN.ttl \
    TEST_SEQUENCES.ttl  \
    TEST_ORGANISM.ttl \
    sparql/template_matches.ttl > TEST_DATA.ttl
```

## Use a SPARQL engine to apply the rules

There are many [triplestores](https://en.wikipedia.org/wiki/Comparison_of_triplestores) available that you can use to execute SPARQL queries. For this tutorial, we describe two that are easy to install. To process large amounts of data, other products may have better performance.

### Option 1: In-memory RDF store

An in-memory RDF store is generally simple to use, but not optimized for speed and large amounts of data.
It is not recommended for a production service, but can be useful for testing.
We use here the in-memory store of the [Apache Jena](https://jena.apache.org/about_jena/)
project to illustrate how you can annotate sequences with SPARQL rules.

* Download Apache Jena:
  ```
  wget "http://mirror.easyname.ch/apache/jena/binaries/apache-jena-3.13.1.tar.gz"
  tar -xzvf apache-jena-3.13.1.tar.gz
  export JENA_HOME="$(pwd)/apache-jena-3.13.1"
  export PATH="$PATH:$JENA_HOME/bin"
  ```

* Test that it works with the first rule in the 'simple' format:
  ```
  sparql --data TEST_DATA.ttl --results=TSV --query \
    <(head -n1 sparql/hamap.simple) | tail -n +2
  ```

* Now loop over all rules:
  ```
  while read rule
  do
      sparql --data TEST_DATA.ttl --results=TSV "$rule" \
        | tail -n +2 > $(echo "$rule" | grep -oP "MF_\d{5}" | head -n1).tsv
  done < sparql/hamap.simple
  ```

Note: This simple approach of looping over all rules is not very efficient.
In our tests of annotating all Swiss-Prot sequences with HAMAP rules,
the overhead of reloading the data and restarting the SPARQL engine was 2-5 seconds per rule,
and the total process was about 10 times as long as the scanning of the sequences with the signatures.
For large amounts of data we recommend to use a persistent RDF store.


## Option 2: Persistent RDF store

We use here the [Apache Jena Fuseki](https://jena.apache.org/documentation/fuseki2/) SPARQL server.
It is tightly integrated with [TDB2](https://jena.apache.org/documentation/tdb2/) to provide a robust, transactional persistent storage layer.
Fuseki can run as a webserver, which allows to send queries over HTTP to the database engine.

* Download Apache Jena Fuseki:
  ```
  wget "http://mirror.easyname.ch/apache/jena/binaries/apache-jena-fuseki-3.13.1.tar.gz"
  tar xzvf apache-jena-fuseki-3.13.1.tar.gz
  ```

* Load the data into the TDB2 database (the command `tdb2.tdbloader` is in the Apache Jena package):
  ```
  tdb2.tdbloader --loc ./TEST_TDB2 TEST_DATA.ttl
  ```

* Start the Fuseki webserver:
  ```
  apache-jena-fuseki-3.13.1/fuseki-server --tdb2 --loc ./TEST_TDB2/ /sparql
  ```
  Fuseki normally runs on port 3030.
  You can use your browser to see the web interface of your SPARQL endpoint at http://localhost:3030/

* Run all queries using the `rsparql` command or an HTTP client like `curl`, `wget`, etc.

  * Example 1: Run the 'simple' rules with the `rsparql` command:
    ```
    while read rule
    do 
        rsparql --service "http://localhost:3030/sparql" --results=TSV "$rule" \
          | tail -n +2 > $(echo "$rule" | grep -oP "MF_\d{5}" | head -n1).tsv
    done < sparql/hamap.simple
    ```

  * Example 2: Run the 'complete' rules with the `curl` command:
    ```
    while read rule
    do 
        curl "http://localhost:3030/sparql" --data-urlencode "query=$rule" \ 
          > $(echo "$rule" | grep -oP "MF_\d{5}" | head -n1).ttl
    done < sparql/hamap.sparql
    ```

In our tests of annotating all Swiss-Prot sequences with HAMAP rules,
the execution of the queries via a `curl` command on a Fuseki server
was faster than the scanning of the sequences with the signatures.
The throughput can easily be increased by running queries in parallel.
Here is an example that uses the `xargs` command to run four parallel processes:

```
time xargs -a sparql/hamap.simple -P 4 -d '\n' -I % \
  -exec sh -c "curl \"http://localhost:3030/sparql\" --data-urlencode \"query=%\" > \$(echo \"%\" | grep -oP \"MF_\d{5}\" | head -n1).ttl"
```
