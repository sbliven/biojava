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
import java.util.Set;

import org.ensembl.datamodel.Persistent;

/**
 * I represent a type of analysis which could be applied between 
 * the elements of a set of genomes and the actual set which had it
 * applied.
**/
public interface MethodLinkSpeciesSet extends Persistent{
  public Set getSpeciesSet();
  public void setSpeciesSet(Set speciesSet);
  public MethodLink getMethodLink();
  public void setMethodLink(MethodLink link);
  public Set getSpeciesSetdbIDs();
  public void setSpeciesSetDBIDs(Set speciesSetdbIDs);
}
