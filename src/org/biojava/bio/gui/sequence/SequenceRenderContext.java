/*
 * BioJava development code
 * 
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 * 
 * http://www.gnu.org/copyleft/lesser.html
 * 
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 * 
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 * 
 * http://www.biojava.org
 * 
 */

package org.biojava.bio.gui.sequence;

import java.awt.*;
import javax.swing.*;

import org.biojava.utils.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.gui.*;

/**
 * A context within sequence information may be rendered. It encapsulates the
 * rendering direction, the size of the leading and trailing (header/footer
 *, left/right areas), scale and the currently rendered sequence.
 *
 * @author     Thomas Down
 * @author     Matthew Pocock
 */
public interface SequenceRenderContext extends SwingConstants {
  public static final ChangeType REPAINT = new ChangeType(
    "Something that affects rendering has changed",
    "org.biojava.bio.gui.sequence.SequenceRenderContext",
    "REPAINT"
  );
  
  public static final ChangeType LAYOUT = new ChangeType(
    "Something that affects layout has changed",
    "org.biojava.bio.gui.sequence.SequenceRenderContext",
    "LAYOUT",
    REPAINT
  );
  
  /**
   *  Gets the direction in which this context expects sequences to be rendered
   * - HORIZONTAL or VERTICAL.
   *
   * @return    The Direction value 
   */
  int getDirection();

  /**
   *  Gets the scale as pixles per Symbol 
   *
   * @return    The scale value 
   */
  double getScale();

  /**
   *  Converts a sequence index into a graphical coordinate. You will need to
   * use this in conjunction with getDirection to correctly lay graphics out.
   *
   * @param  i  Index within the sequence
   * @return    Equivalent graphical position in pixles 
   */
  double sequenceToGraphics(int i);

  /**
   *  Converts a graphical position into a sequence coordinate. You will need
   * to have used getDirection to decide wether to use the x or y coordinate.
   *
   * @param  d  A pixle position
   * @return    The corresponding sequence index 
   */
  int graphicsToSequence(double d);
  
  /**
   *  Converts a graphical position into a sequence coordinate. This will
   * use getDirection to decide wether to use the x or y coordinate.
   *
   * @param  point  a point representing the position
   * @return the corresponding sequence index 
   */
  int graphicsToSequence(Point point);

  /**
   *  The Sequence that is currently rendered by this SequenceRenderContext. 
   *
   * @return    The Sequence value 
   */
  SymbolList getSequence();

  /**
   *  Gets the LeadingBorder attribute of the SequenceRenderContext object.
   * This represents the space between the beginning of the redering area and
   * the beginning of the sequence.
   *
   * @return    The LeadingBorder value 
   */
  Border getLeadingBorder();

  /**
   *  Gets the TrailingBorder attribute of the SequenceRenderContext object.
   * This represents the space between the end of the sequence and the end of
   * the redering area.
   *
   * @return    The TrailingBorder value 
   */
  Border getTrailingBorder();

  /**
   *  Gets the Font attribute of the SequenceRenderContext object 
   *
   * @return    The Font value 
   */
  Font getFont();

  /**
   * The metric object for the 'border' area - the area between the extent of
   * the rendered area and the beginning or end of the sequence. This provides
   * information about its size, and hints about how to align information within
   * the borders.
   *
   * @author     mrp 
   * @created    01 November 2000 
   */
  public static class Border
  implements java.io.Serializable, SwingConstants {
    private double size = 0.0;
    private int alignment = CENTER;

    public Border() {
      alignment = CENTER;
    }

    /**
     *  Sets the size of the border in number of pixles.
     *
     * @param  size  The new size in pixles 
     */
    public void setSize(double size) {
      this.size = size;
    }

    /**
     *  Sets the Alignment attribute of the Border object. This will be one of
     * the values LEADING, TRAILING or CENTER.
     *
     * @param  alignment                     The new Alignment value 
     * @exception  IllegalArgumentException  Description of Exception 
     */
    public void setAlignment(int alignment)
         throws IllegalArgumentException {
      switch (alignment) {
        case LEADING:
        case TRAILING:
        case CENTER:
          int old = this.alignment;
          this.alignment = alignment;
          break;
        default:
          throw new IllegalArgumentException(
              "Alignment must be one of the constants LEADING, TRAILING or CENTER"
          );
      }
    }

    /**
     *  Gets the current size of the border in pixles.
     *
     * @return    The Size value 
     */
    public double getSize() {
      return size;
    }

    /**
     *  Gets the Alignment - one of LEADING, TRAILING or CENTER. 
     *
     * @return    The alignment value 
     */
    public int getAlignment() {
      return alignment;
    }
  }
}
