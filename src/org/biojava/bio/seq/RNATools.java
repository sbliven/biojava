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

package org.biojava.bio.seq;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;

/**
 * Useful functionality for processing DNA and RNA sequences.
 *
 * @author Matthew Pocock
 * @author Keith James (docs)
 * @author Thomas Down
 * @author Greg Cox
 */
public final class RNATools {
  private static final ReversibleTranslationTable complementTable;
  private static final ReversibleTranslationTable transcriptionTable;
  static private final FiniteAlphabet rna;
  static private final Map geneticCodes;

  static private final AtomicSymbol a;
  static private final AtomicSymbol g;
  static private final AtomicSymbol c;
  static private final AtomicSymbol u;

  static private Map symbolToComplement;

  static {
    try {
      rna = (FiniteAlphabet) AlphabetManager.alphabetForName("RNA");

      SymbolList syms = new SimpleSymbolList(rna.getTokenization("token"), "agcu");
      a = (AtomicSymbol) syms.symbolAt(1);
      g = (AtomicSymbol) syms.symbolAt(2);
      c = (AtomicSymbol) syms.symbolAt(3);
      u = (AtomicSymbol) syms.symbolAt(4);

      symbolToComplement = new HashMap();

      // add the gap symbol
      Symbol gap = rna.getGapSymbol();
      symbolToComplement.put(gap, gap);

      // add all other ambiguity symbols
      for(Iterator i = AlphabetManager.getAllSymbols(rna).iterator(); i.hasNext();) {
	  Symbol as = (Symbol) i.next();
	  FiniteAlphabet matches = (FiniteAlphabet) as.getMatches();
	  if (matches.size() > 1) {   // We've hit an ambiguous symbol.
	      Set l = new HashSet();
	      for(Iterator j = matches.iterator(); j.hasNext(); ) {
		  l.add(complement((Symbol) j.next()));
	      }
	      symbolToComplement.put(as, rna.getAmbiguity(l));
	  }
      }
      complementTable = new RNAComplementTranslationTable();
      transcriptionTable = new TranscriptionTable();

      geneticCodes = new HashMap();
      loadGeneticCodes();
    } catch (Throwable t) {
      throw new BioError(t, "Unable to initialize RNATools");
    }
  }

  public static AtomicSymbol a() { return a; }
  public static AtomicSymbol g() { return g; }
  public static AtomicSymbol c() { return c; }
  public static AtomicSymbol u() { return u; }

  /**
   * Return the RNA alphabet.
   *
   * @return a flyweight version of the RNA alphabet
   */
  public static FiniteAlphabet getRNA() {
    return rna;
  }

  /**
   * Return a new RNA <span class="type">SymbolList</span> for
   * <span class="arg">rna</span>.
   *
   * @param rna a <span class="type">String</span> to parse into RNA
   * @return a <span class="type">SymbolList</span> created form
   *         <span class="arg">rna</span>
   * @throws IllegalSymbolException if  <span class="arg">rna</span> contains
   *         any non-RNA characters
   */
  public static SymbolList createRNA(String rna)
  throws IllegalSymbolException {
    try {
      SymbolTokenization p = getRNA().getTokenization("token");
      return new SimpleSymbolList(p, rna);
    } catch (BioException se) {
      throw new BioError(se, "Something has gone badly wrong with RNA");
    }
  }

  /**
   * Return an integer index for a symbol - compatible with forIndex.
   * <p>
   * The index for a symbol is stable accross virtual machines & invocations.
   *
   * @param sym  the Symbol to index
   * @return     the index for that symbol
   * @throws IllegalSymbolException if sym is not a member of the DNA alphabet
   */
  public static int index(Symbol sym) throws IllegalSymbolException {
    if(sym == a) {
      return 0;
    } else if(sym == g) {
      return 1;
    } else if(sym == c) {
      return 2;
    } else if(sym == u) {
      return 3;
    }
    getRNA().validate(sym);
    throw new IllegalSymbolException("Realy confused. Can't find index for " +
                                      sym.getName());
  }

  /**
   * Return the symbol for an index - compatible with index.
   * <p>
   * The index for a symbol is stable accross virtual machines & invocations.
   *
   * @param index  the index to look up
   * @return       the symbol at that index
   * @throws IndexOutOfBoundsException if index is not between 0 and 3
   */
  static public Symbol forIndex(int index)
  throws IndexOutOfBoundsException {
    if(index == 0)
      return a;
    else if(index == 1)
      return g;
    else if(index == 2)
      return c;
    else if(index == 3)
      return u;
    else throw new IndexOutOfBoundsException("No symbol for index " + index);
  }

