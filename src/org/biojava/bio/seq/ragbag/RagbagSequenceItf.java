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
 
import java.io.*;
import java.util.*;
 
import org.xml.sax.*;
import org.biojava.utils.stax.*;
import org.apache.xerces.parsers.*;
 
import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.io.game.*;
import org.biojava.utils.*;
 
/**
 * object that instantiates a sequence in Ragbag.
 * It accepts a single sequence file and any number
 * of feature files that are to be applied to that
 * sequence.
 *
 * @author David Huen
 * @since 1.2
 */
interface RagbagSequenceItf extends Sequence
{

/**
 * Processes a file that contains features only.
 * It may be called repeatedly.
 * <p>
 * it should be noted that the file must NOT create any features that
 * don't have a location or the makeSequence() will barf.  This happens
 * with GAME on files that don't have a &lt;seq&gt; element with sequence
 * info.
 * </p>
 * @param filename name of file to be processed (currently GAME format only).
 */
  public void addFeatureFile(File thisFile) 
    throws BioException;

/**
 * Processes a file that contains sequence and (optionally) features.
 * Must be called ONCE only.
 * <p>
 * @param filename name of file to be processed (currently GAME format only).
 */
  public void addSequenceFile(File thisFile)
    throws BioException;

/**
 * completes instantiation of the sequence
 */ 
  public void makeSequence()
    throws BioException;

}
