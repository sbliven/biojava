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
import java.awt.geom.*;
import java.awt.font.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.gui.*;

import java.util.List;

public class SimpleLabelRenderer
extends AbstractForwarder
implements LabelRenderer{
  private static final ChangeType LABEL = new ChangeType(
    "The label has changed",
    "org.biojava.bio.gui.sequence.SimpleLabelRenderer",
    "LABEL"
  );
  
  private static final AffineTransform FLIP =
    new AffineTransform(0.0, 1.0, -1.0, 0.0, 0.0, 0.0); 
  private String label;
  private Shape labelGlyphH;
  private Shape labelGlyphV;
  
  protected Shape getLabelGlyph(
        SequenceRenderContext src,
        FontRenderContext frc
  ) {
    Shape s;
    
    if (src.getDirection() == src.HORIZONTAL) {
      if(labelGlyphH == null) {
        Font font = src.getFont();
        labelGlyphH = font.createGlyphVector(frc, label).getOutline();
      }
      s = labelGlyphH;
    } else {
      if(labelGlyphV == null) {
        Font font = src.getFont().deriveFont(FLIP);
        labelGlyphV = font.createGlyphVector(frc, label).getOutline();
      }
      s = labelGlyphV;
    }
    
    return s;
  }
  
  public void setLabel(String label)
  throws ChangeVetoException {
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(SequenceRenderContext.LAYOUT);
      synchronized(cs) {
        ChangeEvent ce = new ChangeEvent(
          this, SequenceRenderContext.LAYOUT, null, null, new ChangeEvent(
            this, LABEL, this.label, label
          )
        );
        cs.firePreChangeEvent(ce);
        this.label = label;
        this.labelGlyphH = null;
        this.labelGlyphV = null;
        cs.firePostChangeEvent(ce);
      }
    } else {
      this.label = label;
      this.labelGlyphH = null;
      this.labelGlyphV = null;
    }
  }
    
  public String getLabel() {
    return label;
  }

  public double getMinimumWidth(SequenceRenderContext sp) {
    if(label == null) {
      return 0.0;
    }
    Font f = sp.getFont();
    FontRenderContext frc = new FontRenderContext(null, true, true);
    GlyphVector gv = f.createGlyphVector(frc, label);
    return gv.getVisualBounds().getWidth();
  }
  
  public void paint(
    Graphics2D g, SequenceRenderContext sp,
    int min, int max, SequenceRenderContext.Border side
  ) {
/*    if(label != null) {
      Rectangle2D.Double labelBox = null;
      Shape labelGlyph = getLabelGlyph(sp, g.getFontRenderContext());
      if (sp.getDirection() == sp.HORIZONTAL) {
        labelBox = new Rectangle2D.Double(
          0, 0,
          leading.getSize(), getDepth()
        );
      } else {
        labelBox = new Rectangle2D.Double(
          seqBox.getMinX(), seqBox.getMinY() - side.getSize(),
          seqBox.getWidth(), side.getSize()
        );
      }
      renderLabel(g, labelGlyph, labelBox, sp, side);
    }*/
  }
  
  private void renderLabel(
      Graphics2D g,
      Shape gv, Rectangle2D labelBox,
      SequenceRenderContext sp, SequenceRenderContext.Border border
    ) {
      Rectangle2D bounds = gv.getBounds2D();
      double along = 0.0;
      double across = 0.0;
      if (sp.getDirection() == sp.HORIZONTAL) {
        across = labelBox.getCenterY() - bounds.getCenterY();
	int balign = border.getAlignment();
        
        if (balign == border.LEADING) 
            along = labelBox.getMinX() - bounds.getMinX();
        else if (balign == border.TRAILING)
            along = labelBox.getMaxX() - bounds.getMaxX();
        else if (balign == border.CENTER)
            along = labelBox.getCenterX() - bounds.getCenterX();

        AffineTransform at = g.getTransform();
        g.translate(along, across);
        g.fill(gv);
        g.draw(gv);
        g.setTransform(at);
      } else {
        across = labelBox.getCenterX() - bounds.getCenterX();
	int balign = border.getAlignment();

	if (balign == border.LEADING)
            along = labelBox.getMinY() - bounds.getMinY();
        else if (balign == border.TRAILING)
            along = labelBox.getMaxY() - bounds.getMaxY();
        else if (balign == border.CENTER)
            along = labelBox.getCenterY() - bounds.getCenterY();

        AffineTransform at = g.getTransform();
        g.translate(across, along);
        g.fill(gv);
        g.draw(gv);
        g.setTransform(at);
      }
    }
}
