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

package org.ensembl.registry.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.ensembl.compara.driver.ComparaDriver;
import org.ensembl.datamodel.Location;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.ServerDriverFactory;
import org.ensembl.registry.DriverGroup;
import org.ensembl.registry.Registry;
import org.ensembl.registry.RegistryLoaderIni;
import org.ensembl.registry.RegistryLoaderJython;
import org.ensembl.variation.driver.VariationDriver;

/**
 * Test the Registry class.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class RegistryTest extends TestCase {

  //  public static TestSuite suite() {
  //    TestSuite s = new TestSuite();
  //    s.addTest(new RegistryTest("testJythonConfig"));
  //// s.addTest(new RegistryTest("testCustomGroups"));
  //// s.addTest(new RegistryTest("testDefaultRegistry"));
  //// s.addTest(new RegistryTest("testParameterPassThrough"));
  //    return s;
  //  }

  public RegistryTest(String arg0) {
    super(arg0);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(RegistryTest.class);
  }

  public void testDefaultRegistry() throws Exception {

    Registry r = Registry.createDefaultRegistry();

    checkRegistry(r, true);

    assertEquals(r.getGroup("mus_musculus"), r.getGroup("mouse"));

    checkDriverGroup(r.getGroup("mouse"),
        "group=mouse, config=" + r.toString(), true, true, false);

    assertEquals(r.getGroup("ensembl_compara"), r.getGroup("compara"));
    checkDriverGroup(r.getGroup("compara"), r.toString(), false, false, true);

  }

  public void testCustomGroups() throws Exception {

    // Ini string for configurating the Registry
    StringBuffer buf = new StringBuffer();

    buf.append("[rat]\n");
    buf.append("core.host ensembldb.ensembl.org\n");
    buf.append("core.user anonymous\n");
    buf.append("core.database_prefix rattus_norvegicus_core\n");
    buf.append("\n");

    buf.append("[some_dataset]\n");
    buf.append("host ensembldb.ensembl.org\n");
    buf.append("# ignore this comment\n"); // ensure comments ignored
    buf.append("user anonymous\n");
    buf.append("\n"); // leave a blank line to ensure that's ignored
    buf.append("database_prefix homo_sapiens\n");
    buf.append("variation.database_prefix DISABLED_BECUASE_DB_BUG\n");
    buf.append("\n");
    buf.append("aliases bob\n");
    buf.append("\n");

    // try setting default values in the middle of the config, values should
    // be used in remaining groups.
    buf.append("[default]\n");
    buf.append("host ensembldb.ensembl.org\n");
    buf.append("user anonymous\n");

    buf.append("[funky_compara]\n");
    buf.append("type compara\n");
    buf.append("aliases compara\n");
    buf.append("database_prefix ensembl_compara\n");
    buf.append("\n");

    buf.append("[broken_compara_expect_warning]\n");
    buf.append("aliases broken\n");
    buf.append("database_prefix a_made_up_database\n");

    String config = buf.toString();

    Registry r = new Registry(new RegistryLoaderIni(config.getBytes(),
        "testCustomGroupString"));

    checkRegistry(r, true);

    checkDriverGroup(r.getGroup("rat"), config, true, false, false);

    assertEquals(r.getGroup("some_dataset"), r.getGroup("bob"));
    checkDriverGroup(r.getGroup("some_dataset"), config, true, false, false);

    assertEquals(r.getGroup("compara"), r.getGroup("funky_compara"));
    checkDriverGroup(r.getGroup("compara"), config, false, false, true);

    assertEquals(r.getGroup("broken"), r
        .getGroup("broken_compara_expect_warning"));
    assertNull(r.getGroup("broken"));

    r.closeAllConnections();

  }

  public void testParameterPassThrough() throws Exception {

    String config = "[homo_sapiens]\n" + "type species\n"
        + "host ensembldb.ensembl.org\n" + "user anonymous\n"
        + "someCustomParameter bobbyTheParameter\n";

    Registry r = null;

    try {
      r = new Registry(new RegistryLoaderIni(config.getBytes(),
          "parameter pass through test"));

      DriverGroup dg = r.getGroup("homo_sapiens");
      assertNotNull(dg);

      Properties cc = dg.getCoreConfig();
      assertNotNull(cc);
      assertEquals("Expected these values to be passed through: " + config
          + " but got:" + cc, "bobbyTheParameter", cc
          .getProperty("someCustomParameter"));

      CoreDriver cd = dg.getCoreDriver();
      assertNotNull(cd);
      assertEquals("bobbyTheParameter", cd.getConfiguration().getProperty(
          "someCustomParameter"));

      VariationDriver vd = dg.getVariationDriver();
      assertNotNull(vd);
      assertEquals("bobbyTheParameter", vd.getConfiguration().getProperty(
          "someCustomParameter"));
    } finally {
      r.closeAllConnections();
    }
  }

  public void testStandardConfig() throws Exception {

    Registry r = new Registry(new RegistryLoaderIni(new String[] {
        "resources/data/unit_test.ini" }, new ServerDriverFactory(), true));
    DriverGroup dg = r.getGroup("homo_sapiens");
    assertNotNull(dg);
    r.closeAllConnections();

  }

  private void checkRegistry(Registry r, boolean expectAliases)
      throws Exception {

    List mds = r.getGroups();
    assertNotNull(mds);
    assertTrue(mds.size() > 0);

    String[] names = r.getGroupNames();
    assertNotNull(names);
    assertTrue(names.length > 0);

  }

  public void checkDriverGroup(DriverGroup dg, Object config,
      boolean expectCoreDriver, boolean expectVariationDriver,
      boolean expectComparaDriver) throws Exception {

    try {
      assertNotNull("DriverGroup is null so we can't check it.", dg);

      if (expectCoreDriver) {

        assertNotNull("No core driver for config: " + config, dg
            .getCoreDriver());
        assertSame("Core driver isn't being cached", dg.getCoreDriver(), dg
            .getCoreDriver());
        assertNotNull("No genes available", dg.getCoreDriver().getGeneAdaptor()
            .fetchIterator(new Location("chromosome:1")).next());
        assertSame("Core driver isn't being cached", dg.getCoreDriver(), dg
            .getCoreDriver());
        assertNotNull("No genes available", dg.getCoreDriver().getGeneAdaptor()
            .fetchIterator(new Location("chromosome:1")).next());

      } else {

        if (dg.getCoreConfig() != null)
          // expect a dodgy config to cause an Adaptor exception.
          try {
            CoreDriver cd = dg.getCoreDriver();
            fail("Should have failed to create core driver: "
                + dg.getCoreConfig());
          } catch (AdaptorException e) {
            // expected behaviour
          }
        else
          assertNull(dg.getCoreConfig());

      }

      if (expectVariationDriver) {

        assertNotNull("No variation driver for config: " + config, dg
            .getVariationDriver());
        assertSame("Variation driver isn't being cached", dg
            .getVariationDriver(), dg.getVariationDriver());
        assertNotNull("No variation features", dg.getVariationDriver()
            .getVariationFeatureAdaptor().fetch(
                new Location("chromosome:1:1-10000")));

        assertNull("homo sapiens doesn't have a compara driver.", dg
            .getComparaDriver());

        // Amongst other things this tests that the meta_coord tables
        // are loaded from both dbs.
        // choose a location that contains variation features on all
        // species we test for variation support.
        assertNotNull("No variation features", dg.getVariationDriver()
            .getVariationFeatureAdaptor().fetchIterator(
                new Location("chromosome:19:4.8m-5m")).next());

      } else {

        if (dg.getVariationConfig() != null)
          // expect a dodgy config to cause an Adaptor exception.
          try {
            VariationDriver cd = dg.getVariationDriver();
            fail("Should have failed to create VariationDriver: "
                + dg.getVariationConfig());
          } catch (AdaptorException e) {
            // expected behaviour
          }
        else
          assertNull(dg.getVariationConfig());

      }

      if (expectComparaDriver) {

        assertNotNull("No compara driver for config: " + config, dg
            .getComparaDriver());

        assertNotNull(dg.getComparaDriver().getDnaDnaAlignFeatureAdaptor()
            .fetch("Homo sapiens",
                new Location("chromosome:1:1000000-1200000:0"), "Mus musculus",
                "BLASTZ_NET").iterator().next());

      } else {

        if (dg.getComparaConfig() != null)
          // expect a dodgy config to cause an Adaptor exception.
          try {
            ComparaDriver cd = dg.getComparaDriver();
            fail("Should have failed to create core driver: "
                + dg.getComparaConfig());
          } catch (AdaptorException e) {
            // expected behaviour
          }
        else
          assertNull(dg.getComparaConfig());

      }
    } finally {

      if (dg != null)
        dg.closeAllConnections();

    }

  }

  public void testJythonConfig() throws Exception {
    Registry r = new Registry(new RegistryLoaderJython(
        "resources/data/registry_jython.py"));

    System.out.println("Jython loaded registry:\n" + r);

  }

  private InputStream string2InputStream(String s) {
    return new ByteArrayInputStream(s.getBytes());
  }

}