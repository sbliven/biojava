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
import java.util.*;

import org.biojava.bio.program.sax.PdbSAXParser;
import org.biojava.bio.program.xml.SimpleXMLEmitter;

import org.biojava.bio.program.PdbToXMLConverter;

/**
 * Pdb2XML is an application for the conversion native output from
 * BLAST-like bioinformatics software into an XML format. For
 * currently supported bioinformatics programs, please see the
 * documentation for the BlastLikeSaxParser.
 * <p>
 * The XML produced should validate against the biojava
 * MacromolecularStructureCollection DTD.
 *
 * Usage is:<pre><font color="#0000FF">
 * java Pdb2XML &lt;pdb file pathname&gt;
 * </font></pre>
 * -mode is optional paramater.
 * <p>
 * Copyright &copy; 2000 Cambridge Antibody Technology.
 * All Rights Reserved.
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
 * @author Cambridge Antibody Technology Group plc (CAT)
 * @version 1.1
 * 
 */
public class Pdb2XML {

    /**
     * Converts an file containing the output from bioinformatics software
     * with a "pdb like" format into an XML format.
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
	    System.out.println(
           "Utility program to convert descriptions of macromolecular ");
	    System.out.println(
           "structures in formats resembline PDB format ");
	    System.out.println(
           "into an XML format that should validate correctly");
	    System.out.println(
           "against the biojava MacromolecularStructureCollection DTD.");
	    System.out.println();
            System.out.println  ("Usage:  java Pdb2XML " +
            "<pdb file pathname>");

	    System.out.println();

            System.exit(1);
        }

	if (args.length == 1) {
	    oInput = args[0];
	}

        //Now the actual application...

	try {

	    PdbToXMLConverter  oPdb2XML = 
		new PdbToXMLConverter(oInput);

	    oPdb2XML.convert();

	} catch (Exception e) {
	    System.out.println("Unrecoverable error. Stack trace follows...");
	    e.printStackTrace();
	}

    }

}
