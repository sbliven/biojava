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
 * Simple filter which performs a default extraction of data from
 * the description lines of FASTA files.  Behaviour is similar
 * to DefaultDescriptionReader in the old I/O framework.
 *
 * @author Thomas Down
 * @since 1.1
 */

public class FastaDescriptionLineParser implements SequenceBuilder {
    /**
     * Factory which wraps SequenceBuilders in a FastaDescriptionLineParser
     *
     * @author Thomas Down
     */

    public static class Factory implements SequenceBuilderFactory {
	private SequenceBuilderFactory delegateFactory;

	public Factory(SequenceBuilderFactory delegateFactory) {
	    this.delegateFactory = delegateFactory;
	}

	public SequenceBuilder makeSequenceBuilder() {
	    return new FastaDescriptionLineParser(delegateFactory.makeSequenceBuilder());
	}
    }

    private SequenceBuilder delegate;

    public FastaDescriptionLineParser(SequenceBuilder delegate) {
	this.delegate = delegate;
    }
	

    public void startSequence() {
	delegate.startSequence();
    }

    public void endSequence() {
	delegate.endSequence();
    }

    public void setName(String name) {
	delegate.setName(name);
    }

    public void setURI(String uri) {
	delegate.setURI(uri);
    }

    public void addSymbols(SymbolReader sr)
        throws IOException, IllegalSymbolException
    {
	delegate.addSymbols(sr);
    }

    public void addSequenceProperty(String key, Object value) {
	delegate.addSequenceProperty(key, value);

	if (FastaFormat.PROPERTY_DESCRIPTIONLINE.equals(key)) {
	    String dline = value.toString();
	    StringTokenizer toke = new StringTokenizer(dline);
	    String name = toke.nextToken();
	    setName(name);
	    setURI("urn:sequence/fasta:" + name);
	    if (toke.hasMoreTokens()) {
		delegate.addSequenceProperty("description", toke.nextToken("******"));
	    }
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
