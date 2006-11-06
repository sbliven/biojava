package org.ensembl.variation.datamodel;

import java.util.logging.Logger;

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.impl.LocatableImpl;

/**
 * Represents part or all of the effect of a single allele on a transcript.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class AlleleConsequence extends LocatableImpl {

  private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(AlleleConsequence.class
      .getName());

  // Types of allele consequences on a transcript

  /** Allele appears upstream of the transcript. */
  public final static String UPSTREAM = "UPSTREAM";

  /** Allele appears upstream of the transcript. */
  public final static String DOWNSTREAM = "DOWNSTREAM";

  /** Allele appears within an intron. */
  public final static String INTRONIC = "INTRONIC";

  /** Allele causes a new stop codon. */
  public final static String STOP_GAINED = "STOP_GAINED";

  /** Allele causes the loss of a stop codon. */
  public final static String STOP_LOST = "STOP_LOST";

  /** Allele does not affect the peptide. */
  public final static String SYNONYMOUS_CODING = "SYNONYMOUS_CODING";

  /** Allele affects the peptide. */
  public final static String NON_SYNONYMOUS_CODING = "NON_SYNONYMOUS_CODING";

  /** Allele causes a frameshift. */
  public final static String FRAMESHIFT_CODING = "FRAMESHIFT_CODING";

  /** Allele affects the 5prime UTR. */
  public final static String FIVE_PRIME_UTR = "FIVE_PRIME_UTR";

  /** Allele affects the 3prim UTR. */
  public final static String THREE_PRIME_UTR = "THREE_PRIME_UTR";

  // Types of effect on splice sites
  /** Allele hits the 3rd, 4th, 5th, 6th, 7th or 8th bases at the start or end of an 
	 * intron OR the 1st or 2nd bases of an exon (adjacent to an intron). */
  public final static String SPLICE_SITE = "SPLICE_SITE";

  /**
   * Allele hits the 1st or 2nd bases at the beginning or end of an
   * intron.
   */
  public final static String ESSENTIAL_SPLICE_SITE = "ESSENTIAL_SPLICE_SITE";

  private String type;

  private String spliceSite;

  private Location location;

  public AlleleConsequence(Location location, String type, String spliceSite) {
    this.location = location;
    this.type = type;
    this.spliceSite = spliceSite;
  }

  /**
   * Type of consequence.
   * 
   * Legal values listed below.
   * 
   * @return type of this consequence.
   * @see #UPSTREAM
   * @see #DOWNSTREAM
   * @see #FRAMESHIFT_CODING
   * @see #INTRONIC
   * @see #NON_SYNONYMOUS_CODING
   * @see #SYNONYMOUS_CODING
   * @see #STOP_GAINED
   * @see #STOP_LOST
   * @see #FIVE_PRIME_UTR
   * @see #THREE_PRIME_UTR
   */
  public String getType() {
    return type;
  }

  /**
   * Type of effect on splice site, if any.
   * 
   * See below for legal values.
   * 
   * @return splice site, null if splice site unaffected by allele.
   * @see #SPLICE_SITE
   * @see #ESSENTIAL_SPLICE_SITE
   */
  public String getSpliceSite() {
    return spliceSite;
  }


	public String toString() {
    StringBuffer buf = new StringBuffer();

    buf.append("[");
    buf.append("type=").append(type);
    buf.append(", spliceSite=").append(spliceSite);
    buf.append(", location=").append(location);
    buf.append("]");

    return buf.toString();
  }

	
}