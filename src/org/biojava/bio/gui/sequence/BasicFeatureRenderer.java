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
import java.beans.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.gui.*;

import java.awt.*;
import java.awt.geom.*;

import java.util.List;

public class BasicFeatureRenderer implements FeatureRenderer {
  private Paint fill;
  private Paint outline;
  private float arrowSize = 15.0f;
  private float arrowScoop = 4.0f;

  protected PropertyChangeSupport pcs;

  public BasicFeatureRenderer() {
    fill = Color.red;
    outline = Color.black;
    pcs = new PropertyChangeSupport(this);
  }

  public void setFill(Paint p) {
    Paint oldFill = fill;
    fill = p;
    pcs.firePropertyChange("fill", oldFill, fill);
  }

    public Paint getFill() {
	return fill;
    }

    public void setOutline(Paint p) {
	Paint oldOutline = outline;
	outline = p;
	pcs.firePropertyChange("outline", oldOutline, outline);
    }

    public Paint getOutline() {
      return outline;
    }
    
    public void setArrowSize(float arrowSize) {
      float oldArrowSize = this.arrowSize;
      this.arrowSize = arrowSize;
      pcs.firePropertyChange(
        "arrowSize",
        new Float(oldArrowSize),
        new Float(arrowSize)
      ); 
    }
    
    public float getArrowSize() {
      return arrowSize;
    }
    
    public void setArrowScoop(float arrowScoop) {
      float oldArrowScoop = this.arrowScoop;
      this.arrowScoop = arrowScoop;
      pcs.firePropertyChange(
        "arrowScoop", 
        new Float(oldArrowScoop),
        new Float(oldArrowScoop)
      ); 
    }
    
    public float getArrowScoop() {
      return arrowScoop;
    }
    
    public void renderFeature(
      Graphics2D g,
      Feature f, 
      Rectangle2D box,
      SequenceRenderContext context
    ) {
      Shape s = box;
      if (f instanceof StrandedFeature) {
        StrandedFeature.Strand strand = ((StrandedFeature) f).getStrand();
        if(context.getDirection() == context.HORIZONTAL) {
          if(box.getWidth() >= arrowSize && box.getHeight() >= arrowScoop*2.0) {
            float minY = (float) box.getMinY();
            float maxY = (float) box.getMaxY();
            float minYS = minY + arrowScoop;
            float maxYS = maxY - arrowScoop;
            float midY = (minY + maxY) * 0.5f;
            float minX = (float) box.getMinX();
            float maxX = (float) box.getMaxX();
            if(strand == StrandedFeature.POSITIVE) {
              float midX = maxX - arrowSize;
              GeneralPath path = new GeneralPath();
              path.moveTo(minX, minYS);
              path.lineTo(midX, minYS);
              path.lineTo(midX, minY);
              path.lineTo(maxX, midY);
              path.lineTo(midX, maxY);
              path.lineTo(midX, maxYS);
              path.lineTo(minX, maxYS);
              path.closePath();
              s = path;
            } else if(strand == StrandedFeature.NEGATIVE) {
              float midX = minX + arrowSize;
              GeneralPath path = new GeneralPath();
              path.moveTo(maxX, minYS);
              path.lineTo(midX, minYS);
              path.lineTo(midX, minY);
              path.lineTo(minX, midY);
              path.lineTo(midX, maxY);
              path.lineTo(midX, maxYS);
              path.lineTo(maxX, maxYS);
              path.closePath();
              s = path;
            }
          }
        } else { // vertical
          if(box.getHeight() >= arrowSize && box.getWidth() >= arrowScoop*2.0) {
            float minX = (float) box.getMinX();
            float maxX = (float) box.getMaxX();
            float minXS = minX + arrowScoop;
            float maxXS = maxX - arrowScoop;
            float midX = (minX + maxX) * 0.5f;
            float minY = (float) box.getMinY();
            float maxY = (float) box.getMaxY();
            if(strand == StrandedFeature.POSITIVE) {
              float midY = maxY - arrowSize;
              GeneralPath path = new GeneralPath();
              path.moveTo(minXS, minY);
              path.lineTo(minXS, midY);
              path.lineTo(minX, midY);
              path.lineTo(midX, maxY);
              path.lineTo(maxX, midY);
              path.lineTo(maxXS, midY);
              path.lineTo(maxXS, minY);
              path.closePath();
              s = path;
            } else if(strand == StrandedFeature.NEGATIVE) {
              float midY = minX + arrowSize;
              GeneralPath path = new GeneralPath();
              path.moveTo(minXS, maxY);
              path.lineTo(minXS, midY);
              path.lineTo(minX, midY);
              path.lineTo(midX, minY);
              path.lineTo(maxX, midY);
              path.lineTo(maxXS, midY);
              path.lineTo(maxXS, maxY);
              path.closePath();
              s = path;
            }
          }
        }
      }
      g.setPaint(fill);
      g.fill(s);
      if (outline != fill) {
        g.setPaint(outline);
        g.draw(s);
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
}
