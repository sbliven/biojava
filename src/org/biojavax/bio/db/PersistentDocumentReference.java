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

 * PersistentDocumentReference.java

 *

 * Created on June 15, 2005, 5:56 PM

 */



package org.biojavax.bio.db;

import java.sql.SQLException;

import org.biojava.bio.BioError;

import org.biojava.utils.ChangeVetoException;

import org.biojavax.CrossRef;

import org.biojavax.DocumentReference;

import org.biojavax.SimpleDocumentReference;







/**

 * A basic DocumentReference implementation.

 *

 * Equality is having a unique author and location.

 *

 * @author Richard Holland

 * @author Mark Schreiber

 */

public abstract class PersistentDocumentReference extends SimpleDocumentReference implements Persistent {

    

    private int status;

    private int uid;

    private PersistentBioDB db;

    

    protected PersistentDocumentReference(PersistentBioDB db, String authors, String location) {

        super(authors, location);

        this.status = Persistent.UNMODIFIED;

        this.uid = Persistent.UID_UNKNOWN;

        this.db = db;

    }

    

    protected PersistentDocumentReference(PersistentBioDB db, DocumentReference dr) {

        this(db, dr.getAuthors(), dr.getLocation());

        try {

            this.setTitle(dr.getTitle());

            this.setCRC(dr.getCRC());

            this.setCrossref((PersistentCrossRef)db.convert(dr.getCrossref()));

        } catch (ChangeVetoException e) {

            throw new BioError("Whoops! Parent class does not understand its own data!");

        }

    }

    

    public PersistentBioDB getDB() {

        return this.db;

    }

    

    public int getStatus() {

        return this.status;

    }

    

    public int getUid() {

        return this.uid;

    }

    

    public void setStatus(int status) throws IllegalArgumentException {

        if (status!=Persistent.UNMODIFIED && status!=Persistent.MODIFIED && status!=Persistent.DELETED)

            throw new IllegalArgumentException("Invalid status code");

        this.status = status;

    }

    

    public void setUid(int uid) {

        this.uid = uid;

    }

    

    public abstract Persistent load(Object[] vars) throws Exception;

    

    public abstract boolean remove(Object[] vars) throws Exception;

    

    public abstract Persistent store(Object[] vars) throws Exception;

    

    public void setTitle(String title) throws ChangeVetoException {

        super.setTitle(title);

        this.status = Persistent.MODIFIED;

    }

    

    public void setCRC(String CRC) throws ChangeVetoException {

        super.setCRC(CRC);

        this.status = Persistent.MODIFIED;

    }

    

    public void setCrossref(CrossRef crossref) throws ChangeVetoException {

        PersistentCrossRef pc = (PersistentCrossRef)this.db.convert(crossref);

        super.setCrossref(pc);

        this.status = Persistent.MODIFIED;

    }

}

