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

package org.biojava.directory;

import java.util.Map;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.db.SequenceDBLite;

/**
 * <code>SequenceDBProvider</code> describes a source of sequence
 * databases defined by the OBDA standard.
 *
 * @author Keith James
 */
public interface SequenceDBProvider {

    /**
     * <code>getName</code> returns the name of a provider.
     *
     * @return a <code>String</code>.
     */
    public String getName();

    /**
     * <code>getSequenceDB</code> retrieves a reference to a database.
     *
     * @param config a <code>Map</code> containing configuration.
     *
     * @return a <code>SequenceDBLite</code>.
     *
     * @exception RegistryException if the registry fails.
     * @exception BioException if an error occurs.
     */
    public SequenceDBLite getSequenceDB(Map config)
        throws RegistryException, BioException;
}
