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
 * A simple no-frills implementation of the HomologeneBuilder interface.
 * Used to instantiate a in-memory copy of the Homologene data.
 *
 * @author David Huen
 */
public class SimpleHomologeneBuilder implements HomologeneBuilder
{
    HomologeneDB db = null;

    OrthologyTemplate orthologyTmpl = null;

    OrthologueTemplate orthologueTmpl = null;

    HomoloGroup group = null;

    private int level = 0;


    private class OrthologyTemplate
    {
        Orthologue     firstOrtho;
        Orthologue     secondOrtho;
        SimilarityType type;
        double         percentIdentity;
        String         ref;
    }

    private class OrthologueTemplate
    {
        int taxonID = - 9999;
        String locusID = null;
        String homologeneID = null;
        String accession = null;
    }

    public void startDB()
    {
        if (level != 0) return;

        db = new SimpleHomologeneDB();
        level++;
    }

    public void startGroup()
    {
        if (level != 1) return;

        group = db.createHomoloGroup();
        level++;
    }

    public void startOrthology()
    {
        if (level != 2) return;

        orthologyTmpl = new OrthologyTemplate();
        level++;
    }

    public void startOrthologue()
    {
        if (level != 3) return;

        orthologueTmpl = new OrthologueTemplate();
        level++;
    }

    public void addOrthologueProperty(String key, String value)
    {
        if (level != 4) return;

        // the property can be taxon id, locus id, homologene id
        if (orthologueTmpl != null) {
            if (key.equals(TAXONID)) {
                orthologueTmpl.taxonID = Integer.parseInt(value);
            }
            else if (key.equals(LOCUSID)) {
                orthologueTmpl.locusID = value;
            }
            else if (key.equals(HOMOID)) {
                orthologueTmpl.homologeneID = value;
            }
            else if (key.equals(ACCESSION)) {
                orthologueTmpl.accession = value;
            }
        }
    }

    public void endOrthologue()
    {
        if (level != 4) return;

        // validate the template
        if ((orthologueTmpl.taxonID != -9999) 
           || (orthologueTmpl.homologeneID != null)
           || (orthologueTmpl.accession != null))
            return;

        // get the taxon
        Taxon taxon;
        if ((taxon = HomologeneTools.getTaxon(orthologueTmpl.taxonID)) == null) return;

        // create the Orthologue
        Orthologue orthologue;
        if ((orthologue = db.getOrthologue(orthologueTmpl.homologeneID)) == null) {
            orthologue = new SimpleOrthologue(taxon, orthologueTmpl.locusID, orthologueTmpl.homologeneID, orthologueTmpl.accession);
        }

        // fill in the orthology template
        if (orthologyTmpl.firstOrtho == null) 
            orthologyTmpl.firstOrtho = orthologue;
        else
            orthologyTmpl.secondOrtho = orthologue;
 
        orthologueTmpl = null;
        level--;
    }

    public void addOrthologyProperty(String key, String value)
    {
        if (level != 3) return;

           if (key.equals(SIMILARITYTYPE)) {
               String type = value.trim();

               if (type.equals(TWIN)) {
                   orthologyTmpl.type = SimilarityType.TWIN;
               }
               else if (type.equals(MULTIPLE)) {
                   orthologyTmpl.type = SimilarityType.MULTIPLE;
               }
               else if (type.equals(CURATED)) {
                   orthologyTmpl.type = SimilarityType.CURATED;
               }
           }
           else if (key.equals(PERCENTIDENTITY)) {
               orthologyTmpl.percentIdentity = Integer.parseInt(value);
           }
           else if (key.equals(REFERENCE)) {
               orthologyTmpl.ref = value;
           }
    }

    public void endOrthology()
    {
        if (level !=3) return;

        // validate template
        if (orthologyTmpl.type == null) return;
        if ((orthologyTmpl.firstOrtho == null) || (orthologyTmpl.secondOrtho == null)) return;

        if (orthologyTmpl.type == SimilarityType.CURATED) {
            Orthology orthology = db.createOrthology(
                orthologyTmpl.firstOrtho,
                orthologyTmpl.secondOrtho,
                orthologyTmpl.ref);

            group.addOrthology(orthology);
        }
        else {
            if (orthologyTmpl.ref == null) return;
            Orthology orthology = db.createOrthology(
                orthologyTmpl.firstOrtho,
                orthologyTmpl.secondOrtho,
                orthologyTmpl.type,
                orthologyTmpl.percentIdentity);

            group.addOrthology(orthology);
            
        }

        level--;
    }

    public void addTitle(int taxonID, String homologeneID, String title)
    {

    }

    public void endGroup()
    {
        if (level != 2) return;

        level--;
    }

    public void endDB()
    {
        if (level != 1) return;

        level--;
    }

}

