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

import org.biojava.bio.BioError;
import org.biojava.bio.seq.*;
import org.biojava.bio.dp.*;

/**
 * The gui component for rendering a StateLogo. By default, this uses the text
 * logo style - with letters stacked on top of one another, scaled by the total
 * information in the state, and uses a PlainStyle colorer that outlines in
 * black, and fills in grey.
 *
 * @author Matthew Pocock
 */
public class StateLogo extends JComponent {
  /**
   * The default logo painter.
   */
  private static final LogoPainter DEFAULT_LOGO_PAINTER = new TextLogoPainter();
  
  /**
   * A usefull constant to keep arround.
   */
  private static double bits = Math.log(2.0);
  
  /**
   * The state to render.
   */
  private EmissionState state;
  
  /**
   * The logoPainter property.
   */
  private LogoPainter logoPainter = DEFAULT_LOGO_PAINTER;

  /**
   * The style property.
   */
  private ResidueStyle style = new PlainStyle(Color.black, Color.gray);
  
  /**
   * Retrieve the currently rendered state.
   *
   * @return  an EmissionState
   */
  public EmissionState getState() {
    return state;
  }
  
  /**
   * Set the state to render.
   *
   * @param state the new EmissionState to render
   */
  public void setState(EmissionState state) {
    firePropertyChange("state", this.state, state);
    this.state = state;
  }
  
  /**
   * Retrieve the current logo painter.
   *
   * @return  the LogoPainter used to render the state
   */
  public LogoPainter getLogoPainter() {
    return logoPainter;
  }
  
  /**
   * Set the logo painter.
   * <P>
   * This will alter the way that the state is rendered to screen.
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
   * @return the current ResidueStyle
   */
  public ResidueStyle getStyle() {
    return style;
  }
  
  /**
   * Set the residue style.
   * <P>
   * This will change the outline and fill paints for the logos
   *
   * @param style the new ResidueStyle to use
   */
  public void setStyle(ResidueStyle style) {
    firePropertyChange("style", this.style, style);
    this.style = style;
  }
  
  /**
   * Create a new StateLogo object. It will set up all the properties except the
   * state to render.
   */
  public StateLogo() {
    this.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if(name.equals("state") ||
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
   * Calculate the information content of a residue in bits.
   *
   * @param r the residue to calculate for
   * @throws IllegalResidueException if r is not within the state.
   */
  public double entropy(Residue r) throws IllegalResidueException {
    EmissionState state = getState();
    double lp = state.getWeight(r);
    double p = Math.exp(lp);
    
    return -p * lp / bits;
  }
  
  /**
   * Retrieve the maximal number of bits possible for this type of state.
   *
   * @return maximum bits as a double
   */
  public double totalBits() {
    return Math.log(getState().alphabet().size()) / bits;
  }
  
  /**
   * Calculates the total information of the state in bits.
   * <P>
   * This calculates <code>totalBits - sum_r(entropy(r))</code>
   *
   * @return  the total information in the state
   */
  public double totalInformation() {
    double inf = totalBits();
    EmissionState eState = getState();
    
    for(Iterator i = eState.alphabet().residues().iterator(); i.hasNext();) {
      Residue r = (Residue) i.next();
      try {
        inf -= entropy(r);
      } catch (IllegalResidueException ire) {
        throw new BioError(ire,
        "Residue evaporated while calculating information");
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
    if(getState() == null) {
      return;
    }

    getLogoPainter().paintLogo(g, this);
  }
}
