
import java.io.*;
import org.xml.sax.*;
import org.biojava.utils.stax.*;
import org.apache.xerces.parsers.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.game.*;

public class TestGAME {
    public static void main(String[] args) 
        throws Exception
    {
	SeqIOListener siol = new SeqIOTatler();
	final GAMEHandler gamehandler = new GAMEHandler();
	gamehandler.setFeatureListener(siol);

	SAXParser parser = new SAXParser();
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

	parser.setContentHandler(new SAX2StAXAdaptor(gamehandler));

	InputSource is = new InputSource(new FileReader(args[0]));
	parser.parse(is);
    }
}
