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
 * Created on 11.05.2004
 */

package org.biojava.bio.program.das.dasalignment;

import java.util.* ;

/**
 * Alignment object to contain/manage a DAS alignment.  the
 * description of the alignment objects is an ArrayList of HashMaps 
 * @see also DAS specification at http://www.sanger.ac.uk/Users/ap3/DAS/new_spec.html
 * <p>
 * Each objects is described by a HashMap that has the following keys:
 * <pre>
 * mandatory: 
 * * id      
 * * version 
 * * type    
 * * coordinateSystem
 * * optional:
 * * description
 * * sequence
 * ArrayList notes (a list of strings)
 * </pre>
 * <p>
 * scores is an ArrayList of HashMaps (can be empty)
 * keys:
 * mandatory:
 * scorename 
 * scorevalue
 *
 * <p>
 * blocks: ArrayList consisting of ArrayList block. 
 * block:  ArrayList of hashMaps with the following keys:
 * mandatory:
 * id
 * start
 * end
 * optional:
 * orientation
 * cigarstring
 * @author Andreas Prlic
 */


public class Alignment {
 
    ArrayList objects ;

    ArrayList scores  ;
    
    ArrayList blocks ;
    
   
    public Alignment() {
	objects  = new ArrayList();
	scores   = new ArrayList() ;
	blocks   = new ArrayList() ;
    }
    
    public void addObject(HashMap object)
	throws DASException
    {
	// check if object is valid, throws DASException ...
	checkObjectHash(object);
	objects.add(object) ;
    }

    public ArrayList getObjects(){
	return objects ;
    }

    public void addScore(HashMap score)
	throws DASException
    {
	checkScoreHash(score) ;
    }
    
    public ArrayList getScores(){
	return scores ;
    }

    public void addBlock(ArrayList segment)
	throws DASException
    {
	checkBlockList(segment);
	blocks.add(segment);
	
    }


    public ArrayList getBlocks() {
	return blocks;
    }
    
    public String toString() {
	String str = "" ;
	for ( int i=0;i<objects.size();i++){
	    HashMap object = (HashMap)objects.get(i);
	    str += "object: "+ (String)object.get("id")+"\n";	    
	}
	str += "number of blocks: "+blocks.size();
	return str ;
    }

    /** test if the HashMap that decribes an alignment object has
     * all mandatory keys . ( id, version,type,coordinateSystem) */    
    private void checkObjectHash(HashMap object) 
	throws DASException
    {
	validateMap(object, new Object[] {"id","version","type","coordinateSystem"});
    }

    /** test if the HashMap that decribes an alignment score has
     * all mandatory keys . (scorename, scorevalue) */
    private void checkScoreHash(HashMap score)
	throws DASException
    {
	validateMap(score, new Object[] {"scorename","scorevalue"});
    }

    private void checkBlockList(ArrayList block)
	throws DASException
    {
	for (int i=0;i<block.size();i++){
	    HashMap member = (HashMap) block.get(i);
	    validateMap(member,new Object[] {"id","start","end"});
	}
    }

    static void  validateMap(Map m, Object[] requiredKeys)
	throws DASException
    {
	for (int i = 0; i < requiredKeys.length; ++i) {
	    if (!m.containsKey(requiredKeys[i])) {
		throw new DASException("Required key >" + requiredKeys[i] + "< is not present");
	    }
	}
    }
}
