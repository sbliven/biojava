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
package org.ensembl.datamodel;

/**
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 * @version $Revision$
 */

public interface AssemblyElement {

    public void setChromosomeName(String chrName);
    public void setChromosomeStart(int chrStart);
    public void setChromosomeEnd(int chrEnd);
    public void setCloneFragmentInternalID(long id);
    public void setCloneFragmentStart(int cloneFragStart);
    public void setCloneFragmentEnd(int cloneFragEnd);
    public void setCloneFragmentOri(int cloneFragOri);
    public void setType(String type);

    public String getChromosomeName();
    public int getChromosomeStart();
    public int getChromosomeEnd();
    public long getCloneFragmentInternalID();
    public int getCloneFragmentStart();
    public int getCloneFragmentEnd();
    public int getCloneFragmentOri();
    public String getType();

}// AssemblyElement
