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

import java.awt.Font;
import java.awt.Point;

import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.symbol.RangeLocation;
import org.biojava.bio.symbol.SymbolList;

/**
 * @author Matthew Pocock
 */
public class SubSequenceRenderContext
implements SequenceRenderContext {
  private final SequenceRenderContext src;
  private final SymbolList symbols;
  private final FeatureHolder features;
  private final RangeLocation range;
  
  public SubSequenceRenderContext(
    SequenceRenderContext src,
    SymbolList symbols,
    FeatureHolder features,
    RangeLocation range
  ) {
    this.src = src;
    this.symbols = symbols;
    this.features = features;
    this.range = range;
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
  
  public int graphicsToSequence(Point point) {
    return src.graphicsToSequence(point);
  }

  public SymbolList getSymbols() {
    if(symbols == null) {
      return src.getSymbols();
    } else {
      return symbols;
    }
  }

  public FeatureHolder getFeatures() {
    if(features == null) {
      return src.getFeatures();
    } else {
      return features;
    }
  }
  
  public RangeLocation getRange() {
    if(range == null) {
      return src.getRange();
    } else {
      return range;
    }
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
}
