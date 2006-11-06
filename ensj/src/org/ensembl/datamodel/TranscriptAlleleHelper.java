/*
 Copyright (C) 2005 EBI, GRL

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.driver.AdaptorException;
import org.ensembl.util.SequenceUtil;
import org.ensembl.util.StringUtil;
import org.ensembl.variation.datamodel.AlleleConsequence;
import org.ensembl.variation.datamodel.AlleleFeature;
import org.ensembl.variation.datamodel.impl.AlleleFeatureImpl;

/**
 * Denormalised representation of the transcript optimised for answering
 * questions about variations.
 * 
 * Precalculates and caches variation related information about the transcript.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 * 
 * TODO finish testing: org.ensembl.variation.test.TranscriptAlleleHelperTest
 * TODO add Transcript.getTranscriptAlleleHelper() once testing completed.
 * TODO compare results to perl API.
 * TODO optimise further?
 */
final public class TranscriptAlleleHelper {

  /**
   * Simple and fast start-and-end struct.
   */
  final class Range {
    final int start;

    final int end;

    Range(int start, int end) {
      this.start = start;
      this.end = end;
    }

    Range(Location[] locs) {
      int s = Integer.MAX_VALUE;
      int e = Integer.MIN_VALUE;
      for (int i = 0; i < locs.length; i++) {
        final Location l = locs[i];
        s = Math.min(s, l.getStart());
        e = Math.max(e, l.getEnd());
      }
      start = s;
      end = e;
    }

    boolean overlaps(final int otherStart, final int otherEnd) {
      return otherStart < end && otherEnd > start;
    }

    public String toString() {
      StringBuffer buf = new StringBuffer();

      buf.append("[");
      buf.append("start=").append(start);
      buf.append(", end=").append(end);
      buf.append("]");

      return buf.toString();
    }
  }

  private static final Logger logger = Logger.getLogger(TranscriptAlleleHelper.class
      .getName());

  final int SPLICE_SITE_LEN = 2;

  final int ESSENTIAL_SPLICE_SITE_LEN = 8;

  final Transcript transcript;

  final boolean hasSpliceSites;

  final Location transcriptLoc;

  final int transcriptStart;

  final int transcriptEnd;

  final boolean forward;

  final Translation tn;

  final boolean hasTranslation;

  final Location[] fivePrimeUTR;

  final boolean hasFivePrimeUTR;

  final Range fivePrimeRange;

  final Location[] coding;

  /**
   * cache of codingDNA strings corresponding to the parts of coding exons and
   * _coding_ locations.
   */
  final String[] codingDNA;

  final boolean hasCoding;

  final Range codingRange;

  final Location[] threePrimeUTR;

  final boolean hasThreePrimeUTR;

  final Range threePrimeRange;

  final int cdnaStart;

  final int cdnaEnd;

  final int cdsStart;

  final int cdsEnd;

  final List exons;

  final Location exonSpliceSites;

  final Location intronSpliceSites;

  final Location intronEssentialSpliceSites;

  /**
   * Contains the first and last base of each exon.
   */
  final int[] exonBoundaries;

  public String toString() {
    return toString(", ");
  }

