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
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.gui.*;

import java.awt.*;
import java.awt.geom.*;

import java.util.List;

public class SymbolSequenceRenderer implements SequenceRenderer {
    private final double minWidth = 3.0;
    private double depth = 25.0;
    
    public double getDepth(SequencePanel sp) {
      /*if(sp.getScale() < minWidth) {
        return 0.0;
      } else {*/
        return depth;
      //}
    }

    public double getMinimumLeader(SequencePanel sp) {
      return 0.0;
    }

    public double getMinimumTrailer(SequencePanel sp) {
      return 0.0;
    }

    public void paint(Graphics2D g, SequencePanel sp) {
      Sequence seq = sp.getSequence();
      Rectangle2D clip = g.getClipBounds();
      int direction = sp.getDirection();
      int minP;
      int maxP;
      
      if(direction == sp.HORIZONTAL) {
        minP = Math.max(1, sp.graphicsToSequence(clip.getMinX()));
        maxP = Math.min(seq.length(), sp.graphicsToSequence(clip.getMaxX()));
      } else {
        minP = Math.max(1, sp.graphicsToSequence(clip.getMinY()));
        maxP = Math.min(seq.length(), sp.graphicsToSequence(clip.getMaxY()));
      }
      
      g.setColor(Color.black);
      
      if(sp.getScale() < minWidth) {
        g.fill(clip);
      } else {
        for (int pos = minP; pos <= maxP; ++pos) {
          double gPos = sp.sequenceToGraphics(pos);
          char c = seq.symbolAt(pos).getToken();
          if (direction == SequencePanel.HORIZONTAL) {
            g.drawString("" + c, (int) gPos, 20);
          } else {
            g.drawString("" + c, 10, (int) gPos + 10);  // FIXME!
          }
        }
      }
    }
}
