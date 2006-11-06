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

package org.ensembl.variation.test;

import java.util.Collections;
import java.util.List;

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.TranscriptAlleleHelper;
import org.ensembl.util.StringUtil;

/**
 * Tests the support for applying AlleleFeatures to Transcripts via
 * the TranscriptAlleleHelper class. 
 * 
 * NOTE: the Helper class is incomplete and should not be used. This 
 * test only tests work in progress. 
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 * 
 * TODO test transcript without translation
 * TODO test an allele that crosses exon boundary
 * TODO test upstream allele
 * TODO test downstream allele
 * TODO test exonic allele
 * TODO test intro allele
 * TODO test 5primeUTR
 * TODO test 3primeUTR
 * TODO test multiple alleleFeatures
 * TODO test all alleleFeatures that cross transcript
 * */
public class TranscriptAlleleHelperTest extends VariationBase {

  public TranscriptAlleleHelperTest(String name) throws Exception {
  	super(name);
  }

  
  public void testSimple() throws Exception {
    
    long id = 34; // one exon with translation 
    Transcript t = vdriver.getCoreDriver().getTranscriptAdaptor().fetch(id);
    assertNotNull(t);
    
    TranscriptAlleleHelper tw = new TranscriptAlleleHelper(t);
    //List alleleConsequences = vdriver.getAlleleConsequenceAdaptor().fetch(t);
    Location loc = t.getLocation().transform(-300,+300);
    List alleleFeatures = vdriver.getAlleleFeatureAdaptor().fetch(loc);
    
    List alleleConsequences = tw.toAlleleConsequences(alleleFeatures);
    Collections.sort(alleleConsequences);
    assertNotNull(alleleConsequences);
    assertTrue(alleleConsequences.size()>0);
    //System.out.println(StringUtil.toString(alleleConsequences,"\n"));
    
    //System.out.println(tw.toString("\n"));
    // TODO test the returned values more closely
    
  }
  
}
