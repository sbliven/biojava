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

package org.ensembl.test;

import java.util.List;

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Qtl;
import org.ensembl.datamodel.QtlFeature;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;

/**
 * Tests the QTL adaptor using a rat db, rather the usual human db,
 * because the human db lacks qtl data.
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class QtlFeatureAdaptorTest extends Base {

  private CoreDriver qtlDriver = null;

  public QtlFeatureAdaptorTest(String name) throws AdaptorException {
    super(name);
	}

  protected void setUp() throws Exception {
    super.setUp();
    //  rat db has QTLs
		qtlDriver = registry.getGroup("rat").getCoreDriver();
  }


  static void check(QtlFeature f) {
    assertNotNull(f);
    assertNotNull("QTL feature lacks analysis: "+f,
        f.getAnalysis());
    assertNotNull(f.getQtl());
    QtlAdaptorTest.checkQtl(f.getQtl(), true);
  }
  
  
  public void testFetchByLocation() throws Exception {
  
    List qtls = qtlDriver.getQtlFeatureAdaptor().fetch(new Location("chromosome:1"));
    assertTrue(qtls.size() > 1);
    check((QtlFeature) qtls.get(0));
  
  }

  public void testFetchByQtl() throws Exception {
  
    // choose a QTL with no NULL fields and qtl_features
    Qtl qtl = qtlDriver.getQtlAdaptor().fetch(90);
    assertNotNull(qtl);
    List fs = qtlDriver.getQtlFeatureAdaptor().fetch(qtl);
    assertTrue(fs.size() > 0);
    check((QtlFeature) fs.get(0));
  
  }


  
}
