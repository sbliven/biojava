/**
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

package org.biojava.bio.seq.io;

import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.PackedSymbolListFactory;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.StaticMemberPlaceHolder;

/**
 * Basic SequenceBuilder implementation which accumulates all
 * notified information and chooses a sequence implementation
 * suited to the size of the sequence.
 *
 */

public class SmartSequenceBuilder extends SequenceBuilderBase {
    public final static SequenceBuilderFactory FACTORY = new SSBFactory();

    private static class SSBFactory implements SequenceBuilderFactory, Serializable {
	private SSBFactory() {
	}

	public SequenceBuilder makeSequenceBuilder() {
	    return new SmartSequenceBuilder();
	}

	private Object writeReplace() throws ObjectStreamException {
	    try {
		return new StaticMemberPlaceHolder(SimpleSequenceBuilder.class.getField("FACTORY"));
	    } catch (NoSuchFieldException nsfe) {
		throw new NotSerializableException(nsfe.getMessage());
	    }
	}
    }

    private ChunkedSymbolListFactory slFactory;

    {
	slFactory = new ChunkedSymbolListFactory(new PackedSymbolListFactory(), -1);
    }

    //
    // SeqIOListener
    //

    public void addSymbols(Alphabet alpha, Symbol[] syms, int pos, int len)
        throws IllegalAlphabetException
    {
	slFactory.addSymbols(alpha, syms, pos, len);
    }


    public Sequence makeSequence() 
        throws BioException
    {
        SymbolList symbols;
        try {
	    symbols = slFactory.makeSymbolList();
            seq = new SimpleSequence(symbols, uri, name, annotation);
        }
        catch (IllegalAlphabetException iae) {
            // this shouldn't happen!!!
        }

        return super.makeSequence();
    }
}
