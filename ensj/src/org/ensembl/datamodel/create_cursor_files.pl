# Create Cursor classes from SNPCursor template
foreach $TYPE ("Gene", "Exon", "Feature", "Marker", "SimilarityFeature", "Transcript", "Translation", "RepeatFeature", "Clone") {
  $cmd = "perl -pe \"s/SNP/".$TYPE."/;\" SNPCursor.java > " . $TYPE . "Cursor.java";
  print $cmd . "\n";
  system $cmd;
}
