/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.bio.seq;

import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * Constant TranslationTable instances that embody the genetic code.
 * <P>
 * The genetic code defines how non-overlapping DNA triplets map into
 * amino-acids and the translation-termination signal.
 * 
 * @author Matthew Pocock
 */
public class GeneticCode {
  /**
   * The UNIVERSAL genetic code - used by most organisms for their primary
   * genome.
   */
  public static final TranslationTable UNIVERSAL;
  
  static {
    try {
      FiniteAlphabet DNA = DNATools.getAlphabet();
      CrossProductAlphabet codon = AlphabetManager.instance().getCrossProductAlphabet(
        Collections.nCopies(3, DNA)
      );
      FiniteAlphabet PROT = ProteinTools.getTAlphabet();
      SymbolParser dp = DNA.getParser("symbol");
      SymbolParser aap = PROT.getParser("name");
      SimpleTranslationTable univ = new SimpleTranslationTable(
        (FiniteAlphabet) codon,
        PROT
      );
      process(univ, dp, aap, codon, "ttt", "phe");
      process(univ, dp, aap, codon, "ttc", "phe");
      process(univ, dp, aap, codon, "tta", "leu");
      process(univ, dp, aap, codon, "ttg", "leu");
      process(univ, dp, aap, codon, "tct", "ser");
      process(univ, dp, aap, codon, "tcc", "ser");
      process(univ, dp, aap, codon, "tca", "ser");
      process(univ, dp, aap, codon, "tcg", "ser");
      process(univ, dp, aap, codon, "tat", "tyr");
      process(univ, dp, aap, codon, "tac", "tyr");
      process(univ, dp, aap, codon, "taa", "ter");
      process(univ, dp, aap, codon, "tag", "ter");
      process(univ, dp, aap, codon, "tgt", "cys");
      process(univ, dp, aap, codon, "tgc", "cys");
      process(univ, dp, aap, codon, "tga", "ter");
      process(univ, dp, aap, codon, "tgg", "trp");

      process(univ, dp, aap, codon, "ctt", "leu");
      process(univ, dp, aap, codon, "ctc", "leu");
      process(univ, dp, aap, codon, "cta", "leu");
      process(univ, dp, aap, codon, "ctg", "leu");
      process(univ, dp, aap, codon, "cct", "pro");
      process(univ, dp, aap, codon, "ccc", "pro");
      process(univ, dp, aap, codon, "cca", "pro");
      process(univ, dp, aap, codon, "ccg", "pro");
      process(univ, dp, aap, codon, "cat", "his");
      process(univ, dp, aap, codon, "cac", "his");
      process(univ, dp, aap, codon, "caa", "gln");
      process(univ, dp, aap, codon, "cag", "gln");
      process(univ, dp, aap, codon, "cgt", "arg");
      process(univ, dp, aap, codon, "cgc", "arg");
      process(univ, dp, aap, codon, "cga", "arg");
      process(univ, dp, aap, codon, "cgg", "arg");

      process(univ, dp, aap, codon, "att", "ile");
      process(univ, dp, aap, codon, "atc", "ile");
      process(univ, dp, aap, codon, "ata", "ile");
      process(univ, dp, aap, codon, "atg", "met");
      process(univ, dp, aap, codon, "act", "thr");
      process(univ, dp, aap, codon, "acc", "thr");
      process(univ, dp, aap, codon, "aca", "thr");
      process(univ, dp, aap, codon, "acg", "thr");
      process(univ, dp, aap, codon, "aat", "asn");
      process(univ, dp, aap, codon, "aac", "asn");
      process(univ, dp, aap, codon, "aaa", "lys");
      process(univ, dp, aap, codon, "aag", "lys");
      process(univ, dp, aap, codon, "agt", "ser");
      process(univ, dp, aap, codon, "agc", "ser");
      process(univ, dp, aap, codon, "aga", "arg");
      process(univ, dp, aap, codon, "agg", "arg");

      process(univ, dp, aap, codon, "gtt", "val");
      process(univ, dp, aap, codon, "gtc", "val");
      process(univ, dp, aap, codon, "gta", "val");
      process(univ, dp, aap, codon, "gtg", "val");
      process(univ, dp, aap, codon, "gct", "ala");
      process(univ, dp, aap, codon, "gcc", "ala");
      process(univ, dp, aap, codon, "gca", "ala");
      process(univ, dp, aap, codon, "gcg", "ala");
      process(univ, dp, aap, codon, "gat", "asp");
      process(univ, dp, aap, codon, "gac", "asp");
      process(univ, dp, aap, codon, "gaa", "glu");
      process(univ, dp, aap, codon, "gag", "glu");
      process(univ, dp, aap, codon, "ggt", "gly");
      process(univ, dp, aap, codon, "ggc", "gly");
      process(univ, dp, aap, codon, "gga", "gly");
      process(univ, dp, aap, codon, "ggg", "gly");
      
      UNIVERSAL = univ;
    } catch (Throwable t) {
      throw new BioError(t, "Unable to initialize GeneticCode");
    }
  }
  
  private static void process(
    SimpleTranslationTable table, SymbolParser dp, SymbolParser aap,
    CrossProductAlphabet codonA, String codon, String aa
  ) throws IllegalSymbolException {
    Symbol codonR = codonA.getSymbol(
      dp.parse(codon).toList()
    );
    
    Symbol aaR = aap.parseToken(aa);
    
    table.setTranslation(codonR, aaR);
  }
}
