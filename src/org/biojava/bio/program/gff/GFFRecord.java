package org.biojava.bio.program.gff;

import java.util.*;

/**
 * A single GFF record.
 * <P>
 * This object has fields for each GFF field. It also defines a couple of
 * usefull constants.
 * <P>
 * Gff is described at http://www.sanger.ac.uk/Software/formats/GFF/
 */
public interface GFFRecord {
  /**
   * The sequence name field.
   * <P>
   * This should be the name of the sequence that this gff record is within.
   *
   * @return the name of the sequence
   */
  public String getSeqName();
  
  /**
   * The source, or creator of this feature.
   * <P>
   * This is usualy a program name.
   *
   * @return the feature source
   */
  public String getSource();
  
  /**
   * The feature type filed.
   * <P>
   * This is something like "exon" - usualy corresponds to an EMBL term.
   *
   * @return the feature type
   */
  public String getFeature();
  
  /**
   * The start of this feature within the source sequence.
   *
   * @return the start index
   */
  public int getStart();

  /**
   * The end of this feature within the source sequence.
   *
   * @return the end index
   */
  public int getEnd();
  
  /**
   * The score of the feature.
   * <P>
   * For sequences that have no score, this will be set to GFFRecord.NO_SCORE.
   *
   * @return the score, or NO_SCORE
   */
  public double getScore();
  
  /**
   * The strand of the feature.
   * <P>
   * This will be one of GFFRecord.POSITIVE_STRAND or GFFRecord.NEGATIVE_STRAND,
   * or GFFRecord.NO_STRAND.
   *
   * @return the strand field
   */
  public int getStrand();
  
  /**
   * The frame of the feature.
   * <P>
   * This will be one of {1, 2, 3} or GFFRecord.NO_FRAME.
   *
   * @return the frame field
   */
  public int getFrame();
  
  /**
   * A Map containing the group / attribute information.
   * <P>
   * This will be a map of group-names to List objects.
   *
   * @return a Map containing the group and attribute info.
   */
  public Map getGroupAttributes();
  
  /**
   * The feature comment.
   *
   * @return null or the feature comment
   */
  public String getComment();
  
  /**
   * Flag to indicate that there is no score info.
   */
  public static double NO_SCORE = Double.NEGATIVE_INFINITY;
  /**
   * The feature is on the positive strand.
   */
  public static int POSITIVE_STRAND = +1;
  /**
   * The feature is on the negative strand.
   */
  public static int NEGATIVE_STRAND = -1;
  /**
   * Flag to indicate that there is no strand info.
   */
  public static int NO_STRAND = 0;
  /**
   * Flag to indicate that there is no frame info.
   */
  public static int NO_FRAME = -1;
}

