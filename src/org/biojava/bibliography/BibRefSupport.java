// BibRefSupport.java
//
//    senger@ebi.ac.uk
//    April 2001
//

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
package org.biojava.bibliography;

import org.biojava.utils.candy.*;
import org.biojava.utils.*;
import java.util.*;

/**
 * This interface defines supporting utilities for working with
 * bibliographic repositories.
 *
 *<P>
 * The fundamental part of this interface deals with the controlled
 * vocabularies. However, the <tt>BibRefSupport</tt> interface is here
 * just a gateway to other Java interfaces defined in a separate
 * package {@link org.biojava.utils.candy}.
 *
 *<P>
 * The controlled vocabularies are used in order to find names of
 * all available attributes of the given bibliographic repository, to
 * find all possible values of some attributes, and to specify
 * availability of the ordering and searching criteria. Here belong
 * methods {@link #getVocabularyFinder getVocabularyFinder}, {@link
 * #getSupportedValues getSupportedValues}, and {@link
 * #getSupportedCriteria getSupportedCriteria}.
 *
 *<P>
 * The other <em>raison d'etre</em> for the BibRefSupport interface is
 * to have a place where some common constants can be put in. The
 * constants specify common vocabulary names (examples are {@link
 * #RESOURCE_TYPES} or {@link #JOURNAL_TITLES}, explicitly defined
 * bibliographic resource types (for example, {@link #TYPE_BOOK} or
 * {@link #TYPE_ARTICLE}), and few other things.
 *
 *<P>
 * And finally, there are some methods allowing to improve an
 * efficient access to the supporting resources by calling explicitly
 * {@link #connect connect} and {@link #disconnect disconnect}.
 *
 *<P>
 * It was an intention to separate completely methods dealing with
 * bibliographic repositories (as defined in interface {@link
 * BibRefQuery}) and methods helping with other things (as defined
 * here). This <em>box of bricks</em> approach helps to use different
 * communication protocols for bibliographic and supporting
 * repositories. For example, the performance can be sometimes
 * improved when the client loads separately all controlled
 * vocabularies and use them locally while the access to the
 * bibliographic repository is still remote.
 *
 *<P>
 *<H3>The implementation is advised to used the following constructor</h3>
 *<pre>
 *    public NameOfAnImplementation (String[] args, Hashtable props) {...}
 *</pre>
 *    where both <tt>args</tt> and <tt>props</tt> contain implementation
 *    specific parameters and properties. However, some properties are
 *    more probable to be used - the suggested names for them are defined
 *    also in this interface (e.g. {@link #INIT_PROP_LOG}).
 *<P>
 * The use of this constructor makes easier to load dynamically different
 * supporting implementations.
 *<P>
 * @author <A HREF="mailto:senger@ebi.ac.uk">Martin Senger</A>
 * @version $Id$
 */

public interface BibRefSupport {

    //
    // names for global vocabulary names
    //

    /**
     * A vocabulary name. The vocabulary contains stringified names of
     * all citation types stored in the repository. The names of types
     * that are explicitly defined by this interface should be equal
     * to the constant strings for types (such as {@link #TYPE_BOOK}).
     */
    static final String RESOURCE_TYPES     = "resource_types";

    /**
     * A vocabulary name. Some bibliographic repositories consist of
     * {@link BiblioEntryStatus#repositorySubset several
     * databases}. Their list can be provided by this vocabulary.
     */
    static final String REPOSITORY_SUBSETS = "repository_subsets";

    /**
     * A vocabulary name. The vocabulary contains available
     * {@link BiblioSubject#subjectHeadings subject headings}.
     */
    static final String SUBJECT_HEADINGS   = "subject_headings";

    /**
     * A vocabulary name. The vocabulary contains available languages
     * used in {@link BibRef#language} and {@link BiblioDescription#language}.
     */
    static final String LANGUAGES          = "languages";

    /**
     * A vocabulary name. The vocabulary contains journal titles as
     * used in {@link BiblioJournal#name}.
     */
    static final String JOURNAL_TITLES     = "journal_titles";

    /**
     * A vocabulary name. The vocabulary contains journal
     * abbreviations as used in {@link BiblioJournal#abbreviation}.
     */
    static final String JOURNAL_ABBREV     = "journal_abbreviations";

