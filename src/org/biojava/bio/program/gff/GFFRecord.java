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
 * A single GFF record.
 * <P>
 * This object has fields for each GFF field. It also defines a couple of
 * usefull constants.
 * <P>
 * Gff is described at http://www.sanger.ac.uk/Software/formats/GFF/
 *
 * @author Matthew Pocock
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
   * For sequences that have no score, this will be set to
   * <span class="type">GFFRecord</span>.<span class="const">NO_SCORE</span>.
   *
   * @return the score, or NO_SCORE
   */
  public double getScore();
  
  /**
   * The strand of the feature.
   * <P>
   * This will be one of <span class="type">GFFRecord</span>.<span class="const">POSITIVE_STRAND</span>,
   * <span class="type">GFFRecord</span>.<span class="const">NEGATIVE_STRAND</span>,
   * or <span class="type">GFFRecord</span>.<span class="const">NO_STRAND</span>.
   *
   * @return the strand field
   */
  public int getStrand();
  
  /**
   * The frame of the feature.
   * <P>
   * This will be one of <code>{1, 2, 3}</code> or
   * <span class="type">GFFRecord</span>.<span class="const">NO_FRAME</span>.
   *
   * @return the frame field
   */
  public int getFrame();
  
  /**
   * A <span class="type">Map</span> containing the group / attribute information.
   * <P>
   * This will be a <span class="type">Map</span> of group-names to
   * <span class="type">List</span> objects.
   *
   * @return a <span class="type">Map</span> containing the group and attribute info.
   */
  public Map getGroupAttributes();
  
  /**
   * The feature comment.
   *
   * @return <span class="kw">null</span> or the feature comment
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

