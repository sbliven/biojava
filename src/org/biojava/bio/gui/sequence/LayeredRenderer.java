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
import java.util.*;
import java.util.List;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.gui.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

public class LayeredRenderer
extends AbstractForwarder
implements SequenceRenderer {
  public static final ChangeType RENDERER = new ChangeType(
    "The renderer has changed.",
    "org.biojava.bio.gui.sequence.LayeredRenderer",
    "RENDERER"
  );
  
  private SequenceRenderer lineRenderer;

  public LayeredRenderer() {
  }
  
  public LayeredRenderer(SequenceRenderer lineRenderer) {
    try {
      setLineRenderer(lineRenderer);
    } catch (ChangeVetoException cve) {
      throw new BioError(cve, "Assertion Failure: Should have no listeners");
    }
  }
  
  public void setLineRenderer(SequenceRenderer lineRenderer)
  throws ChangeVetoException {
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(RENDERER);
      synchronized(cs) {
        ChangeEvent ce = new ChangeEvent(
          this, SequenceRenderContext.LAYOUT,
          null, null, new ChangeEvent(
            this, RENDERER, lineRenderer, this.lineRenderer
          )
        );
        cs.firePreChangeEvent(ce);
        this.lineRenderer = lineRenderer;
        cs.firePostChangeEvent(ce);
      }
    } else {
      this.lineRenderer = lineRenderer;
    }
  }
  
  public SequenceRenderer getLineRenderer() {
    return this.lineRenderer;
  }
  
  public double getDepth(SequenceRenderContext src, int min, int max) {
    double depth = 0.0;
    List layers = layer((Sequence) src.getSequence());

    for(Iterator i = layers.iterator(); i.hasNext(); ) {
      FeatureHolder layer = (FeatureHolder) i.next();
      SequenceRenderContext subsrc = new SubSequenceRenderContext(
        src,
        layer
      );
      depth += getLineRenderer().getDepth(subsrc, min, max);
    }
    
    return depth;
  }
  
  public double getMinimumLeader(SequenceRenderContext src) {
    return getLineRenderer().getMinimumLeader(src);
  }
  
  public double getMinimumTrailer(SequenceRenderContext src) {
    return getLineRenderer().getMinimumTrailer(src);
  }
  
  public void paint(
    Graphics2D g,
    SequenceRenderContext src,
    int min, int max
  ) {
    List layers = layer((Sequence) src.getSequence());
    SequenceRenderer sr = getLineRenderer();
    double offset = 0.0;
    double depth = sr.getDepth(src, min, max);
    
    for(Iterator i = layers.iterator(); i.hasNext(); ) {
      FeatureHolder layer = (FeatureHolder) i.next();
      
      SequenceRenderContext subsrc = new SubSequenceRenderContext(
        src,
        layer
      );
      
      int dir = src.getDirection();
      if(dir == src.HORIZONTAL) {
        g.translate(0.0, offset);
      } else {
        g.translate(offset, 0.0);
      }
      
      sr.paint(g, subsrc, min, max);
      
      if(dir == src.HORIZONTAL) {
        g.translate(0.0, -offset);
      } else {
        g.translate(-offset, 0.0);
      }
      
      offset += depth;
    }
  }
  
  protected List layer(Sequence seq) {
    List layers = new ArrayList();
    List layerLocs = new ArrayList();
    
    for(Iterator fi = seq.features(); fi.hasNext(); ) {
      Feature f = (Feature) fi.next();
      Location fLoc = f.getLocation();
      if(!fLoc.isContiguous()) {
        fLoc = new RangeLocation(fLoc.getMin(), fLoc.getMax());
      }
      Iterator li = layerLocs.iterator();
      Iterator fhI = layers.iterator();
      SimpleFeatureHolder fhLayer = null;
      List listLayer = null;
    LAYER:
      while(li.hasNext()) {
        List l = (List) li.next();
        SimpleFeatureHolder fh = (SimpleFeatureHolder) fhI.next();
        for(Iterator locI = l.iterator(); locI.hasNext(); ) {
          Location loc = (Location) locI.next();
          if(loc.overlaps(fLoc)) {
            continue LAYER;
          }
        }
        listLayer = l;
        fhLayer = fh;
        break;
      }
      if(listLayer == null) {
        layerLocs.add(listLayer = new ArrayList());
        layers.add(fhLayer = new SimpleFeatureHolder());
      }
      listLayer.add(fLoc);
      try {
        fhLayer.addFeature(f);
      } catch (ChangeVetoException cve) {
        throw new BioError(cve, "Pants");
      }
    }
    return layers;
  }
}
