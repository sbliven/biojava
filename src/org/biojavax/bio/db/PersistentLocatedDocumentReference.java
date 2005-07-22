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

 * PersistentLocatedDocumentReference.java

 *

 * Created on July 12, 2005, 8:10 AM

 */



package org.biojavax.bio.db;



import java.sql.SQLException;

import org.biojavax.DocumentReference;

import org.biojavax.LocatedDocumentReference;

import org.biojavax.SimpleLocatedDocumentReference;



/**

 * Represents a documentary reference, the bioentryreference table in BioSQL.

 * @author Richard Holland

 */

public abstract class PersistentLocatedDocumentReference extends SimpleLocatedDocumentReference implements Persistent {

        

    private int status;

    private int uid;

    private PersistentBioDB db;

    

    protected PersistentLocatedDocumentReference(PersistentBioDB db, DocumentReference dr, int start, int end) {

        super((PersistentDocumentReference)db.convert(dr),start,end);

        this.status = Persistent.UNMODIFIED;

        this.uid = Persistent.UID_UNKNOWN;

        this.db = db;

    }

    

    protected PersistentLocatedDocumentReference(PersistentBioDB db, LocatedDocumentReference dr) {

        this(db, dr.getDocumentReference(), dr.getStart(), dr.getEnd());

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

    

}



