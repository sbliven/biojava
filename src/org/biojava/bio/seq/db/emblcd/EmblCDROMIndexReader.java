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
 * <p><code>EmblCDROMIndexReader</code> reads EMBL CD-ROM format
 * indices from an underlying <code>InputStream</code>. This format is
 * used by the EMBOSS package for database indexing (see programs
 * dbiblast, dbifasta, dbiflat and dbigcg). Indexing produces four
 * binary files with a simple format:</p>
 * 
 * <ul>
 *   <li>division.lkp : master index</li>
 *   <li>entrynam.idx : sequence ID index</li>
 *   <li>   acnum.trg : accession number index</li>
 *   <li>   acnum.hit : accession number auxiliary index</li>
 * </ul>
 *
 * <p>Internally EMBOSS checks for Big-endian architechtures and
 * switches the byte order to Little-endian. This means trouble if you
 * try to read the file using <code>DataInputStream</code>, but at
 * least the binaries are consistent across architechtures. This class
 * carries out the necessary conversion.</p>
 *
 * <p>The EMBL CD-ROM format stores the date in 4 bytes. One byte is
 * unused (the first one), leaving one byte for the day, one for the
 * month and one (!)  for the year.</p>
 *
 * <p>For further information see the EMBOSS documentation, or for a
 * full description, the source code of the dbi programs and the Ajax
 * library.</p>
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public abstract class EmblCDROMIndexReader
{
    protected InputStream input;
    protected StringBuffer sb;

    /**
     * <code>headerParsed</code> is a flag indicating that the 300
     * byte header has been parsed and the contents cached.
     */
    protected boolean headerParsed = false;

    // Header fields
    private byte []      int4 = new byte [4];
    private byte []      int2 = new byte [2];
    private byte []    dbName = new byte [20];
    private byte [] dbRelease = new byte [10];
    private byte []    dbDate = new byte [4];
    // Record field
    private byte []    record;

    long   fileLength;
    long  recordCount;
    int  recordLength;

    String name;
    String release;
    String date;

    /**
     * Creates a new <code>EmblCDROMIndexReader</code> instance. A
     * <code>BufferedInputStream</code> is probably the most suitable.
     *
     * @param input an <code>InputStream</code>.
     */
    public EmblCDROMIndexReader(InputStream input)
    {
        this.input = input;
        sb = new StringBuffer(512);
    }

    /**
     * <code>readFileLength</code> returns the file length in bytes
     * (stored within the file's header by the indexing program). This
     * may be called more than once as the value is cached.
     *
     * @return a <code>long</code>.
     *
     * @exception IOException if an error occurs.
     */
    public long readFileLength() throws IOException
    {
        if (! headerParsed)
            parseHeader();

        return fileLength;
    }

    /**
     * <code>readRecordCount</code> returns the number of records in
     * the file. This may be called more than once as the value is
     * cached.
     *
     * @return a <code>long</code>.
     *
     * @exception IOException if an error occurs.
     */
    public long readRecordCount() throws IOException
    {
        if (! headerParsed)
            parseHeader();

        return recordCount;
    }

    /**
     * <code>readRecordLength</code> returns the record length
     * (bytes). This may be called more than once as the value is
     * cached.
     *
     * @return an <code>int</code>.
     *
     * @exception IOException if an error occurs.
     */
    public int readRecordLength() throws IOException
    {
        if (! headerParsed)
            parseHeader();

        return recordLength;
    }

    /**
     * <code>readDBName</code> returns the database name from the
     * index header. This may be called more than once as the value is
     * cached.
     *
     * @return a <code>String</code>.
     *
     * @exception IOException if an error occurs.
     */
    public String readDBName() throws IOException
    {
        if (! headerParsed)
            parseHeader();

        return name;
    }

    /**
     * <code>readDBRelease</code> returns the database release from
     * the index header. This may be called more than once as the
     * value is cached.
     *
     * @return a <code>String</code>.
     *
     * @exception IOException if an error occurs.
     */
    public String readDBRelease() throws IOException
    {
        if (! headerParsed)
            parseHeader();

        return release;
    }

    /**
     * <code>readDBDate</code> reads the date from the index
     * header. The date is stored in 4 bytes: 0, unused; 1, year; 2,
     * month; 3, day. With a 1 byte year it's not very much use and
     * I'm not sure that the EMBOSS programs set the value correctly
     * anyway.
     *
     * @return a <code>String</code>.
     *
     * @exception IOException if an error occurs.  */
    public String readDBDate() throws IOException
    
    {
        if (! headerParsed)
            parseHeader();

        return date;
    }

    /**
     * <code>readRecord</code> returns an array of objects parsed from
     * a single record. Its content will depend on the type of index
     * file. Concrete subclasses must provide an implementation of
     * this method.
     *
     * @return an <code>Object []</code> array.
     *
     * @exception IOException if an error occurs.
     */
    public abstract Object [] readRecord() throws IOException;

    /**
     * <code>readRawRecord</code> returns the raw bytes of a single
     * record from the index.
     *
     * @return a <code>byte []</code> array.
     *
     * @exception IOException if an error occurs.
     */
    public byte [] readRawRecord() throws IOException
    {
        input.read(record);

        return record;
    }

    /**
     * <code>parseInt4</code> creates a long from Little-endian. Named
     * after the EMBOSS Ajax function which wrote the data.
     *
     * @param int4 a <code>byte []</code> array.
     *
     * @return a <code>long</code>.
     */
    long parseInt4(byte [] int4)
    {
        int result = 0;

        for (int i = 4; --i >= 0;)
        {
            if (int4[i] != 0)
                result += ((int4[i] & 0xff) << (8 * i));
        }

        return result;
    }

    /**
     * <code>parseInt2</code> creates an int from Little-endian. Named
     * after the EMBOSS Ajax function which wrote the data.
     *
     * @param int2 a <code>byte []</code> array.
     *
     * @return an <code>int</code>.
     */
    int parseInt2(byte [] int2)
    {
        int result = 0;

        for (int i = 2; --i >= 0;)
        {
            if (int2[i] != 0)
                result += ((int2[i] & 0xff) << (8 * i));
        }

        return result;
    }

    /**
     * <code>parseDate</code> parses a String from an array of
     * bytes. The date is stored in 4 bytes: 0, unused; 1, year; 2,
     * month; 3, day. With a 1 byte year it's not very much use and
     * I'm not sure that the EMBOSS programs set the value correctly
     * anyway.
     *
     * @param sb a <code>StringBuffer</code>.
     * @param dbDate a <code>byte []</code> array.
     *
     * @return a <code>String</code>.
     */
    String parseDate(StringBuffer sb, byte [] dbDate)
    {
        // The first byte is unused
        for (int i = dbDate.length; --i > 0;)
        {
            sb.append(dbDate[i] + ":");
        }

        // Remove the trailing ':'
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * <code>parseString</code> parses a String from an array of
     * bytes, skipping the empties.
     *
     * @param sb a <code>StringBuffer</code>.
     * @param characters a <code>byte []</code> array.
     *
     * @return a <code>String</code>.
     */
    String parseString(StringBuffer sb, byte [] characters)
    {
        for (int i = 0; i < characters.length; i++)
        {
            if (characters[i] == 0)
                break;

            sb.append((char) characters[i]);
        }

        return sb.toString();
    }

    /**
     * <code>parseHeader</code> carries out a full parse of the 300
     * byte header (common to all the index types) when first
     * encountered.
     *
     * @exception IOException if an error occurs
     */
    private void parseHeader() throws IOException
    {
        input.read(int4);
        fileLength = parseInt4(int4);

        input.read(int4);
        recordCount = parseInt4(int4);

        input.read(int2);
        recordLength = parseInt2(int2);

	System.err.println("Got record length: " + recordLength);

        // Set up array for reading records now that we know their
        // length
        record = new byte [recordLength];

        input.read(dbName);
        sb.delete(0, sb.length());
        name = parseString(sb, dbName);

        input.read(dbRelease);
        sb.delete(0, sb.length());
        release = parseString(sb, dbRelease);

        input.read(dbDate);
        sb.delete(0, sb.length());
        date = parseDate(sb, dbDate);

        // Skip the remainder of the header (padding)
        input.skip(256);

        headerParsed = true;
    }
}
