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


package org.acedb.seq;

import java.util.*;

import org.acedb.*;
import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;

/**
 * An implementation of SequenceDB that lets either an entire ACeDB database,
 * or some sub-set of its sequences be viewed as a native BioJava sequence
 * database.
 *
 * @author Matthew Pocock
 * @author <A href="mailto:Gerald.Loeffler@vienna.at">Gerald Loeffler</A>
 */

public class AceSequenceDB implements SequenceDB {
  protected AceSet seqSet;
  
  public String getName() {
    return seqSet.getName();
  }
  
  public Sequence getSequence(String id) throws BioException {
    try {
      return new AceSequence((AceObject) seqSet.retrieve(id));
    } catch (AceException ae) {
      throw new BioException(ae, "unable to retrieve sequence " + id);
    }
  }
  
  public Set ids() {
    return new AbstractSet() {
      public int size() {
        try {
          return seqSet.size();
        } catch (AceException ae) {
          throw new AceError(ae, "Couldn't retrieve set size");
        }
      }

      public Iterator iterator() {
        try {
          return seqSet.nameIterator();
        } catch (AceException ae) {
          throw new AceError(ae, "Couldn't retrieve set size");
        }
      }

      public boolean contains(Object o) {
        if (! (o instanceof String)) {
          return false;
        } else {
          try {
            return seqSet.contains((String) o);
          } catch (AceException ae) {
            throw new AceError(ae, "Couldn't retrieve set size");
          }
        }
      }
    };
  }
  
  public SequenceIterator sequenceIterator() {
    return new SequenceIterator() {
      Iterator id = ids().iterator();
      
      public boolean hasNext() {
        return id.hasNext();
      }
      
      public Sequence nextSequence() throws BioException {
        return getSequence((String) id.next());
      }
    };
  }

  public void addSequence(Sequence seq)
  throws ChangeVetoException {
    throw new ChangeVetoException("SequenceDBAdapter is immutable");
  }

  public void removeSequence(String id)
  throws ChangeVetoException {
    throw new ChangeVetoException("SequenceDBAdapter is immutable");
  }
  
  public void addChangeListener(ChangeListener cl) {}
  public void addChangeListener(ChangeListener cl, ChangeType ct) {}
  public void removeChangeListener(ChangeListener cl) {}
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {}

  public AceSequenceDB(AceURL dbURL, String pattern) throws AceException {
    AceURL url = new AceURL(
      dbURL.getProtocol(),
      dbURL.getHost(),
      dbURL.getPort(),
      "Sequence",
      pattern,
      dbURL.getRef(),
      dbURL.getUserInfo(),
      dbURL.getAuthority()
    );
    this.seqSet = Ace.fetch(url);
  }

  public AceSequenceDB(AceURL dbURL) throws AceException {
    this(dbURL, "*");
  }
} 
