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

import org.biojava.bio.BioException;

import java.lang.*;
import java.util.*;

/**
 * <code>SeqFileFormerFactory</code>.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public abstract class SeqFileFormerFactory
{
    private static Map factories = new HashMap();

    // FIXME - Registration of valid (and default) formers for each
    // SequenceFormat implementation needs to be done somewhere
    // sensible.

    // This is an array of any and all formats and is used to fix the
    // case of the format name
    private static String [] formats = { "Embl" };

    /**
     * <code>addFactory</code> installs the static factories
     * subclassed from <code>SeqFileFormerFactory</code> into its
     * factories hash, indexed by file format name. This method is
     * called from within a static block in each implementation of
     * <code>SeqFileFormer</code>. Thus, when the class is dynamically
     * loaded its factory is automatically installed.
     *
     * @param format a <code>String</code> identifer which specifies
     * the file format name.
     * @param factory a <code>SeqFileFormerFactory</code> to be
     * installed.
     */
    static void addFactory(String format, SeqFileFormerFactory factory)
    {
	factories.put(format, factory);
    }

    /**
     * <code>makeFormer</code> responds to requests from
     * <code>SequenceFormat</code> instances for file formatting
     * objects, loading new classes if necessary.
     *
     * @param format a <code>String</code> identifer which specifies
     * the name of the file format that the resultant
     * <code>SeqFileFormer</code> will generate.
     *
     * @return a <code>SeqFileFormer</code> object.
     *
     * @exception BioException if a non-existent file format is
     * requested.  */
    public static final SeqFileFormer makeFormer(String format)
	throws BioException
    {
	// Allow client programmers to use whichever case they like
	for (int i = 0; i < formats.length; i++)
	{
	    if (formats[i].equalsIgnoreCase(format))
		format = formats[i];
	}

	// If there is no Factory for this format load the class
	// dynamically
	if (! factories.containsKey(format))
	{
	    try
	    {
		// Load the class, causing its static block to insert
		// a Factory instance into the SeqFileFormerFactory
		// HashMap
		Class.forName("org.biojava.bio.seq.io."
			      + format
			      + "FileFormer");
	    }
	    catch (ClassNotFoundException cnfe)
	    {
		throw new BioException("Attempted to load non-existent FileFormer class '"
				       + "org.biojava.bio.seq.io."
				       + format
				       + "FileFormer'");
	    }

	    if (! factories.containsKey(format))
		throw new BioException("Failed to load non-existent FileFormer class '"
				       + "org.biojava.bio.seq.io."
				       + format
				       + "FileFormer'");
	}

	// Call make method on the relevant Factory to create a new
	// SeqFileFormer
	return ((SeqFileFormerFactory) factories.get(format)).make();
    }

    /**
     * <code>make</code> is an abstract method which must be
     * overridden by the various concrete Factories subclassed from
     * it. These are present as static inner classes within their
     * respective SeqFileFormer implementations.
     *
     * @return a <code>SeqFileFormer</code> value.
     */
    protected abstract SeqFileFormer make();
}
