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

package org.biojava.bio.seq.db.emblcd;

import java.io.InputStream;
import java.io.IOException;

public class AcnumHitReader extends EmblCDROMIndexReader
{
    private byte [] recNumBytes = new byte [4];

    public AcnumHitReader(InputStream input)
    {
        super(input);
    }

    public Object [] readRecord() throws IOException
    {
        byte [] rnRecord = readRawRecord();

        System.arraycopy(rnRecord, 0, recNumBytes, 0, 4);

        Long rNumber = new Long(parseInt4(recNumBytes));

        return new Object [] { rNumber };
    }
}
