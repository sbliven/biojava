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

package org.biojava.bio.program.homologene;

import java.net.URL;
import java.util.Iterator;

/**
 * Homologene is a NCBI dataset that curates sets
 * of orthologues from the reference model ogranisms.
 * <p>
 * This class is a Collection of methods for handling
 * data from the Homologene dataset.
 *
 * @author David Huen
 */
public class HomologeneTools
{
    /**
     * get the Taxon corresponding to this Taxon ID
     */
    public static Taxon getTaxon(int taxonID)
    {
        // currently just does a linear search
        for (Iterator taxaI = Taxon.taxa.iterator(); taxaI.hasNext(); ) {
            Taxon curr = (Taxon) taxaI.next();

            if (curr.getTaxonID() == taxonID) return curr;
        }

        return null;
    }

    /**
     * add a Taxon
     */
    public static Taxon createTaxon(int taxonID, String description)
        throws DuplicateTaxonException
    {
        // first check that the taxon des not exist
        if (getTaxon(taxonID) != null) throw new DuplicateTaxonException();

        Taxon newTaxon = new Taxon.TaxonStub(taxonID, description);

        return newTaxon;
    }

    /**
     * instantiate a HomologeneDB
     */
    public static void instantiateDB(URL url)
    {

    }
}

