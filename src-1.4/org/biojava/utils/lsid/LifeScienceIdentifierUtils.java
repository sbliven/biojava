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

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * Provides reference implementations and utilities
 * for the LifeScienceIdentifier interface.
 *
 * @author Michael Heuer
 */
public class LifeScienceIdentifierUtils {

    /**
     * The regular expression pattern that defines
     * the format of a LifeScienceIdentifier string.
     *
     * @see #parse(String)
     */
    private static final Pattern pattern;

    /**
     * The default factory used to create LifeScienceIdentifiers.
     *
     * @see #getDefaultFactory()
     * @see #setDefaultFactory(LifeScienceIdentifierFactory)
     */
    private static LifeScienceIdentifierFactory factory;

    static {
	pattern = Pattern.compile("\\A(.*):(.*):([0-9]+):([0-9]*)\\Z");
	factory = new DefaultLifeScienceIdentifierFactory();
    }

    /**
     * Restricted utils class constructor.
     */
    private LifeScienceIdentifierUtils() {
	super();
    }

    /**
     * Returns the default LifeScienceIdentifier factory used
     * in this utils class.
     */
    public static LifeScienceIdentifierFactory getDefaultFactory() {
	return factory;
    }

    /**
     * Sets the default LifeScienceIdentifier factory used
     * in this utils class.
     *
     * @throws IllegalArgumentException if defaultFactory is null
     */
    public static void setDefaultFactory(LifeScienceIdentifierFactory defaultFactory) {
	if (defaultFactory==null)
	    throw new IllegalArgumentException("default factory must not be null");

	factory = defaultFactory;
    }

    /**
     * Parse the specified formatted string into a LifeScienceIdentifier using
     * the default LifeScienceIdentifier factory.
     *
     * <p>
     * Uses the regular expression pattern:  <code>\A(.*):(.*):([0-9]+)\.([0-9]*)\Z</code>
     * </p>
     *
     * @see #setDefaultFactory(LifeScienceIdentifierFactory)
     *
     * @param formattedString a string of the format <code>authority:namespace:object_id:version</code>
     * @throws IllegalArgumentException if formattedString is null
     * @throws LifeScienceIdentifierParseException if formattedString cannot be parsed
     */
    public static final LifeScienceIdentifier parse(String formattedString)
	throws LifeScienceIdentifierParseException
    {
	if (formattedString==null)
	    throw new IllegalArgumentException("formatted string must not be null");

	// FIXME
	// reimpl parsing without regex for < 1.4 compat

	try {
	    Matcher m = pattern.matcher(formattedString);
	    if (m.matches()) {
		
		String authority = m.group(1);
		String namespace = m.group(2);
		Object objectId = m.group(3);
		Object version = m.group(4);

		final LifeScienceIdentifier lsid = factory.create(authority,
								  namespace,
								  objectId,
								  version);

		return (lsid);

	    } else {
		throw new LifeScienceIdentifierParseException("cannot parse formatted string");
	    }
	} catch (NumberFormatException nfe) {
	    throw new LifeScienceIdentifierParseException("cannot parse formatted string: " + nfe);
	}
    }

    // default factory

    private static final class DefaultLifeScienceIdentifierFactory
	implements LifeScienceIdentifierFactory {

	private DefaultLifeScienceIdentifierFactory() {
	    super();
	}

	public LifeScienceIdentifier create(String authority,
					    String namespace,
					    Object objectId,
					    Object version) {

	    return new DefaultLifeScienceIdentifier(authority,
						    namespace,
						    objectId,
						    version);	    
	}

	private static final class DefaultLifeScienceIdentifier
	    implements LifeScienceIdentifier {

	    private final String authority;
	    private final String namespace;
	    private final Object objectId;
	    private final Object version;

	    private DefaultLifeScienceIdentifier(String authority,
						 String namespace,
						 Object objectId,
						 Object version) {

		this.authority = authority;
		this.namespace = namespace;
		this.objectId = objectId;
		this.version = version;
	    }
	    public String getAuthority() {
		return authority;
	    }
	    public String getNamespace() {
		return namespace;
	    }
	    public Object getObjectId() {
		return objectId;
	    }
	    public Object getVersion() {
		return version;
	    }
	    //public Object getSecurity();

	    public boolean equals(Object value) {
		if (value instanceof LifeScienceIdentifier) {

		    LifeScienceIdentifier lsid = (LifeScienceIdentifier) value;

		    // FIXME
		    // if version is optional, it may be null

		    return ( (authority.equals(lsid.getAuthority())) &&
			     (namespace.equals(lsid.getNamespace())) &&
			     (objectId.equals(lsid.getObjectId())) &&
			     (version.equals(lsid.getVersion())) );
		} else {
		    return false;
		}
	    }
	    public int hashCode() {
		return (toString().hashCode());
	    }
	    public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getAuthority());
		sb.append(":");
		sb.append(getNamespace());
		sb.append(":");
		sb.append(getObjectId().toString());

		// if version is optional, it may be null

		if (getVersion()!=null) {
		    sb.append(":");
		    sb.append(getVersion().toString());
		}
		return (sb.toString());
	    }
	}
    }
}
