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

import java.util.*;

/**
 * A no-frills implementation of a <span class="type">GFFRecord</span>.
 *
 * @author Matthew Pocock
 */
public class SimpleGFFRecord implements GFFRecord {
  /**
   * The sequence name.
   */
  private String seqName;
  /**
   * The source.
   */
  private String source;
  /**
   * The feature type.
   */
  private String feature;
  /**
   * The start coordinate.
   */
  private int start;
  /**
   * The end coordinate.
   */
  private int end;
  /**
   * The feature score.
   */
  private double score;
  /**
   * The feature strand.
   */
  private int strand;
  /**
   * The feature frame.
   */
  private int frame;
  /**
   * The group-name -> <span class="type">List</span> &lt;attribute&gt;
   * <span class="type">Map</span>
   */
  private Map groupAttributes;
  /**
   * The comment.
   */
  private String comment;
  
  /**
   * Set the sequence name to <span class="arg">seqName</span>.
   *
   * @param seqName  the new name
   */
  public void setSeqName(String seqName) {
    this.seqName = seqName;
  }
  
  public String getSeqName() {
    return seqName;
  }
  
  /**
   * Set the feature source to <span class="arg">source</source>.
   *
   * @param source  the new source
   */
  public void setSource(String source) {
    this.source = source;
  }
  
  public String getSource() {
    return source;
  }
  
  /**
   * Set the feature type to <span class="arg">type</source>.
   *
   * @param feature  the new feature type
   */
  public void setFeature(String feature) {
    this.feature = feature;
  }
  
  public String getFeature() {
    return feature;
  }
  
  /**
   * Set the start coordinate to <span class="arg">start</source>.
   *
   * @param start  the new start coordinate
   */
  public void setStart(int start) {
    this.start = start;
  }
  
  public int getStart() {
    return start;
  }
  
  /**
   * Set the end coordinate to <span class="arg">end</source>.
   *
   * @param end  the new end coordinate
   */
  public void setEnd(int end) {
    this.end = end;
  }
  
  public int getEnd() {
    return end;
  }
  
  /**
   * Set the score to <span class="arg">score</source>.
   * <P>
   * The score must be a double, inclusive of <code>0</code>.
   * If you wish to indicate that there is no score, then use
   * <span class="type">GFFRecord</span>.<span class="const">NO_SCORE</span>.
   *
   * @param score  the new score
   */
  public void setScore(double score) {
    this.score = score;
  }
  
  public double getScore() {
    return score;
  }
  
  /**
   * Set the strand to <span class="arg">strand</source>.
   * <P>
   * The strand must be one of
   * <span class="type">GFFRecord</span>.<span class="const">NO_STRAND</span>,
   * <span class="type">GFFRecord</span>.<span class="const">POSITIVE_STRAND</span> or
   * <span class="type">GFFRecord</span>.<span class="const">NEGATIVE_STRAND</span>.
   *
   * @param score  the new score
   * @throws IllegalArgumentException if strand is not a legal strand
   */
  public void setStrand(int strand) throws IllegalArgumentException {
    if(strand != GFFRecord.POSITIVE_STRAND &&
       strand != GFFRecord.NEGATIVE_STRAND &&
       strand != GFFRecord.NO_STRAND )
    {
      throw new IllegalArgumentException("Illegal strand: " + strand);
    }
    this.strand = strand;
  }
  
  public int getStrand() {
    return strand;
  }
  
  /**
   * Set the frame to <span class="arg">frame</source>.
   * <P>
   * The score must be  one of <code>{0, 1, 2}</code> or
   * <span class="type">GFFRecord</span>.<span class="const">NO_FRAME</span>.
   *
   * @param score  the new score
   * @throws IllegalArgumentException if score is not valid.
   */
  public void setFrame(int frame) throws IllegalArgumentException {
    if(frame != GFFRecord.NO_FRAME &&
       (frame < 0 || frame > 2))
    {
      throw new IllegalArgumentException("Illegal frame: " + frame);
    }
    this.frame = frame;
  }
  
  public int getFrame() {
    return frame;
  }
  
