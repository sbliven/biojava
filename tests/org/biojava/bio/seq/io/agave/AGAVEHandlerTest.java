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

package org.biojava.bio.seq.io.agave;


import junit.framework.TestCase;



import java.io.*;

import java.util.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.SeqIOAdapter;
import org.biojava.bio.seq.io.SeqIOListener;
import org.biojava.bio.symbol.Location;
import org.biojava.utils.xml.ResourceEntityResolver;
import org.biojava.bio.seq.io.agave.SAX2StAXAdaptor;
import org.xml.sax.*;




/**

 *.

 * JUnit test case of AGAVE XML to BioJava input/output.

 *

 * @author Brian King

 */

public class AGAVEHandlerTest extends TestCase

{

    /**

     * Constructs a test case with the given name

     *

     */

    public AGAVEHandlerTest(String name)

    {

        super(name);

    }



    /**

     *  Test translation of AGAVE sequence and feature to BioJava model.

     */

    public void

    testBioSequence() throws Exception

    {

        // create initial data model

        //

        Reader r = new StringReader

        (

            "<?xml version=\"1.0\"?>\n" +

            "<!DOCTYPE sciobj SYSTEM \"agave.dtd\">\n" +

            "<sciobj version=\"2\" release=\"3\">\n" +

            "  <bio_sequence seq_length=\"20\">" +

            "    <db_id id=\"AC1234\" db_code=\"fake\"/>\n" +

            "    <sequence>aaaaaaaaaaaaaaaaaaaa</sequence>\n" +

            "    <sequence_map label=\"algorithm\">\n" +

            "      <annotations>\n" +

            "        <seq_feature element_id=\"ABC\" feature_type=\"exon\">\n" +

            "          <seq_location is_on_complement=\"false\" least_start=\"1\" greatest_end=\"20\"></seq_location>\n" +

            "        </seq_feature>\n" +

             "      </annotations>\n" +

            "    </sequence_map>\n" +

            "  </bio_sequence>\n" +

            "</sciobj>"

        );

        Sequence seq = parse(r);



        assertEquals("Sequence strings not equal",

                     "aaaaaaaaaaaaaaaaaaaa",

                     seq.seqString());



        // now check that the expected features are in the model

        //



        // get sequence_map feature container

        //

        Iterator itr = seq.features();

        Feature set = (Feature) itr.next();



        assertNotNull("Did not find feature set", set);

        assertEquals("Feature Set does not have correct type",

                     "sequence_map",

                     set.getType());



        // get feature within sequence_map

        //

        itr = set.features();

        Feature f = (Feature) itr.next();

        assertEquals("Feature does not have correct type",

                     "exon",

                     f.getType());



        // get location

        //

        Location loc = f.getLocation();



        assertEquals("location start incorrect.",

                     1,

                     loc.getMin());

        assertEquals("location end incorrect.",

                     20,

                     loc.getMax());





    }



    /**

     * Demonstrate nested features and feature orientation.

     */

    public void

    testNestedSeqFeature() throws Exception

    {

        // create initial data model

        //

        Reader r = new StringReader

        (

            "<?xml version=\"1.0\"?>\n" +

            "<!DOCTYPE sciobj SYSTEM \"agave.dtd\">\n" +

            "<sciobj version=\"2\" release=\"3\">\n" +

            "  <bio_sequence seq_length=\"20\">" +

            "    <db_id id=\"AC1234\" db_code=\"fake\"/>\n" +

            "    <sequence>aaaaaaaaaaaaaaaaaaaa</sequence>\n" +

            "    <sequence_map label=\"algorithm\">\n" +

            "      <annotations>\n" +

            "        <seq_feature element_id=\"ABC\" feature_type=\"gene\">\n" +

            "          <seq_location is_on_complement=\"true\" least_start=\"1\" greatest_end=\"20\"></seq_location>\n" +

            "          <seq_feature element_id=\"ABC.1\" feature_type=\"exon\">\n" +

            "              <seq_location is_on_complement=\"true\" least_start=\"1\" greatest_end=\"10\"></seq_location>\n" +

            "          </seq_feature>\n" +

            "          <seq_feature element_id=\"ABC.2\" feature_type=\"exon\">\n" +

            "              <seq_location is_on_complement=\"true\" least_start=\"15\" greatest_end=\"20\"></seq_location>\n" +

           "          </seq_feature>\n" +

            "        </seq_feature>\n" +

             "      </annotations>\n" +

            "    </sequence_map>\n" +

            "  </bio_sequence>\n" +

            "</sciobj>"

        );

        Sequence seq = parse(r);



        // check that the expected features are in the model

        //



        // get sequence_map feature container

        //

        Iterator itr = seq.features();

        Feature set = (Feature) itr.next();



        assertEquals("Feature Set does not have correct type",

                     "sequence_map",

                     set.getType());



        // get feature within sequence_map

        //

        itr = set.features();

        Feature f = (Feature) itr.next();

        assertEquals("Feature does not have correct type",

                     "gene",

                     f.getType());



        // get sub-features

        //

        itr = f.features();

        StrandedFeature sub1 = (StrandedFeature) itr.next();

        StrandedFeature sub2 = (StrandedFeature) itr.next();



        assertEquals("Feature does not have correct type",

                     "exon",

                     sub1.getType());

        Location loc = sub1.getLocation();

        assertEquals("location start incorrect.",

                     1,

                     loc.getMin());

        assertEquals("location end incorrect.",

                     10,

                     loc.getMax());



        assertEquals("Feature does not have correct type",

                     "exon",

                     sub2.getType());

        loc = sub2.getLocation();

        assertEquals("location start incorrect.",

                     15,

                     loc.getMin());

        assertEquals("location end incorrect.",

                     20,

                     loc.getMax());



        assertEquals("Feature does not have correct type",

                     "exon",

                     sub2.getType());



        assertEquals("sub feature is not on reverse strand.",

                     StrandedFeature.NEGATIVE,

                     sub2.getStrand());

    }



