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

/**
 * <code>AcnumHitReader</code> reads the "acnum.hit" file of an EMBL
 * CD-ROM format binary index.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
*/
public class AcnumHitReader extends EmblCDROMIndexReader
{
    private byte [] recNumBytes = new byte [4];

    /**
     * Creates a new <code>AcnumHitReader</code>.
     *
     * @param input an <code>InputStream</code>.
     */
    public AcnumHitReader(InputStream input)
    {
        super(input);
    }

    /**
     * <code>readRecord</code> creates an array of Objects from the
     * raw byte array of a single record.
     *
     * @return an <code>Object []</code> array.
     *
     * @exception IOException if an error occurs.
     */
    public Object [] readRecord() throws IOException
    {
        byte [] rnRecord = readRawRecord();

        System.arraycopy(rnRecord, 0, recNumBytes, 0, 4);

        Long rNumber = new Long(parseInt4(recNumBytes));

        return new Object [] { rNumber };
    }
}