  /**
   * Replace the group-attribute <span class="type">Map</span> with 
   * <span class="arg">ga</span>.
   * <P>
   * To efficiently add a key, call <span class="method">getGroupAttributes</span>
   * and modify the <span class="type">Map</span>.
   *
   * @param ga  the new group-attribute <span class="type">Map</span>
   */
  public void setGroupAttributes(Map ga) {
    this.groupAttributes = ga;
  }
  
  public Map getGroupAttributes() {
    if(groupAttributes == null) {
      groupAttributes = new HashMap();
    }
    return groupAttributes;
  }
  
  /**
   * Set the comment to <span class="arg">comment</source>.
   * <P>
   * If you set it to null, then the comment for this line will be ignored.
   *
   * @param the new comment
   */
  public void setComment(String comment) {
    this.comment = comment;
  }
  
  public String getComment() {
    return comment;
  }
  
  /**
   * Parse <span class="arg">attValList</span> into a
   * <span class="type">Map</span> of attributes and value lists.
   * <P>
   * The resulting <span class="type">Map</span> will have
   * <span class="type">String</span> keys, with
   * <span class="type">List</span> values. If there are no values
   * associated with a key, then it will have an empty
   * <span class="type">List</span>, not <span class="kw">null</span> as
   * its value.
   *
   * @param attValList  the <span class="type">String</span> to parse
   * @return a <span class="type">Map</span> of parsed attributes and value lists
   */
  public static Map parseAttribute(String attValList) {
    Map attMap = new HashMap();
    
    StringTokenizer sTok = new StringTokenizer(attValList, ";", false);
    while(sTok.hasMoreTokens()) {
      String attVal = sTok.nextToken().trim();
      String attName;
      List valList = new ArrayList();
      int spaceIndx = attVal.indexOf(" ");
      if(spaceIndx == -1) {
        attName = attVal;
      } else {
        attName = attVal.substring(0, spaceIndx);
        attValList = attVal.substring(spaceIndx).trim();
        while(attValList.length() > 0) {
          if(attValList.startsWith("\"")) {
	    // System.out.println("Quoted");
            int quoteIndx = 0;
            do {
              quoteIndx++;
              quoteIndx = attValList.indexOf("\"", quoteIndx);
            } while(quoteIndx != -1 && attValList.charAt(quoteIndx-1) == '\\');
            valList.add(attValList.substring(1, quoteIndx));
            attValList = attValList.substring(quoteIndx+1).trim();
          } else {
            spaceIndx = attValList.indexOf(" ");
            if(spaceIndx == -1) {
              valList.add(attValList);
              attValList = "";
            } else {
              valList.add(attValList.substring(0, spaceIndx));
              attValList = attValList.substring(spaceIndx).trim();
            }
          }
        }
      }
      attMap.put(attName, valList);
    }
    
    return attMap;
  }
  
  /**
   * Create a <span class="type">String</span> representation of
   * <span class="arg">attMap</span>.
   *
   * <span class="arg">attMap</span> is assumed to contain
   * <span class="type">String</span> keys and
   * <span class="type">List</span> values.
   *
   * @param attMap  the <span class="type">Map</span> of attributes and value lists
   * @return  a GFF attribute/value <span class="type">String</span>
   */
  public static String stringifyAttributes(Map attMap) {
    StringBuffer sBuff = new StringBuffer();
    Iterator ki = attMap.keySet().iterator();
    if(ki.hasNext()) {
      String key = (String) ki.next();
      sBuff.append(key);
      List values = (List) attMap.get(key);
      for(Iterator vi = values.iterator(); vi.hasNext();) {
        String value = (String) vi.next();
        if(value.indexOf(" ") != -1) {
          sBuff.append(" \"" + value + "\"");
        } else {
          sBuff.append(" " + value);
        }
      }
    }
    while( ki.hasNext() ) {
      String key = (String) ki.next();
      sBuff.append("; " + key);
      List values = (List) attMap.get(key);
      for(Iterator vi = values.iterator(); vi.hasNext();) {
        String value = (String) vi.next();
        if(value.indexOf(" ") != -1) {
          sBuff.append(" \"" + value + "\"");
        } else {
          sBuff.append(" " + value);
        }
      }      
    }
    return sBuff.toString();
  }
}

