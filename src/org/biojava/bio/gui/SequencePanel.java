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

package org.biojava.bio.gui;

import java.util.*;
import java.util.List; // usefull trick to 'hide' javax.swing.List
import java.beans.*;
import java.lang.reflect.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.gui.sequence.*;

/**
 * A panel that visualy displays a Sequence.
 * <P>
 * A SequencePanel can either display the sequence from left-to-right
 * (HORIZONTAL) or from top-to-bottom (VERTICAL). It has an associated scale
 * which is the number of pixels per symbol. It also has a lines property that
 * controls how to wrap the sequence off one end and onto the other.
 * <P>
 * Each line in the SequencePanel is broken down into a list of strips,
 * each rendered by an individual SequenceRenderer object.
 * You could add a SequenceRenderer that draws on genes, another that
 * draws repeats and another that prints out the DNA sequence. They are
 * responsible for rendering their view of the sequence in the place that the
 * SequencePanel positions them.  
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */
public class SequencePanel extends JComponent implements SwingConstants {
  private Sequence sequence;
  private int direction;
  private double scale;
  private int lines;

  private double insetBefore = 10;
  private double insetAfter = 10;
  private double alongDim = 0.0;
  private double acrossDim = 0.0;
  private double lineDepth = 0.0;
  private int realLines = 0;

  private List views;
  private Map depths;

  private RendererMonitor theMonitor;

  /**
   * Initializer.
   */

  {
    views = new ArrayList();
    depths = new HashMap();
    direction = HORIZONTAL;
    scale = 12.0;
    lines = 1;

    theMonitor = new RendererMonitor();
  }

  public SequencePanel() {
    super();
    this.addPropertyChangeListener(theMonitor);
  }
    
  public void setSequence(Sequence s) {
    Sequence oldSequence = sequence;
    this.sequence = s;
    resizeAndValidate();
    firePropertyChange("sequence", oldSequence, s);
  }

  public Sequence getSequence() {
    return sequence;
  }

  public void setDirection(int dir) 
  throws IllegalArgumentException {
    if(dir != HORIZONTAL && dir != VERTICAL) {
      throw new IllegalArgumentException(
        "Direction must be either HORIZONTAL or VERTICAL"
      );
    }
    int oldDirection = direction;
    direction = dir;
    resizeAndValidate();
    firePropertyChange("direction", oldDirection, direction);
  }

  public int getDirection() {
    return direction;
  }

  public void setScale(double scale) {
    double oldScale = this.scale;
    this.scale = scale;
    resizeAndValidate();
    firePropertyChange("scale", oldScale, scale);
  }

  public double getScale() {
    return scale;
  }

  public void setLines(int lines) {
    int oldLines = this.lines;
    this.lines = lines;
    resizeAndValidate();
    firePropertyChange("lines", oldLines, lines);
  }
  
  public int getLines() {
    return lines;
  }
  
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;

    Rectangle2D.Double boxClip = new Rectangle2D.Double();
    switch (direction) {
      case HORIZONTAL:
        boxClip.width = alongDim;
        boxClip.height = acrossDim;
        break;
      case VERTICAL:
        boxClip.width = acrossDim;
        boxClip.height = alongDim;
        break;
    }
    
    g2.clip(boxClip);
    Rectangle2D newClip = g2.getClip().getBounds2D();
    
    int minLine = 0; 
    int maxLine = realLines;
    Rectangle2D.Double clip = new Rectangle2D.Double();
    switch (direction) {
      case HORIZONTAL:
        clip.width = insetBefore + insetAfter + scale * sequence.length();
        minLine = (int) Math.max(minLine, Math.floor(newClip.getMinY()/lineDepth));
        maxLine = (int) Math.min(maxLine, Math.ceil(newClip.getMaxY()/lineDepth));
        g2.translate(-minLine * alongDim, minLine * lineDepth);
        break;
      case VERTICAL:
        clip.height = insetBefore + insetAfter + scale * sequence.length();
        minLine = (int) Math.max(minLine, Math.ceil(newClip.getMinX()/lineDepth));
        maxLine = (int) Math.min(maxLine, Math.ceil(newClip.getMaxX()/lineDepth));
        g2.translate(minLine * lineDepth, -minLine * alongDim);
        break;
    }

