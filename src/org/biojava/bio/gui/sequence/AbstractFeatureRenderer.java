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
import org.biojava.bio.gui.*;

/**
 * Usefull abstract class for renderers that just mixes in a
 * PropertyChangeSupport for fireing property change events.
 *
 * @author Matthew Pocock
 */
public abstract class AbstractFeatureRenderer implements FeatureRenderer {
  protected PropertyChangeSupport pcs;

  public AbstractFeatureRenderer() {
    pcs = new PropertyChangeSupport(this);
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
