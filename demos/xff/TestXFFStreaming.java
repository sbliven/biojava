package xff;

import java.io.*;
import org.xml.sax.*;
import org.biojava.utils.stax.*;
import org.apache.xerces.parsers.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.xff.*;

public class TestXFFStreaming {
    public static void main(String[] args) 
        throws Exception
    {
	SeqIOListener siol = new TestSIOL();
	final XFFFeatureSetHandler xffhandler = new XFFFeatureSetHandler();
	xffhandler.setFeatureListener(siol);

	SAXParser parser = new SAXParser();
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
	    }));
	InputSource is = new InputSource(new FileReader(args[0]));
	parser.parse(is);
    }

    private static class TestSIOL extends SeqIOAdapter {
	int level;

	public void startFeature(Feature.Template templ) {
	    for (int i = 0; i < level; ++i) {
		System.out.print("    ");
	    }
	    System.out.print(templ.type + " at " + templ.location.toString());
	    if (templ instanceof StrandedFeature.Template) {
		StrandedFeature.Strand strand = ((StrandedFeature.Template) templ).strand;
		if (strand == StrandedFeature.POSITIVE) {
		    System.out.print(" (strand = +)");
		} else if (strand == StrandedFeature.NEGATIVE) {
		    System.out.print(" (strand = -)");
		}
	    }
	    System.out.println();
	    ++level;
	}

	public void endFeature() {
	    --level;
	}
    }
}
