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
 * StAX handler for GAME &lt;description&gt; elements.
 * derived from Thomas Down's PropDetailHandler
 *
 * @author David Huen
 * @author Thomas Down
 * @since 1.2
 */
public class GAMEDescriptionPropHandler extends StringElementHandlerBase {
  // this just sets up a proprty named "description" in the annotation bundle.
  public static final StAXHandlerFactory GAME_DESCRIPTION_PROP_HANDLER_FACTORY = new StAXHandlerFactory() {
	  public StAXContentHandler getHandler(StAXFeatureHandler staxenv) {
                 return new GAMEDescriptionPropHandler(staxenv);
	  }
            } ;

  private StAXFeatureHandler staxenv;

  public GAMEDescriptionPropHandler(StAXFeatureHandler staxenv) {
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
  }

  protected void setStringValue(String s)
        throws SAXException
  {
//      System.out.println("GAMEDescriptionPropHandler: string is " + s); 
      String trimmed = s.trim();

      try {
        staxenv.featureTemplate.annotation.setProperty("description", trimmed);
      }
      catch (ChangeVetoException cve) {
        System.err.println("GAMEDescriptionPropHandler: veto exception caught.");
      }
  }
  
}
