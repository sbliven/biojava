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

package org.ensembl.datamodel;

/**
 * Represents a marker feature in the EnsEMBL database.  A marker
 * feature is the location where a marker has been mapped to the
 * genome by ePCR.
 *
 * @see org.ensembl.datamodel.Marker
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface MarkerFeature extends Feature {

    Marker getMarker();
    void setMarker(Marker marker);

    /**
     * The number of times that this marker has been mapped to the
     * genome.  E.g.  a marker iwth map weight 3 has been mapped to 3
     * locations in the genome.
     *
     * @return number of times that this marker has been mapped to the
     * genome.
     */
    int getMapWeight();

    /**
     * The number of times that this marker has been mapped to the
     * genome.  E.g.  a marker iwth map weight 3 has been mapped to 3
     * locations in the genome.
     *
     * @param mapWeight number of times that this marker has been mapped to the
     * genome.
     */
    void setMapWeight(int mapWeight);

    /**
     * This method returns a string that is considered to be the
     * 'display' identifier.  For marker features this is the name of
     * the display synonym or '' if it is not defined.
     * @return displayID for this marker
     */
    String getDisplayName();
    void setDisplayName(String displayID);

}
