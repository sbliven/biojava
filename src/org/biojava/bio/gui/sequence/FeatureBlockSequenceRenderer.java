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

public class FeatureBlockSequenceRenderer implements SequenceRenderer {
    private FeatureFilter filter;
    private double depth = 25.0;
    private Paint fill;
    private Paint outline;
    
    protected PropertyChangeSupport pcs;

    public FeatureBlockSequenceRenderer() {
	filter = FeatureFilter.all;
	fill = Color.red;
	outline = Color.black;
	pcs = new PropertyChangeSupport(this);
    }

    public FeatureFilter getFilter() {
	return filter;
    }

    public void setFilter(FeatureFilter f) {
	FeatureFilter oldFilter = filter;
	filter = f;
	pcs.firePropertyChange("filter", oldFilter, filter);
	
    }

    public double getDepth(SequencePanel sp) {
	return depth;
    }

    public double getMinimumLeader(SequencePanel sp) {
	return 0.0;
    }

    public double getMinimumTrailer(SequencePanel sp) {
	return 0.0;
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

    public void paint(Graphics2D g, SequencePanel sp) {
	for (Iterator i = sp.getSequence().filter(filter, true).features();
	     i.hasNext(); )
	{
	    Feature f = (Feature) i.next();
	    Location l = f.getLocation();
	    double min = sp.sequenceToGraphics(l.getMin());
	    double max = sp.sequenceToGraphics(l.getMax() + 1);

	    Rectangle2D box = null;
	    if (sp.getDirection() == SequencePanel.HORIZONTAL)
		box = new Rectangle2D.Double(min, 5, max-min, 20);
	    else
		box = new Rectangle2D.Double(5, min, 15, max-min);
	    g.setPaint(fill);
	    g.fill(box);
	    if (outline != fill) {
		g.setPaint(outline);
		g.draw(box);
	    }
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

