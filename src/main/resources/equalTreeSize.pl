
# perl equalTreeSize.pl <saddleOut> <hyperOut>
open ($saddle,$ARGV[0]) or die "Saddle Out nicht gefunden\n";
open($hyper, $ARGV[1]) or die "Hyper Out nicht gefunden\n";
$scountn=0;
$scounth=0;
while($line=<$saddle>) {
    if(grep(/->/,$line)) {$scounte++;}
    else{$scountn++;}
}
$hcountn=0;
$hcounth=0;
while($line=<$hyper>) {
    if(grep(/->/,$line)) {$hcounte++;}
    else{$hcountn++;}
}
# ungleiche Baumgröße, wenn Knoten oder Kanten-Anzahl unterschiedlich
if($scountn != $hcountn || $scounte != $hcounte) { 
    print "INEQUAL ".$ARGV[0]." and ".$ARGV[1]."\n";
}
close($saddle);
close($hyper);
