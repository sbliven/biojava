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
 * A logo painter that paints in bars. The total height of the bars is
 * proportional to the total informaton in the state.
 *
 * @author Matthew Pocock
 */
public class BarLogoPainter implements LogoPainter {
  public void paintLogo(Graphics g, DistributionLogo sl) {
    Graphics2D g2 = (Graphics2D) g;
    Distribution dis = sl.getDistribution();
    SymbolStyle style = sl.getStyle();
    
    Rectangle bounds = sl.getBounds();
    double width = bounds.getWidth();
    double stepWidth = width / (double) ((FiniteAlphabet) dis.getAlphabet()).size();
    double height = bounds.getHeight();
    double scale = height * (sl.totalInformation() / sl.totalBits());

    double w = 0.0;    
    for(
      Iterator i = ((FiniteAlphabet) dis.getAlphabet()).iterator();
      i.hasNext();
    ) {
      Symbol r = (Symbol) i.next();
      double rh = 0.0;
     
      try {
        rh = dis.getWeight(r) * scale;
      } catch (IllegalSymbolException ire) {
        throw new BioError(ire, "State alphabet has changed while painting");
      }
      
      Shape outline = new Rectangle2D.Double(w, height - rh, stepWidth, rh);
      
      try {
        g2.setPaint(style.fillPaint(r));
      } catch (IllegalSymbolException ire) {
        g2.setPaint(Color.black);
      }
      g2.fill(outline);
      
      try {
        g2.setPaint(style.outlinePaint(r));
      } catch (IllegalSymbolException ire) {
        g2.setPaint(Color.gray);
      }
      g2.draw(outline);
      
      w += stepWidth;
    }
  }
  
  public BarLogoPainter() {}
}
