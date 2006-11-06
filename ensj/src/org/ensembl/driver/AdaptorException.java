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
package org.ensembl.driver;
import java.rmi.RemoteException;

import org.ensembl.util.Version;

/**
 * Thrown when a problem occurs running code in ensj.
 */
public class AdaptorException extends RemoteException {

  /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

public AdaptorException() {
    super();
  }

  public AdaptorException(String message) {
    super(message + buildLabel());
  }

  public AdaptorException(String message, Exception parentException) {
    super(message + buildLabel(), parentException);
  }

  public AdaptorException(Exception parentException) {
    super(buildLabel(),parentException);
  }
  
  private static String buildLabel() {
  	return " ["+Version.buildVersion()+"]";
  }
}
