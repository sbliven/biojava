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

import java.util.*;
import java.io.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.*;

/**
 * Simple filter which handles attribute lines from an EMBL file
 *
 * @author Thomas Down
 * @since 1.1
 */

public class EmblProcessor implements SequenceBuilder {
    public static final String PROPERTY_EMBL_ACCESSIONS = "embl_accessions";

    /**
     * Factory which wraps SequenceBuilders in an EmblProcessor
     *
     * @author Thomas Down
     */

    public static class Factory implements SequenceBuilderFactory, Serializable {
	private SequenceBuilderFactory delegateFactory;

	public Factory(SequenceBuilderFactory delegateFactory) {
	    this.delegateFactory = delegateFactory;
	}

	public SequenceBuilder makeSequenceBuilder() {
	    return new EmblProcessor(delegateFactory.makeSequenceBuilder());
	}
    }

    private SequenceBuilder delegate;
    private FeatureTableParser features;

    public EmblProcessor(SequenceBuilder delegate) {
	this.delegate = delegate;
	features = new FeatureTableParser(this);
    }

    public void startSequence() {
	delegate.startSequence();
    }

    public void endSequence() {
	if (accessions.size() > 0) {
	    String id = (String) accessions.get(0);
	    delegate.setName(id);
	    delegate.setURI("urn:sequence/embl:" + id);
	    delegate.addSequenceProperty(PROPERTY_EMBL_ACCESSIONS, accessions);
	}
	delegate.endSequence();
    }

    public void setName(String name) {
	delegate.setName(name);
    }

    public void setURI(String uri) {
	delegate.setURI(uri);
    }

    public void addSymbols(Alphabet a, Symbol[] syms, int pos, int len)
        throws IllegalAlphabetException
    {
	delegate.addSymbols(a, syms, pos, len);
    }

    private List accessions;

    {
	accessions = new ArrayList();
    }

    public void addSequenceProperty(String key, Object value) {
	try {
	    // Tidy up any end-of-block jobbies
	    
	    if (features.inFeature() && !key.equals("FT")) {
		features.endFeature();
	    }
       
	    if (key.equals("FT")) {
		String featureLine = value.toString();
		if (featureLine.charAt(0) != ' ') {
		    // This is a featuretype field
		    if (features.inFeature())
			features.endFeature();
		    
		    features.startFeature(featureLine.substring(0, 15).trim());
		}
		features.featureData(featureLine.substring(16));
	    } else {
		delegate.addSequenceProperty(key, value);
		
		if (key.equals("AC")) {
		    String acc= value.toString();
		    StringTokenizer toke = new StringTokenizer(acc, "; ");
		    while (toke.hasMoreTokens())
			accessions.add(toke.nextToken());
		}
	    }
	} catch (BioException ex) {
	    throw new BioError("FIXME");
	}
    }

    public void startFeature(Feature.Template templ) {
	delegate.startFeature(templ);
    }

    public void addFeatureProperty(String key, Object value) {
	delegate.addFeatureProperty(key, value);
    }

    public void endFeature() {
	delegate.endFeature();
    }

    public Sequence makeSequence() throws BioException {
	return delegate.makeSequence();
    }
}
