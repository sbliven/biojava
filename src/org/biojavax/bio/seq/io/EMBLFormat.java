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

package	org.biojavax.bio.seq.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.ParseException;
import org.biojava.bio.seq.io.SeqIOListener;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.Comment;
import org.biojavax.CrossRef;
import org.biojavax.DocRef;
import org.biojavax.DocRefAuthor;
import org.biojavax.Namespace;
import org.biojavax.Note;
import org.biojavax.RankedCrossRef;
import org.biojavax.RankedDocRef;
import org.biojavax.RichAnnotation;
import org.biojavax.SimpleComment;
import org.biojavax.SimpleCrossRef;
import org.biojavax.SimpleDocRef;
import org.biojavax.SimpleNote;
import org.biojavax.SimpleRankedCrossRef;
import org.biojavax.SimpleRankedDocRef;
import org.biojavax.SimpleRichAnnotation;
import org.biojavax.RichObjectFactory;
import org.biojavax.SimpleDocRefAuthor;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.taxa.NCBITaxon;
import org.biojavax.bio.taxa.SimpleNCBITaxon;
import org.biojavax.ontology.ComparableTerm;
import org.biojavax.utils.StringTools;

/**
 * Format reader for EMBL files. This version of EMBL format will generate
 * and write RichSequence objects. Loosely Based on code from the old, deprecated,
 * org.biojava.bio.seq.io.EmblLikeFormat object.
 *
 * @author Richard Holland
 */
public class EMBLFormat extends RichSequenceFormat.HeaderlessFormat {
    
    /**
     * The name of this format
     */
    public static final String EMBL_FORMAT = "EMBL";
    
    protected static final String LOCUS_TAG = "ID";
    protected static final String ACCESSION_TAG = "AC";
    protected static final String VERSION_TAG = "SV";
    protected static final String DEFINITION_TAG = "DE";
    protected static final String DATE_TAG = "DT";
    protected static final String DATABASE_XREF_TAG = "DR";
    protected static final String SOURCE_TAG = "OS";
    protected static final String ORGANISM_TAG = "OC";
    protected static final String ORGANELLE_TAG = "OG";
    protected static final String REFERENCE_TAG = "RN";
    protected static final String REFERENCE_POSITION_TAG = "RP";
    protected static final String REFERENCE_XREF_TAG = "RX";
    protected static final String AUTHORS_TAG = "RA";
    protected static final String CONSORTIUM_TAG = "RG";
    protected static final String TITLE_TAG = "RT";
    protected static final String LOCATOR_TAG = "RL";
    protected static final String REMARK_TAG = "RC";
    protected static final String KEYWORDS_TAG = "KW";
    protected static final String COMMENT_TAG = "CC";
    protected static final String FEATURE_HEADER_TAG = "FH";
    protected static final String FEATURE_TAG = "FT";
    protected static final String CONTIG_TAG = "CO";
    protected static final String TPA_TAG = "AH";
    protected static final String START_SEQUENCE_TAG = "SQ";
    protected static final String DELIMITER_TAG = "XX";
    protected static final String END_SEQUENCE_TAG = "//";
    
    // the date pattern
    // date (Rel. N, Created)
    // date (Rel. N, Last updated, Version 10)
    protected static final Pattern dp = Pattern.compile("([^\\s]+)\\s+\\(Rel\\.\\s+(\\d+), ([^\\)\\d]+)\\d*\\)$");
    // locus line
    protected static final Pattern lp = Pattern.compile("^(\\S+)\\s+standard;\\s+(circular)?\\s*(\\S+);\\s+(\\S+);\\s+\\d+\\s+BP\\.$");
    // version line
    protected static final Pattern vp = Pattern.compile("^(\\S+?)\\.(\\d+)$");
    // reference position line
    protected static final Pattern rpp = Pattern.compile("^(\\d+)(-(\\d+))?$");
    // dbxref line
    protected static final Pattern dbxp = Pattern.compile("^(\\S+?):(\\S+)$");
    
    /**
     * Implements some EMBL-specific terms.
     */
    public static class Terms extends RichSequence.Terms {
        private static ComparableTerm EMBL_TERM = null;
        
