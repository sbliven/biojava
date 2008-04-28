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
 * created at Mar 4, 2008
 */
package org.biojava.bio.structure.io.mmcif;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.mmcif.model.AtomSite;
import org.biojava.bio.structure.io.mmcif.model.DatabasePDBrev;
import org.biojava.bio.structure.io.mmcif.model.Entity;
import org.biojava.bio.structure.io.mmcif.model.Struct;

/** An interface for the events triggered by a MMcifParser.
 * The Consumer listens to the events and builds up the protein structure.
 *  
 * @author Andreas Prlic
 *
 */
public interface MMcifConsumer {
	public void documentStart();
	public void newAtomSite(AtomSite atom);
	public void newEntity(Entity entity);
	public void setStruct(Struct struct);
	public void newDatabasePDBrev(DatabasePDBrev dbrev);
	
	public void documentEnd();
	
	/** This method will return the parsed protein structure, once the parsing has been finished
	 * 
	 * @return a BioJava protein structure object
	 */
	public Structure getStructure();
}
