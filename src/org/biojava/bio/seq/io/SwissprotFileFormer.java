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

import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.Feature;

import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;

/**
 * Formats a sequence into Swissprot/TrEMBL format.  Modeled after
 * EmblFileFormer.
 *
 * @author Greg Cox
 * @since 1.2
 */
public class SwissprotFileFormer implements SeqFileFormer
{
// Static variables
	static int LOCATION_WIDTH = 6;

// Member variables
	PrintStream mStream;

// Constructors and initialization

    // Registers this format with the factory
	static
	{
		SeqFileFormerFactory.addFactory("Swissprot", new SwissprotFileFormer.Factory());
	}

	private static class Factory extends SeqFileFormerFactory
	{
		protected SeqFileFormer make()
		{
			return new SwissprotFileFormer(System.out);
		}
	}

	/**
	 * Creates a new <code>EmblFileFormer</code> object. Instances are
	 * made by the <code>Factory</code>.
	 *
	 * @param theStream a <code>PrintStream</code> object.
	 */
	private SwissprotFileFormer(final PrintStream theStream)
	{
		this.mStream = theStream;
	}

// Interface implementations
	// SeqIOListener methods
	/**
	 * Start the processing of a sequence.  This method exists primarily
	 * to enforce the life-cycles of SeqIOListener objects.
	 */

	public void startSequence() throws ParseException
	{
	}

	/**
	 * Notify the listener that processing of the sequence is complete.
	 */

	public void endSequence() throws ParseException
	{
	}

	/**
	 * Null implementation.  This object formats and prints a sequence.  The
	 * name alone cannot be printed in Swissprot format.  Therefore, it's
	 * easiest to ignore it.
	 *
	 * @param the String that should be returned by getName for the sequence
	 * being parsed
	 */

	public void setName(String theName) throws ParseException
	{
	}

	/**
	 * Null implementation.  This object formats and prints a sequence.  The
	 * URI alone cannot be printed in Swissprot format.  Therefore, it's
	 * easiest to ignore it.	 *
	 * @param the new URI of the sequence
	 */

	public void setURI(String theURI) throws ParseException
	{
	}

	/**
	 * Prints out the symbol array passed in in lines of 60, blocks of 10.
	 * The SQ header line isn't printed out.
	 *
	 * @param theAlphabet The alphabet of the symbol data
	 * @param theSymbols An array containing symbols
	 * @param theStart The start offset of valid data within the array
	 * @param theLength The number of valid symbols in the array
	 *
	 * @throws IllegalAlphabetException if we can't cope with this
	 *                                  alphabet.
	 */

	public void addSymbols(Alphabet theAlphabet,
						   Symbol[] theSymbols,
						   int theStart,
						   int theLength)
		throws IllegalAlphabetException
	{
		// Hook for when the header line is implemented.  Currently null.
		this.printOutSequenceHeaderLine(theAlphabet, theSymbols,
										theStart, theLength);

		// Get newline character
		String newLine = System.getProperty("line.separator");
		List brokenLines = this.breakSymbolArray(theAlphabet, theSymbols,
				theStart, theLength);

		java.util.Iterator iterator = brokenLines.iterator();
		String leader = "     ";
		while(iterator.hasNext())
		{
			this.getPrintStream().print(leader + iterator.next() + newLine);
		}
		this.getPrintStream().println("//");
	}

	/**
	 * Notify the listener of a sequence-wide property.  This might
	 * be stored as an entry in the sequence's annotation bundle.
	 * Null implementation.  Pending a uniform vocabulary, this is tilting at
	 * windmills.
	 *
	 * @param theKey Key the property will be stored under
	 * @param theValue Value stored under the key
	 */

	public void addSequenceProperty(Object theKey, Object theValue) throws ParseException
	{
	}

	/**
	 * Null implementation.
	 *
	 * @param theTemplate The template for this new feature object
	 */

	public void startFeature(Feature.Template theTemplate) throws ParseException
	{
	}

	/**
	 * Null implementation.
	 */
	public void endFeature() throws ParseException
	{
	}

	/**
	 * Null implementation
	 *
	 * @param theKey Key the property will be stored under
	 * @param theValue Value stored under the key
	 */

	public void addFeatureProperty(Object theKey, Object theValue) throws ParseException
	{
	}

	// SeqFileFormer methods
	/**
	 * <code>getPrintStream</code> returns the
	 * <code>PrintStream</code> to which an instance of SwissprotFileFormer
	 * will write the formatted data. The default is System.out
	 *
	 * @return the <code>PrintStream</code> which will be written to.
	 */
	public PrintStream getPrintStream()
	{
		return(this.mStream);
	}

