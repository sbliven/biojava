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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

/**
 * Writes Nexus files.
 * 
 * @author Richard Holland
 * @author Tobias Thierer
 * @author Jim Balhoff
 * @since 1.6
 */
public class NexusFileWriter {

	/**
	 * Set up a new writer object.
	 */
	public NexusFileWriter() {
	}

	/**
	 * Writes the given Nexus output to a file.
	 * 
	 * @param file
	 *            the file to write to.
	 * @param nexusFile
	 *            the Nexus output to write.
	 * @throws IOException
	 *             if there is a problem during writing.
	 */
	public void writeFile(final File file, final NexusFile nexusFile)
			throws IOException {
		final FileWriter fw = new FileWriter(file);
		try {
			this.writeWriter(fw, nexusFile);
		} finally {
			fw.flush();
			fw.close();
		}
	}

	/**
	 * Writes the given Nexus output to a stream.
	 * 
	 * @param os
	 *            the stream to write to.
	 * @param nexusFile
	 *            the Nexus output to write.
	 * @throws IOException
	 *             if there is a problem during writing.
	 */
	public void writeStream(final OutputStream os, final NexusFile nexusFile)
			throws IOException {
		final OutputStreamWriter ow = new OutputStreamWriter(os);
		try {
			this.writeWriter(ow, nexusFile);
		} finally {
			ow.flush();
		}
	}

	/**
	 * Writes the given Nexus output to a writer.
	 * 
	 * @param writer
	 *            the writer to write to.
	 * @param nexusFile
	 *            the Nexus output to write.
	 * @throws IOException
	 *             if there is a problem during writing.
	 */
	public void writeWriter(final Writer writer, final NexusFile nexusFile)
			throws IOException {
		writer.write("#NEXUS");
		writer.write(NexusFileParser.NEW_LINE);
		for (final Iterator i = nexusFile.objectIterator(); i.hasNext(); ) {
			((NexusObject)i.next()).writeObject(writer);
			writer.write(NexusFileParser.NEW_LINE);
		}
		writer.flush();
	}
}
