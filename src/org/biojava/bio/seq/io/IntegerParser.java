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

package org.biojava.bio.seq.io;

import java.util.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * Title:        IntegerParser
 * Description:  Converts characters or a string into Symbols from the IntegerAlphabet.
 *               A hack of the TokenParser class to allow the use of the infinite IntegerAlphabet.
 * Copyright:    Copyright (c) 2001
 * Company:      AgResearch
 * @author Mark Schreiber
 * @since 1.1
 */

public class IntegerParser implements SymbolParser, Serializable {

  private IntegerAlphabet alpha;

  public IntegerParser(){
    alpha = IntegerAlphabet.getInstance();
  }

  public Alphabet getAlphabet(){
    return alpha;
  }

  public Symbol parseToken(String seq)throws IllegalSymbolException{
    int i = Integer.parseInt(seq);
    return alpha.getSymbol(i);
  }

  public SymbolList parse(String seq) throws IllegalSymbolException{
    StringTokenizer st = new StringTokenizer(seq);
    int[] ints = new int[st.countTokens()];
    for(int x = 0; st.hasMoreTokens(); x++){
      String token = st.nextToken();
      try{
        int i = Integer.parseInt(token);
        ints[x] = i;
      }catch(NumberFormatException nfe){
        throw new IllegalSymbolException(nfe, token+" is not a legal integer");
      }
    }

    SymbolList sl = IntegerAlphabet.fromArray(ints);
    return sl;
  }

  public StreamParser parseStream(SeqIOListener listener) {
    return new IPStreamParser(listener);
  }

  Symbol parseCharToken(char token){
      int i = Character.getNumericValue(token);
      return alpha.getSymbol(i);
  }

  private class IPStreamParser implements StreamParser {
    private SeqIOListener listener;
    private Symbol[] buffer;

    public IPStreamParser(SeqIOListener l) {
      this.listener = l;
    }

    public void characters(char[] data, int start, int len) throws IllegalSymbolException{
      String s = new String(data);
      String ss = s.substring(start, start+len);
      SymbolList sl = parse(ss);
      buffer = new Symbol[sl.length()];
      for(int i = 0; i < buffer.length; i++){
        buffer[i] = sl.symbolAt(i+1);
      }
      try {
        listener.addSymbols(getAlphabet(),buffer,0,buffer.length);
      } catch (IllegalAlphabetException ex) {
        throw new BioError(ex, "Assertion failed: can't add symbols.");
      }

    }

    public void close() {
    }
  }
}