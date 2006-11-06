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
package org.ensembl.compara.test;

import java.util.logging.Logger;

import org.ensembl.compara.driver.ComparaDriver;
import org.ensembl.test.Base;

/**
 * Base for compara tests configures the logging system and loads the
 * registry driver in it's constructor.
 * 
 * The registry is configured from a config file.
 * The system uses $HOME/.ensembl/compara_unit_test.pl if it exists otherwise
 * it uses the default config file resource/data/compara_unit_test.pl.
 */
public abstract class ComparaBase extends Base {

  protected ComparaDriver comparaDriver = null;
  
  private static Logger logger = Logger.getLogger( ComparaBase.class.getName() );  
  
  public ComparaBase(String name) throws Exception {
    super(name);
  }

  protected void setUp() throws Exception {
    super.setUp();
    comparaDriver = registry.getGroup("compara").getComparaDriver();
  }

}

