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
 * Handles the GAME &lt;gene&gt; element
 *
 * @author David Huen
 * @since 1.8
 */
public class GAMEGenePropHandler 
               extends StAXPropertyHandler
               implements GAMENameCallbackItf {   
  // this element provides basic identification of the gene
  // as a homolog/paralog/maybe/is.
  String association;
  
  // set up factory method
  public static final StAXHandlerFactory GAME_GENE_PROP_HANDLER_FACTORY 
    = new StAXHandlerFactory() {
    public StAXContentHandler getHandler(StAXFeatureHandler staxenv) {
      return new GAMEGenePropHandler(staxenv);
    }
  };

  GAMEGenePropHandler(StAXFeatureHandler staxenv) {
    // execute superclass method to setup environment
    super(staxenv);
    setHandlerCharacteristics("gene", true);

    // setup handlers
    super.addHandler(new ElementRecognizer.ByLocalName("name"),
      GAMENamePropHandler.GAME_NAME_PROP_HANDLER_FACTORY);
  }

  public void NameSetStringValue(String s) {
//    System.out.println("GAMEGenePropHandler.NameSetStringValue: entering. ");
    if (association.equals("IS")) {
//       System.out.println("GAMEGenePropHandler.NameSetStringValue: assoc IS. ");
      // set gene name
      try {
//       System.out.println("GAMEGenePropHandler.NameSetStringValue: setting name. ");
       staxenv.featureTemplate.annotation.setProperty("id", s.trim());
      }
      catch (ChangeVetoException cve) {
        // baulk and discard exception
        System.err.println("GAMEGenPropHandler: change vetoed");
      }
    }
  }

  public void startElementHandler(
                String nsURI,
                String localName,
                String qName,
                Attributes attrs)
	 throws SAXException
  {
    // determine association type
    association = attrs.getValue("association");
//    System.out.println("GAMEGenePropHandler.startElementHandler: association is " + association);
  }
}

