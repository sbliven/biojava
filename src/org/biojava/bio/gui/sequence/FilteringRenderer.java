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

  public double getDepth(SequenceRenderContext src, int min, int max) {
    return super.getDepth(getContext(src), min, max);
  }    
  
  public double getMinimumLeader(SequenceRenderContext src) {
    return super.getMinimumLeader(getContext(src));
  }
  
  public double getMinimumTrailer(SequenceRenderContext src) {
    return super.getMinimumTrailer(getContext(src));
  }
  
  public void paint(
    Graphics2D g,
    SequenceRenderContext src,
    int min, int max
  ) {
    super.paint(g, getContext(src), min, max);
  }
  
  protected SequenceRenderContext getContext(SequenceRenderContext src) {
    if( (filter == FeatureFilter.all) && (recurse == true) ) {
      return src;
    } else {
      return new SubSequenceRenderContext(
        src,
        ((Sequence) src.getSequence()).filter(filter, recurse)
      );
    }
  }
}
