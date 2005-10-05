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
import org.biojava.bio.proteomics.MassCalc;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.ParseException;
import org.biojava.bio.seq.io.SeqIOListener;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolPropertyTable;
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
import org.biojavax.utils.CRC64Checksum;
import org.biojavax.utils.StringTools;

/**
 * Format reader for UniProt files. This version of UniProt format will generate
 * and write RichSequence objects. Loosely Based on code from the old, deprecated,
 * org.biojava.bio.seq.io.EMBLLikeFormat object.
 *
 * @author Richard Holland
 */
public class UniProtFormat implements RichSequenceFormat {
    
    /**
     * The name of this format
     */
    public static final String UNIPROT_FORMAT = "UniProt";
    
    protected static final String LOCUS_TAG = "ID";
    protected static final String ACCESSION_TAG = "AC";
    protected static final String DEFINITION_TAG = "DE";
    protected static final String DATE_TAG = "DT";
    protected static final String SOURCE_TAG = "OS";
    protected static final String ORGANISM_TAG = "OC";
    protected static final String TAXON_TAG = "OX";
    protected static final String GENE_TAG = "GN";
    protected static final String DATABASE_XREF_TAG = "DR";
    protected static final String REFERENCE_TAG = "RN";
    protected static final String REFERENCE_POSITION_TAG = "RP";
    protected static final String REFERENCE_XREF_TAG = "RX";
    protected static final String AUTHORS_TAG = "RA";
    protected static final String TITLE_TAG = "RT";
    protected static final String JOURNAL_TAG = "RL";
    protected static final String REMARK_TAG = "RC";
    protected static final String KEYWORDS_TAG = "KW";
    protected static final String COMMENT_TAG = "CC";
    protected static final String FEATURE_TAG = "FT";
    protected static final String START_SEQUENCE_TAG = "SQ";
    protected static final String END_SEQUENCE_TAG = "//";
    
    /**
     * Implements some UniProt-specific terms.
     */
    public static class Terms extends RichSequenceFormat.Terms {
        private static ComparableTerm UNIPROT_TERM = null;
        private static ComparableTerm DATACLASS_TERM = null;
        private static ComparableTerm GENENAME_TERM = null;
        private static ComparableTerm FTID_TERM = null;
        private static ComparableTerm FEATUREDESC_TERM = null;
        
        /**
         * Getter for the UniProt term
         * @return The UniProt Term
         */
        public static ComparableTerm getUniProtTerm() {
            if (UNIPROT_TERM==null) UNIPROT_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("UniProt");
            return UNIPROT_TERM;
        }
        
        /**
         * Getter for the DataClass term
         * @return The DataClass Term
         */
        public static ComparableTerm getDataClassTerm() {
            if (DATACLASS_TERM==null) DATACLASS_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("dataclass");
            return DATACLASS_TERM;
        }
        
        /**
         * Getter for the GeneName term
         * @return The GeneName Term
         */
        public static ComparableTerm getGeneNameTerm() {
            if (GENENAME_TERM==null) GENENAME_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("gene");
            return GENENAME_TERM;
        }
        
        /**
         * Getter for the FTId term
         * @return The FTId Term
         */
        public static ComparableTerm getFTIdTerm() {
            if (FTID_TERM==null) FTID_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("FTId");
            return FTID_TERM;
        }
        
