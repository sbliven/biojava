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
import java.awt.geom.*;
import org.biojava.bio.gui.*;
import org.biojava.bio.seq.*;

/**
 * The interface for things that can render a line of information about a
 * sequence.
 * <P>
 * Renderers are always activated within the context of a particular sequence
 * panel. A single Renderer can be shaired among many sequence panels, or added
 * multiple times to the same panel. The renderer is required to request how
 * much leading and trailing space it requires, as well as the depth (space
 * orthoganal to the direction that the sequence is rendered).
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */
public interface SequenceRenderer {
  /**
   * Render a portion (possibly all) of the information for sp to g, displaying
   * all of the data that would fall within seqBox.
   *
   * @param g the Graphics2D to render to
   * @param sp the SequencePanel that encapsulates the information to render
   * @param seqBox the rectangle within which to render sequence stuff
   */
  void paint(Graphics2D g, SequenceRenderContext sp, Rectangle2D seqBox);
  
  /**
   * Retrieve the depth of this renderer when rendering sp.
   * <P>
   * The depth may vary between sequence panels - for example based upon
   * sequence length.
   *
   * @param sp the SequencePanel to return info for
   * @return the depth of the renderer for that sequence panel
   */
    double getDepth(SequenceRenderContext sp);
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
}
