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

import java.util.*;

/**
 * Cross product of two or more alphabets.  This is provided primarily
 * to assist in the implemention of a `multi-headed' hidden markov
 * model.  For instance, in a pair HMM intended for aligning DNA
 * sequence, the emmision alphabet will be (DNA x DNA).
 *
 * @author Thomas Down
 */

public interface CrossProductAlphabet extends Alphabet {
    /**
     * Return an ordered List of the alphabets which make up this
     * compound alphabet.  The returned List should be immutable.
     *
     */

    public List getAlphabets();

    /**
     * Get a residue from the CrossProductAlphabet which corresponds
     * to the specified ordered list of residues.
     *
     * @param rl A list of residues.
     * @throws IllegalAlphabetException if the members of rl are
     *            not Residues over the alphabets returned fro
     *            <code>getAlphabets</code>
     */

    public CrossProductResidue getResidue(List rl) 
	         throws IllegalAlphabetException;
}
