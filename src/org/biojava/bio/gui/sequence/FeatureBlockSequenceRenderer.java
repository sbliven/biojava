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
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.gui.*;

import java.awt.*;
import java.awt.geom.*;

import java.util.List;

public class FeatureBlockSequenceRenderer implements SequenceRenderer, PropertyChangeListener {
    private FeatureFilter filter;
    private double depth = 25.0;
    private FeatureRenderer renderer;
    
    
    protected PropertyChangeSupport pcs;

    public FeatureBlockSequenceRenderer() {
	pcs = new PropertyChangeSupport(this);
	filter = FeatureFilter.all;
	setFeatureRenderer(new BasicFeatureRenderer());
    }

    public FeatureFilter getFilter() {
	return filter;
    }

    public void setFilter(FeatureFilter f) {
	FeatureFilter oldFilter = filter;
	filter = f;
	pcs.firePropertyChange("filter", oldFilter, filter);
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

    public double getDepth(SequencePanel sp) {
	return depth;
    }

    public double getMinimumLeader(SequencePanel sp) {
	return 0.0;
    }

    public double getMinimumTrailer(SequencePanel sp) {
	return 0.0;
    }


    public void paint(Graphics2D g, SequencePanel sp) {
	for (Iterator i = sp.getSequence().filter(filter, true).features();
	     i.hasNext(); )
	{
	    Rectangle2D clip = g.getClipBounds();
	    Feature f = (Feature) i.next();
	    Location l = f.getLocation();
	    double min = sp.sequenceToGraphics(l.getMin());
	    double max = sp.sequenceToGraphics(l.getMax() + 1);

	    Rectangle2D box = null;
	    if (sp.getDirection() == SequencePanel.HORIZONTAL)
		box = new Rectangle2D.Double(min, 3, Math.max(1.0, max-min), depth - 6);
	    else
		box = new Rectangle2D.Double(3, min, depth - 6, Math.max(1.0, max-min));
	    if (box.intersects(clip))
		renderer.renderFeature(g, f, box, sp);
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

