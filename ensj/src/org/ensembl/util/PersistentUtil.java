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
package org.ensembl.util;

import java.util.List;

import org.ensembl.datamodel.Persistent;

public class PersistentUtil {


  /**
   * @param persistentItems List of zero or more Persistent objects.
   * @return array containing internalIDs from list of Persistent items,
   *empty array of length 0 if _persistentItems_==null.
   */
  public static final long[] listToInternalIDArray(List persistentItems) {

    if ( persistentItems==null ) return new long[0];

    long[] ids = new long[ persistentItems.size() ];

    for(int i=0; i< ids.length; ++i)
      ids[ i ] = ((Persistent)persistentItems.get(i)).getInternalID();
    
    return ids;
  }



  /**
   * Resets the internalID for all items to 0.
   * @param persistentItems List of Persistent objects, or null.
   */
  public final static void resetInternalIDs(List persistentItems) {
    if ( persistentItems==null ) return;
    final int nItems = persistentItems.size();
    for(int i=0; i<nItems; ++i)
      ((Persistent)persistentItems.get(i)).setInternalID(0);
  }

}
