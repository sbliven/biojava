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
 * @author Thomas Down
 */
public final class AlphabetManager {
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
  private Map nameToResidue;
  private Map gappedAlphabets;
  private Residue gapResidue;

  /**
   * Initialize nameToAlphabet.
   */
  {
    nameToAlphabet = new HashMap();
    nameToResidue = new HashMap();
    gappedAlphabets = new HashMap();
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

    private Residue residueForName(String name) 
        throws NoSuchElementException
    {
	return (Residue) nameToResidue.get(name);
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
     * Get the special `gap' Residue.
     */

    public Residue getGapResidue() {
	return gapResidue;
    }

    /**
     * Return a new alphabet which includes the gap residue
     * plus all other residues from the speicified alphabet.
     */

    public Alphabet getGappedAlphabet(Alphabet a) 
    {
	if (a.contains(gapResidue))
	    return a;
	Alphabet b = (Alphabet) gappedAlphabets.get(a);
	if (b == null) {
	    b = new GappedAlphabet(a);
	    gappedAlphabets.put(a, b);
	}
	return b;
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

      gapResidue = (Residue) nameToResidue.get("gap");
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

    Residue res = new WellKnownResidue(symbol, name, (Annotation) null);
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
    SimpleAlphabet alphabet = new WellKnownAlphabet();

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

    /**
     * Alphabet which adds the gap residue to an existing alphabet.
     * AlphabetManager internal use only.
     */

    private class GappedAlphabet implements Alphabet, Serializable {
	private Alphabet child;

	GappedAlphabet(Alphabet child) {
	    this.child = child;
	}

	public boolean contains(Residue r) {
	    return (child.contains(r) || r == gapResidue);
	}

	public void validate(Residue r) throws IllegalResidueException {
	    if (! ((child.contains(r) || r == gapResidue)))
		throw new IllegalResidueException("Residue " + r.getName() + " is not found in Alphabet " + getName());
	}

	public String getName() {
	    return child.getName() + "-GAP";
	}

	public ResidueParser getParser(String name) {
	    throw new NoSuchElementException("Autogenerated gapped alphabets aren't clever enough to have parsers yet...");
	}

	public ResidueList residues() {
	    List l = child.residues().toList();
	    List nl = new ArrayList(l);
	    nl.add(gapResidue);
	    return new SimpleResidueList(this, nl);
	}

	public int size() {
	    return child.size() + 1;
	}

	public Annotation getAnnotation() {
	    return child.getAnnotation();
	}

	private Object writeReplace() {
	    return new GappedAlphabetOPH(child);
	}
    }

    /**
     * Placeholder for a GappedAlphabet in a serialized object
     * stream.
     */


    private static class GappedAlphabetOPH implements Serializable {
	private Alphabet child;

	public GappedAlphabetOPH(Alphabet child) {
	    this.child = child;
	}

	public GappedAlphabetOPH() {
	}

	private Object readResolve() throws ObjectStreamException {
	    try {
		Alphabet a = AlphabetManager.instance().getGappedAlphabet(child);
		return a;
	    } catch (Exception ex) {
		throw new InvalidObjectException("Couldn't reconstruct gapped Alphabet from " + child.getName());
	    }
	} 
    }

    /**
     * A well-known alphabet.  In principle this is just like
     * SimpleAlphabet, but it is replaces by a placeholder in
     * serialized data.
     */

    private static class WellKnownAlphabet extends SimpleAlphabet 
                                           implements Serializable 
    {
	private Object writeReplace() {
	    return new OPH(getName());
	}

	/**
	 * Placeholder for a WellKnownAlphabet in a serialized
	 * object stream.
	 */

	private static class OPH implements Serializable {
	    private String name;

	    public OPH(String name) {
		this.name = name;
	    }
	
	    public OPH() {
	    }

	    private Object readResolve() throws ObjectStreamException {
		try {
		    Alphabet a = AlphabetManager.instance().
                                    alphabetForName(name);
		    return a;
		} catch (NoSuchElementException ex) {
		    throw new InvalidObjectException("Couldn't resolve alphabet " + name);
		}
	    }
	}
    }

    /**
     * A well-known residue.  Replaced by a placeholder in
     * serialized data.
     */

    private static class WellKnownResidue extends SimpleResidue
                                          implements Serializable
    {
	public WellKnownResidue(char symbol, String name, Annotation a) {
	    super(symbol, name, a);
	}

	private Object writeReplace() {
	    return new OPH(getName());
	}

	private static class OPH implements Serializable {
	    private String name;

	    public OPH(String name) {
		this.name = name;
	    }
	
	    public OPH() {
	    }

	    private Object readResolve() throws ObjectStreamException {
		try {
		    Residue a = AlphabetManager.instance().
                                    residueForName(name);
		    return a;
		} catch (NoSuchElementException ex) {
		    throw new InvalidObjectException("Couldn't resolve residue " + name);
		}
	    }
	}
    }
}
