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
import org.biojava.bio.symbol.*;

import org.w3c.dom.*;
import org.apache.xerces.parsers.*;
import org.xml.sax.*;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
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
          DOMParser parser = new DOMParser();
          parser.parse(is);
          doc = parser.getDocument();

        }catch (MissingResourceException mre) {
            System.err.println(mre.getMessage());
        }catch(Exception e){//err
            e.printStackTrace();
        }
               
        try {
            SimpleSymbolPropertyTable simplePropertyTable = new SimpleSymbolPropertyTable(
            getAlphabet(),
            SymbolPropertyTable.MONO_MASS
            );
            Iterator it = getAlphabet().iterator();
            NodeList children = doc.getDocumentElement().getChildNodes();
            
            while(it.hasNext()){
                Symbol s = (Symbol)it.next();
                //  simplePropertyTable.setDoubleProperty(s, "1202.00");
                for(int i = 0; i < children.getLength(); i++) {                   
                    Node cnode = (Node) children.item(i); 
                    if(! (cnode instanceof Element)) {
                        continue;
                    }
                    Element child = (Element) cnode;
                    if(child.getNodeName().equals("residue")) {
                        
                        if(child.getAttribute("token").equals(s.getToken()+"")){
                            
                            NodeList properyNodes = child.getChildNodes();
                            for(int j = 0; j < properyNodes.getLength(); j++)
                            {
                                cnode = (Node) properyNodes.item(j);
                                if(! (cnode instanceof Element)) {
                                    continue;
                                }
                                Element el = (Element) cnode;
                                String name = el.getAttribute("name");
                                if(name.equals(SymbolPropertyTable.MONO_MASS)) {
                                    String value = el.getAttribute("value");
                                    simplePropertyTable.setDoubleProperty(s, value);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            
            propertyTableMap.put(SymbolPropertyTable.MONO_MASS, (SymbolPropertyTable) simplePropertyTable);

            //Build AVG_MASS table
            simplePropertyTable = new SimpleSymbolPropertyTable(
            getAlphabet(),
            SymbolPropertyTable.AVG_MASS
            );
            it = getAlphabet().iterator();

            while(it.hasNext()){
                Symbol s = (Symbol)it.next();
                //  simplePropertyTable.setDoubleProperty(s, "1202.00");
                for(int i = 0; i < children.getLength(); i++) {
                     Node cnode = (Node) children.item(i); 
                    if(! (cnode instanceof Element)) {
                        continue;
                    }
                    Element child = (Element) cnode;
                               
                    if(child.getNodeName().equals("residue")) {
                        if(child.getAttribute("token").equals(s.getToken()+"")){                           
                            NodeList properyNodes = child.getChildNodes();
                            for(int j = 0; j < properyNodes.getLength(); j++)
                            {
                                cnode = (Node) properyNodes.item(j);
                                if(! (cnode instanceof Element)) {
                                    continue;
                                }
                                Element el = (Element) cnode;
                                String name = el.getAttribute("name");
                                 
                                if(name.equals(SymbolPropertyTable.AVG_MASS)) {
                                    String value = el.getAttribute("value");
                                    simplePropertyTable.setDoubleProperty(s, value);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            propertyTableMap.put(SymbolPropertyTable.AVG_MASS, (SymbolPropertyTable) simplePropertyTable);
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
}
