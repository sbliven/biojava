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

    public void renderFeature(Graphics2D g, Feature f, Rectangle2D box,
			      SequencePanel context)
    {
	Shape s = box;
	if (f instanceof StrandedFeature && box.getWidth() > 10) {
	    int strand = ((StrandedFeature) f).getStrand();
	    if (strand == StrandedFeature.POSITIVE) {
		GeneralPath path = new GeneralPath();
		path.moveTo((float) box.getMinX(), (float) (box.getMinY() + 4));
		path.lineTo((float) (box.getMaxX() - 8),
			    (float) (box.getMinY() + 4));
		path.lineTo((float) (box.getMaxX() - 8),
			    (float) (box.getMinY()));
		path.lineTo((float) (box.getMaxX()),
			    (float) ((box.getMaxY() + box.getMinY()) / 2));
		path.lineTo((float) (box.getMaxX() - 8),
			    (float) (box.getMaxY()));
		path.lineTo((float) (box.getMaxX() - 8),
			    (float) (box.getMaxY() - 4));
		path.lineTo((float) box.getMinX(), (float) (box.getMaxY() - 4));
		path.closePath();
		s = path;
	    } else if (strand == StrandedFeature.NEGATIVE) {		
		GeneralPath path = new GeneralPath();
		path.moveTo((float) box.getMaxX(), (float) (box.getMinY() + 4));
		path.lineTo((float) (box.getMinX() + 8),
			    (float) (box.getMinY() + 4));
		path.lineTo((float) (box.getMinX() + 8),
			    (float) (box.getMinY()));
		path.lineTo((float) (box.getMinX()),
			    (float) ((box.getMaxY() + box.getMinY()) / 2));
		path.lineTo((float) (box.getMinX() + 8),
			    (float) (box.getMaxY()));
		path.lineTo((float) (box.getMinX() + 8),
			    (float) (box.getMaxY() - 4));
		path.lineTo((float) box.getMaxX(), (float) (box.getMaxY() - 4));
		path.closePath();
		s = path;
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
