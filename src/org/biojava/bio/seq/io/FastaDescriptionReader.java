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


package org.biojava.bio.seq.io;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * Converts the description line of a fasta entry into useful information.
 * <P>
 * Fasta files can have rich information shoe-horned into the description. This
 * is unpredictable, and it would be fruitless to write a single routine for
 * decoding all description formats. This interface lets you pop in your own
 * parser, as apropriate.
 * 
 * @author Matthew Pocock
 */
public interface FastaDescriptionReader {
  /**
   * Parse out the name and urn from the description line.
   * <P>
   * Most description lines contain some sort of unique id for the sequence.
   * This routine should extract that information, and construct a sequence name
   * and urn from it.
   *
   * @param desc  the description text from the first non-white space character
   *              after &gt;  in the file untill the end of that line
   * @return String [] { urn, name }
   */
  String [] parseURNName(String desc);
  
  /**
   * Add any annotation to this annotation bundle that can be extracted from
   * the description.
   *
   * @param desc  the description text from the first non-white space character
   *              after &gt;  in the file untill the end of that line
   * @param annotation the Annotation bundle to modify
   */
  void parseAnnotation(String desc, Annotation annotation);
  /**
   * Write the description for a sequence.
   * <P>
   * This should generate a suitable description line, not including the leading
   * &gt; and spaces, or the trailing newline, and return it as a string.
   *
   * @param seq the Sequence to generate a description for
   * @return  the description text
   */
  String writeDescription(Sequence seq);
}
