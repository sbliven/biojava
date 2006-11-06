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

import org.ensembl.compara.datamodel.GenomeDB;
import org.ensembl.driver.Adaptor;
import org.ensembl.driver.AdaptorException;

/**
 * I am a part of the compara analysis
**/
public interface GenomeDBAdaptor extends Adaptor{
  static final String TYPE = "genome_db";

  public abstract List fetch() throws AdaptorException;
  
  public abstract GenomeDB fetch(String speciesName, String assembly) throws AdaptorException;
  
  public abstract GenomeDB fetch(long internalId) throws AdaptorException;
  
  public abstract boolean firstArgumentIsKnownConsensusAndSecondIsKnowQuery(GenomeDB consensusDB, GenomeDB queryDB);
  
  public int store(GenomeDB genomeDB) throws AdaptorException;
  
  public GenomeDB fetch(String speciesName) throws AdaptorException;
}//end GenomeDBAdaptor