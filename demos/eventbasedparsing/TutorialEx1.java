package eventbasedparsing;


import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import java.util.*;
import java.io.*;

import org.biojava.bio.program.sax.BlastLikeSAXParser;

/**
 * 
 */
public class TutorialEx1 {

    /**
     * Takes an XML doc as input and parses it
		 *
     * @param args a <code>String[]</code> representation of a pathname
     * @exception Exception if an error occurs
     */
    public static void main(String[] args) throws Exception {

	String oInput = null;
	String oMode  = null;

	boolean tStrict = true;

        // Catch wrong number of arguments or help requests

        if ( (args.length != 1)        ||
	     (args[0].equals("-help")) || 
	     (args[0].equals("-h")) ) {

	    System.out.println();
	    System.out.println("Usage: java TutorialEx1 <pathname>");

	    System.out.println();

            System.exit(1);
        }

	if (args.length == 1) {
	    oInput = args[0];
	}

        //Now the actual application...
	//Create the object(s) we want to populate

	ArrayList oDatabaseIdList = new ArrayList();

	/**
	 * Create a SAX Parser
	 */
	XMLReader oParser = (XMLReader) new BlastLikeSAXParser();


	/**
	 * Create an XML ContentHandler. This
	 * implementation of the DocumentHandler
	 * interface simple outputs nicely formatted
	 * XML. Passing a true value to the SimpleXMLEmitter
	 * constructor instructs the ContentHandler
	 * to take QNames from the SAXParser, rather
	 * than LocalNames.
	 */
	

	ContentHandler oHandler  = 
	    (ContentHandler) new TutorialEx1Handler(oDatabaseIdList);

	/*
	 * Give the parser a reference to the ContentHandler
	 * so that it can send SAX2 mesagges.
	 */
	oParser.setContentHandler(oHandler);


	//Parsing using ByteSteam as InputSource
        // Open file and read all lines from file sequentially
	FileInputStream           oInputFileStream;
	BufferedReader            oContents;

        try {
             // create input stream
             oInputFileStream = new FileInputStream(oInput);

	     oParser.parse(new InputSource(oInputFileStream));
         } catch (java.io.FileNotFoundException x) {
             System.out.println(x.getMessage());
             System.out.println("Couldn't open file");
             System.exit(0);
         }

	//At this point, the output is parsed, and objects populated
	System.out.println("Results of parsing");
	System.out.println("==================");
	for (int i = 0; i < oDatabaseIdList.size();i++) {
	    System.out.println(oDatabaseIdList.get(i));
	}
    }
}
