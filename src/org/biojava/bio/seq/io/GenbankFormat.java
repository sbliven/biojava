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

public class GenbankFormat implements SequenceFormat {
    private FeatureBuilder featureBuilder;

    /**
     * Constuct a GENBANK format processor using a default
     * FeatureBuilder object. (actually a SimpleFeatureBuilder).
     */

    public GenbankFormat() {
	featureBuilder = new SimpleFeatureBuilder();
    }

    /**
     * Construct a GENBANK format processor using the specified
     * FeatureBuilder object.
     */

    public GenbankFormat(FeatureBuilder fb) {
	this.featureBuilder = fb;
    }

    public Sequence readSequence(StreamReader.Context context,
				 ResidueParser resParser,
				 SequenceFactory sf)
	throws IllegalResidueException, IOException, SeqException
    {
	GenbankContext ctx = new GenbankContext(resParser, sf);

	BufferedReader in = context.getReader();
	String line;

	while ((line = in.readLine()) != null) {
	    if (line.startsWith("//")) {
		in.mark(2);
		if (in.read() == -1)
		    context.streamEmpty();
		else
		    in.reset();
		return ctx.makeSequence();
	    }

	    ctx.processLine(line);
	}

	context.streamEmpty();
	throw new IOException("Premature end of stream for EMBL");
    }

    private class GenbankContext {
	private final static int HEADER = 1;
	private final static int FEATURES = 2;
	private final static int SEQUENCE = 3;

	private int status;

	private ResidueParser resParser;
	private SequenceFactory sf;

	private List residues;
	private FeatureTableParser features;

	private String accession;

	GenbankContext(ResidueParser resParser, SequenceFactory sf) {
	    this.resParser = resParser;
	    this.sf = sf;

	    residues = new ArrayList();
	    features = new FeatureTableParser(featureBuilder);
	    status = HEADER;
	}

	void processLine(String line) throws SeqException,
	                                     IllegalResidueException
	{
	    if (line.startsWith("FEATURES")) {
		status = FEATURES;
	    } else if (line.startsWith("ORIGIN")) {
		if (features.inFeature())
		    features.endFeature();
		status = SEQUENCE;
	    } else if (line.startsWith("ACCESSION")) {
		accession = line.substring(12);
	    } else if (status == FEATURES) {
		if (line.charAt(0) != ' ') {
		    if (features.inFeature())
			features.endFeature();
		    status = HEADER;
		} else if (line.charAt(5) != ' ') {
		    // Has a featureType field -- should be a new feature
		    if (features.inFeature())
			features.endFeature();

		    features.startFeature(line.substring(5, 20).trim());
		    // featureData(line.substring(21));
		} 
		features.featureData(line.substring(21));  
	    } else if (status == SEQUENCE) {
		processSeqLine(line);
	    }
	}

	private void processSeqLine(String line)
	    throws IllegalResidueException 
	{
	    StringTokenizer st = new StringTokenizer(line);
	    while(st.hasMoreTokens()) {
		String token = st.nextToken();

		char c = token.charAt(token.length()-1);
		if(!Character.isDigit(c)) {
		    residues.addAll(resParser.parse(token).toList());
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
	    for (Iterator i = features.getFeatures().iterator(); i.hasNext(); ) {
		ss.createFeature((MutableFeatureHolder) ss, (Feature.Template) i.next());
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
	throw new RuntimeException("Can't write in GENBANK format...");
    }
}