  public String toString(String separator) {
    StringBuffer buf = new StringBuffer();

    buf.append("[");
    buf.append(" transcript=").append(transcript.getInternalID());
    buf.append(separator).append("tn=").append(tn.getInternalID());
    buf.append(separator).append("transcriptStart=").append(transcriptStart);
    buf.append(separator).append("transcriptEnd=").append(transcriptEnd);
    buf.append(separator).append("forward=").append(forward);
    buf.append(separator).append("cdnaStart=").append(cdnaStart);
    buf.append(separator).append("cdnaEnd=").append(cdnaEnd);
    buf.append(separator).append("cdsStart=").append(cdsStart);
    buf.append(separator).append("cdsEnd=").append(cdsEnd);
    buf.append(separator).append("fivePrimeUTR=").append(
        StringUtil.toString(fivePrimeUTR));
    buf.append(separator).append("fivePrimeRange=").append(fivePrimeRange);
    buf.append(separator).append("coding=").append(StringUtil.toString(coding));
    buf.append(separator).append("codingDNA=").append(
        StringUtil.toString(codingDNA));
    buf.append(separator).append("codingRange=").append(codingRange);
    buf.append(separator).append("threePrimeUTR=").append(
        StringUtil.toString(threePrimeUTR));
    buf.append(separator).append("threePrimeRange=").append(threePrimeRange);
    buf.append(separator).append("exonSpliceSites=").append(exonSpliceSites);
    buf.append(separator).append("intronSpliceSites=")
        .append(intronSpliceSites);
    buf.append(separator).append("intronEssentialSpliceSites=").append(
        intronEssentialSpliceSites);
    buf.append(separator).append("exonBoundaries=").append(
        StringUtil.toString(exonBoundaries));
    buf.append("]");

    return buf.toString();
  }

  /**
   * Denormalise relevant information from transcript.
   * 
   * @param transcript
   *          source transcript.
   */
  public TranscriptAlleleHelper(Transcript transcript) {

    this.transcript = transcript;
    transcriptLoc = transcript.getLocation();

    transcriptStart = transcriptLoc.getStart();
    transcriptEnd = transcriptLoc.getEnd();
    forward = transcriptLoc.getStrand() != -1;
    exons = transcript.getExons(); // sorted by location

    tn = transcript.getTranslation();
    hasTranslation = tn != null;
    if (!hasTranslation)
      logger
          .warning("Transcript has no translation; some allele consequences may be missed. "
              + transcript);
    fivePrimeUTR = (tn == null) ? null : toLocationArray(tn.getFivePrimeUTR());
    hasFivePrimeUTR = fivePrimeUTR != null && fivePrimeUTR.length > 0;
    fivePrimeRange = (hasFivePrimeUTR) ? new Range(fivePrimeUTR) : null;
    coding = (tn == null) ? null : toLocationArray(tn.getCodingLocations());
    hasCoding = coding != null && coding.length > 0;
    codingRange = (hasCoding) ? new Range(coding) : null;
    codingDNA = (hasCoding) ? new String[coding.length] : null;
    //codingDNA=null;

    threePrimeUTR = (tn == null) ? null
        : toLocationArray(tn.getThreePrimeUTR());
    hasThreePrimeUTR = threePrimeUTR != null && threePrimeUTR.length > 0;

    threePrimeRange = (hasThreePrimeUTR) ? new Range(threePrimeUTR) : null;

    cdnaStart = transcriptStart;
    cdnaEnd = transcriptEnd;
    cdsStart = codingRange.start;
    cdsEnd = codingRange.end;

    // Create all splice sites. We need to assign splice sites in constructor
    // because they
    // are final.

    Exon e = (Exon) exons.get(0);
    Location el = e.getLocation();
    final int nExons = exons.size();
    hasSpliceSites = nExons > 1;

    if (!hasSpliceSites) {

      exonSpliceSites = null;
      intronSpliceSites = null;
      intronEssentialSpliceSites = null;
      exonBoundaries = new int[] { el.getStart(), el.getEnd() };

    } else {

      // create splice locations around first or last exon
      exonSpliceSites = (forward) ? spliceAtEnd(el) : spliceAtStart(el);
      intronSpliceSites = (forward) ? spliceAfter(el, SPLICE_SITE_LEN)
          : spliceBefore(el, SPLICE_SITE_LEN);
      intronEssentialSpliceSites = (forward) ? spliceAfter(el,
          ESSENTIAL_SPLICE_SITE_LEN) : spliceBefore(el,
          ESSENTIAL_SPLICE_SITE_LEN);
      // TODO exon boundaries
      exonBoundaries = new int[nExons * 2];
      int exonBoundaryIndex = 0;
      exonBoundaries[exonBoundaryIndex++] = el.getStart();
      exonBoundaries[exonBoundaryIndex++] = el.getEnd();

      // create splice locations around 'middle' exons
      for (int i = 1; i < nExons - 1; i++) {

        Location prev = el;
        e = (Exon) exons.get(i);
        el = e.getLocation();

        // splice sites at start and end of exon
        exonSpliceSites.append(spliceAtStart(el)).append(spliceAtEnd(el));

        // 2base splice sites at start and end of intron
        intronSpliceSites.append(spliceBefore(prev, SPLICE_SITE_LEN)).append(
            spliceAfter(el, SPLICE_SITE_LEN));

        // 8base splice sites at start and end of intron
        intronEssentialSpliceSites.append(
            spliceBefore(prev, ESSENTIAL_SPLICE_SITE_LEN)).append(
            spliceAfter(el, ESSENTIAL_SPLICE_SITE_LEN));

        exonBoundaries[exonBoundaryIndex++] = el.getStart();
        exonBoundaries[exonBoundaryIndex++] = el.getEnd();
      }

      // create splice locations around first or last exon
      el = ((Exon) exons.get(nExons - 1)).getLocation();
      exonSpliceSites.append((forward) ? spliceAtStart(el) : spliceAtEnd(el));
      intronSpliceSites.append((forward) ? spliceBefore(el, SPLICE_SITE_LEN)
          : spliceAfter(el, SPLICE_SITE_LEN));
      intronEssentialSpliceSites.append((forward) ? spliceBefore(el,
          ESSENTIAL_SPLICE_SITE_LEN) : spliceAfter(el,
          ESSENTIAL_SPLICE_SITE_LEN));
      exonBoundaries[exonBoundaryIndex++] = el.getStart();
      exonBoundaries[exonBoundaryIndex++] = el.getEnd();

      Arrays.sort(exonBoundaries);

    }

  }

