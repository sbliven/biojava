/*
 * RichSequenceBuilder.java
 *
 * Created on August 24, 2005, 4:43 PM
 */

package org.biojavax.bio.seq.io;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.SequenceBuilder;
import org.biojavax.bio.seq.RichSequence;

/**
 * An interface for objects that can build <code>RichSequence</code>s.
 * @author Mark Schreiber
 */
public interface RichSequenceBuilder extends RichSeqIOListener, SequenceBuilder{
    
    /**
     * Inherited from <code>SequenceBuilder</code>.
     * Implementations of this for a <code>RichSequenceBuilder</code> should
     * call <code>makeRichSequence()</code>
     * @return a <code>RichSequence</code>
     * @see #makeRichSequence()
     * @throws BioException if it is not possible to build a <code>RichSequence</code>
     */
    public Sequence makeSequence() throws BioException;
    
    /**
     * Build a <code>RichSequence</code>.
     * @return a <code>RichSequence</code>
     * @throws BioException if it is not possible to build a <code>RichSequence</code>
     */
    public RichSequence makeRichSequence() throws BioException;
}
