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

package org.biojava.bio.molbio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.regex.Pattern;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.SmallAnnotation;
import org.biojava.bio.program.tagvalue.*;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.MotifTools;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeListener;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.ParserException;
import org.biojava.utils.SmallSet;

/**
 * <p><code>RestrictionEnzymeManager</code> manages collections of
 * static <code>RestrictionEnzyme</code> instances. A properties file
 * should be placed in the CLASSPATH containing a key
 * "rebase.data.file" and a corresponding value of a REBASE file
 * (standard REBASE format #31 conventionally named withrefm.### where
 * ### is the version number). This file will be loaded by the
 * <code>RestrictionEnzymeManager</code> <code>ClassLoader</code>. The
 * properties are loaded as a <code>ResourceBundle</code>, so the file
 * should be named "RestrictionEnzymeManager.properties".</p>
 *
 * @author Keith James
 * @since 1.3
 */
public final class RestrictionEnzymeManager
{
    /**
     * <code>REBASE_DATA_KEY</code> the ResourceBundle key which
     * specifies the location of the REBASE flat file.
     */
    public static final String REBASE_DATA_KEY = "rebase.data.file";

    /**
     * <code>REBASE_TAG_NAME</code> the REBASE tag containing the
     * enzyme name.
     */
    public static final String REBASE_TAG_NAME = "<1>";

    /**
     * <code>REBASE_TAG_ISZR</code> the REBASE tag containing the
     * enzyme isoschizomers.
     */
    public static final String REBASE_TAG_ISZR = "<2>";

    /**
     * <code>REBASE_TAG_SITE</code> the REBASE tag containing the
     * enzyme site.
     */
    public static final String REBASE_TAG_SITE = "<3>";

    /**
     * <code>REBASE_TAG_METH</code> the REBASE tag containing the
     * methylation site.
     */
    public static final String REBASE_TAG_METH = "<4>";

    /**
     * <code>REBASE_TAG_ORGN</code> the REBASE tag containing the
     * organism.
     */
    public static final String REBASE_TAG_ORGN = "<5>";

    /**
     * <code>REBASE_TAG_SRCE</code> the REBASE tag containing the
     * source.
     */
    public static final String REBASE_TAG_SRCE = "<6>";

    /**
     * <code>REBASE_TAG_COMM</code> the REBASE tag containing the
     * commercial suppliers.
     */
    public static final String REBASE_TAG_COMM = "<7>";

    /**
     * <code>REBASE_TAG_REFS</code> the REBASE tag containing the
     * references.
     */
    public static final String REBASE_TAG_REFS = "<8>";

    private static ResourceBundle bundle =
        ResourceBundle.getBundle(RestrictionEnzymeManager.class.getName());

    static
    {
        String rebaseDataFileName = bundle.getString(REBASE_DATA_KEY);
        InputStream is = RestrictionEnzymeManager.class.getResourceAsStream(rebaseDataFileName);
        loadData(is);
    }

    private static Map nameToEnzyme;
    private static Map nameToIsoschizomers;
    private static Map sizeToCutters;
    private static Map enzymeToPattern;
    private static Map enzymeToAnnotation;

    /**
     * <code>RestrictionEnzymeManager</code> is a static utility
     * method class and no instances should be created.
     */
    private RestrictionEnzymeManager() { }

    /**
     * <code>getAllEnzymes</code> returns an unmodifable set of all
     * available enzymes.
     *
     * @return a <code>Set</code> of <code>RestrictionEnzyme</code>s.
     */
    public static Set getAllEnzymes()
    {
        return Collections.unmodifiableSet(enzymeToPattern.keySet());
    }

    /**
     * <code>getEnzyme</code> returns an enzyme by name.
     *
     * @param name a <code>String</code> such as EcoRI, case
     * sensitive.
     *
     * @return a <code>RestrictionEnzyme</code>.
     */
    public static RestrictionEnzyme getEnzyme(String name)
    {
        if (! nameToEnzyme.containsKey(name))
            throw new IllegalArgumentException("Unknown RestrictionEnzyme name '"
                                               + name
                                               + "'");

        return (RestrictionEnzyme) nameToEnzyme.get(name);
    }

