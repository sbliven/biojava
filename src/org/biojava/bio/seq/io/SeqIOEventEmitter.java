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

import org.biojava.bio.BioException;
import org.biojava.bio.Annotation;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;

/**
 * <code>SeqIOEventEmitter</code> is a utility class which scans a
 * <code>Sequence</code> object and sends events describing its
 * constituent data to a <code>SeqIOListener</code>. The listener
 * should be able to reconstruct the <code>Sequence</code> from these
 * events.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class SeqIOEventEmitter
{
    /**
     * <code>SeqIOEventEmitter</code> can not be instantiated.
     */
    private SeqIOEventEmitter() { };

    /**
     * <code>getSeqIOEvents</code> scans a <code>Sequence</code>
     * object and sends events describing its data to the
     * <code>SeqIOListener</code>.
     *
     * @param seq a <code>Sequence</code> object.
     * @param listener a <code>SeqIOListener</code> object.
     */
    public static void getSeqIOEvents(Sequence seq, SeqIOListener listener)
	throws BioException
    {
	// Some EMBL features cause exceptions futher down. This
	// hasn't been debugged yet.

	// Inform listener of sequence start
	try
	{
	    listener.startSequence();
	}
	catch (ParseException pe)
	{
	    pe.printStackTrace();
	}

	// Pass name to listener
	try
	{
	    listener.setName(seq.getName());
	}
	catch (ParseException pe)
	{
	    pe.printStackTrace();
	}

	// Pass URN to listener
	try
	{
	    listener.setURI(seq.getURN());
	}
	catch (ParseException pe)
	{
	    pe.printStackTrace();
	}

	// Pass sequence properties to listener
	Annotation a = seq.getAnnotation();

	try
	{
	    for (Iterator ai = a.keys().iterator(); ai.hasNext();)
	    {
		Object key = ai.next();
		listener.addSequenceProperty(key, a.getProperty(key));
	    }
	}
	catch (ParseException pe)
	{
	    pe.printStackTrace();
	}

	// Recurse through sub feature tree, flattening it for
	// EMBL
	List subs = getSubFeatures(seq);
	Collections.sort(subs, Feature.byLocationOrder);

	for (Iterator fi = subs.iterator(); fi.hasNext();)
	{		
	    // The template is required to call startFeature
	    Feature.Template t =
		((Feature) fi.next()).makeTemplate();
	    
	    // Inform listener of feature start
	    try
	    {
		listener.startFeature(t);
	    }
	    catch (ParseException pe)
	    {
		pe.printStackTrace();
	    }

	    // Pass feature properties (i.e. qualifiers to
	    // listener)
	    try
	    {
		for (Iterator ki = t.annotation.keys().iterator(); ki.hasNext();)
		{
		    Object key = ki.next();
		    listener.addFeatureProperty(key, t.annotation.getProperty(key));
		}
	    }
	    catch (ParseException pe)
	    {
		pe.printStackTrace();
	    }

	    // Inform listener of feature end
	    try
	    {
		listener.endFeature();
	    }
	    catch (ParseException pe)
	    {
		pe.printStackTrace();
	    }
	}

	// Pass sequence symbols to listener
	try
	{
	    listener.addSymbols(seq.getAlphabet(),
				(Symbol []) seq.toList().toArray(new Symbol [0]),
				1,
				seq.length());
	}
	catch (IllegalAlphabetException iae)
	{
	    // This should never happen as the alphabet is being used
	    // by this Sequence instance
	    throw new BioException("An internal error occurred processing symbols of "
				   + seq.toString()
				   + " into SeqIO events");
	}
	    // Inform listener of sequence end
	    listener.endSequence();
    }

    /**
     * <code>getSubFeatures</code> is a recursive method which returns
     * a list of all <code>Feature</code>s within a
     * <code>FeatureHolder</code>.
     *
     * @param fh a <code>FeatureHolder</code> object.
     * @return a <code>List</code> value.
     */
    private static List getSubFeatures(FeatureHolder fh)
    {
	List subfeat = new ArrayList();

	for (Iterator fi = fh.features(); fi.hasNext();)
	{
	    FeatureHolder sfh = (FeatureHolder) fi.next();

	    subfeat.addAll((Collection) getSubFeatures(sfh));
	    subfeat.add(sfh);
	}
	return subfeat;
    }
}
