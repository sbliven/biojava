package org.biojava.bio.program.indexdb;

import java.util.Map;
import java.util.List;
import java.io.RandomAccessFile;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.utils.io.RAF;

public interface IndexStore {
  public Record get(String id)
  throws BioException;
  
  public List get(String id, String namespace)
  throws BioException;
  
  public Annotation getMetaData();
  
  public void writeRecord(
    RAF file,
    long offset,
    int length,
    String id,
    Map secIDs
  );
}
