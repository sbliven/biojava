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
 * A set of entries and comments as a representation of a GFF file.
 */
public class GFFEntrySet {
  private List lines;
  
  public GFFEntrySet() {
    lines = new ArrayList();
  }
  
  public Iterator lineIterator() {
    return lines.iterator();
  }
  
  public void add(String comment) {
    lines.add(comment);
  }
  
  public void add(GFFRecord record) {
    lines.add(record);
  }
  
  public int size() {
    return lines.size();
  }
  
  public Annotator getAnnotator() {
    return new AbstractAnnotator() {
      public boolean annotate(Sequence seq) throws SeqException {
        Feature.Template plain = new Feature.Template();
        StrandedFeature.Template stranded = new StrandedFeature.Template();
        plain.annotation = Annotation.EMPTY_ANNOTATION;
        stranded.annotation = Annotation.EMPTY_ANNOTATION;
        boolean addedAny = false;
        for(Iterator i = lineIterator(); i.hasNext(); ) {
          Object o = i.next();
          if(o instanceof GFFRecord) {
            GFFRecord rec = (GFFRecord) o;
            if(rec.getSeqName().equals(seq.getName())) {
              if(rec.getStrand() == GFFRecord.NO_STRAND) {
                plain.location = new RangeLocation(rec.getStart(), rec.getEnd());
                plain.type = rec.getFeature();
                plain.source = rec.getSource();
                seq.createFeature((MutableFeatureHolder) seq, plain);
                addedAny = true;
              } else {
                stranded.location = new RangeLocation(rec.getStart(), rec.getEnd());
                stranded.type = rec.getFeature();
                stranded.source = rec.getSource();
                int strand = rec.getStrand();
                if(strand == GFFRecord.POSITIVE_STRAND) {
                  stranded.strand = StrandedFeature.POSITIVE;
                } else if(strand == GFFRecord.NEGATIVE_STRAND) {
                  stranded.strand = StrandedFeature.NEGATIVE;
                }
                seq.createFeature((MutableFeatureHolder) seq, stranded);
                addedAny = true;
              }
            }
          }
        }
        return addedAny;
      }
    };
  }
  
  public GFFEntrySet filter(GFFRecordFilter filter) {
    GFFEntrySet accepted = new GFFEntrySet();
    for(Iterator i = lineIterator(); i.hasNext(); ) {
      Object o = i.next();
      if(o instanceof GFFRecord) {
        GFFRecord record = (GFFRecord) o;
        if(filter.accept(record)) {
          accepted.add(record);
        }
      }
    }
    
    return accepted;
  }
  
  public GFFDocumentHandler getAddHandler() {
    return new EntrySetBuilder();
  }
  
  private class EntrySetBuilder implements GFFDocumentHandler {
    public void startDocument() {}
    public void endDocument()   {}
  
    public void commentLine(String comment) {
      lines.add(comment);
    }
    
    public void recordLine(GFFRecord record) {
      lines.add(record);
    }
  
    public void invalidStart(String token, NumberFormatException nfe)
    throws BioException {
      throw new BioException(nfe, "Invalid start: " + token);
    }
    public void invalidEnd(String token, NumberFormatException nfe)
    throws BioException {
      throw new BioException(nfe, "Invalid end: " + token);
    }
    public void invalidScore(String token, NumberFormatException nfe)
    throws BioException {
      throw new BioException(nfe, "Invalid score: " + token);
    }
    public void invalidStrand(String token)
    throws BioException {
      throw new BioException("Invalid strand: " + token);
    }
    public void invalidFrame(String token, NumberFormatException nfe)
    throws BioException {
      throw new BioException(nfe, "Invalid frame: " + token);
    }
  }
}
