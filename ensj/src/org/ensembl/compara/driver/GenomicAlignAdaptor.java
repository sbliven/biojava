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
package org.ensembl.compara.driver;

import java.util.List;

import org.ensembl.compara.datamodel.DnaFragment;
import org.ensembl.compara.datamodel.GenomeDB;
import org.ensembl.compara.datamodel.GenomicAlign;
import org.ensembl.compara.datamodel.GenomicAlignBlock;
import org.ensembl.driver.Adaptor;
import org.ensembl.driver.AdaptorException;

/**
 * I am a part of the compara analysis
**/
public interface GenomicAlignAdaptor extends Adaptor{
  static final String TYPE = "genomic_align";

  public abstract List fetch() throws AdaptorException;
  
  public GenomicAlign fetch(long internalId) throws AdaptorException;
  
  public abstract List fetch(
    DnaFragment dnaFragment, //implicit source species
    GenomeDB targetGenome, 
    int start,
    int end,
    String methodLinkType
  ) throws AdaptorException;
  
  public abstract List fetch(GenomicAlignBlock block) throws AdaptorException;

  public List fetchByBlocks(List blocks) throws AdaptorException;
  
  public int store(GenomicAlign genomicAlign) throws AdaptorException;
}//end GenomicAlignAdaptor