  /**
   * Complement the symbol.
   *
   * @param sym  the symbol to complement
   * @return a Symbol that is the complement of sym
   * @throws IllegalSymbolException if sym is not a member of the DNA alphabet
   */
  static public Symbol complement(Symbol sym)
  throws IllegalSymbolException {
    if(sym == a) {
      return u;
    } else if(sym == g) {
      return c;
    } else if(sym == c) {
      return g;
    } else if(sym == u) {
      return a;
    }
    Symbol s = (Symbol) symbolToComplement.get(sym);
    if(s != null) {
      return s;
    } else {
      getRNA().validate(sym);
      throw new BioError(
        "Really confused. Can't find symbol " +
        sym.getName()
      );
    }
  }

  /**
   * Retrieve the symbol for a symbol.
   *
   * @param token  the char to look up
   * @return  the symbol for that char
   * @throws IllegalSymbolException if the char does not belong to {a, g, c, t}
   */
  static public Symbol forSymbol(char token)
  throws IllegalSymbolException {
    if(token == 'a') {
      return a;
    } else if(token == 'g') {
      return g;
    } else if(token == 'c') {
      return c;
    } else if(token == 'u') {
      return u;
    }
    throw new IllegalSymbolException("Unable to find symbol for token " + token);
  }

  /**
   * Retrieve a complement view of list.
   *
   * @param list  the SymbolList to complement
   * @return a SymbolList that is the complement
   * @throws IllegalAlphabetException if list is not a complementable alphabet
   */
  public static SymbolList complement(SymbolList list)
  throws IllegalAlphabetException {
    return SymbolListViews.translate(list, complementTable());
  }

  /**
   * Retrieve a reverse-complement view of list.
   *
   * @param list  the SymbolList to complement
   * @return a SymbolList that is the complement
   * @throws IllegalAlphabetException if list is not a complementable alphabet
   */
  public static SymbolList reverseComplement(SymbolList list)
  throws IllegalAlphabetException {
    return SymbolListViews.translate(SymbolListViews.reverse(list), complementTable());
  }

  /**
   * Transcribe DNA into RNA.
   *
   * @param list the SymbolList to transcribe
   * @return a SymbolList that is the transcribed view
   * @throws IllegalAlphabetException if the list is not DNA
   */
   public static SymbolList transcribe(SymbolList list)
   throws IllegalAlphabetException {
     return SymbolListViews.translate(list, transcriptionTable());
   }

  /**
   * Get a translation table for complementing DNA symbols.
   *
   * @since 1.1
   */

  public static ReversibleTranslationTable complementTable() {
    return complementTable;
  }

  /**
   * Get a translation table for converting DNA to RNA.
   *
   * @since 1.1
   */
  public static ReversibleTranslationTable transcriptionTable() {
    return transcriptionTable;
  }

  /**
   * Retrieve a TranslationTable by name. The valid names are:
   * <ul>
   * <li>"UNIVERSAL"
   * <li>"BACTERIAL"
   * <li>"YEAST_MITOCHONDRIAL"
   * <li>"VERTEBRATE_MITOCHONDRIAL"
   * <li>"MOLD_MITOCHONDRIAL"
   * <li>"INVERTEBRATE_MITOCHONDRIAL"
   * <li>"ECHINODERM_MITOCHONDRIAL"
   * <li>"ASCIDIAN_MITOCHONDRIAL"
   * <li>"FLATWORM_MITOCHONDRIAL"
   * <li>"CILIATE_NUCLEAR"
   * <li>"EUPLOTID_NUCLEAR"
   * <li>"ALTERNATIVE_YEAST_NUCLEAR"
   * <li>"BLEPHARISMA_MACRONUCLEAR"
   * </ul>
   *
   * @since 1.1
   */
  public static TranslationTable getGeneticCode(String name) {
    return (TranslationTable) geneticCodes.get(name);
  }

  /**
   * Retrieve a Set containing the name of each genetic code.
   *
   * @since 1.1
   */
  public static Set getGeneticCodeNames() {
    return geneticCodes.keySet();
  }

  /**
   * Translate RNA into protein (with termination symbols).  For
   * compatibility with BioJava 1.1, this will also handle sequences
   * which are already expressed in the (RNA x RNA x RNA) (codon)
   * alphabet.
   *
   * @since 1.1
   */
  public static SymbolList translate(SymbolList syms)
    throws IllegalAlphabetException
  {
      if (syms.getAlphabet() == getRNA()) {
	  syms = SymbolListViews.windowedSymbolList(syms, 3);
      }
      return SymbolListViews.translate(syms, getGeneticCode("UNIVERSAL"));
  }

