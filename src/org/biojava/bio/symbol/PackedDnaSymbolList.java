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

package org.biojava.bio.symbol;

import java.io.Serializable;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;

/**
 * a class that implements storage of symbols
 * in packed form (2 symbols per byte).
 *
 * @author David Huen
 * @since 1.2
 */
public class PackedDnaSymbolList
    extends AbstractSymbolList implements Serializable
{
    /*
     * The data is organised at two levels:-
     *  i) underlying storage: byte in my case
     *  ii) cache line       : 2**n number of bytes
     *
     *  in this case all but the terminal cache and
     *  byte have a fixed, known number of bytes
     *  and 4-bit nibbles to process.
     *
     *  in the terminal cache line, the number of bytes
     *  to be processed and the number of nibbles in the
     *  final byte may vary from that in the other cache
     *  lines.
     */

    // class variables
    // sequence length
    int length = 0;

    // 2's exponent expressing number of packed symbols per
    // storage unit (2 packed symbols per storage unit)
    // log2PackedSymsPerStorageUnit and storageUnitOffsetMask
    // MUST be kept consistent
    int log2PackedSymsPerStorageUnit = 1;
    int packedSymsPerStorageUnit = 0;
    int storageUnitOffsetMask = 0x0001;

    // size of the array in multiples of underlying storage units
    // (ignore the zero, just a compiler suppression initialisation).
    int storageUnitSize = 0;

    // array for storing packed symbols of type storage unit
    byte [] packedSymbolArray = null;

    // 2's exponent expressing number of symbols per
    // cache line. (in this implementation, 16 per cache line)
    // log2SymsPerCacheLine and cacheLineOffsetMask
    // MUST be kept consistent
    int log2SymsPerCacheLine = 4;
    int cacheLineOffsetMask = 0x000F;

    // symbol cache
    Symbol [] symbolCache = null;
    int currCacheIndex = -1;

    // terminal cache line/storage unit parameters
    // number of packed symbols in final storage unit
    // (ignore zero value here, initialised later)
    int cacheLineCount = 0;
    int symsInFinalStorageUnit = 0;
    int symsInFinalCacheLine = 0;
    int fullStorageUnitsInFinalCacheLine = 0;
    int log2StorageUnitsPerCacheLine = 0;
    int storageUnitsPerCacheLine = 0;

    static AlphabetIndex alfaIndex = null;

/**
 * constructor taking another symbol list.
 */
    public PackedDnaSymbolList(SymbolList symList)
        throws BioException, IllegalAlphabetException
    {
        // check that SymbolList is on DNA alphabet
        if ( symList.getAlphabet() != DNATools.getDNA() ) throw new IllegalAlphabetException();

        // create cache array
        symbolCache = new Symbol[0x0001<<log2SymsPerCacheLine];

        // compute derived shifts
        log2StorageUnitsPerCacheLine
            = log2SymsPerCacheLine - log2PackedSymsPerStorageUnit;
        storageUnitsPerCacheLine = 0x0001 << log2StorageUnitsPerCacheLine;
        packedSymsPerStorageUnit = 0x0001 << log2PackedSymsPerStorageUnit;

        // compute the size of array required
        length = symList.length();
        storageUnitSize = length >> log2PackedSymsPerStorageUnit;
        symsInFinalStorageUnit = length & storageUnitOffsetMask;

        // create the array
        if (symsInFinalStorageUnit == 0)
            packedSymbolArray = new byte[storageUnitSize];
        else
            packedSymbolArray = new byte[storageUnitSize + 1];


        // compute number of full cache lines
        cacheLineCount = length >> log2SymsPerCacheLine;

        // compute number of storage units in final cache line
        // if = 0, then final line is a full line
        symsInFinalCacheLine = length & cacheLineOffsetMask;

        // note that this is for FILLED storage units.
        fullStorageUnitsInFinalCacheLine
            = symsInFinalCacheLine >> log2PackedSymsPerStorageUnit;

        // get access to an AlphabetIndex
        if (alfaIndex == null) alfaIndex = new FullDnaAlphabetIndex();

        // now copy over contents into array
        // I will assume LSB first!
        int symbolPointer = 1;
        int bytePointer = 0;

        // load the all the completely filled storage units.
        // note that the types will have to be changed here if the storage unit is changed
        byte currByte = 0;
//        System.out.println("storageUnitSize: " + storageUnitSize);
        for (int i=0; i<storageUnitSize; i++) {

            // each byte consists of 4 bit nibbles
            // process each separately
            for (int j=0; j < 2; j++) {
                currByte = (byte) (currByte<<4 | alfaIndex.indexForSymbol(symList.symbolAt(symbolPointer)));
                symbolPointer++;
            }

            packedSymbolArray[bytePointer] = currByte;
//            System.out.println("setting position " + bytePointer + " to " + Integer.toString((int) currByte, 16));
            bytePointer++;
        }

        // now handle the final incompletely filled storage unit
        currByte=0;
//        System.out.println("symsInFinalStorageUnit: " + symsInFinalStorageUnit);
        if (symsInFinalStorageUnit != 0) {
            for (int i=0; i<symsInFinalStorageUnit; i++) {
                currByte = (byte) (currByte<<4 | alfaIndex.indexForSymbol(symList.symbolAt(symbolPointer)));
//                System.out.println("value is " + alfaIndex.indexForSymbol(symList.symbolAt(symbolPointer)));
                symbolPointer++;
            }
            packedSymbolArray[bytePointer] = currByte;
//            System.out.println("setting position " + bytePointer + " to " + Integer.toString((int) currByte, 16));
        }
    }

/**
 * constructor taking a byte array previously
 * created by another PackedDnaSymbolList object.
 */
    public PackedDnaSymbolList(int length, byte [] byteArray)
        throws BioException
    {
        // initialise the class variables
        storageUnitSize = length>>log2PackedSymsPerStorageUnit;
        symsInFinalStorageUnit = length & storageUnitOffsetMask;

        // check that the array is correct in size
        int expectedArraySize;
        if (symsInFinalStorageUnit == 0)
            expectedArraySize = storageUnitSize;
        else
            expectedArraySize = storageUnitSize + 1;

        if (byteArray.length < expectedArraySize)
            throw new BioException("array is too small to represent given sequence length.");

        packedSymbolArray = byteArray;
    }

    /*************************/
    /* SymbolList operations */
    /*************************/

    public int length() { return length; }

    public Alphabet getAlphabet() { return DNATools.getDNA(); }

    public Symbol symbolAt(int index)
    {
        // index is origin1;

        // compute cache index
        if (index < 1) throw new IndexOutOfBoundsException("");
        if (index > length) throw new IndexOutOfBoundsException("");
        int cacheIndex = (index-1)>>log2SymsPerCacheLine;
        int cacheOffset = (index-1) & cacheLineOffsetMask;
//        System.out.println("index,cacheIndex,cacheOffset: " + index + " " + cacheIndex + " " + cacheOffset);

        // check if access is to current cache line
        if (currCacheIndex != cacheIndex) {
            // fill symbol cache

            // do we have a full cache line
            // or is this a partially-filled final cache line?
            int storageUnitIndex = cacheIndex << log2StorageUnitsPerCacheLine;
//            System.out.println("storageUnitIndex,cacheLineCount,symsInFinalCacheLine " + storageUnitIndex + " " + cacheLineCount + " " + symsInFinalCacheLine);
            if ((cacheIndex == cacheLineCount) && (symsInFinalCacheLine !=0))   {
                // incomplete cache line, deal with it
                int symIndex = 0;
                byte currStorageUnit=-1;

                // transfer the filled storage units
//                System.out.println("fullStorageUnitsInFinalCacheLine: " + fullStorageUnitsInFinalCacheLine);
                if (fullStorageUnitsInFinalCacheLine != 0) {
                    for (int i=0; i < fullStorageUnitsInFinalCacheLine; i++) {
                        currStorageUnit = packedSymbolArray[storageUnitIndex];

                    // remember that packedSymbolArray shifts then loads latest nibble into low order end
                    // when decoding, it is necessary to reverse the order here.
                    for (int j=packedSymsPerStorageUnit-1; j >= 0; j--) {
                        symbolCache[symIndex + j] = alfaIndex.symbolForIndex((int)(currStorageUnit & 0x000F));
                        currStorageUnit = (byte) (currStorageUnit>>4);
                    }
                    symIndex += packedSymsPerStorageUnit;
                    storageUnitIndex++;
                    }
                }

                // transfer the last incompletely-filled storage unit
                if ( symsInFinalStorageUnit !=0) {
                    currStorageUnit = packedSymbolArray[storageUnitIndex];
//                    System.out.println("symIndex, symsInFinalStorageUnit: " + symIndex + " " + symsInFinalStorageUnit);
                    for (int j=symsInFinalStorageUnit-1; j >=0; j--) {
                        symbolCache[symIndex+j] = alfaIndex.symbolForIndex((int)(currStorageUnit & 0x000F));
                        currStorageUnit = (byte) (currStorageUnit>>4);
                    }
                }
            }
            else {
                // complete cache line

                // logically-driven version
                int symIndex = 0;
                for (int i=0; i < storageUnitsPerCacheLine; i++) {
                    byte currStorageUnit = packedSymbolArray[storageUnitIndex];

                    // remember that packedSymbolArray shifts then loads latest nibble into low order end
                    // when decoding, it is necessary to reverse the order here.
                    for (int j=packedSymsPerStorageUnit-1; j >= 0; j--) {
                        symbolCache[symIndex + j] = alfaIndex.symbolForIndex((int)(currStorageUnit & 0x000F));
                        currStorageUnit = (byte) (currStorageUnit>>4);
                    }
                    symIndex += packedSymsPerStorageUnit;
                    storageUnitIndex++;
                }

/*
                // unrolled version
                byte currStorageUnit = packedSymbolArray[storageUnitIndex++];
                symbolCache[1] = alfaIndex.symbolForIndex((int)(currStorageUnit & 0x000F));
                symbolCache[0] = alfaIndex.symbolForIndex((int)((currStorageUnit>>4) & 0x000F));

                currStorageUnit = packedSymbolArray[storageUnitIndex++];
                symbolCache[3] = alfaIndex.symbolForIndex((int)(currStorageUnit & 0x000F));
                symbolCache[2] = alfaIndex.symbolForIndex((int)((currStorageUnit>>4) & 0x000F));

                currStorageUnit = packedSymbolArray[storageUnitIndex++];
                symbolCache[5] = alfaIndex.symbolForIndex((int)(currStorageUnit & 0x000F));
                symbolCache[4] = alfaIndex.symbolForIndex((int)((currStorageUnit>>4) & 0x000F));

                currStorageUnit = packedSymbolArray[storageUnitIndex++];
                symbolCache[7] = alfaIndex.symbolForIndex((int)(currStorageUnit & 0x000F));
                symbolCache[6] = alfaIndex.symbolForIndex((int)((currStorageUnit>>4) & 0x000F));

                currStorageUnit = packedSymbolArray[storageUnitIndex++];
                symbolCache[9] = alfaIndex.symbolForIndex((int)(currStorageUnit & 0x000F));
                symbolCache[8] = alfaIndex.symbolForIndex((int)((currStorageUnit>>4) & 0x000F));

                currStorageUnit = packedSymbolArray[storageUnitIndex++];
                symbolCache[11] = alfaIndex.symbolForIndex((int)(currStorageUnit & 0x000F));
                symbolCache[10] = alfaIndex.symbolForIndex((int)((currStorageUnit>>4) & 0x000F));

                currStorageUnit = packedSymbolArray[storageUnitIndex++];
                symbolCache[13] = alfaIndex.symbolForIndex((int)(currStorageUnit & 0x000F));
                symbolCache[12] = alfaIndex.symbolForIndex((int)((currStorageUnit>>4) & 0x000F));

                currStorageUnit = packedSymbolArray[storageUnitIndex++];
                symbolCache[15] = alfaIndex.symbolForIndex((int)(currStorageUnit & 0x000F));
                symbolCache[14] = alfaIndex.symbolForIndex((int)((currStorageUnit>>4) & 0x000F));
*/

            }
            currCacheIndex = cacheIndex;
        }

        return symbolCache[cacheOffset];
    }

/**
 * returns the byte array backing the SymbolList.
 * (can be written out and stored).
 */
    public byte [] getArray()
    {
        return packedSymbolArray;
    }
}


