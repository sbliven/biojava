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

package org.biojava.directory;

import java.util.*;
import java.io.*;

import javax.naming.*;
import javax.naming.spi.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;

/**
 * JNDI context reflecting the /etc/bioinformatics style configuration
 *
 * @author Thomas Down
 */

class BioContext implements Context {
    private Map databaseRecords;
    private Map databases = new HashMap();

    private Hashtable env;
    private static final NameParser parser;

    static {
	final Properties syntax = new Properties();
	syntax.put("jndi.syntax.direction", "left_to_right");
	syntax.put("jndi.syntax.ignorecase", "false");
	syntax.put("jndi.syntax.separator", "/");

	parser = new NameParser() {
		public Name parse(String name) throws NamingException {
		    return new CompoundName(name, syntax);
		}
	    } ;
    }

    BioContext(Hashtable env) {
	if (env == null) {
	    this.env = new Hashtable();
	} else {
	    this.env = new Hashtable(env);
	}
    }

    private static class DBRecord {
	public String name;
	public String meta;
	public boolean update;
	public String locator;
    }

    protected void loadConf() {
	try {
	    Map conf = new HashMap();
	    File f = new File(System.getProperty("user.home") + "/.bioinformatics");
	    if (! f.exists()) {
		f = new File("/etc/bioinformatics");
	    }

	    if (f.exists()) {
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		
		while ((line = br.readLine()) != null) {
		    if (line.length() == 0 || line.startsWith("#")) {
			continue;
		    }

		    StringTokenizer toke = new StringTokenizer(line);
		    try {
			DBRecord record = new DBRecord();

			record.name = toke.nextToken();
			record.meta = toke.nextToken();
			record.update = toke.nextToken().equalsIgnoreCase("update");
			record.locator = toke.nextToken();

			if (! record.update) {
			    conf.put(record.name, record);
			}
		    } catch (Exception ex) {
			ex.printStackTrace();
		    }
		}
	    }
	    
	    databaseRecords = conf;
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    /**
     * Get the SequenceDBFactory for a given meta tag.
     */

    protected SequenceDBFactory getDBFactory(String metaTag)
        throws NoSuchElementException
    {
	String expectedClassName = "org.biojava.directory.seqdb_providers." + metaTag + ".Factory";
	try {
	    Class factoryClass = getClass().getClassLoader().loadClass(expectedClassName);
	    return (SequenceDBFactory) factoryClass.newInstance();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new NoSuchElementException("Couldn't find provider for metatag: " + metaTag);
	}
    }

    protected Set getIDs() {
	if (databaseRecords == null) {
	    loadConf();
	}
	return databaseRecords.keySet();
    }

    protected SequenceDB getDB(String id) 
        throws NamingException
    {
	if (databaseRecords == null) {
	    loadConf();
	}
	
	SequenceDB db = (SequenceDB) databases.get(id);
	if (db == null) {
	    DBRecord record = (DBRecord) databaseRecords.get(id);
	    if (record != null) {
		try {
		    SequenceDBFactory factory = getDBFactory(record.meta);
		    db = factory.getSequenceDB(id, record.locator);
		    databases.put(id, db);
		} catch (Exception ex) {
		    throw new NamingException("Error instantiating database: " + ex.toString());
		}
	    }
	}
	return db;
    }
    
    public Object lookup(String name)
        throws NamingException
    {
	return lookup(parser.parse(name));
    }

    public Object lookup(Name name)
        throws NamingException
    {
	if (name.isEmpty()) {
	    // Return a clone of this object
	    return new BioContext(env);
	}

	String id = name.get(0);
	try {
	    SequenceDB db = getDB(id);
	    if (db == null) {
		throw new NameNotFoundException("Couldn't lookup " + id);
	    }
	    Context result = new SeqDBContext(null, db);
	    if (name.size() > 1) {
		return result.lookup(name.getSuffix(1));
	    } else {
		return result;
	    }
	} catch (Exception ex) {
	    throw new NameNotFoundException("Couldn't lookup " + id);
	}
    }

    public Object lookupLink(String name)
        throws NamingException
    {
	return lookup(name);
    }

    public Object lookupLink(Name name)
        throws NamingException
    {
	return lookup(name);
    }

    public NamingEnumeration list(String name) 
        throws NamingException
    {
	return list(parser.parse(name));
    }

    public NamingEnumeration list(Name name)
        throws NamingException
    {
	if (name.isEmpty()) {
	    return new IDEnumeration();
	}

	Object target = lookup(name);
	if (target instanceof Context) {
	    try {
		return ((Context) target).list("");
	    } finally {
		((Context) target).close();
	    }
	}
	throw new NotContextException(name.toString() + " cannot be listed");
    }

    public NamingEnumeration listBindings(String name) 
        throws NamingException
    {
	return listBindings(parser.parse(name));
    }

    public NamingEnumeration listBindings(Name name)
        throws NamingException
    {
	if (name.isEmpty()) {
	    return new BindingEnumeration();
	}

	Object target = lookup(name);
	if (target instanceof Context) {
	    try {
		return ((Context) target).listBindings("");
	    } finally {
		((Context) target).close();
	    }
	}
	throw new NotContextException(name.toString() + " cannot be listed");
    }

    public void bind(String name, Object obj) throws NamingException {
	bind(parser.parse(name), obj);
    }

    public void bind(Name name, Object obj) throws NamingException {
	throw new OperationNotSupportedException();
    }

    public void rebind(String name, Object obj) throws NamingException {
	rebind(parser.parse(name), obj);
    }

    public void rebind(Name name, Object obj) throws NamingException {
	throw new OperationNotSupportedException();
    }

    public void unbind(String name) throws NamingException {
	unbind(parser.parse(name));
    }

    public void unbind(Name name) throws NamingException {
	throw new OperationNotSupportedException();
    }

    public void rename(String name1, String name2) throws NamingException {
	rename(parser.parse(name1), parser.parse(name2));
    }

    public void rename(Name name1, Name name2) throws NamingException {
	throw new OperationNotSupportedException();
    }

    public NameParser getNameParser(String name) {
	return parser;
    }

    public NameParser getNameParser(Name name) {
	return parser;
    }

    public Context createSubcontext(String s)
        throws NamingException
    {
	return createSubcontext(parser.parse(s));
    }

    public Context createSubcontext(Name name)
        throws NamingException
    {
	throw new OperationNotSupportedException();
    }

    public void destroySubcontext(String s)
        throws NamingException
    {
	destroySubcontext(parser.parse(s));
    }

    public void destroySubcontext(Name name)
        throws NamingException
    {
	throw new OperationNotSupportedException();
    }

    public String composeName(String name, String prefix)
        throws NamingException
    {
	return composeName(parser.parse(name), parser.parse(prefix)).toString();
    }

    public Name composeName(Name name, Name prefix)
        throws NamingException
    {
	Name result = (Name) prefix.clone();
	result.addAll(name);
	return result;
    }

    public Hashtable getEnvironment() {
	return new Hashtable(env);
    }

    public Object addToEnvironment(String propName, Object propVal)
        throws NamingException
    {
	return env.put(propName, propVal);
    }

    public Object removeFromEnvironment(String propName) 
        throws NamingException
    {
	return env.remove(propName);
    }

    public String getNameInNamespace() {
	return "";
    }

    public void close() {
    }

    private class IDEnumeration implements NamingEnumeration {
	private Iterator i;

	IDEnumeration() {
	    i = getIDs().iterator();
	}

	public boolean hasMoreElements() {
	    try {
		return hasMore();
	    } catch (NamingException e) {
		return false;
	    }
	}

	public boolean hasMore() throws NamingException {
	    return i.hasNext();
	}

	public Object next() throws NamingException {
	    String id = (String) i.next();
	    return new NameClassPair(id, Context.class.getName());
	}

	public Object nextElement() {
	    try {
		return next();
	    } catch (NamingException ex) {
		throw new NoSuchElementException(ex.toString());
	    }
	}

	public void close() {
	}
    }

    public class BindingEnumeration extends IDEnumeration {
	BindingEnumeration() {
	    super();
	}

	public Object next()
	    throws NamingException
	{
	    NameClassPair ncp = (NameClassPair) super.next();
	    try {
		String name = ncp.getName();
		return new Binding(name, 
				   Context.class.getName(),
				   new SeqDBContext(null, getDB(name)));
	    } catch (Exception ex) {
		throw new NamingException();
	    }
	}
    }
}
