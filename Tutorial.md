# HAMAP as SPARQL tutorial

This tutorial was tested on CentOS 7 and should also work on recent Fedora.

*Table of contents*

* [Get the protein sequences and HAMAP signatures](#get-the-protein-sequences-and-hamap-signatures)
* [Scan the protein sequences with the HAMAP signatures](#scan-the-protein-sequences-with-the-hamap-signatures)
  * [Method 1: PfTools v3.2](#method-1-pftools-v32)
  * [Method 2: InterProScan](#method-2-interproscan)
* [Generate organism information](#generate-organism-information)
* [Get the SPARQL representation of the HAMAP rules](#get-the-sparql-representation-of-the-hamap-rules)
* [Combine all data into one file](#combine-all-data-into-one-file)
* [Use a SPARQL engine to apply the rules](#use-a-sparql-engine-to-apply-the-rules)
  * [Option 1: In-memory RDF store](#option-1-in-memory-rdf-store)
  * [Option 2: Persistent RDF store](#option-2-persistent-rdf-store)
* [Optimizations](#optimizations)
  * [Parallel rule execution](#parallel-rule-execution)
  * [Materializing taxonomy data](#materializing-taxonomy-data)

## Get the protein sequences and HAMAP signatures

The [HAMAP](https://hamap.expasy.org) system classifies and annotates protein sequences using a collection of expert-curated protein family signatures and annotation rules. We describe here two methods to scan protein sequences with the HAMAP signatures and both require the sequences in [FASTA format](https://en.wikipedia.org/wiki/FASTA_format). 

For this tutorial we will use the sequences of the _E. coli_ proteome [UP000069664](https://www.uniprot.org/proteomes/UP000069664) that you can download in FASTA format from [UniParc](https://www.uniprot.org/uniparc/):

```bash
wget "https://www.uniprot.org/uniparc/?query=proteome:UP000069664&format=fasta" \
  -O TEST_SEQUENCES.fasta
```

Now download the HAMAP signatures:

```bash
wget "https://ftp.expasy.org/databases/hamap/hamap.prf.gz"
gunzip hamap.prf.gz
```


## Scan the protein sequences with the HAMAP signatures

In the [Swiss-Prot group](https://www.sib.swiss/alan-bridge-group) we use the program `pfscanV3` to scan our protein sequences with the HAMAP signatures,
as well as users' sequences via the [HAMAP-Scan web service](https://hamap.expasy.org/hamap_scan.html),
but the more widely used `InterProScan` will give you nearly identical results.
Choose one of the two methods below.
Both will take about 5min to run and generate data in [RDF Turtle format](https://en.wikipedia.org/wiki/Turtle_(syntax)).

### Method 1: PfTools v3.2

* Download the [PfTools v3.2](https://github.com/sib-swiss/pftools3/blob/master/README.md) code from GitHub and follow the instructions in the [INSTALL](https://github.com/sib-swiss/pftools3/blob/master/INSTALL) file to compile the code, e.g.:
  ```bash
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
  Note: You may have to install a recent version of cmake to compile the code.

* Scan the FASTA file with the HAMAP signatures, using the -o10 option for the RDF Turtle output format:
  ```bash
  pftools3/build/src/C/pfscanV3 hamap.prf -f TEST_SEQUENCES.fasta -o10 > TEST_SCAN.ttl
  ```

* Generate RDF statements for the sequences.
  
  The -o10 option of `pfscanV3` does unfortunately not generate RDF statements for the sequences.
  You have to append a statement of the following format for each sequence to the scan result:
  ```turtle
  ys:IDENTIFIER rdf:value "SEQUENCE" .
  ```
  You can do this by converting the FASTA file:
  
  * First define the namespaces:
    ```bash
    printf "PREFIX ys:<http://example.org/yoursequence/>
    PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" \
      >> TEST_SCAN.ttl
    ```
  * Now execute the following command to convert the FASTA file to RDF turtle format:
    ```bash
    cat TEST_SEQUENCES.fasta \
      | awk '/^>/ {printf("\" . \nys:%s rdf:value \"", substr($1,2)); next; } \
                  {printf("%s", $0)} END {printf("\" .\n")}' \
      | grep -v -P '^"' >> TEST_SCAN.ttl
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

### Method 2: InterProScan

* Download the [InterProScan](https://github.com/ebi-pf-team/interproscan/wiki) package.
  This will take a while because the package is very big!
  ```bash
  wget "ftp://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/5.39-77.0/interproscan-5.39-77.0-64-bit.tar.gz"
  tar -xvzf interproscan-5.39-77.0-64-bit.tar.gz
  ```

* Scan the FASTA file with the HAMAP signatures:
  ```bash
  interproscan-5.39-77.0/interproscan.sh --disable-precalc --applications hamap \
    --formats XML --input TEST_SEQUENCES.fasta --outfile TEST_SCAN.xml
  ```

* Download the XSLT stylesheet [`interproToRdf.xslt`](src/main/xlst/interproToRdf.xslt) and use it to convert the InterPro XML result file to RDF Turtle format:
  ```bash
  xsltproc interproToRdf.xslt TEST_SCAN.xml > TEST_SCAN.ttl
  ```

## Generate organism information

HAMAP needs to know the [NCBI taxonomy identifier](https://www.ncbi.nlm.nih.gov/taxonomy)
of the organism from which the sequences come.
Our _E. coli_ proteome [UP000069664](https://www.uniprot.org/proteomes/UP000069664) has the taxonomy identifier [511145](https://www.uniprot.org/taxonomy/511145).
If you try this tutorial with sequences from a different organism, please [lookup the NCBI taxonomy identifier](https://www.uniprot.org/taxonomy/) of that organism.
If you cannot find an identifier for your species, use instead the identfier of its genus or a closely related species. Then replace the number '511145' in the code examples below with the NCBI taxonomy identifier you found.

* First define the namespaces that we will use in the RDF statements:
  ```bash
  printf "PREFIX yr:<http://example.org/yourrecord/>
  PREFIX up:<http://purl.uniprot.org/core/>
  PREFIX taxon:<http://purl.uniprot.org/taxonomy/>\n" \
    > TEST_ORGANISM.ttl
  ```

* Then append the full taxonomic lineage for the organism in RDF.
  You can retrieve this data from the [UniProt](https://www.uniprot.org/taxonomy/) website:
  ```bash
  wget "http://www.uniprot.org/taxonomy/?query=id:511145&include=yes&format=ttl" \
    -O - >> TEST_ORGANISM.ttl
  ```

* Now you must add one RDF statement of the following format for each of the sequence records:
  ```turtle
  yr:IDENTIFIER up:organism taxon:511145 .
  ```
  You can use the previously generate `TEST_SEQUENCES.ttl` file to do this:
  ```bash
  grep -oP "^ys:\w+" TEST_SEQUENCES.ttl | cut -c 4- \
    | awk '{print "yr:"$1" up:organism taxon:511145 ."}' >> TEST_ORGANISM.ttl
  ```

The result file `TEST_ORGANISM.ttl` should now look similar to this:

```turtle
PREFIX yr:<http://example.org/yourrecord/>
PREFIX up:<http://purl.uniprot.org/core/>
PREFIX taxon:<http://purl.uniprot.org/taxonomy/>
... taxonomy data retrieved from uniprot.org here ...
yr:UPI0000000053 up:organism taxon:511145 .
yr:UPI0000000054 up:organism taxon:511145 .
...
```


## Get the SPARQL representation of the HAMAP rules

The HAMAP FTP site distributes several representations of the rules for use with Semantic Web technologies that are described in the [README](https://ftp.expasy.org/databases/hamap/sparql/README) file.

For this tutorial you need to download the following files:

```bash
wget "https://ftp.expasy.org/databases/hamap/sparql/hamap.sparql"
wget "https://ftp.expasy.org/databases/hamap/sparql/hamap.simple"
wget "https://ftp.expasy.org/databases/hamap/sparql/template_matches.ttl"
```

* The `hamap.sparql` file contains the complete rules in SPARQL syntax with a CONSTRUCT clause to generate RDF statements for all annotation types in [UniProt RDF format](https://sparql.uniprot.org/uniprot).

* The `hamap.simple` file contains simplified rules in SPARQL syntax with a SELECT clause to generate only a limited set of key annotation types
([GO terms](http://geneontology.org/),
[EC numbers](https://en.wikipedia.org/wiki/Enzyme_Commission_number),
[UniProt keywords](https://www.uniprot.org/keywords/))
in tabular format.

* The file `template_matches.ttl` contains the results of a scan of 
the Swiss-Prot entries that are used to curate the HAMAP rules (so called 'template entries')
with the HAMAP signatures.
The alignment of these Swiss-Prot sequences to HAMAP signatures is required
to 'transfer' sequence features like active sites, etc.
from the Swiss-Prot template entries to the query sequences that match a HAMAP signature.


## Combine all data into one file

Now you have all the data that you need in RDF Turtle format.
Concatenate the results of the scan, the organism information, and the HAMAP template matches.

```bash
cat TEST_SCAN.ttl \
    TEST_ORGANISM.ttl \
    template_matches.ttl > TEST_DATA.ttl
```


## Use a SPARQL engine to apply the rules

There are many [triplestores](https://en.wikipedia.org/wiki/Comparison_of_triplestores) available that you can use to execute SPARQL queries. For this tutorial we describe two that are easy to install. To process large amounts of data, other products may have better performance.

### Option 1: In-memory RDF store

An in-memory RDF store is generally simple to use, but not optimized for speed and large amounts of data.
It is not recommended for a production service, but can be useful for testing.
We use here the in-memory store of the [Apache Jena](https://jena.apache.org/about_jena/)
project to illustrate how you can annotate sequences with SPARQL rules.

* Download Apache Jena:
  ```bash
  wget "http://mirror.easyname.ch/apache/jena/binaries/apache-jena-3.13.1.tar.gz"
  tar -xzvf apache-jena-3.13.1.tar.gz
  export JENA_HOME="$(pwd)/apache-jena-3.13.1"
  export PATH="$PATH:$JENA_HOME/bin"
  ```

* Test that it works with the first rule in the 'simple' format:
  ```bash
  sparql --data TEST_DATA.ttl --results=TSV --query \
    <(head -n1 hamap.simple) | tail -n +2
  ```

* Now loop over all rules:
  ```bash
  while read rule
  do
      sparql --data TEST_DATA.ttl --results=TSV "$rule" \
        | tail -n +2 > $(echo "$rule" | grep -oP "MF_\d{5}" | head -n1).tsv
  done < hamap.simple
  ```

This simple approach of looping over all rules is not very efficient.
It takes about 1.5h to process this _E. coli_ proteome.
In our tests of annotating all Swiss-Prot sequences with HAMAP rules,
the overhead of reloading the data and restarting the SPARQL engine was 2-5 seconds per rule,
and the total process was about 10 times as long as the scanning of the sequences with the signatures.
For large amounts of data we recommend to use a persistent RDF store.

### Option 2: Persistent RDF store

We use here the [Apache Jena Fuseki](https://jena.apache.org/documentation/fuseki2/) SPARQL server.
It is tightly integrated with [TDB2](https://jena.apache.org/documentation/tdb2/) to provide a robust, transactional persistent storage layer.
Fuseki can run as a webserver, which allows to send queries over HTTP to the database engine.

* Download Apache Jena Fuseki:
  ```bash
  wget "http://mirror.easyname.ch/apache/jena/binaries/apache-jena-fuseki-3.13.1.tar.gz"
  tar xzvf apache-jena-fuseki-3.13.1.tar.gz
  ```

* Load the data into the TDB2 database (the command `tdb2.tdbloader` is in the Apache Jena package):
  ```bash
  tdb2.tdbloader --loc ./TEST_TDB2 TEST_DATA.ttl
  ```

* Start the Fuseki webserver:
  ```bash
  apache-jena-fuseki-3.13.1/fuseki-server --tdb2 --loc ./TEST_TDB2/ /sparql
  ```
  Fuseki normally runs on port 3030.
  You can use your browser to see the web interface of your SPARQL endpoint at http://localhost:3030/

* Run all queries with an HTTP client like `curl`, `wget`, etc.

  * <a name="option-2-persistent-rdf-store-example-1"></a>
    Example 1: Run the 'simple' rules with a `curl` command:
    ```bash
    while read rule
    do 
        curl -s "http://localhost:3030/sparql" --data-urlencode "query=$rule" \
          -H "Accept: text/tab-separated-values" | tail -n +2 \
          > $(echo "$rule" | grep -oP "MF_\d{5}" | head -n1).tsv
    done < hamap.simple
    ```
    With the `curl -H` option you can request a different output format from Fuseki.
    Here we ask for tab-separated values.

  * <a name="option-2-persistent-rdf-store-example-2"></a>
    Example 2: Run the 'complete' rules with a `curl` command:
    ```bash
    while read rule
    do 
        curl -s "http://localhost:3030/sparql" --data-urlencode "query=$rule" \
          > $(echo "$rule" | grep -oP "MF_\d{5}" | head -n1).ttl
    done < hamap.sparql
    ```

With the persistent store TDB2, it takes only 2-3 min to process this _E. coli_ proteome.


## Optimizations

### Parallel rule execution

In our tests of annotating all Swiss-Prot sequences with HAMAP rules,
the execution of the queries via a `curl` command on a Fuseki server
was faster than the scanning of the sequences with the signatures.

Both steps can be easily parallelized to reduce the execution time when processing large amounts of data.

Here is an example that uses the `xargs` command to run four parallel processes of the command that was shown in [Example 1](#option-2-persistent-rdf-store-example-1) to execute the rules with a `curl` command on a Fuseki webserver:

```bash
xargs -a hamap.simple -P 4 -d '\n' -I % -exec sh -c \
  "curl -s \"http://localhost:3030/sparql\" --data-urlencode \"query=%\" "`
 `"  -H \"Accept: text/tab-separated-values\" | tail -n +2 "`
 `"  > \$(echo \"%\" | grep -oP \"MF_\d{5}\" | head -n1).tsv"
```

### Materializing taxonomy data

The rules that you downloaded from the HAMAP FTP site use `rdfs:subClassOf+` statements to determine whether the organism of your sequences is within the taxonomic scope of a rule. This has the advantage that you have to load less taxonomy data into your RDF store, but means that the SPARQL query engine has to re-build the taxonomy hierarchy for each query, and this can be the rate-limiting step for many of the rules.

To optimize for speed, you can materialize the taxonomy hierarchy in your RDF store with this statement:

```sparql
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
INSERT {?taxon rdfs:subClassOf ?super} WHERE { ?taxon rdfs:subClassOf+ ?super }
```

On a Fuseki server, you can execute this statement via a `curl` command:

```bash
curl http://localhost:3030/sparql/update -X POST \
--data 'update=PREFIX+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%0A%0AINSERT+%7B%3Ftaxon+rdfs%3AsubClassOf+%3Fsuper%7D+WHERE+%7B+%3Ftaxon+rdfs%3AsubClassOf%2B+%3Fsuper+%7D+' \
-H 'Accept: text/plain,*/*;q=0.9'
```

Then you can replace `rdfs:subClassOf+` with `rfds:subClassOf` in the rules:

```bash
sed -r -i 's/rdfs:subClassOf\+/rdfs:subClassOf/g' hamap.sparql
sed -r -i 's/rdfs:subClassOf\+/rdfs:subClassOf/g' hamap.simple
```

This will reduce the query execution time in most RDF stores, including TDB2.
