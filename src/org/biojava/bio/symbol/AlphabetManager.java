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

import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.net.*;

import org.w3c.dom.*;
import org.apache.xerces.parsers.*;
import org.xml.sax.*;

import org.biojava.bio.*;
import org.biojava.utils.*;
import org.biojava.utils.bytecode.*;

/**
 * The first port of call for retrieving standard alphabets.
 * <P>
 * The alphabet interfaces themselves don't give you a lot of help in actually
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
  static private Map gapBySize;
  static private Map alphabetToIndex = new HashMap();

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
    *
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
   * The gap symbol is a Symbol that has an empty alphabet of matches. As such
   *, ever alphabet contains gap, as there is no symbol that matches gap, so
   * there is no case where an alphabet doesn't contain a symbol that matches
   * gap.
   * <P>
   * Gap can be thought of as an empty sub-space within the space of all
   * possible symbols. If you are working in a cross-product alphabet, you
   * should chose wether to use gap to represent 'no symbol', or a basis symbol
   * of the apropreate size built entirely of gaps to represent 'no symbol in
   * each of the slots'. Perhaps this could be explained better.
   *
   * @return the system-wide symbol that represents a gap
   */
  static public Symbol getGapSymbol() {
    return gapSymbol;
  }

  static public Symbol getGapSymbol(List alphas) {
    SizeQueen sq = new SizeQueen(alphas);
    Symbol s = (Symbol) gapBySize.get(sq);

    if(s == null) {
      if(alphas.size() == 0) { // should never be needed
        s = gapSymbol;
      } else if(alphas.size() == 1) { // should never happen
        Alphabet a = (Alphabet) alphas.get(0);
        s = getGapSymbol(a.getAlphabets());
      } else {
        List symList = new ArrayList(alphas.size());
        for(Iterator i = alphas.iterator(); i.hasNext(); ) {
          Alphabet a = (Alphabet) i.next();
          symList.add(getGapSymbol(a.getAlphabets()));
        }
        try {
          s = new SimpleBasisSymbol(
            '-',
            Annotation.EMPTY_ANNOTATION,
            symList,
            Alphabet.EMPTY_ALPHABET
          );
        } catch (IllegalSymbolException ise) {
          throw new BioError(
            ise,
            "Assertion Failure: Should be able to make gap basis"
          );
        }
      }
      gapBySize.put(sq, s);
    }

    return s;
  }

  /**
   * Generate a new AtomicSymbol instance with a token, name and Annotation.
   * <P>
   * Use this method if you wish to create an AtomicSymbol instance. Initialy it
   * will not be a member of any alphabet.
   *
   * @param token  the Char token returned by getToken()
   * @param name  the String returned by getName()
   * @param annotatin the Annotation returned by getAnnotation()
   * @return a new AtomicSymbol instance
   */
  static public AtomicSymbol createSymbol(
    char token, String name, Annotation annotation
  ) {
    AtomicSymbol as = new FundamentalAtomicSymbol(name, token, annotation);
    return as;
  }

  /**
   * Generates a new Symbol instance that represents the tuple of Symbols in
   * symList.
   * <P>
   * This method is most usefull for writing Alphabet implementations. It should
   * not be invoked by casual users. Use alphabet.getSymbol(List) instead.
   *
   * @param token   the Symbol's token
   * @param symList a list of Symbol objects
   * @param alpha   the Alphabet that this Symbol will reside in
   * @return a Symbol that encapsulates that List
   */
  static public Symbol createSymbol(
    char token, Annotation annotation,
    List symList, Alphabet alpha
  ) throws IllegalSymbolException {
    Iterator i = symList.iterator();
    int basis = 0;
    int atomC = 0;
    while(i.hasNext()) {
      Symbol s = (Symbol) i.next();
      if(s instanceof BasisSymbol) {
        basis++;
        if(s instanceof AtomicSymbol) {
          atomC++;
        }
      }
    }

    if(atomC == symList.size()) {
      return new SimpleAtomicSymbol(token, annotation, symList);
    } else if(basis == symList.size()) {
      return new SimpleBasisSymbol(
        token, annotation,
        symList,
        new SimpleAlphabet(
          expandMatches(alpha, symList, new ArrayList())
        )
      );
    } else {
      return new SimpleSymbol(
        token, annotation,
        new SimpleAlphabet(
          expandBasis(alpha, symList, new ArrayList())
        )
      );
    }
  }

  /**
   * Expands a list of BasisSymbols into the set of AtomicSymbol instances
   * it matches.
   */
  private static Set expandBasis(Alphabet alpha, List symList, List built) {
    int indx = built.size();
    if(indx < symList.size()) {
      Symbol s = (Symbol) symList.get(indx);
      if(s instanceof AtomicSymbol) {
        built.add(s);
        return expandBasis(alpha, symList, built);
      } else {
        Set res = new HashSet();
        Iterator i = ((FiniteAlphabet) s.getMatches()).iterator();
        while(i.hasNext()) {
          AtomicSymbol as = (AtomicSymbol) i.next();
          List built2 = new ArrayList(built);
          built2.add(as);
          res.addAll(expandBasis(alpha, symList, built2));
        }
        return res;
      }
    } else {
      try {
        return Collections.singleton(alpha.getSymbol(built));
      } catch (IllegalSymbolException ise) {
        throw new BioError(
          ise,
          "Assertion Failure: Should just have legal AtomicSymbol instances."
        );
      }
    }
  }

  /**
   * Generates a new Symbol instance that represents the tuple of Symbols in
   * symList.
   * <P>
   * This method is most usefull for writing Alphabet implementations. It should
   * not be invoked by users. Use alphabet.getSymbol(Set) instead.
   *
   * @param token   the Symbol's token
   * @param name    the Symbol's name
   * @param symSet  a Set of Symbol objects
   * @param alpha   the Alphabet that this Symbol will reside in
   * @return a Symbol that encapsulates that List
   */
  static public Symbol createSymbol(
    char token, Annotation annotation,
    Set symSet, Alphabet alpha
  ) throws IllegalSymbolException {
    if(symSet.size() == 0) {
      return getGapSymbol();
    }
    Set asSet = new HashSet();
    int len = -1;
    for(
      Iterator i = symSet.iterator();
      i.hasNext();
    ) {
      Symbol s = (Symbol) i.next();
      if(s instanceof AtomicSymbol) {
        AtomicSymbol as = (AtomicSymbol) s;
        int l = as.getSymbols().size();
        if(len == -1) {
          len = l;
        } else if(len != l) {
          throw new IllegalSymbolException(
            "Can't build ambiguity symbol as the symbols have inconsistent " +
            "length"
          );
        }
        asSet.add(as);
      } else {
        for(Iterator j = ((FiniteAlphabet) s.getMatches()).iterator();
          j.hasNext();
        ) {
          AtomicSymbol as = ( AtomicSymbol) j.next();
          int l = as.getSymbols().size();
          if(len == -1) {
            len = l;
          } else if(len != l) {
            throw new IllegalSymbolException(
              "Can't build ambiguity symbol as the symbols have inconsistent " +
              "length"
            );
          }
          asSet.add(as);
        }
      }
    }
    if(asSet.size() == 0) {
      return getGapSymbol();
    } else if(asSet.size() == 1) {
      return (Symbol) asSet.iterator().next();
    } else {
      if(len == 1) {
        return new SimpleBasisSymbol(
          token, annotation, new SimpleAlphabet(asSet)
        );
      } else {
        List fs = factorize(alpha, asSet);
        if(fs == null) {
          return new SimpleSymbol(
            token, annotation,
            new SimpleAlphabet(asSet)
          );
        } else {
          return new SimpleBasisSymbol(
            token, annotation,
            fs, new SimpleAlphabet(
              expandBasis(alpha, fs, new ArrayList())
            )
          );
        }
      }
    }
  }

  /**
   * Generates a new CrossProductAlphabet from the give name.
   *
   * @param name  the name to parse
   * @return the associated Alphabet
   */
  static public Alphabet generateCrossProductAlphaFromName(
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
  static public Alphabet getCrossProductAlphabet(List aList) {
    return getCrossProductAlphabet(aList, null);
  }

  /**
   * Retrieve a CrossProductAlphabet instance over the alphabets in aList.
   * <P>
   * This method is most usefull for implementors of cross-product alphabets,
   * allowing them to safely build the matches alphabets for ambiguity symbols.
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
  static public Alphabet getCrossProductAlphabet(
    List aList, Alphabet parent
  ) {
    // This trap means that the `product' operator can be
    // safely applied to a single alphabet.

    if (aList.size() == 1)
        return (Alphabet) aList.get(0);

    if(crossProductAlphabets == null) {
      crossProductAlphabets = new HashMap();
    }

    Alphabet cpa = (Alphabet) crossProductAlphabets.get(aList);

    int size = 1;
    if(cpa == null) {
      for(Iterator i = aList.iterator(); i.hasNext(); ) {
        Alphabet aa = (Alphabet) i.next();
        if(! (aa instanceof FiniteAlphabet) ) {
          cpa =  new InfiniteCrossProductAlphabet(aList);
          break;
        }
        if(size <= 1000) {
          size *= ((FiniteAlphabet) aa).size();
        }
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
      crossProductAlphabets.put(new ArrayList(aList), cpa);
      registerAlphabet(cpa.getName(), cpa);
    }

    return cpa;
  }

  private static Set expandMatches(Alphabet parent, List symList, List built) {
    int indx = built.size();
    if(indx < symList.size()) {
      BasisSymbol bs = (BasisSymbol) symList.get(indx);
      if(bs instanceof AtomicSymbol) {
        built.add(bs);
        return expandMatches(parent, symList, built);
      } else {
        Set syms = new HashSet();
        Iterator i = ((FiniteAlphabet) bs.getMatches()).iterator();
        while(i.hasNext()) {
          List built2 = new ArrayList(built);
          built2.add((AtomicSymbol) i.next());
          syms.addAll(expandMatches(parent, symList, built2));
        }
        return syms;
      }
    } else {
      try {
        Symbol s = parent.getSymbol(built);
        if(s instanceof AtomicSymbol) {
          return Collections.singleton((AtomicSymbol) s);
        } else {
          Set syms = new HashSet();
          for(Iterator i = ((FiniteAlphabet) s.getMatches()).iterator(); i.hasNext(); ) {
            syms.add((AtomicSymbol) i.next());
          }
          return syms;
        }
      } catch (IllegalSymbolException ise) {
        throw new BioError(ise, "Assertion Failure: Couldn't create symbol.");
      }
    }
  }

  /**
   * Return a list of BasisSymbol instances that uniquely sum up all
   * AtomicSymbol
   * instances in symSet. If the symbol can't be represented by a single list of
   * BasisSymbol instances, return null.
   * <P>
   * This method is most usefull for implementers of Alphabet and Symbol. It
   * probably should not be invoked by users.
   *
   * @param symSet  the Set of AtomicSymbol instances
   * @param alpha   the Alphabet instance that the Symbols are from
   * @return a List of BasisSymbols
   */
  public static List factorize(Alphabet alpha, Set symSet)
  throws IllegalSymbolException {
    List alphas = alpha.getAlphabets();
    List facts = new ArrayList();
    int size = symSet.size();
    Set syms = new HashSet();
    for(int col = 0; col < alphas.size(); col++) {
      Alphabet a = (Alphabet) alphas.get(col);
      for(Iterator i = symSet.iterator(); i.hasNext(); ) {
        syms.add(
          (AtomicSymbol) ((AtomicSymbol)
          i.next()).getSymbols().get(col)
        );
      }
      int s = syms.size();
      if( (size % s) != 0 ) {
        return null;
      }
      size /= s;
      facts.add(a.getAmbiguity(syms));
      syms.clear();
    }
    if(size != 1) {
      return null;
    }
    return facts;
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
    gapBySize = new HashMap();
    gapBySize.put(new SizeQueen(new ArrayList()), gapSymbol);
    try {
      gapBySize.put(
        new SizeQueen(Arrays.asList(
                new Alphabet[] { DoubleAlphabet.getInstance() }
        )),
        new SimpleBasisSymbol(
                '-',
                Annotation.EMPTY_ANNOTATION,
                Arrays.asList(new Symbol[] { gapSymbol }),
                Alphabet.EMPTY_ALPHABET
        )
      );
    } catch (IllegalSymbolException ise) {
      throw new BioError(
        ise,
        "Assertion Failure: Should be able to make gap basis"
      );
    }

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
              if (pa instanceof SimpleAlphabet) {
              for(Iterator j = ((SimpleAlphabet)pa).ambiguities(); j.hasNext(); ) {
                alpha.addAmbiguity((Symbol) j.next());
                }
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
   * @param symE an XML Element specifying the element
   * @return the new AtomicSymbol object
   */
  static private AtomicSymbol symbolFromXML(
    Element symE, WellKnownAlphabet alpha
  ) {
    char token = '\0';
    String name = null;
    String description = null;

    NodeList children = symE.getChildNodes();
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

    AtomicSymbol sym = new WellKnownSymbol(
      alpha, token, name, new SimpleAnnotation()
    );
    try {
      sym.getAnnotation().setProperty("description", description);
    } catch (ChangeVetoException cve) {
      throw new BioError(
        cve,
        "Assertion voilated: there should be nothing to veto this property"
      );
    }
    return sym;
  }

  /**
   * Build an individual symbol.
   *
   * @param symE an XML Element specifying the element
   * @return the new AmbiguitySymbol object
   */
  static private Symbol ambiguityFromXML(
    Alphabet alpha, Element symE, Map nameToSym
  ) throws IllegalSymbolException {
    char token = '\0';
    String name = null;
    String description = null;
    Set syms = new HashSet();

    NodeList children = symE.getChildNodes();
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
            "Got symbol ref to " + refName + " but it doesn't match anything"
          );
        }
        syms.add(s);
      }
    }

    Symbol sym = createSymbol(
      token, Annotation.EMPTY_ANNOTATION,
      syms, alpha
    );
    return sym;
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
          alphabet.addAmbiguity(ambiguityFromXML(alphabet, el, nameToSym));
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

    private static class WellKnownSymbol extends FundamentalAtomicSymbol
                                          implements Serializable
    {
      WellKnownAlphabet alpha;
      public WellKnownSymbol(WellKnownAlphabet alpha, char token, String name, Annotation a) {
        super(name, token, a);
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
   * This means that it notionaly represents an unfilled slot in a sequence.
   * It should be a singleton, hence the
   * placement in AlphabetManager and also the method normalize.
   *
   * @author Matthew Pocock
   */
  private static class GapSymbol implements Symbol, Serializable {
      public GapSymbol() {
      }

      public String getName() {
          return "gap";
      }

      public char getToken() {
          return '-';
      }

      public Annotation getAnnotation() {
          return Annotation.EMPTY_ANNOTATION;
      }

      public Alphabet getMatches() {
          return Alphabet.EMPTY_ALPHABET;
      }

      public void addChangeListener(ChangeListener cl) {
      }

      public void addChangeListener(ChangeListener cl, ChangeType ct) {
      }

      public void removeChangeListener(ChangeListener cl) {
      }

      public void removeChangeListener(ChangeListener cl, ChangeType ct) {
      }
  }

  /**
   * Get an indexer for a specified alphabet.
   *
   * @param alpha The alphabet to index
   * @return an AlphabetIndex instance
   * @since 1.1
   */
  public static AlphabetIndex getAlphabetIndex(
    FiniteAlphabet alpha
  ) {
    final int generateIndexSize = 160;
    AlphabetIndex ai = (AlphabetIndex) alphabetToIndex.get(alpha);
    if(ai == null) {
      int size = alpha.size();
      if(size <= generateIndexSize) {
        ai = new LinearAlphabetIndex(alpha);
      } else {
        ai = new HashedAlphabetIndex(alpha);
      }
      alphabetToIndex.put(alpha, ai);
    }
    return ai;
  }

  /**
   * Get an indexer for an array of symbols.
   *
   * @param syms the Symbols to index in that order
   * @return an AlphabetIndex instance
   * @since 1.1
   */
  public static AlphabetIndex getAlphabetIndex (
    Symbol[] syms
  ) throws IllegalSymbolException, BioException {
    return new LinearAlphabetIndex(syms);
  }

  private static final class SizeQueen extends AbstractList {
    private final List alphas;

    public SizeQueen(List alphas) {
      this.alphas = alphas;
    }

    public int size() {
      return alphas.size();
    }

    public Object get(int pos) {
      Alphabet a = (Alphabet) alphas.get(pos);
      List al = a.getAlphabets();
      int size = al.size();
      if(size > 1) {
        return new SizeQueen(al);
      } else {
        return new Integer(size);
      }
    }
  }
}
