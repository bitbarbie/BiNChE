#!C:\Perl\bin\perl
#
# Stephan A. Beisken
#
# Script stripping the ChEBI ontology file of the "role"
# and "subatomic particle" hierarchies.
#
use strict;
use warnings;

# role
open(ROLE, "./chebi_50906_children.xml") or die "$!\n";

my $roleIds = { };
while(<ROLE>) {
	if ($_ =~ /<chebiId>CHEBI:(.*)<\/chebiId>/) {
		if (not exists $roleIds->{$1}) {
			$roleIds->{$1 . "\n"} = '';
		}
	}
}

close ROLE;

# subatomic particle
open(PARTICLE, "./chebi_36342_children.xml") or die "$!\n";

while(<PARTICLE>) {
	if ($_ =~ /<chebiId>CHEBI:(.*)<\/chebiId>/) {
		if (not exists $roleIds->{$1}) {
			$roleIds->{$1 . "\n"} = '';
		}
	}
}

close PARTICLE;

open(CHEBI, "./chebi.obo") or die "$!\n";
open(CLEAN, ">./chebi_clean.obo") or die "$!\n";

my $skip = 0;
while(<CHEBI>) {

	if ($_ =~ /Term/) {
		next;
	} elsif ($_ =~ /^id: CHEBI:/) {
		$skip = (exists $roleIds->{[ split(/CHEBI:/, $_) ]->[1]});
		if (!$skip) { 
			print CLEAN "[Term]\n";
		}
	}
	
	if (!$skip) {
		if ($_ !~ /CHEBI:/) {
			print CLEAN $_;
		} elsif ($_ =~ /CHEBI:/ && not exists $roleIds->{[ split(/CHEBI:/, $_) ]->[1]}) {
			print CLEAN $_;
		}
	} else {
		#print $_;
	}
}

close CLEAN;
close CHEBI;

exit(0);