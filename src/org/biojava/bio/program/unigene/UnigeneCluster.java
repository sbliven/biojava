package org.biojava.bio.program.unigene;

import org.biojava.bio.Annotation;
import org.biojava.bio.Annotatable;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.db.SequenceDB;

/**
 * <p>
 * A single unigene cluster.
 * </p>
 *
 * <p>
 * This represents all of the information available about a single unigene
 * cluster. This includes the sequences that are in it, the unique sequence that
 * is its representative, the cluster id and all the annotation available via
 * the data file. Much of the annotation may be accessible via the
 * getAnnotation() method.
 * </p>
 *
 * @author Matthew Pocock
 */
public interface UnigeneCluster
extends Annotatable {
  public String getID();
  public String getTitle();
  public SequenceDB getAll();
  public Sequence getUnique();
}
