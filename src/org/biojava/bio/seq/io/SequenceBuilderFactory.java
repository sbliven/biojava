package org.biojava.bio.seq.io;

/**
 * Simple factory for constructing new SequenceBuilder objects.
 *
 * @author Thomas Down
 * @since 1.1 [newio proposal]
 */

public interface SequenceBuilderFactory {
    public SequenceBuilder makeSequenceBuilder();
}
