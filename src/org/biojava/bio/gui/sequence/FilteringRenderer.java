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

public class FilteringRenderer
extends SequenceRendererWrapper {
  public static ChangeType FILTER = new ChangeType(
    "The filter has changed",
    "org.biojava.bio.gui.sequence.FilteringRenderer",
    "FILTER",
    SequenceRenderContext.LAYOUT
  );
  
  public static ChangeType RECURSE = new ChangeType(
    "The recurse flag has changed",
    "org.biojava.bio.gui.sequence.FilteringRenderer",
    "RECURSE",
    SequenceRenderContext.LAYOUT
  );

  protected FeatureFilter filter;
  protected boolean recurse;

  protected boolean hasListeners() {
    return super.hasListeners();
  }

  protected ChangeSupport getChangeSupport(ChangeType ct) {
    return super.getChangeSupport(ct);
  }
  
  public FilteringRenderer() {
    filter = FeatureFilter.all;
    recurse = false;
  }
  
  public FilteringRenderer(
    SequenceRenderer renderer,
    FeatureFilter filter,
    boolean recurse
  ) {
    super(renderer);
    try {
      setFilter(filter);
      setRecurse(recurse);
    } catch (ChangeVetoException cve) {
      throw new NestedError(cve, "Assertion Failure: Should have no listeners");
    }
  }
  
  public void setFilter(FeatureFilter filter)
  throws ChangeVetoException {
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(FILTER);
      synchronized(cs) {
        ChangeEvent ce = new ChangeEvent(
          this, FILTER, this.filter, filter
        );
        cs.firePreChangeEvent(ce);
        this.filter = filter;
        cs.firePostChangeEvent(ce);
      }
    } else {
      this.filter = filter;
    }
  }
  
  public FeatureFilter getFilter() {
    return this.filter;
  }
  
  public void setRecurse(boolean recurse)
  throws ChangeVetoException {
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(RECURSE);
      synchronized(cs) {
        ChangeEvent ce = new ChangeEvent(
          this, RECURSE, new Boolean(recurse), new Boolean(this.recurse)
        );
        cs.firePreChangeEvent(ce);
        this.recurse = recurse;
        cs.firePostChangeEvent(ce);
      }
    } else {
      this.recurse = recurse;
    }
  }
  
  public boolean getRecurse() {
    return this.recurse;
  }

  public double getDepth(SequenceRenderContext src, RangeLocation pos) {
    return super.getDepth(getContext(src, pos), pos);
  }    
  
  public double getMinimumLeader(SequenceRenderContext src, RangeLocation pos) {
    return super.getMinimumLeader(getContext(src, pos), pos);
  }
  
  public double getMinimumTrailer(SequenceRenderContext src, RangeLocation pos) {
    return super.getMinimumTrailer(getContext(src, pos), pos);
  }
  
  public void paint(
    Graphics2D g,
    SequenceRenderContext src,
    RangeLocation pos
  ) {
    super.paint(g, getContext(src, pos), pos);
  }
  
  public SequenceViewerEvent processMouseEvent(
    SequenceRenderContext src,
    MouseEvent me,
    List path,
    RangeLocation pos
  ) {
    return super.processMouseEvent(
      getContext(src, pos),
      me,
      path,
      pos
    );
  }
  
  private CacheMap contextCache = new FixedSizeMap(10);
  private Set flushers = new HashSet();
  
  protected SequenceRenderContext getContext(
    SequenceRenderContext src,
    RangeLocation pos
  ) {
    FeatureFilter actual = new FeatureFilter.And(
      filter,
      new FeatureFilter.OverlapsLocation(pos)
    );
    
    CtxtFilt gopher = new CtxtFilt(src, actual, recurse);
    SequenceRenderContext subSrc = (SequenceRenderContext) contextCache.get(gopher);
    if(subSrc == null) {
      subSrc = new SubSequenceRenderContext(
                src,
                ((Sequence) src.getSequence()).filter(actual, recurse)
      );
      contextCache.put(gopher, subSrc);
      CacheFlusher cf = new CacheFlusher(gopher);
      ((Sequence) src.getSequence()).addChangeListener(cf, FeatureHolder.FEATURES);
      flushers.add(cf);
    }
    
    return subSrc;
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
            FilteringRenderer.this,
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
