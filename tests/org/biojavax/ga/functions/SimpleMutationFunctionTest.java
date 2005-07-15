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

package org.biojavax.ga.functions;

import java.util.*;
import junit.framework.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojavax.ga.util.GATools;



/**
 * @author Mark Schreiber
 */
public class SimpleMutationFunctionTest extends TestCase {

  public SimpleMutationFunctionTest(String s) {
    super(s);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  public void testMutate() {
    SimpleMutationFunction func = new SimpleMutationFunction();
    try {
      SymbolList seq=  DNATools.createDNA("aaaaaaaaa");
      SymbolList symbollistRet = func.mutate(seq);

      func.setMutationProbs(new double[]{0.0, 0.0, 1.0, 0.0});
      func.setMutationSpectrum(
          GATools.standardMutationDistribution(DNATools.getDNA()));
      func.mutate(seq);

      Set syms = new HashSet();
      syms.add(DNATools.c()); syms.add(DNATools.g()); syms.add(DNATools.t());
      Symbol ambig = DNATools.getDNA().getAmbiguity(syms);

      assertTrue( seq.symbolAt(3) != DNATools.a());
      //System.out.println(seq.subStr(3,3));
      assertTrue( ambig.getMatches().contains(seq.symbolAt(3)));
    }
    catch(Exception e) {
      fail(e.getMessage());
    }
  }
}
