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

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.*;

/**
 * <code>EmblLikeLocationParser</code> parses EMBL/Genbank style
 * locations. Supported location forms:
 *
 * <pre>
 *   123
 *  <123 or >123
 *  (123.567)
 *  (123.567)..789
 *   123..(567.789)
 *  (123.345)..(567.789)
 *   123..456
 *  <123..567 or 123..>567 or <123..>567
 *   123^567
 * </pre>
 *
 * Specifically not supported are:
 * <pre>
 *   AL123465:(123..567)
 * </pre>
 *
 * Use of 'order' rather than 'join' is not retained over a read/write
 * cycle. i.e. 'order' is converted to 'join'
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
class EmblLikeLocationParser
{
    // For the LocationLexer inner classs
    private String        location;
    private LocationLexer lexer;
    private int           nextCharIndex;
    private Object        thisToken;

    // Stores join/order/complement instructions
    private List instructStack = new ArrayList();
    // List of sublocations
    private List  subLocations = new ArrayList();

    // These hold working data for each (sub)location and are cleared
    // by calling the processCoords() function
    private List   startCoords = new ArrayList();
    private List     endCoords = new ArrayList();
    private boolean isPointLoc = true;
    private boolean fuzzyCoord = false;
    private boolean unboundMin = false;
    private boolean unboundMax = false;
    private boolean isBetweenLocation = false;

    // Currently set per Feature; this is a deficiency in the current
    // parser
    // Features are assumed to be on the positive strand until complemented
    // No features have a strand type of UNKNOWN
    private StrandedFeature.Strand mStrandType = StrandedFeature.POSITIVE;

    EmblLikeLocationParser()
    {
	this.lexer = new LocationLexer();
    }

    /**
     * <code>parseLocation</code> creates a <code>Location</code> from
     * the String and returns an array of Objects. The first element
     * is the <code>Location</code>, the second is a Boolean value
     * indicating whether it is on the complementary strand. A true
     * value indicates complement.
     *
     * @param location a location <code>String</code>.
     *
     * @return a StrandedLocation with the location and strand information
     * from the string
     *
     * @exception BioException if an error occurs.
     */
    StrandedLocation parseLocation(final String location)
		throws BioException
    {
		this.location = location;

		if ((countChar(location, '(')) != (countChar(location, ')')))
	    	throw new BioException("Unbalanced parentheses in location: "
				   + location);

		nextCharIndex = 0;

		instructStack.clear();
		subLocations.clear();

		thisToken = lexer.getNextToken();
		while (thisToken != null)
		{
		    if (String.class.isInstance(thisToken))
		    {
				String toke = (String) thisToken;
				if (toke.equals(".."))
				{
			    	// This token indicates that this isn't a point
			    	isPointLoc = false;
				}
				else
				{
				    instructStack.add(thisToken);
				}
		    }
		    else if (Integer.class.isInstance(thisToken))
		    {
				if (isPointLoc)
			    	startCoords.add(thisToken);
				else
				    endCoords.add(thisToken);
		    }
		    else if (Character.class.isInstance(thisToken))
		    {
				char toke = ((Character) thisToken).charValue();

				switch (toke)
				{
			    	case '(': case ':':
						break;

				    case '^':
						isBetweenLocation = true;

				    case '<':
						unboundMin = true;
						break;

				    case '>':
						unboundMax = true;
						break;

				    case '.':
						// Catch range: (123.567)
						fuzzyCoord = true;
						break;

				    case ',':
						processCoords();
						break;

				    case ')':
						// Catch the end of range: (123.567)
						if (fuzzyCoord)
						{
						    fuzzyCoord = false;
						}
						else
						{
						    processCoords();
						    processInstructs();
						}
						break;

				    default:
						throw new BioException("Unknown character '"
							       + toke
							       + "' within location: "
							       + location);
				}
		    }
		    thisToken = lexer.getNextToken();
		}
		processCoords();

		StrandedLocation returnLocation;
		if (subLocations.size() == 1)
		{
		    returnLocation = new StrandedLocation((Location)subLocations.get(0),
					   mStrandType);
		}
		else
		{
		    // EMBL ordering is in reverse on the complementary strand
		    // but LocationTools sorts them anyway
		    returnLocation = new StrandedLocation(LocationTools.union(subLocations),
					   mStrandType);
		}
		return returnLocation;
    }

    /**
     * <code>processCoords</code> uses the coordinate data in the
     * start/endCoords Lists to create a Location and add to the
     * subLocations List. As this code will require further
     * modification to support fuzzy point locations, please keep any
     * changes well-commented.
     *
     * @exception BioException if an error occurs.
     */
    private void processCoords()
	throws BioException
    {
	int outerMin, innerMin, innerMax, outerMax;

	// This is expected where two calls to processCoords() are
	// made sequentially e.g. where two levels of parens are
	// closed. The second call will have no data to process.
	if (startCoords.isEmpty() && endCoords.isEmpty())
	    return;

	// Range of form 5^6 or 5^7
	if (isBetweenLocation)
	{
		// Create a ranged location, and wrap it in a between location
		int minCoord = ((Integer)startCoords.get(0)).intValue();
		int maxCoord = ((Integer)startCoords.get(1)).intValue();
		subLocations.add(new BetweenLocation(new RangeLocation(minCoord, maxCoord)));
	}
	// Range of form: 123
	else if (startCoords.size() == 1 && endCoords.isEmpty())
	{
	    innerMin = outerMin = ((Integer) startCoords.get(0)).intValue();
	    innerMax = outerMax = innerMin;

	    // This looks like a point, but is actually a range which
	    // lies entirely outside the current entry
	    if (unboundMin || unboundMax)
	    {
		subLocations.add(new FuzzyPointLocation(unboundMin ? Integer.MIN_VALUE : innerMin,
							unboundMax ? Integer.MAX_VALUE : innerMax,
							FuzzyPointLocation.RESOLVE_AVERAGE));
	    }
	    else if (isPointLoc)
	    {
		subLocations.add(new PointLocation(innerMin));
	    }
	    else
	    {
		// I'm really sorry about this exception message! This
		// should not happen
		throw new BioException("Internal error in location parsing; parser became confused: "
				       + location);
	    }
	}
	// Range of form: (123.567)
	else if (startCoords.size() == 2 && endCoords.isEmpty())
	{
	    innerMin = outerMin = ((Integer) startCoords.get(0)).intValue();
	    innerMax = outerMax = ((Integer) startCoords.get(1)).intValue();

	    subLocations.add(new FuzzyPointLocation(innerMin,
						    innerMax,
						    FuzzyPointLocation.RESOLVE_AVERAGE));
	}
	// Range of form: 123..567 or <123..567 or 123..>567 or <123..>567
	else if (startCoords.size() == 1 && endCoords.size() == 1)
	{
	    innerMin = outerMin = ((Integer) startCoords.get(0)).intValue();
	    innerMax = outerMax = ((Integer) endCoords.get(0)).intValue();

	    if (unboundMin || unboundMax)
	    {
		subLocations.add(new FuzzyLocation(unboundMin ? Integer.MIN_VALUE : outerMin,
						   unboundMax ? Integer.MAX_VALUE : outerMax,
						   innerMin,
						   innerMax,
						   FuzzyLocation.RESOLVE_INNER));
	    }
	    else
	    {
		try
		{
		    subLocations.add(new RangeLocation(outerMin, outerMax));
		}
		catch (IndexOutOfBoundsException ioe)
		{
		    throw new BioException(ioe);
		}
	    }
	}
	// Range of form: (123.567)..789
	else if (startCoords.size() == 2 && endCoords.size() == 1)
	{
	    outerMin = ((Integer) startCoords.get(0)).intValue();
	    innerMin = ((Integer) startCoords.get(1)).intValue();
	    innerMax = outerMax = ((Integer) endCoords.get(0)).intValue();

	    subLocations.add(new FuzzyLocation(outerMin,
					       outerMax,
					       innerMin,
					       innerMax,
					       FuzzyLocation.RESOLVE_INNER));
	}
	// Range of form: 123..(567.789)
	else if (startCoords.size() == 1 && endCoords.size() == 2)
	{
	    outerMin = innerMin = ((Integer) startCoords.get(0)).intValue();
	    innerMax = ((Integer) endCoords.get(0)).intValue();
	    outerMax = ((Integer) endCoords.get(1)).intValue();

	    subLocations.add(new FuzzyLocation(outerMin,
					       outerMax,
					       innerMin,
					       innerMax,
					       FuzzyLocation.RESOLVE_INNER));
	}
	// Range of form: (123.345)..(567.789)
	else if (startCoords.size() == 2 && endCoords.size() == 2)
	{
	    outerMin = ((Integer) startCoords.get(0)).intValue();
	    innerMin = ((Integer) startCoords.get(1)).intValue();
	    innerMax = ((Integer) endCoords.get(0)).intValue();
	    outerMax = ((Integer) endCoords.get(1)).intValue();

	    subLocations.add(new FuzzyLocation(outerMin,
					       outerMax,
					       innerMin,
					       innerMax,
					       FuzzyLocation.RESOLVE_INNER));
	}
	else
	{
	    // I'm really sorry about this exception message! This
	    // should not happen
  	    throw new BioException("Internal error in location parsing; parser became confused; "
  				   + location);
	}

	startCoords.clear();
	endCoords.clear();

	isPointLoc   = true;
	unboundMin   = false;
	unboundMax   = false;
	fuzzyCoord   = false;
	isBetweenLocation = false;
	mStrandType  = StrandedFeature.POSITIVE;
    }

    /**
     * <code>processInstructs</code> pops an instruction off the stack
     * and applies it to the sub(locations).
     *
     * @exception BioException if an unsupported instruction is found.
     */
    private void processInstructs()
	throws BioException
    {
	String instruct = (String) instructStack.remove(instructStack.size() - 1);

	if (instruct.equals("join") || instruct.equals("order"))
	{
	    // This is handled implicitly by the parseLocation()
	    // return statement. However, the choice of join/order
	    // should be reported back to the parent Feature and
	    // stored in the annotation bundle.
	}
	else if (instruct.equals("complement"))
	{
	    // This should only set the strand for a single range
	    // within a feature. However, BioJava Locations have no
	    // concept of strand and therefore are unable to support
	    // construction of Features where some ranges are on
	    // different strands. As a result the mStrandType
	    // flag currently sets the strand for the whole feature.
	    mStrandType = StrandedFeature.NEGATIVE;
	}
	else
	{
	    // This is a primary accession number
	    // e.g. J00194:(100..202)
  	     throw new BioException("Remote locations are not supported: " + location);
	}
    }

    private int countChar(final String s, final char c)
    {
	int cnt = 0;
	for (int i = 0; i < s.length(); ++i)
	    if (s.charAt(i) == c)
		++cnt;
	return cnt;
    }

    /**
     * <code>LocationLexer</code> is based on the
     * <code>LocationLexer</code> class in the Artemis source code by
     * Kim Rutherford.
     *
     * @author Kim Rutherford
     * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
     * @author Greg Cox
     * @since 1.2
     */
    private class LocationLexer
    {
	/**
	 * <code>getNextToken</code> returns the next token. A null
	 * indicates no more tokens.
	 *
	 * @return an <code>Object</code> value.
	 */
	Object getNextToken()
	{
	    while (true)
	    {
		if (nextCharIndex == location.length())
		    return null;

		char thisChar = location.charAt(nextCharIndex);

		switch (thisChar)
		{
		    case ' ' : case '\t' :
			continue;

		    case ':' : case '^' : case ',' :
		    case '(' : case ')' : case '<' :
		    case '>' :
			nextCharIndex++;
			return new Character(thisChar);

		    case '.' :
			if (location.charAt(nextCharIndex + 1) == '.')
			{
			    nextCharIndex += 2;
			    return "..";
			}
			else
			{
			    nextCharIndex++;
			    return new Character('.');
			}

		    case '0' : case '1' : case '2' : case '3' : case '4' :
		    case '5' : case '6' : case '7' : case '8' : case '9' :
			return followInteger();

		    default :
			String text = followText();
			if (text.equals(""))
			{
			    nextCharIndex++;
			    return new String("" + thisChar);
			}
			else
			    return text;
		}
	    }
	}

	/**
	 * <code>followInteger</code> returns single sequence
	 * coordinate.
	 *
	 * @return an <code>Integer</code> value.
	 */
	private Integer followInteger()
	{
	    StringBuffer intString = new StringBuffer();
	    char    thisChar = location.charAt(nextCharIndex);

	    while (Character.isDigit(thisChar))
	    {
		intString.append(thisChar);
		nextCharIndex++;

		if (nextCharIndex >= location.length())
		    break;

		thisChar = location.charAt(nextCharIndex);
	    }
	    return new Integer(intString.toString());
	}

	/**
	 * <code>followText</code> returns a single text string.
	 *
	 * @return a <code>String</code> value.
	 */
	private String followText()
	{
	    StringBuffer textString = new StringBuffer("");
	    char     thisChar = location.charAt(nextCharIndex);

	    // First character must be a letter
	    if (! Character.isLetter(thisChar))
		return "";

	    while (Character.isLetterOrDigit(thisChar) ||
		   thisChar == '.')
	    {
		textString.append(thisChar);
		nextCharIndex++;

		if (nextCharIndex >= location.length())
		    break;

		thisChar = location.charAt(nextCharIndex);
	    }
	    return textString.toString();
	}
    }
}
