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
package org.biojava.bio.seq.io.agave;
import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.genomic.Gene;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.das.*;
import org.biojava.utils.*;
import org.xml.sax.*;

/**
 *
 * @author Hanning Ni    Doubletwist Inc
 */
public class AGAVEResultGroupHandler
               extends StAXFeatureHandler

{
  public static final StAXHandlerFactory AGAVE_RESULT_GROUP_HANDLER_FACTORY
    = new StAXHandlerFactory() {
    public StAXContentHandler getHandler(StAXFeatureHandler staxenv) {
      return new AGAVEResultGroupHandler(staxenv);
    }
  };


  AGAVEResultGroupHandler(StAXFeatureHandler staxenv) {
    // setup up environment stuff
    super( staxenv );
    featureListener = staxenv.featureListener;
    setHandlerCharacteristics("result_group", true);
    super.addHandler(new ElementRecognizer.ByLocalName("comp_result"),
      AGAVECompResultHandler.AGAVE_COMP_RESULT_HANDLER_FACTORY);

 }
  /**
  protected Feature.Template createTemplate() {
    // create Gene Template for this
    Feature.Template st = new Feature.Template();

    // assume feature set to describe a transcript
    st.type = "result_group";
    // set up annotation bundle
    st.annotation = new SmallAnnotation();
    st.location = new  Location.EmptyLocation();
    if( staxenv != null )
        staxenv. subFeatures .add( this ) ;

    return st;
  }**/

  public void startElementHandler(
                String nsURI,
                String localName,
                String qName,
                Attributes attrs)
                throws SAXException
  {
      try{
        featureListener.startFeature( featureTemplate );
        boolean forFeature = true ;
        setProperty( "group_order",  attrs.getValue("group_order"), forFeature ) ;
      }catch(Exception e){
         throw new SAXException( e.getMessage() ) ;
      }
  }


}

