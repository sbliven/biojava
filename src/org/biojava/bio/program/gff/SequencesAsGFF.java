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
  /**
   * The <span class="type">FeatureFilter</span> for selecting features to
   * report as <span class="type">GFFRecord</span>s.
   */
  private FeatureFilter filter = FeatureFilter.all;
  
  /**
   * Whether or not to recurse through the features during searching.
   */
  private boolean recurse = false;
  
  /**
   * Return the current <span class="type">FeatureFilter</span>.
   * <P>
   * This is the object that will accept or reject individual features.
   *
   * @return the current <span class="type">FeatureFilter</span>
   */
  public FeatureFilter getFeatureFilter() {
    return filter;
  }
  
  /**
   * Replace the current <span class="type">FeatureFilter</span> with
   * <span class="arg">filter</span>.
   *
   * @param filter  the new <span class="type">FeatureFilter</span>
   */
  public void setFeatureFilter(FeatureFilter filter) {
    this.filter = filter;
  }
  
  /**
   * Return whether features will be filtered recursively or not.
   *
   * @return whether or not to recurse
   */
  public boolean getRecurse() {
    return recurse;
  }
  
  /**
   * Set whether features will be filtered recursively to
   * <span class="arg">recurse</span>.
   *
   * @param recurse  <span class="kw">true</span> if you want to recurse,
   *                 <span class="kw">false</span> otherwise
   */
  public void setRecurse(boolean recurse) {
    this.recurse = recurse;
  }
  
  /**
   * Internal method to process an individual <span class="type">Sequence</span>.
   *
   * @param seq  the <span class="type">Sequence</span> to GFFify
   * @param handler the <span class="type">GFFDocumentHandler</span> that will
   *                receive the GFF for all suitable features within
   *                <span class="arg">seq</span>
   * @param id the value of the <span class="method">seqName</span> field in any
   *           <span class="type">GFFRecord</span>s produced
   */
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

  /**
   * Process an individual <span class="type">Sequence</span>, informing
   * <span class="arg">handler</span> of any suitable features.
   *
   * @param seq  the <span class="type">Sequence</span> to GFFify
   * @param handler the <span class="type">GFFDocumentHandler</span> that will
   *                receive the GFF for all suitable features within
   *                <span class="arg">seq</span>
   */
  public void processSequence(Sequence seq, GFFDocumentHandler handler) 
  throws SeqException, BioException {
    handler.startDocument();
    doProcessSequence(seq, handler, seq.getName());
    handler.endDocument();
  }

  /**
   * Process all <span class="type">Sequence</span>s within a
   * <span class="type">SequenceDB</span>, informing
   * <span class="arg">handler</span> of any suitable features.
   *
   * @param seqDB  the <span class="type">SequenceDB</span> to GFFify
   * @param handler the <span class="type">GFFDocumentHandler</span> that will
   *                receive the GFF for all suitable features within
   *                <span class="arg">seqDB</span>
   */
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