  private Location spliceBefore(Location src, int spliceSiteLen) {
    Location loc = src.copy();
    int start = src.getStart();
    loc.setStart(start - spliceSiteLen);
    loc.setEnd(start - 1);
    return loc;
  }

  private Location spliceAfter(Location src, int spliceSiteLen) {
    Location loc = src.copy();
    int end = src.getEnd();
    loc.setStart(end + 1);
    loc.setEnd(end + spliceSiteLen);
    return loc;
  }

  private Location spliceAtStart(Location src) {
    Location loc = src.copy();
    loc.setEnd(loc.getStart() + 1);
    return loc;
  }

  private Location spliceAtEnd(Location src) {
    Location loc = src.copy();
    loc.setStart(loc.getEnd() - 1);
    return loc;
  }

  /**
   * Returns the splice site string for this allele location relative to the
   * transcript.
   * 
   * @param alleleLoc
   *          location of an allele.
   * @return SPLICE_SITE if alleleLoc hits an exonSpliceSite, otherwise null.
   */
  String getExonSpliceSite(Location alleleLoc) {
    return (hasSpliceSites && alleleLoc.overlaps(exonSpliceSites)) ? AlleleConsequence.SPLICE_SITE
        : null;
  }

  /**
   * 
   * @param alleleLoc
   *          location of an allele.
   * @return ESSENTIAL_SPLICE_SITE if alleleLoc hits the first or last 2 bases
   *         of the intron, SPLICE_SITE if it hits the first or last 3-8 bases,
   *         otherwise null.
   */
  String getIntronSpliceSite(Location alleleLoc) {
    if (hasSpliceSites)
      if (alleleLoc.overlaps(intronEssentialSpliceSites))
        return AlleleConsequence.ESSENTIAL_SPLICE_SITE;
      else if (alleleLoc.overlaps(intronSpliceSites))
        return AlleleConsequence.SPLICE_SITE;
    return null;
  }

