import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

public class TestTranslation {
  public static void main(String [] args) {
    try {
      ResidueList randomSeq = SeqTools.createResidueList(30);
      ResidueList revComp = new ComplementResidueList(randomSeq);
      ResidueList codons = new WindowedResidueList(revComp, 3);
      ResidueList translation = new TranslatedResidueList(
        codons,
        GeneticCode.UNIVERSAL
      );

      System.out.println("DNA:\t" + randomSeq.seqString());
      System.out.println("RevC:\t" + revComp.seqString());
      System.out.println("Trans:\t" + translation.seqString());
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
