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
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */
package org.ensembl.util;


/**
 * Program and function to report ensj build version.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 * @version $Revision$
 */
public class Version{

  
  /**
   * Prints ensj version.  
   * @param args ignored.
   * @see #buildVersion()
   */
  public static void main(String[] args) {

    System.out.println("Ensj version  "
                       + buildVersion());
  }
  
  /**
   * Returns ensj version number if running inside jar, otherwise null.
   * 
   * Version number is the "Implementation-Version" of the "org.ensembl"
   * package in the jar manifest.
   * 
   * @return ensj version number if available, otherwise null.
   */
  public static String buildVersion() {

  	String v = null;
    Package p = org.ensembl.Example.class.getPackage();
  	if (p!=null) 
      v = p.getImplementationVersion();
    return v;
  		
  }
} // Version
