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
  private double acrossDim = 0;;
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
  }
    
  public void setSequence(Sequence s) {
    Sequence oldSequence = sequence;
    this.sequence = s;
    firePropertyChange("sequence", oldSequence, s);
    resizeAndValidate();
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
    firePropertyChange("direction", oldDirection, direction);
    resizeAndValidate();
  }

  public int getDirection() {
    return direction;
  }

  public void setScale(double scale) {
    double oldScale = this.scale;
    this.scale = scale;
    firePropertyChange("scale", oldScale, scale);
    resizeAndValidate();
  }

  public double getScale() {
    return scale;
  }

  public void setLines(int lines) {
    int oldLines = this.lines;
    this.lines = lines;
    firePropertyChange("lines", oldLines, lines);
    resizeAndValidate();
  }
  
  public int getLines() {
    return lines;
  }
  
  public void paintComponent(Graphics g) {
    System.out.println("painting:");
    Graphics2D g2 = (Graphics2D) g;
    Rectangle2D oldClip = g2.getClipBounds();
    Rectangle2D.Double clip = new Rectangle2D.Double();
    Rectangle2D.Double actualClip = new Rectangle2D.Double();

    for(int l = 0; l < realLines; l++) {
      switch (direction) {
        case HORIZONTAL:
          g2.translate(insetBefore, l*acrossDim);
          clip.width = insetBefore + insetAfter + scale * sequence.length();
          break;
        case VERTICAL:
          g2.translate(l*acrossDim, insetAfter);
          clip.height = insetBefore + insetAfter + scale * sequence.length();
          break;
      }

      for (Iterator i = views.iterator(); i.hasNext(); ) {
        SequenceRenderer r = (SequenceRenderer) i.next();
        System.out.println("\t" + r);
        double depth = ((Double) depths.get(r)).doubleValue();

        switch(direction) {
          case HORIZONTAL:
            clip.height = depth;
            break;
	        case VERTICAL:
              clip.width = depth;
              break;
        }
        Rectangle2D.intersect(clip, oldClip, actualClip);
        g2.setClip(actualClip);
	    
	      r.paint(g2, this);

        switch (direction) {
          case HORIZONTAL:
            g2.translate(0.0, depth);
            break;
          case VERTICAL:
            g2.translate(depth, 0.0);
            break;
        }
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
    int alongDim = (int) (
      scale * sequence.length() + 
      insetBefore + insetAfter
    );
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

    Dimension d = null;    
    if(lines < 1) {
      // fit to component size for across, and wrap as many times as is needed
      // to accomodate whole sequence;
      Dimension parentSize = getSize();
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
      int height = (int) (acrossDim * realLines);
      width = (int) Math.ceil((double) width + insetBefore + insetAfter);
      switch (direction) {
        case HORIZONTAL:
          d = new Dimension(width, height);
          break;
        case VERTICAL:
          d = new Dimension(height, width);
          break;
      }
    } else {
      // fit depth to lines*acrossDim and make as wide as necisary to accomodoate the
      // whole sequence
      realLines = lines;
      int alongDimD = (int) Math.ceil(
                         alongDim / (double) lines +
                         insetBefore + insetAfter
                      );
      int acrossDimD = (int) Math.ceil((double) lines * acrossDim);  
      switch (direction) {
        case HORIZONTAL:
          d = new Dimension(alongDimD, acrossDimD);
          break;
        case VERTICAL:
          d = new Dimension(acrossDimD, alongDimD);
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

