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

package org.biojava.bio.program.game;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.xff.*;

import org.biojava.utils.*;
import org.biojava.utils.stax.*;
import org.xml.sax.*;

/**
 * StAX handler for the GAME &lt;name&gt; element.
 * derived from Thomas Down's PropDetailHandler
 *
 * @author David Huen
 * @author Thomas Down
 * @since 1.2
 */

public class GAMEStartEndPropHandler extends IntElementHandlerBase {
    public static final StAXHandlerFactory GAME_STARTEND_PROP_HANDLER_FACTORY = new StAXHandlerFactory() {
	    public StAXContentHandler getHandler(StAXFeatureHandler staxenv) {
		return new GAMEStartEndPropHandler(staxenv);
	    }
	} ;

  private StAXFeatureHandler staxenv;
  String currLocalName = "";

  // this class is not derived from StAXPropertyHandler and doesn't inherit
  // any of the handlerStack maintenance code.  However it does offer
  // context either so it can be omitted from the stack.
  public GAMEStartEndPropHandler(StAXFeatureHandler staxenv) {
    super();
    this.staxenv = staxenv;
  }

  public void startElement(String nsURI,
                                    String localName,
                                    String qName,
                                    Attributes attrs,
                                    DelegationManager dm)
	 throws SAXException
  {
     super.startElement(nsURI, localName, qName, attrs, dm);

     currLocalName = localName;
  }

  protected void setIntValue(int value)
        throws SAXException
  {
    // because I am a leaf element I can safely use the current stack level.
    int currLevel = staxenv.getLevel();

    if (currLevel >=1) {
      // search down stack for callback handler
      ListIterator li = staxenv.getHandlerStackIterator(currLevel);
      while (li.hasPrevious()) {
        Object ob = li.previous();
        if (ob instanceof GAMEStartEndCallbackItf) {
          // we have a nesting handler, use it
          if (currLocalName.equals("start"))
            ((GAMEStartEndCallbackItf) ob).setStartValue(value);
          else if (currLocalName.equals("end"))
            ((GAMEStartEndCallbackItf) ob).setEndValue(value);
          else
            // we don't permit other elements here
            throw new SAXException("illegal element in span.");
          return;
        }
      }
    }
  }
}
