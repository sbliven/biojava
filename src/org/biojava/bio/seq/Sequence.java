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

package org.biojava.bio.seq;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * A sequence.
 * <P>
 * This interface is a symbol list, so it contains symbols. It is annotatable
 * so that you can add annotation to it, and it is a FeatureHolder so that you
 * can add information about specific regions.
 * <P>
 * It is expected that there may be several implementations of this interface,
 * each of which may be fairly heavy-weight. It takes the SymbolList interface
 * that is nice mathematically, and turns it into a biologically useful object.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */

public interface Sequence extends SymbolList, FeatureHolder, Annotatable {
  /**
   * The URN for this sequence. This will be something like
   * <code>urn:sequence/embl:U32766</code> or
   * <code>urn:sequence/fasta:sequences.fasta|hox3</code>.
   *
   * @return the urn as a String
   */
  String getURN();
  
  /**
   * The name of this sequence.
   * <P>
   * The name may contain spaces or odd characters.
   *
   * @return the name as a String
   */
  String getName();
}
