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
 * @author andreas
 *
 * simple implementation of an Atom
 
 */
public interface Atom {
    
    /** trimmed version of atom name, e.g. "CA" */
    public void   setName(String s);
    public String getName();
    
    /** full name of atom e.g. " CA " */
    public void   setFullName(String s) ;
    public String getFullName();

    /** PDB atom number */
    public void setPDBserial(int i) ;
    public int  getPDBserial() ;

    /** the coordinates */    
    public void    setCoords(double[] c);
    public double[] getCoords() ;
    
    public double getX() ;
    public double getY() ;
    public double getZ() ;

    /** get set alternate Location */
    public void setAltLoc(Character c);
    public Character getAltLoc();

    /** store the whole line */
    public void   setPDBline(String s) ;
    public String getPDBline() ;

    /** set occupancy */
    public void setOccupancy(double occupancy) ;
    /** get occupancy */
    public double getOccupancy();

    public void   setTempFactor(double temp) ;
    public double getTempFactor() ;
}
