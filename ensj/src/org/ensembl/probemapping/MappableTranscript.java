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

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.impl.BaseFeatureImpl;

/**
 * A representation of a transcript that includes it's internalID and a location
 * that specifies the coding sequence with a downstream flank.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class MappableTranscript extends BaseFeatureImpl {

  private static final long serialVersionUID = 1L;

  private final String accessionID;

  private Location cDNALocation;

  /**
   * @param transcript
   *          source transcript
   * @param downStreamFlank
   *          size of down stream flanking region in bases, >=0
   */
  public MappableTranscript(Transcript transcript, int downStreamFlank) {

    internalID = transcript.getInternalID();
    accessionID = transcript.getAccessionID();

    // create locations flanks at the 3' end
    location = transcript.getLocation().transform(0, downStreamFlank);
    cDNALocation = transcript.getCDNALocation().transform(0, downStreamFlank);
  }

  /**
   * Location that extends from the start of the first exon in the transcript to
   * the end of the flanking region after the last exon.
   *  
   * @return transcript extent + flank location.
   */
  public Location getLocation() {
    return location;
  }

  /**
   * ID of the transcript this instance represents.
   * 
   * @return transcript's internal ID
   */
  public long getInternalID() {
    return internalID;
  }

  /**
   * Accession of the transcript this instance represents.
   * 
   * @return transcript's accession.
   */
  public String getAccessionID() {
    return accessionID;
  }

  /**
   * The CDNA location of the transcript includes all the exon locations + a
   * flank location attatched to the downstream end of the last transcript.
   * 
   * The CDNA location is stranded.
   * 
   * @return cdna location + flank
   */
  public Location getCDNALocation() {
    return cDNALocation;
  }

  public String toString() {
    return accessionID + "\t"
    + location.toString();
  }
  
}
