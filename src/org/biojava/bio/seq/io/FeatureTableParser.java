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
 * Simple parser for feature tables. This is shared between the EMBL
 * and GENBANK format readers.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @author Greg Cox
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 */

/*
 * Greg Cox: Changed private fields and methods to protected so that
 *           SwissProtFeatureTableParser could subclass and snag the *
 *           implementation.
 *
 * Thomas Down: Post 1.1, finally got round to refactoring this to be
 *              a `nice' player in the newio world.  Needless to say,
 *              this simplified things quite a bit.
 * 
 * Keith James: Added support for reading fuzzy i.e. (123.567)
 *              locations in addition to unbounded i.e. <123..567
 *              locations.
 */

class FeatureTableParser {
    private final static int   WITHOUT = 0;
    private final static int    WITHIN = 1;
    private final static int  LOCATION = 2;
    private final static int ATTRIBUTE = 3;

    private int featureStatus = WITHOUT;
    private StringBuffer featureBuf;
    private StrandedFeature.Template featureTemplate;

    private String                 featureSource;
    private SeqIOListener          listener;
    private EmblLikeLocationParser locParser;

    FeatureTableParser(SeqIOListener listener, String source)
    {
	this.listener      = listener;
	this.featureSource = source;

	featureBuf = new StringBuffer();
	locParser  = new EmblLikeLocationParser();
    }

    //
    // Interface which the processors use to call us
    //

    public void startFeature(String type) throws BioException {
	featureStatus = LOCATION;
	featureBuf.setLength(0);

	featureTemplate = new StrandedFeature.Template();
	featureTemplate.type = type;
	featureTemplate.source = featureSource;
	featureTemplate.annotation = new SimpleAnnotation();
    }

    public void featureData(String line) throws BioException {
	switch (featureStatus) {
	case LOCATION:
	    featureBuf.append(line);
	    if (countChar(featureBuf, '(') == countChar(featureBuf, ')'))
	    {
		// Save the original location in the feature's
		// annotation bundle
		try
		{
		    featureTemplate.annotation.setProperty("location", featureBuf.toString());
		}
		catch (ChangeVetoException cve)
		{
		    throw new BioException("Unable to add location string to feature annotation: "
					   + featureBuf);
		}

		parseLocation(featureBuf.toString(), featureTemplate);

		listener.startFeature(featureTemplate);
		featureStatus = WITHIN;
	    }
	    break;
	case WITHIN:
	    if (line.charAt(0) == '/') {
		// System.out.println("got '/', quotes = " + countChar(line, '"'));
		if (countChar(line, '"') % 2 == 0)
		    processAttribute(line);
		else {
		    featureBuf.setLength(0);
		    featureBuf.append(line);
		    featureStatus = ATTRIBUTE;
		}
	    } else {
		throw new BioException("Invalid line in feature body: " + line);
	    }
	    break;
	case ATTRIBUTE:
	    // If the attribute contains whitespace it probably
	    // consists of whitespace-delimited words. Therefore a
	    // space should be inserted at EOL otherwise words will
	    // get fused.
	    if (countChar(featureBuf, ' ') > 0)
		featureBuf.append(" ");
	    featureBuf.append(line);
	    if (countChar(featureBuf, '"') % 2 == 0) {
		processAttribute(featureBuf.toString());
		featureStatus = WITHIN;
	    }
	    break;
	}
    }

    public void endFeature()
	throws BioException
    {
	listener.endFeature();
	featureStatus = WITHOUT;
    }

    public boolean inFeature() {
	return (featureStatus != WITHOUT);
    }

    /**
     * Parse an EMBL location and fill in the location and strand fields of the
     * template. Updated to support a wider range of location types.
     */
    private void parseLocation(String loc, StrandedFeature.Template fillin) 
	throws BioException 
    {
	Object [] locStruct = locParser.parseLocation(loc);

	if (((Boolean) locStruct[1]).booleanValue())
	{
	    fillin.strand = StrandedFeature.NEGATIVE;
	} else {
	    fillin.strand = StrandedFeature.POSITIVE;
	}

	fillin.location = (Location) locStruct[0];
    }

    /**
     * Process the a string corresponding to a feature-table
     * attribute, and fire it off to our listener.
     */
    private void processAttribute(String attr) throws BioException {
	// System.err.println(attr);
	int eqPos = attr.indexOf('=');
	if (eqPos == -1) {
	    listener.addFeatureProperty(attr.substring(1), Boolean.TRUE);
	} else {
	    String tag = attr.substring(1, eqPos);
	    eqPos++;
	    if (attr.charAt(eqPos) == '"')
		++eqPos;
	    int max = attr.length();
	    if (attr.charAt(max - 1) == '"')
		--max;
	    String val = attr.substring(eqPos, max);
	    if (val.indexOf('"') >= 0) {
		StringBuffer sb = new StringBuffer();
		boolean escape = false;
		for (int i = 0; i < val.length(); ++i) {
		    char c = val.charAt(i);
		    if (c == '"') {
			if (escape)
			    sb.append(c);
			escape = !escape;
		    } else {
			sb.append(c);
			escape = false;
		    }
		}
		val = sb.toString();
	    }
	    listener.addFeatureProperty(tag, val);
	}
    }

    private int countChar(StringBuffer s, char c) {
	int cnt = 0;
	for (int i = 0; i < s.length(); ++i)
	    if (s.charAt(i) == c)
		++cnt;
	return cnt;
    }

    private int countChar(String s, char c) {
	int cnt = 0;
	for (int i = 0; i < s.length(); ++i)
	    if (s.charAt(i) == c)
		++cnt;
	return cnt;
    }
}
