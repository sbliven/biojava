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

package org.biojava.utils.lsid;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * Life Science Identifier (LSID).
 *
 * LSID syntax:
 * <p>
 * &lt;authority&gt;:&lt;namespace&gt;:&lt;objectId&gt;:&lt;version&gt;:&lt;security&gt;
 * <p>
 * The elements of a LSID are as follows:
 * <ul>
 * <li>authority = &lt;authority&gt; identifies the organization
 * <li>namespace = &lt;namespace&gt; namespace to scope the identifier value
 * <li>object_id = &lt;objectId&gt; identifier of the object within namespace
 * <li>version = &lt;version&gt; version information
 * <li>security = &lt;security&gt; optional security information
 * </ul>
 *
 * @author Michael Heuer
 */
public final class LifeScienceIdentifier
    implements Serializable
{
    private final String authority;
    private final String namespace;
    private final String objectId;
    private final int version;
    private final String security;

    /**
     * Create a new LifeScienceIdentifier.
     *
     * @param authority identifies the organization
     * @param namespace namespace to scope the identifier value
     * @param objectId identifer of the object within namespace
     * @param version version information
     * @param security optional security information
     *
     * @throws IllegalArgumentException if any of <code>authority</code>,
     *   <code>namespace</code>, or <code>objectId</code> are null
     */
    private LifeScienceIdentifier(String authority,
				  String namespace,
				  String objectId,
				  int version,
				  String security)
    {
	if (authority == null)
	    throw new IllegalArgumentException("authority must not be null");
	if (namespace == null)
	    throw new IllegalArgumentException("namespace must not be null");
	if (objectId == null)
	    throw new IllegalArgumentException("objectId must not be null");

	this.authority = authority;
	this.namespace = namespace;
	this.objectId = objectId;
	this.version = version;
	this.security = security;
    }

    /**
     * Return the authority for this identifier.
     */
    public String getAuthority()
    {
	return authority;
    }

    /**
     * Return the namespace for this identifier
     * within the authority.
     */
    public String getNamespace()
    {
	return namespace;
    }

    /**
     * Return the object id of this identifier.
     */
    public String getObjectId()
    {
	return objectId;
    }

    /**
     * Return the version of the object id of
     * this identifier.
     */
    public int getVersion()
    {
	return version;
    }

    /**
     * Return the security information of this identifier.
     * May return null.
     */
    public String getSecurity()
    {
	return security;
    }

    public boolean equals(Object value)
    {
	if (this == value)
	    return true;
	if (!(value instanceof LifeScienceIdentifier))
	    return false;

	LifeScienceIdentifier lsid = (LifeScienceIdentifier) value;
 
	return (authority.equals(lsid.getAuthority()) &&
		namespace.equals(lsid.getNamespace()) &&
		objectId.equals(lsid.getObjectId()) &&
		version == lsid.getVersion() &&
		(security == null ? lsid.getSecurity() == null : security.equals(lsid.getSecurity())));      
    }

    public int hashCode()
    {
	return (this.toString().hashCode());
    }

    public String toString()
    {
	StringBuffer sb = new StringBuffer();
	sb.append(getAuthority());
	sb.append(":");
	sb.append(getNamespace());
	sb.append(":");
	sb.append(getObjectId());
	sb.append(":");
	sb.append(getVersion());
	
	if (getSecurity() != null)
	{
	    sb.append(":");
	    sb.append(getSecurity());
	}

	return (sb.toString());
    }

    /**
     * Create a new LifeScienceIdentifier parsed
     * from the properly formatted string <code>lsid</code>.
     *
     * @param lsid formatted LifeScienceIdentifier string
     *
     * @throws LifeScienceIdentifierParseException if <code>lsid</code>
     *    is not properly formatted
     */
    public static LifeScienceIdentifier valueOf(String lsid)
	throws LifeScienceIdentifierParseException
    {
	try 
	{
	    StringTokenizer st = new StringTokenizer(lsid, ":");
	    
	    if (st.countTokens() >= 4)
	    {
		String authority = st.nextToken();
		String namespace = st.nextToken();
		String objectId = st.nextToken();
		int version = Integer.parseInt(st.nextToken());
		
		String security = null;
		if (st.hasMoreTokens())
		{
		    security = st.nextToken();
		}
		
		return valueOf(authority, namespace, objectId, version, security);
	    }
	    else
	    {
		throw new LifeScienceIdentifierParseException("couldn't parse: " + lsid);
	    }
	}
	catch (NumberFormatException nfe)
	{
	    throw new LifeScienceIdentifierParseException("couldn't parse: " + lsid + " " + nfe);
	}
    }

    /**
     * Create a new LifeScienceIdentifier from the
     * specified parameters.
     *
     * @param authority identifies the organization
     * @param namespace namespace to scope the identifier value
     * @param objectId identifer of the object within namespace
     * @param version version information
     * @param security optional security information
     *
     * @throws IllegalArgumentException if any of <code>authority</code>,
     *   <code>namespace</code>, or <code>objectId</code> are null
     */	
    public static LifeScienceIdentifier valueOf(String authority,
						String namespace,
						String objectId,
						int version,
						String security)
    {
	return new LifeScienceIdentifier(authority,
					 namespace,
					 objectId,
					 version,
					 security);
    }

    private static final long serialVersionUID = 1836992936659290633L;
}
