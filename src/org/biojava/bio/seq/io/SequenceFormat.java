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
 * <p>Where possible, the methods are parameterised so that they don't
 * need any knowledge of the specific implementation of Sequence they
 * are reading or writing. E.g. it should be possible to parameterise
 * readSequence to read from a Genbank stream and construct Ensembl
 * CORBA objects, just by specifying an Ensembl SequenceFactory.</p>
 *
 * <p>The <code>int</code>s used to specify symbol alphabet and
 * sequence format type are arranged thus:</p>
 *
 * <ul>
 *   <li>
 *    The two least significant bytes are reserved for format types
 *    such as RAW, FASTA, EMBL etc.
 *   </li>
 *
 *   <li>
 *    The two most significant bytes are reserved for alphabet and
 *    symbol information such as AMBIGUOUS, DNA, RNA, AA etc.
 *   </li>
 *
 *   <li>
 *    Bitwise OR combinations of the <code>int</code>s are used to
 *    specify combinations of format type and symbol information. To
 *    derive an <code>int</code> identifier for DNA with ambiguity codes
 *    in Fasta format sequence, bitwise OR AMBIGUOUS, DNA and FASTA.
 *   </li>
 * </ul>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 * @author Keith James
 */

public interface SequenceFormat
{
    /**
     * <code>AMBIGUOUS</code> indicates that a sequence contains
     * ambiguity symbols. The first bit of the most significant byte
     * of the int is set.
     */
    public static final int AMBIGUOUS = 1 << 24;

    /**
     * <code>DNA</code> indicates that a sequence contains DNA
     * (deoxyribonucleic acid) symbols. The second bit of the most
     * significant byte of the int is set.
     */
    public static final int DNA = 1 << 25;

    /**
     * <code>RNA</code> indicates that a sequence contains RNA
     * (ribonucleic acid) symbols. The third bit of the most
     * significant byte of the int is set.
     */
    public static final int RNA = 1 << 26;

    /**
     * <code>AA</code> indicates that a sequence contains AA (amino
     * acid) symbols. The fourth bit of the most significant byte of
     * the int is set.
     */
    public static final int AA = 1 << 27;

    /**
     * <code>INTEGER</code> indicates that a sequence contains integer
     * alphabet symbols, such as used to describe sequence quality
     * data. The fifth bit of the most significant byte of the int is
     * set.
     */
    public static final int INTEGER = 1 << 28;

    /**
     * <code>UNKNOWN</code> indicates that the sequence format is
     * unknown.
     */
    public static final int UNKNOWN = 0;

    /**
     * <code>RAW</code> indicates that the sequence format is raw
     * (symbols only).
     */
    public static final int RAW = 1;

    /**
     * <code>FASTA</code> indicates that the sequence format is Fasta.
     */
    public static final int FASTA = 2;

    /**
     * <code>NBRF</code> indicates that the sequence format is NBRF.
     */
    public static final int NBRF = 3;

    /**
     * <code>IG</code> indicates that the sequence format is IG.
     */
    public static final int IG = 4;

    /**
     * <code>EMBL</code> indicates that the sequence format is EMBL.
     * As EMBL is always DNA, the DNA bit is already set.
     */
    public static final int EMBL = 10 | DNA;

    /**
     * <code>SWISSPROT</code> indicates that the sequence format is
     * SWISSPROT. As SWISSPROT is always AA, the AA bit is already
     * set.
     */
    public static final int SWISSPROT = 11 | AA;

    /**
     * <code>GENBANK</code> indicates that the sequence format is
     * GENBANK. As GENBANK is always DNA, the DNA bit is already set.
     */
    public static final int GENBANK = 12 | DNA;

    /**
     * <code>GENPEPT</code> indicates that the sequence format is
     * GENPEPT. As GENPEPT is always AA, the AA bit is already set.
     */
    public static final int GENPEPT = 13 | AA;

    /**
     * <code>REFSEQ</code> indicates that the sequence format is
     * REFSEQ.
     */
    public static final int REFSEQ = 14;

    /**
     * <code>GCG</code> indicates that the sequence format is GCG.
     */
    public static final int GCG = 15;

    /**
     * <code>GFF</code> indicates that the sequence format is GFF.
     */
    public static final int GFF = 20;

    /**
     * <code>PHRED</code> indicates that the sequence format is
     * PHRED. As PHRED is always DNA and contains integer quality
     * data, the DNA and INTEGER bits are already set.
     */
    public static final int PHRED = 30 | DNA | INTEGER;

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
    public boolean readSequence(BufferedReader     reader,
				SymbolTokenization symParser,
				SeqIOListener      listener)
	throws BioException, IllegalSymbolException, IOException;

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
     * writing.
     * @param os a <code>PrintStream</code> object.
     *
     * @exception IOException if an error occurs.
     * @deprecated use writeSequence(Sequence seq, PrintStream os)
     */
    public void writeSequence(Sequence seq, String format, PrintStream os)
	throws IOException;

    /**
     * <code>getDefaultFormat</code> returns the String identifier for
     * the default sub-format written by a <code>SequenceFormat</code>
     * implementation.
     *
     * @return a <code>String</code>.
     * @deprecated new implementations should only write a single
     * format.
     */
    public String getDefaultFormat();
}
