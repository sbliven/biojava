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

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class SimpleHomologeneDB implements HomologeneDB
{

    // every Orthologue is stored in a Set
    private Set orthologueSet = new HashSet();
    private Map orthologueByHomologeneID = new HashMap();

    // orthologies are also stored in a set
    private Set orthologySet = new HashSet();

    // Homologene Groups are also stored in a set
    Set groups = new HashSet();

    // indices
    private Map orthologyByTaxonID = new HashMap();
    private Map orthologyBySimilarityType = new HashMap();

    public Orthologue createOrthologue(Taxon taxon, String locusID, String homologeneID, String accession)
    {
        // create the Orthologue
        Orthologue newOrthologue = new SimpleOrthologue(taxon, locusID, homologeneID, accession);

        // stash it
        orthologueSet.add(newOrthologue);
        orthologueByHomologeneID.put(homologeneID, newOrthologue);
        return newOrthologue;
    }

    public Orthologue createOrthologue(int taxonID, String locusID, String homologeneID, String accession)
        throws IllegalArgumentException
    {
        // create the Orthologue
        Orthologue newOrthologue = new SimpleOrthologue(taxonID, locusID, homologeneID, accession);

        // stash it
        orthologueSet.add(newOrthologue);
        orthologueByHomologeneID.put(homologeneID, newOrthologue);
        return newOrthologue;
    }

    public Orthologue getOrthologue(String homologeneID)
    {
        return (Orthologue) orthologueByHomologeneID.get(homologeneID);
    }

    public Orthology createOrthology(Orthologue first, Orthologue second, SimilarityType type, double percentIdentity)
    {
        Orthology newOrthology = new SimpleOrthology(first, second, type, percentIdentity);

        // index it
        indexByTaxonID(first.getTaxonID(), newOrthology);
        indexByTaxonID(second.getTaxonID(), newOrthology);
        indexBySimilarityType(type, newOrthology);

        orthologySet.add(newOrthology);

        return newOrthology;
    }

    // should implement a uniqueness check here later!!!!

    public Orthology createOrthology(Orthologue first, Orthologue second, String ref)
    {
        Orthology newOrthology = new SimpleOrthology(first, second, ref);

        // index it
        indexByTaxonID(first.getTaxonID(), newOrthology);
        indexByTaxonID(second.getTaxonID(), newOrthology);
        indexBySimilarityType(SimilarityType.CURATED, newOrthology);

        orthologySet.add(newOrthology);

        return newOrthology;
    }

    public HomoloGroup createHomoloGroup()
    {
        HomoloGroup newGroup = new SimpleHomoloGroup();
        groups.add(newGroup);        

        return newGroup;
    }

    private void indexByTaxonID(int taxonID, Orthology orthology)
    {
        Integer taxonIDIndex = new Integer(taxonID);
        Set indexSet = (Set) orthologyByTaxonID.get(taxonIDIndex);

        if (indexSet == null) {
            indexSet = new HashSet();
            orthologyByTaxonID.put(taxonIDIndex, indexSet);
        }

        indexSet.add(orthology);
    }

    private void removeFromTaxonIDIndex(int taxonID, Orthology orthology)
    {
        Integer taxonIDIndex = new Integer(taxonID);
        Set indexSet = (Set) orthologyByTaxonID.get(taxonIDIndex);

        if (indexSet != null) {
            indexSet.remove(orthology);
        }
    }

    private void indexBySimilarityType(SimilarityType type, Orthology orthology)
    {
        Set indexSet = (Set) orthologyBySimilarityType.get(type);

        if (indexSet == null) {
            indexSet = new HashSet();
            orthologyByTaxonID.put(type, indexSet);
        }

        indexSet.add(orthology);
    }

    private void removeFromSimilarityTypeIndex(SimilarityType type, Orthology orthology)
    {
        Set indexSet = (Set) orthologyBySimilarityType.get(type);

        if (indexSet != null) {
            indexSet = new HashSet();
            orthologyBySimilarityType.remove(orthology);
        }
    }
}
