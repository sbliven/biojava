package seq;

import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;


public class TestTranslation {
  public static void main(String [] args) {
    try {
      SymbolList randomSeq = symbol.Tools.createSymbolList(30);
      SymbolList rev = SymbolListViews.reverse(randomSeq);
      SymbolList revComp = DNATools.complement(rev);
      SymbolList rnaView = RNATools.transcribe(revComp);
      SymbolList codons = SymbolListViews.windowedSymbolList(rnaView, 3);
      SymbolList translation = SymbolListViews.translate(
        codons,
        RNATools.getGeneticCode("UNIVERSAL")
      );

      System.out.println("DNA:\t" + randomSeq.seqString());
      System.out.println("Rev:\t" + rev.seqString());
      System.out.println("RevC:\t" + revComp.seqString());
      System.out.println("Trans:\t" + translation.seqString());
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
