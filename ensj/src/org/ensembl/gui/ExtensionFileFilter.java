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
package org.ensembl.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Simple file extension filter. Can be used to filter files shown in
 * JFileChooser.
 *
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 * @version $Revision$ 
 */

public class ExtensionFileFilter extends FileFilter {

  private String extension;
  private String description;

  public ExtensionFileFilter (String extension, String description){
    this.extension = extension;
    this.description = description;
  }


    public boolean accept(File f) {
      
      if ( f.isDirectory() ) return true;
      
      String path = f.getName().toLowerCase();
      if ( path.endsWith(extension) ) return true;
      
      return false;
    }
    
    public String getDescription() {
      return description;
    }


}// ExtensionFileFilter
