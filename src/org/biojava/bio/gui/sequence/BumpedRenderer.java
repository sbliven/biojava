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

public class BumpedRenderer
extends SequenceRendererWrapper {
  public BumpedRenderer() {}
  
  public BumpedRenderer(SequenceRenderer renderer) {
    super(renderer);
  }
  
  public double getDepth(SequenceRenderContext src, int min, int max) {
    List layers = layer(src);
    return LayeredRenderer.INSTANCE.getDepth(
      layers,
      min, max,
      Collections.nCopies(layers.size(), getRenderer())
    );
  }
  
  public double getMinimumLeader(SequenceRenderContext src) {
    List layers = layer(src);
    return LayeredRenderer.INSTANCE.getMinimumLeader(
      layers,
      Collections.nCopies(layers.size(), getRenderer())
    );
  }
  
  public double getMinimumTrailer(SequenceRenderContext src) {
    List layers = layer(src);
    return LayeredRenderer.INSTANCE.getMinimumTrailer(
      layers,
      Collections.nCopies(layers.size(), getRenderer())
    );
  }
  
  public void paint(
    Graphics2D g,
    SequenceRenderContext src,
    int min, int max
  ) {
    List layers = layer(src);
    LayeredRenderer.INSTANCE.paint(
      g,
      layers,
      min, max,
      Collections.nCopies(layers.size(), getRenderer())
    );
  }
  
  protected List layer(SequenceRenderContext src) {
    Sequence seq = (Sequence) src.getSequence();
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
    
    List contexts = new ArrayList(layers.size());
    for(Iterator i = layers.iterator(); i.hasNext(); ) {
      FeatureHolder layer = (FeatureHolder) i.next();
      contexts.add(new SubSequenceRenderContext(
        src,
        layer
      ));
    }
    
    return contexts;
  }
}
