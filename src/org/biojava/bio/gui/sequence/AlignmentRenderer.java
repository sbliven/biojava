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

import java.util.*;

import java.awt.*;
import java.awt.geom.*;

import org.biojava.utils.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;

import java.util.List;

public class AlignmentRenderer
extends SequenceRendererWrapper {
  public static ChangeType LABEL = new ChangeType(
    "The label used to select the Alignment component to render has changed.",
    "org.biojava.bio.gui.sequence.AlignmentRenderer",
    "LABEL",
    SequenceRenderContext.LAYOUT
  );
  
  private Object label;
  
  public void setLabel(Object label)
  throws ChangeVetoException {
    if(hasListeners()) {
      ChangeEvent ce = new ChangeEvent(
        this, LABEL,
        label, this.label
      );
      ChangeSupport cs = getChangeSupport(LABEL);
      synchronized(cs) {
        cs.firePreChangeEvent(ce);
        this.label = label;
        cs.firePostChangeEvent(ce);
      }
    } else {
      this.label = label;
    }
  }
  
  public Object getLabel() {
    return this.label;
  }
  
  public double getDepth(SequenceRenderContext ctx, RangeLocation pos) {
    SequenceRenderContext subctx = new SequenceRenderContextForLabel(
      ctx, getLabel()
    );
    return super.getDepth(subctx, pos);
  }
  
  public double getMinimumLeader(SequenceRenderContext ctx) {
    SequenceRenderContext subctx = new SequenceRenderContextForLabel(
      ctx, getLabel()
    );
    return super.getMinimumLeader(subctx);
  }
  
  public double getMinimumTrailer(SequenceRenderContext ctx) {
    SequenceRenderContext subctx = new SequenceRenderContextForLabel(
      ctx, getLabel()
    );
    return super.getMinimumTrailer(subctx);
  }
  
  public void paint(
        Graphics2D g,
        SequenceRenderContext ctx,
        RangeLocation pos
  ) {
    SequenceRenderContext subctx = new SequenceRenderContextForLabel(
      ctx, getLabel()
    );
    super.paint(g, subctx, pos);
  }

  private class SequenceRenderContextForLabel implements SequenceRenderContext {
    private SequenceRenderContext parent;
    private Object label;
    
    private SequenceRenderContextForLabel(
            SequenceRenderContext parent,
            Object label
    ) {
      this.parent = parent;
      this.label = label;
    }
    
    public int getDirection() {
      return parent.getDirection();
    }
    
    public double getScale() {
      return parent.getScale();
    }
    
    public double sequenceToGraphics(int i) {
      return parent.sequenceToGraphics(i);
    }
    
    public int graphicsToSequence(double d) {
      return parent.graphicsToSequence(d);
    }
    
    public int graphicsToSequence(Point point) {
      return parent.graphicsToSequence(point);
    }
    
    public SymbolList getSequence() {
      SymbolList sl = null;
      SymbolList pseq = parent.getSequence();
      if (pseq instanceof Alignment) {
        Alignment aseq = (Alignment) parent.getSequence();
        sl = aseq.symbolListForLabel(label);
      }
      
      if(sl == null) {
        sl = SymbolList.EMPTY_LIST;
      }
      
      return sl;
    }
    
    public SequenceRenderContext.Border getLeadingBorder() {
      return parent.getLeadingBorder();
    }
    
    public SequenceRenderContext.Border getTrailingBorder() {
      return parent.getTrailingBorder();
    }
    
    public Font getFont() {
      return parent.getFont();
    }
  }
}
