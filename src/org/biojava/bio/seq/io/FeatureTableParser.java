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
 * Simple parser for feature tables.  This is shared between
 * the EMBL and GENBANK format readers.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @author Greg Cox
 */

/*
 * Greg Cox: Changed private fields and methods to protected so that
 * SwissProtFeatureTableParser could subclass and snag the implementation
 *
 * Thomas Down: Post 1.1, finally got round to refactoring this to be
 *              a `nice' player in the newio world.  Needless to say,
 *              this simplified things quite a bit...
 */

class FeatureTableParser {
    private final static int WITHOUT=0;
    private final static int WITHIN=1;
    private final static int LOCATION=2;
    private final static int ATTRIBUTE=3;

    private int featureStatus = WITHOUT;
    private StringBuffer featureBuf;
    private StrandedFeature.Template featureTemplate;

    private String featureSource;
    private SeqIOListener listener;

    FeatureTableParser(SeqIOListener listener, String source) {
	this.listener = listener;
	this.featureSource = source;

	featureBuf = new StringBuffer();
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
	    if (countChar(featureBuf, '(') == countChar(featureBuf, ')')) {
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
		throw new BioException("Invalid line in feature body: "+line);
	    }
	    break;
	case ATTRIBUTE:
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

    //
    // Internal stuff
    //

    /**
     * Parse an EMBL location and fill in the location and strand fields of the
     * template.  This is still a bit simplistic.
     */

    private void parseLocation(String loc, StrandedFeature.Template fillin) 
	throws BioException 
    {
	    boolean joining = false;
	    boolean complementing = false;
	    boolean isComplement = false;
	    boolean ranging = false;
	    boolean fuzzyMin = false;
	    boolean fuzzyMax = false;

	    int start = -1;

	    Location result = null;
	    List locationList = null;

	    StringTokenizer toke = new StringTokenizer(loc, "(),. ><", true);
	    int level = 0;
	    while (toke.hasMoreTokens()) {
	        String t = toke.nextToken();
	        // System.err.println(t);
	        if (t.equals("join") || t.equals("order")) {
		        joining = true;
	            locationList = new ArrayList();
	        } else if (t.equals("complement")) {
		        complementing = true;
		        isComplement = true;
	        } else if (t.equals("(")) {
		        ++level;
	        } else if (t.equals(")")) {
		        --level;
	        } else if (t.equals(".")) {
	        } else if (t.equals(",")) {
	        } else if (t.equals(">")) {
                if (ranging) {
                    fuzzyMax = true;
                } else {
                    fuzzyMin = true;
                }
	        } else if (t.equals("<")) {
                if (ranging) {
                    fuzzyMax = true;
                } else {
                    fuzzyMin = true;
                }
	        } else if (t.equals(" ")) {
	        } else {
		// System.err.println("Range! " + ranging);
		// This ought to be an actual coordinate.
		int pos = -1;
		try {
		    pos = Integer.parseInt(t);
		} catch (NumberFormatException ex) {
		    throw new BioException("bad locator: " + t + " " + loc);
		}

		if (ranging == false) {
		    start = pos;
		    ranging = true;
		} else {
		    Location rl = new RangeLocation(start, pos);
		    if (fuzzyMin || fuzzyMax) {
			rl = new FuzzyLocation(fuzzyMin ? Integer.MIN_VALUE : start,
					       fuzzyMax ? Integer.MAX_VALUE : pos,
					       start,
					       pos,
					       FuzzyLocation.RESOLVE_INNER);
		    } else {
			rl = new RangeLocation(start, pos);
		    }

		    if (joining) {
			locationList.add(rl);
		    } else {
			if (result != null) {
			    throw new BioException(
						   "Tried to set result to " + rl +
						   " when it was alredy set to " + result
						   );
			}
			result = rl;
		    }
		    ranging = false;
		    complementing = false;
            fuzzyMin = fuzzyMax = false;
		}
	    }
	}
	if (level != 0) {
	    throw new BioException("Mismatched parentheses: " + loc);
	}

	if (ranging) {
	    Location rl = new PointLocation(start);
	    if (joining) {
		locationList.add(rl);
	    } else {
		if (result != null) {
		    throw new BioException();
		}
		result = rl;
	    }
	}

	if (isComplement) {
	    fillin.strand = StrandedFeature.NEGATIVE;
	} else {
	    fillin.strand = StrandedFeature.POSITIVE;
	}

	if (result == null) {
	    if(locationList == null) {
		throw new BioException("Location null: " + loc);
	    }
	    result = new CompoundLocation(locationList);
	}

	fillin.location = result;
    }

    /**
     * Process the a string corresponding to a feature-table attribute, and fire
     * it off to our listener.
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
