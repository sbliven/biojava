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

// The graphics model in Java
// drawing space -> applet space -> device space
// All operations are cumulative, including translates
// translates will move drawing rightward/downward for any supplied value

/**
 * Render the symbols of a sequence.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 * @author David Huen
 */ 


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
      // AffineTransform oldTrans = g.getTransform();
      
      g.setColor(Color.black);
      
      double scale = src.getScale();
      Rectangle2D maxBounds =
        g.getFont().getMaxCharBounds(g.getFontRenderContext());
      if(
        // symbol must be larger than 30% of char size
        // attempting to render
        src.getScale() >= maxBounds.getWidth()*0.3 &&
        src.getScale() >= maxBounds.getHeight()*0.3
      ) {
        double fudgeAcross = 0.0;
	// intended to center text in band
        double fudgeDown = 0.0;
        if (direction == src.HORIZONTAL) {
            fudgeAcross = - maxBounds.getCenterX();
            fudgeDown = depth * 0.5 - maxBounds.getCenterY();
        } else {
            fudgeAcross = depth * 0.5 - maxBounds.getCenterX();
            fudgeDown = scale * 0.5 - maxBounds.getCenterY();
        }
        
        int leading;       // these correspond to the symbol index value
        int trailing;      // of the ends of the clip region
        int symOffset = src.getRange().getMin();    // first symbol by base index value
        double graphOffset = src.sequenceToGraphics(symOffset);   // by pixels
        if(src.getDirection() == src.HORIZONTAL) {
	  // compute base nos. associated with ends of clip region
          leading = src.graphicsToSequence(oldClip.getMinX());
          trailing = src.graphicsToSequence(oldClip.getMaxX());
	  // A transform to render the symbols is setup in 
          // SequencePanel.paintComponent().
          // start of leader will place you at leftmost edge of draw area.

          // the default clip region from SequencePanel.paintComponent()
          // spans the leader, sequence range and trailer.
          // it is adequate for this method although we could further
          // restrict it to the sequence region itself 

          // g.translate(-graphOffset, 0.0);
          // g.setClip(AffineTransform.getTranslateInstance(-graphOffset, 0.0)
          //  .createTransformedShape(oldClip));
        } else {
          leading = src.graphicsToSequence(oldClip.getMinY());
          trailing = src.graphicsToSequence(oldClip.getMaxY());

          // g.translate(0.0, -graphOffset);
          // g.setClip(AffineTransform.getTranslateInstance(0.0, -graphOffset)
          //   .createTransformedShape(oldClip));
        }
	//        Rectangle2D clip = g.getClipBounds();
        
	// can this ever happen? leading > pos.getMin?, 
        //                      pos.getMax() > trailing?        
        int min = Math.max(src.getRange().getMin(), leading);
        int max = Math.min(src.getRange().getMax(), trailing+1);

        /* System.out.println("oldTrans: " + oldTrans);
        System.out.println("pos: " + src.getRange());
        System.out.println("symOffset: " + symOffset);
        System.out.println("graphOffset: " + graphOffset);
        System.out.println("leading: " + leading);
        System.out.println("trailing: " + trailing);
        System.out.println("min: " + min);
        System.out.println("max: " + max);
        System.out.println("-"); */
        
        for (int sPos = min; sPos <= max; ++sPos) {
	  double gPos = src.sequenceToGraphics(sPos /* - symOffset */ + 1);
          char c = seq.symbolAt(sPos).getToken();
          if (direction == SequencePanel.HORIZONTAL) {
            //charBox.x = gPos;
            g.drawString(
              String.valueOf(c),
              (int) (gPos + fudgeAcross), (int) fudgeDown
            );
            // g.drawString(
            //  String.valueOf(sPos).substring(0, 1),
            //  (int) (gPos + fudgeAcross), (int) fudgeDown
            // );
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
      
//      g.setTransform(oldTrans);
//      g.setClip(oldClip);
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
