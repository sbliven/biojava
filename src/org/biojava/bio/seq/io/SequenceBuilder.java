package org.biojava.bio.seq.io;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * Interface for objects which accumulate state via SeqIOListener,
 * then construct a Sequence object.
 *
 * <p>
 * It is possible to build `transducer' objects which implement this
 * interface and pass on filtered notifications to a second, underlying
 * SequenceBuilder.  In this case, they should provide a
 * <code>makeSequence</code> method which delegates to the underlying
 * SequenceBuilder.
 * </p>
 *
 * @author Thomas Down
 * @since 1.1 [newio proposal]
 */

public interface SequenceBuilder extends SeqIOListener {
    /**
     * Return the Sequence object which has been constructed
     * by this builder.  This method is only expected to succeed
     * after the endSequence() notifier has been called.
     */

    public Sequence makeSequence() throws BioException;
}
