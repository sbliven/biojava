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
 * This interface contains both methods for handeling the data, and for
 * recovering from errors.
 * <P>
 * This allows the GFF push model to run over large collections of GFF, filter
 * them and access other resources without requiering vast numbers of GFF
 * records to be in memory at any one time.
 * <P>
 * The stream includes both GFF records and comment lines. A particular
 * handeler may choose to discard either of these.
 * <P>
 * It is assumed that a particular handeler will only be used to listen to
 * a single stream of events in a single thread. Particular implementations
 * may not impose this restriction.
 * 
 * @author Matthew Pocock
 */
public interface GFFDocumentHandler{
  /**
   * Indicates that a new GFF document has been started.
   * <P>
   * This gives you a hook to set up per-document resources.
   */
  void startDocument();
  /**
   * Indicates that the current GFF document has now ended.
   * <P>
   * This gives you the chance to flush results, or do calculations if
   * you wish.
   */
  void endDocument();
  
  /**
   * A comment line has been encountered.
   * <P>
   * <span class="arg">comment</span> has already had the leading '<code>#</code>'
   * removed, and may have had leading-and-trailing whitespace trimmed.
   *
   * @param comment  the comment <span class="type">String</span>
   */
  void commentLine(String comment);
  
  /**
   * A record line has been encountered.
   * <P>
   * It is already preseneted to you into a <span class="type">GFFRecord</span> object.
   *
   * @param record  the <span class="type">GFFRecord</span> containing all the info
   */
  void recordLine(GFFRecord record);
  
  /**
   * The start field was not a valid value.
   * <P>
   * This is invoked if <span class="arg">token</span> could not be turned into a
   * number indicating the start position of a record. It is called
   * before <span class="method">recordLine</span> is called, giving the handeler
   * a chance to correct the problem. If it is unrecoverable, then
   * throw a <span class="type">BioException</span> giving your reasons.
   *
   * @param token  the <span class="type">String</span> that couldn't be parsed into an
   *               index
   * @param nfe    the <span class="type">NumberFormatException</span> that says what is
   *               wrong
   * @throws <span class="type">BioException</span> if the handeler can not recover from
   *         this error
   */
  void invalidStart(String token, NumberFormatException nfe)
  throws BioException;

  /**
   * The end field was not a valid value.
   * <P>
   * This is invoked if <span class="arg">token</span> could not be turned into a
   * number indicating the end position of a record. It is called
   * before <span class="method">recordLine</span> is called, giving the handeler
   * a chance to correct the problem. If it is unrecoverable, then
   * throw a <span class="type">BioException</span> giving your reasons.
   *
   * @param token  the <span class="type">String</span> that couldn't be parsed into an
   *               index
   * @param nfe    the <span class="type">NumberFormatException</span> that says what is
   *               wrong
   * @throws <span class="type">BioException</span> if the handeler can not recover from
   *         this error
   */
  void invalidEnd(String token, NumberFormatException nfe)
  throws BioException;

  /**
   * The score field was not a valid value.
   * <P>
   * This is invoked if <span class="arg">token</span> could not be turned into a
   * number indicating the score of a record, or if the score was
   * not the <i>no score</i> character '<code>.</code>'.
   * It is called
   * before <span class="method">recordLine</span> is called, giving the handeler
   * a chance to correct the problem. If it is unrecoverable, then
   * throw a <span class="type">BioException</span> giving your reasons.
   *
   * @param token  the <span class="type">String</span> that couldn't be parsed into an
   *               index
   * @param nfe    the <span class="type">NumberFormatException</span> that says what is
   *               wrong, or <span class="kw">null</span>
   * @throws <span class="type">BioException</span> if the handeler can not recover from
   *         this error
   */
  void invalidScore(String token, NumberFormatException nfe)
  throws BioException;

  /**
   * The strand field was not a valid value.
   * <P>
   * This is invoked if <span class="arg">token</span> could not be turned into a
   * number indicating the strand of a record, or if the score was
   * not the <i>no strand</i> character '<code>.</code>'.
   * It is called
   * before <span class="method">recordLine</span> is called, giving the handeler
   * a chance to correct the problem. If it is unrecoverable, then
   * throw a <span class="type">BioException</span> giving your reasons.
   *
   * @param token  the <span class="type">String</span> that couldn't be parsed into an
   *               index
   * @param nfe    the <span class="type">NumberFormatException</span> that says what is
   *               wrong if the token could not be parsed, or <span class="kw">null</span>
   * @throws <span class="type">BioException</span> if the handeler can not recover from
   *         this error
   */
  void invalidStrand(String token)
  throws BioException;
  
  /**
   * The frame field was not a valid value.
   * <P>
   * This is invoked if <span class="arg">token</span> could not be turned into a
   * number indicating the frame of a record, or if the frame was
   * not the <i>no frame</i> character '<code>-</code>', or if it was
   * not one of <code>{1, 2, 3}</code>.
   * It is called
   * before <span class="method">recordLine</span> is called, giving the handeler
   * a chance to correct the problem. If it is unrecoverable, then
   * throw a <span class="type">BioException</span> giving your reasons.
   *
   * @param token  the <span class="type">String</span> that couldn't be parsed into an
   *               index
   * @param nfe    the <span class="type">NumberFormatException</span> that says what is
   *               wrong
   * @throws <span class="type">BioException</span> if the handeler can not recover from
   *         this error
   */
  void invalidFrame(String token, NumberFormatException nfe)
  throws BioException;
}
