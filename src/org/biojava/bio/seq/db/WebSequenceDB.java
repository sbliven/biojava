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

import java.util.*;
import java.net.*;
import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.io.*;
import org.biojava.utils.*;

/**
 * Functions for access to a web based database that returns sequences
 * in a variety of formats.
 *
 * @author Jason Stajich
 * @author Matthew Pocock
 */

public abstract class WebSequenceDB
extends AbstractChangeable
implements SequenceDBLite {
  protected abstract SequenceFormat getSequenceFormat();

  protected abstract URL getAddress(String id)
  throws MalformedURLException;

  protected abstract Alphabet getAlphabet();

  public Sequence getSequence(String id)
  throws BioException {
    if( id.equals("") ) {
      throw new BioException("did not specify a valid id for getSequence");
    }

    try {
      URL queryURL = getAddress(id);
      System.err.println("query is "+ queryURL.toString());
      URLConnection connection = queryURL.openConnection();
      SequenceFormat sFormat = getSequenceFormat();

//      SequenceBuilder sbuilder = new SimpleSequenceBuilder();
//      FastaDescriptionLineParser sFact =
//        new FastaDescriptionLineParser(sbuilder);

      Alphabet alpha = getAlphabet();
      SequenceBuilderFactory sFact = SeqIOTools.formatToFactory(sFormat,alpha);
      SequenceBuilder sbuilder = sFact.makeSequenceBuilder();
      SymbolTokenization rParser = alpha.getTokenization("token");
      System.err.println("got data from "+ queryURL);
      SequenceIterator seqI = new StreamReader(
        connection.getInputStream(),
        sFormat, rParser, sFact
      );

      return seqI.nextSequence();
    } catch ( Exception e ){
      throw new BioException(e);
    }
  }


  public void addSequence(Sequence seq)
  throws ChangeVetoException {
    throw new ChangeVetoException(
      "Can't add sequences from web sequence DB: " +
      seq.getName()
    );
  }

  public void removeSequence(String id)
  throws ChangeVetoException {
    throw new ChangeVetoException(
      "Can't remove sequences from web sequence DB: " +
      id
    );
  }
}
