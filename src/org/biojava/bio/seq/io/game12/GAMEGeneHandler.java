/**
 *  BioJava development code This code may be freely distributed and modified
 *  under the terms of the GNU Lesser General Public Licence. This should be
 *  distributed with the code. If you do not have a copy, see:
 *  http://www.gnu.org/copyleft/lesser.html Copyright for this code is held
 *  jointly by the individual authors. These should be listed in
 *
 *@author    doc comments. For more information on the BioJava project and its
 *      aims, or to join the biojava-l mailing list, visit the home page at:
 *      http://www.biojava.org/
 */

package org.biojava.bio.seq.io.game12;

import org.biojava.bio.seq.io.ParseException;
import org.biojava.bio.seq.io.game.ElementRecognizer;
import org.biojava.utils.stax.StAXContentHandler;
import org.biojava.utils.stax.StringElementHandlerBase;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *  Handles the GAME &lt;annotation&gt; element
 *
 * @author     David Huen
 * @since      1.2
 */
public class GAMEGeneHandler
         extends StAXFeatureHandler {
    // <annotation> is a container for all features of a "gene".
    // the only important property of this container is its id
    // which I need to capture and supply nested classes.

    // database columns
    private String release;
    private String name;
    private String association;
    private String synonym;

    // set up factory method
    /**
     *  Description of the Field
     */
    public final static StAXHandlerFactory GAME_GENE_HANDLER_FACTORY
             =
        new StAXHandlerFactory() {
            public StAXContentHandler getHandler(StAXFeatureHandler staxenv) {
                return new GAMEGeneHandler(staxenv);
            }
        };

    // subclass <dbxref> handler to pick modify the form of the entry
    // subclass <dbxref> to write a feature property here
    private class DbxrefHandler extends GAMEDbxrefHandler
    {
        private DbxrefHandler(StAXFeatureHandler staxenv)
        {
            super(staxenv);
        }

        public void endElementHandler(
                String nsURI,
                String localName,
                String qName,
                StAXContentHandler contentHandler)
            throws SAXException
        {
            // validate before going further
            if ((db_xref_db == null) || (db_xref_id == null)) {
                return;
            }

            try {
                listener.addFeatureProperty(db_xref_db, db_xref_id);
            }
            catch (ParseException pe) {
                pe.printStackTrace();
                throw new SAXException("unexpected exception while add <dbxref> as a feature property.");
            }
        }
    }

    /**
     *  Constructor for the GAMEGeneHandler object
     *
     *@param  staxenv   Description of the Parameter
     *@param  parentID  Description of the Parameter
     */
    GAMEGeneHandler(StAXFeatureHandler staxenv) {
        // setup environment
        super(staxenv);

        // setup handlers
        // <name>
        super.addHandler(new ElementRecognizer.ByLocalName("name"),
            new StAXHandlerFactory() {
                public StAXContentHandler getHandler(StAXFeatureHandler staxenv) {
                    return new NameHandler();
                }
            }
                );
        // <synonym>
//        super.addHandler(new ElementRecognizer.ByLocalName("synonym"),
//            new StAXHandlerFactory() {
//                public StAXContentHandler getHandler(StAXFeatureHandler staxenv, long parentID) {
//                    return new SynonymHandler();
//                }
//            }
//                );
        // <dbxref>
        super.addHandler(new ElementRecognizer.ByLocalName("dbxref"),
                GAMEDbxrefHandler.GAME_DBXREF_HANDLER_FACTORY);
    }


    /**
     *  Description of the Class
     *
     *@author     david
     */
    private class NameHandler extends StringElementHandlerBase {
        /**
         *  Sets the stringValue attribute of the NameHandler object
         *
         *@param  s  The new stringValue value
         */
        protected void setStringValue(String s) {
            name = s.trim();
        }
    }

//    private class SynonymHandler extends StringElementHandlerBase {
//        /**
//         *  Sets the stringValue attribute of the SynonymHandler object
//         *
//         *@param  s  The new stringValue value
//         */
//        protected void setStringValue(String s) {
//            synonym = s.trim();
//        }
//    }

    public void startElementHandler(
            String nsURI,
            String localName,
            String qName,
            Attributes attrs) {
        association = attrs.getValue("association");
    }

    public void endElementHandler(
            String nsURI,
            String localName,
            String qName,
            StAXContentHandler contentHandler) {
    }

}