    /**

     * Demonstrate hierarchical DNA assembly

     *

     */

    public void

    testContig() throws Exception

    {

        // create initial data model

        //

        String doc =

            "<?xml version=\"1.0\"?>\n" +

            "<!DOCTYPE sciobj SYSTEM \"agave.dtd\">\n" +

            "<sciobj version=\"2\" release=\"3\">\n" +

            "  <contig length=\"40\">\n"  +

            "    <db_id id=\"AC1234\" db_code=\"fake\"/>\n" +

            "    <assembly>" +

            "      <bio_sequence seq_length=\"20\">" +

            "        <db_id id=\"AC1234.1\" db_code=\"fake1\"/>\n" +

            "        <sequence>atatatatatatatatatat</sequence>\n" +

            "        <sequence_map label=\"algorithm\">\n" +

            "          <annotations>\n" +

            "            <seq_feature element_id=\"ABCD\" feature_type=\"exon\">\n" +

            "              <seq_location is_on_complement=\"false\" least_start=\"1\" greatest_end=\"20\"></seq_location>\n" +

            "            </seq_feature>\n" +

            "          </annotations>\n" +

            "        </sequence_map>\n" +

            "      </bio_sequence>\n" +

            "      <bio_sequence seq_length=\"20\">" +

            "        <db_id id=\"AC1234.2\" db_code=\"fake2\"/>\n" +

            "        <sequence>gggggggggggggggggggg</sequence>\n" +

            "        <sequence_map label=\"algorithm\">\n" +

            "          <annotations>\n" +

            "            <seq_feature element_id=\"ABC\" feature_type=\"exon\">\n" +

            "              <seq_location is_on_complement=\"false\" least_start=\"1\" greatest_end=\"20\"></seq_location>\n" +

            "            </seq_feature>\n" +

            "          </annotations>\n" +

            "        </sequence_map>\n" +

            "      </bio_sequence>\n" +

            "    </assembly>" +

            "  </contig>" +

            "</sciobj>";

        Reader r = new StringReader(doc);

        Sequence contig = parse(r);



        // get component sequences

        //

        Iterator itr = contig.features();

        ComponentFeature component = (ComponentFeature) itr.next();

        Sequence seq1 = component.getComponentSequence();

        component = (ComponentFeature) itr.next();

        Sequence seq2 = component.getComponentSequence();



        assertEquals("Sequence 1 strings not equal",

             "atatatatatatatatatat",

             seq1.seqString());



        assertEquals("Sequence 2 strings not equal",

             "gggggggggggggggggggg",

             seq2.seqString());





    }



    /**

     * Parse AGAVE XML from the Reader and return the Sequence.

     *

     */

    protected Sequence

    parse(Reader r) throws Exception

    {

        SeqIOListener siol = new SeqIOAdapter();

        AGAVEHandler agavehandler = new AGAVEHandler();

        agavehandler.setFeatureListener(siol);
        
        SAXParserFactory spf = SAXParserFactory.newInstance();
        
        SAXParser parser = spf.newSAXParser();
        spf.setNamespaceAware(true);
        spf.setValidating(true);

	parser.getXMLReader().setEntityResolver(new ResourceEntityResolver("org/agavexml"));

        parser.getXMLReader().setContentHandler(new SAX2StAXAdaptor(agavehandler));

        InputSource in = new InputSource(r);

        parser.getXMLReader().parse(in);

        Iterator  itr = agavehandler.getSequences();



        Sequence seq = (Sequence) itr.next();

        return seq;

    }

}





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
package org.biojava.bio.seq.io.agave;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.utils.xml.*;

