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
 * <p>
 * This interface is a symbol list, so it contains symbols. It is annotatable
 * so that you can add annotation to it, and it is a FeatureHolder so that you
 * can add information about specific regions.
 * <p>
 * It is expected that there may be several implementations of this interface,
 * each of which may be fairly heavy-weight. It takes the SymbolList interface
 * that is nice mathematically, and turns it into a biologically useful object.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */

public interface Sequence extends SymbolList, FeatureHolder, Annotatable {
  /**
   * A <a href="http://www.rfc-editor.org/rfc/rfc2396.txt">Uniform
   * Resource Identifier</a> (URI) which identifies the sequence
   * represented by this object.  For sequences in well-known
   * database, this may be a URN, e.g.
   *
   * <pre>
   * urn:sequence/embl:AL121903
   * </pre>
   *
   * It may also be a URL identifying a specific resource, either
   * locally or over the network
   *
   * <pre>
   * file:///home/thomas/myseq.fa|seq22
   * http://www.mysequences.net/chr22.seq
   * </pre>
   *
   * @return the URI as a String
   */
  String getURN();
  
  /**
   * The name of this sequence.
   * <p>
   * The name may contain spaces or odd characters.
   *
   * @return the name as a String
   */
  String getName();
}
