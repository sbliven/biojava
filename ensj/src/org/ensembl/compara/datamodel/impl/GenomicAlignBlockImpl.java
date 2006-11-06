/*
  Copyright (C) 2003 EBI, GRL

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
package org.ensembl.compara.datamodel.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ensembl.compara.datamodel.GenomicAlign;
import org.ensembl.compara.datamodel.GenomicAlignBlock;
import org.ensembl.compara.datamodel.MethodLinkSpeciesSet;
import org.ensembl.compara.driver.FatalException;
import org.ensembl.compara.driver.GenomicAlignAdaptor;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.impl.PersistentImpl;
import org.ensembl.driver.AdaptorException;

/**
 * I am part of the compara-analysis. I keep information about
 * groups of dna-dna alignments between species.
**/
public class GenomicAlignBlockImpl extends PersistentImpl implements GenomicAlignBlock{
  
  private static final long serialVersionUID = 1L;
	private List _genomicAligns = null;
  private int _length;
  private GenomicAlign _referenceGenomicAlign;
  private Location _referenceSlice;
  private MethodLinkSpeciesSet _methodLinkSpeciesSet;
  private long _methodLinkSpeciesSetInternalId;
  private int _referenceSliceStart;
  private int _referenceSliceEnd;
  private double _score;
  private double _percentageID;
  private long _referenceGenomicAlignInternalID;
  
  /**
   * Adds a GenomicAlign to this block.
   * 
   * Note that code which calls this method *SHOULD* add
   * all GenomicAligns for this block otherwise
   * getAllGenomicAligns() will be missing items.  
   */
 public void addGenomicAlign(GenomicAlign align){
   	if (_genomicAligns==null) 
   	  _genomicAligns = new ArrayList();
   	_genomicAligns.add(align);
  }

  public  List getAlignmentStrings(){
    Iterator alignIterator = getAllGenomicAligns().iterator();
    GenomicAlign align;
    List returnList = new ArrayList();
    while(alignIterator.hasNext()){
      align = (GenomicAlign)alignIterator.next();
      returnList.add(align.getAlignedSequence());
    }
    return returnList;
  }
  


  /**
   * Return all genomicAligns.
   * 
   * Lazy loads if none currently loaded. 
   */
  public  List getAllGenomicAligns(){
    if(_genomicAligns == null) {
      GenomicAlignAdaptor adaptor;
      try{
        adaptor = (GenomicAlignAdaptor)getDriver().getAdaptor(GenomicAlignAdaptor.TYPE);
        _genomicAligns = adaptor.fetch(this);
      }catch(AdaptorException exception){
        throw new FatalException("Attempt to fetch genomic aligns for block "+getInternalID()+" failed: "+exception.getMessage(), exception);
      }
    }
    return _genomicAligns;
  }
  
  public  List getAllNonreferenceGenomicAligns(){
    long referenceGenomicAlignID = getReferenceGenomicAlign().getInternalID();
    Iterator aligns = getAllGenomicAligns().iterator();
    GenomicAlign align;
    List returnList = new ArrayList();
    while(aligns.hasNext()){
      align = (GenomicAlign)aligns.next();
      if(referenceGenomicAlignID != align.getInternalID()){
        returnList.add(align);
      }
    }
    return returnList;
  }
  
  public  List getAllUngappedGenomicAlignBlocks(){
    throw new org.ensembl.compara.driver.FatalException("NOT IMPLEMENTED YET");
  }
  
  public  int getLength(){
    if(_length == 0){
      _length = ((GenomicAlign)getAllGenomicAligns().get(0)).getAlignedSequence().length();
    }
    return _length;
  }
  
  public  void setLength(int length){
    _length = length;
  }
  
  public  MethodLinkSpeciesSet getMethodLinkSpeciesSet(){
    return _methodLinkSpeciesSet;
  }
  
  public  void setMethodLinkSpeciesSet(MethodLinkSpeciesSet set){
    _methodLinkSpeciesSet = set;
  }
  
  public  long getMethodLinkSpeciesSetInternalId(){
    return _methodLinkSpeciesSetInternalId;
  }
  
  public  void setMethodLinkSpeciesSetInternalId(long id){
    _methodLinkSpeciesSetInternalId = id;
  }
  
  public  double getPercentageID(){
    return _percentageID; 
  }
  
  public  void setPercentageID(double percId){
    _percentageID = percId;
  }
  
  public  GenomicAlign getReferenceGenomicAlign(){
    return _referenceGenomicAlign;
  }
  
  public  void setReferenceGenomicAlign(GenomicAlign align){
    _referenceGenomicAlign = align;
  }
  
  public  Location getReferenceSlice(){
    return _referenceSlice;
  }
  
  public  void setReferenceSlice(Location refSlice){
    _referenceSlice = refSlice;
  }
  
  public  int getReferenceSliceEnd(){
    return _referenceSliceEnd;
  }
  
  public  void setReferenceSliceEnd(int end){
    _referenceSliceEnd = end;
  }
  
  public int getReferenceSliceStart(){
    return _referenceSliceStart;
  }
  
  public  void setReferenceSliceStart(int start){
    _referenceSliceStart = start;
  }

  public  void reverseComplement(){
    
  }
  
  public double getScore(){
    return _score; 
  }
  
  public  void setScore(double score){
    _score = score;
  }
  
  public String toString(){
    StringBuffer output = 
      new StringBuffer()
        .append("GenomicAlignBlock:[")
        .append(getInternalID())
        .append("](Ref Align:")
        .append(getReferenceGenomicAlign().getInternalID())
        .append(")")
        .append(")");
   
    return output.toString();
  } 

  public long getReferenceGenomicAlignInternalID() {
    return _referenceGenomicAlignInternalID;
  }

  public void setReferenceGenomicAlignInternalID(long id) {
    _referenceGenomicAlignInternalID = id;
  }

}
