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

import org.biojava.bio.seq.*;

/**
 * Format reader for EMBL files.
 *
 * @author Thomas Down
 */

public class EmblFormat implements SequenceFormat {
    private FeatureBuilder featureBuilder;

    public EmblFormat() {
	featureBuilder = new BasicFeatureBuilder();
    }

    public Sequence readSequence(StreamReader.Context context,
				 ResidueParser resParser,
				 SequenceFactory sf)
	throws IllegalResidueException, IOException, SeqException
    {
	EmblContext ctx = new EmblContext(resParser, sf);

	BufferedReader in = context.getReader();
	String line;
	boolean isSeq = false;

	while ((line = in.readLine()) != null) {
	    if (line.startsWith("//")) {
		in.mark(2);
		if (in.read() == -1)
		    context.streamEmpty();
		else
		    in.reset();
		return ctx.makeSequence();
	    }
	    
	    if (isSeq) {
		ctx.processSeqLine(line);
	    } else {
		ctx.processLine(line);
		if (line.startsWith("SQ"))
		    isSeq = true;
	    }
	}

	context.streamEmpty();
	throw new IOException("Premature end of stream for EMBL");
    }

    private class EmblContext {
	private final static int WITHOUT=0;
	private final static int WITHIN=1;
	private final static int LOCATION=2;
	private final static int ATTRIBUTE=3;

	private ResidueParser resParser;
	private SequenceFactory sf;

	private List residues;

	private int featureStatus;
	private List features;
	private StringBuffer featureBuf;

	private String featureType;
	private Location featureLocation;
	private Map featureAttributes;
	private int featureStrand;

	private String accession;

	EmblContext(ResidueParser resParser, SequenceFactory sf) {
	    this.resParser = resParser;
	    this.sf = sf;

	    residues = new ArrayList();
	    features = new ArrayList();
	    featureBuf = new StringBuffer();
	    featureAttributes = new HashMap();
	}

	void processLine(String line) throws SeqException {
	    String tag = line.substring(0, 2);

	    // Any tagprocessors which might need some cleaning
	    // up if a different tag is encountered.

	    if (featureStatus != WITHOUT && !(tag.equals("FT")))
		endFeature();

	    if (tag.equals("AC")) {
		accession = line.substring(5, line.length()-1);
	    } else if (tag.equals("FT")) {
		if (line.charAt(5) != ' ') {
		    // Has a featureType field -- should be a new feature
		    if (featureStatus != WITHOUT)
			endFeature();

		    startFeature(line.substring(5, 20).trim());
		    // featureData(line.substring(21));
		} 
		featureData(line.substring(21));  
	    }
	}

	private void startFeature(String type) throws SeqException {
	    featureType = type;
	    featureStatus = LOCATION;
	    featureBuf.setLength(0);
	    featureAttributes.clear();
	    
	}

	private void featureData(String line) throws SeqException {
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
		    throw new SeqException("Invalid line in feature body: "+line);
		}
		break;
	    case ATTRIBUTE:
		featureBuf.append(line);
		if (countChar(featureBuf, '"') == 2) {
		    processAttribute(featureBuf.toString());
		    featureStatus = WITHIN;
		}
		break;
	    }
	}

	private void endFeature() throws SeqException {
	    features.add(featureBuilder.buildFeatureTemplate(featureType, 
							     featureLocation,
							     featureStrand,
							   featureAttributes));
	    featureStatus = WITHOUT;
			 
	}

	private Location parseLocation(String loc) throws SeqException {
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
			throw new SeqException("bad locator: " + t);
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
				throw new SeqException();
			    result = rl;
			}
			ranging = false;
			complementing = false;
		    }
		}
	    }
	    if (level != 0)
		throw new SeqException("Mismatched parentheses: " + loc);

	    if (isComplement)
		featureStrand = StrandedFeature.NEGATIVE;
	    else
		featureStrand = StrandedFeature.POSITIVE;
	    return result;
	}

	private void processAttribute(String attr) throws SeqException {
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
		featureAttributes.put(tag, attr.substring(eqPos, max));
	    }
	}

	void processSeqLine(String line) throws IllegalResidueException {
	    StringTokenizer st = new StringTokenizer(line);
	    while(st.hasMoreTokens()) {
		String token = st.nextToken();
		if(st.hasMoreTokens()) {
		    residues.addAll(resParser.parse(token).toList());
		} else {
		    char c = token.charAt(token.length()-1);
		    if(!Character.isDigit(c)) {
			residues.addAll(resParser.parse(token).toList());
		    }
		}
	    }
	}

	Sequence makeSequence() throws SeqException {
	    Sequence ss;
	    ss = sf.createSequence(new SimpleResidueList(
				   resParser.alphabet(),residues),
				    "urn:whatever",
				    accession,
				    Annotation.EMPTY_ANNOTATION);
	    for (Iterator i = features.iterator(); i.hasNext(); ) {
		ss.createFeature((MutableFeatureHolder) ss, (Feature.Template) i.next());
	    }
	    return ss;
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

    /**
     * This is not implemented. It does not write anything to the stream.
     */
    public void writeSequence(Sequence seq, PrintStream os)
	throws IOException 
    {
	throw new RuntimeException("Can't write in EMBL format...");
    }

    public static interface FeatureBuilder {
	public Feature.Template buildFeatureTemplate(String type,
						     Location loc,
						     int strandHint,
						     Map attrs);
    }
}


class BasicFeatureBuilder implements EmblFormat.FeatureBuilder {
    public Feature.Template buildFeatureTemplate(String type,
						 Location loc,
						 int strandHint,
						 Map attrs) {
	StrandedFeature.Template t = new StrandedFeature.Template();
	t.annotation = new SimpleAnnotation();
	for (Iterator i = attrs.entrySet().iterator(); i.hasNext(); ) {
	    Map.Entry e = (Map.Entry) i.next();
	    t.annotation.setProperty(e.getKey(), e.getValue());
	}

	t.location = loc;
	t.type = type;
	t.source = "EMBL file";
	t.strand = strandHint;

	return t;
    }
}
