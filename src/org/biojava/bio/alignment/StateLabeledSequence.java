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


package org.biojava.bio.alignment;

import org.biojava.bio.seq.*;

/**
 * A state labeled sequence.
 * <P>
 * This extends Alignment, constricting it to containing three sequences.
 * It will contain a ResidueList that is a path through a MarkovModel, a
 * ResidueList that could be emitted by that model, and a ResidueList of
 * step-wise scores. In addition, a total score, a DP object and a
 * posterior-probabilities matrix can be added, or may be unmeaningfull.
 */
public interface StateLabeledSequence extends Alignment {
  ResidueList getSequence();
  ResidueList getStates();
  ResidueList getScores();
  DP getDp();
  double getScore(int column);
  double getScore();
  double [][] getPosterior();
}
