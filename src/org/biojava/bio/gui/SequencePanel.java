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

public class SequencePanel extends JComponent implements SwingConstants {
    private Sequence sequence;
    private int direction;
    private double scale;

    private double insetBefore = 10;
    private double insetAfter = 10;

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
      scale = 15;

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

    public void setDirection(int dir) {
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

    public void paintComponent(Graphics g) {
      System.out.println("painting:");
	Graphics2D g2 = (Graphics2D) g;
	Rectangle2D oldClip = g2.getClipBounds();
	Rectangle2D.Double clip = new Rectangle2D.Double();
	Rectangle2D.Double actualClip = new Rectangle2D.Double();

	switch (direction) {
	case HORIZONTAL:
	    g2.translate(insetBefore, 0.0);
	    clip.width = insetBefore + insetAfter + scale * sequence.length();
	    break;
	case VERTICAL:
	    g2.translate(0.0, insetAfter);
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
      double acrossDim = 0.0;
      insetBefore = 0.0;
      insetAfter = 0.0;
      for (Iterator i = views.iterator(); i.hasNext(); ) {
        SequenceRenderer r = (SequenceRenderer) i.next();
        double depth = r.getDepth(this);
        depths.put(r, new Double(depth));
        acrossDim += depth;
        insetBefore += Math.max(insetBefore, r.getMinimumLeader(this));
        insetAfter += Math.max(insetAfter, r.getMinimumTrailer(this));
      }
      Dimension d = null;
      switch (direction) {
        case HORIZONTAL:
          d = new Dimension(alongDim, (int) acrossDim);
          break;
        case VERTICAL:
          d = new Dimension((int) acrossDim, alongDim);
          break;
      }
      
      setMinimumSize(d);
      setPreferredSize(d);
      revalidate();
    }

    private class RendererMonitor implements PropertyChangeListener {
	public void propertyChange(PropertyChangeEvent ev) {
	    invalidate();
	    repaint();
	}
    }
}

