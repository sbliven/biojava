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
 * A no-frills implementation of a GFFRecord.
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
   * The group-name -> List <attribute> map
   */
  private Map groupAttributes;
  /**
   * The comment.
   */
  private String comment;
  
  /**
   * Set the sequence name.
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
   * Set the feature source.
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
   * Set the feature type.
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
   * Set the start coordinate.
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
   * Set the end coordinate.
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
   * Set the score.
   * <P>
   * The score must be a double, inclusive of 0. If you wish to indicate that
   * there is no score, then use GFFRecord.NO_SCORE.
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
   * Set the score.
   * <P>
   * The score must be a double, inclusive of 0. If you wish to indicate that
   * there is no score, then use GFFRecord.NO_SCORE.
   *
   * @param score  the new score
   * @throws IllegalArgumentException if strand is not one of the GFFRecord
   *         strand constants.
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
   * Set the frame.
   * <P>
   * The score must be  one of 0, 1, 2 or GFFRecord.NO_FRAME.
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
   * Replace the group-attribute map with a new one.
   * <P>
   * To efficiently add a key, call getGroupAttributes and modify the map.
   *
   * @param ga  the new group-attribute map
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
   * Set the comment.
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
            System.out.println("Quoted");
            int quoteIndx = 0;
            do {
              quoteIndx++;
              quoteIndx = attValList.indexOf("\"", quoteIndx);
            } while(quoteIndx != -1 && attValList.charAt(quoteIndx-1) != '\\');
            valList.add(attValList.substring(1, quoteIndx));
            attValList = attValList.substring(quoteIndx).trim();
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

