package org.biojava.directory;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;

/**
 * Service provider interface for Bio-directory.
 *
 * <p>
 * SequenceDBFactories are generally discovered by attempting to
 * load the class <code>org.biojava.directory.seqdb_providers.&lt;meta&gt;.Factory</code>
 * where <em>meta</em> is the meta-tag in the configuration file.
 * </p>
 *
 * @author Thomas Down
 */

public interface SequenceDBFactory {
    public SequenceDB getSequenceDB(String name, String locator) throws BioException;
}
