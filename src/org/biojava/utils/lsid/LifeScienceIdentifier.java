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
 * urn:lsid:&lt;authorityId&gt;:&lt;namespaceId&gt;:&lt;objectId&gt;:&lt;revisionId&gt;
 * <p>
 * The elements of a LSID are as follows:
 * <ul>
 * <li>authorityId = &lt;authorityId&gt; identifies the organization
 * <li>namespaceId = &lt;namespaceId&gt; namespace to scope the identifier value
 * <li>objectId = &lt;objectId&gt; identifier of the object within namespace
 * <li>revisionId = &lt;revisionId&gt; optional version information
 * </ul>
 *
 * <p>Examples:
 * <pre>
 * urn:lsid:ebi.ac.uk:SWISS-PROT/accession:P34355:3
 * urn:lsid:rcsb.org:PDB:1D4X:22
 * urn:lsid:ncbi.nlm.nih.gov:Genbank/accession:NT_001063:2
 * </pre></p>
 *
 * <p>As described in the memo <i>URN Namespace for Life Science Identifiers</i><br/>
 * &gt; <a href="http://www.i3c.org/workgroups/technical_architecture/resources/lsid/docs/LSIDSyntax9-20-02.htm">
 * http://www.i3c.org/workgroups/technical_architecture/resources/lsid/docs/LSIDSyntax9-20-02.htm</a></p>
 *
 * @author Michael Heuer
 */
public final class LifeScienceIdentifier
    implements Serializable
{
    private final String authorityId;
    private final String namespaceId;
    private final String objectId;
    private final String revisionId;

    /**
     * Create a new LifeScienceIdentifier.
     *
     * @param authorityId identifies the organization
     * @param namespaceId namespace to scope the identifier value
     * @param objectId identifer of the object within namespace
     * @param revisionId version information
     *
     * @throws IllegalArgumentException if any of <code>authorityId</code>,
     *   <code>namespaceId</code>, or <code>objectId</code> are null
     */
    private LifeScienceIdentifier(String authorityId,
				  String namespaceId,
				  String objectId,
				  String revisionId)
    {
	if (authorityId == null)
	    throw new IllegalArgumentException("authority must not be null");
	if (namespaceId == null)
	    throw new IllegalArgumentException("namespace must not be null");
	if (objectId == null)
	    throw new IllegalArgumentException("objectId must not be null");

	this.authorityId = authorityId;
	this.namespaceId = namespaceId;
	this.objectId = objectId;
	this.revisionId = revisionId;
    }

    /**
     * Return the authority id for this identifier.
     */
    public String getAuthorityId()
    {
	return authorityId;
    }

    /**
     * Return the namespace id for this identifier
     * within the authority.
     */
    public String getNamespaceId()
    {
	return namespaceId;
    }

    /**
     * Return the object id of this identifier.
     */
    public String getObjectId()
    {
	return objectId;
    }

    /**
     * Return the revision id of this identifier.
     * May return null.
     */
    public String getRevisionId()
    {
	return revisionId;
    }

    public boolean equals(Object value)
    {
	if (this == value)
	    return true;
	if (!(value instanceof LifeScienceIdentifier))
	    return false;

	LifeScienceIdentifier lsid = (LifeScienceIdentifier) value;
 
	return (authorityId.equals(lsid.getAuthorityId()) &&
		namespaceId.equals(lsid.getNamespaceId()) &&
		objectId.equals(lsid.getObjectId()) &&
		(revisionId == null ? lsid.getRevisionId() == null : revisionId.equals(lsid.getRevisionId())));
    }

    public int hashCode()
    {
	return (this.toString().hashCode());
    }

    public String toString()
    {
	StringBuffer sb = new StringBuffer();
	sb.append("urn:lsid:");
	sb.append(getAuthorityId());
	sb.append(":");
	sb.append(getNamespaceId());
	sb.append(":");
	sb.append(getObjectId());

	if (getRevisionId() != null)
	{
	    sb.append(":");
	    sb.append(getRevisionId());
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
	if (lsid.length() < 9)
	    throw new LifeScienceIdentifierParseException("couldn't parse: " + lsid + ", didn't contain urn prefix");
	
	String urnPrefix = lsid.substring(0,9);
	lsid = lsid.substring(9);
	
	if (!("urn:lsid:".equalsIgnoreCase(urnPrefix)))
	    throw new LifeScienceIdentifierParseException("couldn't parse: " + lsid + ", incorrect urn prefix");
	
	StringTokenizer st = new StringTokenizer(lsid, ":", true);
	
	int count = st.countTokens();
	if (count >= 5)
	{
	    String authorityId = st.nextToken();
	    st.nextToken();
	    String namespaceId = st.nextToken();
	    st.nextToken();
	    String objectId = st.nextToken(); 
	    
	    String revisionId = null;
	    if (count >= 6)
	    {
		st.nextToken();
		revisionId = "";
	    }
	    if (st.hasMoreTokens())
	    {
		revisionId = st.nextToken();
	    }
	    if (st.hasMoreTokens())
		throw new LifeScienceIdentifierParseException("couldn't parse: " + lsid + ", too many tokens");
	    
	    return valueOf(authorityId, namespaceId, objectId, revisionId);
	}
	else
	{
	    throw new LifeScienceIdentifierParseException("couldn't parse: " + lsid + ", unproperly formatted lsid");
	}
    }

    /**
     * Create a new LifeScienceIdentifier from the
     * specified parameters.
     *
     * @param authorityId identifies the organization
     * @param namespaceId namespace to scope the identifier value
     * @param objectId identifer of the object within namespace
     * @param revisionId optional version information
     *
     * @throws IllegalArgumentException if any of <code>authorityId</code>,
     *   <code>namespaceId</code>, or <code>objectId</code> are null
     */	
    public static LifeScienceIdentifier valueOf(String authorityId,
						String namespaceId,
						String objectId,
						String revisionId)
    {
	return new LifeScienceIdentifier(authorityId,
					 namespaceId,
					 objectId,
					 revisionId);
    }

    /**
     * Create a new LifeScienceIdentifier from the
     * specified parameters.
     *
     * @param authorityId identifies the organization
     * @param namespaceId namespace to scope the identifier value
     * @param objectId identifer of the object within namespace
     *
     * @throws IllegalArgumentException if any of <code>authorityId</code>,
     *   <code>namespaceId</code>, or <code>objectId</code> are null
     */	
    public static LifeScienceIdentifier valueOf(String authorityId,
						String namespaceId,
						String objectId)
    {
	return new LifeScienceIdentifier(authorityId,
					 namespaceId,
					 objectId,
					 null);
    }

    private static final long serialVersionUID = 8478038493421763123L;
}
