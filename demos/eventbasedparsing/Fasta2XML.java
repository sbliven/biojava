
package eventbasedparsing;

import java.util.*;
import java.io.*;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import org.biojava.bio.program.sax.FastaSearchSAXParser;
import org.biojava.bio.program.xml.SimpleXMLEmitter;

/**
 * <code>Fasta2XML</code> preliminary demo program.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class Fasta2XML
{
    public static void main(String [] argv)
    {
	XMLReader parser = (XMLReader) new FastaSearchSAXParser();

	ContentHandler handler  = 
	    (ContentHandler) new SimpleXMLEmitter(true);

	try
	{
	    FileInputStream inputFileStream = new FileInputStream(argv[0]);
  	    BufferedReader contents =
  		new BufferedReader(new InputStreamReader(inputFileStream));

	    parser.setContentHandler(handler);
	    parser.parse(new InputSource(contents));
	}
        catch (FileNotFoundException fnfe)
	{
	    System.out.println(fnfe.getMessage());
	    System.out.println("Couldn't open file");
	    System.exit(0);
	}
	catch (IOException ioe)
	{
	    ioe.printStackTrace();
	}
	catch (SAXException se)
	{
	    System.out.println(se.getMessage());
	    se.printStackTrace();
	}
    }
}
