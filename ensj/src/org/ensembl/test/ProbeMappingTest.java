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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.ExternalDatabase;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.MiscFeature;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.impl.AttributeImpl;
import org.ensembl.datamodel.impl.ExonImpl;
import org.ensembl.datamodel.impl.ExternalDatabaseImpl;
import org.ensembl.datamodel.impl.MiscFeatureImpl;
import org.ensembl.datamodel.impl.TranscriptImpl;
import org.ensembl.probemapping.MappableTranscript;

/**
 * Tests the ProbeMapping package.
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class ProbeMappingTest extends TestCase {


  ExternalDatabase xDB = new ExternalDatabaseImpl();

  public ProbeMappingTest(String name) {
    super(name);
    xDB.setName("array1");
    xDB.setInternalID(3);
  }

  public void testProbedTranscript() throws ParseException {
    Transcript t = (Transcript) sampleTranscripts().get(0);
    int flank3 = 2000;
    MappableTranscript pt = new MappableTranscript(t, flank3);
    assertEquals(
      t.getCDNALocation().getLength() + flank3,
      pt.getLocation().getLength());
  }

  public void testDummyData() throws Exception {

//    ProbeMapper app =
//      new ProbeMapper();
//    app.importProbeFeatures(sampleProbeMiscFeatures(), xDB);
//
//    MicroArraySet maSet = app.getMicroArraySet();
//
//    MicroArray[] mas = maSet.getArrays();
//    assertTrue(mas.length == 1);
//
//    Composite[] composites = mas[0].getProbeSets();
//    assertTrue(composites.length == 1);
//
//    Probe[] composite = composites[0].getProbes();
//    assertTrue(composite.length == 2);
//
//    Probe p = composite[0];
//    assertNotNull(p);
//    Location loc = p.getLocations()[0];
//    assertNotNull(loc);
//
//    app.importTranscripts(sampleTranscripts());
//
//    MappableTranscript[] pts = app.getProbedTranscripts();
//    assertTrue(pts.length == 1);
  }


  public void testFilterProbeSetsByMode() throws Exception{
    
    // set up 3 arrays where the mode number of probes is 2.
    // One has 3 and should be filtered.
    
    
//    MicroArraySet mas = new MicroArraySet();
//    
//    //  probe set with 2 probes
//    mas.add(xDB, 2, 0.5, "composite1","p11","arr1:composite11:p1", new Location("chromosome:1:100-200"));
//    mas.add(xDB, 2, 0.5,  "composite1","p12","arr1:composite12:p1", new Location("chromosome:1:300-400"));
//    
//    //  probe set with 2 probes
//    mas.add(xDB, 2, 0.5,  "composite2","p21","arr1:composite21:p1", new Location("chromosome:1:500-600"));
//    mas.add(xDB, 2, 0.5,  "composite2","p22","arr1:composite22:p1", new Location("chromosome:1:700-800"));
//    
//    //  probe set with 3 probes
//    mas.add(xDB, 3, 0.5,  "composite3","p31","arr1:composite31:p1", new Location("chromosome:1:900-1000"));
//    mas.add(xDB, 3, 0.5,   "composite3","p32","arr1:composite32:p1", new Location("chromosome:1:1100-1200"));
//    mas.add(xDB, 3, 0.5,   "composite3","p33","arr1:composite33:p1", new Location("chromosome:1:1300-1400"));
//    
//    assertEquals(3,mas.getArrays()[0].getProbeSets().length);
    
  }
  
  
  public void testFilterProbeSetsByTranscriptCount() throws Exception {
    
//    MicroArraySet mas = new MicroArraySet();
//    
//    mas.add(xDB, 2, 0.5, "composite1","p11","arr1:composite11:p1", new Location("chromosome:1:100-200"));
//    mas.add(xDB, 2, 0.5, "composite1","p12","arr1:composite12:p1", new Location("chromosome:1:300-400"));
//
//    mas.add(xDB, 2, 0.5, "composite2","p21","arr1:composite21:p1", new Location("chromosome:1:500-600"));
//    mas.add(xDB, 2, 0.5,  "composite2","p22","arr1:composite22:p1", new Location("chromosome:1:700-800"));
//
//
//    Composite composite = mas.getArrays()[0].getProbeSets()[0];
//    Probe p = composite.getProbes()[0];
//    p.add(createMappableTranscript());
//    // add the same PT to two probes  
//    MappableTranscript pt = createMappableTranscript(); 
//    p.add(pt);
//    p = composite.getProbes()[1];
//    p.add(pt);
//    
//    assertEquals(2, composite.getTranscriptCount());
//    
//    //  second probe set
//    // manipulate second probe set
//    composite = mas.getArrays()[0].getProbeSets()[1];
//    p = composite.getProbes()[0];
//    p.add(createMappableTranscript());
//    p.add(createMappableTranscript());
//    // add 3rd PT, this should make the probe set exceed the threshold we will use for filtering
//    p = composite.getProbes()[1];
//    p.add(createMappableTranscript());
//    
//    assertEquals(3, composite.getTranscriptCount());
//    
//    // Remove all probe sets which have >2 transcripts (1 probe set matches that criteria)
//    assertEquals(2, mas.getArrays()[0].getProbeSets().length);
//    mas.filterProbeSetsByTranscriptCountThreshold(2);
//    assertEquals(1, mas.getArrays()[0].getProbeSets().length);
  }


  public void testFilterByTranscriptFrequency() throws Exception {

    // Create some transcripts that we will later add to probes.
    MappableTranscript pt0 = createMappableTranscript();
    MappableTranscript pt1 = createMappableTranscript();
    MappableTranscript pt2 = createMappableTranscript();
    MappableTranscript pt3 = createMappableTranscript();

    
    // Create a probeset with 4 probes
//    MicroArraySet mas = new MicroArraySet();
//    mas.add(xDB, 4, 0.5, "composite1","p11","arr1:composite1:p11", new Location("chromosome:1:100-200"));
//    mas.add(xDB, 4, 0.5, "composite1","p12","arr1:composite1:p12", new Location("chromosome:1:300-400"));
//    mas.add(xDB, 4, 0.5, "composite1","p13","arr1:composite1:p13", new Location("chromosome:1:500-600"));
//    mas.add(xDB, 4, 0.5, "composite1","p14","arr1:composite1:p14", new Location("chromosome:1:500-600"));
//
//
//    //  get the probes so we can add transcripts to them later
//    Composite composite = mas.getArrays()[0].getProbeSets()[0];
//    Probe p0= composite.getProbes()[0];
//    Probe p1 = composite.getProbes()[1];
//    Probe p2= composite.getProbes()[2];
//    // pt0 should NOT get filtered because hit 3
//    p0.add(pt0);
//    p1.add(pt0);
//    p2.add(pt0);
//    // pt1 and pt2 should NOT get filtered because hit 2 probe
//    p0.add(pt1);
//    p1.add(pt1);
//    p0.add(pt2);
//    p1.add(pt2);
//    //  pt3 should get filter because only hit 1 probe and min is 2
//    p0.add(pt3);
//    
//    
//    // check that all the transcripts are available in the probeset
//    assertEquals(4, composite.getTranscriptCount());
//    
//    
//    // disable this test because the mapping program is going to change
//    // anyway
//    // remove pt3 because it only hits one probe    
//    //assertEquals(3,composite.getTranscriptCount());
    
  }

  private MappableTranscript createMappableTranscript() throws Exception{
    Transcript t = (Transcript) sampleTranscripts().get(0);
    return new MappableTranscript(t, 2000);
  }

  private List sampleTranscripts() throws ParseException {
    
    List es = new ArrayList();
    Exon e = new ExonImpl(1, new Location("chromosome:1:50-150")); 
    es.add(e);
    
    Transcript tt = new TranscriptImpl();
    tt.setLocation(e.getLocation());
    tt.setExons(es);
    
    List l = new ArrayList();
    l.add(tt);
    
    return l;
  }



  /**
   * @return
   */
  private List sampleProbeMiscFeatures() throws Exception {
    List l = new ArrayList();

    l.add(
      createMiscFeature(
        new Location("chromosome:1:100-125"),
        "probeName",
        "Probe name",
        "the name of the probe",
        "HG-Focus:215193_x_at:332:117;"));

    l.add(
      createMiscFeature(
        new Location("chromosome:1:11050-11075"),
        "probeName",
        "Probe name",
        "the name of the probe",
        "HG-Focus:215193_x_at:332:118;"));

    return l;
  }

  /**
   * @param location
   * @param string
   * @param string2
   * @param string3
   * @param string4
   * @return
   */
  private MiscFeature createMiscFeature(
    Location location,
    String code,
    String name,
    String description,
    String value) {
    MiscFeature mf;

    mf = new MiscFeatureImpl();
    mf.setLocation(location);
    mf.add(new AttributeImpl(code, name, description, value));
    return mf;
  }

}
