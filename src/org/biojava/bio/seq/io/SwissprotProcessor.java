/*
 *          BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *    http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *    http://www.biojava.org/
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
 * and doesn't handle any of the header information sensibly except for
 * ID and AC.
 * </p>
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @author Greg Cox
 * @since 1.1
 */

public class SwissprotProcessor extends SequenceBuilderFilter
{
	public static final String PROPERTY_SWISSPROT_ACCESSIONS = "swissprot.accessions";
	public static final String PROPERTY_SWISSPROT_COMMENT = "swissprot.comment";
         public static final String PROPERTY_SWISSPROT_FEATUREATTRIBUTE = "swissprot.featureattribute";

	private boolean mBadFeature = false;

	/**
	 * Factory which wraps SequenceBuilders in a SwissprotProcessor
	 *
	 * @author Thomas Down
	 */

	public static class Factory implements SequenceBuilderFactory, Serializable
	{
		private SequenceBuilderFactory delegateFactory;

		public Factory(SequenceBuilderFactory delegateFactory)
		{
			this.delegateFactory = delegateFactory;
		}

		public SequenceBuilder makeSequenceBuilder()
		{
			return new SwissprotProcessor(delegateFactory.makeSequenceBuilder());
		}
	}

	private SwissprotFeatureTableParser features;
	private static HashSet featureKeys = null;
	private List accessions;
	{
		accessions = new ArrayList();
	}

	public SwissprotProcessor(SequenceBuilder delegate)
	{
		super(delegate);
		features = new SwissprotFeatureTableParser(this, "SWISSPROT");
	}

	public void endSequence() throws ParseException
	{
		if (accessions.size() > 0)
		{
			String id = (String) accessions.get(0);
			getDelegate().setName(id);
			getDelegate().setURI("urn:sequence/swissprot:" + id);
			getDelegate().addSequenceProperty(PROPERTY_SWISSPROT_ACCESSIONS, accessions);
			accessions = new ArrayList();
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
				if((key.equals("FT")) && (featureLine.charAt(0) != ' '))
				{
					// If the offending feature is past, start reading data again
					mBadFeature = false;
					features.startFeature(featureLine.substring(0, 8).trim());
					features.featureData(featureLine.substring(9));
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

			    		features.startFeature(featureLine.substring(0, 8).trim());
					}
					features.featureData(featureLine.substring(9));
				}
				else
				{
					getDelegate().addSequenceProperty(key, value);

					if (key.equals("AC"))
					{
						String acc= value.toString();
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
