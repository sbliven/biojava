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

public class RulerRenderer implements SequenceRenderer {
  private double depth = 25.0;
  
  public double getDepth(SequenceRenderContext src, RangeLocation pos) {
    return depth + 1.0;
  }
  
  public double getMinimumLeader(SequenceRenderContext src, RangeLocation pos) {
    return 0.0;
  }
  
  public double getMinimumTrailer(SequenceRenderContext src, RangeLocation pos) {
    String lengthString = String.valueOf(src.getSequence().length());
    Font f = src.getFont();
    FontRenderContext frc = new FontRenderContext(null, true, true);
    GlyphVector gv = f.createGlyphVector(frc, lengthString);
    return gv.getVisualBounds().getWidth();
  }
  
  public void paint(
    Graphics2D g, SequenceRenderContext src,
    RangeLocation pos
  ) {
    g.setPaint(Color.black);
    
    int min = pos.getMin();
    int max = pos.getMax();
    double minX = src.sequenceToGraphics(min);
    double maxX = src.sequenceToGraphics(max);
    double scale = src.getScale();
    double halfScale = scale * 0.5;
    Line2D line;
    
    if(src.getDirection() == src.HORIZONTAL) {
      line = new Line2D.Double(minX + halfScale, 0.0, maxX + halfScale, 0.0);
    } else {
      line = new Line2D.Double(0.0, minX + halfScale, 0.0, maxX + halfScale);
    }
    
    g.draw(line);
    
    // we want ticks no closer than 40 pixles appart
    double ten = Math.log(10);
    double minGap = Math.log(max) / ten * 7.0;
    int realSymsPerGap = src.graphicsToSequence(minGap + 5.0);
    //System.out.println("Real syms: " + realSymsPerGap);
    int snapSymsPerGap = (int) Math.exp(
      Math.ceil(Math.log(realSymsPerGap) / ten) * ten
    );
    //System.out.println("Snapped syms: " + snapSymsPerGap);
    
    int minP = min + (snapSymsPerGap - min) % snapSymsPerGap;
    for(int indx = minP; indx <= max; indx += snapSymsPerGap) {
      double offset = src.sequenceToGraphics(indx);
      if(src.getDirection() == src.HORIZONTAL) {
        line.setLine(offset + halfScale, 0.0, offset + halfScale, 5.0);
        g.drawString(String.valueOf(indx), (float) (offset + halfScale), 20.0f);
      } else {
        line.setLine(0.0, offset + halfScale, 5.0, offset + halfScale);
      }
      g.draw(line);
    }
  }
  
  public SequenceViewerEvent processMouseEvent(
    SequenceRenderContext src,
    MouseEvent me,
    List path,
    RangeLocation pos
  ) {
    path.add(this);
    int sPos = src.graphicsToSequence(me.getPoint());
    return new SequenceViewerEvent(this, null, sPos, me, path);
  }
}
