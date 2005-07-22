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

 * PersistentNamespace.java

 *

 * Created on June 15, 2005, 6:04 PM

 */



package org.biojavax.bio.db;

import java.net.URI;

import java.sql.SQLException;

import org.biojava.bio.BioError;

import org.biojava.utils.ChangeVetoException;

import org.biojavax.Namespace;

import org.biojavax.SimpleNamespace;





/**

 * A basic Namespace implemenation.

 *

 * Equality is based on the name of the namespace.

 *

 * @author Richard Holland

 * @author Mark Schreiber

 */

public abstract class PersistentNamespace extends SimpleNamespace implements Persistent {

    

    private int status;

    private int uid;

    private PersistentBioDB db;

    

    protected PersistentNamespace(PersistentBioDB db, String name) {

        super(name);

        this.status = Persistent.UNMODIFIED;

        this.uid = Persistent.UID_UNKNOWN;

        this.db = db;

    }

    

    protected PersistentNamespace(PersistentBioDB db, Namespace ns) {

        this(db, ns.getName());

        try {

            this.setAcronym(ns.getAcronym());

            this.setAuthority(ns.getAuthority());

            this.setDescription(ns.getDescription());

            this.setURI(ns.getURI());

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



    public void setDescription(String description) throws ChangeVetoException {

        super.setDescription(description);

        this.status = Persistent.MODIFIED;

    }



    public void setAuthority(String authority) throws ChangeVetoException {

        super.setAuthority(authority);

        this.status = Persistent.MODIFIED;

    }



    public void setAcronym(String acronym) throws ChangeVetoException {

        super.setAcronym(acronym);

        this.status = Persistent.MODIFIED;

    }



    public void setURI(URI URI) throws ChangeVetoException {

        super.setURI(URI);

        this.status = Persistent.MODIFIED;

    }

    

}

