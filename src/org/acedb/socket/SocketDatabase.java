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

    private AceURL dbURL;
    private String host;
    private int port;
    private String user;
    private String passwd;

    private AcePerlParser parser;

    private Map cache;
    private AceSet allClassesSet = null;

    {
	socks = new LinkedList();
	cache = new HashMap();
	parser = new AcePerlParser(this);
    }

    public SocketDatabase(AceURL dbURL) 
	throws AceException 
    {
	this.dbURL = dbURL;
	host = dbURL.getHost();
	port = dbURL.getPort();
	this.user = dbURL.getUserInfo();
	this.passwd = dbURL.getAuthority();
	
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

    public AceSet fetch(AceURL url) throws AceException {
	AceSet resultSet = null;

	String file = url.getFile();

	file = (file == null ? "" : file);

	StringTokenizer toke = new StringTokenizer(file, "/");
	if (toke.countTokens() == 0)
	    return allClasses();
	if (toke.countTokens() == 1) {
	    String clazz = toke.nextToken();
	    resultSet = allClasses().retrieve(clazz);
	} else {
	    String clazz = toke.nextToken();
	    if(clazz == null) {
		throw new AceException("Couldn't extract class name from URL " + file);
	    }
	    String objname = toke.nextToken();
	    if(objname == null) {
		throw new AceException("Couldn't extract class name from URL " + file);
	    }
      
	    resultSet = getObject(clazz, objname);

	    while (toke.hasMoreTokens()) {
		String path = toke.nextToken();
		resultSet = resultSet.retrieve(path);
	    }
	}

	String query = url.getQuery();
	if (query != null && query.length() != 0) {
	    resultSet = resultSet.filter(query);
	}

	return resultSet;
    }

    private AceSet allClasses()
        throws AceException
    {
	AceSet res = null;
	while (res == null) {
	    try {
		res = _allClasses();
	    } catch (AceException ex) {
        ex.printStackTrace();
		if (! ex.isRecoverable())
		    throw ex;
	    }
	}
	return res;
    }

    private AceSet _allClasses() 
        throws AceException
    {
	if (allClassesSet == null) {
	    AceSocket sock = null;
	    try {
		sock = takeSocket();
		String result = sock.transact("classes");
		StaticAceSet set = new StaticAceSet(null, dbURL, null);
		for(StringTokenizer toke = new StringTokenizer(result, "\r\n"); toke.hasMoreTokens(); ) {
		    String line = toke.nextToken();
		    if (line.charAt(0) <= 32) {
			StringTokenizer lineToke = new StringTokenizer(line);
			String name = lineToke.nextToken().trim();
			set.add(
        name,
        new SocketClazzSet(name, dbURL.relativeURL(name + '/'), set, this)
      );
		    }
		}
		allClassesSet = set;
  } catch (AceException ae) {
    if(!ae.isRecoverable())
      sock = null;
    throw ae;
	    } finally {
		if (sock != null)
		    putSocket(sock);
	    }
	}

	return allClassesSet;
    }

    AceSet select(String clazz, String namePattern)
	                  throws AceException
    {
	AceSet res = null;
	while (res == null) {
	    try {
		res = _select(clazz, namePattern);
	    } catch (AceException ex) {
        ex.printStackTrace();
		if (! ex.isRecoverable())
		    throw ex;
	    }
	}
	return res;
    }

    private AceSet _select(String clazz, String namePattern)
	                  throws AceException
    {
	System.out.println("Selecting, clazz=" + clazz + ", pattern=" + namePattern);

	AceSocket sock = null;
	try {
	    sock = takeSocket();
      String _clazz = clazz;
      _clazz = Ace.decode(clazz);
	    String result = sock.transact(
        "find " +
        _clazz +
        " " +
			  namePattern
      );
	    int mpos = result.indexOf("// Found ");
	    if (mpos < 0)
		return null; // FIXME?
	    int numFound = Integer.parseInt(new StringTokenizer(
                               result.substring(mpos + 9)).nextToken());
	    System.out.println("found "+numFound);

	    result = sock.transact("list -j");
	    StringTokenizer listToke = new StringTokenizer(result, "\r\n");
	    List nameList = new ArrayList();
	    while (listToke.hasMoreTokens()) {
        String l = listToke.nextToken();
        Ace.encode(l);
        if (l.startsWith("?") && l.endsWith("?")) {
          int indx = l.indexOf("?", 1);
          String itemClazz = l.substring(1, indx);
          String itemName = l.substring(indx+1, l.length()-1);
          if(itemName.startsWith("\\?")) {
            itemName = itemName.substring(1);
          }
          itemName = Ace.encode(itemName);
          nameList.add(itemName);
        }
	    }
	    
	    return new SocketResultSet(
        this,
        allClasses().retrieve(clazz),
        nameList,
				dbURL.relativeURL(clazz + '?' + namePattern)
      );
  } catch (AceException ae) {
    if(!ae.isRecoverable())
      sock = null;
    throw ae;
	} finally {
	    if (sock != null)
		putSocket(sock);
	}
    }

    public AceURL toURL() {
	return dbURL;
    }

    public AceNode getObject(String clazz, String name)
	                  throws AceException
    {
	AceNode res = null;
	while (res == null) {
	    try {
		res = _getObject(clazz, name);
	    } catch (AceException ex) {
        ex.printStackTrace();
		if (! ex.isRecoverable())
		    throw ex;
	    }
	}
	return res;
    }

    private AceNode _getObject(String clazz, String name) 
            throws AceException
    {
      if(clazz == null) {
        throw new NullPointerException("Class name was null");
      }
	String cacheName = clazz + ":" + name;
	AceNode o = (AceNode) getFromCache(cacheName);
	if (o != null)
	    return o;
	// get object

	AceSocket sock = null;
	try {
	    sock = takeSocket();
      String query =
        "find " +
        Ace.decode(clazz) +
        " " +
			  Ace.decode(name);
	    String result = sock.transact(query);
      String tag = "// Found ";
      int indx = result.indexOf(tag) + tag.length();
      int spc = result.indexOf(" ", indx);
      String bit = result.substring(indx, spc);
      try {
        int count = Integer.parseInt(result.substring(indx, spc));
        if (count == 0) {
          throw new AceException("Couldn't find object " + cacheName + " using query " + query + " - does it exist?");
        } else if (count > 1) {
          throw new AceException("Found multiple objects (" + count + ") with " + cacheName + " using query " + query);
        }
      } catch (NumberFormatException nfe) {
        throw new AceError("Couldn't parse string '" + bit + "' at index " + indx + " to " + spc);
      }

	    String obj = sock.transact("show -p");
	    o = parser.parseObject(obj);
  } catch (AceException ae) {
    if(!ae.isRecoverable())
      sock = null;
    throw ae;
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
}
