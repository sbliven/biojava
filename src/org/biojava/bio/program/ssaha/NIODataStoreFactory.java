package org.biojava.bio.program.ssaha;

import java.io.*;
import java.nio.*;

import java.nio.channels.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;

/**
 * <p>
 * Builder for a datastore that has no practical file size limit.
 * </p>
 *
 * <p>
 * This implementation of the data store factory uses longs as indecies
 * internaly, so can be used with files exceeding 2 gigs in size.
 * </p>
 *
 * <p>
 * The data store file has the following structure.
 * <pre>
 * file: header, hash table, nameTable, hitTable
 *
 * header:
 *   long hashTablePos, // byte offset in file
 *   long hitTablePos,  // byte offset in file
 *   long nameTablePos, // byte offset in file
 *   int wordLength,
 *   int serializedPackingLength,
 *   byte[] serializedPacking
 *
 * hashTable:
 *   int hashTableLength,
 *   long[hashTableLength] hits // byte offset into hitTable
 *
 * nameTable:
 *   int nameTableSize, // size in bytes
 *   (short nameLength, char[nameLength] name)[nameTableSize] names
 *
 * hitTable:
 *   long hitTableSize, // size in bytes
 *   hitTableRecord[hitTableSize] hits
 *
 * hitTableRecord:
 *   int hitCount,
 *   hitRecord[hitCount] hit
 *
 * hit:
 *   long seqOffset, // byte offset into sequence names table
 *   int pos         // biological position in sequence
 * </pre>
 * </p>
 * @author Matthew Pocock
 */
public class NIODataStoreFactory
implements DataStoreFactory {
  public DataStore getDataStore(File storeFile)
  throws IOException {
    //return new NIODataStore(storeFile);
    return null;
  }
  
  public DataStore buildDataStore(
    File storeFile,
    SequenceDB seqDB,
    Packing packing,
    int wordLength,
    int threshold
  ) throws
    IllegalAlphabetException,
    IOException,
    BioException
  {
    ByteArrayOutputStream packingStream = new ByteArrayOutputStream();
    ObjectOutputStream packingSerializer = new ObjectOutputStream(packingStream);
    packingSerializer.writeObject(packing);
    packingSerializer.flush();
    
    final int structDataSize =
      3 * Constants.BYTES_IN_LONG + // positions
      2 * Constants.BYTES_IN_INT +  // word length & packing length
      packingStream.toByteArray().length;
    
    final long hashTablePos;
    final long hitTablePos;
    final long nameTablePos;
    
    storeFile.createNewFile();
    final RandomAccessFile store = new RandomAccessFile(storeFile, "rw");
    final FileChannel channel = store.getChannel();
    
    // allocate array for k-tuple -> hit list
    //System.out.println("Word length:\t" + wordLength);
    int words = 2 << (
      (int) packing.wordSize() *
      (int) wordLength
    );
    //System.out.println("Words:\t" + words);
    
    hashTablePos = structDataSize;
    long hashTableSize =
      Constants.BYTES_IN_INT +         // hash table length
      words * Constants.BYTES_IN_LONG; // hash table entries
    
    //System.out.println("Allocated:\t" + hashTableSize);
    final MappedByteBuffer hashTable_MB = channel.map(
      FileChannel.MapMode.READ_WRITE,
      hashTablePos,
      hashTableSize
    );
    final LongBuffer hashTable = hashTable_MB.asLongBuffer();
    hashTable.put(0, hashTableSize); // write length of k-tuple array
    
    // initialize counts to zero
    for(int i = 0; i < words; i++) {
      hashTable.put(i+1, 0);
    }
    hashTable.position(0);
    
    // 1st pass
    // writes counts as longs for each k-tuple
    // count up the space required for sequence names
    //
    int seqCount = 0;
    int nameChars = 0;
    for(SequenceIterator i = seqDB.sequenceIterator(); i.hasNext(); ) {
      Sequence seq = i.nextSequence();
      if(seq.length() > wordLength) {
        seqCount++;
        nameChars += seq.getName().length();
        
        int word = PackingFactory.primeWord(seq, wordLength, packing);
        //PackingFactory.binary(word);
        addCount(hashTable, word);
        for(int j = wordLength+2; j <= seq.length(); j++) {
          word = PackingFactory.nextWord(seq, word, j, wordLength, packing);
          //PackingFactory.binary(word);
          addCount(hashTable, word);
        }
      }
    }
     
    // map the space for sequence names as short length, char* name
    //
    nameTablePos = hashTablePos + hashTableSize;
    int nameTableSize =
      Constants.BYTES_IN_INT +              // bytes in table
      seqCount * Constants.BYTES_IN_SHORT + // string lengths
      nameChars * Constants.BYTES_IN_CHAR;  // characters
    //System.out.println("nameTableSize:\t" + nameTableSize);
    final MappedByteBuffer nameTable = channel.map(
      FileChannel.MapMode.READ_WRITE,
      nameTablePos,
      nameTableSize
    );
    nameTable.putInt(0, nameTableSize);
    nameTable.position(Constants.BYTES_IN_INT);
    
    // add up the number of k-tuples
    //
    long kmersUsed = 0; // number of kmers with valid hits
    long hitCount = 0;  // total number of individual hits
    for(int i = 0; i < words; i++) {
      long counts = hashTable.get(i + 1);
      if(counts > 0 && counts < threshold) {
        hitCount += counts;
        kmersUsed++;
      }
    }
    
    hitTablePos = nameTablePos + nameTableSize;
    long hitTableSize =
      (long) Constants.BYTES_IN_INT +                            // size
      (long) kmersUsed * (Constants.BYTES_IN_INT + Constants.BYTES_IN_INT) +  // list elements
      (long) hitCount * Constants.BYTES_IN_INT;                  // size of lists
    //System.out.println("hitTableSize:\t" + hitTableSize);
    //System.out.println("hitTableSize:\t" + (int) hitTableSize);
    //System.out.println("hitTablePos:\t" + hitTablePos);
    
    return null;
  }
  
  
  private void addCount(LongBuffer buffer, int word) {
    long count = buffer.get(word+1);
    count++;
    buffer.put(word+1, count);
  }
  
}
