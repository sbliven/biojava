/**
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



import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;

import java.lang.*;
import java.util.*;

/**
 * Dumps a biojava sequence object: primarily for debugging
 */
public class SequenceDumper {

  Sequence sequence;
  String offset = "";
  int level = 0;

  public SequenceDumper(Sequence sequence) {
    this.sequence = sequence;
  }

  public void dumpFeatures(Feature f)
  {
    String oldOffset = offset;
    offset = offset.concat("  ");
    level++;

    // dump this feature
    System.out.println(offset + "feature:  " + f + " " + level);

    // dump annotation
    Annotation annotation = f.getAnnotation();
    Set keys = annotation.keys();

    // iterate over keys
    Iterator ki = keys.iterator();
    if (ki.hasNext())  {
      System.out.println(offset + "<annotation>");
      while (ki.hasNext()) {
        Object key = ki.next();
        Object value = annotation.getProperty(key);
        System.out.println(offset + key + " " + value);
      }
    }

    // process child features
    Iterator fi = f.features();

    // dump each feature in turn
    while (fi.hasNext()) {
      Feature currFeature = (Feature) fi.next();
      dumpFeatures(currFeature);
    }

    // restore offset
    offset = oldOffset;
    level--;

  }

  public void dump()
  {
    Iterator fi = sequence.features();
    while (fi.hasNext()) {
      dumpFeatures((Feature) fi.next());
    }

    // dump annotation
    Annotation annotation = sequence.getAnnotation();
    if (annotation != null) {
      Set keys = annotation.keys();

      // iterate over keys
      Iterator ki = keys.iterator();
      System.out.println("  <annotation>");
      while (ki.hasNext()) {
        Object key = ki.next();
        Object value = annotation.getProperty(key);
        System.out.println("  " + key + " " + value);
      }
    }

    // dump sequence summary
    System.out.println("Sequence length is " + sequence.length());
  }
}

