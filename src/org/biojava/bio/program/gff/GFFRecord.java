package org.biojava.bio.program.gff;

/**
 * A single GFF record.
 * <P>
 * This object has fields for each GFF field. It also defines a couple of
 * usefull constants.
 */
public interface GFFRecord {
  public String getSeqName();
  public String getSource();
  public String getFeature();
  public int getStart();
  public int getEnd();
  public double getScore();
  public int getStrand();
  public int getFrame();
  public String getGroupAttributes();
  public String getComment();
  
  public static double NO_SCORE = Double.NaN;
  public static int POSITIVE_STRAND = +1;
  public static int NEGATIVE_STRAND = -1;
  public static int NO_STRAND = 0;
  public static int NO_FRAME = -1;
}

