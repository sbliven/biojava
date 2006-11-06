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

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.ExternalDatabase;
import org.ensembl.datamodel.ExternalRef;
import org.ensembl.datamodel.impl.ExternalDatabaseImpl;
import org.ensembl.datamodel.impl.ExternalRefImpl;
import org.ensembl.driver.ExternalDatabaseAdaptor;
import org.ensembl.driver.ExternalRefAdaptor;

/**
 * Note that this test requires a DriverGroup called 
 * write_back.Test and this should be defined
 * in a registry ini file $HOME/.ensembl/unit_test.ini.
 */
public class ExternalRefWriteBackTest extends CoreBase {

  private static Logger logger =
    Logger.getLogger(ExternalRefWriteBackTest.class.getName());

  public static final void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public ExternalRefWriteBackTest(String name) throws Exception {
    super(name);
  }

  public static Test suite() {

    TestSuite s = new TestSuite();
    //s.addTest(new ExternalRefWriteBackTest("testSQL"));
    s.addTestSuite(ExternalRefWriteBackTest.class);
    return s;
  }

  protected void setUp() throws Exception {
    super.setUp();
    externalRefAdaptor = driver.getExternalRefAdaptor();
    externalDatabaseAdaptor = driver.getExternalDatabaseAdaptor();
    clearTables();
    Connection conn = driver.getConnection();
    Statement s = conn.createStatement();
    
    // external_db cannot be written to by the API, so need to "manually" populate it
    s.execute("INSERT INTO external_db VALUES(1, 'EMBL', 'dummy', 'KNOWN')");
    // set up xref etc     
    s.execute(
      "INSERT INTO xref (external_db_id, dbprimary_acc) VALUES(1, 'acc1')");
    s.execute(
      "INSERT INTO object_xref (ensembl_id, ensembl_object_type, xref_id) VALUES(1, 'Gene', 1)");
    s.execute("INSERT INTO go_xref VALUES(1, 'curated')");
    s.execute("INSERT INTO identity_xref (object_xref_id, query_identity,  target_identity) VALUES(1, 30, 40)");
    s.execute("INSERT INTO translation (translation_id, transcript_id, seq_start, start_exon_id, seq_end, end_exon_id) VALUES(3,1,1,1,10,2)");
    conn.close();
  }

  /**
   * 
   */
  private void clearTables() throws Exception {
    Connection conn = driver.getConnection();
    Statement s = conn.createStatement();

    s.execute("DELETE FROM external_db");
    s.execute("DELETE FROM xref");
    s.execute("DELETE FROM object_xref");
    s.execute("DELETE FROM go_xref");
    s.execute("DELETE FROM identity_xref");
    s.execute("DELETE FROM external_synonym");
    s.execute("DELETE FROM translation");
    conn.close();
  }

  protected void tearDown() throws Exception {
     clearTables();
    
  }

  static ExternalRef createExternalRefObjectGraph() throws Exception {

    ExternalRef externalRef = new ExternalRefImpl(null);
    externalRef.setDisplayID("dummy_Display_ID");
    externalRef.setPrimaryID("dummy_primary_id" + Math.random());
    externalRef.setVersion("dummy_Version_1");
    externalRef.setDescription("dummy_Description");

    List synonyms =
      Arrays.asList(
        new String[] {
          "dummy_synonym_1",
          "dummy_synonym_2",
          "dummy_synonym_3" });
    externalRef.setSynonyms(synonyms);

    ExternalDatabase externalDatabase = new ExternalDatabaseImpl();
    externalDatabase.setInternalID(1);
    externalDatabase.setName("EMBL");
    externalDatabase.setStatus("KNOWN");
    externalDatabase.setVersion("dummy_external_db_version_1");
    externalRef.setExternalDatabase(externalDatabase);

    return externalRef;
  }

  public void testStoreRetrieveDeleteExternalRef() throws Exception {

    //setupXrefData();

    ExternalRef externalRef = createExternalRefObjectGraph();

    long externalRefID2 = externalRefAdaptor.store(externalRef);
    assertTrue(
      "Invalid externalRef internal id:" + externalRefID2,
      externalRefID2 > 0);

    List externalRef2s = externalRefAdaptor.fetch(externalRefID2);
    assertTrue(
      "Failed to retrieve stored externalRef with internalID = "
        + externalRefID2,
      externalRef2s.size() > 0);
    ExternalRef externalRef2 = (ExternalRef) externalRef2s.get(0);
    assertNotNull("Failed to load ExternalRef from db", externalRef2);

    externalRefAdaptor.delete(externalRefID2);
    assertTrue(
      "Failed to delete Externalref.",
      externalRefAdaptor.fetch(externalRefID2).size() == 0);

    // clean up external db we had to insert into db
    //externalDatabaseAdaptor.delete( externalDatabase );

  }

  public void testObjectExternalRefLinksCanBeStored() throws Exception {

    ExternalRef externalRef = createExternalRefObjectGraph();

    long externalRefID2 = externalRefAdaptor.store(externalRef);
    assertTrue(
      "Invalid externalRef internal id:" + externalRefID2,
      externalRefID2 > 0);

    externalRefAdaptor.storeObjectExternalRefLink(3, ExternalRef.TRANSLATION, externalRefID2);
    
    List ers = externalRefAdaptor.fetch(3, ExternalRef.TRANSLATION);
    assertTrue(ers.size()>0);
    ExternalRef er = (ExternalRef) ers.get(0);
    assertEquals(externalRefID2, er.getInternalID());

  }

  private ExternalRefAdaptor externalRefAdaptor;
  private ExternalDatabaseAdaptor externalDatabaseAdaptor;

} // TranscriptWriteBackTest