  AlleleConsequence createIntronAlleleConsequence(Location alleleLoc) {
    return new AlleleConsequence(alleleLoc, AlleleConsequence.INTRONIC,
        getIntronSpliceSite(alleleLoc));
  }

  boolean map2FivePrimeUTR(List alleleConsequences, Location alleleLoc,
      int len, int alleleStart, int alleleEnd) {

    for (int i = 0; hasFivePrimeUTR && i < fivePrimeUTR.length; i++) {
      final Location target = fivePrimeUTR[i];
      if (alleleLoc.overlaps(target)) {
        alleleConsequences.add(new AlleleConsequence(alleleLoc,
            AlleleConsequence.FIVE_PRIME_UTR, getExonSpliceSite(alleleLoc)));
        return true;
      }
    }
    return false;
  }

  boolean map2ThreePrimeUTR(List alleleConsequences, Location alleleLoc, int len) {

    for (int i = 0; hasThreePrimeUTR && i < threePrimeUTR.length; i++) {
      final Location target = threePrimeUTR[i];
      if (alleleLoc.overlaps(target)) {
        alleleConsequences.add(new AlleleConsequence(alleleLoc,
            AlleleConsequence.THREE_PRIME_UTR, null));
        return true;
      }
    }
    return false;
  }

  /**
   * Creates a ConsequenceAllele from an _alleleFeature_ and adds it to
   * _alleleConsequences_.
   * 
   * 
   * AlleleConsequence.type = FRAMESHIFT_CODING | STOP_GAINED | STOP_LOST |
   * NON_SYNONYMOUS_CODING | SYNONYMOUS_CODING
   * 
   * @param alleleFeature
   *          alleleFeature to be converted into an alleleConsequence
   * @param alleleConsequences
   *          output buffer
   * 
   * @return true if the allele feature overlaps the of coding part of an exon,
   *         otherwise false.
   * @throws AdaptorException
   */
  boolean map2Coding(AlleleFeature alleleFeature, List alleleConsequences)
      throws AdaptorException {

    Location loc = alleleFeature.getLocation();
    int diff = loc.getEnd() - loc.getStart();

    for (int i = 0; i < coding.length; i++) {
      final Location target = coding[i];
      if (loc.overlaps(target)) {

        String type = null;

        // calculate effect (if any) of allele on peptide
        String alleleString = alleleFeature.getAlleleString();
        if ("-".equals(alleleString)) {
          if (diff == 0)
            // insert nothing
            type = AlleleConsequence.SYNONYMOUS_CODING;
          else if (diff % 3 != 0)
            // deletion causing frameshift
            type = AlleleConsequence.FRAMESHIFT_CODING;
          else
            // deletion causing removal of amino acid(s)
            type = AlleleConsequence.NON_SYNONYMOUS_CODING;
        }

        String newPeptide = null;

        if (type == null) {
          int affectedLen = diff + 1 - alleleString.length();
          if (affectedLen % 3 == 0) {

            StringBuffer dna = new StringBuffer();

            // add coding dna before this exon
            for (int j = 0; j < i; j++)
              dna.append(fetchCodingSequence(j));

            // add modified version of this coding exon
            String original = fetchCodingSequence(i);
            String modified = SequenceUtil.replaceSubSequence(original, target, alleleString, loc);
            dna.append(modified);

            // add coding dna after this exon
            for (int j = i + 1; j < coding.length; j++)
              dna.append(fetchCodingSequence(j));

            newPeptide = SequenceUtil.dna2protein(dna.toString(), true);

          } else {
            // replacement causes a frameshift
            type = AlleleConsequence.FRAMESHIFT_CODING;
          }
        }

        if (type == null) {

          final String oldPeptide = transcript.getTranslation().getPeptide();

          if (oldPeptide.equals(newPeptide)) {

            type = AlleleConsequence.SYNONYMOUS_CODING;

          } else {

            if (newPeptide.indexOf('*') > -1 && oldPeptide.indexOf('*') == -1)
              type = AlleleConsequence.STOP_GAINED;
            else if (newPeptide.indexOf('*') == -1
                && oldPeptide.indexOf('*') > -1)
              type = AlleleConsequence.STOP_LOST;
            else
              type = AlleleConsequence.NON_SYNONYMOUS_CODING;
          }
        }

        alleleConsequences.add(new AlleleConsequence(loc, type,
            getExonSpliceSite(loc)));
        return true;
      }
    }
    return false;
  }

