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
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * An abstract implemenation of Annotator that takes care of the loop-over-db
 * step for you.
 *
 * @author Matthew Pocock
 */
public abstract class AbstractAnnotator implements Annotator {

  public SequenceDB annotate(SequenceDB sdb)
        throws IllegalSymbolException, BioException {
    HashSequenceDB hitDB = new HashSequenceDB(null);

    for(Iterator i = sdb.ids().iterator(); i.hasNext(); ) {
      String id = (String) i.next();
      Sequence seq = sdb.getSequence(id);
      if(annotate(seq)) {
        hitDB.addSequence(id, seq);
      }
    }

    return hitDB;
  }
}