  private static void loadGeneticCodes() {
    try {
      InputStream tablesStream = RNATools.class.getClassLoader().getResourceAsStream(
        "org/biojava/bio/seq/TranslationTables.xml"
      );
      if(tablesStream == null ) {
        throw new BioError("Couldn't locate TranslationTables.xml.");
      }

      InputSource is = new InputSource(tablesStream);
      DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = parser.parse(is);

      NodeList children = doc.getDocumentElement().getChildNodes();
      for(int i = 0; i < children.getLength(); i++) {
        Node cnode = children.item(i);
        if(! (cnode instanceof Element)) {
          continue;
        }

        Element child = (Element) cnode;
        String name = child.getNodeName();
        if(name.equals("table")) {
          String tableName = child.getAttribute("name");
          String source = child.getAttribute("source");
          String target = child.getAttribute("target");
          FiniteAlphabet sourceA =
            (FiniteAlphabet) AlphabetManager.alphabetForName(source);
          FiniteAlphabet targetA =
            (FiniteAlphabet) AlphabetManager.alphabetForName(target);
          SymbolTokenization sourceP = sourceA.getTokenization("name");
          SymbolTokenization targetP = targetA.getTokenization("name");
          SimpleTranslationTable table = new SimpleTranslationTable(
            sourceA,
            targetA
          );

          NodeList translates = child.getChildNodes();
          for(int j = 0; j < translates.getLength(); j++) {
            Node tn = translates.item(j);
            if(tn instanceof Element) {
              Element te = (Element) tn;
              String from = te.getAttribute("from");
              String to = te.getAttribute("to");

	      //
	      // Not the most elegant solution, but I wanted this working
	      // quickly for 1.1.  It's been broken for ages.
	      //     -td 26/i/20001
	      //

	      SymbolList fromSymbols = RNATools.createRNA(from);
	      if (fromSymbols.length() != 3) {
		  throw new BioError("`" + from + "' is not a valid codon");
	      }

              // AtomicSymbol fromS = (AtomicSymbol) sourceP.parseToken(from);
	      AtomicSymbol fromS = (AtomicSymbol) sourceA.getSymbol(fromSymbols.toList());
              AtomicSymbol toS   = (AtomicSymbol) targetP.parseToken(to);
              table.setTranslation(fromS, toS);
            }
          }

          geneticCodes.put(tableName, table);
        }
      }
    } catch (Exception e) {
      throw new BioError(e, "Couldn't parse TranslationTables.xml");
    }
  }
  
  private abstract static class AbstractTT
  implements ReversibleTranslationTable {
    protected abstract AtomicSymbol doTranslate(AtomicSymbol sym)
    throws IllegalSymbolException;
    protected abstract AtomicSymbol doUntranslate(AtomicSymbol sym)
    throws IllegalSymbolException;
    
    
    public Symbol translate(Symbol s)
    throws IllegalSymbolException {
      if(s instanceof AtomicSymbol) {
        return doTranslate((AtomicSymbol) s);
      } else {
        Set syms = new HashSet();
        for(
          Iterator i = ((FiniteAlphabet) s.getMatches()).iterator();
          i.hasNext();
        ) {
          AtomicSymbol is = (AtomicSymbol) i.next();
          syms.add(doTranslate(is));
        }
        return getTargetAlphabet().getAmbiguity(syms);
      }
    }
    
    public Symbol untranslate(Symbol s)
    throws IllegalSymbolException {
      if(s instanceof AtomicSymbol) {
        return doUntranslate((AtomicSymbol) s);
      } else {
        Set syms = new HashSet();
        for(
          Iterator i = ((FiniteAlphabet) s.getMatches()).iterator();
          i.hasNext();
        ) {
          AtomicSymbol is = (AtomicSymbol) i.next();
          syms.add(doUntranslate(is));
        }
        return getSourceAlphabet().getAmbiguity(syms);
      }
    }
  }
  
  /**
   * Sneaky class for complementing RNA bases.
   */

  private static class RNAComplementTranslationTable
  extends AbstractTT {
    public AtomicSymbol doTranslate(AtomicSymbol s)
	  throws IllegalSymbolException {
	    return (AtomicSymbol) RNATools.complement(s);
	  }

	  public AtomicSymbol doUntranslate(AtomicSymbol s)
	  throws IllegalSymbolException {
	    return (AtomicSymbol) RNATools.complement(s);
    }

	  public Alphabet getSourceAlphabet() {
	    return RNATools.getRNA();
	  }

	  public Alphabet getTargetAlphabet() {
	    return RNATools.getRNA();
	  }
  }

  /**
   * Sneaky class for converting DNA->RNA.
   */

  private static class TranscriptionTable
  extends AbstractTT {
    public AtomicSymbol doTranslate(AtomicSymbol s)
    throws IllegalSymbolException {
      if(s == DNATools.t()) {
        return RNATools.u();
      }
      DNATools.getDNA().validate(s);
      return s;
    }

    public AtomicSymbol doUntranslate(AtomicSymbol s)
    throws IllegalSymbolException {
      if(s == RNATools.u()) {
        return DNATools.t();
      }
      RNATools.getRNA().validate(s);
      return s;
    }

    public Alphabet getSourceAlphabet() {
      return DNATools.getDNA();
    }

    public Alphabet getTargetAlphabet() {
      return RNATools.getRNA();
    }
  }
}

