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
import java.beans.*;
import java.util.Iterator;
import javax.swing.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;

/**
 * The gui component for rendering a DistributionLogo. By default, this uses the text
 * logo style - with letters stacked on top of one another, scaled by the total
 * information in the dist, and uses a PlainStyle colorer that outlines in
 * black, and fills in grey.
 *
 * @author Matthew Pocock
 */
public class DistributionLogo extends JComponent {
  /**
   * The default logo painter.
   */
  private static final LogoPainter DEFAULT_LOGO_PAINTER = new TextLogoPainter();
  
  /**
   * A usefull constant to keep arround.
   */
  private static double bits = Math.log(2.0);
  
  /**
   * The dist to render.
   */
  private Distribution dist;
  
  /**
   * The logoPainter property.
   */
  private LogoPainter logoPainter = DEFAULT_LOGO_PAINTER;

  /**
   * The style property.
   */
  private SymbolStyle style = new PlainStyle(Color.black, Color.gray);
  
  /**
   * Retrieve the currently rendered dist.
   *
   * @return  a Distribution
   */
  public Distribution getDistribution() {
    return dist;
  }
  
  /**
   * Set the dist to render.
   * <P>
   * The dist must be over a FiniteAlphabet so that we can draw the numbers
   * for each Symbol.
   *
   * @param dist the new Distribution to render
   */
  public void setDistribution(Distribution dist)
  throws IllegalAlphabetException {
    firePropertyChange("dist", this.dist, dist);
    this.dist = dist;
  }
  
  /**
   * Retrieve the current logo painter.
   *
   * @return  the LogoPainter used to render the dist
   */
  public LogoPainter getLogoPainter() {
    return logoPainter;
  }
  
  /**
   * Set the logo painter.
   * <P>
   * This will alter the way that the dist is rendered to screen.
   *
   * @param logoPainter the new logoPainter
   */
  public void setLogoPainter(LogoPainter logoPainter) {
    firePropertyChange("logoPainter", this.logoPainter, logoPainter);
    this.logoPainter = logoPainter;
  }
  
  /**
   * Retrieve the current style.
   *
   * @return the current SymbolStyle
   */
  public SymbolStyle getStyle() {
    return style;
  }
  
  /**
   * Set the symbol style.
   * <P>
   * This will change the outline and fill paints for the logos
   *
   * @param style the new SymbolStyle to use
   */
  public void setStyle(SymbolStyle style) {
    firePropertyChange("style", this.style, style);
    this.style = style;
  }
  
  /**
   * Create a new DistributionLogo object. It will set up all the properties except the
   * dist to render.
   */
  public DistributionLogo() {
    this.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if(name.equals("dist") ||
           name.equals("logoPainter") ||
           name.equals("style") )
        {
          repaint();
        }
      }
    });
    
    Dimension d = new Dimension(20, 20);
    setMinimumSize(d);
    setPreferredSize(d);
  }
  
  /**
   * Calculate the information content of a symbol in bits.
   *
   * @param r the symbol to calculate for
   * @throws IllegalSymbolException if r is not within the dist.
   */
  public double entropy(Symbol r) throws IllegalSymbolException {
    Distribution dist = getDistribution();
    double p = dist.getWeight(r);
    double lp = Math.log(p);
    
    return -p * lp / bits;
  }
  
  /**
   * Retrieve the maximal number of bits possible for this type of dist.
   *
   * @return maximum bits as a double
   */
  public double totalBits() {
    return Math.log(((FiniteAlphabet) getDistribution().getAlphabet()).size()) / bits;
  }
  
  /**
   * Calculates the total information of the dist in bits.
   * <P>
   * This calculates <code>totalBits - sum_r(entropy(r))</code>
   *
   * @return  the total information in the dist
   */
  public double totalInformation() {
    double inf = totalBits();
    Distribution eDistribution = getDistribution();
    
    for(
      Iterator i = ((FiniteAlphabet) eDistribution.getAlphabet()).iterator();
      i.hasNext();
    ) {
      Symbol r = (Symbol) i.next();
      try {
        inf -= entropy(r);
      } catch (IllegalSymbolException ire) {
        throw new BioError(ire,
        "Symbol evaporated while calculating information");
      }
    }
    
    return inf;
  }
  
  /**
   * Transforms the graphics context so that it is in bits space,
   * and then requests the logo painter to fill the area.
   */
  public void paintComponent(Graphics g) {    
    Graphics2D g2 = (Graphics2D) g;
    Rectangle clip = g2.getClipBounds();
    if(isOpaque()) {
      g2.clearRect(clip.x, clip.y, clip.width, clip.height);
    }
    if(getDistribution() == null) {
      return;
    }

    getLogoPainter().paintLogo(g, this);
  }
}
