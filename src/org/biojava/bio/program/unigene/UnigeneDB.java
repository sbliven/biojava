package org.biojava.bio.program.unigene;

import org.biojava.bio.*;
import org.biojava.utils.*;

public interface UnigeneDB
extends Changeable {
  public UnigeneCluster getCluster(String clusterID)
  throws BioException;

  public UnigeneCluster addCluster(UnigeneCluster cluster)
  throws BioException, ChangeVetoException;
}
