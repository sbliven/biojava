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

public class DivisionLkpReader extends EmblCDROMIndexReader
{
    private byte []  fNumBytes = new byte [2];

    public DivisionLkpReader(InputStream input)
    {
        super(input);
    }

    public Object [] readRecord()  throws IOException
    {
        byte [] divRecord = readRawRecord();

	// The variable part of record is the name. Other parts are
	// int which sum to 2
	int nameLen = divRecord.length - 2;
	byte [] fNameBytes = new byte [nameLen];

        System.arraycopy(divRecord, 0, fNumBytes, 0, 2);
        System.arraycopy(divRecord, 2, fNameBytes, 0, nameLen);

        sb.delete(0, sb.length());
        Integer fileNumber = new Integer(parseInt2(fNumBytes));
        String    fileName = parseString(sb, fNameBytes);

        return new Object [] { fileNumber, fileName };
    }
}
