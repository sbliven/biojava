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

public class LayeredRenderer implements SequenceRenderer {
  protected PropertyChangeSupport pcs;
  protected FeatureFilter filter;
  protected boolean recurse;
  
  private SequenceRenderer lineRenderer;

  public LayeredRenderer() {
    pcs = new PropertyChangeSupport(this);
    //filter = FeatureFilter.AcceptAllFilter;
    //recurse = false;
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
    List layers = layer((Sequence) src.getSequence());
    return getLineRenderer().getDepth(src) * ((double) layers.size());
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
    Rectangle2D seqBox
  ) {
    List layers = layer((Sequence) src.getSequence());
    SequenceRenderer sr = getLineRenderer();
    double offset = 0.0;
    double depth = sr.getDepth(src);
    Rectangle2D subSeqBox = new Rectangle2D.Double();
    
    for(Iterator i = layers.iterator(); i.hasNext(); ) {
      FeatureHolder layer = (FeatureHolder) i.next();
      
      SequenceRenderContext subsrc = new SubSequenceRenderContext(
        src,
        layer
      );
      
      int dir = src.getDirection();
      if(dir == src.HORIZONTAL) {
        g.translate(0.0, offset);
        subSeqBox.setRect(
          seqBox.getX(), seqBox.getY(),
          seqBox.getWidth(), depth
        );
      } else {
        g.translate(offset, 0.0);
        subSeqBox.setRect(
          seqBox.getX(), seqBox.getY(),
          depth, seqBox.getHeight()
        );
      }
      
      sr.paint(g, subsrc, subSeqBox);
      
      if(dir == src.HORIZONTAL) {
        g.translate(0.0, -offset);
      } else {
        g.translate(-offset, 0.0);
      }
      
      offset += depth;
    }
  }
  
  protected List layer(Sequence seq) {
    FeatureHolder filtered = seq.filter(filter, recurse);
    List layers = new ArrayList();
    List layerLocs = new ArrayList();
    
    for(Iterator fi = filtered.features(); fi.hasNext(); ) {
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
  
  private static class SubSequenceRenderContext
  implements SequenceRenderContext {
    private final Sequence seq;
    private final SequenceRenderContext src;
    
    public SubSequenceRenderContext(
      SequenceRenderContext src, FeatureHolder fh 
    ) {
      this.src = src;
      this.seq = new PartialSequence((Sequence) src.getSequence(), fh);
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
    
    public SymbolList getSequence() {
      return seq;
    }
    
    public Border getLeadingBorder() {
      return src.getLeadingBorder();
    }
    
    public Border getTrailingBorder() {
      return src.getTrailingBorder();
    }
    
    public Font getFont() {
      return src.getFont();
    }
  }
  
  private static class PartialSequence implements Sequence {
    private final Sequence seq;
    private final FeatureHolder fh;
    
    public PartialSequence(Sequence seq, FeatureHolder fh) {
      this.seq = seq;
      this.fh = fh;
    }
    
    public String getURN() {
      return seq.getURN();
    }
    
    public String getName() {
      return seq.getName();
    }
    
    public Annotation getAnnotation() {
      return seq.getAnnotation();
    }
    
    public int countFeatures() {
      return fh.countFeatures();
    }
    
    public Iterator features() {
      return fh.features();
    }
    
    public FeatureHolder filter(FeatureFilter fc, boolean recurse) {
      return fh.filter(fc, recurse);
    }
    
    public Feature createFeature(Feature.Template ft)
    throws BioException, ChangeVetoException {
      return fh.createFeature(ft);
    }
    
    public void removeFeature(Feature f)
    throws ChangeVetoException {
      fh.removeFeature(f);
    }
    
    public Alphabet getAlphabet() {
      return seq.getAlphabet();
    }
    
    public int length() {
      return seq.length();
    }
    
    public Symbol symbolAt(int index) throws IndexOutOfBoundsException {
      return seq.symbolAt(index);
    }
    
    public List toList() {
      return seq.toList();
    }
    
    public Iterator iterator() {
      return seq.iterator();
    }
    
    public SymbolList subList(int start, int end)
    throws IndexOutOfBoundsException {
      return seq.subList(start, end);
    }
    
    public String seqString() {
      return seq.seqString();
    }
    
    public String subStr(int start, int end)
    throws IndexOutOfBoundsException {
      return seq.subStr(start, end);
    }
    
    public void edit(Edit edit)
    throws IndexOutOfBoundsException, IllegalAlphabetException,
    ChangeVetoException {
      seq.edit(edit);
    }
    
    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
  }
}
