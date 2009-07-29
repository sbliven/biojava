package ssbind;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.alignment.SimpleAlignment;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.search.*;

/**
 * <p>
 * This filter intercepts the querySequence and subjectSequence events to build
 * an alignment.
 * </p>
 *
 * <p>
 * It will collect up the sequence information per sub hit, and then pass on a
 * newly built alignment object with labels AlignmentBuilder.QUERY and
 * AlignmentBuilder.SUBJECT under the event key AlignmentBuilder.ALIGNMEMT.
 * </p>
 *
 * <h2>Example</h2>
 *
 * <pre>
 * java ProcessBlastReport blast.out ssbind.AlignmentBuilder ssbind.Echoer
 * </pre> 
 *
 * @author Matthew Pocock
 */
public class AlignmentBuilder
extends SearchContentFilter {
  public static final Object QUERY = AlignmentBuilder.class.toString() + ".QUERY";
  public static final Object SUBJECT = AlignmentBuilder.class.toString() + ".SUBJECT";
  public static final Object ALIGNMENT = AlignmentBuilder.class.toString() + ".ALIGNMENT";
  
  private SymbolList querySequence;
  private SymbolList subjectSequence;
  
  public void addSubHitProperty(Object key, Object val) {
    try {
      if("querySequence".equals(key)) {
        querySequence = DNATools.createDNA((String) val);
      } else if("subjectSequence".equals(key)) {
        subjectSequence = DNATools.createDNA((String) val);
      } else {
        super.addSubHitProperty(key, val);
      }
    } catch (IllegalSymbolException ise) {
      throw new BioError(ise);
    }
  }
  
  public void endSubHit() {
    // build alignment
    Map seqMap = new SmallMap();
    seqMap.put(QUERY, querySequence);
    seqMap.put(SUBJECT, subjectSequence);
    super.addSubHitProperty(ALIGNMENT, new SimpleAlignment(seqMap));
    
    super.endSubHit();
  }
  
  public AlignmentBuilder(SearchContentHandler delegate) {
    super(delegate);
  }
}
