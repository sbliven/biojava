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


package org.biojava.bio.symbol;

import org.biojava.utils.*;
import org.biojava.bio.seq.*;

/**
 * This class makes PackedSymbolLists.
 *
 * @author David Huen
 */
public class PackedSymbolListFactory

    implements SymbolListFactory
{
    private boolean ambiguity;


    /**
     * Create a factory for PackedSymbolLists.
     *
     * @param is ambiguity to be supported by the encoding?
     */
    public PackedSymbolListFactory(boolean ambiguity)
    {
        this.ambiguity = ambiguity;
    }

    public SymbolList makeSymbolList(Symbol [] symbolArray, int size, Alphabet alfa)
        throws IllegalAlphabetException
    {
        return new PackedSymbolList(PackingFactory.getPacking((FiniteAlphabet) alfa, ambiguity), symbolArray, size, alfa);
    }
}

