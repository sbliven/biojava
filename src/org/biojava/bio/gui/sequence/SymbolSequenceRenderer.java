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
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.gui.*;

import java.util.List;

public class SymbolSequenceRenderer implements SequenceRenderer {
    private double depth = 25.0;
    
    public double getDepth(SequenceRenderContext src) {
      return depth + 1.0;
    }

    public double getMinimumLeader(SequenceRenderContext src) {
      return 0.0;
    }

    public double getMinimumTrailer(SequenceRenderContext src) {
      return 0.0;
    }

    public void paint(
      Graphics2D g,
      SequenceRenderContext src
    ) {
      SymbolList seq = src.getSymbols();
      int direction = src.getDirection();
      
      g.setFont(src.getFont());
      Rectangle2D oldClip = g.getClipBounds();
      AffineTransform oldTrans = g.getTransform();
      
      g.setColor(Color.black);
      
      double scale = src.getScale();
      Rectangle2D maxBounds =
        g.getFont().getMaxCharBounds(g.getFontRenderContext());
      if(
        src.getScale() >= maxBounds.getWidth()*0.3 &&
        src.getScale() >= maxBounds.getHeight()*0.3
      ) {
        double fudgeAcross = 0.0;
        double fudgeDown = 0.0;
        if (direction == src.HORIZONTAL) {
          fudgeAcross = 0.0 /*- maxBounds.getCenterX()*/;
          fudgeDown = depth * 0.5 - maxBounds.getCenterY();
        } else {
          fudgeAcross = depth * 0.5 - maxBounds.getCenterX();
          fudgeDown = scale * 0.5 - maxBounds.getCenterY();
        }
        
        int leading;
        int trailing;
        int symOffset = src.getRange().getMin();
        double graphOffset = src.sequenceToGraphics(symOffset);
        if(src.getDirection() == src.HORIZONTAL) {
          leading = src.graphicsToSequence(oldClip.getMinX());
          trailing = src.graphicsToSequence(oldClip.getMaxX());
          g.translate(-graphOffset, 0.0);
          g.setClip(AffineTransform.getTranslateInstance(-graphOffset, 0.0)
            .createTransformedShape(oldClip));
        } else {
          leading = src.graphicsToSequence(oldClip.getMinY());
          trailing = src.graphicsToSequence(oldClip.getMaxY());
          g.translate(0.0, -graphOffset);
          g.setClip(AffineTransform.getTranslateInstance(0.0, -graphOffset)
            .createTransformedShape(oldClip));
        }
        Rectangle2D clip = g.getClipBounds();
        
        int min = Math.max(src.getRange().getMin(), leading);
        int max = Math.min(src.getRange().getMax(), trailing+1);
        
        System.out.println("oldTrans: " + oldTrans);
        System.out.println("pos: " + src.getRange());
        System.out.println("symOffset: " + symOffset);
        System.out.println("graphOffset: " + graphOffset);
        System.out.println("leading: " + leading);
        System.out.println("trailing: " + trailing);
        System.out.println("min: " + min);
        System.out.println("max: " + max);
        System.out.println("-");
        
        for (int sPos = min; sPos <= max; ++sPos) {
          double gPos = src.sequenceToGraphics(sPos - symOffset + 1);
          char c = seq.symbolAt(sPos).getToken();
          if (direction == SequencePanel.HORIZONTAL) {
            //charBox.x = gPos;
            //g.drawString(
            //  String.valueOf(c),
            //  (int) (gPos + fudgeAcross), (int) fudgeDown
            //);
            g.drawString(
              String.valueOf(sPos).substring(0, 1),
              (int) (gPos + fudgeAcross), (int) fudgeDown
            );
            if(sPos == 10) {
              g.draw(new Rectangle2D.Double(gPos, 0.0, src.getScale(), 10.0));
            }
          } else {
            //charBox.y = gPos;
            g.drawString(
              String.valueOf(c),
              (int) fudgeAcross, (int) (gPos + fudgeDown)
            );
          }
          //g.draw(charBox);
        }
      }
      
      g.setTransform(oldTrans);
      g.setClip(oldClip);
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
