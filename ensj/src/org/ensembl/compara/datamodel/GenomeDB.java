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
public interface GenomeDB extends Persistent{
  public String getAssembly();
  public void setAssembly(String newValue);
  public String getName();
  public void setName(String name);
  public int getTaxonId();
  public void setTaxonId(int taxonId);  
  public boolean isDefaultAssembly();
  public void setDefaultAssembly(boolean defaultAssembly);
  public String getGeneBuild();
  public void setGeneBuild(String geneBuild);
  public String getLocator();
  public void setLocator(String locator);
}
