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

public class AcnumTrgReader extends EmblCDROMIndexReader
{
    private byte [] recNumBytes = new byte [4];
    private byte [] recTotBytes = new byte [4];

    public AcnumTrgReader(InputStream input)
    {
        super(input);
    }

    public Object [] readRecord() throws IOException
    {
        byte [] acRecord = readRawRecord();

	// The variable part of record is the acc. Other parts are
	// long, long which sum to 8
	int accLen = acRecord.length - 8;
	byte [] accBytes = new byte [accLen];

        System.arraycopy(acRecord, 0, recNumBytes, 0, 4);
        System.arraycopy(acRecord, 4, recTotBytes, 0, 4);
        System.arraycopy(acRecord, 8, accBytes,    0, accLen);

        sb.delete(0, sb.length());
        Long  rNumber = new Long(parseInt4(recNumBytes));
        Long   rTotal = new Long(parseInt4(recTotBytes));
        String seqAcc = parseString(sb, accBytes);

        return new Object [] { rNumber, rTotal, seqAcc };
    }
}
