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
package org.ensembl.datamodel.impl;
import org.ensembl.datamodel.ExternalDatabase;
import org.ensembl.driver.CoreDriver;

public class ExternalDatabaseImpl 
  extends PersistentImpl implements ExternalDatabase 
{

  /**
   * Used by the (de)serialization system to determine if the data 
   * in a serialized instance is compatible with this class.
   *
   * It's presence allows for compatible serialized objects to be loaded when
   * the class is compatible with the serialized instance, even if:
   *
   * <ul>
   * <li> the compiler used to compile the "serializing" version of the class
   * differs from the one used to compile the "deserialising" version of the
   * class.</li>
   *
   * <li> the methods of the class changes but the attributes remain the same.</li>
   * </ul>
   *
   * Maintainers must change this value if and only if the new version of
   * this class is not compatible with old versions. e.g. attributes
   * change. See Sun docs for <a
   * href="http://java.sun.com/j2se/1.4.2/docs/guide/serialization/">
   * details. </a>
   *
   */
  private static final long serialVersionUID = 1L;



  public ExternalDatabaseImpl() {
    super();
  }


  public ExternalDatabaseImpl( CoreDriver driver) {
    super( driver );
  }

  private String name;

  public String getName(){ return name; }

  private String version;

  public String getVersion(){ return version; }

  private String status;

  public String getStatus(){ return status; }

  public void setName(String name) {
    this.name = name;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  private int priority;

  public int getPriority(){ return priority; }

  public void   setPriority(int priority){ this.priority = priority; }

  
  /**
   * Determines if external database is known. Database is known if  status
   * is  "KNOWN" or "KNOWNXREF".
   *
   * @return true if known, otherwise false.
   */
  public boolean isKnown() {
    if ( status==null ) return false;
    else return ( "KNOWN".equals(status) || "KNOWNXREF".equals(status) ) ;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("[");
    buf.append(super.toString()).append(", ");
    buf.append("name=").append(name).append(", ");
    buf.append("version=").append(version).append(", ");
    buf.append("status=").append(status).append(", ");
    buf.append("priority=").append(priority);
    buf.append("]");
    return buf.toString();
  }

}
