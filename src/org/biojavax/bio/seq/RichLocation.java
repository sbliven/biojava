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
 * RichLocation.java
 *
 * Created on July 28, 2005, 5:29 PM
 */
package org.biojavax.bio.seq;
import org.biojava.bio.symbol.Location;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.CrossRef;
import org.biojavax.RichAnnotatable;
import org.biojavax.ontology.ComparableTerm;

/**
 * Holds enough info about locations to keep BioSQL happy if needs be.
 *
 * @author Richard Holland
 */
public interface RichLocation extends Location,RichAnnotatable,Comparable {
    
    public static final ChangeType NOTE = new ChangeType(
            "This location's notes have changed",
            "org.biojavax.bio.seq.RichLocation",
            "NOTE"
            );
    public static final ChangeType TERM = new ChangeType(
            "This location's term has changed",
            "org.biojavax.bio.seq.RichLocation",
            "TERM"
            );
    public static final ChangeType RANK = new ChangeType(
            "This location's rank has changed",
            "org.biojavax.bio.seq.RichLocation",
            "RANK"
            );
    
    /**
     * Retrieves the crossref associated with this location.
     * @return the crossref.
     */
    public CrossRef getCrossRef();
    
    /**
     * Retrieves the term associated with this location.
     * @return the term.
     */
    public ComparableTerm getTerm();
    
    /**
     * Sets the term for this location.
     * @param term the term this location should adopt.
     * @throws ChangeVetoException in case of error.
     */
    public void setTerm(ComparableTerm term) throws ChangeVetoException;
    
    /**
     * Retrieves the strand associated with this location.
     * @return the strand.
     */
    public Strand getStrand();
    
    /**
     * Retrieves the rank associated with this location.
     * @return the rank.
     */
    public int getRank();
    
    /**
     * Sets the rank for this location.
     * @param rank the rank this location should adopt.
     * @throws ChangeVetoException in case of error.
     */
    public void setRank(int rank) throws ChangeVetoException;
    
    public Position getMinPosition();
    
    public Position getMaxPosition();
    
    public void setPositionResolver(PositionResolver p);
    
    public static final RichLocation EMPTY_LOCATION = new EmptyRichLocation();
    
    public static class Strand implements Comparable {
        public static final Strand POSITIVE_STRAND = new Strand("+",1);
        public static final Strand NEGATIVE_STRAND = new Strand("-",-1);
        public static final Strand UNKNOWN_STRAND = new Strand("?",0);
        public static Strand forValue(int value) {
            switch (value) {
                case 1: return POSITIVE_STRAND;
                case 0: return UNKNOWN_STRAND;
                case -1: return NEGATIVE_STRAND;
                default: throw new IllegalArgumentException("Unknown strand type: "+value);
            }
        }
        public static Strand forName(String name) {
            if (name.equals("+")) return POSITIVE_STRAND;
            else if (name.equals("?")) return UNKNOWN_STRAND;
            else if (name.equals("-")) return NEGATIVE_STRAND;
            else throw new IllegalArgumentException("Unknown strand type: "+name);
        }
        private String name;
        private int value;
        public Strand(String name,int value) { this.name = name; this.value = value; }
        public int intValue() { return this.value; }
        public String toString() { return this.name; }
        public int hashCode() {
            int code = 17;
            code = 31*code + this.name.hashCode();
            code = 31*code + this.value;
            return code;
        }
        public boolean equals(Object o) {
            if (!(o instanceof Strand)) return false;
            if (o==this) return true;
            Strand them = (Strand)o;
            if (!them.toString().equals(this.name)) return false;
            if (them.intValue()!=this.value) return false;
            return true;
        }
        public int compareTo(Object o) {
            Strand fo = (Strand) o;
            if (!this.name.equals(fo.toString())) return this.name.compareTo(fo.toString());
            return this.value-fo.intValue();
        }
    }
}
