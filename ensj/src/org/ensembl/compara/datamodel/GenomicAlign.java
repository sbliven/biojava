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

import org.ensembl.datamodel.Persistent;

/**
 * I am part of the compara-analysis. I keep information about
 * DnaDna alignments between species.
**/
public interface GenomicAlign extends Persistent{
  public String getCigarString();
  public void setCigarString(String cigarString);
  public MethodLinkSpeciesSet getMethodLinkSpeciesSet();
  public long getMethodLinkSpeciesSetInternalId();
  public void setMethodLinkSpeciesSet(MethodLinkSpeciesSet newValue);
  public void setMethodLinkSpeciesSetInternalId(long newValue);
  
  public int getStrand(); 
  public void setStrand(int strand);
  public DnaFragment getDnaFragment();
  public void setDnaFragment(DnaFragment dnaFrag);
  public long getDnaFragmentId();
  public void setDnaFragmentId(long dnaFrag);
  public int getStart();
  public void setStart(int newValue);
  public int getEnd();
  public void setEnd(int newValue);

  public String getAlignedSequence();
  
  public void setAlignedSequence(String sequence);

  public String getOriginalSequence();
  
  public void setOriginalSequence(String sequence);
  
  public String  getDisplayID();
  
  public GenomicAlignBlock getGenomicAlignBlock();
  public void setGenomicAlignBlock(GenomicAlignBlock block);
  
  public long getGenomicAlignBlockInternalID();
  public void setGenomicAlignBlockInternalID(long block);
  
  public void reverseComplement();
  
}
