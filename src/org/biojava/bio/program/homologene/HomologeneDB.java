


package org.biojava.bio.program.homologene;


/**
 * Homologene is a NCBI dataset that curates sets
 * of orthologues from the reference model organisms.
 * <p>
 * This class is a Collection of methods for handling
 * data from the Homologene dataset.
 *
 * @author David Huen
 */
public interface HomologeneDB
{
    /**
     * an iterator for HomoloGroups
     */
    public interface HomoloGroupIterator
    {
        public boolean hasNext();
        public HomoloGroup nextGroup();
    }

    /**
     * create a orthologue
     */
    public Orthologue createOrthologue(Taxon taxon, String locusID, String homologeneID, String accession);

    /**
     * returns an orthologue of specified ID
     */
    public Orthologue getOrthologue(String homologeneID);

    /**
     * create a computed orthology entry
     */
    public Orthology createOrthology(Orthologue first, Orthologue second, SimilarityType type, double percentIdentity);

    /**
     * create a curated orthology entry
     */
    public Orthology createOrthology(Orthologue first, Orthologue second, String ref);

    /**
     * create a Homologene Group
     */
    public HomoloGroup createHomoloGroup();    

    /**
     * get the HomologeneGroups in this database
     */
    public HomoloGroupSet getHomoloGroups();

    /**
     * filter the database for a specified group
     */
    public HomoloGroupSet filter(HomoloGroupFilter filters);
}

