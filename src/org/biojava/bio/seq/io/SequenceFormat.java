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


package org.biojava.bio.seq.io;

import java.util.*;
import java.io.*;

import org.biojava.bio.seq.*;

/**
 * Defines what a sequence format does.
 * <P>
 * Sequence formats are responsible for both reading and writing a sequence in
 * a format, presumably in such a way as the written record can be read back in
 * by the same formatter.
 * <P>
 * Where possible, the methods are parameterised so that they don't need any
 * knowledge of the specific implementation of Sequence they are reading or
 * writing. E.g. it should be possible to parameterise readSequence to read from
 * a GENBANK stream and construct ensembl corba objects, just by specifying an
 * ensembl SequenceFactory.
 *
 * @author Matthew Pocock
 */
public interface SequenceFormat {
  /**
   * Read in a single sequence.
   * <P>
   * The format is responsible for converting characters in a stream into a
   * complete sequence. It should read from the stream contained in 
   * <code>context</code>, parse the residue characters using
   * <code>resParser</code> and generate a sequence from the resulting
   * residue list using <code>sf</code>. Any non-sequence information within
   * the format should be read in either as features, or as annotation.
   *
   * @param context the context to parse from
   * @param resParser the parser to parse chars to Residue objects
   * @param sf the sequence factory for generating a full sequence
   * @return the resulting sequence
   */
  Sequence readSequence(StreamReader.Context context,
                        ResidueParser resParser,
                        SequenceFactory sf)
         throws SeqException, IllegalResidueException, IOException;

  void writeSequence(Sequence seq, PrintStream os) throws IOException;
}
