/*
 *          BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *    http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *    http://www.biojava.org/
 *
 */

package org.biojava.bio.seq.io;

import java.util.*;
import java.io.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.*;

/**
 * Simple filter which handles attribute lines from an Swissprot entry.
 * Skeleton implementation, please add more functionality.
 *
 * <p>
 * <strong>FIXME:</strong> Note that this is currently rather incomplete, 
 * and doesn't handle any of the header information sensibly except for
 * ID and AC.
 * </p>
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.1
 */

public class SwissprotProcessor extends SequenceBuilderFilter {
  public static final String PROPERTY_SWISSPROT_ACCESSIONS = "swissprot.accessions";
  public static final String PROPERTY_SWISSPROT_COMMENT = "swissprot.comment";
  /**
   * Factory which wraps SequenceBuilders in a SwissprotProcessor
   *
   * @author Thomas Down
   */

  public static class Factory implements SequenceBuilderFactory, Serializable {
    private SequenceBuilderFactory delegateFactory;
    
    public Factory(SequenceBuilderFactory delegateFactory) {
      this.delegateFactory = delegateFactory;
    }
    
    public SequenceBuilder makeSequenceBuilder() {
      return new SwissprotProcessor(delegateFactory.makeSequenceBuilder());
    }
  }

  private List accessions;
  private Feature.Template tplt;
  private boolean inFeature;
  private String featureKey;
  private StringBuffer featureValue;

  public SwissprotProcessor(SequenceBuilder delegate) {
    super(delegate);

    accessions = new ArrayList();
    tplt = new Feature.Template();
    tplt.annotation = Annotation.EMPTY_ANNOTATION;
    tplt.source="SWISSPROT";
    inFeature = false;
    featureValue = new StringBuffer();
  }

  public void endSequence() throws ParseException {
    if (accessions.size() > 0) {
      String id = (String) accessions.get(0);
      getDelegate().setName(id);
      getDelegate().setURI("urn:sequence/swissprot:" + id);
      getDelegate().addSequenceProperty(PROPERTY_SWISSPROT_ACCESSIONS, accessions);
      accessions = new ArrayList();
    }
    if(inFeature) {
      getDelegate().endFeature();
    }
    getDelegate().endSequence();
  }

  public void addSequenceProperty(Object key, Object value) throws ParseException {
    if (key.equals("ID")) {
      String line = (String) value;
      int space = line.indexOf(' ');
      String id = line.substring(0, space);
    } else if (key.equals("AC")) {
      String acc= value.toString();
      StringTokenizer toke = new StringTokenizer(acc, "; ");
      while (toke.hasMoreTokens()) {
        accessions.add(toke.nextToken());
      }
    } else if (key.equals("FT")) {
      try {
        String line = (String) value;
        String type = line.substring(0, 8).trim();
        if(type.length() != 0) {
          if(inFeature) {
            flushFeature();
            getDelegate().endFeature();
          }
          tplt.type = type;
          // flush any feature info for the previous feature
          featureKey = PROPERTY_SWISSPROT_COMMENT;
          
          // process this feature into a type and a location
          String locStart = line.substring(8, 15).trim();
          String locEnd = line.substring(15, 22).trim();
          
          int start;
          int end;
          try {
            start = Integer.parseInt(locStart);
          } catch (NumberFormatException nfe) {
            start = Integer.MIN_VALUE;
          }
          
          try {
            end = Integer.parseInt(locEnd);
          } catch (NumberFormatException nfe) {
            end = Integer.MAX_VALUE;
          }

          tplt.location = new RangeLocation(start, end);
          
          getDelegate().startFeature(tplt);
        }
        if(line.length() > 28) {
          String rest = line.substring(28).trim();
          if(rest.startsWith("/")) {
            flushFeature();
            int eq = rest.indexOf("=");
            featureKey = rest.substring(1, eq);
            featureValue.append(rest.substring(eq+1));
          } else {
            featureValue.append(rest);
          }
        }
      } catch (Exception e) {
        // fixme: should be throwing some parse exception
        throw new BioError(e, "Can't parse FT line:\n" + value.toString());
      }
    } else {
      getDelegate().addSequenceProperty(key, value);
    }
  }
  
  protected void flushFeature() throws ParseException {
    int l = featureValue.length();
    if(l >= 0) {
      if(featureValue.charAt(l-1) == '.') {
        featureValue.setLength(l-1);
      }
      getDelegate().addFeatureProperty(featureKey, featureValue.toString());
    }
  }
}
