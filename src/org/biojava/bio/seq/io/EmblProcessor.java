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
 * @author Greg Cox
 * @since 1.1
 */

public class EmblProcessor extends SequenceBuilderFilter {
    public static final String PROPERTY_EMBL_ACCESSIONS = "embl_accessions";

    private boolean mBadFeature = false;

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

    private FeatureTableParser features;

    public EmblProcessor(SequenceBuilder delegate) {
	super(delegate);
	features = new FeatureTableParser(this, "EMBL");
    }

    public void endSequence() throws ParseException {
	if (accessions.size() > 0) {
	    String id = (String) accessions.get(0);
	    getDelegate().setName(id);
	    getDelegate().setURI("urn:sequence/embl:" + id);
	    getDelegate().addSequenceProperty(PROPERTY_EMBL_ACCESSIONS, accessions);
	}
	getDelegate().endSequence();
    }

    private List accessions;

    {
	accessions = new ArrayList();
    }

    public void addSequenceProperty(Object key, Object value) throws ParseException
    {
	try
	{
	    if (mBadFeature)
	    {
		// If this feature is bad in some way, ignore it.
		String featureLine = value.toString();
		if((key.equals("FT")) && (featureLine.charAt(0) != ' '))
		{
		    // If the offending feature is past, start reading data again
		    mBadFeature = false;
		    features.startFeature(featureLine.substring(0, 15).trim());
		    features.featureData(featureLine.substring(16));
		}
	    }
	    else
	    {
		// Tidy up any end-of-block jobbies
		if (features.inFeature() && !key.equals("FT"))
		{
		    features.endFeature();
		}

		if (key.equals("FT"))
		{
		    String featureLine = value.toString();
		    if (featureLine.charAt(0) != ' ')
		    {
			// This is a featuretype field
			if (features.inFeature())
			{
			    features.endFeature();
			}

			features.startFeature(featureLine.substring(0, 15).trim());
		    }
		    features.featureData(featureLine.substring(16));
		}
		else
		{
		    getDelegate().addSequenceProperty(key, value);

		    if (key.equals("AC"))
		    {
			String acc = value.toString();
			StringTokenizer toke = new StringTokenizer(acc, "; ");
			while (toke.hasMoreTokens())
			{
			    accessions.add(toke.nextToken());
			}
		    }
		}
	    }
	}
	catch (BioException ex)
	{
	    // If an exception is thrown, read past the offending feature
	    mBadFeature = true;
	    System.err.println(ex);
	}
    }
}
