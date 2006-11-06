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

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Persistent;

/**
 * I am a part of the compara- analysis. I store a fragment
 * of the chromsome for a particular species.
**/
public interface DnaFragment extends Persistent{
  public GenomeDB getGenomeDB();
  public void setGenomeDB(GenomeDB genomeDB);
  public long getGenomeDbInternalId();
  public void setGenomeDbInternalId(long id);
  public String getCoordSystemName();
  public void setCoordSystemName(String newValue);
  public String getName();
  public void setName(String newValue);
  public void setLength(int location);
  public int getLength();
  public void setLocation(Location location);
  public Location getLocation();
}
