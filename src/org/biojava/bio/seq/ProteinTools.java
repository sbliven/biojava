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

 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.bio.seq;

import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import javax.xml.parsers.*;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.net.URL;

/**
 * The central port-of-call for all information and functionality specific to
 * SymbolLists over the protein alphabet.
 *
 * @author Matthew Pocock
 * @author Greg Cox
 * @author Thomas Down
 */
public class ProteinTools {
    private static final FiniteAlphabet proteinAlpha;
    private static final FiniteAlphabet proteinTAlpha;

    private static final Map propertyTableMap = new HashMap();

    static {
        try {
            proteinAlpha = (FiniteAlphabet) AlphabetManager.alphabetForName("PROTEIN");
            proteinTAlpha = (FiniteAlphabet) AlphabetManager.alphabetForName("PROTEIN-TERM");
        } catch (Exception e) {
            throw new BioError(e, " Could not initialize ProteinTools");
        }
    }


    static {

        Document doc = null;
     /*   try {
            URL proteaseManagerURL = ProteinTools.class.getClassLoader().getResource(
            "org/biojava/bio/symbol/ResidueProperties.xml"
            );
            //If I try and do this here on compile it says "An exception can't be thrown by an initializer"
            InputSource is = Resolver.createInputSource(proteaseManagerURL, true);
            doc = XmlDocument.createXmlDocument(is, true);*/

      try {
          InputStream tablesStream = ProteinTools.class.getClassLoader().getResourceAsStream(
            "org/biojava/bio/symbol/ResidueProperties.xml"
          );
          if(tablesStream == null ) {
            throw new BioError("Couldn't locate ResidueProperties.xml.");
          }

          InputSource is = new InputSource(tablesStream);
          DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	  doc = parser.parse(is);
        }catch (MissingResourceException mre) {
            System.err.println(mre.getMessage());
        }catch(Exception e){//err
            e.printStackTrace();
        }

        try {
            SimpleSymbolPropertyTable monoMassPropertyTable = new SimpleSymbolPropertyTable(
            getAlphabet(),
            SymbolPropertyTable.MONO_MASS
            );
	    
	    SimpleSymbolPropertyTable avgMassPropertyTable = new SimpleSymbolPropertyTable(
            getAlphabet(),
            SymbolPropertyTable.AVG_MASS
            );

            SimpleSymbolPropertyTable pKPropertyTable = new SimpleSymbolPropertyTable(
            getAlphabet(),
            SymbolPropertyTable.PK
            );
 
	    SymbolTokenization tokens = getAlphabet().getTokenization("token");

            NodeList children = doc.getDocumentElement().getChildNodes();
	    for(int i = 0; i < children.getLength(); i++) {
		Node cnode = (Node) children.item(i);
		if(! (cnode instanceof Element)) {
		    continue;
		}
		Element child = (Element) cnode;
		if(child.getNodeName().equals("residue")) {
		    String token = child.getAttribute("token");
		    Symbol s = tokens.parseToken(token);
			
		    NodeList properyNodes = child.getChildNodes();
		    for(int j = 0; j < properyNodes.getLength(); j++) {
			cnode = (Node) properyNodes.item(j);
			if(! (cnode instanceof Element)) {
			    continue;
			}
			Element el = (Element) cnode;
			String name = el.getAttribute("name");
			if(name.equals(SymbolPropertyTable.MONO_MASS)) {
			    String value = el.getAttribute("value");
			    monoMassPropertyTable.setDoubleProperty(s, value);
			} else if (name.equals(SymbolPropertyTable.AVG_MASS)) {
			    String value = el.getAttribute("value");
			    avgMassPropertyTable.setDoubleProperty(s, value);
			} else if (name.equals(SymbolPropertyTable.PK)) {
                            String value = el.getAttribute("value");
                            pKPropertyTable.setDoubleProperty(s, value);
                            break;
                        }
		    }
		}
	    }

            propertyTableMap.put(SymbolPropertyTable.MONO_MASS, (SymbolPropertyTable) monoMassPropertyTable);
            propertyTableMap.put(SymbolPropertyTable.AVG_MASS, (SymbolPropertyTable) avgMassPropertyTable);
            propertyTableMap.put(SymbolPropertyTable.PK, (SymbolPropertyTable) pKPropertyTable);
        } catch (Exception e) {
            throw new BioError(e, " Could not initialize ProteinTools");
        }
    }
    /**
     *Gets the protein alphabet
     */
    public static final FiniteAlphabet getAlphabet() {
        return proteinAlpha;
    }

    /**
     *Gets the protein alphabet including the translation termination symbols
     */
    public static final FiniteAlphabet getTAlphabet() {
        return proteinTAlpha;
    }

    public static final SymbolPropertyTable getSymbolPropertyTable(String name)
    {
        return (SymbolPropertyTable)propertyTableMap.get(name);
    }

	/**
	 * Return a new Protein <span class="type">SymbolList</span> for
	 * <span class="arg">protein</span>.
	 *
	 * @param theProtein a <span class="type">String</span> to parse into Protein
	 * @return a <span class="type">SymbolList</span> created form
	 *         <span class="arg">Protein</span>
	 * @throws IllegalSymbolException if  <span class="arg">dna</span> contains
	 *         any non-Amino Acid characters.
	 */
	public static SymbolList createProtein(String theProtein)
		throws IllegalSymbolException
	{
		try
		{
			org.biojava.bio.seq.io.SymbolTokenization p = getTAlphabet().getTokenization("token");
			return new SimpleSymbolList(p, theProtein);
		}
		catch (BioException se)
		{
			throw new BioError(se, "Something has gone badly wrong with Protein");
		}
	}
}
