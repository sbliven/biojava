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

package org.biojava.bio.seq;

import java.util.*;
import java.io.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.impl.*;
import junit.framework.TestCase;

/**
 * Tests for Serialization
 *
 * @author Mark Schreiber
 * @since 1.3
 */

public class ViewSeqSerializationTest extends TestCase
{
    protected Sequence seq;
    protected Sequence seq2;


    public ViewSeqSerializationTest(String name) {
	super(name);
    }

    protected void setUp() throws Exception {
	seq = new SimpleSequence(DNATools.createDNA("aacgtaggttccatgc"),
				       "fragment1",
				       "fragment1",
				       Annotation.EMPTY_ANNOTATION);
	
	Feature.Template sft = new Feature.Template();
	sft.type = "test";
	sft.source = "test";
	sft.annotation = Annotation.EMPTY_ANNOTATION;
	sft.location = new RangeLocation(1, 3);
	seq.createFeature(sft);

	sft.location = new RangeLocation(10, 12);
	seq.createFeature(sft);
	seq = new CircularView(seq);
	sft.location = new RangeLocation(5,8);
	seq.createFeature(sft);

	File f = File.createTempFile("ViewSeqSerTest",".tmp");
	ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));        
	oos.writeObject(seq);
	oos.flush();
	oos.close();
	ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
	seq2 = (Sequence)ois.readObject();
    }

    private boolean compareSymbolList(SymbolList sl1, SymbolList sl2) {
	if (sl1.length() != sl2.length()) {
	    return false;
	}
	
	Iterator si1 = sl1.iterator();
	Iterator si2 = sl2.iterator();
	while (si1.hasNext()) {
	    if (! (si1.next() == si2.next())) {
		return false;
	    }
	}

	return true;
    }

    public void testSymbols()
	throws Exception
    {
	assertTrue(compareSymbolList(seq,seq2));
    }

    public void testAlphabet()throws Exception{
	assertTrue(seq.getAlphabet()==seq2.getAlphabet());
    }

    public void testFeatures() throws Exception{
        HashSet features = new HashSet();
	for(Iterator i = seq.features(); i.hasNext();){
	    features.add(i.next());
	}
	for(Iterator i = seq2.features(); i.hasNext();){
	    assertTrue(features.contains(i.next()));
	}
    }
}
