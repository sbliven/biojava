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

package game;

import java.util.*;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.io.SeqIOAdapter;

/**
 * dumps events being passed to the class
 */

public class SeqIOTatler 
               extends SeqIOAdapter
{
  List featureStack;
  int level = 0;

  {
    featureStack = new ArrayList();
  }

  public void addFeatureProperty(Object key, Object value) {
    System.out.println("addFeatureProperty: " + key + "|" + value);
  }

  public void addSequenceProperty(Object key, Object value) {
    System.out.println("addFeatureProperty: " + key + "|" + value);
  }

  public void endFeature() {

    // pop top of stack and dump it
    Feature.Template feature = (Feature.Template)featureStack.get(--level);
    featureStack.remove(level);

    System.out.println("endFeature: " + feature);
    System.out.println("=========== ");

    // dump its contents
    System.out.println("location: " + feature.location);
    System.out.println("source:   " + feature.source);
    System.out.println("type:     " + feature.type);
    
    // handle subclass specific stuff
    if (feature instanceof StrandedFeature.Template) {
//      System.out.println("SeqIOListener checking strand");
      System.out.println("strand:   " + ((StrandedFeature.Template)feature).strand.getToken());
    }
    
    // dump annotation
//    System.out.println("SeqIOTatler.endElement, check annotation bundle.");
    if (feature.annotation != Annotation.EMPTY_ANNOTATION) {
      // get the keys
//      System.out.println("SeqIOTatler.endElement dump annotation.");
      Set keys = feature.annotation.keys();
      Iterator ki = keys.iterator();
//      System.out.println("SeqIOTatler.endElement keys,ki: " + keys + " " + ki);

      while (ki.hasNext()) {
        Object key = ki.next();
        Object value = feature.annotation.getProperty(key);
        System.out.println("key: " + key + "  value: " + value);
      }
    }
//    System.out.println("Leaving SeqIOTatlerIO.endFeature");
  }

  public void endSequence() {
    System.out.println("endSequence: ");
  }

  public void setName(String name) {
    System.out.println("setName: " + name);
  }

  public void startFeature(Feature.Template feature) {
    System.out.println("startFeature: ");

    // add to stack
    featureStack.add(feature);
    level++;
  }

  public void startSequence() {
    System.out.println("startFeature: ");
  }
}
