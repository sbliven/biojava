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

import org.biojava.bio.symbol.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.impl.*;
import java.util.*;

/**
 * <p><code>AlignmentSequenceIterator</code> implements a
 * <code>SequenceIterator</code> by creating new
 * <code>Sequence</code>s which wrap the <code>SymbolList</code>s in
 * an <code>Alignment</code>.</p>
 *
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nimesh Singh
 * @version 1.0
 */

public class AlignmentSequenceIterator implements SequenceIterator {

    private Alignment align;
    private Iterator labels;
    private SequenceFactory sf;

    public AlignmentSequenceIterator(Alignment align) {
        this.align = align;
        labels = align.getLabels().iterator();
        sf = new SimpleSequenceFactory();
    }

    public boolean hasNext() {
        return labels.hasNext();
    }

    public Sequence nextSequence() throws NoSuchElementException, BioException {
        if (!hasNext()) {
            throw new NoSuchElementException("No more sequences in the alignment.");
        }
        else {
            try {
                Object label = labels.next();
                SymbolList symList = align.symbolListForLabel(label);
                Sequence seq = sf.createSequence(symList, label.toString(), label.toString(), null);
                return seq;
            } catch (Exception e) {
	        throw new BioException(e, "Could not read sequence");
	    }
        }
    }
}
