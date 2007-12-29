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
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.Symbol;
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
  String offset = "";

  {
    featureStack = new ArrayList();
  }

  public void addFeatureProperty(Object key, Object value) {
    System.out.println(offset + "addFeatureProperty: " + key + "|" + value);
  }

  public void addSequenceProperty(Object key, Object value) {
    System.out.println(offset + "addFeatureProperty: " + key + "|" + value);
  }

  public void endFeature() {

    // pop top of stack and dump it
    Feature.Template feature = (Feature.Template)featureStack.get(--level);
    featureStack.remove(level);

    System.out.println(offset + "endFeature: " + feature);
    System.out.println(offset + "=========== ");

    // dump its contents
    System.out.println(offset + "location: " + feature.location);
    System.out.println(offset + "source:   " + feature.source);
    System.out.println(offset + "type:     " + feature.type);

    // handle subclass specific stuff
    if (feature instanceof StrandedFeature.Template) {
//      System.out.println("SeqIOListener checking strand");
      System.out.println(offset + "strand:   " + ((StrandedFeature.Template)feature).strand.getToken());
    }

    // dump annotation
//    System.out.println("SeqIOTatler.endElement, check annotation bundle.");
    if (feature.annotation != Annotation.EMPTY_ANNOTATION) {
      // get the keys
//      System.out.println("SeqIOTatler.endElement dump annotation.");
      Set keys = feature.annotation.keys();
      Iterator ki = keys.iterator();
//      System.out.println(offset + "SeqIOTatler.endElement keys,ki: " + keys + " " + ki);

      while (ki.hasNext()) {
        Object key = ki.next();
        Object value = feature.annotation.getProperty(key);
        System.out.println(offset + "key: " + key + "  value: " + value);
      }
    }
//    System.out.println("Leaving SeqIOTatlerIO.endFeature");
      offset = offset.substring(2, offset.length());
  }

  public void endSequence() {
    System.out.println(offset + "endSequence: ");
  }

  public void setName(String name) {
    System.out.println(offset + "setName: " + name);
  }

  public void startFeature(Feature.Template feature) {
    offset = offset.concat("  ");
    System.out.println(offset + "startFeature: ");

    // add to stack
    featureStack.add(feature);
    level++;
  }

  public void startSequence() {
    System.out.println(offset + "startFeature: ");
  }

  public void addSymbols(Alphabet alpha, Symbol [] syms, int start, int length)
  {
    // dump start and length only
//    System.out.println("addSymbols: start, length: " + start + " " + length);
  }
}
