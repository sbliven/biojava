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
import org.biojava.bio.structure.jama.Matrix;



/** utility operations on Atoms, AminoAcids, etc. 
 * <p>
 * Currently the
 * coordinates of an Atom are stored as an array of size 3
 * (double[3]). It would be more powerful to use Point3D from
 * javax.vecmath.  but unfortunately this is not a part of standard
 * java installations, since it comes with java3d . So to keep things
 * simple at the moment biojava does not depend on java3d.  
 * @author Andreas Prlic
 * @since 1.4
 * @version %I% %G%
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
     * calculate distance between two atoms.
     *
     * @param a  an Atom object
     * @param b  an Atom object
     * @return a double
     * @throws StructureException ...
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
    
    /** add two atoms ( a + b).
     *
     * @param a  an Atom object
     * @param b  an Atom object
     * @return an Atom object
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
    
    /** substract two atoms ( a - b).
     *
     * @param a  an Atom object
     * @param b  an Atom object
     * @return an Atom object
     * @throws StructureException ...
     
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
    
    
    /** Vector product .
     *
     * @param a  an Atom object
     * @param b  an Atom object
     * @return an Atom object
     */
    public static Atom vectorProduct(Atom a , Atom b){
        double[] coords = new double[3];
        
        coords[0] = a.getY() * b.getZ() - a.getZ() * b.getY();
        coords[1] = a.getZ() * b.getX() - a.getX() * b.getZ();
        coords[2] = a.getX() * b.getY() - a.getY() * b.getX();
        
        Atom c = new AtomImpl();
        c.setCoords(coords);
        return c ;
        
    }
    
    /** skalar product.
     *
     * @param a  an Atom object
     * @param b  an Atom object
     * @return a double
     */
    public static double skalarProduct(Atom a, Atom b){
        double skalar ;
        skalar = a.getX() * b.getX() + a.getY()* b.getY() + a.getZ() * b.getZ();
        return skalar ;
    }
    
    
    /** amount.
     *
     * @param a  an Atom object
     * @return a double
     */
    public static double amount(Atom a){
        return Math.sqrt(skalarProduct(a,a));
    }
    
    /** angle.
     *
     * @param a  an Atom object
     * @param b  an Atom object
     * @return a double
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
    
    /** return the unit vector of vector a .
     *
     * @param a  an Atom object
     * @return an Atom object
     */
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
     * two plains a-b-c and b-c-d.
     *
     * @param a  an Atom object
     * @param b  an Atom object
     * @param c  an Atom object
     * @param d  an Atom object
     * @return a double
     * @throws StructureException ...
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
    
    /** phi angle.
     *
     * @param a  an AminoAcid object
     * @param b  an AminoAcid object
     * @return a double
     * @throws StructureException ...
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
    
    /** psi angle.
     *
     * @param a  an AminoAcid object
     * @param b  an AminoAcid object
     * @return a double
     * @throws StructureException ...
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
     * if the distance from C to N < 2,5 Angstrom.
     *
     * @param a  an AminoAcid object
     * @param b  an AminoAcid object
     * @return true if ...
     * @throws StructureException ...
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
    
    
    
    /** rotate a single atom aroud a rotation matrix.
     * matrix must be a 3x3 matrix.
     * @param atom
     * @param m
     */
    public static void rotate(Atom atom, double[][] m){

        double x = atom.getX();
        double y = atom.getY() ;
        double z = atom.getZ();
        
        double nx = m[0][0] * x + m[0][1] * y +  m[0][2] * z ;
        double ny = m[1][0] * x + m[1][1] * y +  m[1][2] * z ;
        double nz = m[2][0] * x + m[2][1] * y +  m[2][2] * z ;
        
        double[] coords = new double[3] ;
        coords[0] = nx ;
        coords[1] = ny ;
        coords[2] = nz ;
        
        atom.setCoords(coords);
    }
    
    /** rotate a structure .
     *
     * @param structure  a Structure object
     * @param m          an array of double arrays
     * @throws StructureException ...
     */
    public static void rotate(Structure structure, double[][] rotationmatrix)
    throws StructureException
    {
        double[][]m = rotationmatrix;
        if ( m.length != 3 ) {
            throw new StructureException ("matrix does not have size 3x3 !");
        }
        AtomIterator iter = new AtomIterator(structure) ;
        while (iter.hasNext()) {
            Atom atom = (Atom) iter.next() ;
            Calc.rotate(atom,rotationmatrix);
        }
    }
    
    /** rotate a structure .
    *
    * @param structure  a Structure object
    * @param m          an array of double arrays
    * @throws StructureException ...
    */
   public static void rotate(Group group, double[][] rotationmatrix)
   throws StructureException
   {
       double[][]m = rotationmatrix;
       if ( m.length != 3 ) {
           throw new StructureException ("matrix does not have size 3x3 !");
       }
       AtomIterator iter = new AtomIterator(group) ;
       while (iter.hasNext()) {
           Atom atom = null ;
           
           atom = (Atom) iter.next() ;
           rotate(atom,rotationmatrix);
         
       }
   }
    
   /** rotate an atom around a Matrix object
    * 
    * @param atom
    * @param m
    */
   public static void rotate(Atom atom, Matrix m){

       double x = atom.getX();
       double y = atom.getY() ;
       double z = atom.getZ();
       double[][] ad = new double[][]{{x,y,z}};
       
       Matrix am = new Matrix(ad);
       Matrix na = am.times(m);
       
       double[] coords = new double[3] ;
       coords[0] = na.get(0,0);
       coords[1] = na.get(0,1);
       coords[2] = na.get(0,2);
       atom.setCoords(coords);
   
   }
   
   /** rotate a group object
    * 
    * @param structure
    * @param m
    */
   public static void rotate(Group group, Matrix m){
       
       AtomIterator iter = new AtomIterator(group) ;
     
       while (iter.hasNext()) {
           Atom atom = (Atom) iter.next() ;
           rotate(atom,m);
           
       }
      
   }
   
    /** rotate a structure object
     * 
     * @param structure
     * @param m
     */
    public static void rotate(Structure structure, Matrix m){
        
        AtomIterator iter = new AtomIterator(structure) ;
      
        while (iter.hasNext()) {
            Atom atom = (Atom) iter.next() ;
            rotate(atom,m);
            
        }
       
    }
    
    /** calculate structure + Matrix coodinates ... 
     * 
     * @param s
     * @param matrix
     */
    public static void plus(Structure s, Matrix matrix){
        AtomIterator iter = new AtomIterator(s) ;
        Atom oldAtom = null;
        Atom rotOldAtom = null;
        while (iter.hasNext()) {
            Atom atom = null ;
            
            atom = (Atom) iter.next() ;
            try {
            if ( oldAtom != null){
                //System.out.println("before " +getDistance(oldAtom,atom));
            }
            } catch (Exception e){
                e.printStackTrace();
            }
            oldAtom = (Atom)atom.clone();
            
            double x = atom.getX();
            double y = atom.getY() ;
            double z = atom.getZ();
            double[][] ad = new double[][]{{x,y,z}};
            
            Matrix am = new Matrix(ad);
            Matrix na = am.plus(matrix);
            
            double[] coords = new double[3] ;
            coords[0] = na.get(0,0);
            coords[1] = na.get(0,1);
            coords[2] = na.get(0,2);
            atom.setCoords(coords);
            try {
                if ( rotOldAtom != null){
                    //System.out.println("after " + getDistance(rotOldAtom,atom));
                }
                } catch (Exception e){
                    e.printStackTrace();
                }
            rotOldAtom  = (Atom) atom.clone();
        }
        
    }
    
    
    
    /** shift a structure with a vector.
     *
     * @param structure  a Structure object
     * @param a          an Atom object representing a shift vector
     */
    public static void shift(Structure structure, Atom a ){
        
        AtomIterator iter = new AtomIterator(structure) ;
        while (iter.hasNext() ) {
            Atom atom = null ;
            
            atom = (Atom) iter.next()  ;	    
           
            Atom natom = add(atom,a);	   
            double x = natom.getX();
            double y = natom.getY() ;
            double z = natom.getZ();
            atom.setX(x);
            atom.setY(y);
            atom.setZ(z);
      
        }
    }
    
    /** shift a Group with a vector.
    *
    * @param structure  a Structure object
    * @param a          an Atom object representing a shift vector
    */
   public static void shift(Group group , Atom a ){
       
       AtomIterator iter = new AtomIterator(group) ;
       while (iter.hasNext() ) {
           Atom atom = null ;
           
           atom = (Atom) iter.next()  ;     
          
           Atom natom = add(atom,a);       
           double x = natom.getX();
           double y = natom.getY() ;
           double z = natom.getZ();
           atom.setX(x);
           atom.setY(y);
           atom.setZ(z);
           
       }
   }
    
    
    
    /** returns the center  of mass of the set of atoms
     * 
     */
    public static Atom getCentroid(Atom[] atomSet){
        
        double[] coords = new double[3];
        
        coords[0] = 0;
        coords[1] = 0;
        coords[2] = 0 ;
        
        for (int i =0 ; i < atomSet.length; i++){
            Atom a = atomSet[i];
            coords[0] += a.getX();
            coords[1] += a.getY();
            coords[2] += a.getZ();
        }
        
        int n = atomSet.length;
        coords[0] = coords[0] / n;
        coords[1] = coords[1] / n;
        coords[2] = coords[2] / n;
        
        Atom vec = new AtomImpl();
        vec.setCoords(coords);
        return vec;
        
    }
    
    public static Atom getCenterVector(Atom[] atomSet){
        Atom centroid = getCentroid(atomSet);
        
        double[] coords = new double[3];
        coords[0] = 0 - centroid.getX();
        coords[1] = 0 - centroid.getY();
        coords[2] = 0 - centroid.getZ();
        
        Atom shiftVec = new AtomImpl();
        shiftVec.setCoords(coords);
        return shiftVec;
        
    }
    
    /** center the atoms at the Centroid 
     * */
    public static Atom[] centerAtoms(Atom[] atomSet) throws StructureException {
       
        Atom shiftVector = getCenterVector(atomSet);
        
        Atom[] newAtoms = new AtomImpl[atomSet.length];
        
        for (int i =0 ; i < atomSet.length; i++){
            Atom a = atomSet[i];
            Atom n = add(a,shiftVector);
            newAtoms[i] = n ;
        }
        return newAtoms;
    }
    
    
    /** creates a virtual C-beta atom. this might be needed when working with GLY
     * 
     * thanks to Peter Lackner for a python template of this method.
     * @param amino
     * @return a "virtual" CB atom
     */
    public static Atom createVirtualCBAtom(AminoAcid amino) 
    throws StructureException{
        
        // define a standard ALA:
        // O is just for completeness.
        
        double[] ala_N  =  new double[] { -0.525,  1.373,  0.000};
        double[] ala_CA =  new double[] {  0.000,  0.000,  0.000};
        double[] ala_C  =  new double[] {  1.530,  0.000,  0.000};
        //double[] ala_O  =  new double[] {  2.152, -1.073,  0.000};
        double[] ala_CB =  new double[] { -0.523, -0.769, -1.215};
        
        Atom aN = new AtomImpl();
        aN.setCoords(ala_N);
        
        Atom aCA = new AtomImpl();
        aCA.setCoords(ala_CA);
        
        Atom aC = new AtomImpl();
        aC.setCoords(ala_C);
        
        Atom aCB = new AtomImpl();
        aCB.setCoords(ala_CB);
        
        
        Atom[] arr1 = new Atom[3];
        arr1[0] = aN;
        arr1[1] = aCA;
        arr1[2] = aC;
        
        Atom[] arr2 = new Atom[3];
        arr2[0] = amino.getN();
        arr2[1] = amino.getCA();
        arr2[2] = amino.getC();
        
        // ok now we got the two arrays, do a SVD:
        
        SVDSuperimposer svd = new SVDSuperimposer(arr2,arr1);
        
        Matrix rotMatrix = svd.getRotation();
        Atom tranMatrix = svd.getTranslation();
                    
        Calc.rotate(aCB,rotMatrix);

        Atom virtualCB = Calc.add(aCB,tranMatrix);
        virtualCB.setName("CB");
        virtualCB.setFullName(" CB ");
        
        return virtualCB;
    }    
}


