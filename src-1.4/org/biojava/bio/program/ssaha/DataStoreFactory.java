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
 * Builds a data store.
 * <p>
 * This should realy be modified to allow multiple implementations of data
 * stores to be built in a uniform manner.
 *
 * @author Matthew Pocock
 */
public class DataStoreFactory {
  private static int INT_BYTES = 32 / 8;
  private static int SHORT_BYTES = 16 / 8;
  private static int CHAR_BYTES = 16 / 8;
  private static int BYTE_BYTES = 8 / 8;
  
  /**
   * Get a pre-built data store associated with a file.
   *
   * @param storeFile  the File to map in as a data store
   * @return the DataStore made by mapping the file
   *
   * @throws IOException if the file could not be mapped
   */
  public DataStore getDataStore(File storeFile)
  throws IOException {
    return new MappedDataStore(storeFile);
  }
  
  /**
   * Build a new DataStore.
   *
   * @param storeFile  the file to store the data store
   * @param seqDB  the SequenceDB to store in the data store
   * @param packing  the Packing used to bit-encode the sequences
   * @param wordLength the number of symbols per word
   * @param threshold  the number of times a word must appear to be ignored
   *
   * @throws IllegalAlphabetException if the packing does not agree with
   *         the sequences
   * @throws BioException if there is a problem building the data store
   */
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
    
    final int structDataSize = 6 * INT_BYTES + packingStream.toByteArray().length;
    
    final int hashTablePos;
    final int hitTablePos;
    final int nameArrayPos;
    final int nameTablePos;
    
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
    int hashTableSize = (words + 1) * (int) INT_BYTES;
    //System.out.println("Allocated:\t" + hashTableSize);
    final MappedByteBuffer hashTable_MB = channel.map(
      FileChannel.MapMode.READ_WRITE,
      hashTablePos,
      hashTableSize
    );
    final IntBuffer hashTable = hashTable_MB.asIntBuffer();
    hashTable.put(0, hashTableSize); // write length of k-tuple array
    
    // initialize counts to zero
    for(int i = 0; i < words; i++) {
      hashTable.put(i+1, 0);
    }
    hashTable.position(0);
    
    // 1st pass
    // writes counts as ints for each k-tuple
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
    
    // map the space for sequence index->name
    //
    nameArrayPos = hashTablePos + hashTableSize;
    int nameArraySize = (seqCount + 1) * INT_BYTES;
    //System.out.println("seqCount:\t" + seqCount);
    //System.out.println("nameArraySize:\t" + nameArraySize);
    final MappedByteBuffer nameArray_MB = channel.map(
      FileChannel.MapMode.READ_WRITE,
      nameArrayPos,
      nameArraySize
    );
    final IntBuffer nameArray = nameArray_MB.asIntBuffer();
    nameArray.put(0, nameArraySize);
    
    // map the space for sequence names as short length, char* name
    //
    nameTablePos = nameArrayPos + nameArraySize;
    int nameTableSize =
      INT_BYTES +
      seqCount * INT_BYTES +
      nameChars * CHAR_BYTES;
    //System.out.println("nameTableSize:\t" + nameTableSize);
    final MappedByteBuffer nameTable = channel.map(
      FileChannel.MapMode.READ_WRITE,
      nameTablePos,
      nameTableSize
    );
    nameTable.putInt(0, nameTableSize);
    nameTable.position(INT_BYTES);
    
    // add up the number of k-tuples
    //
    int kmersUsed = 0;
    int hitCount = 0;
    for(int i = 0; i < words; i++) {
      int counts = hashTable.get(i + 1);
      if(counts > 0 && counts < threshold) {
        hitCount++;
        kmersUsed += counts;
      }
    }
    
    // map the space for hits
    hitTablePos = nameTablePos + nameTableSize;
    long hitTableSize =
      (long) INT_BYTES +                            // size
      (long) kmersUsed * (INT_BYTES + INT_BYTES) +  // list elements
      (long) hitCount * INT_BYTES;                  // size of lists
    System.out.println("hitTableSize:\t" + hitTableSize);
    System.out.println("hitTableSize:\t" + (int) hitTableSize);
    System.out.println("hitTablePos:\t" + hitTablePos);
    final MappedByteBuffer hitTable = channel.map(
      FileChannel.MapMode.READ_WRITE,
      hitTablePos,
      (int) hitTableSize
    );
    hitTable.putInt(0, (int) hitTableSize);
    hitTable.position(INT_BYTES);
    
