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


package org.acedb;

import java.io.IOException;

/**
 * A call-level connection to an ACeDB object.  This is only
 * needed if you need to directly execute a command on the server,
 * and might indicate a deficiency in the ACeDBC API.
 *
 * <P>Note that this class is not thread-safe at present.
 *
 * @author Thomas Down
 */

public interface Connection {
    /**
     * Execute a command on an ACeDB database.
     *
     * @param aceCmd A valid ACeDB command (e.g. something that you
     *               might type at the tace prompt)
     * @throws AceException if the command fails, or if there is
     *                      a problem with the connection.
     */

    public String transact(String aceCmd) throws AceException;
    
    /**
     * Safely dispose of this connection.  This will often
     * mean returning it to some pool.  The connection should
     * not be used after this method has been called.
     *
     * @throws AceException if there is a problem with the
     *                      connection.
     */ 

    public void dispose() throws AceException;
}
