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
 * Created on 28.04.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.bio.structure;

/**
 * Implementation of an Atom of a PDB file.
 * currently the coordinates of an atom are represented by a doubl[3] array.
 */
public class AtomImpl implements Atom {

    String name     ;
    String fullName ;
    double[] coords ;
    String pdbline  ;
    int pdbserial   ;

    public AtomImpl () {
	name     = null        ;
	fullName = null        ;
	coords   = new double[3];
	pdbline  = ""          ;  

    }

    /** trimmed version of atom name, e.g. "CA" */
    public void   setName(String s) { name = s ;}
    public String getName()         { return name ;}
    
    /** set full name of atom e.g. " CA " */
    public void   setFullName( String s ) { fullName =s ; }
    /** get full name of atom e.g. " CA " */
    public String getFullName()           { return fullName; }

    /** set PDB atom number */
    public void setPDBserial(int i) { pdbserial = i    ; }
    /** get PDB atom number */
    public int  getPDBserial()      { return pdbserial ; }

    /** the coordinates */    
    public void     setCoords( double[] c ) {
	coords = c; 

    } 
    /** get the coordinates as a double[3] array */
    public double[] getCoords()            { return coords ; }

    /** get the X coordinate */
    public double getX() { return coords[0]; }

    /** get the Y coordinate */
    public double getY() { return coords[1]; }

    /** get the Z coordinate */
    public double getZ() { return coords[2]; }
    
    /** store the whole line */
    public void   setPDBline(String s) { pdbline = s;}

    /** get the whole line */
    public String getPDBline() { return pdbline ;}

    /** string representation */
    public String toString() {
	String str = name +" "+ pdbserial + " " + coords[0] + " " + coords[1] + " " + coords[2];
	return str ;
    }
}
