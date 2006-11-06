/*
  Copyright (C) 2002 EBI, GRL
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
 
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.datamodel;

import java.text.ParseException;
import java.util.Comparator;

import org.ensembl.util.Warnings;

/**
 * Location on an assembly. The location is specified by any or
 * all of these properties <i>AssemblyMap</i>, <i>chromosome</i>,
 * <i>start</i>, <i>end</i> and <i>strand</i>.
 *
 * @deprecated Create instances of Location directly. The specific 
 * location types such as AssemblyLocation have been superseded by 
 * the more generic Location class.
 */
public class AssemblyLocation extends Location {

  /**
   * Used by the (de)serialization system to determine if the data 
   * in a serialized instance is compatible with this class.
   *
   * It's presence allows for compatible serialized objects to be loaded when
   * the class is compatible with the serialized instance, even if:
   *
   * <ul>
   * <li> the compiler used to compile the "serializing" version of the class
   * differs from the one used to compile the "deserialising" version of the
   * class.</li>
   *
   * <li> the methods of the class changes but the attributes remain the same.</li>
   * </ul>
   *
   * Maintainers must change this value if and only if the new version of
   * this class is not compatible with old versions. e.g. attributes
   * change. See Sun docs for <a
   * href="http://java.sun.com/j2se/1.4.2/docs/guide/serialization/">
   * details. </a>
   *
   */
  private static final long serialVersionUID = 1L;


  

  public final static CoordinateSystem DEFAULT_CS = new CoordinateSystem("chromosome");
  // must iniitialise DEFAULT after DEFAULT_CS because the empty constructor requires
  // DEFAULT_CS to be set, produces ExceptionInInitializer at runtime otherwise.
  public final static AssemblyLocation DEFAULT = new AssemblyLocation();
  
  /**
   * @return new Location("chromosome:"+s);
   */
  public static Location valueOf(String s) throws ParseException {
    return new Location("chromosome:"+s);
  }
  
  
  /**
   * Returns a comparator that orders <code>AssemblyLocation</code> by the
   * following fields in this order: chromosome, start, end and strand.
   */
  public final static Comparator ASCENDING_ORDER = new AscendingOrder();
  
  private static class AscendingOrder implements Comparator {
    public int compare(Object o1, Object o2) {
      AssemblyLocation l1 = (AssemblyLocation)o1;
      AssemblyLocation l2 = (AssemblyLocation)o2;
      
      final int l1Chr = l1.encodeChromosomeAsInt();
      final int l2Chr = l2.encodeChromosomeAsInt();
      if ( l1Chr > l2Chr ) return 1;
      if ( l1Chr < l2Chr ) return -1;
      
      if ( l1.getStart() > l2.getStart() ) return 1;
      if ( l1.getStart() < l2.getStart() ) return -1;
      
      if ( l1.getEnd() > l2.getEnd() ) return 1;
      if ( l1.getEnd() < l2.getEnd() ) return -1;
      
      if ( l1.getStrand() > l2.getStrand() ) return 1;
      if ( l1.getStrand() < l2.getStrand() ) return -1;
      
      return 0;
    }
  }
  
  
  /**
   * Encode chromosome string as an int. This value is used by the
   * _AscendingOrder_ comparator for ordering AssemblyLocations.
   */
  private int encodeChromosomeAsInt() {
    if ( chromosomeAsInt == -1 ) {
      if ( chromosome == null ) chromosomeAsInt = 0;
      else {
        
        try {
          chromosomeAsInt = Integer.parseInt(chromosome);
        }catch(NumberFormatException e) {
          // Treat chromosome as a string and convert each character to ascii.
          byte[] bytes = chromosome.getBytes();
          final int len = bytes.length;
          chromosomeAsInt = 0;
          int power = len;
          for (int i=0; i<len; ++i ) {
            chromosomeAsInt += bytes[i]*Math.pow(1000, power--);
          }
        }
      }
    }
    
    return chromosomeAsInt;
  }
  
  
  public AssemblyLocation(CoordinateSystem cs, String chromosome, int start,int end, int strand) throws InvalidLocationException {
    super(cs, chromosome, start, end, strand);
    setChromosome(chromosome);
    Warnings.deprecated("Instantiating AssemblyLocation directly is no longer advisable - use Location instead."); 
  }
  
  
  
  /**
   * Sets map to AssemblyLocation.DEFAULT_CS.
   */
  public AssemblyLocation(String chromosome,int start, int end, int strand) {
    this(AssemblyLocation.DEFAULT_CS, chromosome, start, end, strand);
    Warnings.deprecated("Instantiating AssemblyLocation directly is no longer advisable - use Location instead.");
  }
  
  
  /**
   * Constructs a location with map=AssemblyLocation.DEFAULT_MAP,
   * start=1, end=length+1, strand=unset and whether it is a gap.
   */
  public AssemblyLocation(boolean gap, int length) {
    super(AssemblyLocation.DEFAULT_CS, gap, length);
    Warnings.deprecated("Instantiating AssemblyLocation directly is no longer advisable - use Location instead.");   
  }
  
