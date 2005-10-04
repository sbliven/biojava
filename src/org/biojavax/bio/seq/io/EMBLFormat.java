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
public class EMBLFormat implements RichSequenceFormat {
    
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
    protected static final String REFERENCE_TAG = "RN";
    protected static final String REFERENCE_POSITION_TAG = "RP";
    protected static final String REFERENCE_XREF_TAG = "RX";
    protected static final String AUTHORS_TAG = "RA";
    protected static final String TITLE_TAG = "RT";
    protected static final String JOURNAL_TAG = "RL";
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
    
    /**
     * Implements some EMBL-specific terms.
     */
    public static class Terms extends RichSequenceFormat.Terms {
        private static ComparableTerm EMBL_TERM = null;
        private static ComparableTerm DATES_TERM = null;
        
        /**
         * Getter for the EMBL term
         * @return The EMBL Term
         */
        public static ComparableTerm getEMBLTerm() {
            if (EMBL_TERM==null) EMBL_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("EMBL");
            return EMBL_TERM;
        }
        
        /**
         * getter for the Dates term
         * @return a Term that represents the misc date info from EMBL or UniProt files
         */
        public static ComparableTerm getDatesTerm() {
            if (DATES_TERM==null) DATES_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("EMBLDATE");
            return DATES_TERM;
        }
    }
    
    private int lineWidth = 80;
    
