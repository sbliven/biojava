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

package org.biojava.bio.program.game;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;

import org.biojava.utils.*;
import org.biojava.utils.stax.*;
import org.xml.sax.*;

/**
 * Handles the GAME &lt;span&gt; element
 * Currently, it just ignores it!
 *
 * @author David Huen
 * @since 1.8
 */
public class GAMESpanPropHandler 
               extends StAXPropertyHandler 
               implements GAMEStartEndCallbackItf {
  // the <span> element supplies limits of a sequence span.
  // unfortunately, the spans can be either numeric or
  // alphanumeric (with cytological map_position).
  // set up factory method
  public static final StAXHandlerFactory GAME_SPAN_PROP_HANDLER_FACTORY 
    = new StAXHandlerFactory() {
    public StAXContentHandler getHandler(StAXFeatureHandler staxenv) {
      return new GAMESpanPropHandler(staxenv);
    }
  };

  private int start = 0;
  private int stop = 0;
  private StAXFeatureHandler staxenv;

  GAMESpanPropHandler(StAXFeatureHandler staxenv) {
    // execute superclass method to setup environment
    super(staxenv);
    setHandlerCharacteristics("span", true);
   
    // cache environment: this is of PREVIOUS feature handler as
    // delegation is invoked in StaxFeatureHandler itself which means
    // that the this pointer that is passed is the Feature one.
    this.staxenv = staxenv;

    // setup handlers
    super.addHandler(new ElementRecognizer.ByLocalName("start"),
      GAMEStartEndPropHandler.GAME_STARTEND_PROP_HANDLER_FACTORY);
    super.addHandler(new ElementRecognizer.ByLocalName("end"),
      GAMEStartEndPropHandler.GAME_STARTEND_PROP_HANDLER_FACTORY);
  }

// handlers for <start> and <stop>
  public void setStartValue(int value) {
    start = value;
  }

  public void setEndValue(int value) {
    stop = value;
  }
/*
  public void startElementHandler(
                String nsURI,
                String localName,
                String qName,
                Attributes attrs)
	 throws SAXException
  {
    System.out.println("GAMESpanPropHandler.startElementHandler entered.");
  }
*/
  public void endElementHandler(
                String nsURI,
                String localName,
                String qName,
                StAXContentHandler handler)
  {
    // check that it IS a StrandedFeature.Template
    boolean isStrandedTemplate = staxenv.featureTemplate instanceof StrandedFeature.Template;

    Feature.Template templ = staxenv.featureTemplate;

    // go set strandedness and range
    if (start < stop) {
      templ.location = new RangeLocation(start, stop);
      if (isStrandedTemplate)
        ((StrandedFeature.Template) templ).strand = StrandedFeature.POSITIVE;
    }
    else if (start > stop) {
      staxenv.featureTemplate.location = new RangeLocation(stop, start);
      if (isStrandedTemplate)
        ((StrandedFeature.Template) templ).strand = StrandedFeature.NEGATIVE;
    }
    else {
      staxenv.featureTemplate.location = new PointLocation(start);
      if (isStrandedTemplate)
        ((StrandedFeature.Template) templ).strand = StrandedFeature.UNKNOWN;
    } 
  }
}

