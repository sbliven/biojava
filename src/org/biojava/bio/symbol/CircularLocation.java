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
 */

package org.biojava.bio.symbol;

import java.util.*;


import org.biojava.bio.*;



/**
 * Circular view onto an underlying Location instance. If the location overlaps
 * the origin of the sequence the underlying location will be a CompoundLocation
 * Note that in this case isContiguous() will return false. This behaviour is
 * desirable for proper treatment of the location with LocationTools.
 * To find if a location overlaps the origin use the overlapsOrigin() method
 * <p>
 * Note also that as a location that overlaps the origin is a compound location it's
 * min will be 1 and its max will be length (by default). In these cases it is imperative to
 * use the block iterator if you wish to know the 'true' min and max (bearing in mind that
 * there is no logical value for a min or max on a circular sequence).
 * </p>
 *  <p>
 * The symbols() method has been overridden to handle the weirdness of a location crossing the
 * origin.
 * </p>
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


  public String toString(){

    StringBuffer sb = new StringBuffer(getWrapped().toString());

    sb.append("  (circular)");

    return sb.toString();

  }


  /**
   * Delegates to the wrapped location. Currently as locations that cross
   * the origin are wrapped CompoundLocations they are not considered contiguous.
   * This is desirable from the point of view of logical operations as it greatly
   * simplifies the calculations of things such as contains, overlaps etc.
   */
  public boolean isContiguous() {
    return getWrapped().isContiguous();
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

