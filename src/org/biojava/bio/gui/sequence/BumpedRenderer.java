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
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import org.biojava.utils.*;
import org.biojava.utils.cache.*;
import org.biojava.bio.*;
import org.biojava.bio.gui.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * @author Matthew Pocock
 */
public class BumpedRenderer
extends SequenceRendererWrapper {
  private int leadingPixles;
  private int trailingPixles;

  public BumpedRenderer() {}
  
  public BumpedRenderer(SequenceRenderer renderer) {
    super(renderer);
  }

  public BumpedRenderer(SequenceRenderer renderer, int leading, int trailing) {
    super(renderer);
    this.leadingPixles = leading;
    this.trailingPixles = trailing;
  }

  public int getLeadingPixles() {
    return leadingPixles;
  }

  public void setLeadingPixles(int leading) {
    this.leadingPixles = leading;
  }

  public int getTrailingPixles() {
    return trailingPixles;
  }

  public void setTrailingPixles(int trailing) {
    this.trailingPixles = trailing;
  }

  protected boolean hasListeners() {
    return super.hasListeners();
  }

  protected ChangeSupport getChangeSupport(ChangeType ct) {
    return super.getChangeSupport(ct);
  }
  
  public double getDepth(SequenceRenderContext src) {
    List layers = layer(src);
    return LayeredRenderer.INSTANCE.getDepth(
      layers,
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
    SequenceRenderContext src
  ) {
    List layers = layer(src);
    LayeredRenderer.INSTANCE.paint(
      g,
      layers,
      Collections.nCopies(layers.size(), getRenderer())
    );
  }
  
  public SequenceViewerEvent processMouseEvent(
    SequenceRenderContext src,
    MouseEvent me,
    List path
  ) {
    path.add(this);
    List layers = layer(src);
    SequenceViewerEvent sve = LayeredRenderer.INSTANCE.processMouseEvent(
      layers,
      me,
      path,
      Collections.nCopies(layers.size(), getRenderer())
    );
    
    if(sve == null) {
      sve = new SequenceViewerEvent(
        this,
        null,
        src.graphicsToSequence(me.getPoint()),
        me,
        path
      );
    }
    
    return sve;
  }

  private CacheMap contextCache = new FixedSizeMap(5);
  private Set flushers = new HashSet();
  
  protected List layer(SequenceRenderContext src) {
    FeatureFilter filt = new FeatureFilter.OverlapsLocation(src.getRange());
    CtxtFilt gopher = new CtxtFilt(src, filt, false);
    List layers = (List) contextCache.get(gopher);
    if(layers == null) {
      layers = doLayer(src, filt);
      contextCache.put(gopher, layers);
      CacheFlusher cf = new CacheFlusher(gopher);
      ((Changeable) src.getSymbols()).addChangeListener(cf, FeatureHolder.FEATURES);
      flushers.add(cf);
    }
    
    return layers;
  }
  
  protected List doLayer(SequenceRenderContext src, FeatureFilter filt) {
    FeatureHolder features = src.getFeatures();
    List layers = new ArrayList();
    List layerLocs = new ArrayList();
    int lead = (int) (leadingPixles / src.getScale());
    int trail = (int) (trailingPixles / src.getScale());

    for(
      Iterator fi = features.filter(
        filt, false
      ).features();
      fi.hasNext();
    ) {
      Feature f = (Feature) fi.next();
      try {
        Location fLoc = f.getLocation();
        fLoc = new RangeLocation(fLoc.getMin() - lead, fLoc.getMax() + trail);
        
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
        fhLayer.addFeature(f);
      } catch (ChangeVetoException cve) {
        throw new BioError(cve, "Pants");
      } catch (Throwable t) {
        throw new NestedError(t, "Could not bump feature: " + f);
      }
    }
    
    List contexts = new ArrayList(layers.size());
    for(Iterator i = layers.iterator(); i.hasNext(); ) {
      FeatureHolder layer = (FeatureHolder) i.next();
      contexts.add(new SubSequenceRenderContext(
        src,
        null,
        layer,
        null
      ));
    }
    
    return contexts;
  }

  private class CacheFlusher implements ChangeListener {
    private CtxtFilt ctxtFilt;
    
    public CacheFlusher(CtxtFilt ctxtFilt) {
      this.ctxtFilt = ctxtFilt;
    }
    
    public void preChange(ChangeEvent ce) {
    }
    
    public void postChange(ChangeEvent ce) {
      contextCache.remove(ctxtFilt);
      flushers.remove(this);
      
      if(hasListeners()) {
        ChangeSupport cs = getChangeSupport(SequenceRenderContext.LAYOUT);
        synchronized(cs) {
          ChangeEvent ce2 = new ChangeEvent(
            BumpedRenderer.this,
            SequenceRenderContext.LAYOUT
          );
          cs.firePostChangeEvent(ce2);
        }
      }
    }
  }
  
  private class CtxtFilt {
    private SequenceRenderContext src;
    private FeatureFilter filter;
    private boolean recurse;
    
    public CtxtFilt(SequenceRenderContext src, FeatureFilter filter, boolean recurse) {
      this.src = src;
      this.filter = filter;
      this.recurse = recurse;
    }
    
    public boolean equals(Object o) {
      if(! (o instanceof CtxtFilt) ) {
        return false;
      }
      CtxtFilt that = (CtxtFilt) o;
      return
        src.equals(that.src) &&
        filter.equals(that.filter) && 
        (recurse == that.recurse);
    }
    
    public int hashCode() {
      return src.hashCode() ^ filter.hashCode();
    }
  }
}
