#!/bin/sh
#sh iterateAllTreesize.sh

for file in /home/sarah/NetBeansProjects/BiNChE_fork/BiNChE/src/main/resources/BiNGO/data/chebi_resultfiles/saddle/dot/*.dot ; do 
	fname=$(basename "$file")
    perl equalTreeSize.pl BiNGO/data/chebi_resultfiles/saddle/dot/$fname BiNGO/data/chebi_resultfiles/hyper/dot/$fname
done


