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
import java.lang.reflect.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.gui.*;

import java.util.List;

public class FeatureBlockSequenceRenderer
extends AbstractChangeable
implements SequenceRenderer {
  public static ChangeType FEATURE_RENDERER = new ChangeType(
    "The associated FeatureRenderer has changed",
    "org.biojava.bio.gui.sequence.FeatureBlockSequenceRenderer",
    "FEATURE_RENDERER",
    SequenceRenderContext.LAYOUT
  );
  
  private FeatureRenderer renderer;
  private transient ChangeForwarder rendForwarder;
  
  
  protected ChangeSupport getChangeSupport(ChangeType ct) {
    ChangeSupport cs = super.getChangeSupport(ct);
    
    if(rendForwarder == null) {
      rendForwarder = new SequenceRenderer.RendererForwarder(this, cs);
      if((renderer != null) && (renderer instanceof Changeable)) {
        Changeable c = (Changeable) this.renderer;
        c.addChangeListener(
          rendForwarder,
          SequenceRenderContext.REPAINT
        );
      }
    }
    
    return cs;
  }
  
  public FeatureBlockSequenceRenderer() {
    try {
      setFeatureRenderer(new BasicFeatureRenderer());
    } catch (ChangeVetoException cve) {
      throw new NestedError(cve, "Assertion Failure: Should have no listeners");
    }
  }
    
  public FeatureRenderer getFeatureRenderer() {
    return renderer;
  }

  public void setFeatureRenderer (FeatureRenderer renderer)
  throws ChangeVetoException {
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(FEATURE_RENDERER);
      synchronized(cs) {
        ChangeEvent ce = new ChangeEvent(
          this, FEATURE_RENDERER, this.renderer, renderer
        );
        cs.firePreChangeEvent(ce);
        if((this.renderer != null) && (this.renderer instanceof Changeable)) {
          Changeable c = (Changeable) this.renderer;
          c.removeChangeListener(rendForwarder);
        }
        this.renderer = renderer;
        if(renderer instanceof Changeable) {
          Changeable c = (Changeable) renderer;
          c.removeChangeListener(rendForwarder);
        }
        cs.firePostChangeEvent(ce);
      }
    } else {
      this.renderer = renderer;
    }
  }
  
  public double getDepth(SequenceRenderContext src, int min, int max) {
    Sequence sp = (Sequence) src.getSequence();
    FeatureFilter filter =
      new FeatureFilter.OverlapsLocation(new RangeLocation(min, max));
    FeatureHolder fh = sp.filter(filter, false);
    if(fh.countFeatures() > 0) {
      return renderer.getDepth(src);
    } else {
      return 0.0;
    }
  }
  
  public double getMinimumLeader(SequenceRenderContext src) {
    return 0.0;
  }
  
  public double getMinimumTrailer(SequenceRenderContext src) {
    return 0.0;
  }
  
  public void paint(
      Graphics2D g,
      SequenceRenderContext sp, 
      int min, int max
  ) {
    Shape oldClip = g.getClip();
    
    Rectangle2D clip = g.getClipBounds();
    Rectangle2D box = new Rectangle2D.Double();
    
    for(
      Iterator i = ((Sequence) sp.getSequence()).filter(
        new FeatureFilter.OverlapsLocation(new RangeLocation(min, max)), false
      ).features();
      i.hasNext();
    ) {
      Feature f = (Feature) i.next();
      Location l = f.getLocation();
      
      renderer.renderFeature(g, f, sp);
    }
  }

  public SequenceViewerEvent processMouseEvent(
    SequenceRenderContext src,
    MouseEvent me,
    List path,
    int min, int max
  ) {
    path.add(this);
    int pos;
    if(src.getDirection()==src.HORIZONTAL) {
      pos = src.graphicsToSequence(me.getX());
    } else {
      pos = src.graphicsToSequence(me.getY());
    }
    
    FeatureHolder hits = ((Sequence) src.getSequence()).filter(
      new FeatureFilter.OverlapsLocation(new PointLocation(pos)), false
    );
    
    hits = renderer.processMouseEvent(hits, src, me);
    
    return new SequenceViewerEvent(
      this,
      hits,
      pos,
      me,
      path
    );
  }
}

