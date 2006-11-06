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
package org.ensembl.compara.datamodel;

import java.util.List;

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Persistent;

/**
 * I am part of the compara-analysis. I keep information about
 * groups of dna-dna alignments between species.
**/
public interface GenomicAlignBlock extends Persistent{
  public  List getAlignmentStrings();
  
  /**
   * Add a GenomicAlign to the block.
   * 
   * @param align an alignment associated with this Block.
   */
  public void addGenomicAlign(GenomicAlign align);

  /**
   * Return genomicAligns. 
   * 
   * @see #addGenomicAlign(GenomicAlign)
   */
  public  List getAllGenomicAligns();
  
  public  List getAllNonreferenceGenomicAligns();
  
  public  List getAllUngappedGenomicAlignBlocks();
  
  public  int getLength();
  
  public  void setLength(int length);
  
  public  MethodLinkSpeciesSet getMethodLinkSpeciesSet();
  
  public  void setMethodLinkSpeciesSet(MethodLinkSpeciesSet set);  
  
  public  long getMethodLinkSpeciesSetInternalId();
  
  public  void setMethodLinkSpeciesSetInternalId(long id);  
  
  public  double getPercentageID();
  
  public  void setPercentageID(double percId);
  
  public  GenomicAlign getReferenceGenomicAlign();
  
  public  void setReferenceGenomicAlign(GenomicAlign align);
  
  public  Location getReferenceSlice();
  
  public  void setReferenceSlice(Location refSlice);
  
  public  int getReferenceSliceEnd();
  
  public  void setReferenceSliceEnd(int end);
  
  public  int getReferenceSliceStart();
  
  public  void setReferenceSliceStart(int end);

  public  void reverseComplement();
  
  public  double getScore();
  
  public  void setScore(double score);

  public long getReferenceGenomicAlignInternalID();

  public void setReferenceGenomicAlignInternalID(long id);

  
}
