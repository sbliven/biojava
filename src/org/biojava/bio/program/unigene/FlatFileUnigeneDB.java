package org.biojava.bio.program.unigene;

import java.io.*;
import java.util.*;

import org.biojava.utils.*;
import org.biojava.utils.cache.*;
import org.biojava.utils.io.*;
import org.biojava.bio.*;
import org.biojava.bio.program.indexdb.*;
import org.biojava.bio.program.tagvalue.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;

import org.biojava.bio.program.indexdb.IndexStore;

class FlatFileUnigeneDB
implements UnigeneDB {
  private final IndexStore dataStore;
  private final IndexStore liStore;
  private final IndexStore uniqueStore;
  private final IndexStore allStore;
  
  private final Map clusterCache;
  private final ParserListener dataPL;
  private final Parser dataParser;
  
  public FlatFileUnigeneDB(
    IndexStore dataStore,
    IndexStore liStore,
    IndexStore uniqueStore,
    IndexStore allStore
  ) throws BioException {
    this.dataStore = dataStore;
    this.liStore = liStore;
    this.uniqueStore = uniqueStore;
    this.allStore = allStore;
    
    try {
      clusterCache = new WeakValueHashMap();
      dataPL = UnigeneTools.buildDataParser(new AnnotationBuilder(
        UnigeneTools.UNIGENE_ANNOTATION
      ));
      dataParser = new Parser();
    } catch (ParserException pe) {
      throw new BioException(pe, "Could not initialize unigene DB");
    }
  }
  
  public UnigeneCluster getCluster(String clusterID)
  throws BioException {
    UnigeneCluster cluster = (UnigeneCluster) clusterCache.get(clusterID);
    if(cluster == null) {
      Record rec = dataStore.get(clusterID);
      synchronized(dataParser) {
        cluster = (UnigeneCluster) clusterCache.get(clusterID);
        if(cluster == null) { // break race condition
          try {
            RandomAccessReader rar = new RandomAccessReader(rec.getFile());
            rar.seek(rec.getOffset());
            BufferedReader reader = new BufferedReader(rar);
            dataParser.read(reader, dataPL.getParser(), dataPL.getListener());
          } catch (IOException ioe) {
            throw new BioException(ioe, "Failed to load cluster: " + clusterID);
          } catch (ParserException pe) {
            throw new BioException(pe, "Failed to parse cluster: " + clusterID);
          }
        }
        cluster = new AnnotationCluster(
          ((AnnotationBuilder) dataPL.getListener()).getLast()
        );
        clusterCache.put(clusterID, cluster);
      }
    }
    return cluster;
  }
  
  public SequenceDB getAll(String clusterID) {
    return null;
  }
  
  public Sequence getUnique(String clusterID) {
    return null;
  }
  
  private class AnnotationCluster
  extends Unchangeable
  implements UnigeneCluster {
    private Annotation ann;
    
    public AnnotationCluster(Annotation ann) {
      this.ann = ann;
    }
    
    public String getID() {
      return (String) ann.getProperty("ID");
    }
    
    public String getTitle() {
      return (String) ann.getProperty("TITLE");
    }
    
    public SequenceDB getAll() {
      return FlatFileUnigeneDB.this.getAll(getID());
    }
    
    public Sequence getUnique() {
      return FlatFileUnigeneDB.this.getUnique(getID());
    }
    
    public Annotation getAnnotation() {
      return ann;
    }
  }
}
