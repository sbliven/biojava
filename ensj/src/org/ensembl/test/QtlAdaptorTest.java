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

import org.ensembl.datamodel.Qtl;
import org.ensembl.datamodel.QtlFeature;
import org.ensembl.datamodel.QtlSynonym;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.QtlAdaptor;

/**
 * Tests the QTL adaptor using a rat db, rather the usual human db, because the
 * human db lacks qtl data.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 * 
 */
public class QtlAdaptorTest extends CoreBase {

	private QtlAdaptor adaptor;

	public QtlAdaptorTest(String name) throws AdaptorException {
		super(name);
	}

  protected void setUp() throws Exception {
    super.setUp();
    // rat db has QTLs
		adaptor = registry.getGroup("rat").getCoreDriver().getQtlAdaptor();
  }

  static void checkQtl(Qtl qtl, boolean expectAllChildDataAvailable) {
		assertNotNull(qtl);
		QtlSynonym synonym = (QtlSynonym) qtl.getSynoyms().get(0);
		assertNotNull(synonym);
		assertNotNull(synonym.getSourceDatabaseName());
		assertNotNull(synonym.getSourceID());
		assertNotNull(qtl.getTrait());
		// peakMarker can be null so we can't check here
		if (expectAllChildDataAvailable) {
			assertNotNull(qtl.getPeakMarker());
			assertNotNull(qtl.getFlankMarker1());
			assertNotNull(qtl.getFlankMarker2());
			QtlFeature f = (QtlFeature) qtl.getQtlFeatures().get(0);
			assertNotNull(f.getLocation().getSeqRegionName());
		}
	}

	public void testFetchByID() throws Exception {

		// pick one with flanks, peak values and qtl_features
		Qtl qtl = adaptor.fetch(14);
		checkQtl(qtl,true);

	}

	public void testFetchByTrait() throws Exception {

		// Note: must choose a trait which is both a QTL and mapped to the
		// genome
		// otherwise the check(..) will fail because it enseures the qtl has qtl
		// features.
		// select * from qtl_feature qf, qtl q where qf.qtl_id=q.qtl_id limit
		// 10;
		List qtls = adaptor.fetchByTrait("Body weight QTL 17");
		assertTrue(qtls.size() > 0);
		checkQtl((Qtl) qtls.get(0), true);

	}

	public void testFetchBySourceDatabase() throws Exception {

		List qtls = adaptor.fetchBySourceDatabase("ratmap");
		assertTrue(qtls.size() > 1);
		checkQtl((Qtl) qtls.get(0),false);

	}

	public void testFetchBySourceDatabase2() throws Exception {

		List qtls = adaptor.fetchBySourceDatabase("ratmap", "44536");
		assertTrue(qtls.size() > 0);
		checkQtl((Qtl) qtls.get(0),false);

	}

}
