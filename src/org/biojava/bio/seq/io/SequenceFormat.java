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
 * construct Ensembl CORBA objects, just by specifying an Ensembl
 * SequenceFactory.</p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 */

public interface SequenceFormat
{
    /**
     * <p>This is where the various <code>SequenceFormat</code>
     * implementations should register the names of the formats they
     * are able to write. The Map key should be the implementation's
     * classname and the values a Set of Strings describing the
     * formats.</p>
     *
     * <p>E.g. org.biojava.bio.seq.io.EmblLikeFormat</p>
     *
     * <ul>
     * <li>Key: org.biojava.bio.seq.io.EmblLikeFormat</li>
     * <li>Values: "Embl", "Swissprot"</li>
     * </ul>
     *
     * <p>When writeSequence() is called with the format argument
     * "EMBL" (the parameter is not case-sensitive) the
     * <code>SeqFileFormerFactory</code> checks the values registered
     * here against the argument. As the format "Embl" is registered,
     * it attempts to load a class named "EmblFileFormer". If
     * successful, the factory method returns an instance of this
     * class and writeSequence() uses this to write formatted strings
     * to the PrintStream.</p>
     */
    public static final Map FORMATS = new HashMap();

    /**
     * <code>writeSequence</code> writes a sequence to the specified
     * PrintStream, using the default format.
     *
     * @param seq the sequence to write out.
     * @param os the printstream to write to.
     */
    public void writeSequence(Sequence seq, PrintStream os)
	throws IOException;

    /**
     * <code>writeSequence</code> writes a sequence to the specified
     * <code>PrintStream</code>, using the specified format.
     *
     * @param seq a <code>Sequence</code> to write out.
     * @param format a <code>String</code> indicating which sub-format
     * of those available from a particular
     * <code>SequenceFormat</code> implemention to use when
     * writing. E.g. when writing a sequence using the EmblLikeFormat
     * implementation, choices will be 'EMBL', 'SwissProt' etc. The
     * available choices may be obtained calling the
     * <code>getFormats</code> method on a <code>SequenceFormat</code>
     * instance.
     * @param os a <code>PrintStream</code> object.
     *
     * @exception IOException if an error occurs.
     */
    public void writeSequence(Sequence seq, String format, PrintStream os)
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
     * @return a boolean indicating whether or not the stream contains
     * any more sequences.
     *
     * @throws IOException if an error occurs while reading from the
     * stream.
     * @throws IllegalSymbolException if it is not possible to
     * translate character data from the stream into valid BioJava
     * symbols.
     * @throws BioException if there is an error in the format of the
     * stream.
     */
    public boolean readSequence(BufferedReader      reader,
				SymbolTokenization  symParser,
				SeqIOListener       listener)
	throws BioException, IllegalSymbolException, IOException;

    /**
     * <code>getFormats</code> returns a set of String identifiers for
     * the format(s) written by a <code>SequenceFormat</code>
     * implementation.
     *
     * @return a <code>Set</code> of Strings.
     */
    public Set getFormats();

    /**
     * <code>getDefaultFormat</code> returns the String identifier for
     * the default format written by a <code>SequenceFormat</code>
     * implementation.
     *
     * @return a <code>String</code>.
     */
    public String getDefaultFormat();
}
