package org.biojava.bio.structure;

import java.lang.reflect.Method;
import java.util.Date;


/** A class that contains PDB Header information.
 * 
 * @author Andreas Prlic
 * @since 1.6
 *
 */
public class PDBHeader {
    String method;
    String title;
    String description;
    String idCode;
    String classification;
    Date depDate;
    Date modDate;
    String technique;
    float resolution;

    private Long id;
    
    public PDBHeader(){
    	
    	depDate = new Date(0);
    	modDate = new Date(0);
    	
    }

    /** String representation
     * 
     */
    public String toString(){
        StringBuffer buf = new StringBuffer();

        try {

            Class c = Class.forName("org.biojava.bio.structure.PDBHeader");
            Method[] methods  = c.getMethods();

            for (int i = 0; i < methods.length; i++) {
                Method m = methods[i];     

                String name = m.getName();

                if ( name.substring(0,3).equals("get")) {                   
                    if (name.equals("getClass"))
                        continue;
                    Object o  = m.invoke(this, new Object[]{});
                    if ( o != null){
                        buf.append(name.substring(3,name.length()));
                        buf.append(": " + o + " ");
                        //if ( o instanceof Date) {
                        //    buf.append(": " + FlatFileInstallation.dateFormat.format(o) + " ");     
                        //} else  {
                        //    buf.append(": " + o + " ");
                        //}
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return buf.toString();
    }
    
    /** Get the ID used by Hibernate.
     * 
     * @return the ID used by Hibernate
     * @see #setId(Long)
     */
    public Long getId() {
        return id;
    }

    /** Set the ID used by Hibernate.
     * 
     * @param id the id assigned by Hibernate
     * @see #getId()
     * 
     */ 
    private void setId(Long id) {
        this.id = id;
    }

    /** Compare two PDBHeader objects
     * 
     * @param other a PDBHeader object to compare this one to.
     * @return true if they are equal or false if they are not.
     */
    public boolean equals(PDBHeader other){
        try {

            Class c = Class.forName("org.biojava.bio.structure.PDBHeader");
            Method[] methods  = c.getMethods();

            for (int i = 0; i < methods.length; i++) {
                Method m = methods[i];     
                String name = m.getName();

                if ( name.substring(0,3).equals("get")) {                   
                    if (name.equals("getClass"))
                        continue;
                    Object a  = m.invoke(this,  new Object[]{});
                    Object b  = m.invoke(other, new Object[]{});
                    if ( a == null ){
                        if ( b == null ){
                            continue;
                        } else { 
                            System.out.println(name + " a is null, where other is " + b);
                            return false; 
                        }
                    }
                    if ( b == null) {
                        System.out.println(name + " other is null, where a is " + a);
                        return false;
                    }
                    if (! (a.equals(b))){
                        System.out.println("mismatch with " + name + " >" + a + "< >" + b + "<");
                        return false;
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /** The PDB code for this protein structure.
     * 
     * @return the PDB identifier
     * @see #setIdCode(String)
     */
    public String getIdCode() {
        return idCode;
    }
    /** The PDB code for this protein structure.
     * 
     * @param idCode the PDB identifier
     * @see #getIdCode()
     * 
     */ 
    public void setIdCode(String idCode) {
        this.idCode = idCode;
    }

    
    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public Date getDepDate() {
        return depDate;
    }

    public void setDepDate(Date depDate) {
        this.depDate = depDate;
    }

    public String getTechnique() {
        return technique;
    }

    public void setTechnique(String technique) {
        this.technique = technique;
    }

    public float getResolution() {
        return resolution;
    }

    public void setResolution(float resolution) {
        this.resolution = resolution;
    }

    public Date getModDate() {
        return modDate;
    }

    public void setModDate(Date modDate) {
        this.modDate = modDate;
    }

    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

}
