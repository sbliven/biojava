package org.biojava.utils.io;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
 * Wrapper arround MappedByteBuffers to allow long-indexed access to files
 * larger than 2 gigs.
 *
 * @author Matthews Pocock
 */
public class LongBuffer {
  private List buffers;
  
  public LongBuffer(FileChannel channel, long pos, long size)
  throws IOException {
    buffers = new ArrayList();
    
    while(true) {
      if(size > Integer.MAX_VALUE) {
        buffers.add(
          channel.map(FileChannel.MapMode.READ_WRITE,
          pos, Integer.MAX_VALUE
        ));
        
        pos += Integer.MAX_VALUE;
        size -= Integer.MAX_VALUE;
      } else {
        buffers.add(
          channel.map(FileChannel.MapMode.READ_WRITE,
          pos, size
        ));
        
        break;
      }
    }
  }
  
  public byte get(long pos)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    return buffer.get(offset);
  }
  
  public void put(long pos, byte b)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    buffer.put(offset, b);
  }
  
  public char getChar(long pos)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    return buffer.getChar(offset);
  }
  
  public void putChar(long pos, char c)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    buffer.putChar(offset, c);
  }
  
  public double getDouble(long pos)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    return buffer.getDouble(offset);
  }
  
  public void putDouble(long pos, double d)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    buffer.putDouble(offset, d);
  }
  
  public float getFloat(long pos)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    return buffer.getFloat(offset);
  }
  
  public void putFloat(long pos, float f)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    buffer.putFloat(offset, f);
  }
  
  public int getInt(long pos)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    return buffer.getInt(offset);
  }
  
  public void putInt(long pos, int i)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    buffer.putInt(offset, i);
  }
  
  public long getLong(long pos)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    return buffer.getLong(offset);
  }
  
  public void putLong(long pos, long l)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    buffer.putLong(offset, l);
  }
  
  public short getShort(long pos)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    return buffer.getShort(offset);
  }
  
  public void putShort(long pos, short s)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    buffer.putShort(offset, s);
  }
  
  private int getOffset(long pos) {
    return (int) (pos / (long) Integer.MAX_VALUE);
  }
  
  private int getIndex(long pos) {
    return (int) pos % Integer.MAX_VALUE;
  }
}
