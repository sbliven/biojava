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
 */

package org.biojava.utils;

/**
 * @author Matthew Pocock
 */
public abstract class AbstractChangeable implements Changeable {
  private transient ChangeSupport changeSupport = null;

  protected boolean hasListeners() {
    return changeSupport != null;
  }

  protected ChangeSupport getChangeSupport(ChangeType ct) {
    if(changeSupport != null) {
      return changeSupport;
    }
    
    synchronized(this) {
      if(changeSupport == null) {
        changeSupport = new ChangeSupport();
      }
    }
    
    return changeSupport;
  }

  public final void addChangeListener(ChangeListener cl) {
    addChangeListener(cl, ChangeType.UNKNOWN);
  }

  public final void addChangeListener(ChangeListener cl, ChangeType ct) {
    ChangeSupport cs = getChangeSupport(ct);
    cs.addChangeListener(cl, ct);
  }

  public final void removeChangeListener(ChangeListener cl) {
    removeChangeListener(cl, ChangeType.UNKNOWN);
  }

  public final void removeChangeListener(ChangeListener cl, ChangeType ct) {
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(ct);
      cs.removeChangeListener(cl, ct);
    }
  }
  
  public final boolean isUnchanging(ChangeType ct) {
    ChangeSupport cs = getChangeSupport(ct);
    return cs.isUnchanging(ct);
  }
}
