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
import org.biojava.bio.seq.io.*;

import org.biojava.bio.program.indexdb.IndexStore;

class FlatFileUnigeneDB
extends Unchangeable
implements UnigeneDB {
  private final BioStore dataStore;
  private final BioStore liStore;
  private final BioStore uniqueStore;
  private final BioStore allStore;
  
  private final Map clusterCache;
  private final Map allCache;
  private final SequenceDB uniqueDB;
  private final ParserListener dataPL;
  private final Parser dataParser;
  private final AnnotationBuilder dataBuilder;
  
  public FlatFileUnigeneDB(
    BioStore dataStore,
    BioStore liStore,
    BioStore uniqueStore,
    BioStore allStore
  ) throws BioException {
    this.dataStore = dataStore;
    this.liStore = liStore;
    this.uniqueStore = uniqueStore;
    this.allStore = allStore;
    
    try {
      clusterCache = new WeakValueHashMap();
      allCache = new WeakValueHashMap();
      
      FastaFormat fasta = new FastaFormat();
      uniqueDB = new CachingSequenceDB(
        new BioIndexSequenceDB(
          uniqueStore,
          fasta
        )
      );

      dataBuilder = new AnnotationBuilder(
        UnigeneTools.UNIGENE_ANNOTATION
      );
      dataPL = UnigeneTools.buildDataParser(dataBuilder);
      dataParser = new Parser();
    } catch (ParserException pe) {
      throw new BioException(pe, "Could not initialize unigene DB");
    }
  }
  
  public UnigeneCluster getCluster(String clusterID)
  throws BioException {
    UnigeneCluster cluster = (UnigeneCluster) clusterCache.get(clusterID);
    if(cluster == null) {
      synchronized(dataParser) {
        cluster = (UnigeneCluster) clusterCache.get(clusterID);
        if(cluster == null) { // break race condition
          try {
            Record rec = dataStore.get(clusterID);
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
        cluster = new AnnotationCluster(dataBuilder.getLast());
        clusterCache.put(clusterID, cluster);
      }
    }
    return cluster;
  }
  
  public SequenceDB getAll(String clusterID)
  throws BioException {
    SequenceDB db = (SequenceDB) allCache.get(clusterID);
    
    if(db == null) {
      synchronized(db) {
        db = (SequenceDB) allCache.get(clusterID);
        if(db == null) {
          allCache.put(clusterID, db = new AllDB(getCluster(clusterID), allStore));
        }
      }
    }
    
    return db;
  }

  public UnigeneCluster addCluster(UnigeneCluster cluster)
  throws BioException, ChangeVetoException {
    throw new ChangeVetoException("Can't alter a file-based unigene installation");
  }
  
  public Sequence getUnique(String clusterID)
  throws IllegalIDException, BioException {
    return uniqueDB.getSequence(clusterID);
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
      try {
        return FlatFileUnigeneDB.this.getAll(getID());
      } catch (BioException be) {
        throw new BioError(be);
      }
    }
    
    public Sequence getUnique() {
      try {
        return FlatFileUnigeneDB.this.getUnique(getID());
      } catch (BioException be) {
        throw new BioError(be);
      }
    }
    
    public Annotation getAnnotation() {
      return ann;
    }
  }
  
  private static class BioIndexSequenceDB
  extends AbstractSequenceDB {
    private final BioStore store;
    private final SequenceFormat format;
    private Set ids = null;
    
    public BioIndexSequenceDB(BioStore store, SequenceFormat format) {
      this.store = store;
      this.format = format;
    }
    
    public Set ids() {
      if(ids == null) {
        ids = new AbstractSet() {
          public int size() {
            return store.getRecordList().size();
          }
          
          public boolean contains(Object o) {
            return store.get((String) o) != null;
          }
          
          public Iterator iterator() {
            return store.getRecordList().iterator();
          }
        };
      }
      
      return ids;
    }
    
    public String getName() {
      return "UniqueStore";
    }
    
    public Sequence getSequence(String id)
    throws BioException {
      try {
        Record rec = store.get(id);
        RandomAccessReader rar = new RandomAccessReader(rec.getFile());
        rar.seek(rec.getOffset());
        BufferedReader reader = new BufferedReader(rar);
        return SeqIOTools.readFastaDNA(reader).nextSequence();
      } catch (IOException ioe) {
        throw new BioException(ioe);
      }
    }
  }
  
  private static class AllDB
  extends AbstractSequenceDB {
    private final Set ids;
    private final BioStore store;
    private final String name;
    
    public AllDB(UnigeneCluster cluster, BioStore store) {
      this.name = "All:" + cluster.getID();
      ids = new HashSet();
      this.store = store;
      
      Annotation ann = cluster.getAnnotation();
      Set seqs = (Set) ann.getProperty("SEQUENCES");
      for(Iterator i = seqs.iterator(); i.hasNext(); ) {
        Annotation sa = (Annotation) i.next();
        ids.add(sa.getProperty("ACC"));
      }
    }
    
    public Set ids() {
      return ids;
    }
    
    public String getName() {
      return name;
    }
    
    public Sequence getSequence(String id)
    throws BioException {
      try {
        Record rec = store.get(id);
        RandomAccessReader rar = new RandomAccessReader(rec.getFile());
        rar.seek(rec.getOffset());
        BufferedReader reader = new BufferedReader(rar);
        return SeqIOTools.readFastaDNA(reader).nextSequence();
      } catch (IOException ioe) {
        throw new BioException(ioe);
      }
    }
  }
}
