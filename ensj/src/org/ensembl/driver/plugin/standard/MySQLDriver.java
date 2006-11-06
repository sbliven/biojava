/*
	Copyright (C) 2005 EBI, GRL

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

package org.ensembl.driver.plugin.standard;

import java.util.Properties;

import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.impl.CoreDriverImpl;

/**
 * A depreacted driver for ensembl core databases.
 * 
 * The class is included in ensj so that legacy user code which requires it will
 * continue to work unmodified.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 * @deprecated Deprecated in version 29.2, use org.ensembl.driver.impl.CoreDriverImpl instead.
 */
public class MySQLDriver extends CoreDriverImpl {

  /**
   * @see org.ensembl.driver.impl.CoreDriverImpl#CoreDriverImpl()
   */
  public MySQLDriver() {
    super();
  }


  /**
   * @see org.ensembl.driver.impl.CoreDriverImpl#CoreDriverImpl(Properties)
   */
  public MySQLDriver(Properties configuration) throws AdaptorException {
    super(configuration);
  }

  /**
   * @see org.ensembl.driver.impl.CoreDriverImpl#CoreDriverImpl(String, String, String)
   */
  public MySQLDriver(String host, String database, String user)
      throws AdaptorException {
    super(host, database, user);
  }

  /**
   * @see org.ensembl.driver.impl.CoreDriverImpl#CoreDriverImpl(String, String, String, String)
   */
  public MySQLDriver(String host, String database, String user, String password)
      throws AdaptorException {
    super(host, database, user, password);
  }

  /**
   * @see org.ensembl.driver.impl.CoreDriverImpl#CoreDriverImpl(String, String, String, String, String)
   */
  public MySQLDriver(String host, String database, String user,
      String password, String port) throws AdaptorException {
    super(host, database, user, password, port);
  }

  /**
   * @see org.ensembl.driver.impl.CoreDriverImpl#CoreDriverImpl(String, String, String, boolean)
   */
  public MySQLDriver(String host, String database, String user,
      boolean databaseIsPrefix) throws AdaptorException {
    super(host, database, user, databaseIsPrefix);
  }

  /**
   * @see org.ensembl.driver.impl.CoreDriverImpl#CoreDriverImpl(String, String, String, String, boolean)
   */
  public MySQLDriver(String host, String database, String user,
      String password, boolean databaseIsPrefix) throws AdaptorException {
    super(host, database, user, password, databaseIsPrefix);
  }

  /**
   * @see org.ensembl.driver.impl.CoreDriverImpl#CoreDriverImpl(String, String, String, String, String, boolean)
   */
  public MySQLDriver(String host, String database, String user,
      String password, String port, boolean databaseIsPrefix)
      throws AdaptorException {
    super(host, database, user, password, port, databaseIsPrefix);
  }

}
