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
 * A simple implementation of SymbolStyle optimized for DNA.
 *
 * @author Matthew Pocock
 */
public class DNAStyle implements SymbolStyle {
  private Map outlinePaint;
  private Map fillPaint;
  
  {
    outlinePaint = new HashMap();
    fillPaint = new HashMap();
  }
  
  public Paint outlinePaint(Symbol r) throws IllegalSymbolException {
    DNATools.getAlphabet().validate(r);
    return (Paint) outlinePaint.get(r);
  }
  
  public Paint fillPaint(Symbol r) throws IllegalSymbolException {
    DNATools.getAlphabet().validate(r);
    return (Paint) fillPaint.get(r);
  }
  
  public void setOutlinePaint(Symbol r, Paint paint)
  throws IllegalSymbolException {
    DNATools.getAlphabet().validate(r);
    outlinePaint.put(r, paint);
  }

  public void setFillPaint(Symbol r, Paint paint)
  throws IllegalSymbolException {
    DNATools.getAlphabet().validate(r);
    fillPaint.put(r, paint);
  }
  
  public DNAStyle() {
    try {
      setOutlinePaint(DNATools.t(), Color.black);
      setFillPaint(DNATools.t(), Color.red);
      setOutlinePaint(DNATools.a(), Color.black);
      setFillPaint(DNATools.a(), Color.green);
      setOutlinePaint(DNATools.g(), Color.black);
      setFillPaint(DNATools.g(), Color.blue);
      setOutlinePaint(DNATools.c(), Color.black);
      setFillPaint(DNATools.c(), Color.yellow);
    } catch (IllegalSymbolException ire) {
      throw new BioError(ire, "DNA symbols dissapeared from DNA alphabet");
    }
  }
}
