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


package org.acedb.seq;

import java.util.*;

import org.acedb.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

/**
 * @author Matthew Pocock
 */

public class AceSequence implements Sequence {
  protected AceObject seqObj;
  private String name;
  private ResidueList resList;
  private Annotation annotation;
  private SimpleMutableFeatureHolder fHolder;
  
  public String getName() {
    return name;
  }
  
  public String getURN() {
    return "urn://sequence:acedb/" + getName();
  }
  
  public Alphabet alphabet() {
    return DNATools.getAlphabet();
  }
  
  public Annotation getAnnotation() {
    return annotation;
  }

  public Iterator iterator() {
    return resList.iterator();
  }
  
  public int length() {
    return resList.length();
  }
  
  public Residue residueAt(int index) {
    return resList.residueAt(index);
  }
  
  public List toList() {
    return resList.toList();
  }
  
  public ResidueList subList(int start, int end) {
    return resList.subList(start, end);
  }
  
  public String subStr(int start, int end) {
    return resList.subStr(start, end);
  }
  
  public String seqString() {
    return resList.seqString();
  }
  
  public int countFeatures() {
    return fHolder.countFeatures();
  }
  
  public Iterator features() {
    return fHolder.features();
  }
  
  public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
    return fHolder.filter(ff, recurse);
  }

  /**
   * Add a new feature to this sequence.
   * <P>
   * Ace sequences are currently immutable. This may be changed in the future.
   * This method will always throw an UnsupportedOperationException.
   */
  public Feature createFeature(MutableFeatureHolder fh, Feature.Template template)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException("ACeDB sequences can't be modified");
  }
    
  public AceSequence(Database aceDB, String id) throws AceException, SeqException {
    this.name = id;
    this.fHolder = new SimpleMutableFeatureHolder();
    
    try {
      // load in relevent ACeDB object & construct annotation wrapper
      seqObj = aceDB.getObject(AceType.getClassType(aceDB, "Sequence"), id);
      annotation = new AceAnnotation(seqObj);
      
      // load in the corresponding dna
      Connection con = aceDB.getConnection();
      String selectString = con.transact("Find Sequence " + id);
      String dnaString = con.transact("dna");
      ResidueParser rParser = alphabet().getParser("symbol");
      List rl = new ArrayList();
      StringTokenizer st = new StringTokenizer(dnaString, "\n");
      while(st.hasMoreElements()) {
        String line = st.nextToken();
        if(!line.startsWith(">")) {
          if(line.startsWith("//"))
            break;
          rl.addAll(rParser.parse(line).toList());
        }
      }
      resList = new SimpleResidueList(alphabet(), rl);
      con.dispose();
      
      // Feature template for stuff
      Feature.Template template = new Feature.Template();

      // make features for 'Subsequence' objects
      if(seqObj.contains("Details:")) {
        AceSet dets = seqObj.retrieve("Details:");
        if(dets.contains("Subsequence")) {
          AceSet subSeq = dets.retrieve("Subsequence");
          for(Iterator ssI = subSeq.nameIterator(); ssI.hasNext(); ) {
            String name = (String) ssI.next();
            Reference ref = (Reference) subSeq.retrieve(name);
            IntValue start = (IntValue) AceUtils.pick(ref);
            for(Iterator eI = start.iterator(); eI.hasNext(); ) {
              IntValue end = (IntValue) eI.next();
              Annotation fAnn = new SimpleAnnotation();
              fAnn.setProperty("references", ref);
              template.annotation = fAnn;
              template.location = new RangeLocation(start.toInt(), end.toInt());
              template.source = "ACeDB";
              template.type = name;
              Feature f = new SimpleFeature(this, template);
              fHolder.addFeature(f);
            }
          }
        }
      }
      // make features for each 'Sequence_Feature:' child.
      if(seqObj.contains("Sequence_feature:")) {
        AceSet sf = seqObj.retrieve("Sequence_feature:");
        for(Iterator nameI = sf.nameIterator(); nameI.hasNext(); ) {
          String name = (String) nameI.next();
          AceSet fTypeNode = sf.retrieve(name);
          for(Iterator fI = fTypeNode.nameIterator(); fI.hasNext(); ) {
            AceNode an = (AceNode) fTypeNode.retrieve((String) fI.next());
            IntValue start = (IntValue) an;
            for(Iterator eI = start.iterator(); eI.hasNext(); ) {
              IntValue end = (IntValue) eI.next();
              Annotation fAnn = null;
              if((end.size() > 0)) {
                StringBuffer comment = new StringBuffer();
                Iterator cI = end.nameIterator();
                comment.append(cI.next());
                while(cI.hasNext())
                  comment.append("\n" + cI.next());
                fAnn = new SimpleAnnotation();
                fAnn.setProperty("description", comment.toString());
              }
              template.location = new RangeLocation(start.toInt(), end.toInt());
              template.source = "ACeDB";
              template.type = name;
              template.annotation = fAnn;
              Feature f = new SimpleFeature(this, template);
              fHolder.addFeature(f);
            }
          }
        }
      }
    } catch (Exception ex) {
      if(ex instanceof AceException || ex instanceof SeqException)
        throw (AceException) ex;
      throw new AceException(ex, "Fatal error constructing sequence for " + id);
    }
  }
}
