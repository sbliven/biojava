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

import java.util.Map;
import java.util.HashMap;

import org.biojava.bio.BioException;

/**
 * <code>SeqFileFormerFactory</code> is an abstract base class for
 * factories which create instances of <code>SeqFileFormer</code>. A
 * <code>SeqFileFormer</code> is dynamically loaded by a
 * <code>SequenceFormat</code> when it needs to write a complex file
 * format to a <code>PrintStream</code>. Factories subclassed from
 * <code>SeqFileFormerFactory</code> are present as inner classes
 * within <code>SeqFileFormer</code> implementations.
 *
 * @author Keith James
 * @author Greg Cox
 * @since 1.2
 * @see <code>SequenceFormat</code>
 * @see <code>SequenceFileFomer</code>
 */
public abstract class SeqFileFormerFactory
{
    private static Map factories = new HashMap();

    /**
     * <p<code>addFactory</code> installs the static factories
     * subclassed from <code>SeqFileFormerFactory</code> into its
     * factories hash, indexed by file format name. This method is
     * called from within a static block in each implementation of
     * <code>SeqFileFormer</code>. Thus, when the class is dynamically
     * loaded its factory is automatically installed.</p>
     *
     * <p>The valid arguments are described in the public static final
     * <code>Map</code> </code>SequenceFormat.FORMATS</code>. Those
     * currently available are Embl or Genbank. Capitalization is
     * important.</p>
     *
     * <p>How the public methods see this depends on the
     * implementation of <code>SequenceFormat</code>; see the
     * <code>writeSequence</code> method in each
     * implementation. Current implementations are
     * <strong>not</strong> case-sensitive because they check the
     * argument to <code>writeSequence</code> using
     * <code>equalsIgnoreCase()</code> before calling this method
     * (which <strong>is</strong> case-sensitive) with the correct
     * case.</p>
     *
     * @param format a <code>String</code> identifer which specifies
     * the file format name.
     * @param factory a <code>SeqFileFormerFactory</code> to be
     * installed. 
     *     
     * @see <code>EmblLikeFormat</code>
     * @see <code>GenbankFormat</code>
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
     * @return a <code>SeqFileFormer</code>.
     *
     * @exception BioException if a non-existent file format is
     * requested.
     */
    public static final SeqFileFormer makeFormer(String format)
	throws BioException
    {
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
     * @return a <code>SeqFileFormer</code>.
     */
    protected abstract SeqFileFormer make();
}
