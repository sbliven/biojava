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



import org.biojava.bio.*;

import org.biojava.utils.*;

import org.biojava.bio.seq.*;

import org.biojava.bio.seq.impl.*;

import org.biojava.bio.seq.io.*;



import java.util.*;



/**

 * Circular view onto an underlying Location instance. If the location overlaps

 * the origin of the sequence the underlying location will be a CompoundLocation

 * Note that in this case isContiguous() will return false. This behaviour is

 * desirable for proper treatment of the location with LocationTools.

 * To find if a location overlaps the origin use the overlapsOrigin() method

 *

 * @author Matthew Pocock

 * @author Mark Schreiber

 * @since 1.2

 */

public class CircularLocation

extends AbstractLocationDecorator {

  private final int length;

  private final boolean overlaps;



  public final int getLength() {

    return length;

  }



  public boolean overlapsOrigin(){

    return overlaps;

  }


  /**
   * Constructs a CircularLocation by wrapping another Location
   * <strong>It is preferable to use LocationTools to make CircularLocations</strong>
   * @param wrapped the Location to wrap.
   * @param length the length of the Sequence
   */
  public CircularLocation(Location wrapped, int length) {
    super(wrapped);
    this.length = length;
    this.overlaps = CircularLocationTools.overlapsOrigin(this);
  }



  protected Location decorate(Location loc) {

    return new CircularLocation(loc, getLength());

  }



  public boolean contains(int p) {

    int pp = p % getLength() + (super.getMin() / getLength());



    return getWrapped().contains(pp);

  }





  public Location intersection(Location l) {

    return LocationTools.intersection(this,l);

  }

  public boolean overlaps(Location l) {

    return LocationTools.overlaps(this,l);

  }

  public Location union(Location l) {

    return LocationTools.union(this,l);

  }

  public boolean contains(Location l) {

    return LocationTools.contains(this,l);

  }

  public boolean equals(Object o){

    if((o instanceof Location)==false) return false;

    return LocationTools.areEqual(this, (Location)o);

  }

  public int getMax() {

    if(getWrapped().isContiguous()){

      if(getWrapped().getMin() ==1 && getWrapped().getMax() == length){

        return length;

      }

    }

    if(overlaps){

      int max = 1;

      for(Iterator i = getWrapped().blockIterator();i.hasNext();){

        Location l = ((Location)i.next());

        if(l.getMin() == 1) max = l.getMax();

      }

      return max;

    }else{

      return super.getMax();

    }

  }

  public int getMin() {

    if(getWrapped().isContiguous()){

      if(getWrapped().getMin() ==1 && getWrapped().getMax() == length){

        return 1;

      }

    }

    if(overlaps){

      int min = 1;

      for(Iterator i = getWrapped().blockIterator();i.hasNext();){

        Location l = ((Location)i.next());

        if(l.getMax() == length) min = l.getMin();

      }

      return min;

    }else{

      return super.getMin();

    }

  }







  public String toString(){

    StringBuffer sb = new StringBuffer(getWrapped().toString());

    sb.append("  (circular)");

    return sb.toString();

  }



  public boolean isContiguous() {

    boolean a = false;
    boolean b = false;
    int i = 1;

    if(super.isContiguous()) return true;
    if(getWrapped() instanceof CompoundLocation){
      CompoundLocation l = (CompoundLocation)getWrapped();

      for(Iterator iter = l.blockIterator(); iter.hasNext(); i++){
        if(i > 2) return false;
        Location block = (Location)iter.next();
        if(block.getMin() == 1) a = true;
        if(block.getMax() == this.getLength()) b = true;
      }
    }

    return(a && b);
  }


  public SymbolList symbols(SymbolList seq) {
    SymbolList syms;

    //currently overlaps are stored as pseudo compound locations
    if(getWrapped() instanceof CompoundLocation && overlapsOrigin()){
      CompoundLocation loc = (CompoundLocation)getWrapped();

      /*
       * for purposes of constructing the SymbolList the first location
       * on the list is really merged to the last vis a vis the use of
       * a psuedo CompoundLocation.
       */
      LinkedList ll = new LinkedList(loc.getBlockList());
      Object o = ll.removeFirst();
      ll.addLast(o);

      List residues = new ArrayList();
      for (Iterator i = ll.listIterator(); i.hasNext(); ) {
        Location subloc = (Location)i.next();
        residues.addAll(seq.subList(subloc.getMin(), subloc.getMax()).toList());
      }

      try {
        syms = new SimpleSymbolList(seq.getAlphabet(), residues);
      }
      catch (IllegalSymbolException ex) {
        throw new BioError(ex, "Failure to build a SymbolList from its own Symbols!?");
      }
      return syms;
    }

    else if(overlapsOrigin()){
      if(getMin() == 1 && getMax() == length){
        //no special treatment needed
        return seq;
      }
      List l = new ArrayList(seq.subList(this.getMin(), seq.length()).toList());
      l.addAll(seq.subList(1, this.getMax()).toList());

      try {
        syms = new SimpleSymbolList(seq.getAlphabet(), l);
      }
      catch (IllegalSymbolException ex) {
        throw new BioError(ex, "Failure to build a SymbolList from its own Symbols!?");
      }
      return syms;
    }

    //otherwise
    return super.symbols(seq);
  }

}

