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


/**
 * an interface for Homologene dataset Builders
 *
 * @author David Huen
 */
public interface HomologeneBuilder
{
    public String TAXONID = "TaxonID";
    public String LOCUSID = "LocusID";
    public String HOMOID = "HomologeneID";
    public String ACCESSION = "Accession";

    public String SIMILARITYTYPE = "SimilarityType";
    public String PERCENTIDENTITY = "PercentIdentity";
    public String REFERENCE = "Reference";

    public String TWIN = "twin";
    public String MULTIPLE = "multiple";
    public String CURATED = "curated";


    public void startDB();

    public void startGroup();

    public void startOrthology();
    public void startOrthologue();
    public void addOrthologueProperty(String key, String value);
    public void endOrthologue();
    public void addOrthologyProperty(String key, String value);
    public void endOrthology();

    public void addTitle(int taxonID, String homologeneID, String title);

    public void endGroup();

    public void endDB();

}