        /**
         * Getter for the FeatureDesc term
         * @return The FeatureDesc Term
         */
        public static ComparableTerm getFeatureDescTerm() {
            if (FEATUREDESC_TERM==null) FEATUREDESC_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("description");
            return FEATUREDESC_TERM;
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
                // entryname  dataclass; moltype; sequencelength AA.
                String loc = ((String[])section.get(0))[1];
                String regex = "^((\\S+)_(\\S+))\\s+(\\S+);\\s+(PRT);\\s+\\d+\\s+AA\\.$";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(loc);
                if (m.matches()) {
                    rlistener.setName(m.group(2));
                    rlistener.setDivision(m.group(3));
                    rlistener.addSequenceProperty(Terms.getDataClassTerm(),m.group(4));
                    rlistener.addSequenceProperty(Terms.getMolTypeTerm(),m.group(5));
                } else {
                    throw new ParseException("Bad ID line found: "+loc);
                }
            } else if (sectionKey.equals(DEFINITION_TAG)) {
                rlistener.setDescription(((String[])section.get(0))[1]);
            } else if (sectionKey.equals(SOURCE_TAG)) {
                // use SOURCE_TAG and TAXON_TAG values
                String sciname = null;
                String comname = null;
                int taxid = 0;
                for (int i = 0; i < section.size(); i++) {
                    String tag = ((String[])section.get(i))[0];
                    String value = ((String[])section.get(i))[1].trim();
                    if (tag.equals(SOURCE_TAG)) {
                        String[] parts = value.split("\\(");
                        sciname = parts[0].trim();
                        if (parts.length>1) {
                            comname = parts[1].trim();
                            comname = comname.substring(0,comname.length()-2); // chomp trailing bracket and dot
                        }
                    } else if (tag.equals(TAXON_TAG)) {
                        String[] parts = value.split(";");
                        for (int j = 0; j < parts.length; j++) {
                            String[] bits = parts[j].split("=");
                            if (bits[0].equals("NCBI_TaxID")) {
                                String[] morebits = bits[1].split(",");
                                taxid = Integer.parseInt(morebits[0].trim());
                            }
                        }
                    }
                }
                // Set the Taxon
                tax = (NCBITaxon)RichObjectFactory.getObject(SimpleNCBITaxon.class, new Object[]{new Integer(taxid)});
                rlistener.setTaxon(tax);
                try {
                    if (sciname!=null) tax.addName(NCBITaxon.SCIENTIFIC,sciname);
                    if (comname!=null) tax.addName(NCBITaxon.COMMON,comname);
                } catch (ChangeVetoException e) {
                    throw new ParseException(e);
                }
            } else if (sectionKey.equals(DATE_TAG)) {
                // we store it as the modification date, for compatibility with single-date formats
                String date = ((String[])section.get(0))[1].substring(0,11);
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
            } else if (sectionKey.equals(KEYWORDS_TAG)) {
                String[] kws = ((String[])section.get(0))[1].split(";");
                for (int i = 0; i < kws.length; i++) {
                    String kw = kws[i].trim();
                    if (kw.length()==0) continue;
                    if (i==kws.length-1) kw=kw.substring(0,kw.length()-1); // chomp trailing dot
                    rlistener.addSequenceProperty(Terms.getKeywordsTerm(), kw);
                }
            } else if (sectionKey.equals(GENE_TAG)) {
                String[] gs = ((String[])section.get(0))[1].split("(\\s+or\\s+|\\s+and\\s+|;)");
                for (int i = 0; i < gs.length; i++) {
                    String raw = gs[i].trim();
                    if (raw.length()==0) continue;
                    String[] parts = raw.split("=");
                    String[] moreparts = parts[1].split(",");
                    for (int j = 0; j < moreparts.length; j++) rlistener.addSequenceProperty(Terms.getGeneNameTerm(), moreparts[j].trim());
                }
            } else if (sectionKey.equals(DATABASE_XREF_TAG)) {
                // database_identifier; primary_identifier; secondary_identifier....
                String[] parts = ((String[])section.get(0))[1].split(";");
                String finalPart = parts[parts.length-1].trim();
                finalPart = finalPart.substring(0,finalPart.length()-1); // chomp trailing dot
                parts[parts.length-1]=finalPart;
                // construct a DBXREF out of the dbname part[0] and accession part[1]
                CrossRef crossRef = (CrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{parts[0].trim(),parts[1].trim()});
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
                RankedCrossRef rcrossRef = new SimpleRankedCrossRef(crossRef, ++xrefCount);
                rlistener.setRankedCrossRef(rcrossRef);
            } else if (sectionKey.equals(REFERENCE_TAG)) {
                Pattern rppat = Pattern.compile("\\(SEQUENCE OF (\\d+)-(\\d+).*\\)");
                // first line of section has rank and location
                String refrank = ((String[])section.get(0))[1];
                int ref_rank = Integer.parseInt(refrank.substring(1,refrank.length()-1));
                // rest can be in any order
                String authors = null;
                String title = null;
                String journal = null;
                String pubmed = null;
                String medline = null;
                String doi = null;
                String remark = null;
                Integer rstart = null;
                Integer rend = null;
                for (int i = 1; i < section.size(); i++) {
                    String key = ((String[])section.get(i))[0];
                    String val = ((String[])section.get(i))[1];
                    if (key.equals(AUTHORS_TAG)) authors = val;
                    if (key.equals(TITLE_TAG)) title = val;
                    if (key.equals(JOURNAL_TAG)) journal = val;
                    if (key.equals(REFERENCE_XREF_TAG)) {
                        // database_identifier=primary_identifier;
                        String[] refs = val.split(";");
                        for (int j = 0 ; j < refs.length; j++) {
                            if (refs[j].trim().length()==0) continue;
                            String[] parts = refs[j].split("=");
                            String db = parts[0].toUpperCase();
                            String ref = parts[1];
                            if (db.equals("PUBMED")) pubmed = ref;
                            else if (db.equals("MEDLINE")) medline = ref;
                            else if (db.equals("DOI")) doi = ref;
                        }
                    }
                    if (key.equals(REFERENCE_POSITION_TAG)) {
                        // NO REFERENCE_POSITION_TAG (RP) TAG - BIOSQL CANNOT HANDLE
                        // Just use it to find the location of the reference, if we have one.
                        Matcher m = rppat.matcher(val);
                        if (m.matches()) {
                            rstart = Integer.valueOf(m.group(1));
                            rend = Integer.valueOf(m.group(2));
                        }
                    }
                    if (key.equals(REMARK_TAG)) remark = val;
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
                    RankedDocRef rdr = new SimpleRankedDocRef(dr,rstart,rend,ref_rank);
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
                Pattern p = Pattern.compile("\\s*([\\d\\?<]+)\\s+([\\d\\?>]+)(\\s+(\\S.*))?");
                for (int i = 1 ; i < section.size(); i++) {
                    String key = ((String[])section.get(i))[0];
                    String val = ((String[])section.get(i))[1];
                    if (key.startsWith("/")) {
                        key = key.substring(1); // strip leading slash
                        val = val.replaceAll("\"","").trim(); // strip quotes
                        if (key.equals("FTId")) {
                            // add all except trailing dot
                            rlistener.addFeatureProperty(Terms.getFTIdTerm(),val.substring(0,val.length()-1));
                        } else {
                            // add the whole lot - should never happen anyway
                            rlistener.addFeatureProperty(RichObjectFactory.getDefaultOntology().getOrCreateTerm(key),val);
                        }
                    } else {
                        // new feature!
                        // end previous feature
                        if (seenAFeature) rlistener.endFeature();
                        // start next one, with lots of lovely info in it
                        RichFeature.Template templ = new RichFeature.Template();
                        templ.annotation = new SimpleRichAnnotation();
                        templ.sourceTerm = Terms.getUniProtTerm();
                        templ.typeTerm = RichObjectFactory.getDefaultOntology().getOrCreateTerm(key);
                        templ.featureRelationshipSet = new TreeSet();
                        templ.rankedCrossRefs = new TreeSet();
                        String desc = null;
                        Matcher m = p.matcher(val);
                        if (m.matches()) {
                            String start = m.group(1);
                            String end = m.group(2);
                            desc = m.group(4);
                            templ.location = UniProtLocationParser.parseLocation(start, end);
                        } else {
                            throw new ParseException("Bad feature value: "+val);
                        }
                        rlistener.startFeature(templ);
                        if (desc!=null) rlistener.addFeatureProperty(Terms.getFeatureDescTerm(),desc);
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
                            sb.append(line);
                        }
                    }
                    section.add(new String[]{START_SEQUENCE_TAG,sb.toString()});
                }
                // READ COMMENT SECTION
                else if (token.equals(COMMENT_TAG)) {
                    // read from first line till next that begins with "CC   -!-"
                    StringBuffer currentVal = new StringBuffer();
                    boolean wasMisc = false;
                    if (!line.substring(0,8).equals(COMMENT_TAG+"   -!-")) wasMisc = true;
                    currentVal.append(line.substring(5));
                    while (!done) {
                        br.mark(160);
                        line = br.readLine();
                        if (((!wasMisc) && line.charAt(5)!=' ') || line.charAt(0)!='C' || line.substring(0,8).equals(COMMENT_TAG+"   -!-")) {
                            br.reset();
                            done = true;
                            // dump current tag if exists
                            section.add(new String[]{COMMENT_TAG,currentVal.toString()});
                        } else {
                            currentVal.append("\n");
                            currentVal.append(line.substring(5));
                        }
                    }
                }
                // READ FEATURE TABLE SECTION
                else if (token.equals(FEATURE_TAG)) {
                    br.reset();
                    //      read all FT lines until first non-FT starting line
                    String currentTag = null;
                    StringBuffer currentVal = new StringBuffer();
                    section.add(new String[]{FEATURE_TAG,null});
                    while (!done) {
                        br.mark(160);
                        line = br.readLine();
                        if (!line.substring(0,2).equals(FEATURE_TAG)) {
                            br.reset();
                            done = true;
                            // dump current tag if exists
                            if (currentTag!=null) section.add(new String[]{currentTag,currentVal.toString()});
                        } else {
                            //         FT lines:   FT   KEY_NAME     x      x        description
                            //         or:         FT                                ....
                            //         or          FT                                /FTId=899.
                            line = line.substring(5); // chomp off "FT   "
                            if (line.charAt(0)!=' ') {
                                // dump current tag if exists
                                if (currentTag!=null) section.add(new String[]{currentTag,currentVal.toString()});
                                // case 1 : word value - splits into key-value based on first 8 chars
                                currentTag = line.substring(0,8).trim();
                                currentVal = new StringBuffer();
                                currentVal.append(line.substring(8));
                            } else {
                                line = line.trim();
                                if (line.charAt(0)=='/') {
                                    // dump current tag if exists
                                    if (currentTag!=null) section.add(new String[]{currentTag,currentVal.toString()});
                                    // case 3 : /word=.....
                                    String[] parts = line.split("=");
                                    currentTag = parts[0].trim();
                                    currentVal = new StringBuffer();
                                    currentVal.append(parts[1]);
                                } else {
                                    // case 2 : ...."
                                    currentVal.append("\n");
                                    currentVal.append(line);
                                }
                            }
                        }
                    }
                }
                // READ DOCREF
                else if (token.equals(DATABASE_XREF_TAG)) {
                    section.add(new String[]{DATABASE_XREF_TAG,line.substring(5).trim()});
                    done = true;
                }
                // READ END OF SEQUENCE
                else if (token.equals(END_SEQUENCE_TAG)) {
                    section.add(new String[]{END_SEQUENCE_TAG,null});
                    done = true;
                }
                // READ NORMAL TAG/VALUE SECTION
                else {
                    //      rewind buffer to mark
                    br.reset();
                    //      read token/values until first with non-same first character
                    //      exceptions: DE/DT, and RN...RN
                    String currentTag = null;
                    char currentTagStart = '\0';
                    StringBuffer currentVal = null;
                    while (!done) {
                        br.mark(160);
                        line = br.readLine();
                        if (currentTagStart=='\0') currentTagStart = line.charAt(0);
                        if (line.charAt(0)!=currentTagStart ||
                                (currentTagStart=='D' && currentTag!=null && !currentTag.equals(line.substring(0,2))) ||
                                (currentTagStart=='R' && currentTag!=null && "RN".equals(line.substring(0,2)))) {
                            br.reset();
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
     * Namespace is ignored as UniProt has no concept of it.
     */
    public void	writeSequence(Sequence seq, PrintStream os, Namespace ns) throws IOException {
        this.writeSequence(seq, getDefaultFormat(), os, ns);
    }
    
    /**
     * {@inheritDoc}
     * Namespace is ignored as UniProt has no concept of it.
     */
    public void writeSequence(Sequence seq, String format, PrintStream os, Namespace ns) throws IOException {
        // UniProt only really - others are treated identically for now
        if (!(
                format.equalsIgnoreCase(UNIPROT_FORMAT)
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
        List genenames = new ArrayList();
        String mdat = "";
        String dataclass = "STANDARD";
        for (Iterator i = notes.iterator(); i.hasNext(); ) {
            Note n = (Note)i.next();
            if (n.getTerm().equals(Terms.getModificationTerm())) mdat=n.getValue();
            else if (n.getTerm().equals(Terms.getDataClassTerm())) dataclass = n.getValue();
            else if (n.getTerm().equals(Terms.getAccessionTerm())) accessions = accessions+" "+n.getValue()+";";
            else if (n.getTerm().equals(Terms.getGeneNameTerm())) genenames.add(n.getValue());
        }
        
        // entryname  dataclass; [circular] molecule; division; sequencelength BP.
        StringBuffer locusLine = new StringBuffer();
        locusLine.append(StringTools.rightPad(rs.getName()+"_"+rs.getDivision(),11));
        locusLine.append(" ");
        locusLine.append(StringTools.leftPad(dataclass,12));
        locusLine.append(";      PRT; ");
        locusLine.append(StringTools.leftPad(""+rs.length(),5));
        locusLine.append(" AA.");
        this.writeWrappedLine(LOCUS_TAG, 5, locusLine.toString(), os);
        
        // accession line
        this.writeWrappedLine(ACCESSION_TAG, 5, accessions, os);
        
        // date line
        this.writeWrappedLine(DATE_TAG, 5, mdat+" (Rel. 00, Created)", os);
        this.writeWrappedLine(DATE_TAG, 5, mdat+" (Rel. 00, Last sequence update)",os);
        this.writeWrappedLine(DATE_TAG, 5, mdat+" (Rel. 00, Last annotation update)",os);
        
        // definition line
        this.writeWrappedLine(DEFINITION_TAG, 5, rs.getDescription(), os);
        
        // gene line
        String geneline = null;
        for (int i = 0; i < genenames.size(); i++) {
            if (i==0) {
                geneline="Name="+genenames.get(i)+";";
                if (genenames.size()>1) geneline+=" Synonyms=";
            } else {
                geneline+=genenames.get(i);
                if (i==genenames.size()) geneline+=".";
                else geneline+=", ";
            }
        }
        if (geneline!=null) this.writeWrappedLine(GENE_TAG, 5, geneline, os);
        
        // source line (from taxon)
        //   organism line
        NCBITaxon tax = rs.getTaxon();
        if (tax!=null) {
            String[] sciNames = (String[])tax.getNames(NCBITaxon.SCIENTIFIC).toArray(new String[0]);
            String[] comNames = (String[])tax.getNames(NCBITaxon.COMMON).toArray(new String[0]);
            if (sciNames.length>0 && comNames.length>0) {
                this.writeWrappedLine(SOURCE_TAG, 5, sciNames[0]+" ("+comNames[0]+").", os);
                this.writeWrappedLine(ORGANISM_TAG, 5, sciNames[0]+" ("+comNames[0]+").", os);
            } else if (sciNames.length>0) {
                this.writeWrappedLine(SOURCE_TAG, 5, sciNames[0]+".", os);
                this.writeWrappedLine(ORGANISM_TAG, 5, sciNames[0]+".", os);
            }
            this.writeWrappedLine(TAXON_TAG, 5, "NCBI_TaxID="+tax.getNCBITaxID()+";", os);
        }
        
        // references - rank (bases x to y)
        for (Iterator r = rs.getRankedDocRefs().iterator(); r.hasNext(); ) {
            RankedDocRef rdr = (RankedDocRef)r.next();
            DocRef d = rdr.getDocumentReference();
            // RN, RP, RC, RX, RG, RA, RT, RL
            this.writeWrappedLine(REFERENCE_TAG, 5, "["+rdr.getRank()+"]", os);
            // NO RP TAG - CANNOT FORCE INTO BIOSQL!
            // Just print out ref position if present
            if (rdr.getStart()!=null && rdr.getEnd()!=null) this.writeWrappedLine(REFERENCE_POSITION_TAG, 5, "(SEQUENCE OF "+rdr.getStart()+"-"+rdr.getEnd()+")",os);
            if (d.getRemark()!=null) this.writeWrappedLine(REMARK_TAG, 5, d.getRemark(), os);
            CrossRef c = d.getCrossref();
            if (c!=null) this.writeWrappedLine(REFERENCE_XREF_TAG, 5, c.getDbname().toUpperCase()+"="+c.getAccession()+";", os);
            if (d.getAuthors()!=null) this.writeWrappedLine(AUTHORS_TAG, 5, d.getAuthors(), os);
            this.writeWrappedLine(TITLE_TAG, 5, d.getTitle(), os);
            this.writeWrappedLine(JOURNAL_TAG, 5, d.getLocation(), os);
        }
        
        // comments - if any
        if (!rs.getComments().isEmpty()) {
            for (Iterator i = rs.getComments().iterator(); i.hasNext(); ) {
                Comment c = (SimpleComment)i.next();
                String text = c.getComment().trim();
                if (text.length()>3 && text.substring(0,3).equals("-!-")) this.writeWrappedCommentLine(COMMENT_TAG, 5, "-!- ", 4, text.substring(4), os);
                else this.writeWrappedLine(COMMENT_TAG, 5, text, os);
            }
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
            this.writeWrappedLine(KEYWORDS_TAG, 5, keywords+".", os);
        }
        
        // feature_type     location
        for (Iterator i = rs.getFeatureSet().iterator(); i.hasNext(); ) {
            RichFeature f = (RichFeature)i.next();
            String desc = " ";
            String ftid = null;
            for (Iterator j = f.getNoteSet().iterator(); j.hasNext(); ) {
                Note n = (Note)j.next();
                if (n.getTerm().equals(Terms.getFTIdTerm())) ftid = n.getValue();
                else if (n.getTerm().equals(Terms.getFeatureDescTerm())) desc = n.getValue();
            }
            String kw = f.getTypeTerm().getName();
            String leader = StringTools.rightPad(kw,8)+" "+UniProtLocationParser.writeLocation((RichLocation)f.getLocation());
            this.writeWrappedLocationLine(FEATURE_TAG, 5, leader, 24, desc, os);
            if (ftid!=null) this.writeWrappedLine(FEATURE_TAG,29,"/FTId="+ftid+".", os);
        }
        
        // sequence header
        int mw = 0;
        try {
            mw = (int)MassCalc.getMass(rs, SymbolPropertyTable.AVG_MASS, false);
        } catch (IllegalSymbolException e) {
            throw new RuntimeException("Found illegal symbol", e);
        }
        CRC64Checksum crc = new CRC64Checksum();
        String seqstr = rs.seqString();
        crc.update(seqstr.getBytes(),0,seqstr.length());
        os.print(START_SEQUENCE_TAG+"   SEQUENCE "+StringTools.rightPad(""+rs.length(),4)+" AA; ");
        os.print(StringTools.rightPad(""+mw,5)+" MW; ");
        os.println(crc+" CRC64;");
        
        // sequence stuff
        Symbol[] syms = (Symbol[])rs.toList().toArray(new Symbol[0]);
        int symCount = 0;
        os.print("    ");
        for (int i = 0; i < syms.length; i++) {
            if (symCount % 60 == 0 && symCount>0) {
                os.print("\n    ");
            }
            if (symCount % 10 == 0) {
                os.print(" ");
            }
            try {
                os.print(tok.tokenizeSymbol(syms[i]));
            } catch (IllegalSymbolException e) {
                throw new RuntimeException("Found illegal symbol: "+syms[i]);
            }
            symCount++;
        }
        os.print("\n");
        os.println(END_SEQUENCE_TAG);
    }
    
// writes a line wrapped over spaces
    private void writeWrappedLine(String key, int indent, String text, PrintStream os) throws IOException {
        this._writeWrappedLine(key,indent,text,os,"\\s");
    }
    
// writes a line wrapped over spaces
    private void writeWrappedCommentLine(String key, int initialIndent, String name, int secondIndent, String comment, PrintStream os) {
        this._writeDoubleWrappedLine("\\s", key, initialIndent, name, secondIndent, comment, os);
    }
    
// writes a line wrapped over commas
    private void writeWrappedLocationLine(String key, int initialIndent, String name, int secondIndent, String location, PrintStream os) {
        this._writeDoubleWrappedLine("\\s", key, initialIndent, name, secondIndent, location, os);
    }
    
// writes a line wrapped to a certain width and indented
    private void _writeWrappedLine(String key, int indent, String text, PrintStream os, String sep) throws IOException {
        if (key==null || text==null) return;
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
    private void _writeDoubleWrappedLine(String sep, String key, int initialIndent, String text, int secondIndent, String location, PrintStream os) {
        if (key==null || text==null) return;
        int totalIndent = initialIndent+secondIndent;
        String[] lines = StringTools.writeWordWrap(location, sep, this.getLineWidth()-totalIndent);
        lines[0] = StringTools.rightPad(key,initialIndent)+
                StringTools.rightPad(text,secondIndent)+
                lines[0];
        os.println(lines[0]);
        for (int i = 1; i < lines.length; i++) os.println(StringTools.rightPad(key,totalIndent)+lines[i]);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultFormat() {
        return UNIPROT_FORMAT;
    }
}

