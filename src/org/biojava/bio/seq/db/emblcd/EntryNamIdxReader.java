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

public class EntryNamIdxReader extends EmblCDROMIndexReader
{
    private byte [] rPosBytes = new byte [4];
    private byte [] sPosBytes = new byte [4];
    private byte [] fNumBytes = new byte [2];

    public EntryNamIdxReader(InputStream input)
    {
        super(input);
    }

    public Object [] readRecord() throws IOException
    {
        byte [] enRecord = readRawRecord();

	// The variable part of record is the id. Other parts are
	// long, long, int which sum to 10
	int idLen = enRecord.length - 10;
	byte [] idBytes =  new byte [idLen];

        System.arraycopy(enRecord, 0,           idBytes, 0, idLen);
        System.arraycopy(enRecord, idLen,     rPosBytes, 0, 4);
        System.arraycopy(enRecord, idLen + 4, sPosBytes, 0, 4);
        System.arraycopy(enRecord, idLen + 8, fNumBytes, 0, 2);

        sb.delete(0, sb.length());
        String       seqID = parseString(sb, idBytes);
        Long     rPosition = new Long(parseInt4(rPosBytes));
        Long     sPosition = new Long(parseInt4(sPosBytes));
        Integer fileNumber = new Integer(parseInt2(fNumBytes));

        return new Object [] { seqID, rPosition, sPosition, fileNumber };
    }
}
