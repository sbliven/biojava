package org.biojava.directory;

import java.util.*;
import java.io.*;

import javax.naming.*;
import javax.naming.spi.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;

/**
 * JNDI context reflecting a SequenceDB
 *
 * @author Thomas Down
 */

class SeqDBContext implements Context {
    private SequenceDB seqDB;
    private Hashtable env;
    private static final NameParser parser;

    static {
	final Properties syntax = new Properties();
	syntax.put("jndi.syntax.direction", "flat");
	syntax.put("jndi.syntax.ignorecase", "false");

	parser = new NameParser() {
		public Name parse(String name) throws NamingException {
		    return new CompoundName(name, syntax);
		}
	    } ;
    }

    SeqDBContext(Hashtable env, SequenceDB db) {
	if (env != null) {
	    this.env = new Hashtable(env);
	} else {
	    this.env = new Hashtable();
	}
	this.seqDB = db;
    }

    protected SequenceDB getSeqDB() 
        throws NamingException
    {
	return seqDB;
    }
    
    protected String getMyComponents(Name name) throws NamingException {
	if (name instanceof CompositeName) {
	    if (name.size() > 1) {
		throw new InvalidNameException(name.toString() + " has unhandlable components");
	    }
	    return name.get(0);
	} else {
	    return name.toString();
	}
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
	    return new SeqDBContext(env, seqDB);
	}

	String id = getMyComponents(name);
	try {
	    return getSeqDB().getSequence(id);
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
	    return new IDEnumeration(getSeqDB());
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
	    return new BindingEnumeration(getSeqDB());
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

	IDEnumeration(SequenceDB seqDB) {
	    i = seqDB.ids().iterator();
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
	    return new NameClassPair(id, Sequence.class.getName());
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
	private SequenceDB seqDB;

	BindingEnumeration(SequenceDB seqDB) {
	    super(seqDB);
	    this.seqDB = seqDB;
	}

	public Object next()
	    throws NamingException
	{
	    NameClassPair ncp = (NameClassPair) super.next();
	    try {
		String name = ncp.getName();
		return new Binding(name, 
				   Sequence.class.getName(),
				   seqDB.getSequence(name));
	    } catch (Exception ex) {
		throw new NamingException();
	    }
	}
    }
}
