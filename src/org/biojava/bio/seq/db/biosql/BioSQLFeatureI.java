package org.biojava.bio.seq.db.biosql;

import org.biojava.bio.*;

/**
 * Internal interface which allows BioSQL some priveleged access
 * to its features.
 *
 * @author Thomas Down
 * @since 1.3
 */

interface BioSQLFeatureI {
    public void _setInternalID(int id);
    public int _getInternalID();

    public void _setAnnotation(Annotation a);
}
