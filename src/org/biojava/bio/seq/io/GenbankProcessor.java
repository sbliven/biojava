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
 * Simple filter which handles attribute lines from a Genbank file
 *
 * @author Greg Cox
 */

public class GenbankProcessor extends SequenceBuilderFilter
{
	public static final String PROPERTY_GENBANK_ACCESSIONS = "genbank_accessions";
	private boolean mBadFeature = false;

	/**
	 * Factory which wraps sequence builders in a GenbankProcessor
	 *
	 * @author Greg Cox
	 */
	public static class Factory implements SequenceBuilderFactory, Serializable
	{
		private SequenceBuilderFactory delegateFactory;

		public Factory(SequenceBuilderFactory theDelegate)
		{
			delegateFactory = theDelegate;
		}

		public SequenceBuilder makeSequenceBuilder()
		{
			return new GenbankProcessor(delegateFactory.makeSequenceBuilder());
		}
	}

	protected FeatureTableParser features;
	private static HashSet featureKeys = null;
	private List accessions;
	{
		accessions = new ArrayList();
	}

	public GenbankProcessor(SequenceBuilder theDelegate)
	{
		super(theDelegate);
		features = new FeatureTableParser(this, "GenBank");
	}

	public void endSequence() throws ParseException
	{
		if (accessions.size() > 0)
		{
			String id = (String) accessions.get(0);
			getDelegate().setName(id);
			getDelegate().setURI("urn:sequence/genbank:" + id);
			getDelegate().addSequenceProperty(PROPERTY_GENBANK_ACCESSIONS, accessions);
		}
		getDelegate().endSequence();
	}

	public void addSequenceProperty(Object key, Object value) throws ParseException
	{
		try
		{
			if(mBadFeature)
			{
				// If this feature is bad in some way, ignore it.
				String featureLine = value.toString();
				if((key.equals(GenbankFormat.FEATURE_FLAG)) && (featureLine.charAt(0) != ' '))
				{
					// If the offending feature is past, start reading data again
					mBadFeature = false;
					features.startFeature(featureLine.substring(0, 16).trim());
					features.featureData(featureLine.substring(16));
				}
			}
			else
			{
				if (features.inFeature() && !(key.equals(GenbankFormat.FEATURE_FLAG)))
				{
					features.endFeature();
				}

				if(key.equals(GenbankFormat.FEATURE_FLAG))
				{
					String featureLine = value.toString();
					if (featureLine.charAt(0) != ' ')
					{
						// This is a featuretype field
						if (features.inFeature())
						{
							features.endFeature();
						}
						features.startFeature(featureLine.substring(0, 16).trim());
					}
					features.featureData(featureLine.substring(16));
				}
				else
				{
					getDelegate().addSequenceProperty(key, value);
					if (key.equals(GenbankFormat.ACCESSION_TAG))
					{
						accessions.add(value);
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