package ssbind;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.search.*;

/**
 * <p>
 * Build SeqSimilaritySearchSubHit instances from the sub hit info.
 * <em>Note: You must have an AlignmentBuilder in the chain <i>before</i>
 * this handler</em>
 * </p>
 *
 * <p>
 * This will consume all the information necessary to build a
 * SeqSimilaritySearchSubHit instance and then emit one under the key
 * SubHitBuilder.SEARCH_SUB_HIT to the next handler in the chain.
 * </p>
 *
 * @author Matthew Pocock
 */
public class SubHitBuilder
extends SearchContentFilter {
  public static final Object SEARCH_SUB_HIT = SubHitBuilder.class.toString() + ".SEARCH_SUB_HIT";
  
  private Alignment alignment;
  private double eValue;
  private double pValue;
  private double score;

  private int subjectStart;
  private int subjectEnd;
  private StrandedFeature.Strand subjectStrand;

  private int queryStart;
  private int queryEnd;
  private StrandedFeature.Strand queryStrand;
  
  public SubHitBuilder(SearchContentHandler delegate) {
    super(delegate);
  }
  
  public void startSubHit() {
    alignment = null;
    eValue = Double.NaN;
    pValue = Double.NaN;
    score = 0;
    
    subjectStart = Integer.MIN_VALUE;
    subjectEnd = Integer.MAX_VALUE;
    subjectStrand = StrandedFeature.UNKNOWN;

    queryStart = Integer.MIN_VALUE;
    queryEnd = Integer.MAX_VALUE;
    queryStrand = StrandedFeature.UNKNOWN;
  }
  
  public void addSubHitProperty(Object key, Object val) {
    if(false) {
    } else if("expectedValue".equals(key)) {
      eValue = Double.parseDouble((String) val);
    } else if("score".equals(key)) {
      score = Double.parseDouble((String) val);
    } else if("subjectSequenceStart".equals(key)) {
      subjectStart = Integer.parseInt((String) val);
    } else if("subjectSequenceEnd".equals(key)) {
      subjectEnd = Integer.parseInt((String) val);
    } else if("subjectStrand".equals(key)) {
      subjectStrand = StrandParser.parseStrand((String) val);
    } else if("querySequenceStart".equals(key)) {
      queryStart = Integer.parseInt((String) val);
    } else if("querySequenceEnd".equals(key)) {
      queryEnd = Integer.parseInt((String) val);
    } else if("queryStrand".equals(key)) {
      queryStrand = StrandParser.parseStrand((String) val);
    } else if(AlignmentBuilder.ALIGNMENT.equals(key)) {
      alignment = (Alignment) val;
    } else {
      super.addSubHitProperty(key, val);
    }
  }
  
  public void endSubHit() {
    super.addSubHitProperty(
      SEARCH_SUB_HIT,
      new SimpleSeqSimilaritySearchSubHit(
        score, eValue, pValue,
        Math.min(queryStart, queryEnd), Math.max(queryStart, queryEnd), queryStrand,
        Math.min(subjectStart, subjectEnd), Math.max(subjectStart, subjectEnd), subjectStrand,
        alignment, new SmallAnnotation()
      )
    );
    
    super.endSubHit();
  }
}
