/**
 * BioJava development code
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

package org.biojava.bio.seq.db.gadfly;

import org.biojava.bio.BioException;


public class InvalidSequenceIDException
    extends BioException
{
    public static final int NO_SEQ_ID = -1;

    int id = NO_SEQ_ID;

    public InvalidSequenceIDException()
    {
        this(null, NO_SEQ_ID, null);
    }

    public InvalidSequenceIDException(String message)
    {
        this(null, NO_SEQ_ID, message);
    }

    public InvalidSequenceIDException(Throwable cause)
    {
        this(cause, NO_SEQ_ID, null);
    }

    public InvalidSequenceIDException(Throwable cause, String message)
    {
        this(cause, NO_SEQ_ID, message);
    }

    public InvalidSequenceIDException(
        Throwable cause, 
        int id, 
        String message)
    {
        super(message, cause);

        this.id = id;
    }

    public int getID() { return id; }
}