    /**
     * A vocabulary name. The vocabulary contains names of properties
     * that characterize a citation as a {@link
     * BiblioEntryStatus#properties repository/database record}.
     */
    static final String ENTRY_PROPERTIES   = "entry_properties";

    //
    // names for (some) bibliographic resource types
    //

    /** A name of a bibliographic resource type. */
    static final String TYPE_BOOK            = "Book";

    /** A name of a bibliographic resource type. */
    static final String TYPE_ARTICLE         = "Article";

    /** A name of a bibliographic resource type. */
    static final String TYPE_BOOK_ARTICLE    = "BookArticle";

    /** A name of a bibliographic resource type. */
    static final String TYPE_JOURNAL_ARTICLE = "JournalArticle";

    /** A name of a bibliographic resource type. */
    static final String TYPE_PATENT          = "Patent";

    /** A name of a bibliographic resource type. */
    static final String TYPE_THESIS          = "Thesis";

    /** A name of a bibliographic resource type. */
    static final String TYPE_PROCEEDING      = "Proceeding";

    /** A name of a bibliographic resource type. */
    static final String TYPE_TECH_REPORT     = "TechReport";

    /** A name of a bibliographic resource type. */
    static final String TYPE_WEB_RESOURCE    = "WebResource";

    //
    // names for (some) other corners of a bibliographic repository
    //

    /** A name of a provider type. */
    static final String PROVIDER_PERSON       = "Person";

    /** A name of a provider type. */
    static final String PROVIDER_ORGANISATION = "Organisation";

    /** A name of a provider type. */
    static final String PROVIDER_SERVICE      = "Service";

    /** A name of a provider type. */
    static final String GENERIC_PROVIDER      = "Provider";

    //
    // names for (some) attribute names
    //

    /**
     * A part of a vocabulary name. It is usually coupled together
     * with a bibliographic resource type to give a full vocabulary
     * name. For example: {@link BibRefSupport#TYPE_JOURNAL_ARTICLE
     * JournalArticle}/ATTR_PROPERTIES.
     *<P>
     * The vocabulary contains property names for the given resource
     * type as defined in {@link BibRef#properties}.
     */
    static final String ATTR_PROPERTIES = "properties";

    /**
     * A vocabulary name, or a part of a vocabulary name.
     * The vocabulary contains all allowed keys in
     * {@link BiblioScope#properties}.
     */
    static final String ATTR_SCOPE      = "scope";

    /**
     * A vocabulary name, or a part of a vocabulary name.
     * The vocabulary contains all allowed keys in
     * {@link BibRef#format}.
     */
    static final String ATTR_FORMAT     = "format";

    //
    // names characterizing attributes
    //

    /**
     * A role of an attribute.  
     *<P>
     * The introspection mechanism (provided by using controlled
     * vocabularies) allows to find what attributes are available in
     * the repository. The attributes which can be used in query
     * methods should be identified by putting this constant into
     * their vocabulary entry (somewhere in the {@link
     * CandyEntry#description description} field).
     */
    static final String ROLE_ATTR_QUERYABLE   = "queryable";

    /**
     * A role of an attribute.  
     *<P>
     * The introspection mechanism (provided by using controlled
     * vocabularies) allows to find what attributes are available in
     * the repository. The attributes which can be used in retrieval
     * methods should be identified by putting this constant into
     * their vocabulary entry (somewhere in the {@link
     * CandyEntry#description description} field).
     */
    static final String ROLE_ATTR_RETRIEVABLE = "retrievable";

    /**
     * A property name ("<b>log</b>").
     *<p>
     * Used for passing an instance dealing with logging.
     */
    static final String INIT_PROP_LOG = "log";

    /**
     * A property name ("<b>bibrefsupport</b>").
     *<p>
     * Used for passing an instance of a class implementing this
     * interface. It is recommended to pass this property, for
     * example, in the constructor of an implementation of the {@link
     * BibRefQuery} interace}.
     */
    static final String INIT_PROP_SUPPORT = "bibrefsupport";


