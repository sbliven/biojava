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

import org.biojava.bio.seq.io.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.utils.*;

import java.util.*;

/**
 * <p>
 * A circular view onto another Sequence object.  The class allows for
 * reinterpretation of locations and indices onto the sequence to allow for
 * overlapping of the origin. The origin is assumed to be the first symbol.
 * Future versions may support changing the origin.
 * </p>
 *
 * <p>
 * This code is currently experimental
 * </p>
 *
 * @author Mark Schreiber
 * @version 1.1
 * @since 1.1
 */

public class CircularView extends ViewSequence{
  public CircularView(Sequence seq, FeatureRealizer fr){
    super(seq, fr);
  }

  public CircularView(Sequence seq){
    super(seq);
  }

  /**
   * <p>
   * Over rides ViewSequence. Allows any integer index, positive or negative
   * to return a symbol via the equation
   * <CODE>index = ((index -1) % length)+1</CODE>
   * </p>
   *
   * <p>
   * Note that an index of 0 will throw an IllegalArgumentException.
   * </p>
   */
  public Symbol symbolAt(int index){

    if (index == 0) throw new IllegalArgumentException("Must use a non 0 integer");
    index = ((index-1) % super.length()) + 1;
    if(index < 0) index = super.length()+1 + index;
    return super.symbolAt(index);
  }

  /**
   * <p>
   * Over rides ViewSequence. Allows any integer index, positive or negative
   * to return a symbol via the equation
   * <CODE>index = ((index -1) % length)+1</CODE>
   * </p>
   *
   * <p>
   * Will return a linear String which can, if nescessary, span the origin.
   * </p>
   *
   * <p>
   * Note that an index of 0 will throw an IllegalArgumentException.
   * </p>
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
   * Over rides ViewSequence to allow the use of locations that have
   * coordinates outside of the sequence length (which are needed to
   * describe locations that overlap the origin of a circular sequence).
   *
   * @since 1.2
   */
  public Feature createFeature(Feature.Template template)
        throws ChangeVetoException, BioException
    {
      Location loc = template.location;
      if(loc.getMax() > length() && (loc instanceof CircularLocation == false)){
        throw new BioException("Only CircularLocations may exceed sequence length");
      }
      Feature f = realizeFeature(this, template);
      ((SimpleFeatureHolder)getAddedFeatures()).addFeature(f);
      return f;
    }

  /**
   * <p>
   * Over rides ViewSequence. Allows any integer index, positive or negative
   * to return a symbol via the equation
   * <CODE>index = ((index -1) % length)+1</CODE>
   * </p>
   *
   * <p>
   * Will return a linear SymbolList which can ,if nescessary, span the origin.
   * </p>
   *
   * <p>
   * Note that an index of 0 will throw an IllegalArgumentException.
   * </p>
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
      String toEnd = super.subStr(start,super.length());
      String fromStart = super.subStr(1,end);
      String s = toEnd + fromStart;
     try{
        Alphabet alpha = super.getAlphabet();
        SymbolTokenization sp = alpha.getTokenization("token");
        SymbolList seq = new SimpleSymbolList(sp, s);
        return seq;
     }catch(BioException be){
        System.err.println(// This should never happen
          "A serious error has occured during the reconstruction of " +
          super.getName());
          return null;
     }
    }
  }
}