	/**
	 * <code>setPrintStream</code> informs an instance which
	 * <code>PrintStream</code> to use.
	 *
	 * @param stream a <code>PrintStream</code> to write to.
	 */
	public void setPrintStream(PrintStream theStream)
	{
		this.mStream = theStream;
	}

	/**
	 * <code>formatLocation</code> creates a String representation of
	 * a <code>Location</code>. Strand information is ignored, as Swissprot
	 * files represent proteins. An alternative form of this function does not
	 * take a Strand; that form is available only on SwissprotFileFormer; it
	 * is not part of the SeqFileFormer interface.
	 *
	 * @param theBuffer a <code>StringBuffer</code> to append the location
	 * to.
	 * @param theLocation a <code>Location</code> to format.
	 * @param theStrand a <code>StrandedFeature.Strand</code> indicating nothing
	 * of relevance
	 *
	 * @return a <code>StringBuffer</code> with the location appended.
	 */
	public StringBuffer formatLocation(final StringBuffer theBuffer,
									   final Location theLocation,
									   final StrandedFeature.Strand theStrand)
	{
		return(this.formatLocation(theBuffer, theLocation));
	}

	/**
	 * Creates a string representation of the location of a feature
	 *
	 * @param theFeature The feature with the location to format
	 * @return String The formatted location
	 */
	public String formatLocation(Feature theFeature)
	{
		StringBuffer toReturn = this.formatLocation(new StringBuffer(), theFeature.getLocation());
		return toReturn.toString();
	}

// Public methods
	/**
	 * <code>formatLocation</code> creates a String representation of
	 * a <code>Location</code>. The stringbuffer returned represents columns
	 * 15-27 of the Swissprot feature table entry. An alternative form of this
	 * function takes a Strand; that form is part of the SeqFileFormer
	 * interface.
	 *
	 * @param theBuffer a <code>StringBuffer</code> to append the location
	 * to.
	 * @param theLocation a <code>Location</code> to format.
	 *
	 * @return a <code>StringBuffer</code> with the location appended.
	 */
	public StringBuffer formatLocation(final StringBuffer theBuffer,
									   final Location theLocation)
	{
		// Five Location cases, each treated seperately:
		//   Point Location: "     5      5"
		//   Range Location: "     5     10"
		//   Fuzzy Location: "    <5     10"
		//   Fuzzy Location: "     ?     10"
		//   Fuzzy Location: "   ?24     35" (Not in the current
		//       specification, but used anyways
		StringBuffer startPoint = new StringBuffer(LOCATION_WIDTH);
		StringBuffer endPoint   = new StringBuffer(LOCATION_WIDTH);
		if((theLocation instanceof PointLocation) ||
			(theLocation instanceof RangeLocation))
		{
			//   Point Location: "     5      5"
			//   Range Location: "     5     10"
			startPoint = formatPoint(theLocation.getMin(), theLocation.getMin(), false);
			endPoint = formatPoint(theLocation.getMax(), theLocation.getMax(), false);
		}
		else if(theLocation instanceof FuzzyLocation)
		{
			// Handle all fuzzy location types through the magic of delegation.
			// If you pass things around long enough, someone's bound to do it
			// for you
			FuzzyLocation tempLocation = (FuzzyLocation)theLocation;
//System.out.println("OuterMin: " + tempLocation.getOuterMin());
//System.out.println("InnerMin: " + tempLocation.getInnerMin());
//System.out.println("InnerMax: " + tempLocation.getInnerMax());
//System.out.println("OuterMax: " + tempLocation.getOuterMax());
			startPoint = this.formatPoint(tempLocation.getOuterMin(),
					tempLocation.getInnerMin(), tempLocation.isMinFuzzy());
			endPoint = this.formatPoint(tempLocation.getInnerMax(),
					tempLocation.getOuterMax(), tempLocation.isMaxFuzzy());
		}

		return new StringBuffer(startPoint.toString() + " " + endPoint.toString());
	}

// Protected methods
	/**
	 * Null implementation.  A hook for printing out the header SQ line.
	 *
	 * @param theAlphabet The alphabet of the symbol data
	 * @param theSymbols An array containing symbols
	 * @param theStart The start offset of valid data within the array
	 * @param theLength The number of valid symbols in the array
	 *
	 * @throws IllegalAlphabetException if we can't cope with this
	 *                                  alphabet.
	 */
	protected void printOutSequenceHeaderLine(Alphabet theAlphabet,
											  Symbol[] theSymbols,
											  int theStart,
											  int theLength)
		throws IllegalAlphabetException
	{
	}

