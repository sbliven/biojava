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

import java.io.PrintStream;

import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;

/**
 * Objects implementing the <code>SeqFileFormer</code> interface are
 * responsible for the detailed formatting of sequence data prior to
 * writing to a <code>PrintStream</code>. They are typically
 * dynamically loaded by a <code>SequenceFormat</code> implementation
 * when a complex file format needs to be written. Example
 * implementations are <code>EmblFileFormer</code> and
 * <code>GenbankFileFormer</code>. Some file formats, such as Fasta,
 * are very simple and don't require a <code>SeqFileFormer</code>.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public interface SeqFileFormer extends SeqIOListener
{
    /**
     * <code>getPrintStream</code> returns the
     * <code>PrintStream</code> to which an instance will write the
     * formatted data. If this has not been set, an implementation
     * should default to System.out.
     *
     * @return the <code>PrintStream</code> which will be written to.
     */
    public PrintStream getPrintStream();

    /**
     * <code>setPrintStream</code> informs an instance which
     * <code>PrintStream</code> to use.
     *
     * @param stream a <code>PrintStream</code> to write to.
     */
    public void setPrintStream(PrintStream stream);

    /**
     * <code>formatLocation</code> creates a String representation of
     * a <code>Location</code>. The strand may not be relevant for all
     * formats (e.g. it is relevant for Genbank and EMBL, but not for
     * SwissProt). In such cases the implementation may accept a
     * strand of 'unknown', '0' or '.'.
     *
     * @param loc a <code>Location</code> to format.
     * @param strand a <code>StrandedFeature.Strand</code> indicating
     * any relevant strandedness.
     *
     * @return a <code>String</code> representation.
     */
    public String formatLocation(final Location               loc,
				 final StrandedFeature.Strand strand);
}
