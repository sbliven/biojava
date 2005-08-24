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

/*
 * CRC64Checksum.java
 *
 * Created on August 24, 2005, 1:10 PM
 */

package org.biojavax.utils;

import java.util.zip.Checksum;

/**
 *
 * @author Richard Holland
 */
public class CRC64Checksum implements Checksum {
        
    private final static long[] CRC64Tableh = new long[256];
    private final static long[] CRC64Tablel = new long[256];
    
    // Construct the CRC64Checksum lookup tables.
    static {
        long POLY64REVh = 0xd8000000;
        
        /*
        for (my $i=0; $i<256; $i++) {
             my $partl = $i;
             my $parth = 0;
             for (my $j=0; $j<8; $j++) {
                 my $rflag = $partl & 1;
                 $partl >>= 1;
                 $partl |= (1 << 31) if $parth & 1;
                 $parth >>= 1;
                 $parth ^= $POLY64REVh if $rflag;
             }
             $CRCTableh[$i] = $parth;
             $CRCTablel[$i] = $partl;
         }
         */
        for (int i = 0; i < 256; i++) {
            long partl = (long)i;
            long parth = 0;
            for (int j = 0; j < 8; j++) {
                boolean rflag = (partl & 1L) > 0L;
                partl >>= 1L;
                if ((parth & 1L) > 0L) partl |= (1L << 31L);
                parth >>= 1L;
                if (rflag) parth ^= POLY64REVh;
            }
            CRC64Tableh[i] = parth;
            CRC64Tablel[i] = partl;
        }
    }
    
    private long crcl;
    private long crch;
    
    /**
     * Creates a new instance of CRC64Checksum
     */
    public CRC64Checksum() {
        this.reset();
    }
    
    public void reset() {
        this.crcl = 0L;
        this.crch = 0L;
    }
    
    public void update(int c) {
        long shr = (this.crch & 0xff) << 24L;
        long templh = (this.crch >> 8L);
        long templl = (this.crcl >> 8L) | shr;
        int tableindex = (int)((this.crcl ^ (long)c) & 0xff);
        this.crch = templh ^ CRC64Tableh[tableindex];
        this.crcl = templl ^ CRC64Tablel[tableindex];
    }
    
    public void update(byte[] values, int offset, int len) {
        for (int i = offset; i < offset+len; i++) this.update((int)values[i]);
    }
    
    public long getValue() {
        return (this.crch<<32L) | this.crcl;
    }
    
    /**
     * Displays the current CRC64Checksum checksum as a 16-digit hex string.
     * The algorithm is a copy of that found in BioPerl Swissprot parser.
     * Note that we use longs then cast them to avoid the lack of an
     * unsigned int in Java. Longs are 64-bit but we are only using the
     * bottom 32 bits. An int is 32-bit but encodes sign so we can get amusing
     * results if we don't allow for this.
     * @return the CRC64Checksum checksum.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(StringTools.leftPad(Long.toHexString(crch), '0', 8));
        sb.append(StringTools.leftPad(Long.toHexString(crcl), '0', 8));
        return sb.toString();
    }
    
}
