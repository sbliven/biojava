package org.biojava.bio.program.unigene;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.utils.cache.*;
import org.biojava.bio.*;

class SQLUnigeneDB
extends AbstractChangeable
implements UnigeneDB {
  private final JDBCConnectionPool connPool;
  private final Map clusterCache;
  
  public SQLUnigeneDB(JDBCConnectionPool connPool) {
    this.connPool = connPool;
    this.clusterCache = new WeakValueHashMap();
  }
  
  public UnigeneCluster getCluster(String clusterID)
  throws BioException {
    UnigeneCluster cluster = (UnigeneCluster) clusterCache.get(clusterID);
    
    if(cluster == null) {
      clusterCache.put(clusterID, cluster = fetchCluster(clusterID));
    }
    
    return cluster;
  }
  
  public UnigeneCluster addCluster(UnigeneCluster cluster)
  throws BioException, ChangeVetoException {

    return fetchCluster(cluster.getID());
  }
  
  public UnigeneCluster fetchCluster(String clusterID) {
    return null;
  }
}
