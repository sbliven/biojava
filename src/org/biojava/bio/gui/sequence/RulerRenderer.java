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
import java.awt.event.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.gui.*;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

import java.util.List;

/**
 * Draw a `ruler' showing sequence coordinates.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 * @author David Huen
 */

public class RulerRenderer implements SequenceRenderer {
  private double depth = 25.0;
  
  public double getDepth(SequenceRenderContext src) {
    return depth + 1.0;
  }
  
  public double getMinimumLeader(SequenceRenderContext src) {
    return 0.0;
  }
  
  public double getMinimumTrailer(SequenceRenderContext src) {
    String lengthString = String.valueOf(src.getSymbols().length());
    Font f = src.getFont();
    FontRenderContext frc = new FontRenderContext(null, true, true);
    GlyphVector gv = f.createGlyphVector(frc, lengthString);
    return gv.getVisualBounds().getWidth();
  }
  
  public void paint(
    Graphics2D g, SequenceRenderContext src
  ) {
    g.setPaint(Color.black);
    
    int min = src.getRange().getMin();
    int max = src.getRange().getMax();
    double minX = src.sequenceToGraphics(min);
    double maxX = src.sequenceToGraphics(max);
    double scale = src.getScale();
    double halfScale = scale * 0.5;
    Line2D line;
    Rectangle2D activeClip = g.getClipBounds();

    // dump some info
    //System.out.println("ruler transform: " + g.getTransform());
    //System.out.println("ruler min max: " + min + " " + max);
    //System.out.println("ruler minX maxX: " + minX + " " + maxX);
    //System.out.println("ruler activeClip:" + activeClip);
    
    if(src.getDirection() == src.HORIZONTAL) {
      line = new Line2D.Double(minX - halfScale, 0.0, maxX + halfScale, 0.0);
    } else {
      line = new Line2D.Double(0.0, minX - halfScale, 0.0, maxX + halfScale);
    }
    
    g.draw(line);
    
    // tick spacing should be decided by the size of the text needed
    // to display the largest coordinate value and some
    // minimum spacing limit.
    // we want ticks no closer than 40 pixels appart
    double ten = Math.log(10);
FontMetrics myFontMetrics = g.getFontMetrics();    
    int coordWidth = myFontMetrics.stringWidth(Integer.toString(max));
    // System.out.println("coordWidth: " + coordWidth);
    double minGap = (double) Math.max(coordWidth, 40);
    int realSymsPerGap = (int) Math.ceil(((minGap + 5.0) / src.getScale()));
    // System.out.println("Real syms: " + realSymsPerGap);

    // we need to snap to a value beginning 1, 2 or 5.
    double exponent =  Math.floor(Math.log(realSymsPerGap) / ten);
    double characteristic = realSymsPerGap 
                            / Math.pow(10.0, exponent);
    int snapSymsPerGap;
    if (characteristic > 5.0) {
      // use unit ticks
      snapSymsPerGap = (int) Math.pow(10.0, exponent + 1.0);
    } else if (characteristic > 2.0) {
      // use ticks of 5
      snapSymsPerGap = (int)(5.0 * Math.pow(10.0, exponent));
    } else {
      snapSymsPerGap = (int)(2.0 * Math.pow(10.0, exponent)); 
    }
    // System.out.println("Snapped syms: " + snapSymsPerGap);
    
    int minP = min + (snapSymsPerGap - min) % snapSymsPerGap;
    for(int indx = minP; indx <= max; indx += snapSymsPerGap) {
      double offset = src.sequenceToGraphics(indx);
      // System.out.println("ruler indx offset: " + indx + " " +  offset);
      if(src.getDirection() == src.HORIZONTAL) {
        line.setLine(offset + halfScale, 0.0, offset + halfScale, 5.0);
	String labelString = String.valueOf(indx);
        int halfLabelWidth = myFontMetrics.stringWidth(labelString) / 2;
        g.drawString(String.valueOf(indx), 
                     (float) (offset + halfScale - halfLabelWidth), 20.0f);
      } else {
        line.setLine(0.0, offset + halfScale, 5.0, offset + halfScale);
      }
      g.draw(line);
    }
  }
  
  public SequenceViewerEvent processMouseEvent(
    SequenceRenderContext src,
    MouseEvent me,
    List path
  ) {
    path.add(this);
    int sPos = src.graphicsToSequence(me.getPoint());
    return new SequenceViewerEvent(this, null, sPos, me, path);
  }
}
