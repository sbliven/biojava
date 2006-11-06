/*
    Copyright (C) 2001 EBI, GRL

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

import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.impl.CoreDriverImpl;


/**
 * Convenience class that prints the databases available on
 * ensembldb.ensembl.org to stdout. 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *  */
public class EnsemblDBLister {

  public static void main(String[] args) throws AdaptorException {
    String[] dbs =
      new CoreDriverImpl("ensembldb.ensembl.org", null, "anonymous")
        .fetchDatabaseNames();

    for (int i = 0; i < dbs.length; ++i)
      System.out.println(dbs[i]);
  }
}
