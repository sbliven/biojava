package org.biojava.bio.dp;

import org.biojava.bio.BioException;

/**
 * Flags an object as being able to register itself with a model trainer.
 *
 * @author Matthew Pocock
 */
public interface Trainable {
  /**
   * Perform any registration that is necisary with mt.
   * <P>
   * This may include registering handelers for transition or emission counts,
   * or registering other Trainable objects with the ModelTrainer.
   *
   * @param mt  the ModelTrainer that encapsulates the training environment
   * @return a StateTrainer
   */
  public void registerWithTrainer(ModelTrainer mt)
  throws BioException;
}
