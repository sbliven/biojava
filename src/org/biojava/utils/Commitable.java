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

package org.biojava.utils;

/**
 * Implementations of <code>Commitable</code> support atomic changes
 * from one known state to another via commit/rollback semantics.
 *
 * @author Unknown
 * @author Keith James
 */
public interface Commitable {

    /**
     * <code>commit</code> commits pending changes.
     *
     * @exception NestedException if an error occurs
     */
    public void commit()
        throws NestedException;
  
    /**
     * <code>rollback</code> reverses pending changes to restore
     * initial (or prior commit) state.
     */
    public void rollback();
}
