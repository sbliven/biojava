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
 * @author Greg Cox (Based heavily off of FeatureTableParser)
 * @author Thomas Down
 */

/*
 * Thomas Down refactored to remove dependancy on FeatureTableParser, since we
 *             weren't actually reusing very much code, and FeatureTableParser
 *             changed quite a bit when it went fully-newio.
 */

class SwissprotFeatureTableParser
{
    private SeqIOListener listener;
    private String featureSource;

    private boolean inFeature = false;
    private Feature.Template featureTemplate;
    private StringBuffer descBuf;

    {
	descBuf = new StringBuffer();
    }

    SwissprotFeatureTableParser(SeqIOListener listener, String source)
    {
	this.listener = listener;
	this.featureSource = source;
    }

    public void startFeature(String type)
	throws BioException
    {
	featureTemplate = new Feature.Template();
	featureTemplate.source = featureSource;
	featureTemplate.type = type;
	descBuf.setLength(0);
	inFeature = true;
    }

	public void featureData(String line) 
	    throws BioException
	{
		boolean newFeature = false;
		// Check if there is a location section.
		if(line.charAt(5) != ' ')
		{
			StringTokenizer tokens = new StringTokenizer(line);
			featureTemplate.location = getLocation(tokens);

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
			descBuf.setLength(0);
		}
		descBuf.append(" " + line.trim());
		newFeature = false;
	}

	public void endFeature()
	    throws BioException
	{
	    if (descBuf.length() > 0) {
		featureTemplate.annotation = new SimpleAnnotation();
		try {
		    featureTemplate.annotation.setProperty(SwissprotProcessor.PROPERTY_SWISSPROT_FEATUREATTRIBUTE, descBuf.toString());
		} catch (ChangeVetoException ex) {
		    throw new BioException(ex, "Couldn't alter annotation");
		}
	    } else {
		featureTemplate.annotation = Annotation.EMPTY_ANNOTATION;
	    }

	    listener.startFeature(featureTemplate);
	    listener.endFeature();
	    
	    inFeature = false;
	}

        public boolean inFeature()
	{
	    return inFeature;
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
		if(startIsFuzzy || endIsFuzzy) {
		    theLocation = new FuzzyLocation(startIsFuzzy ? Integer.MIN_VALUE : startPoint.intValue(),
						    endIsFuzzy ? Integer.MAX_VALUE : endPoint.intValue(),
						    startPoint.intValue(),
						    endPoint.intValue(),
						    FuzzyLocation.RESOLVE_INNER);
		} else {
		    if(endPoint.equals(startPoint))
			{
			    theLocation = new PointLocation(startPoint.intValue());
			}
		    else
			{
			    theLocation = new RangeLocation(startPoint.intValue(), endPoint.intValue());
			}
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