    /**************************************************************************
     * It creates a connection to an object providing the supporting
     * utilities, or/and it makes all necessary initialization steps
     * needed for further communication.
     *
     *<P>
     * However, there should be no need to call this method
     * explicitly, the other methods should do it automatically before
     * they need to use any supporting utility.
     *
     *<P>
     * @throws NestedException if the connection/initialization cannot
     * be established
     *************************************************************************/
    void connect()
	throws NestedException;

    /**************************************************************************
     * It checks if a utility object is available. The semantic of 
     * <em>available</em>depends on the implementation.
     *************************************************************************/
    boolean isReady();

    /**************************************************************************
     * It closes connection with a utility object. Implementations may
     * choose to use this method for freeing resources.
     *************************************************************************/
    void disconnect();

    /**************************************************************************
     * It returns an object representing a central place where all
     * controlled vocabularies can be received from and shared by all
     * users.
     *
     *<P>
     * The controlled vocabularies are used for finding names of
     * all available attributes of the given bibliographic repository,
     * for finding all possible values of some attributes, and for
     * specifying availability of the ordering and searching criteria.
     *
     *<P>
     * @return an instance implementing {@link CandyFinder} interface
     * @throws NestedException if the vocabulary finder cannot be found
     *************************************************************************/
    CandyFinder getVocabularyFinder()
	throws NestedException;

    /**************************************************************************
     * It returns a controlled vocabulary containing all possible
     * values of the attribute given in <tt>attrName</tt> in the
     * context given in <tt>resourceType</tt>. It is up to the
     * implementation to define the context.
     *
     *<P>
     * Specifically, for <tt>attrName</tt> equals to {@link
     * #ATTR_PROPERTIES} it returns a vocabulary containing attribute
     * names available for the given citation type.
     *
     *<P>
     * @param resourceType is usually a name of a citation type (e.g. "Book",
     *        "JournalArticle"), see {@link #TYPE_BOOK}, etc., but can define
     *        other contexts as well (e.g. "Person" as defined by constant
     *        {@link #PROVIDER_PERSON})
     * @param attrName a name of an attribute whose values should be
     *        available from the returned vocabulary
     * @return a controlled vocabulary
     * @throws NestedException if there is no such vocabulary available, or
     *        something else wrong happened
     *************************************************************************/
    CandyVocabulary getSupportedValues (String resourceType, String attrName)
	throws NestedException;

    /**************************************************************************
     * It returns all supported searching and sorting criteria for the
     * whole bibliographic repository.
     *<P>
     * @see #getSupportedCriteria(String) getSupportedCriteria for a repository subset
     * @return available criteria
     * @throws NestedException if something bad happened
     *************************************************************************/
    BiblioCriterion[] getSupportedCriteria()
	throws NestedException;

    /**************************************************************************
     * It returns all supported searching and sorting criteria in the given
     * repository subset.
     *<P>
     * @see #getSupportedCriteria getSupportedCriteria regardless of repository subsets
     * @param repositorySubset a name of a repository subset as used in
     *                         {@link BiblioEntryStatus#repositorySubset}, and
     *                         as (possibly) defined in the controlled
     *                         vocabulary {@link #REPOSITORY_SUBSETS}
     * @return available criteria
     * @throws NestedException if there is no such repository subset, or
     *        something else wrong happened
     *************************************************************************/
    BiblioCriterion[] getSupportedCriteria (String repositorySubset)
	throws NestedException;

    /*************************************************************************
     * It merges all given collections together. The result should
     * eliminate redundancy - which usually means removing the same
     * citations.
     *
     * <P>
     * The merging process can be influenced by specifying some
     * <tt>properties</tt> (but they are not defined by this
     * interface, they depend on the implementation).
     *
     * <P>
     * <em>
     * Note that the merging is independent on the repository, or repositories
     * where the collections come from. The main raison d'etre is actually
     * to allow to merge collections from different repositories. But it
     * opens the question what to do with the resulting collection (how to
     * query it, for example, if it is a "virtual" collection). So it can
     * be quite difficult to implement this method :-(.
     * </em>
     * <P>
     * @param collections to be merged together
     * @param properties define features how to do merging
     * @return a merged collection
     * @throws NestedException if merging failed (which may also happen when
     *         any of the collection is too large)
     *************************************************************************/
    BibRefQuery union (BibRefQuery[] collections, Hashtable properties)
	throws NestedException;

}
