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

package org.biojava.bio.seq.db.biosql;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;

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

    public void _addFeature(Feature f) throws ChangeVetoException;
}
