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

import java.io.*;
import java.util.*;
import java.net.*;

import org.w3c.dom.*;
import com.sun.xml.parser.*;
import com.sun.xml.tree.*;
import org.xml.sax.*;

import org.biojava.bio.*;

/**
 * The first port of call for retrieving standard alphabets.
 * <P>
 * The alphabet interfaces themselves don't give you a lot of help in actualy
 * getting an alphabet instance. This is where the AlphabetManager comes in
 * handy. It helps out in serialization, generating derived alphabets and
 * building CrossProductAlphabet instances. It also contains limited support for
 * parsing complex alphabet names back into the alphabets.
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
  private Map nameToSymbol;
  private Map gappedAlphabets;
  private Map crossProductAlphabets;
  private Symbol gapSymbol;

  /**
   * Initialize nameToAlphabet.
   */
  {
    nameToAlphabet = new HashMap();
    nameToSymbol = new HashMap();
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
    Alphabet alpha = (Alphabet) nameToAlphabet.get(name);
    if(alpha == null) {
      if(name.startsWith("(") && name.endsWith(")")) {
        alpha = generateCrossProductAlphaFromName(name);
      } else if(name.endsWith("-GAP")) {
        alpha = generateGappedAlphaFromName(name);
      } else {
        throw new NoSuchElementException(
          "No alphabet for name " + name + " could be found"
        );
      }
    }
    return alpha;
  }

  public Symbol symbolForName(String name) 
  throws NoSuchElementException {
    Symbol r = (Symbol) nameToSymbol.get(name);
    if(r == null) {
      throw new NoSuchElementException("Could not find symbol under the name " + name);
    }
    return r;
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
   * Get the special `gap' Symbol.
   * <P>
   * @return the system-wide symbol that represents a gap
   */
  public Symbol getGapSymbol() {
    return gapSymbol;
  }

  /**
   * Returns a gapped alphabet for the alphabet name.
   * <P>
   * The name should be of the form Alphabet-GAP.
   *
   * @param name  the name to parse
   * @return the associated Alphabet
   */
  public Alphabet generateGappedAlphaFromName(String name) {
    if(!name.endsWith("-GAP")) {
      throw new Error(
        "Tried to give me a name that isn't a gap alphabet: " + name
      );
    }
    String prefix = name.substring(0, name.length() - 4);
    return getGappedAlphabet(alphabetForName(prefix));
  }
  
  /**
   * Return a new alphabet which includes the gap symbol
   * plus all other symbols from the speicified alphabet.
   *
   * @param a the Alphabet to gap
   * @return  an Alphabet that contains all of the symbols in
   *          a and the gap character
   */
  public Alphabet getGappedAlphabet(Alphabet a) {
    if(a == null) {
      throw new NullPointerException("Can't add a gap to 'null'");
    }
    
    if (a.contains(gapSymbol)) {
      return a;
    }
    Alphabet b = (Alphabet) gappedAlphabets.get(a);
    if (b == null) {
      if(a instanceof FiniteAlphabet) {
        b = new FiniteGappedAlphabet((FiniteAlphabet) a);
      } else {
        b = new GappedAlphabet(a);
      }
      gappedAlphabets.put(a, b);
      registerAlphabet(b.getName(), b);
    }
    return b;
  }

  /**
   * Generates a new CrossProductAlphabet from the give name.
   *
   * @param name  the name to parse
   * @return the associated Alphabet
   */
  public CrossProductAlphabet generateCrossProductAlphaFromName(String name) {
    if(!name.startsWith("(") || !name.endsWith(")")) {
      throw new BioError(
        "Can't parse " + name +
        " into a cross-product alphabet as it is not bracketed"
      );
    }
    
    name = name.substring(1, name.length()-1).trim();
    List aList = new ArrayList(); // the alphabets
    int i = 0;
    while(i < name.length()) {
      String alpha = null;
      if(name.charAt(i) == '(') {
        int depth = 1;
        int j = i+1;
        while(j < name.length() && depth > 0) {
          char c = name.charAt(j);
          if(c == '(') {
            depth++;
          } else if(c == ')') {
            depth--;
          }
          j++;
        }
        if(depth == 0) {
          aList.add(alphabetForName(name.substring(i, j)));
          i = j;
        } else {
          throw new BioError(
            "Error parsing alphabet name: could not find matching bracket\n" +
            name.substring(i)
          );
        }
      } else {
        int j = name.indexOf(" x ", i);
        if(j < 0) {
          aList.add(alphabetForName(name.substring(i).trim()));
          i = name.length();
        } else {
          aList.add(alphabetForName(name.substring(i, j).trim()));
          i = j + " x ".length();
        }
      }
    }
    
    return getCrossProductAlphabet(aList);
  }
  
  /**
   * Retrieve a CrossProductAlphabet instance over the alphabets in aList.
   * <P>
   * If all of the alphabets in aList implements FiniteAlphabet then the
   * method will return a FiniteAlphabet. Otherwise, it returns a non-finite
   * alphabet.
   * <P>
   * If you call this method twice with a list containing the same alphabets,
   * it will return the same alphabet. This promotes the re-use of alphabets
   * and helps to maintain the 'flyweight' principal for finite alphabet
   * symbols.
   * <P>
   * The resulting alphabet cpa will be retrievable via
   * AlphabetManager.instance().alphabetForName(cpa.getName())
   *
   * @param aList a list of Alphabet objects
   * @return a CrossProductAlphabet that is over the alphabets in aList
   */
  public CrossProductAlphabet getCrossProductAlphabet(List aList) {
    if(crossProductAlphabets == null) {
      crossProductAlphabets = new HashMap();
    }

    ListWrapper aw = new ListWrapper(aList);
        
    CrossProductAlphabet cpa =
      (CrossProductAlphabet) crossProductAlphabets.get(aw);
    
    int size = 1;
    if(cpa == null) {
      for(Iterator i = aList.iterator(); i.hasNext(); ) {
        Alphabet aa = (Alphabet) i.next();
        if(! (aa instanceof FiniteAlphabet) ) {
          cpa =  new InfiniteCrossProductAlphabet(aList);
          break;
        }
        size *= ((FiniteAlphabet) aa).size();
      }
      if(cpa == null) {
        try {
          if(size > 0 && size < 1000) {
            cpa = new SimpleCrossProductAlphabet(aList);
          } else {
            cpa = new SparseCrossProductAlphabet(aList);
          }
        } catch (IllegalAlphabetException iae) {
          throw new BioError(
            "Could not create SimpleCrossProductAlphabet for " + aList +
            " even though we should be able to. No idea what is wrong."
          );
        }
      }
      crossProductAlphabets.put(aw, cpa);
      registerAlphabet(cpa.getName(), cpa);
    }
    
    return cpa;
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
        getClass().getClassLoader().getResource("org/biojava/bio/symbol/AlphabetManager.xml");
      InputSource is = Resolver.createInputSource(alphabetURL, true);
      Document doc = XmlDocument.createXmlDocument(is, true);

      NodeList children = doc.getDocumentElement().getChildNodes();
      for(int i = 0; i < children.getLength(); i++) {
        Element child = (Element) children.item(i);
        String name = child.getNodeName();
        if(name.equals("symbol")) {
          nameToSymbol.put(child.getAttribute("name"),
                            symbolFromXML(child));
        } else if(name.equals("alphabet")) {
          Alphabet alpha = alphabetFromXML(child, nameToSymbol);
          registerAlphabet(alpha.getName(), alpha);
        }
      }

      gapSymbol = (Symbol) nameToSymbol.get("gap");
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
   * Build an individual symbol.
   *
   * @param resE an XML Element specifying the element
   * @return the new Symbol object
   */
  protected Symbol symbolFromXML(Element resE) {
    char token = '\0';
    String name = null;
    String description = null;

    NodeList children = resE.getChildNodes();
    for(int i = 0; i < children.getLength(); i++) {
      Element el = (Element) children.item(i);
      String nodeName = el.getNodeName();
      String content = el.getFirstChild().getNodeValue();
      if(nodeName.equals("short")) {
        token = content.charAt(0);
      } else if(nodeName.equals("long")) {
        name = content;
      } else if(nodeName.equals("description")) {
        description = content;
      }
    }

    Symbol res = new WellKnownSymbol(token, name, (Annotation) null);
    res.getAnnotation().setProperty("description", description);
    return res;
  }

  /**
   * Generate an alphabet from an XML element and a map of symbol names to
   * symbol objects.
   *
   * @param alph  the alphabet XML Element
   * @param nameToRes Map from symbol name to Symbol object
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
        } else if(name.equals("symbol")) {
          alphabet.addSymbol(symbolFromXML(el));
        } else if(name.equals("symbolref")) {
          alphabet.addSymbol((Symbol) nameToRes.get(el.getAttribute("name")));
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
   * @param nameToRes  a map of symbol name to Symbol Object
   * @return a newly built custom parser
   */
  protected SymbolParser parserFromXML(Element tok, Alphabet alpha, Map nameToRes) 
         throws IllegalSymbolException {
    FixedWidthParser tokenizer =
      new FixedWidthParser(alpha, Integer.parseInt(tok.getAttribute("tokenLength")));

    NodeList children = tok.getElementsByTagName("map");
    for(int i = 0; i < children.getLength(); i++) {
      Element map = (Element) children.item(i);
      String resName = map.getAttribute("symbol");
      Symbol res = (Symbol) nameToRes.get(resName);
      if(res == null)
        throw new IllegalSymbolException("Unknown symbol: " + resName);
      if(!alpha.contains(res))
        throw new IllegalSymbolException(resName + " not found in " + alpha.getName());
      tokenizer.addTokenMap(map.getAttribute("token"), res);
    }

    return tokenizer;
  }

  /**
   * Alphabet which adds the gap symbol to an existing alphabet.
   * AlphabetManager internal use only.
   */
  private class GappedAlphabet implements Alphabet, Serializable {
    protected Alphabet child;

    GappedAlphabet(Alphabet child) {
	      this.child = child;
    }

    public boolean contains(Symbol r) {
	    return (child.contains(r) || r == gapSymbol);
    }

    public void validate(Symbol r) throws IllegalSymbolException {
      if(r == gapSymbol) {
        return;
      }
      
      try {
        child.validate(r);
      } catch (IllegalSymbolException ire) {
        throw new IllegalSymbolException(
          ire,
          "Symbol not found in underlying alphabet and is not gap in " +
          getName()
        );
      }
    }

    public String getName() {
	    return child.getName() + "-GAP";
    }

    public SymbolParser getParser(String name) {
      throw new NoSuchElementException("No parsers associated with " + getName());
    }

    public Annotation getAnnotation() {
	    return child.getAnnotation();
    }

    private Object writeReplace() {
	    return new GappedAlphabetOPH(child);
    }
  }

  /**
   * A gapped alphabet over a finite underlying alphabet.
   */
  private class FiniteGappedAlphabet
  extends GappedAlphabet implements FiniteAlphabet {
    protected FiniteAlphabet getSourceAlphabet() {
      return (FiniteAlphabet) child;
    }
    
    FiniteGappedAlphabet(FiniteAlphabet child) {
      super(child);
    }

    public Iterator iterator() {
      return symbols().iterator();
    }
    
    public SymbolList symbols() {
	    List l = getSourceAlphabet().symbols().toList();
	    List nl = new ArrayList(l);
	    nl.add(gapSymbol);
	    return new SimpleSymbolList(this, nl);
    }

    public int size() {
	    return getSourceAlphabet().size() + 1;
    }

    public SymbolParser getParser(String name) {
      if(name.equals("token")) {
        return new TokenParser(this);
      } else if(name.equals("name")) {
        return new NameParser(this);
      } else {
        throw new NoSuchElementException(
          "Could not create parser for " + name +
          " in alphabet " + getName()
        );
      }
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

    private static class WellKnownAlphabet
    extends SimpleAlphabet implements Serializable {
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
            Alphabet a = AlphabetManager.instance().alphabetForName(name);
            return a;
          } catch (NoSuchElementException ex) {
            throw new InvalidObjectException("Couldn't resolve alphabet " + name);
          }
        }
      }
    }

    /**
     * A well-known symbol.  Replaced by a placeholder in
     * serialized data.
     */

    private static class WellKnownSymbol extends SimpleSymbol
                                          implements Serializable
    {
	public WellKnownSymbol(char token, String name, Annotation a) {
	    super(token, name, a);
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
		    Symbol a = AlphabetManager.instance().
                                    symbolForName(name);
		    return a;
		} catch (NoSuchElementException ex) {
		    throw new InvalidObjectException("Couldn't resolve symbol " + name);
		}
	    }
	}
    }
   
  /**
   * Simple wrapper to assist in list-comparisons.
   *
   * @author Thomas Down
   */

  public static class ListWrapper {
    List l;

    ListWrapper(List l) {
      this.l = l;
    }

    ListWrapper() {
    }

    public boolean equals(Object o) {
      if (! (o instanceof ListWrapper)) {
        return false;
      }
      List ol = ((ListWrapper) o).l;
      if (ol.size() != l.size()) {
        return false;
      }
      Iterator i1 = l.iterator();
      Iterator i2 = ol.iterator();
      while (i1.hasNext()) {
        if (i1.next() != i2.next()) {
          return false;
        }
      }
      return true;
    }

    public int hashCode() {
      int c = 0;
      for (Iterator i = l.iterator(); i.hasNext(); ) {
        c += i.next().hashCode();
      }
      return c;
    }
  }
}
