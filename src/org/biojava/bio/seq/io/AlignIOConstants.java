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

/**
 * <code>AlignIOConstants</code> contains constants used to identify
 * sequence formats, alphabets etc, in the context of reading and
 * writing alignments.
 *
 * <p>An <code>int</code> used to specify symbol alphabet and
 * sequence format type is derived thus:</p>
 *
 * <ul>
 *   <li>
 *    The two least significant bytes are reserved for format types
 *    such as MSF, CLUSTAL etc.
 *   </li>
 *
 *   <li>
 *    The two most significant bytes are reserved for alphabet and
 *    symbol information such as AMBIGUOUS, DNA, RNA, AA etc.
 *   </li>
 *
 *   <li>
 *    Bitwise OR combinations of each component <code>int</code> are used
 *    to specify combinations of format type and symbol information. To
 *    derive an <code>int</code> identifier for DNA with ambiguity codes
 *    in Fasta format, bitwise OR the AMBIGUOUS, DNA and FASTA values.
 *   </li>
 * </ul>
 *
 * @author Keith James
 */
public final class AlignIOConstants
{
    public static final int UNKNOWN = 100;
    public static final int     RAW = 101;
    public static final int   FASTA = 102;
    public static final int CLUSTAL = 103;
    public static final int     MSF = 104;

    public static final int RAW_DNA     = RAW     | SeqIOConstants.DNA;
    public static final int RAW_RNA     = RAW     | SeqIOConstants.RNA;
    public static final int RAW_AA      = RAW     | SeqIOConstants.AA;
    public static final int FASTA_DNA   = FASTA   | SeqIOConstants.DNA;
    public static final int FASTA_RNA   = FASTA   | SeqIOConstants.RNA;
    public static final int FASTA_AA    = FASTA   | SeqIOConstants.AA;
    public static final int CLUSTAL_DNA = CLUSTAL | SeqIOConstants.DNA;
    public static final int CLUSTAL_RNA = CLUSTAL | SeqIOConstants.RNA;
    public static final int CLUSTAL_AA  = CLUSTAL | SeqIOConstants.AA;
    public static final int MSF_DNA     = MSF     | SeqIOConstants.DNA;
    public static final int MSF_RNA     = MSF     | SeqIOConstants.RNA;
    public static final int MSF_AA      = MSF     | SeqIOConstants.AA;
}