import org.xml.sax.*;

import org.apache.xerces.parsers.*;

import org.biojava.bio.seq.io.agave.*;

/**
 *.
 * JUnit test case of AGAVE XML to BioJava input/output.
 *
 * @author Brian King
 */
public class AGAVEHandlerTest extends TestCase
{
    /**
     * Constructs a test case with the given name
     *
     */
    public AGAVEHandlerTest(String name)
    {
        super(name);
    }

    /**
     *  Test translation of AGAVE sequence and feature to BioJava model.
     */
    public void
    testBioSequence() throws Exception
    {
        // create initial data model
        //
        Reader r = new StringReader
        (
            "<?xml version=\"1.0\"?>\n" +
            "<!DOCTYPE sciobj SYSTEM \"agave.dtd\">\n" +
            "<sciobj version=\"2\" release=\"3\">\n" +
            "  <bio_sequence seq_length=\"20\">" +
            "    <db_id id=\"AC1234\" db_code=\"fake\"/>\n" +
            "    <sequence>aaaaaaaaaaaaaaaaaaaa</sequence>\n" +
            "    <sequence_map label=\"algorithm\">\n" +
            "      <annotations>\n" +
            "        <seq_feature element_id=\"ABC\" feature_type=\"exon\">\n" +
            "          <seq_location is_on_complement=\"false\" least_start=\"1\" greatest_end=\"20\"></seq_location>\n" +
            "        </seq_feature>\n" +
             "      </annotations>\n" +
            "    </sequence_map>\n" +
            "  </bio_sequence>\n" +
            "</sciobj>"
        );
        Sequence seq = parse(r);

        assertEquals("Sequence strings not equal",
                     "aaaaaaaaaaaaaaaaaaaa",
                     seq.seqString());

        // now check that the expected features are in the model
        //

        // get sequence_map feature container
        //
        Iterator itr = seq.features();
        Feature set = (Feature) itr.next();

        assertNotNull("Did not find feature set", set);
        assertEquals("Feature Set does not have correct type",
                     "sequence_map",
                     set.getType());

        // get feature within sequence_map
        //
        itr = set.features();
        Feature f = (Feature) itr.next();
        assertEquals("Feature does not have correct type",
                     "exon",
                     f.getType());

        // get location
        //
        Location loc = f.getLocation();

        assertEquals("location start incorrect.",
                     1,
                     loc.getMin());
        assertEquals("location end incorrect.",
                     20,
                     loc.getMax());


    }

    /**
     * Demonstrate nested features and feature orientation.
     */
    public void
    testNestedSeqFeature() throws Exception
    {
        // create initial data model
        //
        Reader r = new StringReader
        (
            "<?xml version=\"1.0\"?>\n" +
            "<!DOCTYPE sciobj SYSTEM \"agave.dtd\">\n" +
            "<sciobj version=\"2\" release=\"3\">\n" +
            "  <bio_sequence seq_length=\"20\">" +
            "    <db_id id=\"AC1234\" db_code=\"fake\"/>\n" +
            "    <sequence>aaaaaaaaaaaaaaaaaaaa</sequence>\n" +
            "    <sequence_map label=\"algorithm\">\n" +
            "      <annotations>\n" +
            "        <seq_feature element_id=\"ABC\" feature_type=\"gene\">\n" +
            "          <seq_location is_on_complement=\"true\" least_start=\"1\" greatest_end=\"20\"></seq_location>\n" +
            "          <seq_feature element_id=\"ABC.1\" feature_type=\"exon\">\n" +
            "              <seq_location is_on_complement=\"true\" least_start=\"1\" greatest_end=\"10\"></seq_location>\n" +
            "          </seq_feature>\n" +
            "          <seq_feature element_id=\"ABC.2\" feature_type=\"exon\">\n" +
            "              <seq_location is_on_complement=\"true\" least_start=\"15\" greatest_end=\"20\"></seq_location>\n" +
           "          </seq_feature>\n" +
            "        </seq_feature>\n" +
             "      </annotations>\n" +
            "    </sequence_map>\n" +
            "  </bio_sequence>\n" +
            "</sciobj>"
        );
        Sequence seq = parse(r);

        // check that the expected features are in the model
        //

        // get sequence_map feature container
        //
        Iterator itr = seq.features();
        Feature set = (Feature) itr.next();

        assertEquals("Feature Set does not have correct type",
                     "sequence_map",
                     set.getType());

        // get feature within sequence_map
        //
        itr = set.features();
        Feature f = (Feature) itr.next();
        assertEquals("Feature does not have correct type",
                     "gene",
                     f.getType());

        // get sub-features
        //
        itr = f.features();
        StrandedFeature sub1 = (StrandedFeature) itr.next();
        StrandedFeature sub2 = (StrandedFeature) itr.next();

        assertEquals("Feature does not have correct type",
                     "exon",
                     sub1.getType());
        Location loc = sub1.getLocation();
        assertEquals("location start incorrect.",
                     1,
                     loc.getMin());
        assertEquals("location end incorrect.",
                     10,
                     loc.getMax());

        assertEquals("Feature does not have correct type",
                     "exon",
                     sub2.getType());
        loc = sub2.getLocation();
        assertEquals("location start incorrect.",
                     15,
                     loc.getMin());
        assertEquals("location end incorrect.",
                     20,
                     loc.getMax());

        assertEquals("Feature does not have correct type",
                     "exon",
                     sub2.getType());

        assertEquals("sub feature is not on reverse strand.",
                     StrandedFeature.NEGATIVE,
                     sub2.getStrand());
    }

