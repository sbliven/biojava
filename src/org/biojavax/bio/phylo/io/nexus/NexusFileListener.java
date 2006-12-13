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
package org.biojavax.bio.phylo.io.nexus;

import org.biojava.bio.seq.io.ParseException;

/**
 * Listens to events fired by the Nexus parser. Use these events to handle data
 * directly or construct objects.
 * 
 * @author Richard Holland
 * @author Tobias Thierer
 * @author Jim Balhoff
 * @since 1.6
 */
public interface NexusFileListener {

	/**
	 * About to start a new file.
	 */
	public void startFile();

	/**
	 * Finished reading a file.
	 */
	public void endFile();

	/**
	 * Opening a comment tag.
	 */
	public void beginComment();

	/**
	 * Closing a comment tag.
	 */
	public void endComment();

	/**
	 * Closing a line (semi-colon encountered). This indicates that anything
	 * received after it is on the next logical line of the file.
	 */
	public void newLine();

	/**
	 * Receiving free text inside a comment tag.
	 * 
	 * @param comment
	 *            the text of the comment.
	 */
	public void commentText(String comment) throws ParseException;
}