    /**
     * <code>getIsoschizomers</code> returns an unmodifable set of the
     * isoschizomers of this enzyme.
     *
     * @param name a <code>String</code> such as EcoRI, case
     * sensitive.
     *
     * @return a <code>Set</code> of <code>RestrictionEnzyme</code>s.
     */
    public static Set getIsoschizomers(String name)
    {
        if (! nameToIsoschizomers.containsKey(name))
            throw new IllegalArgumentException("Unknown RestrictionEnzyme name '"
                                               + name
                                               + "'");
        
        return Collections.unmodifiableSet((Set) nameToIsoschizomers.get(name));
    }

    /**
     * <code>getNCutters</code> returns an unmodifable set of all
     * enzymes with a cut site of size n.
     *
     * @param n an <code>int</code> cut site size.
     *
     * @return a <code>Set</code> of <code>RestrictionEnzyme</code>s.
     */
    public static Set getNCutters(int n)
    {
        Integer size = new Integer(n);
        if (! sizeToCutters.containsKey(size))
            return Collections.EMPTY_SET;

        return Collections.unmodifiableSet((Set) sizeToCutters.get(size));
    }

    /**
     * <code>getPatterns</code> returns two <code>Pattern</code>
     * objects for an enzyme, one matches the forward strand and one
     * the reverse. This enables searching of both strands of a
     * sequence without reverse-complementing it. As
     * <code>Pattern</code> objects are thread-safe these may be used
     * for all searches.
     *
     * @param enzyme a <code>RestrictionEnzyme</code>.
     *
     * @return a <code>Pattern []</code> array with the forward strand
     * <code>Pattern</code> at index 0 and the reverse at index 1.
     */
    public static Pattern [] getPatterns(RestrictionEnzyme enzyme)
    {
        if (! enzymeToPattern.containsKey(enzyme))
            throw new IllegalArgumentException("RestrictionEnzyme '"
                                               + enzyme.getName()
                                               + "' is not registered. No precompiled Pattern is available");

        return (Pattern []) enzymeToPattern.get(enzyme);
    }

    /**
     * <code>getAnnotation</code> returns an immutable, static
     * annotation describing the enzyme. This is suitable for adding
     * to <code>Feature</code>s which represent restriction sites. The
     * annotation produced currently contains one key "dbxref" in line
     * with the GenBank/EMBL qualifier for the "misc_binding" feature
     * key. The key has a corresponding value "REBASE:&lt;enzyme
     * name&gt;".
     *
     * @param enzyme a <code>RestrictionEnzyme</code>.
     *
     * @return an <code>Annotation</code>.
     */
    public static Annotation getAnnotation(RestrictionEnzyme enzyme)
    {
        if (! enzymeToAnnotation.containsKey(enzyme))
            throw new IllegalArgumentException("RestrictionEnzyme '"
                                               + enzyme.getName()
                                               + "' is not registered. No Annotation is available");

        return (Annotation) enzymeToAnnotation.get(enzyme);
    }

    /**
     * <code>register</code> regisiters a new
     * <code>RestrictionEnzyme</code> with the manager. It does not
     * check that the isoschizomers are known to the manager. If there
     * are custom isoschizomers in the <code>Set</code>, they should
     * be also be registered.
     *
     * @param enzyme a <code>RestrictionEnzyme</code> to register.
     *
     * @param isoschizomers a <code>Set</code> of
     * <code>RestrictionEnzyme</code>s which are isoschizomers.
     */
    public synchronized static void register(RestrictionEnzyme enzyme,
                                             Set               isoschizomers)
    {
        for (Iterator ii = isoschizomers.iterator(); ii.hasNext();)
        {
            Object o = ii.next();

            if (! (o instanceof RestrictionEnzyme))
            {
                throw new IllegalArgumentException("Isoschizomers set may contain only RestrictionEnzymes. Found '"
                                                   + o
                                                   + "'");
            }
        }

        registerEnzyme(enzyme);

        String name = enzyme.getName();
        nameToIsoschizomers.put(name, isoschizomers);
    }

