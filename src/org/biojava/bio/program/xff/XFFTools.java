package org.biojava.bio.program.xff;

import java.io.*;

import org.xml.sax.*;
import org.apache.xerces.parsers.*;

import org.biojava.utils.xml.*;
import org.biojava.utils.stax.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.impl.*;

public class XFFTools {
    public static void annotateXFF(File xffFile, final Sequence sequence)
    throws IOException, SAXException, BioException {
        
        SequenceBuilder sb = new SequenceBuilderBase() {
            { seq = sequence; }
            public void addSymbols(Alphabet alpha, Symbol[] syms, int start, int length) {}
        };
        
        XFFFeatureSetHandler xffHandler = new XFFFeatureSetHandler();
        xffHandler.setFeatureListener(sb);
        
        ContentHandler saxHandler = new SAX2StAXAdaptor(xffHandler);
        SAXParser parser = new SAXParser();
        parser.setContentHandler(saxHandler);
        InputSource is = new InputSource(new FileReader(xffFile));
        parser.parse(is);
        
        sb.makeSequence();
    }
    
    public static FeatureHolder readXFF(File xffFile, String seqID, FiniteAlphabet alpha)
    throws IOException, SAXException, BioException {
        SymbolList dummy = new DummySymbolList(alpha, Integer.MAX_VALUE);
        Sequence ourSeq = new SimpleSequence(dummy, seqID, seqID, new SmallAnnotation());
        annotateXFF(xffFile, ourSeq);
        return ourSeq;
    }
    
    public static void writeXFF(File xffFile, FeatureHolder features)
    throws IOException {
        PrintWriter xffPR = new PrintWriter(new FileWriter(xffFile));
        XMLWriter xmlWriter = new PrettyXMLWriter(xffPR);
        XFFWriter xffWriter = new XFFWriter(new PropertyWriter());
        xffWriter.writeFeatureSet(features, xmlWriter);
        xffPR.flush();
        xffPR.close();
    }
}
