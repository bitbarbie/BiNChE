# BiNChE
Enrichment analysis of molecular structures based on the ChEBI ontology

BiNChE is an enrichment analysis tool based on the source code of BiNGO, adapted to be used with the ChEBI ontology. 
This project is a Java library, meant to be used as a command line tool or as library to be used within other Java software.

BiNChE can be accessed in an interactive web page through the [EBI](www.ebi.ac.uk) within the tools section of [ChEBI](www.ebi.ac.uk/chebi), or by pressing [here](www.ebi.ac.uk/chebi/tools/binche/).

Further information on the use of the web version of BiNChE can be found [here](https://github.com/pcm32/BiNCheWeb/wiki/BiNChE)

# Set up

In order to use the stand-alone version of BiNChE, an initial setup is required. In Linux or MacOS X, this can be done by executing on the base BiNChE directory

    ./runConfig.sh

This will download the latest ChEBI Ontology OBO file, run the reasoner, and produce the files required to be able to run BiNChE locally. This can be re-executed to update the ChEBI ontology derived files (as ChEBI makes a new release every month).
