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

/*
 * RichSequenceIterator.java
 *
 * Created on August 5, 2005, 1:46 PM
 */

package org.biojavax.bio.seq;
import java.util.NoSuchElementException;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.SequenceIterator;


/**
 * Essentially the same as <code>SequenceIterator</code>. It provides a new
 * method that returns <code>RichSequence</code> objects without the need for
 * explicit casting. Implementations of this interface should <b>always</b>
 * return <code>RichSequence</code> objects for both the <code>nextSequence()</code>
 * and <code>nextRichSequence</code> methods.
 *
 * @author Mark Schreiber
 * @see org.biojava.bio.seq.SequenceIterator
 */
public interface RichSequenceIterator extends SequenceIterator{
    public RichSequence nextRichSequence() throws NoSuchElementException, BioException;
}