  /**
   * Constructs a location with specified map, start=1, end=length+1,
   * strand=unset and whether it is a gap.
   */
  public AssemblyLocation(CoordinateSystem cs, boolean gap, int length) {
    super(cs, gap, length);
    Warnings.deprecated("Instantiating AssemblyLocation directly is no longer advisable - use Location instead.");
  }
  
  
  /**
   * Sets map to AssemblyLocation.DEFAULT_MAP.
   */
  public AssemblyLocation(){
    super(AssemblyLocation.DEFAULT_CS);
    Warnings.deprecated("Instantiating AssemblyLocation directly is no longer advisable - use Location instead.");    
  }
  
  public String getChromosome(){
    return chromosome;
  }
  
  /**
   * Note: if you include 'chr' at the beginning of the
   * chromosome name it will be removed. This is done to avoid
   * incompatibities with certain databases.
   */
  public void setChromosome(String chromosome){
    if (chromosome!=null && chromosome.startsWith("chr")) this.chromosome = chromosome.substring(3);
    else this.chromosome = chromosome;
    chromosomeAsInt = -1; // unset cached value.
  }
  
  public boolean isChromosomeSet() {
    return chromosome!=null;
  }
  
  public void clearChromosome() {
    chromosome = null;
  }
  
  
  
  /**
   * Checks if locations overlap each other, ignores strand.
   * @return true if locations overlap.
   */
  public boolean overlaps( Location other ) {
    
    //if ( other.getType()!=TYPE ) return false;
    
    for( AssemblyLocation x=this; x!=null; x=x.nextAL() ) {
      
      for( AssemblyLocation y=(AssemblyLocation)other; y!=null; y=y.nextAL() ) {
        
        if ( ( x.getStart()<=y.getEnd() || !x.isStartSet() || !y.isEndSet()  )
        && ( x.getEnd()>=y.getStart() || !x.isEndSet() || !y.isStartSet() )
        && ( x.chromosome==null
        || y.chromosome==null
        || x.chromosome.equalsIgnoreCase(y.chromosome) )
        && x.getCoordinateSystem().equals(y.getCoordinateSystem())
        ) return true;
        
      }
    }
    
    return false;
    
  }
  
  
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("[");
    buf.append(super.toString());
    buf.append(", chromosome=").append(chromosome);
    buf.append(", next=").append(next);
    buf.append("]");
    
    return buf.toString();
  }
  
  private AssemblyLocation next;
  
  public Location next(){ return next; }
  
  /**
   * @return next location as an AssemblyLocation, or null if not set.
   */
  public AssemblyLocation nextAL() { return next; }
  
  public void setNext(AssemblyLocation next){ this.next = next; }
  
  
  public boolean hasNext(){
    return next!=null;
  }
  
  /**
   * Adds _location_ to the end of the location list, this is the same as using setNext(location) if next==null.
   */
  public AssemblyLocation append(AssemblyLocation location) throws InvalidLocationException {
    if (location==this) throw new InvalidLocationException("Location is already at front of location list: " + location);
    
    AssemblyLocation node = null;
    for(node=this; node.next!=null; node=node.next) {
      if (node==node.next) throw new InvalidLocationException("Location is already in location list: " + location);
    }
    node.next = location;
    
    return this;
  }
  
  public Location append(Location location){
    return append( (AssemblyLocation)location );
  }
  
  
  
  public Location reverse(){
    
    AssemblyLocation nxt = next;
    if ( nxt==null ) return this;
    
    AssemblyLocation prev = null;
    AssemblyLocation curr = this;
    
    // move through list changing next values.
    do {
      curr.next = prev;
      
      prev = curr;
      curr = nxt;
      nxt = nxt.next;
    } while( nxt!=null );
    curr.next = prev;
    
    return curr;
  }
  
  
  
  /**
   * Iterate through this location and the other one comparing them. The
   * order of the attributes used for the ordering is: chromosome, start,
   * end, strand.
   * @return -1 if this location is before other, 0 if it is equivalent, 1 if
   * it is after it.
   */
  public int compareTo(Object other) {
    
    if ( other==null ) return 1;
    
    AssemblyLocation nxt=this;
    AssemblyLocation nxt2 = (AssemblyLocation)other;
    
    while( nxt!=null ) {
      
      final int nxtChr = nxt.encodeChromosomeAsInt();
      final int nxt2Chr = nxt2.encodeChromosomeAsInt();
      if ( nxtChr > nxt2Chr ) return 1;
      if ( nxtChr < nxt2Chr ) return -1;
      
      if ( nxt.getStart() > nxt2.getStart() ) return 1;
      if ( nxt.getStart() < nxt2.getStart() ) return -1;
      
      if ( nxt.getEnd() > nxt2.getEnd() ) return 1;
      if ( nxt.getEnd() < nxt2.getEnd() ) return -1;
      
      if ( nxt.getStrand() > nxt2.getStrand() ) return 1;
      if ( nxt.getStrand() < nxt2.getStrand() ) return -1;
      
      nxt=nxt.nextAL();
      nxt2=nxt2.nextAL();
      
      if ( nxt==null && nxt2!=null ) return -1;
      if ( nxt2==null && nxt!=null ) return 1;
    }
    
    return 0;
  }
  
  private String chromosome;
  
  /**
   * Cached int representation of chromosome.
   */
  private int chromosomeAsInt = -1;
}
