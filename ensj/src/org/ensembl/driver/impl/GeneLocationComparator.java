/*
  Copyright (C) 2002 EBI, GRL

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

package org.ensembl.driver.impl;

import java.util.Comparator;

import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Location;

/**
 * Used to sort Genes by there location. 
 */
public class GeneLocationComparator implements Comparator {

  public int compare(Object o1, Object o2) {
    Gene g1 = (Gene)o1;
    Exon e1 = (Exon)g1.getExons().get(0);
    Location l1 = (Location)e1.getLocation();
    int start1 = l1.getStart();

    Gene g2 = (Gene)o2;
    Exon e2 = (Exon)g2.getExons().get(0);
    Location l2 = (Location)e2.getLocation();
    int start2 = l2.getStart();

    if ( start1==start2 ) return 0;
    if ( start1<start2 ) return -1;
    else return 1;
  }
}
