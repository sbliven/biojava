/*
Copyright (C) 2004 EBI, GRL

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

package org.ensembl.util;

import java.util.Comparator;

import org.ensembl.datamodel.Persistent;

public class AscendingInternalIDComparator implements Comparator {

    public int compare(Object o1, Object o2) {
      
        long id1 = ((Persistent)o1).getInternalID();
        long id2 = ((Persistent)o2).getInternalID();

        if (id1 > id2) {
            return 1;
        } else if (id1 < id2) {
            return -1;
        } else {
            return 0;
        }
        
    }

    public boolean equals(Object o1, Object o2) {

        return compare(o1, o2) == 0;
    }

} // AscendingInternalIDComparator
