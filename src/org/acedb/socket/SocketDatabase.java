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

package org.acedb.socket;

import java.util.*;
import java.net.*;
import java.io.*;
import java.lang.ref.*;

import org.acedb.*;
import org.acedb.staticobj.*;

/**
 * @author Thomas Down
 */

class SocketDatabase implements Database {
    private List socks;

    private URL dbURL;
    private String host;
    private int port;
    private String user;
    private String passwd;

    private AcePerlParser parser;

    private Map cache;

    {
	socks = new LinkedList();
	cache = new HashMap();
	parser = new AcePerlParser(this);
    }

    public SocketDatabase(URL dbURL) throws AceException {
	this.dbURL = dbURL;
	host = dbURL.getHost();
	port = dbURL.getPort();
	user = "anonymous";
	passwd = "";
	
	// check we can connect...
	AceSocket as = takeSocket();
	putSocket(as);
    }

    public SocketDatabase(URL dbURL, String user, String passwd) 
	throws AceException 
    {
	this.dbURL = dbURL;
	host = dbURL.getHost();
	port = dbURL.getPort();
	this.user = user;
	this.passwd = passwd;
	
	// check we can connect...
	AceSocket as = takeSocket();
	putSocket(as);
    }


    synchronized AceSocket takeSocket() throws AceException {
      AceSocket as = null;
      if (socks.size() > 0)
	      as = (AceSocket) socks.remove(0);
      if(as == null)
        as = new AceSocket(host, port, user, passwd);
      return as;
    }

    synchronized void putSocket(AceSocket s) {
      if(s == null)
        throw new AceError("Attemted to add a null socket");
	socks.add(s);
    }

    public Connection getConnection() throws AceException {
	return new ConnectionProxy();
    }

    private synchronized void cacheObject(String file, AceNode o) {
	cache.put(file, new SoftReference(o));
    }

    private synchronized AceNode getFromCache(String file) {
	SoftReference ref = (SoftReference) cache.get(file);
	if (ref != null) {
	    AceNode o = (AceNode) ref.get();
	    if (o != null)
		return o;
	    else
		cache.remove(file);
	}
	return null;
    }

    public String rawQuery(String queryString) throws AceException {
	String result = null;
	AceSocket sock = null;

	try {
	    sock = takeSocket();
	    result = sock.transact(queryString);
	} finally {
	    if (sock != null)
		putSocket(sock);
	}
	return result;
    }

    public List rawQuery(List queries) throws AceException {
	List results = new ArrayList();
	AceSocket sock = null;

	try {
	    sock = takeSocket();
	    for (Iterator i = queries.iterator(); i.hasNext(); )
		results.add(sock.transact((String) i.next()));
	} finally {
	    if (sock != null)
		putSocket(sock);
	}
	return results;
    }

    public AceSet select(AceType.ClassType clazz, String namePattern)
                throws AceException
    {
	AceSocket sock = null;
	try {
	    sock = takeSocket();
	    String result = sock.transact("find " + clazz.getName() + " " +
					  namePattern);
	    int mpos = result.indexOf("// Found ");
	    if (mpos < 0)
		return null; // FIXME?
	    int numFound = Integer.parseInt(new StringTokenizer(
                               result.substring(mpos + 9)).nextToken());
	    // System.out.println("found "+numFound);

	    result = sock.transact("list -j");
	    StringTokenizer listToke = new StringTokenizer(result, "\r\n");
	    List nameList = new ArrayList();
	    while (listToke.hasMoreTokens()) {
		String l = listToke.nextToken();
		if (l.startsWith("?")) {
		    StringTokenizer ltoke = new StringTokenizer(l, "?");
		    if (ltoke.countTokens() < 2) 
			continue;
		    ltoke.nextToken();
		    nameList.add(ltoke.nextToken());
		}
	    }

	    // System.out.println("Actually got :" + nameList.size());
	    
	    return new SocketResultSet(this, clazz, nameList);
	} finally {
	    if (sock != null)
		putSocket(sock);
	}
    }

    public URL toURL() {
	return dbURL;
    }

    public AceObject getObject(AceType.ClassType clazz, String name) 
            throws AceException
    {
	String cacheName = clazz.getName() + ":" + name;
	AceObject o = (AceObject) getFromCache(cacheName);
	if (o != null)
	    return o;
	// get object

	AceSocket sock = null;
	try {
	    sock = takeSocket();
	    // System.out.println("Class " + clazz);
	    // System.out.println("Socket " + sock);
	    String result = sock.transact("find " + clazz.getName() + " " +
					  name);
	    if (result.indexOf("// Found 1 object") < 0) {
		throw new AceException("Couldn't get object "+cacheName);
	    }

	    String obj = sock.transact("show -p");
	    o = parser.parseObject(obj);
	} catch (IOException ex) {
	    throw new AceException(ex);
	} finally {
      if(sock != null)
  	    putSocket(sock);
	}

	cacheObject(cacheName, o);
	return o;
    }

    class ConnectionProxy implements Connection {
	private AceSocket sock;

	ConnectionProxy() throws AceException {
	    sock = takeSocket();
	}

	public String transact(String cmd) throws AceException {
	    return sock.transact(cmd);
	}

	public void dispose() throws AceException {
	    putSocket(sock);
	    sock = null;
	}

	public void finalize() throws IOException {
	    putSocket(sock);
	    sock = null;
	}
    }

    public static void printRes(AceNode n, String prefix) {
	System.out.println(prefix + "("+n.getType().getName()+") " + n.getName()  + " [" + n.toURL().toString() + "]");
	for (Iterator i = n.iterator(); i.hasNext(); ) {
	    AceNode nn = (AceNode) i.next();
	    printRes(nn, prefix + "    ");
	}
    }
}
