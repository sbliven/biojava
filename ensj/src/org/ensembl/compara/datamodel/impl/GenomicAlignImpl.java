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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ensembl.compara.datamodel.DnaFragment;
import org.ensembl.compara.datamodel.GenomicAlign;
import org.ensembl.compara.datamodel.GenomicAlignBlock;
import org.ensembl.compara.datamodel.MethodLinkSpeciesSet;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.SequenceRegion;
import org.ensembl.datamodel.impl.PersistentImpl;

/**
 * I am part of the compara-analysis.
**/
public class GenomicAlignImpl extends PersistentImpl implements GenomicAlign
{

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

  String cigarString;
  MethodLinkSpeciesSet methodLinkSpeciesSet;
  long methodLinkSpeciesSetInternalId;
  
  private int _strand; 
  private DnaFragment _dnaFragment;
  private long _dnaFragmentId;
  private int _start;
  private int _end;
  
  private String _alignedSequence;
  private String _originalSequence;
  private GenomicAlignBlock _genomicAlignBlock;
  private long _genomicAlignBlockInternalID;

  public String getOriginalSequence(){
    if(_originalSequence != null){
      return _originalSequence;
    }
    
    if(_alignedSequence != null){
      _originalSequence = _alignedSequence.replaceAll("_", "");
      return _originalSequence;
    }

    Location dnaFragLocation = getDnaFragment().getLocation();
    Location newLocation = null;
    if(dnaFragLocation != null){
      newLocation = 
        new Location(
          dnaFragLocation.getCoordinateSystem(),
          dnaFragLocation.getSeqRegionName(),
          dnaFragLocation.getStart()+getStart()-1,
          getEnd(),
          dnaFragLocation.getStrand()
        );
    }else{
      throw new org.ensembl.compara.driver.FatalException(
        "Cannot determine original sequence for DnaFrag - DnaFrag is not initialised with a Location"
      );
    }
    
    SequenceRegion seqRegion = getDnaFragment().getLocation().getSequenceRegion();
    if(seqRegion == null){
      throw new org.ensembl.compara.driver.FatalException(
        "Cannot determine original sequence for DnaFrag - DnaFrag's Location is not initialised with a SequenceRegion"
      );
    }
    
    org.ensembl.driver.CoreDriver coreDriver = getDnaFragment().getLocation().getSequenceRegion().getDriver();
    
    if(coreDriver != null){
      try{
        org.ensembl.driver.SequenceAdaptor sequenceAdaptor = coreDriver.getSequenceAdaptor();
        _originalSequence = sequenceAdaptor.fetch(newLocation).getString();
      }catch(org.ensembl.driver.AdaptorException exception){
        throw new org.ensembl.compara.driver.NonFatalException(
          "Problem fetching original sequence for genomic align:"+exception.getMessage(), exception);
      }
    }else{
      throw new org.ensembl.compara.driver.FatalException(
        "Cannot determine original sequence for DnaFrag - DnaFrag's Location's SequenceRegion is not initialised with a Core driver"
      );
    }
    
    return _originalSequence;
  }
  
  public void setOriginalSequence(String sequence){
    _originalSequence = sequence;
  }
  
  public String getAlignedSequence(String[] flags){
    boolean fix_seq = false;
    if(flags != null && (flags[0] != null) && (flags[0].indexOf("+") > 0)){
      if(!flags[0].equals("+FIX_SEQ")){
        throw new org.ensembl.compara.driver.FatalException(
          "Unknown flag "+flags[0]+" when calling GenomicAlign.getAlignedSequence()"
        );
      }
    }

    String derivedSequence = null;
    
    if(_alignedSequence == null){
      
      if(
          getOriginalSequence() != null &&
          getCigarString() != null
      ){
        _alignedSequence = 
          getAlignedSequenceFromOriginalSequenceAndCigarLine(
            getOriginalSequence(),
            getCigarString(),
            false
          );
      }else{
        throw new org.ensembl.compara.driver.FatalException("Couldn't retrieve aligned sequence from original sequence & cigar line");
      }
    }
    
    if((_alignedSequence != null) && fix_seq ){
      getAlignedSequenceFromOriginalSequenceAndCigarLine(
        getOriginalSequence(),
        getGenomicAlignBlock().getReferenceGenomicAlign().getCigarString(),
        fix_seq
      );
    }
    
    return _alignedSequence;
  }
  
  public String getAlignedSequence(){
    return getAlignedSequence(null);
  }
  
