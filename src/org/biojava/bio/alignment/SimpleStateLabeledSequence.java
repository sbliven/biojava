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

import java.util.*;

import org.biojava.bio.seq.*;

public class SimpleStateLabeledSequence implements StateLabeledSequence {
  private ResidueList sequence;
  private ResidueList states;
  private ResidueList scores;
  private DP dp;
  private double score;
  private double [][] posterior;

  private Alignment alignment;
  
  public ResidueList getSequence() {
    return sequence;
  }
  
  public ResidueList getStates() {
    return states;
  }
  
  public ResidueList getScores() {
    return scores;
  }
  
  public double getScore(int column) {
    return ((DoubleAlphabet.DoubleResidue) scores.residueAt(column)).doubleValue();
  }
  
  public DP getDp() {
    return dp;
  }
  
  public double getScore() {
    return score;
  }
  
  public double [][] getPosterior() {
    return posterior;
  }
  
  public int length() {
    return alignment.length();
  }
  
  public Set getSequences() {
    return alignment.getSequences();
  }
  
  public Residue getResidue(ResidueList seq, int col) {
    return alignment.getResidue(seq, col);
  }
  
  public Map getColumn(int column) {
    return alignment.getColumn(column);
  }
  
  public Alignment subAlignment(Set sequences, Location loc) {
    return alignment.subAlignment(sequences, loc);
  }
  
  public Annotation getAnnotation() {
    return alignment.getAnnotation();
  }
  
  public SimpleStateLabeledSequence(ResidueList sequence, ResidueList states,
  ResidueList scores, DP dp, double score, double [][] posterior) {
    this.sequence = sequence;
    this.states = states;
    this.scores = scores;
    this.dp = dp;
    this.score = score;
    this.posterior = posterior;
    
    Set seqSet = new HashSet();
    seqSet.add(sequence);
    seqSet.add(states);
    seqSet.add(scores);
    
    alignment = new SimpleAlignment(seqSet);
  }
}