    /**
     * Demonstrate hierarchical DNA assembly
     *
     */
    public void
    testContig() throws Exception
    {
        // create initial data model
        //
        String doc =
            "<?xml version=\"1.0\"?>\n" +
            "<!DOCTYPE sciobj SYSTEM \"agave.dtd\">\n" +
            "<sciobj version=\"2\" release=\"3\">\n" +
            "  <contig length=\"40\">\n"  +
            "    <db_id id=\"AC1234\" db_code=\"fake\"/>\n" +
            "    <assembly>" +
            "      <bio_sequence seq_length=\"20\">" +
            "        <db_id id=\"AC1234.1\" db_code=\"fake1\"/>\n" +
            "        <sequence>atatatatatatatatatat</sequence>\n" +
            "        <sequence_map label=\"algorithm\">\n" +
            "          <annotations>\n" +
            "            <seq_feature element_id=\"ABCD\" feature_type=\"exon\">\n" +
            "              <seq_location is_on_complement=\"false\" least_start=\"1\" greatest_end=\"20\"></seq_location>\n" +
            "            </seq_feature>\n" +
            "          </annotations>\n" +
            "        </sequence_map>\n" +
            "      </bio_sequence>\n" +
            "      <bio_sequence seq_length=\"20\">" +
            "        <db_id id=\"AC1234.2\" db_code=\"fake2\"/>\n" +
            "        <sequence>gggggggggggggggggggg</sequence>\n" +
            "        <sequence_map label=\"algorithm\">\n" +
            "          <annotations>\n" +
            "            <seq_feature element_id=\"ABC\" feature_type=\"exon\">\n" +
            "              <seq_location is_on_complement=\"false\" least_start=\"1\" greatest_end=\"20\"></seq_location>\n" +
            "            </seq_feature>\n" +
            "          </annotations>\n" +
            "        </sequence_map>\n" +
            "      </bio_sequence>\n" +
            "    </assembly>" +
            "  </contig>" +
            "</sciobj>";
        Reader r = new StringReader(doc);
        Sequence contig = parse(r);

        // get component sequences
        //
        Iterator itr = contig.features();
        ComponentFeature component = (ComponentFeature) itr.next();
        Sequence seq1 = component.getComponentSequence();
        component = (ComponentFeature) itr.next();
        Sequence seq2 = component.getComponentSequence();

        assertEquals("Sequence 1 strings not equal",
             "atatatatatatatatatat",
             seq1.seqString());

        assertEquals("Sequence 2 strings not equal",
             "gggggggggggggggggggg",
             seq2.seqString());


    }

    /**
     * Parse AGAVE XML from the Reader and return the Sequence.
     *
     */
    protected Sequence
    parse(Reader r) throws Exception
    {
        SeqIOListener siol = new SeqIOAdapter();
        AGAVEHandler agavehandler = new AGAVEHandler();
        agavehandler.setFeatureListener(siol);
        SAXParser parser = new SAXParser();
	parser.setEntityResolver(new ResourceEntityResolver("org/agavexml"));
        parser.setContentHandler(new SAX2StAXAdaptor(agavehandler));
        InputSource in = new InputSource(r);
        parser.parse(in);
        Iterator  itr = agavehandler.getSequences();

        Sequence seq = (Sequence) itr.next();
        return seq;
    }
}

