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
 * see also DAS specification at <a href="http://wwwdev.sanger.ac.uk/xml/das/documentation/new_spec.html">http://wwwdev.sanger.ac.uk/xml/das/documentation/new_spec.html</a>
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

    /** define the alignment Score Annotation Type.
     *
     * @return an AnnotationType object representing the score annotation type 
     */
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

    /** define the alignment Block Annotation Type.
     *
     *
     * @return an AnnotationType object representing the block annotation type 
     */
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

      /** define the alignment Segment Annotation Type.
       *
       *
       * @return an AnnotationType object representing the segment annotation type

       */
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


    /** define the alignment object Annotation Type.
     *
     *
     * @return an AnnotationType object representing the object annotation type
     */
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
	
	annType.setConstraints("sequence", 
			       new PropertyConstraint.ByClass(String.class),
			       CardinalityConstraint.ANY );

	annType.setConstraints("seqStart", 
			       new PropertyConstraint.ByClass(String.class),
			       CardinalityConstraint.ANY );

	annType.setConstraints("seqEnd", 
			       new PropertyConstraint.ByClass(String.class),
			       CardinalityConstraint.ANY );
	return annType ;
    }
    
    /** add Annotation of DAS alignment "object" type.
     *
     @see #getObjectAnnotationType
     *
     * @param object  an Annotation object
     * @throws DASException ...
    */

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

    /**
     * Returns the Annotation of all objects in this Alignment.
     *
     * @return an array of Annotation objects 
     */
    public Annotation[] getObjects(){
        return (Annotation[]) objects.toArray(new Annotation[objects.size()]) ;
    }

    /** adds a "Score" Annotation. 
     * @see #getScoreAnnotationType
     * @param score  an Annotation object
     *
     * @throws DASException ...
    */
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
    
    /** get all "Score" Annotations.
     *
     * @throws DASException ...
     *
     * @return an array of Annotation objects representing the scores value
     */
    public Annotation[] getScores(){
        return (Annotation[])scores.toArray(new Annotation[scores.size()]) ;
    }

    /** Add a "Block" Annotation.
     * @see #getBlockAnnotationType
     *
     * @param block  an Annotation object
     * @throws DASException ...
     */
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

    /** get all Annotations of type "Block". 
     * @return an array of Annotation objects representing the Aligmnent blocks 
     */
    public Annotation[] getBlocks() {
        return (Annotation[])blocks.toArray(new Annotation[blocks.size()]);
    }
    
    /** convert to String. */
    public String toString() {
	String str = "" ;
	for ( int i=0;i<objects.size();i++){
	    Annotation object = (Annotation)objects.get(i);
	    str += "object: "+ (String)object.getProperty("dbAccessionId")+"\n";	    
	}
	str += "number of blocks: "+blocks.size();
	return str ;
    }
}
