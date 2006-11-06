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
 * I represent a single species which is part of the compara- analyses.
**/
public interface Member extends Persistent{
  public String getStableId();
  public void setStableId(String _stableId);
  public int getVersion();
  public void setVersion(int _version);
  public String getSourceName();
  public void setSourceName(String _sourceName);
  public long getTaxonId();
  public void setTaxonId(long _taxonId);
  public long getGenomeDbId();
  public void setGenomeDbId(long _genomeDbId);
  public long getSequenceId();
  public void setSequenceId(long _sequenceId);
  public long getGeneMemberId();
  public void setGeneMemberId(long _geneMemberId);
  public String getDescription();
  public void setDescription(String _description);
  public String getChrName();
  public void setChrName(String _chrName);
  public int getChrStart();
  public void setChrStart(int _chrStart);
  public int getChrEnd();
  public void setChrEnd(int _chrEnd);
  public int getChrStrand();
  public void setChrStrand(int _chrStrand);  
}
