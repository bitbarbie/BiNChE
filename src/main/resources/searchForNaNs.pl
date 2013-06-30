# perl searchForNaNs.pl <Pfad>/infile.txt >InfNan.txt


open($infile,$ARGV[0]) or die print "infile geht ni\n";
while($testfile = <$infile>) {
    $result = <$infile>;
    
    chomp($testfile);
    chomp($result);

    open($out, $result.".dot") or die print "out geht ni\n";
    %results;
    while($line = <$out>) {
        if(grep(/NaN/,$line)) {
            print "NaN: ".$testfile."\n";
        } 
        if(grep(/infinity/,$line) || grep(/Infinity/,$line)) {
            print "Infinity: ".$testfile."\n";
        }
    }
    close($out);
  
}
close($infile);
