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

package org.biojavax.bio.seq.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.SequenceFormat;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.utils.ParseErrorSource;
import org.biojavax.Namespace;


/**
 *
 * @author Richard Holland
 */

public interface RichSequenceFormat extends SequenceFormat,ParseErrorSource {
    
    public boolean readRichSequence(
            BufferedReader reader, SymbolTokenization symParser, RichSeqIOListener listener,Namespace ns
            ) throws BioException, IllegalSymbolException, IOException;
    public void writeSequence(Sequence seq, PrintStream os, Namespace ns) throws IOException;
    public void writeSequence(Sequence seq, String format, PrintStream os, Namespace ns) throws IOException;    
}
