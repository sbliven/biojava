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

public class FilteringRenderer implements SequenceRenderer {
  protected PropertyChangeSupport pcs;
  protected SequenceRenderer lineRenderer;
  protected FeatureFilter filter;
  protected boolean recurse;

  public FilteringRenderer() {
    pcs = new PropertyChangeSupport(this);
    filter = FeatureFilter.all;
    recurse = false;
  }
  
  public void addPropertyChangeListener(PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  public void addPropertyChangeListener(String p, PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  public void removePropertyChangeListener(PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }

  public void removePropertyChangeListener(
    String p, PropertyChangeListener l
  ) {
	  pcs.removePropertyChangeListener(p, l);
  }
  

  public void setLineRenderer(SequenceRenderer lineRenderer) {
    SequenceRenderer old = this.lineRenderer;
    this.lineRenderer = lineRenderer;
    pcs.firePropertyChange("lineRenderer", old, lineRenderer);
  }
  
  public SequenceRenderer getLineRenderer() {
    return this.lineRenderer;
  }

  public void setFilter(FeatureFilter filter) {
    FeatureFilter old = this.filter;
    this.filter = filter;
    pcs.firePropertyChange("filter", old, filter);
  }
  
  public FeatureFilter getFilter() {
    return this.filter;
  }
  
  public void setRecurse(boolean recurse) {
    boolean old = this.recurse;
    this.recurse = recurse;
    pcs.firePropertyChange("recurse", old, recurse);
  }
  
  public boolean getRecurse() {
    return this.recurse;
  }

  public double getDepth(SequenceRenderContext src) {
    return getLineRenderer().getDepth(getContext(src));
  }    
  
  public double getMinimumLeader(SequenceRenderContext src) {
    return getLineRenderer().getMinimumLeader(getContext(src));
  }
  
  public double getMinimumTrailer(SequenceRenderContext src) {
    return getLineRenderer().getMinimumTrailer(getContext(src));
  }
  
  public void paint(
    Graphics2D g,
    SequenceRenderContext src,
    Rectangle2D seqBox
  ) {
    getLineRenderer().paint(g, getContext(src), seqBox);
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
