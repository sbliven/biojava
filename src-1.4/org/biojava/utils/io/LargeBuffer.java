package org.biojava.utils.io;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import org.biojava.utils.Constants;

/**
 * Wrapper arround MappedByteBuffers to allow long-indexed access to files
 * larger than 2 gigs.
 *
 * @author Matthews Pocock
 */
public class LargeBuffer {
  /*
   * We will set up MappedByteBuffers that are responsible for PAGE_SIZE
   * bytes. Unfortunately, word boundaries are not aligned, so someone could
   * try to write a double to the last byte in a buffer. So, 
   */

  private static long PAGE_SIZE = Integer.MAX_VALUE - Constants.BYTES_IN_LONG;
  private static long PAGE_OVERLAP = Constants.BYTES_IN_LONG;
  
  static {
    PAGE_OVERLAP = Constants.BYTES_IN_LONG;
    PAGE_SIZE = Integer.MAX_VALUE - PAGE_OVERLAP;
  }
  
  private List buffers;
  private long position = 0;
  
  public LargeBuffer(
    FileChannel channel,
    FileChannel.MapMode mode,
    long pos,
    long size
  ) throws IOException {
    buffers = new ArrayList();
    
    while(true) {
      if(size > PAGE_SIZE) {
        buffers.add(channel.map(mode, pos, PAGE_SIZE + PAGE_OVERLAP));
        
        pos  += PAGE_SIZE;
        size -= PAGE_SIZE;
      } else {
        buffers.add(channel.map(mode, pos, size));
        
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
  
  public byte get()
  throws IndexOutOfBoundsException {
    byte val = get(position);
    position += Constants.BYTES_IN_BYTE;
    return val;
  }
  
  public void put(long pos, byte b)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    buffer.put(offset, b);
  }
  
  public void put(byte val)
  throws IndexOutOfBoundsException {
    put(position, val);
    position += Constants.BYTES_IN_BYTE;
  }
  
  public char getChar(long pos)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    return buffer.getChar(offset);
  }
  
  public char getChar()
  throws IndexOutOfBoundsException {
    char val = getChar(position);
    position += Constants.BYTES_IN_CHAR;
    return val;
  }
  
  public void putChar(long pos, char c)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    buffer.putChar(offset, c);
  }
  
  public void putChar(char val)
  throws IndexOutOfBoundsException {
    putChar(position, val);
    position += Constants.BYTES_IN_CHAR;
  }
  
  public double getDouble(long pos)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    return buffer.getDouble(offset);
  }
  
  public double getDouble()
  throws IndexOutOfBoundsException {
    double val = getDouble(position);
    position += Constants.BYTES_IN_DOUBLE;
    return val;
  }
  
  public void putDouble(long pos, double d)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    buffer.putDouble(offset, d);
  }
  
  public void putDouble(double val)
  throws IndexOutOfBoundsException {
    putDouble(position, val);
    position += Constants.BYTES_IN_DOUBLE;
  }
  
  public float getFloat(long pos)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    return buffer.getFloat(offset);
  }
  
  public float getFloat()
  throws IndexOutOfBoundsException {
    float val = getFloat(position);
    position += Constants.BYTES_IN_FLOAT;
    return val;
  }
  
  public void putFloat(long pos, float f)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    buffer.putFloat(offset, f);
  }
  
  public void putFloat(float val)
  throws IndexOutOfBoundsException {
    putFloat(position, val);
    position += Constants.BYTES_IN_FLOAT;
  }
  
  public int getInt(long pos)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    return buffer.getInt(offset);
  }
  
  public int getInt()
  throws IndexOutOfBoundsException {
    int val = getInt(position);
    position += Constants.BYTES_IN_INT;
    return val;
  }
  
  public void putInt(long pos, int i)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    buffer.putInt(offset, i);
  }
  
  public void putInt(int val)
  throws IndexOutOfBoundsException {
    putInt(position, val);
    position += Constants.BYTES_IN_INT;
  }
  
  public long getLong(long pos)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    return buffer.getLong(offset);
  }
  
  public long getLong()
  throws IndexOutOfBoundsException {
    long val = getLong(position);
    position += Constants.BYTES_IN_LONG;
    return val;
  }
  
  public void putLong(long pos, long l)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    buffer.putLong(offset, l);
  }
  
  public void putLong(long val)
  throws IndexOutOfBoundsException {
    putLong(position, val);
    position += Constants.BYTES_IN_LONG;
  }
  
  public short getShort(long pos)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    return buffer.getShort(offset);
  }
  
  public short getShort()
  throws IndexOutOfBoundsException {
    short val = getShort(position);
    position += Constants.BYTES_IN_SHORT;
    return val;
  }
  
  public void putShort(long pos, short s)
  throws IndexOutOfBoundsException {
    int offset = getOffset(pos);
    int index = getIndex(pos);
    
    MappedByteBuffer buffer = (MappedByteBuffer) buffers.get(index);
    buffer.putShort(offset, s);
  }
  
  public void putShort(short val)
  throws IndexOutOfBoundsException {
    putShort(position, val);
    position += Constants.BYTES_IN_SHORT;
  }
  
  public long position() {
    return position;
  }
  
  public void position(long pos) {
    this.position = pos;
  }
  
  private int getOffset(long pos) {
    return (int) (pos % PAGE_SIZE);
  }
  
  private int getIndex(long pos) {
    return (int) (pos / (long) PAGE_SIZE);
  }
  
  public void force() {
    for(Iterator i = buffers.iterator(); i.hasNext(); ) {
      MappedByteBuffer buff = (MappedByteBuffer) i.next();
      buff.force();
    }
  }
}
