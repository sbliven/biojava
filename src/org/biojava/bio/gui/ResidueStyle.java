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


package org.biojava.bio.gui;

import java.awt.Paint;
import org.biojava.bio.seq.Residue;
import org.biojava.bio.seq.IllegalResidueException;

/**
 * The interface for things that say how to paint a residue.
 * <P>
 * Given a residue, this allows you to get the color to outline or fill the
 * glyphs for rendering the residue. This may be something as simple as coloring
 * dots on a scatter-plot, or labeling a key, or it may be as complicated as
 * sequence logos.
 *
 * @author Matthew Pocock
 */
public interface ResidueStyle {
  /**
   * Return the outline paint for a residue.
   *
   * @param r the residue to outline
   * @return the Paint to use
   * @throws IllegalResidueException if this ResidueStyle can not handle the
   *         residue
   */
  Paint outlinePaint(Residue r) throws IllegalResidueException;

  /**
   * Return the fill paint for a residue.
   *
   * @param r the residue to fill
   * @return the Paint to use
   * @throws IllegalResidueException if this ResidueStyle can not handle the
   *         residue
   */
  Paint fillPaint(Residue r) throws IllegalResidueException;
}
