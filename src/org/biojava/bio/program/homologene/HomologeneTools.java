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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.naming.OperationNotSupportedException;

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
     * instantiate a HomologeneDB.
     * <p>
     * Currently, only file protocol support is available.
     */
    public static void instantiateDB(URL url, HomologeneBuilder builder)
        throws OperationNotSupportedException, FileNotFoundException, IOException
    {
        boolean inDB = false;
        boolean inGroup = false;


        if (!url.getProtocol().equals("file"))
            throw new OperationNotSupportedException();

        // open the file
        BufferedReader rdr = new BufferedReader(
            new FileReader(url.getPath())
            );

        // the file assumes implicitly that a group has started
        builder.startDB();
        builder.startGroup();

        inDB = inGroup = true;

        // read loop
        Pattern titlePattern = Pattern.compile("TITLE\\s(\\d+)_(\\d+)=(\\S+)\\s(.*)");
        Pattern orthoPattern = Pattern.compile("^\\s*(\\d+)\\s*\\|\\s*(\\d+)\\s*\\|([Bbc]{1})\\|(.*)\\|\\s*(\\d+)\\s*\\|(.*)\\|(.*)\\|\\s*(\\d+)\\s*\\|(.*)\\|(.*)");
        String currLine;
        while ((currLine = rdr.readLine()) != null) {

            // parse current line
            if (currLine.startsWith(">")) {
                // start new group
                if (inGroup) builder.endGroup();
                builder.startGroup();
            }
            else if (currLine.startsWith("TITLE")) {
                try {
                    // parse the line
                    Matcher m = titlePattern.matcher(currLine);

                    if (m.matches()) {
                        if (m.groupCount() != 4) continue;

                        // pick up the groups
                        int taxonID = Integer.parseInt(m.group(1));
                        String homologeneID = m.group(2);
                        String sym = m.group(3);
                        String title = m.group(4);

                        builder.addTitle(taxonID, homologeneID.trim(), title.trim());
                    }
                }
                catch (NumberFormatException nfe) {
                    continue;
                }
            }
            else {
                // this is a orthology line

                try {
                    // parse the line
                    Matcher m = orthoPattern.matcher(currLine);

                    if (m.matches()) {
//                        System.out.println("=======================orthology line: " + m.groupCount() + "=====================");
                        if (m.groupCount() != 10) continue;
                        // pick up the groups
                        String taxonID0 = m.group(1).trim();//System.out.println(taxonID0);
                        String taxonID1 = m.group(2).trim();//System.out.println(taxonID1);
                        String type = m.group(3).trim();//System.out.println(type);
                        String locus0 = m.group(4).trim();
                        String homoID0 = m.group(5).trim();
                        String access0 = m.group(6).trim();
                        String locus1 = m.group(7).trim(); 
                        String homoID1 = m.group(8).trim();
                        String access1 = m.group(9).trim();
                        String finale = m.group(10).trim();//System.out.println(finale);

                        // validate numeric formats
                        Integer.parseInt(taxonID0);
                        Integer.parseInt(taxonID1);

                        // validate the similarity type before proceeding
                        if (   (type.equals("B")) 
                            || (type.equals("b"))
                            || (type.equals("c")) ) {

                            if (type.equals("B")) {

                                // validate numeric format
                                Double.parseDouble(finale);

                                builder.startOrthology();
                                builder.addOrthologyProperty(HomologeneBuilder.PERCENTIDENTITY, finale);
                                builder.addOrthologyProperty(HomologeneBuilder.SIMILARITYTYPE, HomologeneBuilder.MULTIPLE);
                            }
                            else if (type.equals("b")) {

                                // validate numeric format
                                Integer.parseInt(finale);

                                builder.startOrthology();
                                builder.addOrthologyProperty(HomologeneBuilder.PERCENTIDENTITY, finale);
                                builder.addOrthologyProperty(HomologeneBuilder.SIMILARITYTYPE, HomologeneBuilder.TWIN);
                            }
                            else if (type.equals("c")) {

                                builder.startOrthology();
                                builder.addOrthologyProperty(HomologeneBuilder.SIMILARITYTYPE, HomologeneBuilder.CURATED);
                                builder.addOrthologyProperty(HomologeneBuilder.PERCENTIDENTITY, finale);
                            }

                            // add the orthologues
                            builder.startOrthologue();
                            builder.addOrthologueProperty(HomologeneBuilder.TAXONID, taxonID0);
                            builder.addOrthologueProperty(HomologeneBuilder.LOCUSID, locus0);
                            builder.addOrthologueProperty(HomologeneBuilder.HOMOID, homoID0);
                            builder.addOrthologueProperty(HomologeneBuilder.ACCESSION, access0);
                            builder.endOrthologue();

                            builder.startOrthologue();
                            builder.addOrthologueProperty(HomologeneBuilder.TAXONID, taxonID1);
                            builder.addOrthologueProperty(HomologeneBuilder.LOCUSID, locus1);
                            builder.addOrthologueProperty(HomologeneBuilder.HOMOID, homoID1);
                            builder.addOrthologueProperty(HomologeneBuilder.ACCESSION, access1);
                            builder.endOrthologue();

                            builder.endOrthology();
                        }
                    }
                }
                catch (NumberFormatException nfe) {
                    builder.endOrthology();
                    continue;
                }
            }
        }

        // EOF
        if (inGroup) builder.endGroup();
        if (inDB) builder.endDB();
    }
}

