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

import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.biojava.bio.BioError;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.dist.DistributionTools;
import org.biojava.bio.dist.Distribution;
import org.biojava.bio.dist.SimpleDistribution;
import org.biojava.bio.dist.Count;
import org.biojava.bio.dist.IndexedCount;

/**
 * An utility class for codon preferences
 *
 * @author David Huen
 * @since 1.3
 */
public class CodonPrefTools
{
    static String JUNIT = "jUnit use only!!!!";
    public static String DROSOPHILA_MELANOGASTER_NUCLEAR = "Drosophila melanogaster-nuclear";

    private static Map prefMap;

    static {
        prefMap = new HashMap();

        loadCodonPreferences();
    }

    /**
     * get the specified codon preference.
     */
    public static CodonPref getCodonPreference(String id)
    {
        return (CodonPref) prefMap.get(id);
    }

    private static void loadCodonPreferences()
    {
        try {
            // parse the predefined codon preferences
            InputStream prefStream = CodonPrefTools.class.getClassLoader().getResourceAsStream(
                "org/biojava/bio/symbol/CodonPrefTables.xml"
            );

            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = parser.parse(prefStream);

            // get tables for each species
            NodeList children = doc.getDocumentElement().getChildNodes();

            for (int i=0; i<children.getLength(); i++) {
                Node cnode = children.item(i);

                if (!(cnode instanceof Element)) continue;

                Element child = (Element) cnode;

                String name = child.getNodeName();

                // the node must be a CodonPref record
                if (!name.equals("CodonPref")) continue;

                // pick up the id and genetic code
                String codonPrefId = child.getAttribute("id");
                String geneticCodeId = child.getAttribute("geneticCodeId");

                // now handle each codon frequency entry
                NodeList freqs = child.getChildNodes();

                // create a Count object for the job
                Count freqCounts = new IndexedCount(RNATools.getCodonAlphabet());

                for (int j=0; j < freqs.getLength(); j++) {
                    // load each entry
                    Node freq = freqs.item(j);

                    if (!(freq instanceof Element)) continue;

                    Element freqElement = (Element) freq;

                    // get attributes
                    String codonString = freqElement.getAttribute("codon");
                    String freqString = freqElement.getAttribute("value");

                    // create codon
                    SymbolList codonSL = RNATools.createRNA(codonString);

                    if (codonSL.length() !=3) throw new BioError("'" + codonString + "' is not a valid codon!");

                    AtomicSymbol codon = (AtomicSymbol) RNATools.getCodonAlphabet().getSymbol(codonSL.toList());

                    // recover frequency value too
                    double freqValue = Double.parseDouble(freqString);
                    freqCounts.increaseCount(codon, freqValue);

                }

                // turn the Counts into a Distribution
                Distribution freqDistribution = DistributionTools.countToDistribution(freqCounts);

                // create a CodonPref object
                CodonPref newCodonPref = new SimpleCodonPref(geneticCodeId, freqDistribution);

                prefMap.put(codonPrefId, newCodonPref);
            }
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * returns an RNA dinucleotide alphabet.
     * used to represent the non-wobble bases in WobbleDistribution
     */
    public static FiniteAlphabet getDinucleotideAlphabet()
    {
        return (FiniteAlphabet)AlphabetManager.generateCrossProductAlphaFromName("(RNA x RNA)");
    }
}

