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
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.dist.DistributionTools;
import org.biojava.bio.dist.Distribution;
import org.biojava.bio.dist.SimpleDistribution;
import org.biojava.bio.dist.Count;
import org.biojava.bio.dist.IndexedCount;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.utils.xml.XMLWriter;
import org.biojava.utils.xml.PrettyXMLWriter;

/**
 * An utility class for codon preferences
 *
 * @author David Huen
 * @since 1.3
 */
public class CodonPrefTools
{
    public static String JUNIT = "jUnit use only!!!!";
    public static String DROSOPHILA_MELANOGASTER_NUCLEAR = "Drosophila melanogaster-nuclear";

    private static Map prefMap;

    final private static Symbol [] cutg = new Symbol[64];

    static {
        prefMap = new HashMap();

        loadCodonPreferences();

        try {
            loadCodonOrder();
        }
        catch (IllegalSymbolException ise) {}
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
                CodonPref newCodonPref = new SimpleCodonPref(geneticCodeId, freqDistribution, codonPrefId);

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

    /**
     * writes out a CodonPref object in XML form
     */
    public static void dumpToXML(CodonPref codonPref, PrintWriter writer)
        throws NullPointerException, IOException, IllegalSymbolException, BioException
    {
        // validate both objects first
        if ((codonPref == null) || (writer == null))
            throw new NullPointerException();

        XMLWriter xw = new PrettyXMLWriter(writer);

        // get the CodonPref Distribution
        Distribution codonDist = codonPref.getFrequency();

        // start <CodonPrefs>
        xw.openTag("CodonPrefs");
        xw.openTag("CodonPref");
        xw.attribute("id", codonPref.getName());
        xw.attribute("geneticCodeId", codonPref.getGeneticCodeName());

        // loop over all codons, writing out the stats
        for (Iterator codonI = RNATools.getCodonAlphabet().iterator(); codonI.hasNext(); ) {
            BasisSymbol codon = (BasisSymbol) codonI.next();

            xw.openTag("frequency");

            // convert codon to a three letter string
            xw.attribute("codon", stringifyCodon(codon));
            xw.attribute("value", Double.toString(codonDist.getWeight(codon)));

            xw.closeTag("frequency");
        }

        xw.closeTag("CodonPref");
        xw.closeTag("CodonPrefs");
    }

    /**
     * converts a String representation of a codon to its Symbol
     */
    private static AtomicSymbol getCodon(String codonString)
        throws IllegalSymbolException
    {
        return (AtomicSymbol) RNATools.getCodonAlphabet().getSymbol(RNATools.createRNA(codonString).toList());
    }

    private static void loadCodonOrder()
        throws IllegalSymbolException
    {
        cutg[0] = getCodon("cga");
        cutg[1] = getCodon("cgc");
        cutg[2] = getCodon("cgg");
        cutg[3] = getCodon("cgu");

        cutg[4] = getCodon("aga");
        cutg[5] = getCodon("agg");

        cutg[6] = getCodon("cua");
        cutg[7] = getCodon("cuc");
        cutg[8] = getCodon("cug");
        cutg[9] = getCodon("cuu");

        cutg[10] = getCodon("uua");
        cutg[11] = getCodon("uug");

        cutg[12] = getCodon("uca");
        cutg[13] = getCodon("ucc");
        cutg[14] = getCodon("ucg");
        cutg[15] = getCodon("ucu");

        cutg[16] = getCodon("agc");
        cutg[17] = getCodon("agu");

        cutg[18] = getCodon("aca");
        cutg[19] = getCodon("acc");
        cutg[20] = getCodon("acg");
        cutg[21] = getCodon("acu");

        cutg[22] = getCodon("cca");
        cutg[23] = getCodon("ccc");
        cutg[24] = getCodon("ccg");
        cutg[25] = getCodon("ccu");

        cutg[26] = getCodon("gca");
        cutg[27] = getCodon("gcc");
        cutg[28] = getCodon("gcg");
        cutg[29] = getCodon("gcu");

        cutg[30] = getCodon("gga");
        cutg[31] = getCodon("ggc");
        cutg[32] = getCodon("ggg");
        cutg[33] = getCodon("ggu");

        cutg[34] = getCodon("gua");
        cutg[35] = getCodon("guc");
        cutg[36] = getCodon("gug");
        cutg[37] = getCodon("guu");

        cutg[38] = getCodon("aaa");
        cutg[39] = getCodon("aag");

        cutg[40] = getCodon("aac");
        cutg[41] = getCodon("aau");

        cutg[42] = getCodon("caa");
        cutg[43] = getCodon("cag");

        cutg[44] = getCodon("cac");
        cutg[45] = getCodon("cau");

        cutg[46] = getCodon("gaa");
        cutg[47] = getCodon("gag");

        cutg[48] = getCodon("gac");
        cutg[49] = getCodon("gau");

        cutg[50] = getCodon("uac");
        cutg[51] = getCodon("uau");

        cutg[52] = getCodon("ugc");
        cutg[53] = getCodon("ugu");

        cutg[54] = getCodon("uuc");
        cutg[55] = getCodon("uuu");

        cutg[56] = getCodon("aua");
        cutg[57] = getCodon("auc");
        cutg[58] = getCodon("auu");

        cutg[59] = getCodon("aug");

        cutg[60] = getCodon("ugg");

        cutg[61] = getCodon("uaa");
        cutg[62] = getCodon("uag");
        cutg[63] = getCodon("uga");
    }

    private static String stringifyCodon(BasisSymbol codon)
        throws IllegalSymbolException, BioException
    {
        // get the component symbols
        List codonList = codon.getSymbols();

        // get a tokenizer
        SymbolTokenization toke = RNATools.getRNA().getTokenization("token");

        String tokenizedCodon = toke.tokenizeSymbol((Symbol) codonList.get(0))
            + toke.tokenizeSymbol((Symbol) codonList.get(1))
            + toke.tokenizeSymbol((Symbol) codonList.get(2));

        return tokenizedCodon;
    }
}