    /**
     * {@inheritDoc}
     */
    public int getLineWidth() {
        return lineWidth;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setLineWidth(int width) {
        this.lineWidth = width;
    }
    
    private boolean elideSymbols = false;
    
    /**
     * {@inheritDoc}
     */
    public boolean getElideSymbols() {
        return elideSymbols;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setElideSymbols(boolean elideSymbols) {
        this.elideSymbols = elideSymbols;
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
        boolean hasInternalWhitespace = false;
        
        rlistener.startSequence();
        
        if (ns==null) ns=RichObjectFactory.getDefaultNamespace();
        rlistener.setNamespace(ns);
        
        // Get an ordered list of key->value pairs in array-tuples
        String sectionKey = null;
        NCBITaxon tax = null;
        String organism = null;
        String accession = null;
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
                String regex = "^(\\S+)\\s+standard;\\s+(circular)?\\s*(\\S+);\\s+(\\S+);\\s+\\d+\\s+BP\\.$";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(loc);
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
                // IGNORE - can get from first feature in feature table
            } else if (sectionKey.equals(DATE_TAG)) {
                String value = ((String[])section.get(0))[1];
                rlistener.addSequenceProperty(Terms.getDatesTerm(), value);
                String date = value.substring(0,11);
                rlistener.addSequenceProperty(Terms.getModificationTerm(), date);
            } else if (sectionKey.equals(ACCESSION_TAG)) {
                // if multiple accessions, store only first as accession,
                // and store rest in annotation
                String[] accs = ((String[])section.get(0))[1].split(";");
                accession = accs[0].trim();
                rlistener.setAccession(accession);
                for (int i = 1; i < accs.length; i++) {
                    rlistener.addSequenceProperty(Terms.getAccessionTerm(),accs[i].trim());
                }
            } else if (sectionKey.equals(VERSION_TAG)) {
                String ver = ((String[])section.get(0))[1];
                String regex = "^(\\S+?)\\.(\\d+)$";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(ver);
                if (m.matches()) {
                    rlistener.setVersion(Integer.parseInt(m.group(2)));
                } else {
                    throw new ParseException("Bad version line found: "+ver);
                }
            } else if (sectionKey.equals(KEYWORDS_TAG)) {
                String[] kws = ((String[])section.get(0))[1].split(";");
                for (int i = 1; i < kws.length; i++) {
                    rlistener.addSequenceProperty(Terms.getKeywordsTerm(), kws[i].trim());
                }
            } else if (sectionKey.equals(DATABASE_XREF_TAG)) {
                // database_identifier; primary_identifier; secondary_identifier.
                String[] refs = ((String[])section.get(0))[1].split("\\.");
                for (int i = 0 ; i < refs.length; i++) {
                    if (refs[i].trim().length()==0) continue;
                    String[] parts = refs[i].split(";");
                    // construct a DBXREF out of the dbname part[0] and accession part[1]
                    CrossRef crossRef = new SimpleCrossRef(parts[0].trim(),parts[1].trim(),0);
                    // assign remaining bits of info as annotations
                    for (int j = 2; j < parts.length; j++) {
                        Note note = new SimpleNote(Terms.getIdentifierTerm(),parts[j].trim(),j);
                        try {
                            ((RichAnnotation)crossRef.getAnnotation()).addNote(note);
                        } catch (ChangeVetoException ce) {
                            ParseException pe = new ParseException("Could not annotate identifier terms");
                            pe.initCause(ce);
                            throw pe;
                        }
                    }
                    RankedCrossRef rcrossRef = new SimpleRankedCrossRef(crossRef, i+1);
                    rlistener.setRankedCrossRef(rcrossRef);
                }
            } else if (sectionKey.equals(REFERENCE_TAG)) {
                // first line of section has rank and location
                String refrank = ((String[])section.get(0))[1];
                int ref_rank = Integer.parseInt(refrank.substring(1,refrank.length()-1));
                int ref_start = -999;
                int ref_end = -999;
                // rest can be in any order
                String authors = null;
                String title = null;
                String journal = null;
                String pubmed = null;
                String medline = null;
                String doi = null;
                String remark = null;
                for (int i = 1; i < section.size(); i++) {
                    String key = ((String[])section.get(i))[0];
                    String val = ((String[])section.get(i))[1];
                    if (key.equals(AUTHORS_TAG)) authors = val;
                    if (key.equals(TITLE_TAG)) title = val;
                    if (key.equals(JOURNAL_TAG)) journal = val;
                    if (key.equals(REFERENCE_XREF_TAG)) {
                        // database_identifier; primary_identifier.
                        String[] refs = val.split("\\.");
                        for (int j = 0 ; j < refs.length; j++) {
                            if (refs[j].trim().length()==0) continue;
                            String[] parts = refs[j].split(";");
                            String db = parts[0].toUpperCase();
                            String ref = parts[1];
                            if (db.equals("PUBMED")) pubmed = ref;
                            else if (db.equals("MEDLINE")) medline = ref;
                            else if (db.equals("DOI")) doi = ref;
                        }
                    }
                    if (key.equals(REMARK_TAG)) remark = val;
                    if (key.equals(REFERENCE_POSITION_TAG)) {
                        // only the first group is taken
                        // if we have multiple lines, only the last line is taken
                        String regex = "^(\\d+)(-(\\d+))?$";
                        Pattern p = Pattern.compile(regex);
                        Matcher m = p.matcher(val);
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
                    pcr = (CrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{Terms.PUBMED_KEY, pubmed});
                    RankedCrossRef rpcr = new SimpleRankedCrossRef(pcr, 0);
                    rlistener.setRankedCrossRef(rpcr);
                }
                // create the medline crossref and assign to the bioentry
                CrossRef mcr = null;
                if (medline!=null) {
                    mcr = (CrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{Terms.MEDLINE_KEY, medline});
                    RankedCrossRef rmcr = new SimpleRankedCrossRef(mcr, 0);
                    rlistener.setRankedCrossRef(rmcr);
                }
                // create the doi crossref and assign to the bioentry
                CrossRef dcr = null;
                if (doi!=null) {
                    dcr = (CrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{Terms.DOI_KEY, doi});
                    RankedCrossRef rdcr = new SimpleRankedCrossRef(dcr, 0);
                    rlistener.setRankedCrossRef(rdcr);
                }
                // create the docref object
                try {
                    DocRef dr = (DocRef)RichObjectFactory.getObject(SimpleDocRef.class,new Object[]{authors,journal});
                    if (title!=null) dr.setTitle(title);
                    // assign either the pubmed or medline to the docref - medline gets priority, then pubmed, then doi
                    if (mcr!=null) dr.setCrossref(mcr);
                    else if (pcr!=null) dr.setCrossref(pcr);
                    else if (dcr!=null) dr.setCrossref(dcr);
                    // assign the remarks
                    dr.setRemark(remark);
                    // assign the docref to the bioentry
                    RankedDocRef rdr = new SimpleRankedDocRef(dr,
                            (ref_start != -999 ? new Integer(ref_start) : null),
                            (ref_end != -999 ? new Integer(ref_end) : null),
                            ref_rank);
                    rlistener.setRankedDocRef(rdr);
                } catch (ChangeVetoException e) {
                    throw new ParseException(e);
                }
            } else if (sectionKey.equals(COMMENT_TAG)) {
                // Set up some comments
                rlistener.setComment(((String[])section.get(0))[1]);
            } else if (sectionKey.equals(FEATURE_TAG)) {
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
                            String regex = "^(\\S+?):(\\S+)$";
                            Pattern p = Pattern.compile(regex);
                            Matcher m = p.matcher(val);
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
                                        CrossRef cr = (CrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{dbname, raccession});
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
            } else if (sectionKey.equals(START_SEQUENCE_TAG) && !this.elideSymbols) {
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
                hasInternalWhitespace = true;
                continue;
            }
            if (hasInternalWhitespace)
                System.err.println("Warning: whitespace found between sequence entries");
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
        this.writeSequence(seq, getDefaultFormat(), os, null);
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeSequence(Sequence seq, String format, PrintStream os) throws IOException {
        this.writeSequence(seq, format, os, null);
    }
    
    /**
     * {@inheritDoc}
     * Namespace is ignored as EMBL has no concept of it.
     */
    public void	writeSequence(Sequence seq, PrintStream os, Namespace ns) throws IOException {
        this.writeSequence(seq, getDefaultFormat(), os, ns);
    }
    
    /**
     * {@inheritDoc}
     * Namespace is ignored as EMBL has no concept of it.
     */
    public void writeSequence(Sequence seq, String format, PrintStream os, Namespace ns) throws IOException {
        // EMBL only really - others are treated identically for now
        if (!(
                format.equalsIgnoreCase(EMBL_FORMAT)
                ))
            throw new IllegalArgumentException("Unknown format: "+format);
        
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
        String mdat = "";
        String moltype = rs.getAlphabet().getName();
        List dates = new ArrayList();
        for (Iterator i = notes.iterator(); i.hasNext(); ) {
            Note n = (Note)i.next();
            if (n.getTerm().equals(Terms.getModificationTerm())) mdat=n.getValue();
            else if (n.getTerm().equals(Terms.getMolTypeTerm())) moltype=n.getValue();
            else if (n.getTerm().equals(Terms.getAccessionTerm())) accessions = accessions+" "+n.getValue()+";";
            else if (n.getTerm().equals(Terms.getDatesTerm())) dates.add(n.getValue());
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
        this.writeWrappedLine(LOCUS_TAG, 5, locusLine.toString(), os);
        os.println(DELIMITER_TAG+"   ");
        
        // accession line
        this.writeWrappedLine(ACCESSION_TAG, 5, accessions, os);
        os.println(DELIMITER_TAG+"   ");
        
        // version line
        this.writeWrappedLine(VERSION_TAG, 5, accession+"."+rs.getVersion(), os);
        os.println(DELIMITER_TAG+"   ");
        
        // date line
        if (dates.size()>0) {
            for (Iterator i = dates.iterator(); i.hasNext(); ) this.writeWrappedLine(DATE_TAG, 5, (String)i.next(), os);
        } else {
            this.writeWrappedLine(DATE_TAG, 5, mdat+" (Rel. 0, Created)", os);
            this.writeWrappedLine(DATE_TAG, 5, mdat+" (Rel. 0, Last Updated "+rs.getVersion()+")", os);
        }
        os.println(DELIMITER_TAG+"   ");
        
        // definition line
        this.writeWrappedLine(DEFINITION_TAG, 5, rs.getDescription(), os);
        os.println(DELIMITER_TAG+"   ");
        
        // keywords line
        String keywords = null;
        for (Iterator n = notes.iterator(); n.hasNext(); ) {
            Note nt = (Note)n.next();
            if (nt.getTerm().equals(Terms.getKeywordsTerm())) {
                if (keywords==null) keywords = nt.getValue();
                else keywords = keywords+"; "+nt.getValue();
            }
        }
        if (keywords!=null) {
            this.writeWrappedLine(KEYWORDS_TAG, 5, keywords, os);
            os.println(DELIMITER_TAG+"   ");
        }
        
        // source line (from taxon)
        //   organism line
        NCBITaxon tax = rs.getTaxon();
        if (tax!=null) {
            String[] sciNames = (String[])tax.getNames(NCBITaxon.SCIENTIFIC).toArray(new String[0]);
            String[] comNames = (String[])tax.getNames(NCBITaxon.COMMON).toArray(new String[0]);
            if (sciNames.length>0 && comNames.length>0) {
                this.writeWrappedLine(SOURCE_TAG, 5, sciNames[0]+" ("+comNames[0]+")", os);
                this.writeWrappedLine(ORGANISM_TAG, 5, sciNames[0]+" ("+comNames[0]+")", os);
                os.println(DELIMITER_TAG+"   ");
            } else if (sciNames.length>0) {
                this.writeWrappedLine(SOURCE_TAG, 5, sciNames[0], os);
                this.writeWrappedLine(ORGANISM_TAG, 5, sciNames[0], os);
                os.println(DELIMITER_TAG+"   ");
            }
        }
        
        // references - rank (bases x to y)
        for (Iterator r = rs.getRankedDocRefs().iterator(); r.hasNext(); ) {
            RankedDocRef rdr = (RankedDocRef)r.next();
            DocRef d = rdr.getDocumentReference();
            // RN, RC, RP, RX, RG, RA, RT, RL
            this.writeWrappedLine(REFERENCE_TAG, 5, "["+rdr.getRank()+"]", os);
            if (d.getRemark()!=null) this.writeWrappedLine(REMARK_TAG, 5, d.getRemark(), os);
            Integer rstart = rdr.getStart();
            if (rstart==null) rstart = new Integer(1);
            Integer rend = rdr.getEnd();
            if (rend==null) rend = new Integer(rs.length());
            this.writeWrappedLine(REFERENCE_POSITION_TAG, 5, rstart+"-"+rend, os);
            CrossRef c = d.getCrossref();
            if (c!=null) this.writeWrappedLine(REFERENCE_XREF_TAG, 5, c.getDbname().toUpperCase()+"; "+c.getAccession()+".", os);
            if (d.getAuthors()!=null) this.writeWrappedLine(AUTHORS_TAG, 5, d.getAuthors(), os);
            this.writeWrappedLine(TITLE_TAG, 5, d.getTitle(), os);
            this.writeWrappedLine(JOURNAL_TAG, 5, d.getLocation(), os);
            os.println(DELIMITER_TAG+"   ");
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
                if (n.getTerm().equals(Terms.getIdentifierTerm())) {
                    sb.append("; ");
                    sb.append(n.getValue());
                    hasSecondary = true;
                }
            }
            if (!hasSecondary) sb.append("; -");
            sb.append(".");
            this.writeWrappedLine(DATABASE_XREF_TAG, 5, sb.toString(), os);
        }
        os.println(DELIMITER_TAG+"   ");
        
        // comments - if any
        if (!rs.getComments().isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (Iterator i = rs.getComments().iterator(); i.hasNext(); ) {
                Comment c = (SimpleComment)i.next();
                sb.append(c.getComment());
                if (i.hasNext()) sb.append("\n");
            }
            this.writeWrappedLine(COMMENT_TAG, 5, sb.toString(), os);
            os.println(DELIMITER_TAG+"   ");
        }
        
        os.println(FEATURE_HEADER_TAG+"   Key             Location/Qualifiers");
        os.println(FEATURE_HEADER_TAG+"   ");
        // feature_type     location
        for (Iterator i = rs.getFeatureSet().iterator(); i.hasNext(); ) {
            RichFeature f = (RichFeature)i.next();
            this.writeWrappedLocationLine(FEATURE_TAG, 5, f.getTypeTerm().getName(), 16, GenbankLocationParser.writeLocation((RichLocation)f.getLocation()), os);
            for (Iterator j = f.getNoteSet().iterator(); j.hasNext(); ) {
                Note n = (Note)j.next();
                // /key="val" or just /key if val==""
                if (n.getValue()==null || n.getValue().equals("")) this.writeWrappedLine("FT",21,"/"+n.getTerm(),os);
                else this.writeWrappedLine(FEATURE_TAG,21,"/"+n.getTerm().getName()+"=\""+n.getValue()+"\"", os);
            }
            // add-in to source feature only db_xref="taxon:xyz" where present
            if (f.getType().equals("source") && tax!=null) {
                this.writeWrappedLine(FEATURE_TAG,21,"/db_xref=\"taxon:"+tax.getNCBITaxID()+"\"", os);
            }
            // add-in other dbxrefs where present
            for (Iterator j = f.getRankedCrossRefs().iterator(); j.hasNext(); ) {
                RankedCrossRef rcr = (RankedCrossRef)j.next();
                CrossRef cr = rcr.getCrossRef();
                this.writeWrappedLine(FEATURE_TAG,21,"/db_xref=\""+cr.getDbname()+":"+cr.getAccession()+"\"", os);
            }
        }
        os.println(DELIMITER_TAG+"   ");
        
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
        os.print(START_SEQUENCE_TAG+"   "+rs.length()+" BP; ");
        os.print(aCount + " A; ");
        os.print(cCount + " C; ");
        os.print(gCount + " G; ");
        os.print(tCount + " T; ");
        os.println(oCount + " other;");
        
        // sequence stuff
        Symbol[] syms = (Symbol[])rs.toList().toArray(new Symbol[0]);
        int lineLen = 0;
        int symCount = 0;
        os.print("    ");
        for (int i = 0; i < syms.length; i++) {
            if (symCount % 60 == 0 && symCount>0) {
                os.print(StringTools.leftPad(""+symCount,10));
                os.print("\n    ");
                lineLen = 0;
            }
            if (symCount % 10 == 0) {
                os.print(" ");
                lineLen++;
            }
            try {
                os.print(tok.tokenizeSymbol(syms[i]));
            } catch (IllegalSymbolException e) {
                throw new RuntimeException("Found illegal symbol: "+syms[i]);
            }
            symCount++;
            lineLen++;
        }
        os.print(StringTools.leftPad(""+symCount,(66-lineLen)+10));
        os.print("\n");
        os.println(END_SEQUENCE_TAG);
    }
    
    // writes a line wrapped over spaces
    private void writeWrappedLine(String key, int indent, String text, PrintStream os) throws IOException {
        this._writeWrappedLine(key,indent,text,os,"\\s");
    }
    
    // writes a line wrapped over commas
    private void writeWrappedLocationLine(String key, int initialIndent, String name, int secondIndent, String location, PrintStream os) {
        this._writeDoubleWrappedLine(",", key, initialIndent, name, secondIndent, location, os);
    }
    
    // writes a line wrapped to a certain width and indented
    private void _writeWrappedLine(String key, int indent, String text, PrintStream os, String sep) throws IOException {
        text = text.trim();
        StringBuffer b = new StringBuffer();
        b.append(StringTools.rightPad(key, indent));
        String[] lines = StringTools.writeWordWrap(text, sep, this.getLineWidth()-indent);
        for (int i = 0; i<lines.length; i++) {
            if (i==0) b.append(lines[i]);
            else {
                b.append(StringTools.rightPad(key, indent));
                b.append(lines[i]);
            }
            // print line before continuing to next one
            os.println(b.toString());
            b.setLength(0);
        }
    }
    
    // writes a line wrapped over two separate indent sizes
    private void _writeDoubleWrappedLine(String sep, String key, int initialIndent, String name, int secondIndent, String location, PrintStream os) {
        int totalIndent = initialIndent+secondIndent;
        String[] lines = StringTools.writeWordWrap(location, sep, this.getLineWidth()-totalIndent);
        lines[0] = StringTools.rightPad(key,initialIndent)+
                StringTools.rightPad(name,secondIndent)+
                lines[0];
        os.println(lines[0]);
        for (int i = 1; i < lines.length; i++) os.println(StringTools.rightPad(key,totalIndent)+lines[i]);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultFormat() {
        return EMBL_FORMAT;
    }
}