  public void setAlignedSequence(String alignedSequence){
    _alignedSequence = alignedSequence;
  }
    
  private String getAlignedSequenceFromOriginalSequenceAndCigarLine(
    String sequence,
    String cigarLine,
    boolean fix_seq
  ){
    if(sequence == null || cigarLine == null){
      return null;
    }
    
    StringBuffer alignedSequenceBuffer = new StringBuffer();
    Pattern pattern = Pattern.compile("(\\d*[GMD])");
    Matcher matcher = pattern.matcher(cigarLine);
    
    int sequencePosition = 0;
    
    for(int group = 1; group < matcher.groupCount(); group++){
      String cigarElement = matcher.group(group);
      int length = cigarElement.length();
      String cigarType = cigarElement.substring(length-1, length);
      int cigarCount = 1;
      if(length > 1){
        String cigarCountString = cigarElement.substring(0, length-1);
        cigarCount = Integer.valueOf(cigarCountString).intValue();
      }
      
      if(cigarType.equals("M")){
        alignedSequenceBuffer.append(sequence.substring(sequencePosition, cigarCount));
        sequencePosition += cigarCount;
      }else if(cigarType.equals("G") || cigarType.equals("D")){
        if(fix_seq){
          sequencePosition += cigarCount;
        }else{
          StringBuffer blankBuffer = new StringBuffer();
          for(int i=0; i<cigarCount; i++){
            blankBuffer.append("-");
          }
          alignedSequenceBuffer.append(blankBuffer);
        }
      }
    }
    
    if(sequencePosition != sequence.length()){
      throw new org.ensembl.compara.driver.FatalException("Cigar Line ("+sequencePosition+") doesn't match original sequence length: "+sequence.length());
    }
    
    return alignedSequenceBuffer.toString();
  }
  
  public String  getDisplayID(){
    throw new org.ensembl.compara.driver.FatalException("IMPLEMENT ME!");
  }
  
  public GenomicAlignBlock  getGenomicAlignBlock(){
    return _genomicAlignBlock;
  }
  
  public void setGenomicAlignBlock(GenomicAlignBlock value){
    _genomicAlignBlock = value;
  }
  
  public void reverseComplement(){
    throw new org.ensembl.compara.driver.FatalException("IMPLEMENT ME!");
  }
 
  public int getStrand(){
    return _strand;
  }
  
  public void setStrand(int strand){
    _strand = strand;
  }
  
  public DnaFragment getDnaFragment(){
    return _dnaFragment;
  }
  
  public void setDnaFragment(DnaFragment dnaFrag){
    _dnaFragment = dnaFrag;
  }
  
  public long getDnaFragmentId(){
    return _dnaFragmentId;
  }
  
  public void setDnaFragmentId(long dnaFragId){
    _dnaFragmentId = dnaFragId;
  }
  
  public int getStart(){
    return _start;
  }
  
  public void setStart(int newValue){
    _start = newValue;
  }
  
  public int getEnd(){
    return _end;
  }
  
  public void setEnd(int newValue){
    _end = newValue;
  }
  public String getCigarString() {
    return cigarString;
  }//end getCigarString
  
  public void setCigarString(String cigarString) {
    this.cigarString = cigarString;
  }//end setCigarString

  public String toString(){
    return 
      (new StringBuffer())
        .append("Genomic align(")
        .append(getInternalID())
        .append(")[")
        .append(getDnaFragment())
        .append(",")
        .append(getStart())
        .append("-")
        .append(getEnd())
        .append(",")
        .append(getStrand())
        .append("]").toString();
  }//end toString
  
  public MethodLinkSpeciesSet getMethodLinkSpeciesSet(){
    return methodLinkSpeciesSet;
  }//end getMethodLink
  
  public long getMethodLinkSpeciesSetInternalId(){
    return methodLinkSpeciesSetInternalId;
  }//end getMethodLinkInternalId
  
  public void setMethodLinkSpeciesSet(MethodLinkSpeciesSet newValue){
    methodLinkSpeciesSet = newValue;
  }//end setMethodLink
  
  public void setMethodLinkSpeciesSetInternalId(long newValue){
    methodLinkSpeciesSetInternalId = newValue;
  }
  
  public long getGenomicAlignBlockInternalID() {
    return _genomicAlignBlockInternalID;
  }
  
  public void setGenomicAlignBlockInternalID(long block) {
    _genomicAlignBlockInternalID = block;
  }
  
//end setMethodLinkSpeciesSetInternalId
  
  
}//end GenomicAlignImpl
