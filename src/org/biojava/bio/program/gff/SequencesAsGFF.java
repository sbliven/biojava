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
  
    protected void doProcessSequence(Sequence seq, GFFDocumentHandler handler,
				     String id) 
        throws SeqException, BioException 
    {
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
	    record.setGroupAttributes(null);
	    record.setComment(null);
        
	    handler.recordLine(record);
	}
    }

    public void processSequence(Sequence seq, GFFDocumentHandler handler) 
        throws SeqException, BioException 
    {
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