  private String fetchCodingSequence(int index) throws AdaptorException {
    String s = codingDNA[index];
    if (s == null) {
      s = transcript.getDriver().getSequenceAdaptor().fetch(coding[index])
          .getString();
      codingDNA[index] = s;
    }
    return s;
  }

  /**
   * @param fivePrimeUTR
   * @return
   */
  private Location[] toLocationArray(List l) {
    return (Location[]) l.toArray(new Location[l.size()]);
  }

  /**
   * Splits any allele features that cross exon-intron or exon-UTR boundaries
   * and removes any insertions or deletions at those places.
   * 
   * @return zero or more AlleleFeatures derived from _alleleFeatures_.
   * @see AlleleFeature
   */
  private LinkedList splitAndFilter(List alleleFeatures) {
    LinkedList r = new LinkedList();
    for (int i = 0; i < alleleFeatures.size(); i++) {
      AlleleFeature af = (AlleleFeature) alleleFeatures.get(i);
      splitAndFilter(af, r);
    }
    return r;
  }

  /**
   * Splits alles that cross exon-intron or exon-utr boundaries and deletes
   * insertions and deletions at these locations.
   * 
   * @param af
   *          allele feature
   * @return false if allele is an insersion or deletion that crosses a
   *         boundary, otherwise true.
   */
  private boolean splitAndFilter(AlleleFeature af,
      LinkedList alleleFeaturesOutput) {

    Location loc = af.getLocation();
    String alleleString = af.getAlleleString();

    if (loc.next() != null) {
      // Could split the location into separate AlleleFeatures but would need
      // to update string as appropriate.
      logger
          .warning("Ignoring allele feature that spans more than one location node: "
              + af);
      return false;
    }

    while (loc != null) {

      boolean crossed = false;
      int start = loc.getStart();
      int end = loc.getEnd();
      // note: exonBoundaries is sorted in ascending order
      for (int i = 0; i < exonBoundaries.length; i++) {

        final int boundary = exonBoundaries[i];
        final boolean crossBoundary = boundary > start && boundary < end;

        if (crossBoundary) {

          crossed = true;
          // Filter unhandled cases
          if (af.getAlleleString().length() != end - start + 1) {
            logger
                .warning("Ignoring deletion that crosses exon-intron or exon-utr boundary: "
                    + af);
            return false;
          } else if (end == start) {
            logger
                .warning("Ignoring insertion that crosses exon-intron or exon-utr boundary: "
                    + af);
            return false;
          }

          // first part of location
          Location before = loc.copyNode();
          before.setEnd(boundary - 1);
          String beforeAlleleString = alleleString.substring(0, before
              .getStart()
              - before.getEnd());
          alleleFeaturesOutput.add(new AlleleFeatureImpl(null, before,
              beforeAlleleString, -1));

          // remaining part of location
          loc = loc.copyNode();
          loc.setStart(boundary);
          alleleString = alleleString.substring(before.getStart()
              - before.getEnd(), alleleString.length() - 1);

        }
      }
      if (!crossed)
        loc = null;
    }

    if (alleleString == af.getAlleleString()) // no boundary crossed, leave
      // alleleFeature unchanged
      alleleFeaturesOutput.add(af);
    else
      alleleFeaturesOutput.add(new AlleleFeatureImpl(null, loc, alleleString,
          -1));

    return true;
  }

