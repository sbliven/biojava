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


package org.biojava.bio.gui;

import java.awt.Paint;
import java.awt.Color;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.DNATools;

/**
 * A no-frills implementation of SymbolStyle.
 *
 * @author Matthew Pocock
 */
public class SimpleSymbolStyle implements SymbolStyle {
  private final Map outlinePaint;
  private final Map fillPaint;
  private final FiniteAlphabet alphabet;
  
  {
    outlinePaint = new HashMap();
    fillPaint = new HashMap();
  }
  
  public Alphabet getAlphabet() {
    return alphabet;
  }
  
  public SimpleSymbolStyle(FiniteAlphabet alphabet) {
    this.alphabet = alphabet;
    Map outline = getStandardOutlinePaints(alphabet);
    Map fill = getStandardFillPaints(alphabet);
    try {
      if(fill == null || outline == null) {
        for(Iterator i = alphabet.iterator(); i.hasNext(); ) {
          Symbol r = (Symbol) i.next();
          if(outline == null) {
            setOutlinePaint(r, Color.black);
          } else {
            setOutlinePaint(r, (Paint) outline.get(r));
          }
          if(fill == null) {
            setFillPaint(r, Color.gray);
          } else {
            setOutlinePaint(r, (Paint) fill.get(r));
          }
        }
      }
    } catch (IllegalSymbolException ire) {
      throw new BioError(ire, "Symbol dissapeared from my alphabet");
    }
  }
  
  public Paint outlinePaint(Symbol r) throws IllegalSymbolException {
    getAlphabet().validate(r);
    return (Paint) outlinePaint.get(r);
  }
  
  public Paint fillPaint(Symbol r) throws IllegalSymbolException {
    getAlphabet().validate(r);
    return (Paint) fillPaint.get(r);
  }
  
  public void setOutlinePaint(Symbol r, Paint paint)
  throws IllegalSymbolException {
    getAlphabet().validate(r);
    outlinePaint.put(r, paint);
  }

  public void setFillPaint(Symbol r, Paint paint)
  throws IllegalSymbolException {
    getAlphabet().validate(r);
    fillPaint.put(r, paint);
  }

  private static Map standardFillPaints;
  private static Map standardOutlinePaints;
  
  public static Map getStandardFillPaints(Alphabet alpha) {
    return (Map) standardFillPaints.get(alpha);
  }
  
  public static Map getStandardOutlinePaints(Alphabet alpha) {
    return (Map) standardOutlinePaints.get(alpha);
  }
  
  static {
    standardFillPaints = new HashMap();
    standardOutlinePaints = new HashMap();
    
    Map dnaFill = new HashMap();
    dnaFill.put(DNATools.t(), Color.red);
    dnaFill.put(DNATools.g(), Color.blue);
    dnaFill.put(DNATools.c(), Color.yellow);
    dnaFill.put(DNATools.a(), Color.green);
    standardFillPaints.put(DNATools.getDNA(), dnaFill);

    Map dnaOutline = new HashMap();
    dnaOutline.put(DNATools.t(), Color.black);
    dnaOutline.put(DNATools.a(), Color.black);
    dnaOutline.put(DNATools.g(), Color.black);
    dnaOutline.put(DNATools.c(), Color.black);
    standardOutlinePaints.put(DNATools.getDNA(), dnaOutline);
  }
}
