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

package org.biojava.bio.program.gff;

import org.biojava.bio.*;

/**
 * The interface for things that listen to GFF event streams.
 * <P>
 * This allows the GFF push model to run over large collections of GFF, filter
 * them and access other resources without requiering vast numbers of GFF
 * records to be in memory at any one time.
 *
 * @author Matthew Pocock
 */
public interface GFFDocumentHandler{
  void startDocument();
  void endDocument();
  
  void commentLine(String comment);
  void recordLine(GFFRecord record);
  
  void invalidStart(String token, NumberFormatException nfe)
  throws BioException;
  void invalidEnd(String token, NumberFormatException nfe)
  throws BioException;
  void invalidScore(String token, NumberFormatException nfe)
  throws BioException;
  void invalidStrand(String token)
  throws BioException;
  void invalidFrame(String token, NumberFormatException nfe)
  throws BioException;
}
