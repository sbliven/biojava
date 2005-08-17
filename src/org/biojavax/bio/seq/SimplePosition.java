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

/*
 * Position.java
 *
 * Created on July 28, 2005, 5:29 PM
 */
package org.biojavax.bio.seq;


/**
 * Holds enough info about positions to keep BioSQL happy if needs be.
 *
 * @author Richard Holland
 */
public class SimplePosition implements Position {
    private boolean fs;
    private boolean fe;
    private int s;
    private int e;
    private String t;
    
    public SimplePosition(boolean fs, boolean fe, int p) {
        this(fs,fe,p,p,null);
    }
    public SimplePosition(boolean fs, boolean fe, int s, int e, String t) {
        this.fs = fs;
        this.fe = fe;
        this.s = s;
        this.e = e;
        this.t = t;
   }
    //Hibernate only - futureproofing
    protected SimplePosition() {}
       
    public boolean hasFuzzyStart() { return this.fs; }
    public boolean hasFuzzyEnd() { return this.fe; }
    
    public int getStart() { return this.s; }
    public int getEnd()  { return this.e; }
    
    public String getType() { return this.t; }
    
    // Hibernate requirement - not for public use - futureproofing
    private void setFuzzyStart(boolean fs) { this.fs = fs; }
    // Hibernate requirement - not for public use - futureproofing
    private void setFuzzyEnd(boolean fe) { this.fe = fe; }
    // Hibernate requirement - not for public use - futureproofing
    private void getStart(int s) { this.s = s; }
    // Hibernate requirement - not for public use - futureproofing
    private void getEnd(int e) { this.e = e; }
    // Hibernate requirement - not for public use - futureproofing
    private void getType(String t) { this.t = t; }
    
    public Position translate(int distance) {
        return new SimplePosition(this.fs,this.fe,this.s+distance,this.e+distance,this.t);
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof Position)) return false;
        if (o==this) return true;
        Position them = (Position)o;
        if (this.hasFuzzyStart() != them.hasFuzzyStart()) return false;
        if (this.hasFuzzyEnd() != them.hasFuzzyEnd()) return false;
        if (this.getStart()!=them.getStart()) return false;
        if (this.getEnd()!=them.getEnd()) return false;
        if (this.getType()!=null || them.getType()!=null) {
            if (this.getType()!=null && them.getType()!=null) {
                if (!this.getType().equals(them.getType())) return false;
            } else return false;
        }
        return true;
    }
    
    // Hibernate requirement - not for public use - futureproofing
    private Long id;
    
    // Hibernate requirement - not for public use - futureproofing
    private Long getId() { return this.id; }
    
    // Hibernate requirement - not for public use - futureproofing
    private void setId(Long id) { this.id = id; }
    
}
