package seq;

import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;


public class TestTranslation {
  public static void main(String [] args) {
    try {
      SymbolList randomSeq = symbol.Tools.createSymbolList(30);
      SymbolList rev = new ReverseSymbolList(randomSeq);
      SymbolList revComp = DNATools.complement(rev);
      SymbolList codons = new WindowedSymbolList(revComp, 3);
      SymbolList translation = new TranslatedSymbolList(
        codons,
        GeneticCode.UNIVERSAL
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