	/**
	 * Converts the symbol list passed in into an array of strings.  The
	 * strings will be blocks of ten, with six blocks on a line.
	 *
	 * @param theAlphabet The alphabet of the symbol data
	 * @param theSymbols An array containing symbols
	 * @param theStart The start offset of valid data within the array
	 * @param theLength The number of valid symbols in the array
	 * @return The symbol list passed in broken into blocks of ten
	 * characters, six to a string.
	 *
	 * @throws IllegalAlphabetException if we can't cope with this
	 *                                  alphabet.
	 */
	protected List breakSymbolArray(Alphabet theAlphabet,
									Symbol[] theSymbols,
									int theStart,
									int theLength)
		throws IllegalAlphabetException
	{
		List returnList = new ArrayList(theLength / 60 + 1);
		int blockCount = 0;
		int blockIndex = 0;
		StringBuffer tempString = new StringBuffer();
		for(int i = theStart; i < theStart + theLength; i++)
		{
			try
			{
				theAlphabet.validate(theSymbols[i]);
			}
			catch (IllegalSymbolException e)
			{
				throw new IllegalAlphabetException(e);
			}

			// Every six completed blocks, put on the stack to return
			if(blockIndex == 10)
			{
				tempString.append(' ');
				blockIndex = 0;
				blockCount++;
			}

			if(blockCount == 6)
			{
				returnList.add(tempString.toString());
				tempString.setLength(0);
				blockCount = 0;
				blockIndex = 0;
			}
			tempString.append(theSymbols[i].getToken());
			blockIndex++;
		}

		// Add the last line on
		if(tempString.length() != 0)
		{
			returnList.add(tempString.toString());
		}
		return returnList;
	}

	/**
	 * Simple method that adds spaces onto the buffer passed in.  This method
	 * exists to refactor some code used in location formatting.  It isn't
	 * intended to be generally used.
	 *
	 * @param theBuffer Buffer to append whitespace to.
	 * @param theLength Ammount of whitespace to append.
	 */
	protected void fillBuffer(StringBuffer theBuffer, int theLength)
	{
		for(int i = 0; i < theLength; i++)
		{
			theBuffer.append(' ');
		}
	}

	/**
	 * Formats the points from fuzzy locations.  This is called easily with
	 * this.formatPoint(FuzzyLocation.getInnerMax(), FuzzyLocation.getOuterMax(), FuzzyLocation.isFuzzyMax())
	 *
	 * @param theMaxIndex Inner index of the fuzzy point
	 * @param theMinIndex Outer index of the fuzzy point
	 * @param isFuzzy Indicates if this point is fuzzy
	 */
	protected StringBuffer formatPoint(int theMinIndex, int theMaxIndex, boolean isFuzzy)
	{
		StringBuffer bufferToReturn = new StringBuffer(LOCATION_WIDTH);
		if(isFuzzy == false)
		{
			String tempString = Integer.toString(theMinIndex);
			int offset = LOCATION_WIDTH - tempString.length();
			this.fillBuffer(bufferToReturn, offset);
			bufferToReturn.append(tempString);
		}
		else
		{
			// MIN_VALUE to MAX_VALUE is the ? location regardless of which end is which
			if((theMinIndex == Integer.MIN_VALUE) && (theMaxIndex == Integer.MAX_VALUE))
			{
				int offset = LOCATION_WIDTH - 1;
				this.fillBuffer(bufferToReturn, offset);
				bufferToReturn.append('?');
			}
			// If the outer index is MIN_VALUE, that's <n
			else if(theMinIndex == Integer.MIN_VALUE)
			{
				String tempString = Integer.toString(theMaxIndex);
				int offset = LOCATION_WIDTH - tempString.length() - 1;
				this.fillBuffer(bufferToReturn, offset);
				bufferToReturn.append('<');
				bufferToReturn.append(tempString);
			}
			// If the outer index is MAX_VALUE, that's >n
			else if(theMaxIndex == Integer.MAX_VALUE)
			{
				String tempString = Integer.toString(theMinIndex);
				int offset = LOCATION_WIDTH - tempString.length() - 1;
				this.fillBuffer(bufferToReturn, offset);
				bufferToReturn.append('>');
				bufferToReturn.append(tempString);
			}
			// The only swissprot location left is ?nn
			else if(theMinIndex == theMaxIndex)
			{
				String tempString = Integer.toString(theMinIndex);
				int offset = LOCATION_WIDTH - tempString.length() - 1;
				this.fillBuffer(bufferToReturn, offset);
				bufferToReturn.append('?');
				bufferToReturn.append(tempString);
			}
			else
			{
				// The location cannot be formatted in Swissprot format
// Revisit
System.out.println("Error in formatPoint");
System.out.println("\tInner: " + theMinIndex);
System.out.println("\tOuter: " + theMaxIndex);
System.out.println("\tFuzzy: " + isFuzzy);
			}
		}
		return bufferToReturn;
	}

// Private methods
}
