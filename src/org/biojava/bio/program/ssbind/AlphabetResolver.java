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

package org.biojava.bio.program.ssbind;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.symbol.FiniteAlphabet;

/**
 * <code>AlphabetResolver</code> objects are helpers which determine
 * which type of sequence <code>Alphabet</code> to expect from a
 * search result.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
class AlphabetResolver
{
    /**
     * <code>resolveAlphabet</code> returns an appropriate
     * <code>Alphabet</code> for an arbitrary identifier.
     *
     * @param identifier a <code>String</code> identifier.
     *
     * @return a <code>FiniteAlphabet</code> value.
     *
     * @exception BioException if the identifier is not known.
     */
    FiniteAlphabet resolveAlphabet(String identifier)
        throws BioException
    {
        // For (t)blastn/p/x
        if (identifier.endsWith("blastn"))
            return DNATools.getDNA();
        else if (identifier.endsWith("blastp") ||
                 identifier.endsWith("blastx"))
            return ProteinTools.getAlphabet();
        // For Fasta
        else if (identifier.equalsIgnoreCase("dna"))
            return DNATools.getDNA();
        else if (identifier.equalsIgnoreCase("protein"))
            return ProteinTools.getAlphabet();
        else
            throw new BioException("Failed to resolve sequence type from identifier '"
                                   + identifier
                                   + "'");
    }
}
