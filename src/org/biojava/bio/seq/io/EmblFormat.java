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
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Format reader for EMBL files.
 *
 * @author Thomas Down
 */

public class EmblFormat implements SequenceFormat, Serializable {
    private FeatureBuilder featureBuilder;

    /**
     * Constuct and EMBL format processor using a default
     * FeatureBuilder object. (actually a SimpleFeatureBuilder).
     */

    public EmblFormat() {
	featureBuilder = new SimpleFeatureBuilder();
    }

    /**
     * Construct an EMBL format processor using the specified
     * FeatureBuilder object.
     */
    
    public EmblFormat(FeatureBuilder fb) {
	this.featureBuilder = fb;
    }

    public Sequence readSequence(StreamReader.Context context,
				 SymbolParser resParser,
				 SequenceFactory sf)
	throws IllegalSymbolException, IOException, BioException
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

	private SymbolParser resParser;
	private SequenceFactory sf;

	private List symbols;
	private FeatureTableParser features;

	private Annotation annotation;
	private List accession;

	EmblContext(SymbolParser resParser, SequenceFactory sf) {
	    this.resParser = resParser;
	    this.sf = sf;

	    symbols = new ArrayList();
	    features = new FeatureTableParser(featureBuilder);
	    annotation = new SimpleAnnotation();
	    accession = new ArrayList();
	}

	void processLine(String line) throws BioException {
	    String tag = line.substring(0, 2);

	    // Any tagprocessors which might need some cleaning
	    // up if a different tag is encountered.

	    if (features.inFeature() && !(tag.equals("FT")))
		features.endFeature();

	    if (tag.equals("AC")) {
		String acc= line.substring(5, line.length()-1);
		StringTokenizer toke = new StringTokenizer(acc, "; ");
		while (toke.hasMoreTokens())
		    accession.add(toke.nextToken());
	    } else if (tag.equals("FT")) {
		if (line.charAt(5) != ' ') {
		    // Has a featureType field -- should be a new feature
		    if (features.inFeature())
			features.endFeature();

		    features.startFeature(line.substring(5, 20).trim());
		    // featureData(line.substring(21));
		} 
		features.featureData(line.substring(21));  
	    }
	}

	void processSeqLine(String line) throws IllegalSymbolException {
	    StringTokenizer st = new StringTokenizer(line);
	    while(st.hasMoreTokens()) {
		String token = st.nextToken();
		if(st.hasMoreTokens()) {
		    symbols.addAll(resParser.parse(token).toList());
		} else {
		    char c = token.charAt(token.length()-1);
		    if(!Character.isDigit(c)) {
			symbols.addAll(resParser.parse(token).toList());
		    }
		}
	    }
	}

	Sequence makeSequence() throws BioException {
	    Sequence ss;
	    String primaryAcc = "unknown";

	    if (accession.size() > 0) {
		primaryAcc = (String) accession.get(0);
		annotation.setProperty("embl_accessions", accession);
	    }

	    ss = sf.createSequence(new SimpleSymbolList(
				   resParser.getAlphabet(),symbols),
				    "urn:sequence/embl:" + primaryAcc,
				    primaryAcc,
				    annotation);
	    for (Iterator i = features.getFeatures().iterator(); i.hasNext(); ) {
		ss.createFeature((Feature.Template) i.next());
	    }
	    return ss;
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
}


