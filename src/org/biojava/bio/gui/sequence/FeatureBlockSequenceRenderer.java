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
import java.awt.geom.*;
import java.awt.font.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.gui.*;

import java.util.List;

public class FeatureBlockSequenceRenderer
implements SequenceRenderer, PropertyChangeListener {
  private static final AffineTransform FLIP =
    new AffineTransform(0.0, 1.0, -1.0, 0.0, 0.0, 0.0); 
  private double depth = 25.0;
  private FeatureRenderer renderer;
  private String label;
  private Shape labelGlyphH;
  private Shape labelGlyphV;
    
  protected PropertyChangeSupport pcs;

  public FeatureBlockSequenceRenderer() {
    pcs = new PropertyChangeSupport(this);
    setFeatureRenderer(new BasicFeatureRenderer());
  }
    
  public FeatureRenderer getFeatureRenderer() {
    return renderer;
  }

  public void setFeatureRenderer (FeatureRenderer r) {
    if (renderer != null) {
	    try {
        BeanInfo bi = Introspector.getBeanInfo(renderer.getClass());
        EventSetDescriptor[] esd = bi.getEventSetDescriptors();
        for (int i = 0; i < esd.length; ++i) {
          if (esd[i].getListenerType() == PropertyChangeListener.class) {
            Method alm = esd[i].getRemoveListenerMethod();
            Object[] args = { this };
            alm.invoke(renderer, args);
          }
        }
	    } catch (Exception ex) {
        ex.printStackTrace();
	    }
    }

    FeatureRenderer oldRenderer = renderer;
    renderer = r;

    try {
	    BeanInfo bi = Introspector.getBeanInfo(renderer.getClass());
	    EventSetDescriptor[] esd = bi.getEventSetDescriptors();
	    for (int i = 0; i < esd.length; ++i) {
        if (esd[i].getListenerType() == PropertyChangeListener.class) {
          Method alm = esd[i].getAddListenerMethod();
          Object[] args = { this };
          alm.invoke(renderer, args);
        }
	    }
    } catch (Exception ex) {
	    ex.printStackTrace();
    }

    pcs.firePropertyChange("featureRenderer", oldRenderer, renderer);
  }

  public void setDepth(double d) {
    double oldDepth = depth;
    depth = d;
    pcs.firePropertyChange("depth", new Double(oldDepth), new Double(d));
  }

  public double getDepth(SequenceRenderContext sp) {
    return depth;
  }

  public void setLabel(String label) {
    String oldLabel = this.label;
    this.label = label;
    this.labelGlyphH = null;
    this.labelGlyphV = null;
    pcs.firePropertyChange("label", oldLabel, this.label);
  }
    
  public String getLabel() {
    return label;
  }
    
  public double getMinimumLeader(SequenceRenderContext sp) {
    if(label == null) {
      return 0.0;
    }
    Font f = sp.getFont();
    FontRenderContext frc = new FontRenderContext(null, true, true);
    GlyphVector gv = f.createGlyphVector(frc, label);
    return gv.getVisualBounds().getWidth();
  }

  public double getMinimumTrailer(SequenceRenderContext sp) {
    return getMinimumLeader(sp);
  }

    public void paint(Graphics2D g, SequenceRenderContext sp, Rectangle2D seqBox) {
	Shape oldClip = g.getClip();

      if(label != null) {
        Rectangle2D.Double labelBox = null;
        Shape labelGlyph = null;
        SequenceRenderContext.Border leading = sp.getLeadingBorder();
        SequenceRenderContext.Border trailing = sp.getTrailingBorder();
        if (sp.getDirection() == sp.HORIZONTAL) {
          if(labelGlyphH == null) {
            Font font = sp.getFont();
            labelGlyphH = font.createGlyphVector(g.getFontRenderContext(), label).getOutline();
          }
          labelGlyph = labelGlyphH;
          labelBox = new Rectangle2D.Double(
            seqBox.getMinX() - leading.getSize(), seqBox.getMinY(),
            leading.getSize(), seqBox.getHeight()
          );
        } else {
          if(labelGlyphV == null) {
            Font font = sp.getFont().deriveFont(FLIP);
            labelGlyphV = font.createGlyphVector(g.getFontRenderContext(), label).getOutline();
          }
          labelGlyph = labelGlyphV;
          labelBox = new Rectangle2D.Double(
            seqBox.getMinX(), seqBox.getMinY() - leading.getSize(),
            seqBox.getWidth(), leading.getSize()
          );
        }
        renderLabel(g, labelGlyph, labelBox, sp, leading);
        if (sp.getDirection() == sp.HORIZONTAL) {
          labelBox = new Rectangle2D.Double(
            seqBox.getMaxX(), seqBox.getMinY(),
            leading.getSize(), seqBox.getHeight()
          );
        } else {
          labelBox = new Rectangle2D.Double(
            seqBox.getMinX(), seqBox.getMaxY(),
            seqBox.getWidth(), leading.getSize()
          );
        }
        renderLabel(g, labelGlyph, labelBox, sp, leading);
      }

      g.clip(seqBox);
      Rectangle2D clip = g.getClipBounds();
      Rectangle2D box = new Rectangle2D.Double();

      int minP;
      int maxP;
      int seqLen = sp.getSequence().length();
      if(sp.getDirection() == sp.HORIZONTAL) {
        minP = Math.max(1,      sp.graphicsToSequence(clip.getMinX()));
        maxP = Math.min(seqLen, sp.graphicsToSequence(clip.getMaxX()));
      } else {
        minP = Math.max(1,      sp.graphicsToSequence(clip.getMinY()));
        maxP = Math.min(seqLen, sp.graphicsToSequence(clip.getMaxY()));
      }
      
      for(
        Iterator i = ((Sequence) sp.getSequence()).features();
	      i.hasNext();
      )	{
        Feature f = (Feature) i.next();
        Location l = f.getLocation();

        if(l.getMin() > maxP || l.getMax() < minP) {
          continue;
        }

        double min = sp.sequenceToGraphics(l.getMin());
        double max = sp.sequenceToGraphics(l.getMax() + 1);

        if (sp.getDirection() == SequencePanel.HORIZONTAL) {
          box.setRect(min, 0.0, Math.max(1.0, max-min), depth);
        } else {
          box.setRect(0.0, min, depth, Math.max(1.0, max-min));
        }
        renderer.renderFeature(g, f, box, sp);
      }

      g.setClip(oldClip);
    }

    private void renderLabel(
      Graphics2D g,
      Shape gv, Rectangle2D labelBox,
      SequenceRenderContext sp, SequenceRenderContext.Border border
    ) {
      Rectangle2D bounds = gv.getBounds2D();
      double along = 0.0;
      double across = 0.0;
      if (sp.getDirection() == sp.HORIZONTAL) {
        across = labelBox.getCenterY() - bounds.getCenterY();
	int balign = border.getAlignment();
        
        if (balign == border.LEADING) 
            along = labelBox.getMinX() - bounds.getMinX();
        else if (balign == border.TRAILING)
            along = labelBox.getMaxX() - bounds.getMaxX();
        else if (balign == border.CENTER)
            along = labelBox.getCenterX() - bounds.getCenterX();

        AffineTransform at = g.getTransform();
        g.translate(along, across);
        g.fill(gv);
        g.draw(gv);
        g.setTransform(at);
      } else {
        across = labelBox.getCenterX() - bounds.getCenterX();
	int balign = border.getAlignment();

	if (balign == border.LEADING)
            along = labelBox.getMinY() - bounds.getMinY();
        else if (balign == border.TRAILING)
            along = labelBox.getMaxY() - bounds.getMaxY();
        else if (balign == border.CENTER)
            along = labelBox.getCenterY() - bounds.getCenterY();

        AffineTransform at = g.getTransform();
        g.translate(across, along);
        g.fill(gv);
        g.draw(gv);
        g.setTransform(at);
      }
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

    public void removePropertyChangeListener(String p,
					     PropertyChangeListener l) {
	pcs.removePropertyChangeListener(p, l);
    }

    public void propertyChange(PropertyChangeEvent ev) {
	pcs.firePropertyChange("featureRenderer", null, renderer);
    }
}

