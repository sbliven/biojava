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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import org.biojava.utils.*;
import org.biojava.bio.gui.*;
import org.biojava.bio.seq.*;

import java.util.List;

/**
 * The interface for things that can render a line of information about a
 * sequence.
 * <P>
 * Renderers are always activated within the context of a particular sequence
 * panel. A single Renderer can be shaired among many sequence panels, or added
 * multiple times to the same panel. The renderer is required to request how
 * much leading and trailing space it requires, as well as the depth (space
 * orthoganal to the direction that the sequence is rendered).
 * <P>
 * The leading and trailing distances are the number of pixles overhang needed
 * to cleanly render any line of sequence information. For example, a ruler will
 * need trailing space to render the total sequence length at the end.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */
public interface SequenceRenderer {
  
  /**
   * Render a portion (possibly all) of the information for sp to g, displaying
v   * all of the data that would fall within seqBox.
   *
   * @param g the Graphics2D to render to
   * @param sp the SequencePanel that encapsulates the information to render
   * @param min the minimum symbol to render (inclusive)
   * @param max the maximum symbol to render (inclusive)
   */
  void paint(
    Graphics2D g, SequenceRenderContext sp,
    int min, int max
  );
  
  /**
   * Retrieve the depth of this renderer when rendering sp.
   * <P>
   * The depth may vary between sequence panels - for example based upon
   * sequence length. Each line of information in the SequenceRendererContext
   * only renders a region of the sequence. The depth for one complete line may
   * be different from that for another due to the sequence having more or less
   * information in that region to show. For example, a feature renderer
   * implementation may chose to collapse down to a depth of zero pixles if
   * there are no features to render within a region.
   *
   * @param sp the SequencePanel to return info for
   * @param min the first symbol rendered (inclusive)
   & @param max the last symbol rendered (inclusive)
   * @return the depth of the renderer for that sequence panel
   */
  double getDepth(SequenceRenderContext sp, int min, int max);

  /**
   * Retrieve the minimum leading distance for this renderer when rendering sp.
   * <P>
   * The leading distance may vary between sequence panels - for example based
   * upon sequence length.
   *
   * @param sp the SequencePanel to return info for
   * @return the leading distance of the renderer for that sequence panel
   */
  double getMinimumLeader(SequenceRenderContext sp);
    
  /**
   * Retrieve the minimum trailing distance for this renderer when rendering sp.
   * <P>
   * The trailing distance may vary between sequence panels - for example based
   * upon sequence length.
   *
   * @param sp the SequencePanel to return info for
   * @return the trailing distance of the renderer for that sequence panel
   */
  double getMinimumTrailer(SequenceRenderContext sp);
  
  /**
   * Produce a SequenceViewerEvent in response to a mouse gesture.
   * <P>
   * A SequenceRenderer that performs any form of coordinate remapping should
   * ensure that it apropreately transforms the mouse event. However, in the
   * SequenceViewerEvent returned, the MouseEvent should be in untransformed
   * coordinates.
   * <P>
   * The SequenceRenderer implementation should append itself to the path list
   * before constructing the SequenceViewerEvent.
   *
   * @param src the SequenceRenderContext currently in scope
   * @param me  a MouseEvent that caused this request
   * @param path the List of SequenceRenderer instances passed through so far
   * @param min the minimum sequence index
   * @param max the maximum sequence index
   * @return a SequenceViewerEvent encapsulating the mouse gesture
   *
   * @since 1.2
   */
  SequenceViewerEvent processMouseEvent(
    SequenceRenderContext src,
    MouseEvent me,
    List path,
    int min, int max
  );
  
  public static class RendererForwarder extends ChangeForwarder {
    public RendererForwarder(SequenceRenderer source, ChangeSupport cs) {
      super(source, cs);
    }
    
    public ChangeEvent generateEvent(ChangeEvent ce) {
      ChangeType cType = ce.getType();
      ChangeType newType;
      if(cType.isMatchingType(SequenceRenderContext.LAYOUT)) {
        newType = SequenceRenderContext.LAYOUT;
      } else if(cType.isMatchingType(SequenceRenderContext.REPAINT)) {
        newType = SequenceRenderContext.REPAINT;
      } else {
        return null;
      }
      return new ChangeEvent(getSource(), newType, null, null, ce);
    }
  }
}