  /**
   * Converts one or more alleleFeatures into alleleConsequences.
   * 
   * @param alleleFeatures
   *          alleleFeatures to be converted.
   * 
   * @return zero or more AlleleConsequences
   * @throws AdaptorException
   */
  public List toAlleleConsequences(List alleleFeatures) throws AdaptorException {

    // handle alleleFeatures that cross exon-intron/exon-utr boundaries
    LinkedList alleleFeatureBuf = splitAndFilter(alleleFeatures);

    List alleleConsequences = new ArrayList();
    toAlleleConsequences(alleleFeatureBuf, alleleConsequences);

    return alleleConsequences;
  }

  /**
   * Converts an alleleFeature into zero or more alleleConsequences.
   * 
   * @param alleleFeature
   *          alleleFeature to be converted.
   * 
   * @return zero or more AlleleConsequences
   * @throws AdaptorException
   */
  public List toAlleleConsequences(AlleleFeature alleleFeature)
      throws AdaptorException {

    List alleleFeatures = new ArrayList(1);
    alleleFeatures.add(alleleFeature);
    return toAlleleConsequences(alleleFeatures);
  }

  /**
   * @param alleleFeatures
   *          list of zero or more alleleFeatures where each alleleFeature does
   *          not cross an exon boundary.
   * @param alleleConsequencesOutput
   *          output buffer to put alleleConsequences into.
   * @throws AdaptorException
   */
  private void toAlleleConsequences(LinkedList alleleFeatures,
      List alleleConsequencesOutput) throws AdaptorException {
    while (alleleFeatures.size() > 0) {

      final AlleleFeature alleleFeature = (AlleleFeature) alleleFeatures
          .removeFirst();

      // Handle each node separately (usually alleleFeatures.getNodeCount()==1)
      for (Location alleleLoc = alleleFeature.getLocation(); alleleLoc != null; alleleLoc = alleleLoc
          .next()) {
        final int alleleLen = alleleLoc.getLength();
        final int alleleStart = alleleLoc.getStart();
        final int alleleEnd = alleleLoc.getEnd();

        if (!transcriptLoc.getCoordinateSystem().equals(
            alleleLoc.getCoordinateSystem())
            || !transcriptLoc.getSeqRegionName().equals(
                alleleLoc.getSeqRegionName()))
          continue;

        if (alleleEnd < transcriptStart) {

          // Allele is 'before' transcript in genomic coordinates
          alleleConsequencesOutput.add(new AlleleConsequence(alleleLoc.copy(),
              forward ? AlleleConsequence.UPSTREAM
                  : AlleleConsequence.DOWNSTREAM, null));

        } else if (alleleStart > transcriptEnd) {

          // Allele is 'after' transcript in genomic coordinates
          alleleConsequencesOutput.add(new AlleleConsequence(alleleLoc.copy(),
              forward ? AlleleConsequence.DOWNSTREAM
                  : AlleleConsequence.UPSTREAM, null));

        } else if (alleleStart < transcriptEnd && alleleEnd > transcriptStart
            && hasTranslation) {

          // Allele overlaps transcript

          boolean mapped = hasFivePrimeUTR
              && fivePrimeRange.overlaps(alleleStart, alleleEnd)
              && map2FivePrimeUTR(alleleConsequencesOutput, alleleLoc,
                  alleleLen, alleleStart, alleleEnd);

          if (!mapped && hasCoding
              && codingRange.overlaps(alleleStart, alleleEnd))
            mapped = map2Coding(alleleFeature, alleleConsequencesOutput);

          if (!mapped && hasThreePrimeUTR
              && threePrimeRange.overlaps(alleleStart, alleleEnd))
            mapped = map2ThreePrimeUTR(alleleConsequencesOutput, alleleLoc,
                alleleLen);

          if (!mapped)
            alleleConsequencesOutput
                .add(createIntronAlleleConsequence(alleleLoc));

        }
      }
    }
  }
}