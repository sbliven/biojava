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
 * A filter that will accept or reject a <span class="type">GFFEntry</span>.
 *
 * @author Matthew Pocock
 */
public interface GFFRecordFilter {
  /**
   * Return wether or not to accept <span class="arg">record</span>.
   *
   * @param entry the <span class="type">GFFRecord</span> to filter
   * @return <span class="kw">true</span> if <span class="arg">record</span>
   *         should be accepted or <span class="kw">false</span> otherwise
   */
  boolean accept(GFFRecord record);
  
  /**
   * A <span class="type">GFFRecordFilter</span> that accepts everything.
   */
  final static GFFRecordFilter ACCEPT_ALL = new AcceptAll();
  
  /**
   * Implementation of <span class="type">GFFRecordFilter</span> that accepts everything.
   *
   * @author Matthew Pocock
   */
  public class AcceptAll implements GFFRecordFilter {
    /**
     * @return <span class="kw">true</span>
     */
    public boolean accept(GFFRecord record) {
      return true;
    }
  }
  
  /**
   * Implementation of <span class="type">GFFRecordFilter</span> that accepts
   * records based upon the sequence name.
   *
   * @author Matthew Pocock
   */
  public class SequenceFilter implements GFFRecordFilter {
    /**
     * The sequence name to accept.
     */
    private String seqName;
    
    /**
     * Retrieve the current sequence name.
     *
     * @return the sequence name <span class="type">String</span>
     */
    public String getSeqName() {
      return seqName;
    }
    
    /**
     * Set the sequence name to <span class="arg">seqName</span>.
     *
     * @param seqName the new sequence name to match
     */
    public void setSeqName(String seqName) {
      this.seqName = seqName;
    }
    
    /**
     * @return <span class="arg">record</span>.
     * <span class="method">getSeqName</span><code>()</code> <code>==</code>
     * <span class="const">this</span>.<span class="method">getSeqName</span><code>()</code>
     */
    public boolean accept(GFFRecord record) {
      return record.getSeqName().equals(seqName);
    }
  }
  
  /**
   * Implementation of <span class="type">GFFRecordFilter</span> that accepts
   * records based upon the source field.
   *
   * @author Matthew Pocock
   */
  public class SourceFilter implements GFFRecordFilter {
    private String source;
    
    /**
     * Retrieve the current source.
     *
     * @return the source <span class="type">String</span>
     */
    public String getSource() {
      return source;
    }
    
    /**
     * Set the source to <span class="arg">source</span>.
     *
     * @param source the new source to match
     */
    public void setSource(String source) {
      this.source = source;
    }
    
    /**
     * @return <span class="arg">record</span>.
     * <span class="method">getSource</span><code>()</code> <code>==</code>
     * <span class="const">this</span>.<span class="method">getSource</span><code>()</code>
     */
    public boolean accept(GFFRecord record) {
      return record.getSource().equals(source);
    }
  }
  
  /**
   * Implementation of <span class="type">GFFRecordFilter</span> that accepts
   * records based upon the feature field.
   *
   * @author Matthew Pocock
   */
  public class FeatureFilter implements GFFRecordFilter {
    private String feature;
    
    /**
     * Set the feature to <span class="arg">feature</span>.
     *
     * @param source the new source to match
     */
    public void setFeature(String feature) {
      this.feature = feature;
    }
    
    /**
     * Retrieve the current feature.
     *
     * @return the feature <span class="type">String</span>
     */
    public String getFeature() {
      return feature;
    }
    
    /**
     * @return <span class="arg">record</span>.
     * <span class="method">getFeature</span><code>()</code> <code>==</code>
     * <span class="const">this</span>.<span class="method">getFeature</span><code>()</code>
     */
    public boolean accept(GFFRecord record) {
      return record.getFeature().equals(feature);
    }
  }
}
