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

import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.Annotation;
import org.biojava.bio.AnnotationType;
import org.biojava.bio.CardinalityConstraint;
import org.biojava.bio.CollectionConstraint;
import org.biojava.bio.PropertyConstraint;

/**
 * Alignment object to contain/manage a DAS alignment.  
 * @see also DAS specification at http://wwwdev.sanger.ac.uk/xml/das/documentation/new_spec.html
 *
 * @author Andreas Prlic
 * @since 1.4
 */

public class Alignment {
    private List objects;
    private List scores;
    private List blocks;

    private static final AnnotationType objectType;
    private static final AnnotationType scoreType;
    private static final AnnotationType blockType;
    private static final AnnotationType segmentType;
    
    static {
		objectType  = getObjectAnnotationType() ;	
		scoreType   = getScoreAnnotationType()  ;
		segmentType = getSegmentAnnotationType();
		blockType   = getBlockAnnotationType()  ;
    }
    
    /**
     * Construct a new empty Alignment object.
     */
    
    public Alignment() {
		objects  = new ArrayList();
		scores   = new ArrayList() ;
		blocks   = new ArrayList() ;
    }

    /** define the alignment Score Annotation Type */
    public static AnnotationType getScoreAnnotationType() {
        AnnotationType annType ;
	
	    annType  = new AnnotationType.Impl();

	    annType.setConstraints("methodName",
				   new PropertyConstraint.ByClass(String.class),
				   CardinalityConstraint.ONE ) ;
	    annType.setConstraints("value",
				   new PropertyConstraint.ByClass(String.class),
				   CardinalityConstraint.ONE ) ;
	    return annType ;
    }

    /** define the alignment Block Annotation Type */
    public static AnnotationType getBlockAnnotationType() {
		AnnotationType annType ;
		
		annType  = new AnnotationType.Impl();
		annType.setConstraints("blockOrder",
				       new PropertyConstraint.ByClass(String.class),
				       CardinalityConstraint.ONE ) ;
		annType.setConstraints("blockScore",
				       new PropertyConstraint.ByClass(String.class),
				       CardinalityConstraint.ANY ) ;
	
		PropertyConstraint prop = new PropertyConstraint.ByAnnotationType(segmentType) ;
	
		annType.setConstraint("segments",
				       new CollectionConstraint.AllValuesIn(prop,CardinalityConstraint.ANY)) ;
		
		return annType ;
    }

      /** define the alignment Segment Annotation Type */
    public static AnnotationType getSegmentAnnotationType() {
		AnnotationType annType ;
		
		annType  = new AnnotationType.Impl();
		annType.setConstraints("intObjectId",
				       new PropertyConstraint.ByClass(String.class),
				       CardinalityConstraint.ONE ) ;
		annType.setConstraints("start",
				       new PropertyConstraint.ByClass(String.class),
				       CardinalityConstraint.ANY ) ;
		annType.setConstraints("end",
				       new PropertyConstraint.ByClass(String.class),
				       CardinalityConstraint.ANY) ;
		annType.setConstraints("strand",
				       new PropertyConstraint.ByClass(String.class),
				       CardinalityConstraint.ANY ) ;
		annType.setConstraints("cigar",
				       new PropertyConstraint.ByClass(String.class),
				       CardinalityConstraint.ANY ) ;
		
		return annType ;
    }


    /** define the alignment object Annotation Type */
    public static AnnotationType getObjectAnnotationType() {
	AnnotationType annType;
	
	annType  = new AnnotationType.Impl();
	//annType.setDefaultConstraints(PropertyConstraint.ANY, CardinalityConstraint.ANY) ;
	annType.setConstraints("dbAccessionId",
			       new PropertyConstraint.ByClass(String.class),
			       CardinalityConstraint.ONE );
	annType.setConstraints("intObjectId",
			       new PropertyConstraint.ByClass(String.class),
			       CardinalityConstraint.ONE );
	annType.setConstraints("objectVersion",
			       new PropertyConstraint.ByClass(String.class),
			       CardinalityConstraint.ONE );
	
	// type is an enumeration ... Hm.
	annType.setConstraints("type",
			       new PropertyConstraint.Enumeration(new Object[] {"DNA","PROTEIN","STRUCTURE"}),
			       CardinalityConstraint.ANY );
	
	annType.setConstraints("dbSource",
			       new PropertyConstraint.ByClass(String.class),
			       CardinalityConstraint.ONE );

	annType.setConstraints("dbVersion",
			       new PropertyConstraint.ByClass(String.class),
			       CardinalityConstraint.ONE );
	
	// optional
	annType.setConstraints("dbCoordSys",
			       new PropertyConstraint.ByClass(String.class),
			       CardinalityConstraint.ANY );
	

	return annType ;
    }
    
    
    public void addObject(Annotation object)
		throws DASException
    {
		// check if object is valid, throws DASException ...
		//checkObjectHash(object);
		if(objectType.instanceOf(object)) {
		    objects.add(object) ;
		}  else {
		    throw new 
			IllegalArgumentException(
						 "Expecting an annotation conforming to: " +
						 objectType + " but got: " + object
						 );
		}
    }

    public List getObjects(){
        return objects ;
    }

    public void addScore(Annotation score)
		throws DASException
    {
	//checkScoreHash(score) ;

	if(scoreType.instanceOf(score)) {
	    scores.add(score) ;
	}  else {
	    throw new 
		IllegalArgumentException(
					 "Expecting an annotation conforming to: " +
					 scoreType + " but got: " + score
					 );
	}

    }
    
    public List getScores(){
        return scores ;
    }

    public void addBlock(Annotation block)
		throws DASException
    {
	//checkBlockList(segment);
	//blocks.add(segment);

	if(blockType.instanceOf(block)) {
	    blocks.add(block) ;
	}  else {
	    throw new 
		IllegalArgumentException(
					 "Expecting an annotation conforming to: " +
					 blockType + " but got: " + block
					 );
	}
	
    }


    public List getBlocks() {
        return blocks;
    }
    
    public String toString() {
	String str = "" ;
	for ( int i=0;i<objects.size();i++){
	    Annotation object = (Annotation)objects.get(i);
	    str += "object: "+ (String)object.getProperty("dbAccessionId")+"\n";	    
	}
	str += "number of blocks: "+blocks.size();
	return str ;
    }

   
    /*
    static void  validateMap(Map m, Object[] requiredKeys)
	throws DASException
    {
	for (int i = 0; i < requiredKeys.length; ++i) {
	    if (!m.containsKey(requiredKeys[i])) {
		throw new DASException("Required key >" + requiredKeys[i] + "< is not present");
	    }
	}
    }
    */
}
