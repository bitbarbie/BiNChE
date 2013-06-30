# prüfe für alle kombinationen der files aus der datei infile.txt ob alle knoten aus der eingabe
# auch im ergebnis file enthalten sind -> ausgegeben wird der filename und der name des
# verlorenen knoten

# aufruf inputNodesLost.pl <pfad>/infile.txt >>lostNodes.txt

open($infile,$ARGV[0]) or die print "infile $infile geht ni\n";
while($testfile = <$infile>) {
    $result = <$infile>;
    
    chomp($testfile);
    chomp($result);

    open($out, $result.".dot") or die print "out $result.dot geht ni\n";
    %results;
    while($line = <$out>) {
        @splits = split(/ /,$line);
        if( $splits[3] =="["){
            $results{$splits[2]}=1;
        } 
    }
    close($out);
    
    open($in,$testfile) or die print "in geht ni\n";
    while($line = <$in>) {
        @splits = split(/:|\t/,$line);
        if(!($results{$splits[1]})) {
            print $testfile." : ".$splits[1]."\n";
            exit;
        }
    }
    close($in);
}
close($infile);
