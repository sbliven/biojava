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

package org.biojava.bio.symbol;

import org.biojava.bio.symbol.CodonPref;

public interface CodonPrefFilter
{
    /**
     * indicates if the current CodonPref is to be accepted
     */
    public boolean isRequired(String name);

    /**
     * handles storage of a CodonPref object
     */
    public void put(CodonPref codonPref);

    public class FilterByName implements CodonPrefFilter
    {
        String name;
        CodonPref filteredCodonPref;

        public FilterByName(String name)
        {
            this.name = name;
        }

        public boolean isRequired(String name)
        {
            return name.equals(this.name);
        }

        public void put(CodonPref codonPref)
        {
            filteredCodonPref = codonPref;
        }

        public CodonPref getCodonPref()
        {
            return filteredCodonPref;
        }
    }
}

