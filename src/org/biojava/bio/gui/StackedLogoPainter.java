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

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.beans.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;

/**
 * A logo painter that paints in stacked areas.
 *
 * @author Matthew Pocock
 */
public class StackedLogoPainter implements LogoPainter {
  /**
   * A comparator to set up our letters & information scores nicely.
   */
  private static final Comparator COMP = new ResValComparator();
  
  /**
   * Supports the bean property logoFont.
   */
  private PropertyChangeSupport pcs;

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(propertyName, listener);
  }
                                        
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(propertyName, listener);
  }
  
  public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    pcs.firePropertyChange(propertyName, oldValue, newValue);
  }

  public void firePropertyChange(String propertyName, int oldValue, int newValue) {
    pcs.firePropertyChange(propertyName, oldValue, newValue);
  }

  public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    pcs.firePropertyChange(propertyName, oldValue, newValue);
  }

  public void firePropertyChange(PropertyChangeEvent evt) {
    pcs.firePropertyChange(evt);
  }

  public boolean hasListeners(String propertyName) {
    return pcs.hasListeners(propertyName);
  }
  
  public void paintLogo(LogoContext lCtxt) {
    Distribution dis = lCtxt.getDistribution();
    SymbolStyle style = lCtxt.getStyle();
    
    Rectangle bounds = lCtxt.getBounds();
    double width = bounds.getWidth();
    double height = bounds.getHeight();
    double base = height;

    SortedSet info = new TreeSet(COMP);
    
    try {
      for(
        Iterator i = ((FiniteAlphabet) dis.getAlphabet()).iterator();
        i.hasNext();
      ) {
        AtomicSymbol s = (AtomicSymbol) i.next();
        info.add(new ResVal(s, dis.getWeight(s) * height));
      }
    } catch (IllegalSymbolException ire) {
      throw new BioError(ire, "Symbol dissapeared from dis alphabet");
    }
    
    Rectangle r = new Rectangle();
    r.x = bounds.x;
    r.y = 0;
    r.width = bounds.width;
    for(Iterator i = info.iterator(); i.hasNext();) {
      ResVal rv = (ResVal) i.next();
      r.height = (int) rv.getValue();
      
      lCtxt.getBlockPainter().paintBlock(lCtxt, r, rv.getToken());
      
      r.y -= rv.getValue();
    }
  }
  
  public StackedLogoPainter() {
    pcs = new PropertyChangeSupport(this);
  }
  
  /**
   * A symbol/information tuple.
   */
  private static class ResVal {
    private AtomicSymbol symbol;
    private double value;
    
    public final AtomicSymbol getToken() {
      return symbol;
    }
    
    public final double getValue() {
      return value;
    }
    
    public ResVal(AtomicSymbol sym, double val) {
      symbol = sym;
      value = val;
    }
  }

  /**
   * The comparator for comparing symbol/information tuples.
   */
  private static class ResValComparator implements Comparator {
    public final int compare(Object o1, Object o2) {
      ResVal rv1 = (ResVal) o1;
      ResVal rv2 = (ResVal) o2;
      
      double diff = rv1.getValue() - rv2.getValue();
      if(diff < 0) return -1;
      if(diff > 0) return +1;
      return rv1.getToken().getToken() - rv2.getToken().getToken();
    }
  }
}
