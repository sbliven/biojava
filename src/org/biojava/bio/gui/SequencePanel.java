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
import java.beans.*;
import java.lang.reflect.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.gui.sequence.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.List;

public class SequencePanel extends JComponent {
    public final static int HORIZONTAL = 1;
    public final static int VERTICAL = 2;

    private Sequence sequence;
    private int direction;
    private double scale;

    private double insetBefore = 10;
    private double insetAfter = 10;

    private List views;
    private Map depths;

    private Dimension ourSize;

    private RendererMonitor theMonitor;

    /**
     * Initializer.
     */

    {
	views = new ArrayList();
	depths = new HashMap();
	direction = HORIZONTAL;
	scale = 15;

	ourSize = new Dimension(1, 1);

	theMonitor = new RendererMonitor();
    }

    public SequencePanel() {
	super();
    }
    
    public void setSequence(Sequence s) {
	Sequence oldSequence = sequence;
	this.sequence = s;
	firePropertyChange("sequence", oldSequence, s);
	invalidate();
    }

    public Sequence getSequence() {
	return sequence;
    }

    public void setDirection(int dir) {
        int oldDirection = direction;
	direction = dir;
	firePropertyChange("direction", oldDirection, direction);
	invalidate();
    }

    public int getDirection() {
	return direction;
    }

    public void setScale(double scale) {
	double oldScale = this.scale;
	this.scale = scale;
	firePropertyChange("scale", oldScale, scale);
	invalidate();
    }

    public double getScale() {
	return scale;
    }

    public void paintComponent(Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	Rectangle2D.Double clip = new Rectangle2D.Double();

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
	    double depth = ((Double) depths.get(r)).doubleValue();

	    switch(direction) {
	    case HORIZONTAL:
		clip.height = depth;
		break;
	    case VERTICAL:
		clip.width = depth;
		break;
	    }
	    g2.setClip(clip);
	    
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
	invalidate();
    }

    public double sequenceToGraphics(int seqPos) {
	return ((seqPos-1) * scale) + insetBefore;
    }

    public int graphicsToSequence(double gPos) {
	int p = (int) ((gPos - insetBefore) / scale) + 1;
	if (p < 1)
	    p = 1;
	if (p > sequence.length())
	    p = sequence.length();
	return p;
    }

    public void invalidate() {
	int alongDim = (int) (scale * sequence.length() + 
			      insetBefore + insetAfter);
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
	switch (direction) {
	case HORIZONTAL:
	    ourSize.setSize(alongDim, (int) acrossDim);
	    break;
	case VERTICAL:
	    ourSize.setSize((int) acrossDim, alongDim);
	    break;
	}
	
	super.invalidate();
    }

    public Dimension getMinimumSize() {
	return ourSize;
    }

    public Dimension getMaximumSize() {
	return ourSize;
    }

    public Dimension getPreferredSize() {
	return ourSize;
    }

    private class RendererMonitor implements PropertyChangeListener {
	public void propertyChange(PropertyChangeEvent ev) {
	    invalidate();
	    repaint();
	}
    }
}

