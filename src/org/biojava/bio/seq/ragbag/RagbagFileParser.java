/**
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
 
package org.biojava.bio.seq.ragbag;

import org.biojava.bio.seq.io.SequenceBuilder;
import org.biojava.bio.BioException;

/**
 * Interface for classes that implement format-specific behaviour
 * for parsing input sequence/feature files.
 */
public interface RagbagFileParser
{
  // as the file was sniffed to determine the format,
  // it would be dangerous to permit the user to change it!
/**
 * set SeqIOListener object for this parser.
 */
  public void setListener(SequenceBuilder builder);

/**
 * parse specified input file to generate SeqIOListener calls
 * to the designated listener.
 */
  public void parse() throws BioException;
}
