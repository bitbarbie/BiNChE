# perl searchForNaNs.pl <Pfad>/infile.txt >InfNan.txt


open($infile,$ARGV[0]) or die print "infile geht ni\n";
while($testfile = <$infile>) {
    $result = <$infile>;
    
    chomp($testfile);
    chomp($result);

    eval{ 
        open($out, substr($result,0,29)."saddle/dot/".substr($result,29,length($result)).".dot") or die print "out geht ni\n";
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
   };
}
close($infile);
