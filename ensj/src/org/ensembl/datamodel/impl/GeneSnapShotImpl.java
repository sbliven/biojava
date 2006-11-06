/*
	Copyright (C) 2003 EBI, GRL

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.ensembl.datamodel.impl;

import java.util.Iterator;

import org.ensembl.datamodel.ArchiveStableID;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.GeneSnapShot;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.TranscriptSnapShot;
import org.ensembl.datamodel.Translation;
import org.ensembl.datamodel.TranslationSnapShot;

/**
 * Implementation of GeneSnapshot.
 */
public class GeneSnapShotImpl implements GeneSnapShot {

  /**
   * Used by the (de)serialization system to determine if the data 
   * in a serialized instance is compatible with this class.
   *
   * It's presence allows for compatible serialized objects to be loaded when
   * the class is compatible with the serialized instance, even if:
   *
   * <ul>
   * <li> the compiler used to compile the "serializing" version of the class
   * differs from the one used to compile the "deserialising" version of the
   * class.</li>
   *
   * <li> the methods of the class changes but the attributes remain the same.</li>
   * </ul>
   *
   * Maintainers must change this value if and only if the new version of
   * this class is not compatible with old versions. e.g. attributes
   * change. See Sun docs for <a
   * href="http://java.sun.com/j2se/1.4.2/docs/guide/serialization/">
   * details. </a>
   *
   */
  private static final long serialVersionUID = 1L;



	/**
	 * Constructs a GeneSnapShotImpl from a Gene.
	 * @param gene gene to create a snapshot from.
	 * @param oldDatabaseName name of the dtabase this snapshot came from
	 */
	public GeneSnapShotImpl(Gene gene, String oldDatabaseName) {

		this.archiveStableID = new ArchiveStableIDImpl(gene.getAccessionID(), gene.getVersion(), oldDatabaseName);
		this.transcriptSnapShots = new TranscriptSnapShot[gene.getTranscripts().size()];

		int i = 0;

		for (Iterator iter = gene.getTranscripts().iterator(); iter.hasNext();) {
			Transcript transcript = (Transcript)iter.next();
			Translation translation = transcript.getTranslation();

			if (translation != null) {
				ArchiveStableID id = new ArchiveStableIDImpl(translation.getAccessionID(), translation.getVersion(), oldDatabaseName);
				TranslationSnapShot translationSnapShot = new TranslationSnapShotImpl(id, translation.getPeptide());

				id = new ArchiveStableIDImpl(transcript.getAccessionID(), transcript.getVersion(), oldDatabaseName);
				transcriptSnapShots[i++] = new TranscriptSnapShotImpl(id, translationSnapShot);
				
			} 
		}
	
	}

	private ArchiveStableID archiveStableID;

	private TranscriptSnapShot[] transcriptSnapShots;

	/**
	 * Construct snapshot with specified archiveStableID and transcripts.
	 */
	public GeneSnapShotImpl(ArchiveStableID archiveStableID, TranscriptSnapShot[] transcriptSnapShots) {
		this.archiveStableID = archiveStableID;
		this.transcriptSnapShots = transcriptSnapShots;
	}

	public TranscriptSnapShot[] getTranscriptSnapShots() {
		return transcriptSnapShots;
	}

	public ArchiveStableID getArchiveStableID() {
		return archiveStableID;
	}

}
