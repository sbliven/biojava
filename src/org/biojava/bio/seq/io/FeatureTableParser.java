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
import org.biojava.bio.seq.*;

/**
 * Simple parser for feature tables.  This is shared between
 * the EMBL and GENBANK format readers.
 *
 * @author Thomas Down
 */

class FeatureTableParser {
    private final static int WITHOUT=0;
    private final static int WITHIN=1;
    private final static int LOCATION=2;
    private final static int ATTRIBUTE=3;

    private int featureStatus = WITHOUT;
    private List features;
    private StringBuffer featureBuf;
    
    private String featureType;
    private Location featureLocation;
    private Map featureAttributes;
    private int featureStrand;

    private FeatureBuilder fb;

    FeatureTableParser(FeatureBuilder fb) {
	this.fb = fb;
	features = new ArrayList();
	featureBuf = new StringBuffer();
	featureAttributes = new HashMap();
    }

    public void startFeature(String type) throws BioException {
	featureType = type;
	featureStatus = LOCATION;
	featureBuf.setLength(0);
	featureAttributes.clear();    
    }

    public void featureData(String line) throws BioException {
	// System.out.println(line);
	// System.out.println(featureStatus);
	switch (featureStatus) {
	case LOCATION:
	    featureBuf.append(line);
	    if (countChar(featureBuf, '(') == countChar(featureBuf, ')')) {
		featureLocation = parseLocation(featureBuf.toString());
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

    public void endFeature() throws BioException {
	features.add(fb.buildFeatureTemplate(featureType, 
					     featureLocation,
					     featureStrand,
					     featureAttributes));
	featureStatus = WITHOUT;
	
    }

    private Location parseLocation(String loc) throws BioException {
	boolean joining = false;
	boolean complementing = false;
	boolean isComplement = false;
	boolean ranging = false;
	
	int start = -1;

	Location result = null;
	
	StringTokenizer toke = new StringTokenizer(loc, "(),. ><", true);
	int level = 0;
	while (toke.hasMoreTokens()) {
	    String t = toke.nextToken();
	    // System.err.println(t);
	    if (t.equals("join")) {
		joining = true;
		result = new CompoundLocation();
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
	    } else if (t.equals("<")) {
	    } else if (t.equals(" ")) {
	    } else {
		// System.err.println("Range! " + ranging);
		// This ought to be an actual oordinate.
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
		    if (joining) {
			((CompoundLocation) result).addLocation(rl);
		    } else {
			if (result != null)
			    throw new BioException();
			result = rl;
		    }
		    ranging = false;
		    complementing = false;
		}
	    }
	}
	if (level != 0)
	    throw new BioException("Mismatched parentheses: " + loc);
	
	if (ranging) {
	    Location rl = new PointLocation(start);
	    if (joining) {
		((CompoundLocation) result).addLocation(rl);
	    } else {
		if (result != null)
		    throw new BioException();
		result = rl;
	    }
	}

	if (isComplement)
	    featureStrand = StrandedFeature.NEGATIVE;
	else
	    featureStrand = StrandedFeature.POSITIVE;

	if (result == null) {
	    throw new BioException("Location null: " + loc);
	}

	return result;
    }

    private void processAttribute(String attr) throws BioException {
	// System.err.println(attr);
	int eqPos = attr.indexOf('=');
	if (eqPos == -1) {
	    featureAttributes.put(attr.substring(1), Boolean.TRUE);
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
	    featureAttributes.put(tag, val);
	}
    }

    public Collection getFeatures() {
	return features;
    }

    public boolean inFeature() {
	return (featureStatus != WITHOUT);
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
