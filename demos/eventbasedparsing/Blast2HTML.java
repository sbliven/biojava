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
package eventbasedparsing;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import java.util.*;
import java.io.*;

import org.biojava.bio.program.sax.BlastLikeSAXParser;
import org.biojava.bio.program.blast2html.*;

/**
 * Example application for converting a Blast - like SAX stream
 * to HTML.
 * 
 * Primary author 
 *                 Colin Hardman      (CAT)
 * Other authors  -
 *                 Tim Dilks          (CAT)
 *                 Simon Brocklehurst (CAT)
 *                 Stuart Johnston    (CAT)
 *                 Lawerence Bower    (CAT)
 *                 Derek Crockford    (CAT)
 *                 Neil Benn          (CAT)
 *
 * Copyright 2001 Cambridge Antibody Technology Group plc.
 * All Rights Reserved.
 *
 * This code released to the biojava project, May 2001
 * under the LGPL license.
 *
 * @author Cambridge Antibody Technology Group plc
 * @version 1.0
 */
public class Blast2HTML {

    private static String oStyleDefinition =
	".footer { font-family: Arial, Helvetica, sans-serif; font-size: 12px; font-style: italic; font-weight: normal ; color: #CC0000}\n.alignment {  font-family: \"Courier New\", Courier, mono; font-size: 14px;}\n.dbRetrieve {  font-family: Arial, Helvetica, sans-serif; font-size: 14px; font-style: normal; font-weight: bold; color: #000000; line-height: normal}\n.titleLevel1 { font-family: Arial, Helvetica, sans-serif; font-size: 30px; font-style: normal; font-weight: bold; color: #000000 ; background-color: #CCCCFF; line-height: normal}\n.titleLevel1Sub { font-family: Arial, Helvetica, sans-serif; font-size: 18px; font-style: normal; font-weight: bold; color: #000000 ; background-color: #CCCCFF; line-height: normal}\n.titleLevel2 { font-family: Arial, Helvetica, sans-serif; font-size: 18px; font-style: normal; font-weight: bold; color: #000000 ; background-color: #99FFCC; line-height: normal}\n.titleLevel3 { font-family: Arial, Helvetica, sans-serif; font-size: 16px; font-style: normal; font-weight: bold; color: #000000 ; background-color: #FFFFCC; line-height: normal; }\n.titleLevel3Sub { font-family: Arial, Helvetica, sans-serif; font-size: 12px; font-style: normal; font-weight: bold; color: #000000 ; background-color: #FFFFCC; line-height: normal}\n.summaryBodyLineOdd { font-family: Arial, Helvetica, sans-serif; font-size: 12px; font-style: normal; font-weight: normal; color: #000000 ; clip: rect( ); line-height: normal; text-indent: 0pt; padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 0px; margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px }\n.summaryBodyLineEven { font-family: Arial, Helvetica, sans-serif; font-size: 12px; font-style: normal; font-weight: normal; color: #000000 ; background-color: #EEEEEE; line-height: normal }\n.titleLevel4 { font-family: Arial, Helvetica, sans-serif; font-size: 14px; font-style: normal; font-weight: bold; color: #000000 ; background-color: #FFFFEE; line-height: normal }\nbody {  font-family: Arial, Helvetica, sans-serif; font-size: 10px}\ntd {  font-family: Arial, Helvetica, sans-serif; font-size: 16px}\na {  text-decoration: none}\n";


    public static HTMLRenderer configureRenderer( String poType,
						  PrintWriter poOut ) {

	if ( poType.equals( "nucleic" ) ) {
	    return Blast2HTML.configureBlastN( poOut );
	} else {
	    return Blast2HTML.configureBlastP( poOut );
	}
    }

    public static HTMLRenderer configureBlastN( PrintWriter poOut ) {

	SimpleAlignmentStyler oStyler 
	    = new SimpleAlignmentStyler( SimpleAlignmentStyler.SHOW_ALL );
	String oRed = "FFA2A2";
	oStyler.addStyle( "-", oRed );
	oStyler.addStyle( "N", oRed );
	oStyler.addStyle( "A", oRed );
	oStyler.addStyle( "T", oRed );
	oStyler.addStyle( "C", oRed );
	oStyler.addStyle( "G", oRed );

	AlignmentMarker oAlignmentMarker = new AlignmentMarker
	    ( new ColourCommand() {
		    /**
		     * Highlight mismatches
		     * NOTE: assuming the chars are the same case.
		     */
		    public  boolean  isColoured
			(  String poFirst, String poSecond ) {

		       if ( poFirst.equals( poSecond ) ) {
			   return false;
		       } else {
			   return true;
		       }
		   }
		} // end ColourCommand
	      , oStyler
	      //	      new BlastMatrixAlignmentStyler() 
	      );

	Properties oProps = new Properties();
	oProps.put( "db", "nucl" );

	HTMLRenderer oRenderer = new HTMLRenderer
	    ( poOut,
	      Blast2HTML.oStyleDefinition,
	      50,
	      new DefaultURLGeneratorFactory(),
	      oAlignmentMarker,
	      oProps );

	return oRenderer;
    }


