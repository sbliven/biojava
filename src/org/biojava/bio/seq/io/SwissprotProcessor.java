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
 * Simple filter which handles attribute lines from an Swissprot entry.
 * Skeleton implementation, please add more functionality.
 *
 * <p>
 * <strong>FIXME:</strong> Note that this is currently rather incomplete, 
 * and doesn't handle the feature table at all.
 * </p>
 *
 * @author Thomas Down
 * @since 1.1
 */

public class SwissprotProcessor implements SequenceBuilder {
    public static final String PROPERTY_SWISSPROT_ACCESSIONS = "swissprot.accessions";

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
	    return new SwissprotProcessor(delegateFactory.makeSequenceBuilder());
	}
    }

    private SequenceBuilder delegate;

    public SwissprotProcessor(SequenceBuilder delegate) {
	this.delegate = delegate;
    }

    public void startSequence() {
	delegate.startSequence();
    }

    public void endSequence() {
	if (accessions.size() > 0) {
	    String id = (String) accessions.get(0);
	    delegate.setName(id);
	    delegate.setURI("urn:sequence/swissprot:" + id);
	    delegate.addSequenceProperty(PROPERTY_SWISSPROT_ACCESSIONS, accessions);
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
	delegate.addSequenceProperty(key, value);
	
	if (key.equals("AC")) {
	    String acc= value.toString();
	    StringTokenizer toke = new StringTokenizer(acc, "; ");
	    while (toke.hasMoreTokens())
		accessions.add(toke.nextToken());
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
