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
import java.lang.String;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Defines what a sequence format does.
 *
 * <p>Sequence formats are responsible for both reading and writing a
 * sequence in a format, presumably in such a way as the written
 * record can be read back in by the same formatter.</p>
 *
 * <p>Where possible, the methods are parameterised so that they
 * don't need any knowledge of the specific implementation of Sequence
 * they are reading or writing. E.g. it should be possible to
 * parameterise readSequence to read from a Genbank stream and
 * construct ensembl corba objects, just by specifying an ensembl
 * SequenceFactory.</p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 */

public interface SequenceFormat
{
    /**
     * Write out a sequence to the specified printstream.
     *
     * @param seq the sequence to write out.
     * @param os the printstream to write to.
     */
    void writeSequence(Sequence seq, PrintStream os)
	throws IOException;

    /**
     * <code>writeSequence</code> writes a sequence to a specified
     * PrintStream in a specified format.
     *
     * @param seq a <code>Sequence</code> to write out.
     * @param format a <code>String</code> indicating which sub-format
     * of those available from a particular
     * <code>SequenceFormat</code> implemention to use when
     * writing. E.g. when writing a sequence using the EmblLikeFormat
     * implementation, choices will be 'EMBL', 'SwissProt' etc. The
     * available choices may be obtained calling the
     * <code>getFormatNames</code> method on a
     * <code>SequenceFormat</code> instance.
     * @param os a <code>PrintStream</code> object.
     *
     * @exception IOException if an error occurs.
     */
    void writeSequence(Sequence seq, String format, PrintStream os)
	throws IOException;
    
    /**
     * Read a sequence and pass data on to a SeqIOListener.
     *
     * @param reader The stream of data to parse.
     * @param symParser A SymbolParser defining a mapping from
     * character data to Symbols.
     * @param listener A listener to notify when data is extracted
     * from the stream.
     *
     * @return a boolean indicating whether or not the stream
     * contains any more sequences.
     *
     * @throws IOException if an error occurs while reading from the
     * stream.
     * @throws IllegalSymbolException if it is not possible to
     * translate character data from the stream into valid BioJava
     * symbols.
     * @throws BioException if there is an error in the format of the
     * stream.
     */
    public boolean readSequence(BufferedReader reader,
				SymbolParser   symParser,
				SeqIOListener  listener)
	throws BioException, IllegalSymbolException, IOException;
}
