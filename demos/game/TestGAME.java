

import java.io.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.*;
import org.biojava.utils.stax.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.io.game.*;

public class TestGAME {
    public static void main(String[] args)
        throws Exception
    {
        SeqIOListener siol = new SeqIOTatler();
        final GAMEHandler gamehandler = new GAMEHandler();
        gamehandler.setFeatureListener(siol);

        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
/*
        parser.setContentHandler(new SAX2StAXAdaptor(new StAXContentHandlerBase() {
                public void startElement(String nsURI,
                                         String localName,
                                         String qName,
                                         Attributes attrs,
                                         DelegationManager dm)
                    throws SAXException
                {
                    if (localName.equals("featureSet")) {
                        dm.delegate(xffhandler);
                    }
                }
            }));*/

        //parser.setContentHandler(new SAX2StAXAdaptor(gamehandler));

        InputSource is = new InputSource(new FileReader(args[0]));
        parser.parse(is, new SAX2StAXAdaptor(gamehandler));
    }
}
