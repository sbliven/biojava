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
import org.biojava.bio.symbol.*;
import org.biojava.bio.gui.*;

import java.awt.*;
import java.awt.geom.*;

import java.util.List;

public class SymbolSequenceRenderer implements SequenceRenderer {
    private double depth = 25.0;
    
    public double getDepth(SequenceRenderContext sp, int min, int max) {
      return depth + 1.0;
    }

    public double getMinimumLeader(SequenceRenderContext sp) {
      return 0.0;
    }

    public double getMinimumTrailer(SequenceRenderContext sp) {
      return 0.0;
    }

    public void paint(
      Graphics2D g, SequenceRenderContext sp,
      int min, int max
    ) {
      SymbolList seq = sp.getSequence();
      int direction = sp.getDirection();
      
      g.setFont(sp.getFont());
      Rectangle2D clip = g.getClipBounds();
      
      g.setColor(Color.black);
      
      double scale = sp.getScale();
      Rectangle2D maxBounds =
        g.getFont().getMaxCharBounds(g.getFontRenderContext());
      if(
        sp.getScale() >= maxBounds.getWidth()*0.3 &&
        sp.getScale() >= maxBounds.getHeight()*0.3
      ) {
        double fudgeAcross = 0.0;
        double fudgeDown = 0.0;
        if (direction == sp.HORIZONTAL) {
          fudgeAcross = 0.0 /*- maxBounds.getCenterX()*/;
          fudgeDown = depth * 0.5 - maxBounds.getCenterY();
        } else {
          fudgeAcross = depth * 0.5 - maxBounds.getCenterX();
          fudgeDown = scale * 0.5 - maxBounds.getCenterY();
        }

        for (int pos = min; pos <= max; ++pos) {
          double gPos = sp.sequenceToGraphics(pos);
          char c = seq.symbolAt(pos).getToken();
          if (direction == SequencePanel.HORIZONTAL) {
            //charBox.x = gPos;
            g.drawString("" + c, (int) (gPos + fudgeAcross), (int) fudgeDown);
          } else {
            //charBox.y = gPos;
            g.drawString("" + c, (int) fudgeAcross, (int) (gPos + fudgeDown));
          }
          //g.draw(charBox);
        }
      }
    }
}
