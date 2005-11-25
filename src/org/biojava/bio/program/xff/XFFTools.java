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

package org.biojava.bio.program.xff;



import java.io.*;

import javax.xml.parsers.*;

import org.xml.sax.*;



import org.biojava.utils.xml.*;

import org.biojava.utils.stax.*;

import org.biojava.bio.*;

import org.biojava.bio.symbol.*;

import org.biojava.bio.seq.*;

import org.biojava.bio.seq.io.*;

import org.biojava.bio.seq.impl.*;



/**

 * Common functionality for manipulating XFF.

 *

 * @author Matthew Pocock

 */

public class XFFTools {
	
	public static final String XFF_NS = "http://www.bioxml.org/2000/xff";
	
	public static final String XFF_BIOJAVA_NS = "http://www.biojava.org/2001/xff-biojava";

    public static void annotateXFF(File xffFile, final Sequence sequence)

    throws IOException, SAXException, BioException {

        annotateXFF(xffFile, sequence, Annotation.EMPTY_ANNOTATION);

    }



    public static void annotateXFF(File xffFile, final Sequence sequence, Annotation ann)

    throws IOException, SAXException, BioException {

        SequenceBuilder sb = new SequenceBuilderBase() {

            { seq = sequence; }

            public void addSymbols(Alphabet alpha, Symbol[] syms, int start, int length) {}

        };



        XFFFeatureSetHandler xffHandler = new XFFFeatureSetHandler();

        xffHandler.setFeatureListener(sb);

        xffHandler.setMergeAnnotation(ann);



        ContentHandler saxHandler = new SAX2StAXAdaptor(xffHandler);

        XMLReader parser;

        try {

            SAXParserFactory spf = SAXParserFactory.newInstance();

            spf.setValidating(false);

            spf.setNamespaceAware(true);

            parser = spf.newSAXParser().getXMLReader();

        } catch (Exception ex) {

            throw new BioException("Error creating SAX parser",ex);

        }

        parser.setContentHandler(saxHandler);

        InputSource is = new InputSource(new FileReader(xffFile));

        parser.parse(is);



        sb.makeSequence();

    }



    public static Sequence readXFF(File xffFile, String seqID, FiniteAlphabet alpha)

    throws IOException, SAXException, BioException {

        SymbolList dummy = new DummySymbolList(alpha, Integer.MAX_VALUE);

        Sequence ourSeq = new SimpleSequence(dummy, seqID, seqID, new SmallAnnotation());

        annotateXFF(xffFile, ourSeq);

        return ourSeq;

    }


    public static Sequence readXFF(File xffFile, String seqID)

    throws IOException, SAXException, BioException {

        SymbolList dummy = new DummySymbolList(Alphabet.EMPTY_ALPHABET, Integer.MAX_VALUE);

        Sequence ourSeq = new SimpleSequence(dummy, seqID, seqID, new SmallAnnotation());

        annotateXFF(xffFile, ourSeq);

        return ourSeq;

    }
        

    public static void writeXFF(File xffFile, FeatureHolder features)

    throws IOException {

        PrintWriter xffPR = new PrintWriter(new FileWriter(xffFile));

        writeXFF(xffPR, features);

    }



    public static void writeXFF(PrintWriter xffPR, FeatureHolder features)

    throws IOException {

        XMLWriter xmlWriter = new PrettyXMLWriter(xffPR);

        XFFWriter xffWriter = new XFFWriter(new PropertyWriter());

        xffWriter.writeFeatureSet(features, xmlWriter);

        xffPR.flush();

        xffPR.close();

    }

}

