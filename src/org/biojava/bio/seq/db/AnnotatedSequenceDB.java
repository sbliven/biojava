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

package org.biojava.bio.seq.db;

import java.io.Serializable;

import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.*;
import java.util.*;

/**
 * SequenceDB implementation which lazily applies a SequenceAnnotator
 * to sequences retrieved from a SequenceDB.
 * 
 * @author Thomas Down
 */

public class AnnotatedSequenceDB implements SequenceDB, Serializable {
  private final SequenceDB parent;
  private final SequenceAnnotator annotator;

  public AnnotatedSequenceDB(SequenceDB parent, SequenceAnnotator a) {
    this.parent = parent;
    this.annotator = a;
  }

  public SequenceDB getParent() {
    return this.parent;
  }
  
  public String getName() {
    return parent.getName() + " (" + annotator.toString() + ")";
  }

  public Sequence getSequence(String id) throws BioException {
    return doAnnotation(parent.getSequence(id));
  }

  public Set ids() {
    return parent.ids();
  }

  public SequenceIterator sequenceIterator() {
    return new SequenceIterator() {
      SequenceIterator pi = parent.sequenceIterator();

	    public boolean hasNext() {
        return pi.hasNext();
	    }

	    public Sequence nextSequence() throws BioException {
        return doAnnotation(pi.nextSequence());
	    }
    };
  }

  protected Sequence doAnnotation(Sequence seq) throws BioException {
    try {
	    return annotator.annotate(seq);
    } catch (IllegalAlphabetException ex) {
	    throw new BioException(ex, "Couldn't apply annotator " + annotator.toString() + " to " + seq.getURN());
    }
  }
}