    // write locations of hit arrays
    int hitOffset = 0;
    for(int i = 0; i < words; i++) {
      int counts = hashTable.get(i+1);
      if(counts > 0 && counts < threshold) {
        try {
        // record location of a block of the form:
        // n,(seqID,offset)1,(seqID,offset)2,...,(seqID,offset)n
        if(hitOffset < 0) {
          throw new IndexOutOfBoundsException("Hit offset negative");
        }
        hashTable.put(i + 1, hitOffset); // wire hash table to hit table
        hitTable.putInt(hitOffset + INT_BYTES, 0); // initialy we have no hits
        hitOffset +=
          INT_BYTES +
          counts * (INT_BYTES + INT_BYTES);
        } catch (IndexOutOfBoundsException e) {
          System.out.println("counts:\t" + counts);
          System.out.println("word:\t" + i);
          System.out.println("hitOffset:\t" + hitOffset);
          throw e;
        }
      } else {
        // too many hits - set the number of hits to the flag value -1
        hashTable.put(i + 1, -1);
      }
    }
    
    // 2nd parse
    // write sequence array and names
    // write hitTable
    int seqNumber = 0;
    nameTable.position(INT_BYTES);
    for(SequenceIterator i = seqDB.sequenceIterator(); i.hasNext(); ) {
      Sequence seq = i.nextSequence();
      
      if(seq.length() > wordLength) {
        try {
          
          // write sequence name reference into nameArray
          nameArray.put(seqNumber + 1, nameTable.position()-INT_BYTES);
          
          // write sequence name length and chars into nameTable
          String name = seq.getName();
          nameTable.putInt(name.length());
          for(int j = 0; j < name.length(); j++) {
            nameTable.putChar((char) name.charAt(j));
          }
          
          // write k-mer seq,offset
          int word = PackingFactory.primeWord(seq, wordLength, packing);
          writeRecord(hashTable, hitTable, 1, seqNumber, word);
          for(int j = wordLength+2; j <= seq.length(); j++) {
            word = PackingFactory.nextWord(seq, word, j, wordLength, packing);
            writeRecord(hashTable, hitTable, j - wordLength, seqNumber, word);
          }
        } catch (BufferOverflowException e) {
          System.out.println("name:\t" + seq.getName());
          System.out.println("seqNumber:\t" + seqNumber);
          System.out.println("na pos:\t" + nameArray.position());
          System.out.println("nt pos:\t" + nameTable.position());
          throw e;
        }
        seqNumber++;
      }
    }
    
    //validateNames(seqCount, nameArray, nameTable);
    
    final MappedByteBuffer rootBuffer = channel.map(
      FileChannel.MapMode.READ_WRITE,
      0,
      structDataSize
    );
    
    rootBuffer.position(0);
    rootBuffer.putInt(hashTablePos);
    rootBuffer.putInt(hitTablePos);
    rootBuffer.putInt(nameArrayPos);
    rootBuffer.putInt(nameTablePos);
    rootBuffer.putInt(wordLength);
    rootBuffer.putInt(packingStream.toByteArray().length);
    rootBuffer.put(packingStream.toByteArray());
    
    rootBuffer.force();
    hashTable_MB.force();
    hitTable.force();
    nameArray_MB.force();
    nameTable.force();
    
    return getDataStore(storeFile);
  }
  
  private void addCount(IntBuffer buffer, int word) {
    int count = buffer.get(word+1);
    count++;
    buffer.put(word+1, count);
  }
  
  private void writeRecord(
    IntBuffer hashTable,
    MappedByteBuffer hitTable,
    int offset,
    int seqNumber,
    int word
  ) {
    int kmerPointer = hashTable.get(word+1);
    if(kmerPointer != -1) {
      kmerPointer += INT_BYTES;

      int hitCount = hitTable.getInt(kmerPointer);
      int pos = kmerPointer + hitCount * (INT_BYTES + INT_BYTES) + INT_BYTES;
      
      hitTable.position(pos);
      hitTable.putInt(seqNumber);
      hitTable.putInt(offset);
      hitTable.putInt(kmerPointer, hitCount + 1);
    }
  }
  
  private void validateNames(int nameCount, IntBuffer nameArray, MappedByteBuffer nameTable) {
    for(int ni = 0; ni < nameCount; ni++) {
      int pos = nameArray.get(ni + 1);
      if(pos < 0) {
        throw new Error("Negative pos at index: " + ni);
      }
      
      int length = nameTable.getInt(INT_BYTES + pos);
      if(length < 1 || length > 100) {
        throw new Error("Silly sequence length for " + ni + " : " + length);
      }
      
      StringBuffer buff = new StringBuffer(length);
      for(int ci = 0; ci < length; ci++) {
        buff.append(nameTable.getChar(INT_BYTES + pos + INT_BYTES + CHAR_BYTES * ci));
      }
      System.out.println(ni + " " + buff);
    }
  }
}
