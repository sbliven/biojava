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
 * Created on 08.05.2004
 *
 */


package org.biojava.bio.structure ;


import org.biojava.bio.structure.StructureException ;

/** utility operations on Atoms, AminoAcids, etc. 
 * <p>
 * Currently the
 * coordinates of an Atom are stored as an array of size 3
 * (double[3]). It would be more powerful to use Point3D from
 * javax.vecmath.  but unfortunately this is not a part of standard
 * java installations, since it comes with java3d . So to keep things
 * simple at the moment biojava does not depend on java3d.  If the
 * structure part of biojava becomes more powerful it could be moved
 * out of the core - biojava and the dependency on java3d could be
 * introduced.
 * @author Andreas Prlic
 */

public class Calc {

    public Calc(){
    }

    // 180 / pi
    static double RADIAN = 57.29577951 ;

    /**
    
     *    
     */

    /**
     * calculate distance between two atoms
     */
    public static double getDistance(Atom a, Atom b) 
	throws StructureException
    {

	Atom   c    = substract(b,a);
	double dist = amount(c)     ;
    
	return dist ;
    }


    private static void nullCheck(Atom a) 
	throws StructureException
    {
	if  (a == null) {
	    throw new StructureException("Atom is null!");
	}
    }

    /** add two atoms ( a + b)
     */
    public static Atom add(Atom a, Atom b){
	double[] coords = new double[3] ;
    
	coords[0] = a.getX() + b.getX();
	coords[1] = a.getY() + b.getY();
	coords[2] = a.getZ() + b.getZ();
   
	Atom c = new AtomImpl();
	c.setCoords(coords);
	return c ;
    }

    /** substract two atoms ( a - b)
     */
    public static Atom substract(Atom a, Atom b) 
	throws StructureException
    {
	nullCheck(a) ;
	nullCheck(b) ;

	double[] coords = new double[3] ;
	
	coords[0] = a.getX() - b.getX();
	coords[1] = a.getY() - b.getY();
	coords[2] = a.getZ() - b.getZ();
   
	Atom c = new AtomImpl();
	c.setCoords(coords);
	return c ;
    }

    
    /** Vector product */
    public static Atom vectorProduct(Atom a , Atom b){
	double[] coords = new double[3];

	coords[0] = a.getY() * b.getZ() - a.getZ() * b.getY();
	coords[1] = a.getZ() * b.getX() - a.getX() * b.getZ();
	coords[2] = a.getX() * b.getY() - a.getY() * b.getX();

	Atom c = new AtomImpl();
	c.setCoords(coords);
	return c ;
	
    }

    /** skalar product
     */
    public static double skalarProduct(Atom a, Atom b){
	double skalar ;
	skalar = a.getX() * b.getX() + a.getY()* b.getY() + a.getZ() * b.getZ();
	return skalar ;
    }


    /** amount
     */
    public static double amount(Atom a){
	return Math.sqrt(skalarProduct(a,a));
    }

    /** angle
     */
    public static double angle(Atom a, Atom b){

	double skalar;
	double angle;
    
	skalar = skalarProduct(a,b);
	
	angle = skalar/( amount(a) * amount (b) );
	angle = Math.acos(angle);
	angle = angle * RADIAN ; 
    
	return angle;
    }

    /** return the unit vector of vector a */
    public static Atom unitVector(Atom a) {
	double amount = amount(a) ;
	Atom U = a ;
	
	double[] coords = new double[3];

	coords[0] = a.getX() / amount ;
	coords[1] = a.getY() / amount ;
	coords[2] = a.getZ() / amount ;
	
	U.setCoords(coords);
	return U ;
	
    }
    
    /** torsion angle 
     * = angle between the normal vectors of the 
     * two plains a-b-c and b-c-d
     */
    
    public static double torsionAngle(Atom a, Atom b, Atom c, Atom d)
	throws StructureException
    {
	
	Atom ab = substract(a,b);
	Atom cb = substract(c,b);
	Atom bc = substract(b,c);
	Atom dc = substract(d,c);

	Atom abc = vectorProduct(ab,cb);	
	Atom bcd = vectorProduct(bc,dc);


	double angl = angle(abc,bcd) ;

	/* calc the sign: */
	Atom vecprod = vectorProduct(abc,bcd);	
	double val = skalarProduct(cb,vecprod);
	if (val<0.0) angl = -angl ;

	return angl;
    }

    /** phi angle
     */
    public static double getPhi(AminoAcid a, AminoAcid b)
	throws StructureException
    {

	if ( ! isConnected(a,b)){
	    throw new StructureException("can not calc Phi - AminoAcids are not connected!") ;
} 

	Atom a_C  = a.getC();
	Atom b_N  = b.getN();
	Atom b_CA = b.getCA();
	Atom b_C  = b.getC();
	
	double phi = torsionAngle(a_C,b_N,b_CA,b_C);
	return phi ;
    }

     /** psi angle
     */
    public static double getPsi(AminoAcid a, AminoAcid b)
	throws StructureException
    {
	if ( ! isConnected(a,b)) {
	    throw new StructureException("can not calc Psi - AminoAcids are not connected!") ;
	}

	Atom a_N   = a.getN();
	Atom a_CA  = a.getCA();
	Atom a_C   = a.getC();
	Atom b_N   = b.getN();

	double psi = torsionAngle(a_N,a_CA,a_C,b_N);
	return psi ;
	   
    }

    /** test if two amino acids are connected, i.e.
     * if the distance from C to N < 2,5 Angstrom
     */    
    public static boolean isConnected(AminoAcid a, AminoAcid b)
	throws StructureException
    {
	Atom C = a.getC();
	Atom N = b.getN();

	// one could also check if the CA atoms are < 4 A...
	double distance = getDistance(C,N);
	if ( distance < 2.5) { 
	    return true ;
	} else {
	    return false ;
	}
    }


    


}


