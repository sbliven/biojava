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

package org.biojava.utils.lsid;

/**
 * Factory interface for creating LifeScienceIdentifiers.
 *
 * @author Michael Heuer
 */
public interface LifeScienceIdentifierFactory
{

    /**
     * Create a new LifeScienceIdentifier from the specified
     * authority, namespace, objectId and version.
     *
     * @throws LifeScienceIdentifierFactoryException (runtime)
     *    if the LifeScienceIdentifier cannot be created
     *
     * @param authority identifies the organization
     * @param namespace namespace to scope the identifier value
     * @param objectId identifier of the object within namaspace
     * @param version optional version information
     */
    public LifeScienceIdentifier create(String authority,
					String namespace,
					Object objectId,
					Object version);
}
