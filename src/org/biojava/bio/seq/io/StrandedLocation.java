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

package org.biojava.bio.seq.io;

import org.biojava.bio.symbol.Location;
import org.biojava.bio.seq.StrandedFeature;

/**
 * This is a helper class that contains a location and strand information.
 * It gets passed back from EmblLikeLocationParser.parseLocation instead of
 * an array of Objects.
 *
 * @author Greg Cox
 */
class StrandedLocation
{
// Static variables

// Member variables
	Location mLocation;
	StrandedFeature.Strand mStrandType;

// Constructors and initialization
	/**
	 * Creates a stranded location with the given location and strand type.
	 * Semantics of location on the 5'-3' strand vrs 3'-5' strand are not
	 * addressed.  This is only to hold the data; it does not interpert it.
	 *
	 * @param theLocation The location of the stranded location
	 * @param theStrandType The strand type of the location.
	 */
	StrandedLocation(Location theLocation,
			StrandedFeature.Strand theStrandType)
	{
		this.mLocation = theLocation;
		this.mStrandType = theStrandType;
	}

// Interface implementations

// Public methods

// Protected methods
	/**
	 * Gets the location of this stranded location
	 *
	 * @return The location
	 */
	Location getLocation()
	{
		return this.mLocation;
	}

	/**
	 * Gets the strand type of this stranded location
	 *
	 * @return The strand type
	 */
	StrandedFeature.Strand getStrandType()
	{
		return this.mStrandType;
	}

// Private methods
}
