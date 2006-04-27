/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojavax.ga;

import org.biojava.bio.symbol.*;
import org.biojava.utils.*;

/**
 * A GA 'organism' contains one or more Chromosomes
 * @author Mark Schreiber
 * @version 1.0
 * @since 1.5
 */

public interface Organism extends Changeable{

  /**
   * Gets the organisms 'chromosome' sequences
   * @return a <code>SymbolList[]</code>
   */
  public SymbolList[] getChromosomes();

  /**
   * Sets the organisms 'chromosome' sequences.
   * @param chromosomes a <code>SymbolList[]</code>
   * @throws ChangeVetoException if the Chromosome collection of the Organism
   * is unchangable
   */
  public void setChromosomes(SymbolList[] chromosomes) throws ChangeVetoException;

  /**
   * Gets the organisms name
   * @return the name String
   */
  public String getName();

  /**
   * Sets the organisms name
   * @param name the name of the organism.
   * @throws ChangeVetoException if the name may not be changed.
   */
  public void setName(String name) throws ChangeVetoException;

  /**
   * Creates a replica of this <code>Organism</code> with a new name.
   * @param name the new name for the sequence.
   * @return the replicated organism.
   */
  public Organism replicate(String name);

  /**
   * Is the organism Haploid?
   * @return true if it is.
   */
  public boolean isHaploid();

  public static final ChangeType CHROMOSOMES =
      new ChangeType("Chromosomes changed", "ga.Organism", "CHROMOSOMES");

  public static final ChangeType NAME =
      new ChangeType("Name changed", "ga.Organism", "NAME");

}