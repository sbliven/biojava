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
class SimpleNCBITaxonName {
    private String nameClass;
    private String name;
    private NCBITaxon parent;
    protected SimpleNCBITaxonName() {}
    public SimpleNCBITaxonName(NCBITaxon parent, String nameClass, String name) {
        this.parent = parent;
        this.nameClass = nameClass;
        this.name = name; }
    public void setNameClass(String nameClass) { this.nameClass = nameClass; }
    public String getNameClass() { return this.nameClass; }
    public void setName(String name) { this.name = name; }
    public String getName() { return this.name; }
    public void setParent(NCBITaxon parent) { this.parent = parent; }
    public NCBITaxon getParent() { return this.parent; }
    public boolean equals(Object o) {
        if (o==this) return true;
        if (!(o instanceof SimpleNCBITaxonName)) return false;
        SimpleNCBITaxonName them = (SimpleNCBITaxonName) o;
        return them.getNameClass().equals(this.nameClass) &&
                them.getName().equals(this.name) &&
                them.getParent().equals(this.parent);
    }
}
