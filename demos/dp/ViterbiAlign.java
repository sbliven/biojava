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

import java.util.*;
import java.net.*;
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import com.sun.xml.tree.*;
import com.sun.xml.parser.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.tools.*;
import org.biojava.bio.dp.*;


/**
 * This demonstrates aligning a sequence to a model.
 * <P>
 * Use: <code>ViterbiAlign model.xml sequence.fa</code>
 * <p>
 * The output will contain an alignment of each sequence in
 * <code>sequence.fa</code> to the HMM described in <code>model.xml</code>. The
 * states will be displayed as their single-character symbol.
 * <P>
 * A dynamic-programming object is created from an HMM with the instruction
 * <code>DP dp = new DP(new FlatModel(model))</code>. The object <code>dp</code>
 * can then be used for finding the Viterbi-Path, the forward and backward scores
 * and even generating sequences from the model. In this example, an alignment is
 * generated with <code>StatePath statePath = dp.viterbi(seq)</code>. The
 * <span class="type">StatePath</span> object contains the most probable state
 * path through the model, given that <code>seq</code> was emitted. It also
 * contains <code>seq</code> and the step-wise probability of each state. As it
 * is a <span class="type">StatePath</span> (and not just an
 * <span class="type">Alignment</span>) it also contains the total likelyhood of
 * the alignment: P(seq | model).
 * <P>
 * The program can easily be addapted to produce a fasta-format file containing
 * the state sequence for each input sequence. This is a very convenient way to
 * stoor the alignment for later. Alternatively, you could print out the full
 * state name, rather than just its symbol. By editing the XML file, you can
 * construct almost any single-head HMM and run it through ViterbiAlign to get
 * a state-path. This is often a quick way to test things out.
 * <P>
 * Have fun.
 *
 * @author Matthew Pocock
 */
public class ViterbiAlign {
  public static void main(String args[]) throws Exception {
    if(args.length != 2) {
      throw new Exception("Use: ViterbiAlign model.xml sequence.fa");
    }
    
    try {
      // parse the arguments
      URL baseURL = new URL("file:");
      URL modelURL = new URL(baseURL, args[0]);
      File seqFile = new File(args[1]);
      File stateFile = null;

      // load in the markov model
      InputSource is = Resolver.createInputSource(modelURL, true);
      XmlDocument doc = XmlDocument.createXmlDocument(is, false);
      MarkovModel model = XmlMarkovModel.readModel(doc.getDocumentElement());

      // make alphabets
      Alphabet alpha = model.emissionAlphabet();
      ResidueParser rParser = alpha.getParser("symbol");

      // make dp object
      DP dp = DPFactory.createDP(model);
    
      SequenceFactory sFact = new SimpleSequenceFactory();
      FastaFormat fFormat = new FastaFormat();
      SequenceIterator stateI = null;

      for(SequenceIterator seqI = new StreamReader(new FileInputStream(seqFile),
                                                   fFormat,
                                                   rParser,
                                                   sFact);
          seqI.hasNext(); )
      {
        Sequence seq = seqI.nextSequence();
        ResidueList [] rl = { seq };
        StatePath statePath = dp.viterbi(rl);
        double fScore = dp.forward(rl);
        double bScore = dp.backward(rl);
      
        System.out.println(
          seq.getName() +
          " viterbi: " + statePath.getScore() +
          ", forwards: " + fScore +
          ", backwards: " + bScore
        );
        for(int i = 0; i <= statePath.length() / 60; i++) {
          for(int j = i*60; j < Math.min((i+1)*60, seq.length()); j++) {
            System.out.print(statePath.residueAt(StatePath.SEQUENCE, j+1).getSymbol()); 
          }
          System.out.print("\n");
          for(int j = i*60; j < Math.min((i+1)*60, seq.length()); j++) {
            System.out.print(statePath.residueAt(StatePath.STATES, j+1).getSymbol()); 
          }
          System.out.print("\n");
          System.out.print("\n");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  } 
} 
    
