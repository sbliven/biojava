package org.biojava.bio.program.indexdb;

import java.util.Map;
import java.util.List;
import java.io.File;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;

public interface IndexStore {
  public Record get(String id)
  throws BioException;
  
  public List get(String id, String namespace)
  throws BioException;
  
  public Annotation getMetaData();
  
  public void writeRecord(
    File file,
    long offset,
    int length,
    String id,
    Map secIDs
  );
}
