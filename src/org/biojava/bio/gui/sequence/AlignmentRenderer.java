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
extends AbstractForwarder
implements SequenceRenderer {
  private List renderList;

  {
    renderList = new ArrayList();
  }

  public void addRenderer(SequenceRenderer sr, Object label) {
    renderList.add(new LabelAndRenderer(sr, label));
    registerRepaint(sr, SequenceRenderContext.REPAINT);
    registerLayout(sr, SequenceRenderContext.LAYOUT);
  }

  public double getDepth(SequenceRenderContext ctx, int min, int max) {
    double depth = 0.0;
    for (Iterator i = renderList.iterator(); i.hasNext(); ) {
      LabelAndRenderer lar = (LabelAndRenderer) i.next();
      SequenceRenderContext subctx = new SequenceRenderContextForLabel(ctx, lar.getLabel());
      depth += lar.getRenderer().getDepth(subctx, min, max);
    }
    return depth;
  }
  
  public double getMinimumLeader(SequenceRenderContext ctx) {
    double leader = 0.0;
    for (Iterator i = renderList.iterator(); i.hasNext(); ) {
      LabelAndRenderer lar = (LabelAndRenderer) i.next();
      SequenceRenderContext subctx = new SequenceRenderContextForLabel(ctx, lar.getLabel());
      leader = Math.max(lar.getRenderer().getMinimumLeader(subctx), leader);
    }
    return leader;
  }
  
  public double getMinimumTrailer(SequenceRenderContext ctx) {
    double trailer = 0.0;
    for (Iterator i = renderList.iterator(); i.hasNext(); ) {
      LabelAndRenderer lar = (LabelAndRenderer) i.next();
      SequenceRenderContext subctx = new SequenceRenderContextForLabel(ctx, lar.getLabel());
      trailer = Math.max(lar.getRenderer().getMinimumTrailer(subctx), trailer);
    }
    return trailer;
  }
  
  public void paint(
        Graphics2D g,
        SequenceRenderContext ctx,
        int min,
        int max
  ) {
    double offset = 0.0;
    
    for (Iterator i = renderList.iterator(); i.hasNext(); ) {
      LabelAndRenderer lar = (LabelAndRenderer) i.next();
      SequenceRenderContext subctx = new SequenceRenderContextForLabel(
      ctx, lar.getLabel()
    );
    
    double depth = lar.getRenderer().getDepth(subctx, min, max);
      int dir = ctx.getDirection();
      if (dir == ctx.HORIZONTAL) {
        g.translate(0.0, offset);
      } else {
        g.translate(offset, 0.0);
      }
      
      lar.getRenderer().paint(g, subctx, min, max);
      
      if (dir == ctx.HORIZONTAL) {
        g.translate(0.0, -offset);
      } else {
        g.translate(-offset, 0.0);
      }
      
      offset += depth;
      }
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
    
  
  private static class LabelAndRenderer {
    private Object label;
    private SequenceRenderer renderer;
    
    private LabelAndRenderer(SequenceRenderer sr, Object l) {
      this.label = l;
      this.renderer = sr;
    }
    
    public Object getLabel() {
      return label;
    }
    
    public SequenceRenderer getRenderer() {
      return renderer;
    }
  }
}
