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
import org.apache.xerces.parsers.*;
import org.xml.sax.*;

import org.biojava.bio.*;
import org.biojava.utils.*;

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
   * @deprecated all AlphabetManager methods have become static
   */
  static public AlphabetManager instance() {
    if(am == null)
      am = new AlphabetManager();
    return am;
  }

  static private Map nameToAlphabet;
  static private Map nameToSymbol;
  static private Map crossProductAlphabets;
  static private Map ambiguitySymbols;
  static private GapSymbol gapSymbol;

  /**
   * Retrieve the alphabet for a specific name.
   *
   * @param name the name of the alphabet
   * @return the alphabet object
   * @throws NoSuchElementException if there is no alphabet by that name
   */
  static public Alphabet alphabetForName(String name)
  throws NoSuchElementException{
    Alphabet alpha = (Alphabet) nameToAlphabet.get(name);
    if(alpha == null) {
      if(name.startsWith("(") && name.endsWith(")")) {
        alpha = generateCrossProductAlphaFromName(name);
      } else {
        throw new NoSuchElementException(
          "No alphabet for name " + name + " could be found"
        );
      }
    }
    return alpha;
  }
    /**
    *Retrieve the symbol represented a String object
    *@param name of the string whose symbol you want to get
    * @throws NoSuchElementException if the string name is invalid.
    */
  static public Symbol symbolForName(String name) 
  throws NoSuchElementException {
    Symbol s = (Symbol) nameToSymbol.get(name);
    if(s == null) {
      throw new NoSuchElementException("Could not find symbol under the name " + name);
    }
    return s;
  }

  /**
   * Register an alphabet by name.
   *
   * @param name  the name by which it can be retrieved
   * @param alphabet the Alphabet to store
   */
  static public void registerAlphabet(String name, Alphabet alphabet) {
    nameToAlphabet.put(name, alphabet);
  }
  
  /**
   * Get an iterator over all alphabets known.
   *
   * @return an Iterator over Alphabet objects
   */
  static public Iterator alphabets() {
    return nameToAlphabet.values().iterator();
  }

  /**
   * Get the special `gap' Symbol.
   * <P>
   * @return the system-wide symbol that represents a gap
   */
  static public CrossProductSymbol getGapSymbol() {
    return gapSymbol;
  }


  /**
   * Generates a new CrossProductSymbol instance.
   * <P>
   * This factory method hides some complexity about exactly what to make. If
   * symList contains only gaps, then the gap symbol is returned. If it contains
   * only AtomicSymbol instances, then a CrossProductSymbol implementing
   * AtomicSymbol will be returned. Otherwise, a plain-old CrossProductSymbol
   * will be returned.
   * <P>
   * This method makes no attempt to force singleton-ness upon the returned
   * symbol. This method will normaly be invoked by methods within alphabet
   * implementations, not by client code.
   *
   * @param symList a list of Symbol objects
   * @return a CrossProductSymbol that encapsulates that list
   */
  static public CrossProductSymbol getCrossProductSymbol(
    char token, List symList
  ) {
    return getCrossProductSymbol(token, symList, null);
  }
  
  /**
   * Generates a new CrossProductSymbol instance.
   * <P>
   * This factory method hides some complexity about exactly what to make. If
   * symList contains only gaps, then the gap symbol is returned. If it contains
   * only AtomicSymbol instances, then a CrossProductSymbol implementing
   * AtomicSymbol will be returned. Otherwise, a plain-old CrossProductSymbol
   * will be returned.
   * <P>
   * This method makes no attempt to force singleton-ness upon the returned
   * symbol. This method will normaly be invoked by methods within alphabet
   * implementations, not by client code.
   *
   * @param symList a list of Symbol objects
   * @param parent  a parental CrossProductAlphabet instance
   * @return a CrossProductSymbol that encapsulates that list
   */
  static public CrossProductSymbol getCrossProductSymbol(
    char token, List symList, CrossProductAlphabet parent
  ) {
    Iterator i = symList.iterator();
    int gapC = 0;
    int atomC = 0;
    while(i.hasNext()) {
      Symbol s = (Symbol) i.next();
      if(s == gapSymbol) {
        gapC++;
      } else if(s instanceof AtomicSymbol) {
        atomC++;
      }
    }
    
    if(gapC == symList.size()) {
      return gapSymbol;
    } else if(atomC == symList.size()) {
      return new AtomicCrossProductSymbol(token, symList);
    } else {
      return new SimpleCrossProductSymbol(token, symList, parent);
    }
  }
  
  /**
   * Generates a new CrossProductAlphabet from the give name.
   *
   * @param name  the name to parse
   * @return the associated Alphabet
   */
  static public CrossProductAlphabet generateCrossProductAlphaFromName(
    String name
  ) {
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
   * AlphabetManager.alphabetForName(cpa.getName())
   *
   * @param aList a list of Alphabet objects
   * @return a CrossProductAlphabet that is over the alphabets in aList
   */
  static public CrossProductAlphabet getCrossProductAlphabet(List aList) {
    return getCrossProductAlphabet(aList, null);
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
   * AlphabetManager.alphabetForName(cpa.getName())
   *
   * @param aList a list of Alphabet objects
   * @param parent a parent alphabet
   * @return a CrossProductAlphabet that is over the alphabets in aList
   */
  static public CrossProductAlphabet getCrossProductAlphabet(
    List aList, CrossProductAlphabet parent
  ) {
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
          if(size >= 0 && size < 1000) {
            cpa = new SimpleCrossProductAlphabet(aList, parent);
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
    *Obtain the default ambiguity symbol to represent the symbols passed in as parameters e.g. using the IUPAC ambiguity code, the symbol W will be returned for the collection [AT]. 
    *@param syms the collection of symbols
    *@throws IllegalSymbolException if the symbols are not recognized.
    */
  static public Symbol getAmbiguitySymbol(Collection syms)
  throws IllegalSymbolException {
    return getAmbiguitySymbol('\0', "", null, syms);
  }
    /**
    *Create a specific ambiguity symbol for a collection of symbols.
    *@param token the ambiguity character's designated token
    *@param name a name for this ambiguity symbol.
    *@param ann Annotation to associate with the ambiguity symbol
    *@param syms the collection of symbols which will be represented by this ambiguity symbol
    *@throws IllegalSymbolException if the symbols are not recognized.
    */
    static public Symbol getAmbiguitySymbol(
    char token,
    String name,
    Annotation ann,
    Collection syms
  ) throws IllegalSymbolException {
    Set symSet = new HashSet();
    for(Iterator i = syms.iterator(); i.hasNext(); ) {
      Symbol s = (Symbol) i.next();
      if(s instanceof AtomicSymbol) {
        symSet.add(s);
      } else {
        Alphabet sa = s.getMatches();
        if(sa instanceof FiniteAlphabet) {
          Iterator j = ((FiniteAlphabet) sa).iterator();
          while(j.hasNext()) {
            symSet.add(j.next());
          }
        } else {
          throw new IllegalSymbolException(
            "Unable to process symbol " + s.getName() +
            " as it matches an infinite number of AtomicSymbol objects."
          );
        }
      }
    }
    Symbol as = (Symbol) ambiguitySymbols.get(symSet);
    if(as == null) {
      as = new SimpleSymbol(
        token,
        name,
        new SimpleAlphabet(symSet),
        ann
      );
      ambiguitySymbols.put(symSet, as);
    }
    return as;
  }
  
  /**
   * Initialize the static AlphabetManager resources.
   * <P>
   * This parses the resource
   * <code>org/biojava/bio/seq/tools/AlphabetManager.xml</code>
   * and builds a basic set of alphabets.
   */
  static {
    nameToAlphabet = new HashMap();
    nameToSymbol = new HashMap();
    ambiguitySymbols = new HashMap();

    gapSymbol = new GapSymbol();
    ambiguitySymbols.put(new HashSet(), gapSymbol);
    try {
      InputStream alphabetStream = AlphabetManager.class.getClassLoader().getResourceAsStream(
        "org/biojava/bio/symbol/AlphabetManager.xml"
      );
      if (alphabetStream == null)
	  throw new BioError("Couldn't locate AlphabetManager.xml.  Badly built biojava archive?");

      InputSource is = new InputSource(alphabetStream);
      DOMParser parser = new DOMParser();
      parser.parse(is);
      Document doc = parser.getDocument();

      NodeList children = doc.getDocumentElement().getChildNodes();
      for(int i = 0; i < children.getLength(); i++) {
	Node cnode = children.item(i);
	if (! (cnode instanceof Element))
	    continue;

        Element child = (Element) cnode;
        String name = child.getNodeName();
        if(name.equals("symbol")) {
          nameToSymbol.put(child.getAttribute("name"),
                            symbolFromXML(child, null));
        } else if(name.equals("alphabet")) {
          String alphaName = child.getAttribute("name");
          String parentName = child.getAttribute("parent");
          try {
            SimpleAlphabet alpha = alphabetFromXML(child, nameToSymbol);
            if(parentName != null && parentName.length() != 0) {
              alphaName = parentName + "-" + alphaName;
              FiniteAlphabet pa = (FiniteAlphabet) alphabetForName(parentName);
              for(Iterator j = pa.iterator(); j.hasNext(); ) {
                alpha.addSymbol((Symbol) j.next());
              }
            }
            alpha.setName(alphaName);
            registerAlphabet(alphaName, alpha);
          } catch (Exception e) {
            throw new BioError(e, "Couldn't construct alphabet " + alphaName);
          }
        }
      }
    } catch (SAXParseException spe) {
      throw new BioError(spe,
                         spe.toString() +
                         spe.getLineNumber() + ":" +
                         spe.getColumnNumber()
      );
    } catch (Exception t) {
      throw new BioError(t, "Unable to initialize AlphabetManager");
    }
  }

  /**
   * Build an individual symbol.
   *
   * @param resE an XML Element specifying the element
   * @return the new AtomicSymbol object
   */
  static private AtomicSymbol symbolFromXML(
    Element resE, WellKnownAlphabet alpha
  ) {
    char token = '\0';
    String name = null;
    String description = null;

    NodeList children = resE.getChildNodes();
    for(int i = 0; i < children.getLength(); i++) {
      Node n = children.item(i);
      if (! (n instanceof Element))
	  continue;

      Element el = (Element) n;
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

    AtomicSymbol res = new WellKnownSymbol(alpha, token, name, (Annotation) null);
    res.getAnnotation().setProperty("description", description);
    return res;
  }

  /**
   * Build an individual symbol.
   *
   * @param resE an XML Element specifying the element
   * @return the new AmbiguitySymbol object
   */
  static private Symbol ambiguityFromXML(Element resE, Map nameToSym)
  throws IllegalSymbolException {
    char token = '\0';
    String name = null;
    String description = null;
    List syms = new ArrayList();
    
    NodeList children = resE.getChildNodes();
    for(int i = 0; i < children.getLength(); i++) {
      Node n = children.item(i);
      if (! (n instanceof Element))
	  continue;

      Element el = (Element) n;
      String nodeName = el.getNodeName();
      if(nodeName.equals("symbol")) {
        NodeList symC = el.getChildNodes();
        for(int j = 0; j < symC.getLength(); j++) {
	  Node en = symC.item(j);
	  if (! (en instanceof Element))
	      continue;

          Element eel = (Element) en;
          String eName = eel.getNodeName();
          String content = eel.getFirstChild().getNodeValue();
          if(eName.equals("short")) {
            token = content.charAt(0);
          } else if(eName.equals("long")) {
            name = content;
          } else if(eName.equals("description")) {
            description = content;
          }
        }
      } else if(nodeName.equals("symbolref")) {
        String refName = el.getAttribute("name");
        Symbol s = (Symbol) nameToSym.get(refName);
        if(s == null) {
          throw new IllegalSymbolException(
            "Got symbol ref to " + refName + " but it doesn't match anythin"
          );
        }
        syms.add(s);
      }
    }

    Symbol res = getAmbiguitySymbol(token, name, (Annotation) null, syms);
    return res;
  }

  /**
   * Generate an alphabet from an XML element and a map of symbol names to
   * symbol objects.
   *
   * @param alph  the alphabet XML Element
   * @param nameToRes Map from symbol name to Symbol object
   * @return a new Alphabet
   * @throws BioException if anything goes wrong
   */
  static private SimpleAlphabet alphabetFromXML(Element alph, Map nameToSym)
  throws BioException {
    nameToSym = new HashMap(nameToSym);
    WellKnownAlphabet alphabet = new WellKnownAlphabet();

    NodeList children = alph.getChildNodes();
    for(int i = 0; i < children.getLength(); i++) {
      Node n = children.item(i);
      if (! (n instanceof Element))
	  continue;

      Element el = (Element) n;
      try {
        String name = el.getNodeName();
        if(name.equals("description")) {
          alphabet.getAnnotation().setProperty("description", el.getFirstChild().getNodeValue());
        } else if(name.equals("symbol")) {
          Symbol sym = symbolFromXML(el, alphabet);
          String symName = el.getAttribute("name");
          if(symName != null) {
            nameToSym.put(symName, sym);
          }
          alphabet.addSymbol(sym);
        } else if(name.equals("symbolref")) {
          alphabet.addSymbol((Symbol) nameToSym.get(el.getAttribute("name")));
        } else if(name.equals("ambiguity")) {
            alphabet.addAmbiguity(ambiguityFromXML(el, nameToSym));
        }
      } catch (Exception e) {
        throw new BioException(e, "Couldn't parse element " + el);
      }
    }

    return alphabet;
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
            Alphabet a = AlphabetManager.alphabetForName(name);
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

    private static class WellKnownSymbol extends SimpleAtomicSymbol
                                          implements Serializable
    {
      WellKnownAlphabet alpha;
      public WellKnownSymbol(WellKnownAlphabet alpha, char token, String name, Annotation a) {
        super(token, name, a);
        this.alpha = alpha;
      }

      private Object writeReplace() {
        return new OPH(alpha, getName());
      }

      private static class OPH implements Serializable {
        private WellKnownAlphabet alpha;
        private String name;

        public OPH(WellKnownAlphabet alpha, String name) {
          this.alpha = alpha;
          this.name = name;
        }
	
	      private Object readResolve() throws ObjectStreamException {
          try {
            if(alpha != null) {
              return alpha.getParser("name").parseToken(name);
            } else {
              return symbolForName(name);
            }
          } catch (NoSuchElementException ex) {
            throw new InvalidObjectException(
              "Couldn't resolve symbol " + name + " as there was no parser"
            );
          } catch (IllegalSymbolException ise) {
            throw new InvalidObjectException(
              "Couldn't resolve symbol " + name + ": " + ise.getMessage()
            );
          }
        }
      }
    }
    
  /** 
   * The class representing the Gap symbol.
   * <P>
   * The gap is quite special. It is an ambiguity symbol with an empty alphabet.
   * This means that it notionaly represents an unfilled slot in a sequence. It
   * is identical to gap^n, so it is a CrossProductSymbol with an infinite list
   * of child symbols, each being itself. It should be a singleton, hence the
   * placement in AlphabetManager and also the method normalize.
   * <P>
   * Basicaly, this is a bit of a mess. We need to put our head together and
   * figure out what is going on.
   *
   * @author Matthew Pocock
   */
  private static class GapSymbol
  extends SimpleSymbol
  implements CrossProductSymbol {
    public GapSymbol() {
      super('-', "gap", Alphabet.EMPTY_ALPHABET, Annotation.EMPTY_ANNOTATION);
    }
    
    /**
     * Returns an infinitely long list of itself.
     *
     * @return a List of length zero, but where get(n) always returns the gap
     *         symbol
     */
    public List getSymbols() {
      return new AbstractList() {
        public int size() {
          return 0;
        }
        
        public Object get(int index) {
          return GapSymbol.this;
        }
      };
    }
  }
}
