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


package org.biojava.bio.seq.tools;

import java.io.*;
import java.util.*;
import java.net.*;

import org.w3c.dom.*;
import com.sun.xml.parser.*;
import com.sun.xml.tree.*;
import org.xml.sax.*;

import org.biojava.bio.seq.*;

/**
 * The first port of call for retrieving standard alphabets.
 *
 * @author Matthew Pocock
 */
public class AlphabetManager {
  /**
   * Singleton instance.
   */
  static private AlphabetManager am;

  /**
   * Retrieve the singleton instance.
   *
   * @return the AlphabetManager instance
   */
  static public AlphabetManager instance() {
    if(am == null)
      am = new AlphabetManager();
    return am;
  }

  /**
   * Maintains the map from name to alphabet.
   */
  private Map nameToAlphabet;

  /**
   * Initialize nameToAlphabet.
   */
  {
    nameToAlphabet = new HashMap();
  }

  /**
   * Retrieve the alphabet for a specific name.
   *
   * @param name the name of the alphabet
   * @return the alphabet object
   * @throws NoSuchElementException if there is no alphabet by that name
   */
  public Alphabet alphabetForName(String name)
  throws NoSuchElementException{
    return (Alphabet) nameToAlphabet.get(name);
  }

  /**
   * Register an alphabet by name.
   *
   * @param name  the name by which it can be retrieved
   * @param alphabet the Alphabet to store
   */
  public void registerAlphabet(String name, Alphabet alphabet) {
    nameToAlphabet.put(name, alphabet);
  }
  
  /**
   * Get an iterator over all alphabets known.
   *
   * @return an Iterator over Alphabet objects
   */
  public Iterator alphabets() {
    return nameToAlphabet.values().iterator();
  }

  /**
   * Constructs a new Alphabetmanager instance.
   * <P>
   * This parses the resource
   * <code>org/biojava/bio/seq/tools/AlphabetManager.xml</code>
   * and builds a basic set of alphabets.
   */
  protected AlphabetManager() {
    try {
      URL alphabetURL =
        getClass().getClassLoader().getResource("org/biojava/bio/seq/tools/AlphabetManager.xml");
      InputSource is = Resolver.createInputSource(alphabetURL, true);
      Document doc = XmlDocument.createXmlDocument(is, true);

      Map nameToResidue = new HashMap();

      NodeList children = doc.getDocumentElement().getChildNodes();
      for(int i = 0; i < children.getLength(); i++) {
        Element child = (Element) children.item(i);
        String name = child.getNodeName();
        if(name.equals("residue")) {
          nameToResidue.put(child.getAttribute("name"),
                            residueFromXML(child));
        } else if(name.equals("alphabet")) {
          Alphabet alpha = alphabetFromXML(child, nameToResidue);
          registerAlphabet(alpha.getName(), alpha);
        }
      }
    } catch (SAXParseException spe) {
      System.out.println(spe.toString() +
                         spe.getLineNumber() + ":" +
                         spe.getColumnNumber());
      System.exit(1);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Build an individual residue.
   *
   * @param resE an XML Element specifying the element
   * @return the new Residue object
   */
  protected Residue residueFromXML(Element resE) {
    char symbol = '\0';
    String name = null;
    String description = null;

    NodeList children = resE.getChildNodes();
    for(int i = 0; i < children.getLength(); i++) {
      Element el = (Element) children.item(i);
      String nodeName = el.getNodeName();
      String content = el.getFirstChild().getNodeValue();
      if(nodeName.equals("short")) {
        symbol = content.charAt(0);
      } else if(nodeName.equals("long")) {
        name = content;
      } else if(nodeName.equals("description")) {
        description = content;
      }
    }

    Residue res = new SimpleResidue(symbol, name, (Annotation) null);
    res.getAnnotation().setProperty("description", description);
    return res;
  }

  /**
   * Generate an alphabet from an XML element and a map of residue names to
   * residue objects.
   *
   * @param alph  the alphabet XML Element
   * @param nameToRes Map from residue name to Residue object
   * @return a new Alphabet
   */
  protected Alphabet alphabetFromXML(Element alph, Map nameToRes) {
    SimpleAlphabet alphabet = new SimpleAlphabet();

    NodeList children = alph.getChildNodes();
    for(int i = 0; i < children.getLength(); i++) {
      Element el = (Element) children.item(i);
      try {
        String name = el.getNodeName();
        if(name.equals("name")) {
          alphabet.setName(el.getFirstChild().getNodeValue());
        } else if(name.equals("description")) {
          alphabet.getAnnotation().setProperty("description", el.getFirstChild().getNodeValue());
        } else if(name.equals("residue")) {
          alphabet.addResidue(residueFromXML(el));
        } else if(name.equals("residueref")) {
          alphabet.addResidue((Residue) nameToRes.get(el.getAttribute("name")));
        } else if(name.equals("tokenizer")) {
          alphabet.putParser(el.getAttribute("name"),
                                parserFromXML(el, alphabet, nameToRes));
        }
      } catch (Exception e) {
        System.err.println(e.toString() + el.toString());
      }
    }

    return alphabet;
  }

  /**
   * Build a custom parser for an alphabet.
   * 
   * @param tok the XML token Element
   * @param alpha the Alphabet for the parser
   * @param nameToRes  a map of residue name to Residue Object
   * @return a newly built custom parser
   */
  protected ResidueParser parserFromXML(Element tok, Alphabet alpha, Map nameToRes) 
         throws IllegalResidueException {
    FixedWidthParser tokenizer =
      new FixedWidthParser(alpha, Integer.parseInt(tok.getAttribute("tokenLength")));

    NodeList children = tok.getElementsByTagName("map");
    for(int i = 0; i < children.getLength(); i++) {
      Element map = (Element) children.item(i);
      String resName = map.getAttribute("residue");
      Residue res = (Residue) nameToRes.get(resName);
      if(res == null)
        throw new IllegalResidueException("Unknown residue: " + resName);
      if(!alpha.contains(res))
        throw new IllegalResidueException(resName + " not found in " + alpha.getName());
      tokenizer.addTokenMap(map.getAttribute("token"), res);
    }

    return tokenizer;
  }
}
