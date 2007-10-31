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

package nativeapps;


import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import java.util.*;

import org.biojava.bio.program.sax.BlastLikeSAXParser;
import org.biojava.bio.program.xml.SimpleXMLEmitter;

import org.biojava.bio.program.BlastLikeToXMLConverter;

/**
 * Blast2XML is an application for the conversion native output from
 * BLAST-like bioinformatics software into an XML format.
 * <p>
 * For currently supported bioinformatics programs, please see the
 * documentation for the BlastLikeSaxParser.
 * This class simply wraps the functionality of BlastLikeToXMLConverter.
 * Please see the documentation of this  class for a documented example
 * of how to use the parsing framework.
 * <p>
 * The XML produced should validate against the biojava
 * BlastLikeDataSetCollection DTD.
 * <p>
 * Usage is:<pre><font color="#0000FF">
 * java Blast2XML &lt;blastlike pathname&gt; [-mode &lt;strict|lazy&gt;]
 * </font></pre>
 * -mode is optional paramater. Choosing strict mode will mean the
 * application throws exceptions if the precise version of a given
 * piece of bioinformatics software is not recognised. Lazy mode will
 * attempt to parse the file regardless of version number.
 * <p>
 * For programs currently supported, please see the documentation
 * for the BlastLikeSAXParser.
 * <p>
 * Copyright &copy; 2000 Cambridge Antibody Technology.
 * 
 * <p>
 * Primary author -<ul>
 * <li>Simon Brocklehurst (CAT)
 * </ul>
 * Other authors  -<ul>
 * <li>Tim Dilks          (CAT)
 * <li>Colin Hardman      (CAT)
 * <li>Stuart Johnston    (CAT)
 *</ul>
 *
 * @deprecated Prior to the biojava1.1 release - use equivalent class in
 *  the demo package eventbasedparsing. Classes in this package were
 *  deprecated because nativeapps is an unhelpful name.
 *
 * @author Cambridge Antibody Technology (CAT)
 * @version 1.0
 * 
 * @see BlastLikeToXMLConverter
 * @see BlastLikeSAXParser
 */
public class BlastLike2XML {

    /**
     * Converts an file containing the output from bioinformatics software
     * with a "blast-like" format into an XML format.
     *
     * @param args a <code>String[]</code> representation of a pathname
     * @exception Exception if an error occurs
     */
    public static void main(String[] args) throws Exception {

	String oInput = null;
	String oMode  = null;

	boolean tStrict = true;

        // Catch wrong number of arguments or help requests

        if ( ( (args.length != 1) &&
 	       (args.length != 3) )    ||
	     (args[0].equals("-help")) || 
	     (args[0].equals("-h")) ) {

	    System.out.println();
	    System.out.println(
           "Utility program to convert the output from  blast-like");
	    System.out.println(
           "software into an XML format that should validate correctly");
	    System.out.println(
           "against the biojava BlastLikeDataSectCollection DTD.");
	    System.out.println();

            System.out.println  ("Usage:  java Blast2XML " +
            "[-mode <strict|lazy>] <blast output file pathname>");

	    System.out.println();

            System.exit(1);
        }

	if (args.length == 1) {
	    oInput = args[0];
	}

	if (args.length == 3) {
	    oMode = args[1];
	    if (oMode.toLowerCase().equals("lazy")) {
		tStrict = false;
	    } else {
		tStrict = true;
	    }
	    oInput = args[2];
	}

        //Now the actual application...

	try {

	    BlastLikeToXMLConverter  oBlast2XML = 
		new BlastLikeToXMLConverter(oInput);

	    if (tStrict) {
		oBlast2XML.setModeStrict();
	    } else {
		oBlast2XML.setModeLazy();
	    }
	    oBlast2XML.convert();

	} catch (Exception e) {
	    System.out.println("Unrecoverable error. Stack trace follows...");
	    e.printStackTrace();
	}

    }

}