    /**
     * <code>registerEnzyme</code> registers an enzyme, but does not
     * populate its isoschizomers. This is because registering the
     * contents of a REBASE file and registering a custom enzyme
     * handle addition of isoschizomers differently, but both use this
     * method for all other registration functions.
     *
     * @param enzyme a <code>RestrictionEnzyme</code>.
     */
    private static void registerEnzyme(RestrictionEnzyme enzyme)
    {
        String name = enzyme.getName();
        nameToEnzyme.put(name, enzyme);

        Integer sizeKey = new Integer(enzyme.getRecognitionSite().length());
        if (sizeToCutters.containsKey(sizeKey))
        {
            Set s = (Set) sizeToCutters.get(sizeKey);
            s.add(enzyme);
        }
        else
        {
            Set s = new HashSet();
            s.add(enzyme);
            sizeToCutters.put(sizeKey, s);
        }

        Pattern forward = Pattern.compile(enzyme.getForwardRegex());
        Pattern reverse = Pattern.compile(enzyme.getReverseRegex());
        enzymeToPattern.put(enzyme, new Pattern [] { forward, reverse });

        Annotation annotation = new SmallAnnotation();
        try
        {
            annotation.setProperty("dbxref", "REBASE:" + name);
        }
        catch (ChangeVetoException cve)
        {
            throw new BioError(cve, "Assertion Failure: failed to modify Annotation");
        }

        annotation.addChangeListener(ChangeListener.ALWAYS_VETO);
        enzymeToAnnotation.put(enzyme, annotation);
    }

