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

public abstract class AbstractChangeable implements Changeable {
  private transient ChangeSupport changeSupport = null;

  protected boolean hasListeners() {
    return changeSupport != null;
  }

  protected ChangeSupport getChangeSupport(ChangeType ct) {
    if(changeSupport == null) {
      changeSupport = new ChangeSupport();
    }

    return changeSupport;
  }

  public void addChangeListener(ChangeListener cl) {
    ChangeSupport cs = getChangeSupport(ChangeType.UNKNOWN);
    synchronized(cs) {
      cs.addChangeListener(cl);
    }
  }

  public void addChangeListener(ChangeListener cl, ChangeType ct) {
    ChangeSupport cs = getChangeSupport(ct);
    synchronized(cs) {
      cs.addChangeListener(cl, ct);
    }
  }

  public void removeChangeListener(ChangeListener cl) {
    ChangeSupport cs = getChangeSupport(ChangeType.UNKNOWN);
    synchronized(cs) {
      cs.removeChangeListener(cl);
    }
  }

  public void removeChangeListener(ChangeListener cl, ChangeType ct) {
    ChangeSupport cs = getChangeSupport(ct);
    synchronized(cs) {
      cs.removeChangeListener(cl, ct);
    }
  }
}
