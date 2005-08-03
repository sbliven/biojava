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
 * SimpleNCBITaxonName.java
 *
 * Created on August 1, 2005, 5:08 PM
 */

package org.biojavax.bio.taxa;

/**
 * Hibernate requirement - not for public use.
 * @author Richard Holland
 */
class SimpleNCBITaxonName implements Comparable {
    private String nameClass;
    private String name;
    protected SimpleNCBITaxonName() {}
    public SimpleNCBITaxonName(String nameClass, String name) {
        if (nameClass==null) throw new IllegalArgumentException("Name class cannot be null");
        if (name==null) throw new IllegalArgumentException("Name cannot be null");
        this.nameClass = nameClass;
        this.name = name; }
    public void setNameClass(String nameClass) { 
        if (nameClass==null) throw new IllegalArgumentException("Name class cannot be null");
        this.nameClass = nameClass; 
    }
    public String getNameClass() { return this.nameClass; }
    public void setName(String name) {       
        if (name==null) throw new IllegalArgumentException("Name cannot be null");
        this.name = name; 
    }
    public String getName() { return this.name; }
    public boolean equals(Object o) {
        if (o==this) return true;
        if (!(o instanceof SimpleNCBITaxonName)) return false;
        // Hibernate comparison - we haven't been populated yet
        if (this.nameClass==null) return false;
        // Normal comparison
        SimpleNCBITaxonName them = (SimpleNCBITaxonName) o;
        return them.getNameClass().equals(this.nameClass) &&
                them.getName().equals(this.name);
    }
    public int compareTo(Object o) {
        // Hibernate comparison - we haven't been populated yet
        if (this.nameClass==null) return -1;
        // Normal comparison
        SimpleNCBITaxonName them = (SimpleNCBITaxonName)o;
        if (!them.getNameClass().equals(this.nameClass)) return this.nameClass.compareTo(them.getNameClass());
        return this.name.compareTo(them.getName());
    }
    public int hashCode() {
        int code = 17;
        // Hibernate comparison - we haven't been populated yet
        if (this.nameClass==null) return code;
        // Normal comparison
        code = 31*code + this.name.hashCode();
        code = 31*code + this.nameClass.hashCode();
        return code;
    }
}
