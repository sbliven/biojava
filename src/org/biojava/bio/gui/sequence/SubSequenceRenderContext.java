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

package org.biojava.bio.gui.sequence;

import java.awt.*;
import java.awt.geom.*;
import java.beans.*;
import java.util.*;
import java.util.List;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.gui.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

public class SubSequenceRenderContext
implements SequenceRenderContext {
  private final Sequence seq;
  private final SequenceRenderContext src;
  
  public SubSequenceRenderContext(
    SequenceRenderContext src, FeatureHolder fh 
  ) {
    this.src = src;
    this.seq = new PartialSequence((Sequence) src.getSequence(), fh);
  }
  
  public int getDirection() {
    return src.getDirection();
  }
  
  public double getScale() {
    return src.getScale();
  }
  
  public double sequenceToGraphics(int i) {
    return src.sequenceToGraphics(i);
  }
  
  public int graphicsToSequence(double d) {
    return src.graphicsToSequence(d);
  }
  
  public SymbolList getSequence() {
    return seq;
  }
  
  public SequenceRenderContext.Border getLeadingBorder() {
    return src.getLeadingBorder();
  }
  
  public SequenceRenderContext.Border getTrailingBorder() {
    return src.getTrailingBorder();
  }
  
  public Font getFont() {
    return src.getFont();
  }
  
  private static class PartialSequence implements Sequence {
    private final Sequence seq;
    private final FeatureHolder fh;
    
    public PartialSequence(Sequence seq, FeatureHolder fh) {
      this.seq = seq;
      this.fh = fh;
    }
    
    public String getURN() {
      return seq.getURN();
    }
    
    public String getName() {
      return seq.getName();
    }
    
    public Annotation getAnnotation() {
      return seq.getAnnotation();
    }
    
    public int countFeatures() {
      return fh.countFeatures();
    }
    
    public Iterator features() {
      return fh.features();
    }
    
    public FeatureHolder filter(FeatureFilter fc, boolean recurse) {
      return fh.filter(fc, recurse);
    }
    
    public Feature createFeature(Feature.Template ft)
    throws BioException, ChangeVetoException {
      return fh.createFeature(ft);
    }
    
    public void removeFeature(Feature f)
    throws ChangeVetoException {
      fh.removeFeature(f);
    }
    
    public Alphabet getAlphabet() {
      return seq.getAlphabet();
    }
    
    public int length() {
      return seq.length();
    }
    
    public Symbol symbolAt(int index) throws IndexOutOfBoundsException {
      return seq.symbolAt(index);
    }
    
    public List toList() {
      return seq.toList();
    }
    
    public Iterator iterator() {
      return seq.iterator();
    }
    
    public SymbolList subList(int start, int end)
    throws IndexOutOfBoundsException {
      return seq.subList(start, end);
    }
    
    public String seqString() {
      return seq.seqString();
    }
    
    public String subStr(int start, int end)
    throws IndexOutOfBoundsException {
      return seq.subStr(start, end);
    }
    
    public void edit(Edit edit)
    throws IndexOutOfBoundsException, IllegalAlphabetException,
    ChangeVetoException {
      seq.edit(edit);
    }
    
    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
  }
}