    for(int l = minLine; l < maxLine; l++) {
      for (Iterator i = views.iterator(); i.hasNext(); ) {
        SequenceRenderer r = (SequenceRenderer) i.next();
        double depth = ((Double) depths.get(r)).doubleValue();

        switch(direction) {
          case HORIZONTAL:
            clip.height = depth;
            break;
	        case VERTICAL:
            clip.width = depth;
            break;
        }
        
        Shape oldClip = g2.getClip();
        g2.clip(clip);
        r.paint(g2, this);
        g2.setClip(oldClip);

        switch (direction) {
          case HORIZONTAL:
            g2.translate(0.0, depth);
            break;
          case VERTICAL:
            g2.translate(depth, 0.0);
            break;
        }
      }
      switch (direction) {
        case HORIZONTAL:
          g2.translate(-alongDim, 0.0);
          break;
        case VERTICAL:
          g2.translate(0.0, -alongDim);
          break;
      }
    }
  }

  public void addRenderer(SequenceRenderer r) {
    try {
	    BeanInfo bi = Introspector.getBeanInfo(r.getClass());
	    EventSetDescriptor[] esd = bi.getEventSetDescriptors();
	    for (int i = 0; i < esd.length; ++i) {
        if (esd[i].getListenerType() == PropertyChangeListener.class) {
          Method alm = esd[i].getAddListenerMethod();
          Object[] args = { theMonitor };
          alm.invoke(r, args);
        }
	    }
    } catch (Exception ex) {
	    ex.printStackTrace();
    }

    views.add(r);
    resizeAndValidate();
  }

  public double sequenceToGraphics(int seqPos) {
    return ((seqPos-1) * scale) + insetBefore;
  }

  public int graphicsToSequence(double gPos) {
    return (int) ((gPos - insetBefore) / scale) + 1;
  }

  public void resizeAndValidate() {
    alongDim = 
      scale * sequence.length() + 
      insetBefore + insetAfter;
    acrossDim = 0.0;
    insetBefore = 0.0;
    insetAfter = 0.0;
    for (Iterator i = views.iterator(); i.hasNext(); ) {
      SequenceRenderer r = (SequenceRenderer) i.next();
      double depth = r.getDepth(this);
      depths.put(r, new Double(depth));
      acrossDim += depth;
      insetBefore = Math.max(insetBefore, r.getMinimumLeader(this));
      insetAfter = Math.max(insetAfter, r.getMinimumTrailer(this));
    }
    lineDepth = acrossDim;

    Dimension d = null;    
    if(lines < 1) {
      // fit to component size for across, and wrap as many times as is needed
      // to accomodate whole sequence;
      Dimension parentSize = getParent().getSize();
      int width = 0;
      switch (direction) {
        case HORIZONTAL:
          width = parentSize.width;
          break;
        case VERTICAL:
          width = parentSize.height;
          break;
      }
      realLines = (int) Math.ceil((double) alongDim / (double) width);
      acrossDim = acrossDim * realLines;
      alongDim = Math.ceil((double) width + insetBefore + insetAfter);
      switch (direction) {
        case HORIZONTAL:
          d = new Dimension((int) alongDim, (int) acrossDim);
          break;
        case VERTICAL:
          d = new Dimension((int) acrossDim, (int) alongDim);
          break;
      }
    } else {
      // fit depth to lines*acrossDim and make as wide as necisary to accomodoate the
      // whole sequence
      realLines = lines;
      alongDim = Math.ceil(
                         alongDim / (double) lines +
                         insetBefore + insetAfter
                      );
      acrossDim = Math.ceil((double) lines * acrossDim);  
      switch (direction) {
        case HORIZONTAL:
          d = new Dimension((int) alongDim, (int) acrossDim);
          break;
        case VERTICAL:
          d = new Dimension((int) acrossDim, (int) alongDim);
          break;
      }
    }
    
    setMinimumSize(d);
    setPreferredSize(d);
    revalidate();
  }

  private class RendererMonitor implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent ev) {
	    repaint();
    }
  }
}

