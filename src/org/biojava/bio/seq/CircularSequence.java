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

import org.biojava.bio.*;
import org.biojava.utils.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.seq.io.*;

import java.util.*;

/**
 * Circular biological sequence.  This code is currently experimental.
 *
 * @author Mark Schreiber
 * @since 1.1
 * @version 1.0
 * @deprecated Use CircularView instead
 */

public class CircularSequence extends SimpleSequence {
  /**
     * Create a CircularSequence with the symbols and alphabet of sym, and the
     * sequence properties listed.
     *
     * @param sym the SymbolList to wrap as a sequence
     * @param urn the URN
     * @param name the name - should be unique if practical
     * @param annotation the annotation object to use or null
     */
    public CircularSequence(SymbolList sym,
                          String urn,
                          String name,
                          Annotation annotation) {
      super(sym, urn,name,annotation);

    }

    /**
     * Create a CircularSequence using a specified FeatureRealizer.
     *
     * @param sym the SymbolList to wrap as a sequence
     * @param urn the URN
     * @param name the name - should be unique if practical
     * @param annotation the annotation object to use or null
     * @param realizer the FeatureRealizer implemetation to use when adding features
     */
    public CircularSequence(SymbolList sym,
			  String urn,
			  String name,
			  Annotation annotation,
			  FeatureRealizer realizer)
    {
	super(sym,urn,name,annotation,realizer);
    }

  /**
   * Over rides ViewSequence. Allows any integer index, positive or negative
   * to return a symbol via the equation
   * <CODE>index = ((index -1) % length)+1</CODE>
   *
   * Note that an index of 0 will throw an IllegalArgumentException.
   */
  public Symbol symbolAt(int index){

    if (index == 0) throw new IllegalArgumentException("Must use a non 0 integer");
    index = ((index-1) % super.length()) + 1;
    if(index < 0) index = super.length()+1 + index;
    return super.symbolAt(index);
  }

  /**
   * Over rides ViewSequence. Allows any integer index, positive or negative
   * to return a symbol via the equation
   * <CODE>index = ((index -1) % length)+1</CODE>
   *
   * Will return a linear String which can ,if nescessary, span the origin.
   *
   * Note that an index of 0 will throw an IllegalArgumentException.
   */
  public String subStr(int start, int end){
    if(start == 0 || end == 0){
      throw new IllegalArgumentException(
        "Must use a non 0 integer"
      );
    }
    start = ((start-1) % super.length()) + 1;
    end = ((end-1) % super.length()) + 1;
    if(start < 0) start = super.length()+1 + start;
    if(end < 0) end = super.length()+1 + end;
    if(start <= end){
      return super.subStr(start, end);
    }
    else{
      String toEnd = super.subStr(start,super.length());
      String fromStart = super.subStr(1,end);
      return toEnd + fromStart;
    }
  }

  /**
   * Over rides ViewSequence. Allows any integer index, positive or negative
   * to return a symbol via the equation
   * <CODE>index = ((index -1) % length)+1</CODE>
   *
   * Will return a linear SymbolList which can ,if nescessary, span the origin.
   *
   * Note that an index of 0 will throw an IllegalArgumentException.
   */
  public SymbolList subList(int start, int end){
    if(start == 0 || end == 0){
      throw new IllegalArgumentException(
        "Must use a non 0 integer"
      );
    }
    start = ((start-1) % super.length()) + 1;
    end = ((end-1) % super.length()) + 1;

    if(start < 0) start = super.length()+1 + start;
    if(end < 0) end = super.length()+1 + end;

     if(start <= end){
      return super.subList(start, end);
    }
    else{
      SymbolList toEnd = super.subList(start,super.length());
      SymbolList fromStart = super.subList(1,end);
     try{
	 List all = new ArrayList();
	 all.addAll(toEnd.toList());
	 all.addAll(fromStart.toList());
	 return new SimpleSymbolList(getAlphabet(), all);
     }catch(BioException ex){
	 throw new BioError(ex,
          "A serious error has occured during the reconstruction of " +
			    super.getName());
     }
    }
  }
}