    private static void loadData(InputStream is)
    {
        nameToEnzyme        = new HashMap();
        nameToIsoschizomers = new HashMap();
        sizeToCutters       = new HashMap();
        enzymeToPattern     = new HashMap();
        enzymeToAnnotation  = new HashMap();

        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            // Basic linesplit parser
            LineSplitParser lsParser = new LineSplitParser();
            lsParser.setEndOfRecord(TagValueParser.EMPTY_LINE_EOR);
            lsParser.setSplitOffset(3);
            lsParser.setContinueOnEmptyTag(true);
            lsParser.setMergeSameTag(true);

            // The end of the chain
            RebaseEnzymeBuilder builder = new RebaseEnzymeBuilder();

            // Create isoschizomer value splitter
            RegexSplitter iso =
                new RegexSplitter(Pattern.compile("([^,]+)"), 1);
            // Create site value splitter
            RegexSplitter site =
                new RegexSplitter(Pattern.compile("(\\(-?\\d/-?\\d+\\)|[A-Za-z^]+)"), 1);

            ChangeTable table = new ChangeTable();
            table.setSplitter(REBASE_TAG_ISZR, iso);
            table.setSplitter(REBASE_TAG_SITE, site);
            ValueChanger changer = new ValueChanger(builder, table);

            // Filter tags
            TagDropper rebaseTags = new TagDropper(changer);
            // Retain the enzyme name
            rebaseTags.addTag(REBASE_TAG_NAME);
            // Retain isoschizomers
            rebaseTags.addTag(REBASE_TAG_ISZR);
            // Retain recognition sequence
            rebaseTags.addTag(REBASE_TAG_SITE);

            Parser parser = new Parser();
            while (parser.read(br, lsParser, rebaseTags))
            {
                continue;
            }

            // Replace isoschizomer names with RestrictionEnzymes
            Map tempMap = new HashMap();
            Set tempSet = null;
            for (Iterator ni = nameToIsoschizomers.keySet().iterator(); ni.hasNext();)
            {
                Object name = ni.next();
                Set isoschizomers = (Set) nameToIsoschizomers.get(name);

                if (isoschizomers.size() == 0)
                    tempSet = Collections.EMPTY_SET;
                else
                    tempSet = (Set) isoschizomers.getClass().newInstance();

                tempMap.put(name, tempSet);

                for (Iterator ii = isoschizomers.iterator(); ii.hasNext();)
                {
                    String isoName = (String) ii.next();
                    tempSet.add(nameToEnzyme.get(isoName));
                }
            }

            nameToIsoschizomers = tempMap;
        }
        catch (Exception e)
        {
            throw new BioError(e, "Failed to read REBASE data file");
        }
    }

    /**
     * <code>RebaseEnzymeBuilder</code> creates enzyme instances and
     * populates the maps.
     */
    private static class RebaseEnzymeBuilder implements TagValueListener
    {
        private String name;
        private Set isoschizomers;
        private List isoBuffer;
        private SymbolList site;
        private int [] usCutPositions;
        private int [] dsCutPositions;

        private String tagState;
        private boolean unknownSite;
        private StringBuffer sb;

        RebaseEnzymeBuilder()
        {
            sb = new StringBuffer();
        }

        public void startRecord() throws ParserException
        {
            isoBuffer = new ArrayList(30);
            site           = null;
            dsCutPositions = null;
            usCutPositions = null;
            unknownSite = false;
        }

        public void endRecord() throws ParserException
        {
            if (! getRecordState())
                return;
            if (unknownSite || site == null)
                return;

            int isoCount = isoBuffer.size();
            if (isoCount < 30)
            {
                isoschizomers = new SmallSet(isoCount);
                for (int i = 0; i < isoCount; i++)
                    isoschizomers.add(isoBuffer.get(i));
            }
            else
            {
                isoschizomers = new HashSet(isoBuffer);
            }

            registerEnzyme(createEnzyme());
            nameToIsoschizomers.put(name, isoschizomers);
        }

        public void startTag(Object tag) throws ParserException
        {
            tagState = (String) tag;
        }

        public void endTag() throws ParserException { }

        public void value(TagValueContext context, Object value)
            throws ParserException
        {
            if (tagState.equals(REBASE_TAG_NAME))
                name = (String) value;
            else if (tagState.equals(REBASE_TAG_ISZR))
                isoBuffer.add(value);
            else if (tagState.equals(REBASE_TAG_SITE))
                processSite(value);
            else
                throw new ParserException("Unable to handle value for tag '"
                                          + tagState
                                          + "'");
        }

        boolean getRecordState()
        {
            return tagState != null;
        }

        RestrictionEnzyme createEnzyme()
        {
            RestrictionEnzyme enzyme = null;

            try
            {
                if (usCutPositions != null)
                {
                    enzyme = new RestrictionEnzyme(name, site,
                                                   usCutPositions[0],
                                                   usCutPositions[1],
                                                   dsCutPositions[0],
                                                   dsCutPositions[1]);
                }
                else
                {
                    enzyme = new RestrictionEnzyme(name, site,
                                                   dsCutPositions[0],
                                                   dsCutPositions[1]);
                }
            }
            catch (IllegalAlphabetException iae)
            {
                throw new BioError(iae, "New DNA SymbolList no longer consists on DNA Alphabet");
            }

            return enzyme;
        }

        private void processSite(Object value) throws ParserException
        {
            sb.setLength(0);
            sb.append((String) value);
            int div, forIdx, revIdx;

            // REBASE marks enzymes whose site is not known with '?'
            if (sb.charAt(0) == '?')
            {
                unknownSite = true;
                return;
            }

            if (sb.charAt(0) == '(')
            {
                // Index separator
                div = sb.indexOf("/");

                try
                {
                    forIdx = Integer.parseInt(sb.substring(1, div));
                    revIdx = Integer.parseInt(sb.substring(div + 1,
                                                           sb.length() - 1));
                }
                catch (NumberFormatException nfe)
                {
                    throw new ParserException(nfe, "Failed to parse cut site index");
                }

                // Indices before the site indicate a double cutter
                if (site == null)
                {
                    usCutPositions = new int [2];
                    usCutPositions[0] = forIdx;
                    usCutPositions[1] = revIdx;
                }
                else
                {
                    dsCutPositions = new int [2];
                    dsCutPositions[0] = forIdx + site.length();
                    dsCutPositions[1] = revIdx + site.length();
                }
            }
            else
            {
                // Explicit cut site marker
                int cut = sb.indexOf("^");
                dsCutPositions = new int [2];

                try
                {
                    if (cut == -1)
                    {
                        site = DNATools.createDNA(sb.substring(0));
                        dsCutPositions[0] = 1;
                        dsCutPositions[1] = 1;
                    }
                    else
                    {
                        sb.deleteCharAt(cut);
                        site = DNATools.createDNA(sb.substring(0));
                        dsCutPositions[0] = cut;
                        dsCutPositions[1] = site.length() - cut;
                    }
                }
                catch (IllegalSymbolException iae)
                {
                    throw new ParserException(iae, "Illegal DNA symbol in recognition site");
                }
            }
        }
    }
}