        /**
         * Getter for the EMBL term
         * @return The EMBL Term
         */
        public static ComparableTerm getEMBLTerm() {
            if (EMBL_TERM==null) EMBL_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("EMBL");
            return EMBL_TERM;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean readSequence(BufferedReader reader,
            SymbolTokenization symParser,
            SeqIOListener listener)
            throws IllegalSymbolException, IOException, ParseException {
        if (!(listener instanceof RichSeqIOListener)) throw new IllegalArgumentException("Only accepting RichSeqIOListeners today");
        return this.readRichSequence(reader,symParser,(RichSeqIOListener)listener,null);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean readRichSequence(BufferedReader reader,
            SymbolTokenization symParser,
            RichSeqIOListener rlistener,
            Namespace ns)
            throws IllegalSymbolException, IOException, ParseException {
        
        String line;
        boolean hasAnotherSequence = true;
        //boolean hasInternalWhitespace = false;
        
        rlistener.startSequence();
        
        if (ns==null) ns=RichObjectFactory.getDefaultNamespace();
        rlistener.setNamespace(ns);
        
        // Get an ordered list of key->value pairs in array-tuples
        String sectionKey = null;
        NCBITaxon tax = null;
        String organism = null;
        String accession = null;
        int xrefCount = 0;
        do {
            List section = this.readSection(reader);
            sectionKey = ((String[])section.get(0))[0];
            if(sectionKey == null){
                throw new ParseException("Section key was null. Accession:"+
                        accession == null ? "Not set" : accession);
            }
            // process section-by-section
            if (sectionKey.equals(LOCUS_TAG)) {
                // entryname  dataclass; [circular] molecule; division; sequencelength BP.
                String loc = ((String[])section.get(0))[1];
                Matcher m = lp.matcher(loc);
                if (m.matches()) {
                    rlistener.setName(m.group(1));
                    rlistener.addSequenceProperty(Terms.getMolTypeTerm(),m.group(3));
                    rlistener.setDivision(m.group(4));
                    // Optional extras
                    String circular = m.group(2);
                    if (circular!=null) rlistener.setCircular(true);
                } else {
                    throw new ParseException("Bad ID line found: "+loc);
                }
            } else if (sectionKey.equals(DEFINITION_TAG)) {
                rlistener.setDescription(((String[])section.get(0))[1]);
            } else if (sectionKey.equals(SOURCE_TAG)) {
                // only interested in organelle sub-tag
                for (int i = 1; i < section.size(); i++) {
                    sectionKey = ((String[])section.get(i))[0];
                    if (sectionKey.equals(ORGANELLE_TAG)) {
                        rlistener.addSequenceProperty(Terms.getOrganelleTerm(), ((String[])section.get(i))[1].trim());
                        break; // skip out of for loop once found
                    }
                }
            } else if (sectionKey.equals(DATE_TAG)) {
                String chunk = ((String[])section.get(0))[1].trim();
                Matcher dm = dp.matcher(chunk);
                if (dm.matches()) {
                    String date = dm.group(1);
                    String rel = dm.group(2);
                    String type = dm.group(3);
                    if (type.equals("Created")) {
                        rlistener.addSequenceProperty(Terms.getDateCreatedTerm(), date);
                        rlistener.addSequenceProperty(Terms.getRelCreatedTerm(), rel);
                    } else if (type.equals("Last updated, Version ")) {
                        rlistener.addSequenceProperty(Terms.getDateUpdatedTerm(), date);
                        rlistener.addSequenceProperty(Terms.getRelUpdatedTerm(), rel);
                    } else throw new ParseException("Bad date type found: "+type);
                } else throw new ParseException("Bad date line found: "+chunk);
            } else if (sectionKey.equals(ACCESSION_TAG)) {
                // if multiple accessions, store only first as accession,
                // and store rest in annotation
                String[] accs = ((String[])section.get(0))[1].split(";");
                accession = accs[0].trim();
                rlistener.setAccession(accession);
                for (int i = 1; i < accs.length; i++) {
                    rlistener.addSequenceProperty(Terms.getAdditionalAccessionTerm(),accs[i].trim());
                }
            } else if (sectionKey.equals(VERSION_TAG)) {
                String ver = ((String[])section.get(0))[1];
                Matcher m = vp.matcher(ver);
                if (m.matches()) {
                    rlistener.setVersion(Integer.parseInt(m.group(2)));
                } else {
                    throw new ParseException("Bad version line found: "+ver);
                }
            } else if (sectionKey.equals(KEYWORDS_TAG)) {
                String val = ((String[])section.get(0))[1];
                val = val.substring(0,val.length()-1); // chomp dot
                String[] kws = val.split(";");
                for (int i = 1; i < kws.length; i++) {
                    rlistener.addSequenceProperty(Terms.getKeywordTerm(), kws[i].trim());
                }
            } else if (sectionKey.equals(DATABASE_XREF_TAG)) {
                String val = ((String[])section.get(0))[1];
                val = val.substring(0,val.length()-1); // chomp dot
                // database_identifier; primary_identifier; secondary_identifier....
                String[] parts = val.split(";");
                // construct a DBXREF out of the dbname part[0] and accession part[1]
                CrossRef crossRef = (CrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{parts[0].trim(),parts[1].trim(), new Integer(0)});
                // assign remaining bits of info as annotations
                for (int j = 2; j < parts.length; j++) {
                    Note note = new SimpleNote(Terms.getAdditionalAccessionTerm(),parts[j].trim(),j);
                    try {
                        ((RichAnnotation)crossRef.getAnnotation()).addNote(note);
                    } catch (ChangeVetoException ce) {
                        ParseException pe = new ParseException("Could not annotate identifier terms");
                        pe.initCause(ce);
                        throw pe;
                    }
                }
                RankedCrossRef rcrossRef = new SimpleRankedCrossRef(crossRef, ++xrefCount);
                rlistener.setRankedCrossRef(rcrossRef);
            } else if (sectionKey.equals(REFERENCE_TAG) && !this.getElideReferences()) {
                // first line of section has rank and location
                String refrank = ((String[])section.get(0))[1];
                int ref_rank = Integer.parseInt(refrank.substring(1,refrank.length()-1));
                int ref_start = -999;
                int ref_end = -999;
                // rest can be in any order
                String consortium = null;
                String authors = null;
                String title = null;
                String locator = null;
                String pubmed = null;
                String medline = null;
                String doi = null;
                String remark = null;
                for (int i = 1; i < section.size(); i++) {
                    String key = ((String[])section.get(i))[0];
                    String val = ((String[])section.get(i))[1];
                    if (key.equals(AUTHORS_TAG)) {
                        val = val.substring(0,val.length()-1); // chomp semicolon
                        authors = val;
                    }
                    if (key.equals(CONSORTIUM_TAG)) {
                        val = val.substring(0,val.length()-1); // chomp semicolon
                        consortium = val;
                    }
                    if (key.equals(TITLE_TAG)) {
                        if (val.length()>1) {
                            val = val.substring(1,val.length()-3); // chomp semicolon + quotes
                            title = val;
                        } else title=null; // single semi-colon indicates no title
                    }
                    if (key.equals(LOCATOR_TAG)) {
                        if (val.charAt(val.length()-1)=='.') val = val.substring(0,val.length()-1); // chomp dot
                        locator = val;
                    }
                    if (key.equals(REFERENCE_XREF_TAG)) {
                        // database_identifier; primary_identifier.
                        String[] refs = val.split("\\.");
                        for (int j = 0 ; j < refs.length; j++) {
                            if (refs[j].trim().length()==0) continue;
                            String[] parts = refs[j].split(";");
                            String db = parts[0].toUpperCase();
                            String ref = parts[1];
                            if (db.equals(Terms.PUBMED_KEY)) pubmed = ref;
                            else if (db.equals(Terms.MEDLINE_KEY)) medline = ref;
                            else if (db.equals(Terms.DOI_KEY)) doi = ref;
                        }
                    }
                    if (key.equals(REMARK_TAG)) remark = val;
                    if (key.equals(REFERENCE_POSITION_TAG)) {
                        // only the first group is taken
                        // if we have multiple lines, only the last line is taken
                        Matcher m = rpp.matcher(val);
                        if (m.matches()) {
                            ref_start = Integer.parseInt(m.group(1));
                            if(m.group(2) != null)
                                ref_end = Integer.parseInt(m.group(3));
                        } else {
                            throw new ParseException("Bad reference line found: "+val);
                        }
                    }
                }
                // create the pubmed crossref and assign to the bioentry
                CrossRef pcr = null;
                if (pubmed!=null) {
                    pcr = (CrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{Terms.PUBMED_KEY, pubmed, new Integer(0)});
                    RankedCrossRef rpcr = new SimpleRankedCrossRef(pcr, 0);
                    rlistener.setRankedCrossRef(rpcr);
                }
                // create the medline crossref and assign to the bioentry
                CrossRef mcr = null;
                if (medline!=null) {
                    mcr = (CrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{Terms.MEDLINE_KEY, medline, new Integer(0)});
                    RankedCrossRef rmcr = new SimpleRankedCrossRef(mcr, 0);
                    rlistener.setRankedCrossRef(rmcr);
                }
                // create the doi crossref and assign to the bioentry
                CrossRef dcr = null;
                if (doi!=null) {
                    dcr = (CrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{Terms.DOI_KEY, doi, new Integer(0)});
                    RankedCrossRef rdcr = new SimpleRankedCrossRef(dcr, 0);
                    rlistener.setRankedCrossRef(rdcr);
                }
                // create the docref object
                try {
                    List authSet = DocRefAuthor.Tools.parseAuthorString(authors);
                    if (consortium!=null) authSet.add(new SimpleDocRefAuthor(consortium, true, false));
                    DocRef dr = (DocRef)RichObjectFactory.getObject(SimpleDocRef.class,new Object[]{authSet,locator});
                    if (title!=null) dr.setTitle(title);
                    // assign either the pubmed or medline to the docref - medline gets priority, then pubmed, then doi
                    if (mcr!=null) dr.setCrossref(mcr);
                    else if (pcr!=null) dr.setCrossref(pcr);
                    else if (dcr!=null) dr.setCrossref(dcr);
                    // assign the remarks
                    if (!this.getElideComments()) dr.setRemark(remark);
                    // assign the docref to the bioentry
                    RankedDocRef rdr = new SimpleRankedDocRef(dr,
                            (ref_start != -999 ? new Integer(ref_start) : null),
                            (ref_end != -999 ? new Integer(ref_end) : null),
                            ref_rank);
                    rlistener.setRankedDocRef(rdr);
                } catch (ChangeVetoException e) {
                    throw new ParseException(e);
                }
            } else if (sectionKey.equals(COMMENT_TAG) && !this.getElideComments()) {
                // Set up some comments
                rlistener.setComment(((String[])section.get(0))[1]);
            } else if (sectionKey.equals(FEATURE_TAG) && !this.getElideFeatures()) {
                // starting from second line of input, start a new feature whenever we come across
                // a key that does not start with /
                boolean seenAFeature = false;
                for (int i = 1 ; i < section.size(); i++) {
                    String key = ((String[])section.get(i))[0];
                    String val = ((String[])section.get(i))[1];
                    if (key.startsWith("/")) {
                        key = key.substring(1); // strip leading slash
                        val = val.replaceAll("\"","").trim(); // strip quotes
                        // parameter on old feature
                        if (key.equals("db_xref")) {
                            Matcher m = dbxp.matcher(val);
                            if (m.matches()) {
                                String dbname = m.group(1);
                                String raccession = m.group(2);
                                if (dbname.equals("taxon")) {
                                    // Set the Taxon instead of a dbxref
                                    tax = (NCBITaxon)RichObjectFactory.getObject(SimpleNCBITaxon.class, new Object[]{Integer.valueOf(raccession)});
                                    rlistener.setTaxon(tax);
                                    try {
                                        if (organism!=null) tax.addName(NCBITaxon.SCIENTIFIC,organism);
                                    } catch (ChangeVetoException e) {
                                        throw new ParseException(e);
                                    }
                                } else {
                                    try {
                                        CrossRef cr = (CrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{dbname, raccession, new Integer(0)});
                                        RankedCrossRef rcr = new SimpleRankedCrossRef(cr, 0);
                                        rlistener.getCurrentFeature().addRankedCrossRef(rcr);
                                    } catch (ChangeVetoException e) {
                                        throw new ParseException(e);
                                    }
                                }
                            } else {
                                throw new ParseException("Bad dbxref found: "+val);
                            }
                        } else if (key.equals("organism")) {
                            try {
                                organism = val;
                                if (tax!=null) tax.addName(NCBITaxon.SCIENTIFIC,organism);
                            } catch (ChangeVetoException e) {
                                throw new ParseException(e);
                            }
                        } else {
                            if (key.equals("translation")) {
                                // strip spaces from sequence
                                val = val.replaceAll("\\s+","");
                            }
                            rlistener.addFeatureProperty(RichObjectFactory.getDefaultOntology().getOrCreateTerm(key),val);
                        }
                    } else {
                        // new feature!
                        // end previous feature
                        if (seenAFeature) rlistener.endFeature();
                        // start next one, with lots of lovely info in it
                        RichFeature.Template templ = new RichFeature.Template();
                        templ.annotation = new SimpleRichAnnotation();
                        templ.sourceTerm = Terms.getEMBLTerm();
                        templ.typeTerm = RichObjectFactory.getDefaultOntology().getOrCreateTerm(key);
                        templ.featureRelationshipSet = new TreeSet();
                        templ.rankedCrossRefs = new TreeSet();
                        String tidyLocStr = val.replaceAll("\\s+","");
                        templ.location = GenbankLocationParser.parseLocation(ns, accession, tidyLocStr);
                        rlistener.startFeature(templ);
                        seenAFeature = true;
                    }
                }
                if (seenAFeature) rlistener.endFeature();
            } else if (sectionKey.equals(START_SEQUENCE_TAG) && !this.getElideSymbols()) {
                StringBuffer seq = new StringBuffer();
                for (int i = 0 ; i < section.size(); i++) seq.append(((String[])section.get(i))[1]);
                try {
                    SymbolList sl = new SimpleSymbolList(symParser,
                            seq.toString().replaceAll("\\s+","").replaceAll("[\\.|~]","-"));
                    rlistener.addSymbols(symParser.getAlphabet(),
                            (Symbol[])(sl.toList().toArray(new Symbol[0])),
                            0, sl.length());
                } catch (Exception e) {
                    throw new ParseException(e);
                }
            }
        } while (!sectionKey.equals(END_SEQUENCE_TAG));
        
        // Allows us to tolerate trailing whitespace without
        // thinking that there is another Sequence to follow
        while (true) {
            reader.mark(1);
            int c = reader.read();
            if (c == -1) {
                hasAnotherSequence = false;
                break;
            }
            if (Character.isWhitespace((char) c)) {
                //hasInternalWhitespace = true;
                continue;
            }
            //if (hasInternalWhitespace)
            //    System.err.println("Warning: whitespace found between sequence entries");
            reader.reset();
            break;
        }
        
        // Finish up.
        rlistener.endSequence();
        return hasAnotherSequence;
    }
    
    // reads an indented section, combining split lines and creating a list of key->value tuples
    private List readSection(BufferedReader br) throws ParseException {
        List section = new ArrayList();
        String line;
        String currKey = null;
        StringBuffer currVal = new StringBuffer();
        boolean done = false;
        int linecount = 0;
        
        // while not done
        try {
            while (!done) {
                // mark buffer
                br.mark(160);
                // read token
                line = br.readLine();
                if (line.length()<2) throw new ParseException("Bad line found: "+line);
                String token = line.substring(0,2);
                // READ SEQUENCE SECTION
                if (token.equals(START_SEQUENCE_TAG)) {
                    //      from next line, read sequence until // - leave // on stack
                    StringBuffer sb = new StringBuffer();
                    while (!done) {
                        br.mark(160);
                        line = br.readLine();
                        if (line.substring(0,2).equals(END_SEQUENCE_TAG)) {
                            br.reset();
                            done = true;
                        } else {
                            //      create sequence tag->value pair to return, sans numbers
                            sb.append(line.replaceAll("\\d",""));
                        }
                    }
                    section.add(new String[]{START_SEQUENCE_TAG,sb.toString()});
                }
                // READ FEATURE TABLE SECTION
                else if (token.equals(FEATURE_HEADER_TAG)) {
                    //      create dummy feature tag->value pair and add to return set
                    section.add(new String[]{FEATURE_TAG,null});
                    //      drop next FH line
                    line = br.readLine(); // skip next line too - it is also FH
                    //      read all FT lines until XX
                    String currentTag = null;
                    StringBuffer currentVal = null;
                    while (!done) {
                        line = br.readLine();
                        if (line.substring(0,2).equals(DELIMITER_TAG)) {
                            done = true;
                            // dump current tag if exists
                            if (currentTag!=null) section.add(new String[]{currentTag,currentVal.toString()});
                        } else {
                            //         FT lines:   FT   word            value
                            //         or          FT                   /word
                            //         or          FT                   /db_xref="taxon:3899....
                            //                                          ......"
                            line = line.substring(5); // chomp off "FT   "
                            if (line.charAt(0)!=' ') {
                                // case 1 : word value - splits into key-value on its own
                                section.add(line.split("\\s+"));
                            } else {
                                line = line.trim();
                                if (line.charAt(0)=='/') {
                                    // dump current tag if exists
                                    if (currentTag!=null) section.add(new String[]{currentTag,currentVal.toString()});
                                    // case 2 : /word[=.....]
                                    String[] parts = line.split("=");
                                    currentTag = parts[0];
                                    currentVal = new StringBuffer();
                                    currentVal.append(parts[1]);
                                } else {
                                    // case 3 : ...."
                                    currentVal.append("\n");
                                    currentVal.append(line);
                                }
                            }
                        }
                    }
                }
                // READ END OF SEQUENCE
                else if (token.equals(END_SEQUENCE_TAG)) {
                    section.add(new String[]{END_SEQUENCE_TAG,null});
                    done = true;
                }
                // READ DELIMITER TAG
                else if (token.equals(DELIMITER_TAG)) {
                    section.add(new String[]{DELIMITER_TAG,null});
                    done = true;
                }
                // READ THIRD PARTY ANNOTATION SECTION
                else if (token.equals(TPA_TAG)) {
                    //      exception = don't know how to do TPA yet
                    throw new ParseException("Unable to handle TPAs just yet");
                }
                // READ CONTIG SECTION
                else if (token.equals(CONTIG_TAG)) {
                    //      exception = don't know how to do contigs yet
                    throw new ParseException("Unable to handle contig assemblies just yet");
                }
                // READ DOCREF
                else if (token.equals(DATABASE_XREF_TAG)) {
                    section.add(new String[]{DATABASE_XREF_TAG,line.substring(5).trim()});
                    done = true;
                }
                // READ DATE
                else if (token.equals(DATE_TAG)) {
                    section.add(new String[]{DATE_TAG,line.substring(5).trim()});
                    done = true;
                }
                // READ NORMAL TAG/VALUE SECTION
                else {
                    //      rewind buffer to mark
                    br.reset();
                    //      read token/values until XX
                    String currentTag = null;
                    StringBuffer currentVal = null;
                    while (!done) {
                        line = br.readLine();
                        if (line.substring(0,2).equals(DELIMITER_TAG)) {
                            done = true;
                            // dump current tag if exists
                            if (currentTag!=null) section.add(new String[]{currentTag,currentVal.toString()});
                        } else {
                            //      merge neighbouring repeated tokens by concatting values
                            //      return tag->value pairs
                            String tag = line.substring(0,2);
                            String value = line.substring(5);
                            if (currentTag==null || !tag.equals(currentTag)) {
                                // dump current tag if exists
                                if (currentTag!=null) section.add(new String[]{currentTag,currentVal.toString()});
                                // start new tag
                                currentTag = tag;
                                currentVal = new StringBuffer();
                                currentVal.append(value);
                            } else {
                                currentVal.append("\n");
                                currentVal.append(value);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ParseException(e);
        }
        return section;
    }
    
    /**
     * {@inheritDoc}
     */
    public void	writeSequence(Sequence seq, PrintStream os) throws IOException {
        if (this.getPrintStream()==null) this.setPrintStream(os);
        this.writeSequence(seq, RichObjectFactory.getDefaultNamespace());
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeSequence(Sequence seq, String format, PrintStream os) throws IOException {
        if (this.getPrintStream()==null) this.setPrintStream(os);
        if (!format.equals(this.getDefaultFormat())) throw new IllegalArgumentException("Unknown format: "+format);
        this.writeSequence(seq, RichObjectFactory.getDefaultNamespace());
    }
    
    /**
     * {@inheritDoc}
     * Namespace is ignored as EMBL has no concept of it.
     */
    public void writeSequence(Sequence seq, Namespace ns) throws IOException {
        RichSequence rs;
        try {
            if (seq instanceof RichSequence) rs = (RichSequence)seq;
            else rs = RichSequence.Tools.enrich(seq);
        } catch (ChangeVetoException e) {
            IOException e2 = new IOException("Unable to enrich sequence");
            e2.initCause(e);
            throw e2;
        }
        
        SymbolTokenization tok;
        try {
            tok = rs.getAlphabet().getTokenization("token");
        } catch (Exception e) {
            throw new RuntimeException("Unable to get alphabet tokenizer",e);
        }
        
        Set notes = rs.getNoteSet();
        String accession = rs.getAccession();
        String accessions = accession+";";
        String cdat = null;
        String udat = null;
        String crel = null;
        String urel = null;
        String organelle = null;
        String moltype = rs.getAlphabet().getName();
        for (Iterator i = notes.iterator(); i.hasNext(); ) {
            Note n = (Note)i.next();
            if (n.getTerm().equals(Terms.getDateCreatedTerm())) cdat=n.getValue();
            else if (n.getTerm().equals(Terms.getDateUpdatedTerm())) udat=n.getValue();
            else if (n.getTerm().equals(Terms.getRelCreatedTerm())) crel=n.getValue();
            else if (n.getTerm().equals(Terms.getRelUpdatedTerm())) urel=n.getValue();
            else if (n.getTerm().equals(Terms.getMolTypeTerm())) moltype=n.getValue();
            else if (n.getTerm().equals(Terms.getAdditionalAccessionTerm())) accessions = accessions+" "+n.getValue()+";";
            else if (n.getTerm().equals(Terms.getOrganelleTerm())) organelle=n.getValue();
        }
        
        // entryname  dataclass; [circular] molecule; division; sequencelength BP.
        StringBuffer locusLine = new StringBuffer();
        locusLine.append(StringTools.rightPad(rs.getName(),9));
        locusLine.append(" standard; ");
        locusLine.append(rs.getCircular()?"circular ":"");
        locusLine.append(moltype);
        locusLine.append("; ");
        locusLine.append(rs.getDivision()==null?"":rs.getDivision());
        locusLine.append("; ");
        locusLine.append(rs.length());
        locusLine.append(" BP.");
        StringTools.writeKeyValueLine(LOCUS_TAG, locusLine.toString(), 5, this.getLineWidth(), null, LOCUS_TAG, this.getPrintStream());
        this.getPrintStream().println(DELIMITER_TAG+"   ");
        
        // accession line
        StringTools.writeKeyValueLine(ACCESSION_TAG, accessions, 5, this.getLineWidth(), null, ACCESSION_TAG, this.getPrintStream());
        this.getPrintStream().println(DELIMITER_TAG+"   ");
        
        // version line
        StringTools.writeKeyValueLine(VERSION_TAG, accession+"."+rs.getVersion(), 5, this.getLineWidth(), null, VERSION_TAG, this.getPrintStream());
        this.getPrintStream().println(DELIMITER_TAG+"   ");
        
        // date line
        StringTools.writeKeyValueLine(DATE_TAG, (cdat==null?udat:cdat)+" (Rel. "+(crel==null?"0":crel)+" Created)", 5, this.getLineWidth(), null, DATE_TAG, this.getPrintStream());
        StringTools.writeKeyValueLine(DATE_TAG, udat+" (Rel. "+(urel==null?"0":urel)+", Last updated, Version "+rs.getVersion()+")", 5, this.getLineWidth(), null, DATE_TAG, this.getPrintStream());
        this.getPrintStream().println(DELIMITER_TAG+"   ");
        
        // definition line
        StringTools.writeKeyValueLine(DEFINITION_TAG, rs.getDescription(), 5, this.getLineWidth(), null, DEFINITION_TAG, this.getPrintStream());
        this.getPrintStream().println(DELIMITER_TAG+"   ");
        
        // keywords line
        String keywords = null;
        for (Iterator n = notes.iterator(); n.hasNext(); ) {
            Note nt = (Note)n.next();
            if (nt.getTerm().equals(Terms.getKeywordTerm())) {
                if (keywords==null) keywords = nt.getValue();
                else keywords = keywords+"; "+nt.getValue();
            }
        }
        if (keywords!=null) {
            StringTools.writeKeyValueLine(KEYWORDS_TAG, keywords+".", 5, this.getLineWidth(), null, KEYWORDS_TAG, this.getPrintStream());
            this.getPrintStream().println(DELIMITER_TAG+"   ");
        }
        
        // source line (from taxon)
        //   organism line
        NCBITaxon tax = rs.getTaxon();
        if (tax!=null) {
            StringTools.writeKeyValueLine(SOURCE_TAG, tax.getDisplayName(), 5, this.getLineWidth(), null, SOURCE_TAG, this.getPrintStream());
            StringTools.writeKeyValueLine(ORGANISM_TAG, tax.getNameHierarchy(), 5, this.getLineWidth(), null, SOURCE_TAG, this.getPrintStream());
            if (organelle!=null) StringTools.writeKeyValueLine(ORGANELLE_TAG, organelle, 5, this.getLineWidth(), null, ORGANELLE_TAG, this.getPrintStream());
            this.getPrintStream().println(DELIMITER_TAG+"   ");
        }
        
        // references - rank (bases x to y)
        for (Iterator r = rs.getRankedDocRefs().iterator(); r.hasNext(); ) {
            RankedDocRef rdr = (RankedDocRef)r.next();
            DocRef d = rdr.getDocumentReference();
            // RN, RC, RP, RX, RG, RA, RT, RL
            StringTools.writeKeyValueLine(REFERENCE_TAG, "["+rdr.getRank()+"]", 5, this.getLineWidth(), null, REFERENCE_TAG, this.getPrintStream());
            StringTools.writeKeyValueLine(REMARK_TAG, d.getRemark(), 5, this.getLineWidth(), null, REMARK_TAG, this.getPrintStream());
            Integer rstart = rdr.getStart();
            if (rstart==null) rstart = new Integer(1);
            Integer rend = rdr.getEnd();
            if (rend==null) rend = new Integer(rs.length());
            StringTools.writeKeyValueLine(REFERENCE_POSITION_TAG, rstart+"-"+rend, 5, this.getLineWidth(), null, REFERENCE_POSITION_TAG, this.getPrintStream());
            CrossRef c = d.getCrossref();
            if (c!=null) StringTools.writeKeyValueLine(REFERENCE_XREF_TAG, c.getDbname().toUpperCase()+"; "+c.getAccession()+".", 5, this.getLineWidth(), null, REFERENCE_XREF_TAG, this.getPrintStream());
            List auths = d.getAuthorList();
            for (Iterator j = auths.iterator(); j.hasNext(); ) {
                DocRefAuthor a = (DocRefAuthor)j.next();
                if (a.isConsortium()) StringTools.writeKeyValueLine(CONSORTIUM_TAG, a+";", 5, this.getLineWidth(), null, CONSORTIUM_TAG, this.getPrintStream());
                else j.remove();
            }
            if (!auths.isEmpty()) StringTools.writeKeyValueLine(AUTHORS_TAG, DocRefAuthor.Tools.generateAuthorString(auths)+";", 5, this.getLineWidth(), null, AUTHORS_TAG, this.getPrintStream());
            if (d.getTitle()!=null && !d.getTitle().equals("")) StringTools.writeKeyValueLine(TITLE_TAG, "\""+d.getTitle()+"\";", 5, this.getLineWidth(), null, TITLE_TAG, this.getPrintStream());
            else StringTools.writeKeyValueLine(TITLE_TAG, ";", 5, this.getLineWidth(), null, TITLE_TAG, this.getPrintStream());
            StringTools.writeKeyValueLine(LOCATOR_TAG, d.getLocation()+".", 5, this.getLineWidth(), null, LOCATOR_TAG, this.getPrintStream());
            this.getPrintStream().println(DELIMITER_TAG+"   ");
        }
        
        // db references - ranked
        for (Iterator r = rs.getRankedCrossRefs().iterator(); r.hasNext(); ) {
            RankedCrossRef rcr = (RankedCrossRef)r.next();
            CrossRef c = rcr.getCrossRef();
            Set noteset = c.getNoteSet();
            StringBuffer sb = new StringBuffer();
            sb.append(c.getDbname().toUpperCase());
            sb.append("; ");
            sb.append(c.getAccession());
            boolean hasSecondary = false;
            for (Iterator i = noteset.iterator(); i.hasNext(); ) {
                Note n = (Note)i.next();
                if (n.getTerm().equals(Terms.getAdditionalAccessionTerm())) {
                    sb.append("; ");
                    sb.append(n.getValue());
                    hasSecondary = true;
                }
            }
            if (!hasSecondary) sb.append("; -");
            sb.append(".");
            StringTools.writeKeyValueLine(DATABASE_XREF_TAG, sb.toString(), 5, this.getLineWidth(), null, DATABASE_XREF_TAG, this.getPrintStream());
        }
        this.getPrintStream().println(DELIMITER_TAG+"   ");
        
        // comments - if any
        if (!rs.getComments().isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (Iterator i = rs.getComments().iterator(); i.hasNext(); ) {
                Comment c = (SimpleComment)i.next();
                sb.append(c.getComment());
                if (i.hasNext()) sb.append("\n");
            }
            StringTools.writeKeyValueLine(COMMENT_TAG, sb.toString(), 5, this.getLineWidth(), null, COMMENT_TAG, this.getPrintStream());
            this.getPrintStream().println(DELIMITER_TAG+"   ");
        }
        
        this.getPrintStream().println(FEATURE_HEADER_TAG+"   Key             Location/Qualifiers");
        this.getPrintStream().println(FEATURE_HEADER_TAG+"   ");
        // feature_type     location
        for (Iterator i = rs.getFeatureSet().iterator(); i.hasNext(); ) {
            RichFeature f = (RichFeature)i.next();
            StringTools.writeKeyValueLine(FEATURE_TAG+"   "+f.getTypeTerm().getName(), GenbankLocationParser.writeLocation((RichLocation)f.getLocation()), 21, this.getLineWidth(), ",", FEATURE_TAG, this.getPrintStream());
            for (Iterator j = f.getNoteSet().iterator(); j.hasNext(); ) {
                Note n = (Note)j.next();
                // /key="val" or just /key if val==""
                if (n.getValue()==null || n.getValue().equals("")) StringTools.writeKeyValueLine(FEATURE_TAG, "/"+n.getTerm().getName(), 21, this.getLineWidth(), null, FEATURE_TAG, this.getPrintStream());
                else StringTools.writeKeyValueLine(FEATURE_TAG, "/"+n.getTerm().getName()+"=\""+n.getValue()+"\"", 21, this.getLineWidth(), null, FEATURE_TAG, this.getPrintStream());
            }
            // add-in to source feature only db_xref="taxon:xyz" where present
            if (f.getType().equals("source") && tax!=null) {
                StringTools.writeKeyValueLine(FEATURE_TAG, "/db_xref=\"taxon:"+tax.getNCBITaxID()+"\"", 21, this.getLineWidth(), null, FEATURE_TAG, this.getPrintStream());
            }
            // add-in other dbxrefs where present
            for (Iterator j = f.getRankedCrossRefs().iterator(); j.hasNext(); ) {
                RankedCrossRef rcr = (RankedCrossRef)j.next();
                CrossRef cr = rcr.getCrossRef();
                StringTools.writeKeyValueLine(FEATURE_TAG, "/db_xref=\"taxon:"+cr.getDbname()+":"+cr.getAccession()+"\"", 21, this.getLineWidth(), null, FEATURE_TAG, this.getPrintStream());
            }
        }
        this.getPrintStream().println(DELIMITER_TAG+"   ");
        
        // SQ   Sequence 1859 BP; 609 A; 314 C; 355 G; 581 T; 0 other;
        int aCount = 0;
        int cCount = 0;
        int gCount = 0;
        int tCount = 0;
        int oCount = 0;
        for (int i = 1; i <= rs.length(); i++) {
            char c;
            try {
                c = tok.tokenizeSymbol(rs.symbolAt(i)).charAt(0);
            } catch (Exception e) {
                throw new RuntimeException("Unable to get symbol at position "+i,e);
            }
            switch (c) {
                case 'a': case 'A':
                    aCount++;
                    break;
                case 'c': case 'C':
                    cCount++;
                    break;
                case 'g': case 'G':
                    gCount++;
                    break;
                case 't': case 'T':
                    tCount++;
                    break;
                default:
                    oCount++;
            }
        }
        this.getPrintStream().print(START_SEQUENCE_TAG+"   "+rs.length()+" BP; ");
        this.getPrintStream().print(aCount + " A; ");
        this.getPrintStream().print(cCount + " C; ");
        this.getPrintStream().print(gCount + " G; ");
        this.getPrintStream().print(tCount + " T; ");
        this.getPrintStream().println(oCount + " other;");
        
        // sequence stuff
        Symbol[] syms = (Symbol[])rs.toList().toArray(new Symbol[0]);
        int lineLen = 0;
        int symCount = 0;
        this.getPrintStream().print("    ");
        for (int i = 0; i < syms.length; i++) {
            if (symCount % 60 == 0 && symCount>0) {
                this.getPrintStream().print(StringTools.leftPad(""+symCount,10));
                this.getPrintStream().print("\n    ");
                lineLen = 0;
            }
            if (symCount % 10 == 0) {
                this.getPrintStream().print(" ");
                lineLen++;
            }
            try {
                this.getPrintStream().print(tok.tokenizeSymbol(syms[i]));
            } catch (IllegalSymbolException e) {
                throw new RuntimeException("Found illegal symbol: "+syms[i]);
            }
            symCount++;
            lineLen++;
        }
        this.getPrintStream().print(StringTools.leftPad(""+symCount,(66-lineLen)+10));
        this.getPrintStream().print("\n");
        this.getPrintStream().println(END_SEQUENCE_TAG);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultFormat() {
        return EMBL_FORMAT;
    }
}

