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

import org.biojava.bio.BioError;
import org.biojava.bio.seq.*;
import org.biojava.bio.dp.*;

/**
 * A logo painter that paints in stacked letters.
 * The total height of the letters is
 * proportional to the total informaton in the state. The height of each letter
 * is proportional to its emission probability. The most likely letter is drawn
 * highest.
 *
 * @author Matthew Pocock
 */
public class TextLogoPainter implements LogoPainter {
  /**
   * A comparator to set up our letters & information scores nicely.
   */
  private static final Comparator COMP = new ResValComparator();
  
  /**
   * Supports the bean property logoFont.
   */
  private PropertyChangeSupport pcs;
  
  /**
   * The property for the logoFont.
   */
  private Font logoFont;
  
  /**
   * Retrieve the current font.
   *
   * @return the current logo font
   */
  public Font getLogoFont() {
    return logoFont;
  }
  
  /**
   * Set the current logo font.
   *
   * @param logoFont the new Font to render the logo letters in
   */
  public void setLogoFont(Font logoFont) {
    firePropertyChange("logoFont", this.logoFont, logoFont);
    this.logoFont = logoFont;
  }

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
  
  public void paintLogo(Graphics g, StateLogo sl) {
    Graphics2D g2 = (Graphics2D) g;
    EmissionState state = sl.getState();
    ResidueStyle style = sl.getStyle();
    
    Rectangle bounds = sl.getBounds();
    double width = bounds.getWidth();
    double height = bounds.getHeight();
    double base = height;
    double scale = height * (sl.totalInformation() / sl.totalBits());

    SortedSet info = new TreeSet(COMP);
    
    try {
      for(
        Iterator i = ((FiniteAlphabet) state.alphabet()).residues().iterator();
        i.hasNext();
      ) {
        Residue r = (Residue) i.next();
        info.add(new ResVal(r, Math.exp(state.getWeight(r)) * scale));
      }
    } catch (IllegalResidueException ire) {
      throw new BioError(ire, "Residue dissapeared from state alphabet");
    }
    
    FontRenderContext frc = g2.getFontRenderContext();
    for(Iterator i = info.iterator(); i.hasNext();) {
      ResVal rv = (ResVal) i.next();
      
      GlyphVector gv = logoFont.createGlyphVector(frc, rv.getResidue().getSymbol() + "");
      Shape outline = gv.getOutline();
      Rectangle2D oBounds = outline.getBounds2D();
      
      AffineTransform at = new AffineTransform();
      at.setToTranslation(0.0, base-rv.getValue());
      at.scale(width / oBounds.getWidth(), rv.getValue() / oBounds.getHeight());
      at.translate(-oBounds.getMinX(), -oBounds.getMinY());
      outline = at.createTransformedShape(outline);
      
      try {
        g2.setPaint(style.fillPaint(rv.getResidue()));
      } catch (IllegalResidueException ire) {
        g2.setPaint(Color.black);
      }
      g2.fill(outline);
      
      try {
        g2.setPaint(style.outlinePaint(rv.getResidue()));
      } catch (IllegalResidueException ire) {
        g2.setPaint(Color.gray);
      }
      g2.draw(outline);
      
      base -= rv.getValue();
    }
  }
  
  public TextLogoPainter() {
    pcs = new PropertyChangeSupport(this);
    logoFont = new Font("Tahoma", Font.PLAIN, 12);
  }
  
  /**
   * A residue/information tuple.
   */
  private static class ResVal {
    private Residue residue;
    private double value;
    
    public final Residue getResidue() {
      return residue;
    }
    
    public final double getValue() {
      return value;
    }
    
    public ResVal(Residue res, double val) {
      residue = res;
      value = val;
    }
  }

  /**
   * The comparator for comparing residue/information tuples.
   */
  private static class ResValComparator implements Comparator {
    public final int compare(Object o1, Object o2) {
      ResVal rv1 = (ResVal) o1;
      ResVal rv2 = (ResVal) o2;
      
      double diff = rv1.getValue() - rv2.getValue();
      if(diff < 0) return -1;
      if(diff > 0) return +1;
      return 0;
    }
  }
}
