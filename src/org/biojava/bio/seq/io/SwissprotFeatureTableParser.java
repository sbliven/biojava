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

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.utils.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Simple parser for swissprot feature tables.
 *
 * <p>
 * This has been partially re-written for newio, but would probably
 * benefit from a few more changes.  In particular, it should notify
 * startFeature as early as possible, then use addFeatureProperty.
 * </p>
 *
 * @author Greg Cox (Based heavily off of FeatureTableParser)
 */

class SwissprotFeatureTableParser extends FeatureTableParser
{
	SwissprotFeatureTableParser(SeqIOListener listener, String source)
	{
		super(listener, source);
	}

	public void startFeature(String type) throws BioException
	{
		super.startFeature(type);
	}

	public void featureData(String line) throws BioException
	{
		boolean newFeature = false;
		// Check if there is a location section.
		if(line.charAt(5) != ' ')
		{
			StringTokenizer tokens = new StringTokenizer(line);
			super.featureLocation = this.getLocation(tokens);
//			String startLocation = tokens.nextToken();
//			boolean startIsFuzzy = false;
//			if(startLocation.indexOf('<') != -1)
//			{
//				startLocation = startLocation.substring(1);
//				startIsFuzzy = true;
//			}
//			Integer startIndex = new Integer(startLocation);
//
//			String endLocation = tokens.nextToken();
//			boolean endIsFuzzy = false;
//			if(endLocation.indexOf('<') != -1)
//			{
//				endLocation = endLocation.substring(1);
//				endIsFuzzy = true;
//			}
//			Integer endIndex = new Integer(endLocation);
//
//			Location theLocation;
//			if(endIndex.equals(startIndex))
//			{
//				theLocation = new PointLocation(startIndex.intValue());
//			}
//			else
//			{
//				theLocation = new RangeLocation(startIndex.intValue(), endIndex.intValue());
//			}
//
//			if(startIsFuzzy || endIsFuzzy)
//			{
//				theLocation = new FuzzyLocation(theLocation, startIsFuzzy, endIsFuzzy);
//			}
//
//			super.featureLocation = theLocation;

			if(line.length() >= 20)
			{
				line = line.substring(20);
			}
			else
			{
				line = "";
			}
			newFeature = true;
		}

		if(newFeature == true)
		{
			featureBuf.setLength(0);
		}
		featureBuf.append(" " + line.trim());
		newFeature = false;
	}

	public void endFeature()
	throws BioException
	{
		featureAttributes.put(featureBuf.toString(), "");
		super.endFeature();
	}

	protected Feature.Template buildFeatureTemplate(String type,
							Location loc,
						    StrandedFeature.Strand strandHint,
						    String source,
							Map attrs)
	{
		Feature.Template t = new Feature.Template();
		t.annotation = new SimpleAnnotation();
		for (Iterator i = attrs.entrySet().iterator(); i.hasNext(); )
		{
			Map.Entry e = (Map.Entry) i.next();
			try
			{
				t.annotation.setProperty(e.getKey(), e.getValue());
			}
			catch (ChangeVetoException cve)
			{
				throw new BioError(cve,
						"Assertion Failure: Couldn't set up the annotation");
	    	}
		}

		t.location = loc;
		t.type = type;
		t.source = source;

		return t;
	}

	public boolean inFeature()
	{
		return super.inFeature();
	}

	/**
	 * Returns the next location contained in theTokens
	 *
	 * @exception bioException Thrown if a non-location is first in theTokens
	 * @param theTokens The tokens to process
	 * @return The location at the front of theTokens
	 */
	private Location getLocation(StringTokenizer theTokens)
		throws BioException
	{
		Index startIndex = this.getIndex(theTokens);
		Index endIndex = this.getIndex(theTokens);
		Integer startPoint = startIndex.point;
		Integer endPoint = endIndex.point;
		boolean startIsFuzzy = startIndex.isFuzzy;
		boolean endIsFuzzy = endIndex.isFuzzy;

		Location theLocation;
		if(endPoint.equals(startPoint))
		{
			theLocation = new PointLocation(startPoint.intValue());
		}
		else
		{
			theLocation = new RangeLocation(startPoint.intValue(), endPoint.intValue());
		}

		if(startIsFuzzy || endIsFuzzy)
		{
			theLocation = new FuzzyLocation(theLocation, startIsFuzzy, endIsFuzzy);
		}

		return theLocation;
	}

	/**
	 * Returns the Integer value of the next token and its fuzzyness
	 *
	 * @exception BioException Thrown if a non-number token is passed in
	 * (fuzzy locations are handled)
	 * @param theTokens The tokens to be processed
	 * @return Index The integer in the next token and if it is a fuzzy integer
	 */
	private Index getIndex(StringTokenizer theTokens)
		throws BioException
	{
		String returnIndex = theTokens.nextToken();
		boolean indexIsFuzzy = false;
		if((returnIndex.indexOf('<') != -1) && (returnIndex.indexOf('>') != -1))
		{
			returnIndex = returnIndex.substring(1);
			indexIsFuzzy = true;
		}

		Index returnValue;
		try
		{
			returnValue = new Index(new Integer(returnIndex), indexIsFuzzy);
		}
		catch (NumberFormatException ex)
		{
			throw new BioException("bad locator: " + returnIndex);
		}
		return returnValue;
	}

	/**
	 * This inner class is a struct so that a boolean and an Int can be passed
	 * around in location parsing
	 */
	private class Index
	{
		public boolean isFuzzy;
		public Integer point;

		public Index(Integer thePoint, boolean theFuzzyness)
		{
			point = thePoint;
			isFuzzy = theFuzzyness;
		}
	}
}
