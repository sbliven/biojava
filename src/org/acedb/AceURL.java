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
package org.acedb;


import java.net.*;

public class AceURL {
    private String authority;
    private String userInfo;
    private String file;
    private String host;
    private int port;
    private String protocol;
    private String query;
    private String ref;

    public AceURL(
      String protocol,
		  String host,
		  int port,
		  String file,
		  String query,
		  String ref,
		  String userInfo,
		  String authority
    ) {
	this.protocol = protocol;
	this.host = host;
	this.port = port;
	this.file = file;
	this.query = query;
	this.ref = ref;
	this.userInfo = userInfo;
	this.authority = authority;
    }

    public AceURL(URL old, String userInfo, String authority, String query) {
	this.protocol = old.getProtocol();
	this.host = old.getHost();
	this.port = old.getPort();
	this.file = old.getFile();
	//this.query = old.getQuery();
  this.query = query;
	this.ref = old.getRef();
	this.userInfo = userInfo;
	this.authority = authority;
    }

    public AceURL(String spec) 
        throws AceURLException
    {
	int colon = spec.indexOf(':');
	if (colon == -1)
	    throw new AceURLException("No protocol");
	protocol = spec.substring(0, colon);
	
	int locatorPart = spec.indexOf("//");
	String localSection = null;
	if (locatorPart == -1) {
	    localSection = spec.substring(colon + 1);
	} else {
	    String locator = null;
	    int startLocal = spec.indexOf('/', locatorPart + 2);
	    if (startLocal != -1) {
		localSection = spec.substring(startLocal);
		locator = spec.substring(locatorPart + 2, startLocal);
	    } else {
		locator = spec.substring(locatorPart + 2);
	    }
	    int at = locator.indexOf('@');
	    if (at != -1) {
		String authData = locator.substring(0, at);
		colon = authData.indexOf(':');
		if (colon == -1) {
		    userInfo = authData;
		} else {
		    userInfo = authData.substring(0, colon);
		    authority = authData.substring(colon + 1);
		}
		locator = locator.substring(at + 1);
	    }

	    colon = locator.indexOf(':');
	    if (colon == -1) {
		host = locator;
	    } else {
		host = locator.substring(0, colon);
		try {
		    port = Integer.parseInt(locator.substring(colon + 1));
		} catch (NumberFormatException ex) {
		    throw new AceURLException(ex, "Bad port number");
		}
	    }
	}

	if (localSection != null) {
	    int hash = localSection.indexOf('#');
	    if (hash != -1) {
		ref = localSection.substring(hash + 1);
		localSection = localSection.substring(0, hash);
	    }

	    int queryq = localSection.indexOf('?');
	    if (queryq == -1) {
		file = localSection;
	    } else {
		query = localSection.substring(queryq + 1);
		file = localSection.substring(0, queryq);
	    }
	}
    }

    public AceURL relativeURL(String fragment) {
      return new AceURL(
        protocol,
        host,
        port,
        (file == null) ? fragment : file + "/" + fragment,
        null,
        null,
        userInfo,
        authority
      );
    }
    
    public String getAuthority() {
	return authority;
    }

    public String getFile() {
	return file;
    }

    public String getHost() {
	return host;
    }

    public String getPath() {
	System.out.println("Fixme AceURL.getPath()");
	return file;
    }

    public int getPort() {
	return port;
    }

    public String getProtocol() {
	return protocol;
    }

    public String getQuery() {
	return query;
    }

    public String getRef() {
	return ref;
    }

    public String getUserInfo() {
	return userInfo;
    }

    public int hashCode() {
	return toString().hashCode();
    }

    private boolean streq(String a, String b) {
	if (a == b)
	    return true;
	if (a == null || b == null)
	    return false;
	return a.equals(b);
    }
    
    public boolean equals(Object o) {
	if (! (o instanceof AceURL))
	    return false;
	AceURL ao = (AceURL) o;
	if (! streq(ao.getProtocol(), protocol))
	    return false;
	if (! streq(ao.getHost(), host))
	    return false;
	if (ao.getPort() != port)
	    return false;
	if (! streq(ao.getFile(), file))
	    return false;
	if (! streq(ao.getQuery(), query))
	    return false;
	if (! streq(ao.getRef(), ref))
	    return false;
	if (! streq(ao.getUserInfo(), userInfo))
	    return false;
	if (! streq(ao.getAuthority(), authority))
	    return false;

	return true;
    }

    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append(protocol);
      sb.append("://");
      if (userInfo != null) {
        sb.append(userInfo);
        if (authority != null) {
          sb.append(':');
          sb.append(authority);
        }
        sb.append('@');
      }
      sb.append(host);
      sb.append(':');
      sb.append(port);
      if (file != null) {
        sb.append('/');
        sb.append(file);
      }
      if (query != null) {
        sb.append('?');
        sb.append(query);
      }
      if (ref != null) {
        sb.append('#');
        sb.append(ref);
      }
      
      return sb.toString();
    }
}
