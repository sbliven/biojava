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

package org.biojava.bio.seq.io.game;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;

import org.biojava.utils.*;
import org.biojava.utils.stax.*;
import org.xml.sax.*;

/**
 * StAX handler for GAME &lt;residues&gt; elements.
 * derived from Thomas Down's PropDetailHandler
 *
 * <p>
 * This takes the sequence supplied by &lt;residues&gt; elements
 * and feeds it to a StreamParser associated with a SeqIOLIstener
 * instance.
 *
 * @author David Huen
 * @author Thomas Down
 * @since 1.8
 */
public class GAMEResiduesPropHandler extends SequenceContentHandlerBase {
  public static final StAXHandlerFactory GAME_RESIDUES_PROP_HANDLER_FACTORY = new StAXHandlerFactory() {
	  public StAXContentHandler getHandler(StAXFeatureHandler staxenv) {
                 return new GAMEResiduesPropHandler(staxenv);
	  }
            } ;

  private StAXFeatureHandler staxenv;
  private TokenParser tokenParser;

  public GAMEResiduesPropHandler(StAXFeatureHandler staxenv) {
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

    // set up StreamParser
    tokenParser = new TokenParser(DNATools.getDNA());    
    super.setStreamParser(tokenParser.parseStream(staxenv.featureListener));
  }

}
