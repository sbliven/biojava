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
import javax.swing.*;
import java.beans.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.gui.*;

public interface SequenceRenderContext extends SwingConstants {
    public int getDirection();

    public double getScale();
    public double sequenceToGraphics(int i);
    public int graphicsToSequence(double d);

    public SymbolList getSequence();
    public Border getLeadingBorder();
    public Border getTrailingBorder();

    public Font getFont();

    public static class Border
	implements java.io.Serializable, SwingConstants 
    {
	protected final PropertyChangeSupport pcs;
	private double size = 0.0;
	private int alignment = CENTER;
    
	public double getSize() {
	    return size;
	}
	
	public void setSize(double size) {
	    this.size = size;
	}
    
	public int getAlignment() {
	    return alignment;
	}
    
	public void setAlignment(int alignment)
	    throws IllegalArgumentException {
	    switch (alignment) {
	    case LEADING:
	    case TRAILING:
	    case CENTER:
		int old = this.alignment;
		this.alignment = alignment;
		pcs.firePropertyChange("alignment", old, alignment);
		break;
	    default:
		throw new IllegalArgumentException(
						   "Alignment must be one of the constants LEADING, TRAILING or CENTER"
						   );
	    }
	}
    
	public Border() {
	    alignment = CENTER;
	    pcs = new PropertyChangeSupport(this);
	}
    
	public void addPropertyChangeListener(PropertyChangeListener listener) {
	    pcs.addPropertyChangeListener(listener);
	}
    
	public void removePropertyChangeListener(PropertyChangeListener listener) {
	    pcs.removePropertyChangeListener(listener);
	}
    }
}
