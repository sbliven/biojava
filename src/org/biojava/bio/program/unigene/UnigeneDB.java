package org.biojava.bio.program.unigene;

import org.biojava.bio.*;

public interface UnigeneDB {
  public UnigeneCluster getCluster(String clusterID)
  throws BioException;
}
