package org.biojava.bio.search;

import org.biojava.utils.contract.Contract;
import org.biojava.utils.ObjectUtil;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.BioException;

//import org.log4j.Category;

import java.util.*;

/**
 * an class that implements the trivial (housekeeping) responsibilities of interface
 * SeqSimilaritySearchResult. Objects of this class are immutable.
 *
 * @author <A href="mailto:Gerald.Loeffler@vienna.at">Gerald Loeffler</A> for the 
 *         <A href="http://www.imp.univie.ac.at">IMP</A>
 */
public class SimpleSeqSimilaritySearchResult implements SeqSimilaritySearchResult, Cloneable {
  //private static final Category logger = Category.getInstance(SimpleSeqSimilaritySearchResult.class.getName());

  private SeqSimilaritySearcher searcher;
  private SymbolList            querySequence;
  private SequenceDB            sequenceDB;
  private Map                   searchParameters;
  private List                  hits;

  /** 
   * construct an immutable object by giving all its properties.
   * @param searcher the sequence similarity searcher that produced this search result. Not null.
   * @param querySequence the query sequence that gave rise to this search result. Not null.
   * @param sequenceDB the sequence database against the search that produced this results was done. Not null.
   * @param searchParameters the search parameters used in the search that produced this result. May be null. If not
   *                         null, the getter for this property returns an unmodifiable view of this object.
   * @param hits the list of SeqSimilaritySearchHit objects that make up this result. Not null. The getter for this
   *             property returns an unmodifiable view of this object.
   */
  public SimpleSeqSimilaritySearchResult(SeqSimilaritySearcher searcher, SymbolList querySequence,
      SequenceDB sequenceDB, Map searchParameters, List hits) {
    Contract.pre(searcher != null, "searcher not null");
    Contract.pre(querySequence != null, "querySequence not null");
    Contract.pre(sequenceDB != null, "sequenceDB not null");
    // searchParameters may be null
    Contract.pre(hits != null, "hits not null");

    this.searcher         = searcher;
    this.querySequence    = querySequence;
    this.sequenceDB       = sequenceDB;
    this.searchParameters = searchParameters;
    this.hits             = hits;
  }
  
  public SeqSimilaritySearcher getSearcher() {
    return searcher;
  }

  public SymbolList getQuerySequence() {
    return querySequence;
  }

  public SequenceDB getSequenceDB() {
    return sequenceDB;
  }

  /**
   * return an unmodifiable view of the search parameters map.
   */
  public Map getSearchParameters() {
    return (searchParameters == null ? null : Collections.unmodifiableMap(searchParameters));
  }

  /**
   * return an unmodifiable view of the hits list.
   */
  public List getHits() {
    return Collections.unmodifiableList(hits);
  }

  public String toString() {
    return "SimpleSeqSimilaritySearchResult of " + getQuerySequence() + " against " + getSequenceDB();
  }
  
  public boolean equals(Object o) {
    if (o == this) return true;
    
    // if this class is a direct sub-class of Object:
    if (o == null) return false;
    if (!o.getClass().equals(this.getClass())) return false;
    
    SimpleSeqSimilaritySearchResult that = (SimpleSeqSimilaritySearchResult) o;
    
    // only compare fields of this class (not of super-classes):
    if (!ObjectUtil.equals(this.searcher, that.searcher)) return false;
    if (!ObjectUtil.equals(this.querySequence, that.querySequence)) return false;
    if (!ObjectUtil.equals(this.sequenceDB, that.sequenceDB)) return false;
    if (!ObjectUtil.equals(this.searchParameters, that.searchParameters)) return false;
    if (!ObjectUtil.equals(this.hits, that.hits)) return false;
    
    // this and that are identical if we made it 'til here
    return true;
  }
  
  public int hashCode() {
    // if this class is a direct sub-class of Object:
    int hc = 0;

    // only take into account fields of this class (not of super-class):
    hc = ObjectUtil.hashCode(hc, searcher);
    hc = ObjectUtil.hashCode(hc, querySequence);
    hc = ObjectUtil.hashCode(hc, sequenceDB);
    hc = ObjectUtil.hashCode(hc, searchParameters);
    hc = ObjectUtil.hashCode(hc, hits);

    return hc;
  }
  
  public Object clone() {
    // this is an immutable class so we can return ourselves
    return this;
  }
}