    public static HTMLRenderer configureBlastP( PrintWriter poOut ) {

	SimpleAlignmentStyler oStyler 
	    = new SimpleAlignmentStyler( SimpleAlignmentStyler.SHOW_SAME );

	oStyler.addStyle( "A", "C8FFC8" );
	oStyler.addStyle( "C", "C8FFC8" );
	oStyler.addStyle( "L", "C8FFC8" );
	oStyler.addStyle( "I", "C8FFC8" );
	oStyler.addStyle( "V", "C8FFC8" );
	oStyler.addStyle( "M", "C8FFC8" );
	oStyler.addStyle( "G", "DCC0FF" );
	oStyler.addStyle( "P", "DCC0FF" );
	oStyler.addStyle( "S", "FFFCA0" );
	oStyler.addStyle( "T", "FFFCA0" );
	oStyler.addStyle( "N", "FFFCA0" );
	oStyler.addStyle( "Q", "FFFCA0" );
	oStyler.addStyle( "K", "FFA2A2" );
	oStyler.addStyle( "R", "FFA2A2" );
	oStyler.addStyle( "D", "A2E2FF" );
	oStyler.addStyle( "E", "A2E2FF" );
	oStyler.addStyle( "H", "50FF50" );
	oStyler.addStyle( "Y", "50FF50" );
	oStyler.addStyle( "W", "50FF50" );
	oStyler.addStyle( "F", "50FF50" );

	AlignmentMarker oAlignmentMarker = new AlignmentMarker
	    ( new ColourCommand() {
		    /**
		     * Highlight mismatches
		     * NOTE: assuming the chars are the same case.
		     */
		    public  boolean  isColoured
			(  String poFirst, String poSecond ) {

		       if ( poFirst.equals( poSecond ) ) {
			   return false;
		       } else {
			   return true;
		       }
		   }
		} // end ColourCommand
	      , oStyler
	      //	      new BlastMatrixAlignmentStyler() 
	      );

	Properties oProps = new Properties();
	oProps.put( "db", "Protein" );

	HTMLRenderer oRenderer = new HTMLRenderer
	    ( poOut,
	      Blast2HTML.oStyleDefinition,
	      60,
	      new DefaultURLGeneratorFactory(),
	      oAlignmentMarker,
	      oProps );

	return oRenderer;
    }


    /**
     * 
     *
     */
    public static void main(String[] args) throws Exception {

	String oInput = null;
	String oMode  = null;

	boolean tStrict = true;

        // Catch wrong number of arguments or help requests

        if ( (args.length < 2)        ||
	     (args[0].equals("-help")) || 
	     (args[0].equals("-h")) ) {

	    System.err.println();
	    System.err.println
		("Usage: java eventbasedparsing.Blast2HTML <nucleic/protein> <pathname> <Output filename>\n");

	    System.err.println
		("For Example - " );
	    System.err.println
		( "java eventbasedparsing.Blast2HTML protein files/ncbiblast/blastp.out");

	    System.err.println();

            System.exit(1);
        }

	PrintWriter oOut = null;

	if ( args.length == 3 ) {
	    
	    File oFile = new File( args[2] );

	    if ( oFile.exists() ) {
		System.err.println( args[2] + " file already exists" );
		System.exit( 1 );
	    }
	    oOut = new PrintWriter( new FileWriter( oFile ) );

	} else { 
	    oOut = new PrintWriter( System.out );
	}


	HTMLRenderer oRenderer = null;

	if ( args[0].equals( "protein" )  || args[0].equals( "nucleic" ) ) {
	    
	    oRenderer = Blast2HTML.configureRenderer( args[0],
						      oOut );
	} else {
	    System.err.println( "Only \"protein\" or \"nucleic\" allowed" );
	    System.exit( 1 );
	}

	oInput = args[1];

	/**
	 * Create a SAX Parser
	 */
	XMLReader oParser = (XMLReader) new BlastLikeSAXParser();
	((BlastLikeSAXParser)oParser).setModeLazy();

	ContentHandler oHandler  = 
	    (ContentHandler) new Blast2HTMLHandler
	    ( oRenderer );

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

	     oOut.println( "<HTML>\n<HEAD>" );
	     oOut.println( oRenderer.getHeaderDefinitions() );
	     oOut.println( "</HEAD>" );
	     oOut.println
     ( "<body bgcolor=\"#FFFFFF\" alink=\"#33FFFF\" vlink=\"#CCCC99\">" );
	     oParser.parse(new InputSource(oInputFileStream));

	     oOut.println( "</BODY>\n</HTML>" );

         } catch (java.io.FileNotFoundException x) {
             System.out.println(x.getMessage());
             System.out.println("Couldn't open file");
             System.exit(0);
         } finally {
	     oOut.flush();
	     oOut.close();
	 }
    }
}
