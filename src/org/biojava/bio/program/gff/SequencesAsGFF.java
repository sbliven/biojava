/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.bio.program.gff;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * Turns a sequence database into a GFF event stream.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */
public class SequencesAsGFF {
  private FeatureFilter filter = FeatureFilter.all;
  private boolean recurse = false;
  
  public FeatureFilter getFeatureFilter() {
    return filter;
  }
  
  public void setFeatureFilter(FeatureFilter filter) {
    this.filter = filter;
  }
  
  public boolean getRecurse() {
    return recurse;
  }
  
  public void setRecurse(boolean recurse) {
    this.recurse = recurse;
  }
  
  protected void doProcessSequence(Sequence seq,
                                   GFFDocumentHandler handler,
                                   String id) 
  throws SeqException, BioException {
    Iterator fi = seq.filter(getFeatureFilter(), getRecurse()).features();
      
    while(fi.hasNext()) {
	    Feature f = (Feature) fi.next();
	    SimpleGFFRecord record = new SimpleGFFRecord();
	    record.setSeqName(id);
	    record.setSource(f.getSource());
	    record.setFeature(f.getType());
	    Location loc = f.getLocation();
	    record.setStart(loc.getMin());
	    record.setEnd(loc.getMax());
	    record.setScore(GFFRecord.NO_SCORE);
	    record.setStrand(GFFRecord.NO_STRAND);
	    if(f instanceof StrandedFeature) {
        StrandedFeature sf = (StrandedFeature) f;
        if(sf.getStrand() == StrandedFeature.POSITIVE) {
          record.setStrand(GFFRecord.POSITIVE_STRAND);
        } else if(sf.getStrand() == StrandedFeature.NEGATIVE) {
          record.setStrand(GFFRecord.NEGATIVE_STRAND);
        }
	    }
	    record.setFrame(GFFRecord.NO_FRAME);
      Map fMap = f.getAnnotation().asMap();
      for(Iterator ki = fMap.keySet().iterator(); ki.hasNext(); ) {
        Object key = ki.next();
        Object value = fMap.get(key);
        String keyS = key.toString();
        List valueList;
        if(value instanceof List) {
          valueList = (List) value;
        } else {
          //valueList = Collections.singletonList(value); 1.3?
          valueList = new ArrayList();
          valueList.add(value);
        }
        for(int i = 0; i < valueList.size(); i++) {
          Object o = valueList.get(i);
          valueList.set(i, o.toString());
        }
        fMap.put(keyS, valueList);
      }
	    record.setGroupAttributes(fMap);
	    record.setComment(null);
        
	    handler.recordLine(record);
    }
  }

  public void processSequence(Sequence seq, GFFDocumentHandler handler) 
  throws SeqException, BioException {
    handler.startDocument();
    doProcessSequence(seq, handler, seq.getName());
    handler.endDocument();
  }

  public void processDB(SequenceDB seqDB, GFFDocumentHandler handler)
  throws SeqException, BioException {
    handler.startDocument();
    for(Iterator i = seqDB.ids().iterator(); i.hasNext(); ) {
      String id = (String) i.next();
      Sequence seq = seqDB.getSequence(id);
      doProcessSequence(seq, handler, id);
    }
    handler.endDocument();
  }
}
