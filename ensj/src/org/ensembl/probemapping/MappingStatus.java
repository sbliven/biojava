/*
 Copyright (C) 2003 EBI, GRL

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

package org.ensembl.probemapping;

import java.util.ArrayList;
import java.util.List;

import org.ensembl.datamodel.OligoArray;

/**
 * Stores the status of the mapping between a microarray ProbeSet (composite)
 * and a Transcript.
 *
 * The toString() method returns a string suitable for entry in a mapping log/audit
 * file.
 * 
 * @see org.ensembl.probemapping.MappableOligoFeature
 * @see org.ensembl.probemapping.MappableTranscript
 * 
 */
public class MappingStatus {

	public final ProbeSet probeSet;

	public final MappableTranscript transcript;

	public final int exonFlankHitCount;

	public final int intronHitCount;

	public final int reverseStrandHitCount;

	public final int exonFlankHitThreshold;
	
	/**
	 * ExternalRefs in an ensembl database corresponding to
	 * the probeset. One per array the probeset appears in.
	 * @see org.ensembl.datamodel.ExternalRef
	 */
	public final List xrefs = new ArrayList();

	/**
	 * Create a status for the mapping between the probeset and the transcript.
	 * 
	 * @param probeSet probeset overlapping transcript
	 * @param exonFlankHitThreshold minimum number of exonHits required to map probeset and transcript 
	 * @param transcript transcript overlapping probeSet
	 * @param exonHitCount number of probe hits in coding part of exons
	 * @param intronHitCount number of probe hits in introns
	 * @param reverseStrandHitCount number of probe hits on reverse strand opposite transcript
	 */
	public MappingStatus(ProbeSet probeSet,
			int exonFlankHitThreshold, MappableTranscript transcript,
			int exonHitCount, int intronHitCount, int reverseStrandHitCount) {
		super();
		this.probeSet = probeSet;
		this.exonFlankHitThreshold = exonFlankHitThreshold;
		this.transcript = transcript;
		this.exonFlankHitCount = exonHitCount;
		this.intronHitCount = intronHitCount;
		this.reverseStrandHitCount = reverseStrandHitCount;
	}


	/**
	 * @return true if the composite is mapped to the transcript, otherwise
	 *         false.
	 */
	public boolean isMapped() {
		return !probeSet.tooManyTranscripts
				&& exonFlankHitCount >= exonFlankHitThreshold;
	}

	/**
	 * Return formated string representing this object suitable for outputting
	 * to mapping log file.
	 * 
	 * <pre>
	 * 
	 *  STRING = COMPOSITE_NAME	TRANSCRIPT_STABLE_ID    MAPPED_STATE     STATS   DESCRIPTION[,DESCRIPTION]* 
	 *  MAPPED_STATE = {0 | 1} # 0 = unmapped, 1 = mapped, quick to search if loaded into db
	 *  STATS = PROBE_SET_SIZE    NUM_EXON_HITS    NUM_INTRON_HITS    NUM_HITS_ON_REVERSE_STRAND
	 *  PROBE_SET_SIZE = number of probes in the microarray(s) this composite appears in
	 *  NUM__HITS = number of times a probe from the composite hits an exon in the transcript (same strand)
	 *  NUM_INTRON = number of times a probe from the composite hits an intron in the transcript (same strand)
	 *  NUM_HITS_ON_REVERSE_STRAND = number of times a probe from the composite hits any where on the reverse strand of the extent transcript+flank
	 *  DESCRIPTION = {mapped | intronic | insufficient | antisense | promiscuous}
	 * 
	 *  e.g.
	 *  comp_at_1    ENST1    1    11    10    0    0    mapped
	 *  comp_at_2    ENST2    1    11    9     1    0    mapped,intronic
	 *  comp_at_3    ENST3    0    16    0     12   0    insufficient,intronic
	 *  comp_at_3    ENST4    0    16    0     0    10   insufficient,antisense
	 *  comp_at_4    ENST4    0    16    2     2    10   insufficient,intronic,antisense
	 *  
	 * </pre>
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {

		StringBuffer buf = new StringBuffer();

		buf.append(probeSet.probeSetName);
		buf.append("\t").append(transcript.getAccessionID());
		boolean mapped = isMapped();
		buf.append("\t").append(mapped ? "1" : "0");
		buf.append("\t").append(getProbeSetSize());
		buf.append("\t").append(exonFlankHitCount);
		buf.append("\t").append(intronHitCount);
		buf.append("\t").append(reverseStrandHitCount);

		// more human readable description
		buf.append("\t");
		if (mapped)
			buf.append("mapped");
		else
			buf.append("insufficient");
		if (intronHitCount > 0)
			buf.append(",").append("intronic");
		if (reverseStrandHitCount > 0)
			buf.append(",").append("antisense");
		if (probeSet.tooManyTranscripts)
			buf.append(",").append("promiscuous");

		return buf.toString();
	}

	public int getProbeSetSize() {
		return ((OligoArray) probeSet.getOligoArrays().get(0)).getProbeSetSize();
	}

}
