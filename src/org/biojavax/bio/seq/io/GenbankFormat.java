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
import org.biojava.bio.symbol.AlphabetManager;
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
import org.biojavax.SimpleComment;
import org.biojavax.SimpleCrossRef;
import org.biojavax.SimpleDocRef;
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
 * Format reader for GenBank files. This version of Genbank format will generate
 * and write RichSequence objects. Loosely Based on code from the old, deprecated,
 * org.biojava.bio.seq.io.GenbankFormat object.
 *
 * @author Richard Holland
 * bugfixes
 * @author MarkSchreiber
 */
public class GenbankFormat extends RichSequenceFormat.HeaderlessFormat {
    
    /**
     * The name of this format
     */
    public static final String GENBANK_FORMAT = "GENBANK";
    
    protected static final String LOCUS_TAG = "LOCUS";
    protected static final String ACCESSION_TAG = "ACCESSION";
    protected static final String VERSION_TAG = "VERSION";
    protected static final String DEFINITION_TAG = "DEFINITION";
    protected static final String SOURCE_TAG = "SOURCE";
    protected static final String ORGANISM_TAG = "ORGANISM";
    protected static final String REFERENCE_TAG = "REFERENCE";
    protected static final String KEYWORDS_TAG = "KEYWORDS";
    protected static final String AUTHORS_TAG = "AUTHORS";
    protected static final String TITLE_TAG = "TITLE";
    protected static final String JOURNAL_TAG = "JOURNAL";
    protected static final String PUBMED_TAG = "PUBMED";
    protected static final String MEDLINE_TAG = "MEDLINE";
    protected static final String REMARK_TAG = "REMARK";
    protected static final String COMMENT_TAG = "COMMENT";
    protected static final String FEATURE_TAG = "FEATURES";
    protected static final String BASE_COUNT_TAG = "BASE";
    protected static final String START_SEQUENCE_TAG = "ORIGIN";
    protected static final String END_SEQUENCE_TAG = "//";
    
    // locus line
    protected static final Pattern lp = Pattern.compile("^(\\S+)\\s+\\d+\\s+bp\\s+([dms]s-)?(\\S+)\\s+(circular|linear)?\\s+(\\S+)\\s+(\\S+)$");
    // version line
    protected static final Pattern vp = Pattern.compile("^(\\S+?)\\.(\\d+)\\s+GI:(\\S+)$");
    // reference line
    protected static final Pattern refp = Pattern.compile("^(\\d+)\\s*(\\(bases\\s+(\\d+)\\s+to\\s+(\\d+)\\)|\\(sites\\))?");
    // dbxref line
    protected static final Pattern dbxp = Pattern.compile("^(\\S+?):(\\S+)$");
    //sections start at a line and continue till the first line afterwards with a
    //non-whitespace first character
    //we want to match any of the following as a new section within a section
    //  \s{0,8} word \s{1,7} value
    //  \s{21} /word = value
    //  \s{21} /word
    protected static final Pattern sectp = Pattern.compile("^(\\s{0,8}(\\S+)\\s{1,7}(.*)|\\s{21}(/\\S+?)=(.*)|\\s{21}(/\\S+))$");
    
    /**
     * Implements some GenBank-specific terms.
     */
    public static class Terms extends RichSequence.Terms {
        private static ComparableTerm GENBANK_TERM = null;
        
        /**
         * Getter for the Genbank term
         * @return The genbank Term
         */
        public static ComparableTerm getGenBankTerm() {
            if (GENBANK_TERM==null) GENBANK_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("GenBank");
            return GENBANK_TERM;
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
        do {
            List section = this.readSection(reader);
            sectionKey = ((String[])section.get(0))[0];
            if(sectionKey == null){
                throw new ParseException("Section key was null. Accession:"+
                        accession == null ? "Not set" : accession);
            }
            // process section-by-section
            if (sectionKey.equals(LOCUS_TAG)) {
                String loc = ((String[])section.get(0))[1];
                Matcher m = lp.matcher(loc);
                if (m.matches()) {
                    rlistener.setName(m.group(1));
                    rlistener.setDivision(m.group(5));
                    rlistener.addSequenceProperty(Terms.getMolTypeTerm(),m.group(3));
                    rlistener.addSequenceProperty(Terms.getDateUpdatedTerm(),m.group(6));
                    // Optional extras
                    String stranded = m.group(2);
                    String circular = m.group(4);
                    if (stranded!=null) rlistener.addSequenceProperty(Terms.getStrandedTerm(),stranded);
                    if (circular!=null && circular.equals("circular")) rlistener.setCircular(true);
                } else {
                    throw new ParseException("Bad locus line found: "+loc);
                }
            } else if (sectionKey.equals(DEFINITION_TAG)) {
                rlistener.setDescription(((String[])section.get(0))[1]);
            } else if (sectionKey.equals(ACCESSION_TAG)) {
                // if multiple accessions, store only first as accession,
                // and store rest in annotation
                String[] accs = ((String[])section.get(0))[1].split("\\s+");
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
                    rlistener.setIdentifier(m.group(3));
                } else {
                    throw new ParseException("Bad version line found: "+ver);
                }
            } else if (sectionKey.equals(KEYWORDS_TAG)) {
                String val = ((String[])section.get(0))[1];
                val = val.substring(0, val.length()-1); // chomp dot
                String[] kws = val.split(";");
                for (int i = 0; i < kws.length; i++) {
                    String kw = kws[i].trim();
                    rlistener.addSequenceProperty(Terms.getKeywordTerm(), kw);
                }
            } else if (sectionKey.equals(SOURCE_TAG)) {
                // ignore - can get all this from the first feature
            } else if (sectionKey.equals(REFERENCE_TAG) && !this.getElideReferences()) {
                // first line of section has rank and location
                int ref_rank;
                int ref_start = -999;
                int ref_end = -999;
                String ref = ((String[])section.get(0))[1];
                Matcher m = refp.matcher(ref);
                if (m.matches()) {
                    ref_rank = Integer.parseInt(m.group(1));
                    if(m.group(2) != null){
                        if (m.group(3)!= null)
                            ref_start = Integer.parseInt(m.group(3));
                        
                        if(m.group(4) != null)
                            ref_end = Integer.parseInt(m.group(4));
                    }
                } else {
                    throw new ParseException("Bad reference line found: "+ref);
                }
                // rest can be in any order
                String authors = null;
                String title = null;
                String journal = null;
                String medline = null;
                String pubmed = null;
                String remark = null;
                for (int i = 1; i < section.size(); i++) {
                    String key = ((String[])section.get(i))[0];
                    String val = ((String[])section.get(i))[1];
                    if (key.equals(AUTHORS_TAG)) authors = val.trim();
                    if (key.equals(TITLE_TAG)) title = val.trim();
                    if (key.equals(JOURNAL_TAG)) journal = val;
                    if (key.equals(MEDLINE_TAG)) medline = val;
                    if (key.equals(PUBMED_TAG)) pubmed = val;
                    if (key.equals(REMARK_TAG)) remark = val;
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
                // create the docref object
                try {
                    DocRef dr = (DocRef)RichObjectFactory.getObject(SimpleDocRef.class,new Object[]{DocRefAuthor.Tools.parseAuthorString(authors),journal});
                    if (title!=null) dr.setTitle(title);
                    // assign either the pubmed or medline to the docref - medline gets priority
                    if (mcr!=null) dr.setCrossref(mcr);
                    else if (pcr!=null) dr.setCrossref(pcr);
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
                        templ.sourceTerm = Terms.getGenBankTerm();
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
            } else if (sectionKey.equals(BASE_COUNT_TAG)) {
                // ignore - can calculate from sequence content later if needed
            } else if (sectionKey.equals(START_SEQUENCE_TAG) && !this.getElideSymbols()) {
                // our first line is ignorable as it is the ORIGIN tag
                // the second line onwards conveniently have the number as
                // the [0] tuple, and sequence string as [1] so all we have
                // to do is concat the [1] parts and then strip out spaces,
                // and replace '.' and '~' with '-' for our parser.
                StringBuffer seq = new StringBuffer();
                for (int i = 1 ; i < section.size(); i++) seq.append(((String[])section.get(i))[1]);
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
        
        try {
            while (!done) {
                br.mark(160);
                line = br.readLine();
                if (line==null || line.equals("") || (line.charAt(0)!=' ' && linecount++>0)) {
                    // dump out last part of section
                    section.add(new String[]{currKey,currVal.toString()});
                    br.reset();
                    done = true;
                } else {
                    Matcher m = sectp.matcher(line);
                    if (m.matches()) {
                        // new key
                        if (currKey!=null) section.add(new String[]{currKey,currVal.toString()});
                        // key = group(2) or group(4) or group(6) - whichever is not null
                        currKey = m.group(2)==null?(m.group(4)==null?m.group(6):m.group(4)):m.group(2);
                        currVal = new StringBuffer();
                        // val = group(3) if group(2) not null, group(5) if group(4) not null, "" otherwise, trimmed
                        currVal.append((m.group(2)==null?(m.group(4)==null?"":m.group(5)):m.group(3)).trim());
                    } else {
                        line = line.trim();
                        // concatted line or SEQ START/END line?
                        if (line.equals(START_SEQUENCE_TAG) || line.equals(END_SEQUENCE_TAG)) currKey = line;
                        else {
                            currVal.append("\n"); // newline in between lines - can be removed later
                            currVal.append(line);
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
     * Namespace is ignored as Genbank has no concept of it.
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
        String accessions = accession;
        String stranded = "";
        String udat = "";
        String moltype = rs.getAlphabet().getName();
        String keywords = "";
        for (Iterator i = notes.iterator(); i.hasNext(); ) {
            Note n = (Note)i.next();
            if (n.getTerm().equals(Terms.getStrandedTerm())) stranded=n.getValue();
            else if (n.getTerm().equals(Terms.getDateUpdatedTerm())) udat=n.getValue();
            else if (n.getTerm().equals(Terms.getMolTypeTerm())) moltype=n.getValue();
            else if (n.getTerm().equals(Terms.getAdditionalAccessionTerm())) accessions = accessions+" "+n.getValue();
            else if (n.getTerm().equals(Terms.getKeywordTerm())) {
                if (keywords.equals("")) keywords=n.getValue();
                else keywords = keywords+"; "+n.getValue();
            }
        }
        
        // locus(name) + length + alpha + div + date line
        StringBuffer locusLine = new StringBuffer();
        locusLine.append(StringTools.rightPad(rs.getName(),10));
        locusLine.append(" ");
        locusLine.append(StringTools.leftPad(""+rs.length(),6));
        locusLine.append(" bp ");
        locusLine.append(StringTools.leftPad(stranded,3));
        locusLine.append(StringTools.rightPad(moltype,6));
        locusLine.append(StringTools.rightPad(rs.getCircular()?"circular":"",10));
        locusLine.append(StringTools.rightPad(rs.getDivision()==null?"":rs.getDivision(),10));
        locusLine.append(udat);
        StringTools.writeKeyValueLine(LOCUS_TAG, locusLine.toString(), 12, this.getLineWidth(), this.getPrintStream());
        
        // definition line
        StringTools.writeKeyValueLine(DEFINITION_TAG, rs.getDescription(), 12, this.getLineWidth(), this.getPrintStream());
        
        // accession line
        StringTools.writeKeyValueLine(ACCESSION_TAG, accessions, 12, this.getLineWidth(), this.getPrintStream());
        
        // version + gi line
        String version = accession+"."+rs.getVersion();
        if (rs.getIdentifier()!=null) version = version + "  GI:"+rs.getIdentifier();
        StringTools.writeKeyValueLine(VERSION_TAG, version, 12, this.getLineWidth(), this.getPrintStream());
        
        // keywords line
        StringTools.writeKeyValueLine(KEYWORDS_TAG, keywords+".", 12, this.getLineWidth(), this.getPrintStream());
        
        // source line (from taxon)
        //   organism line
        NCBITaxon tax = rs.getTaxon();
        if (tax!=null) {
            StringTools.writeKeyValueLine(SOURCE_TAG, tax.getDisplayName(), 12, this.getLineWidth(), this.getPrintStream());
            StringTools.writeKeyValueLine("  "+ORGANISM_TAG, tax.getDisplayName().split("\\s+\\(")[0]+"\n"+tax.getNameHierarchy(), 12, this.getLineWidth(), this.getPrintStream());
        }
        
        // references - rank (bases x to y)
        for (Iterator r = rs.getRankedDocRefs().iterator(); r.hasNext(); ) {
            RankedDocRef rdr = (RankedDocRef)r.next();
            DocRef d = rdr.getDocumentReference();
            Integer rstart = rdr.getStart();
            if (rstart==null) rstart = new Integer(1);
            Integer rend = rdr.getEnd();
            if (rend==null) rend = new Integer(rs.length());
            StringTools.writeKeyValueLine(REFERENCE_TAG, rdr.getRank()+"  (bases "+rstart+" to "+rend+")", 12, this.getLineWidth(), this.getPrintStream());
            StringTools.writeKeyValueLine("  "+AUTHORS_TAG, d.getAuthors(), 12, this.getLineWidth(), this.getPrintStream());
            StringTools.writeKeyValueLine("  "+TITLE_TAG, d.getTitle(), 12, this.getLineWidth(), this.getPrintStream());
            StringTools.writeKeyValueLine("  "+JOURNAL_TAG, d.getLocation(), 12, this.getLineWidth(), this.getPrintStream());
            CrossRef c = d.getCrossref();
            if (c!=null) StringTools.writeKeyValueLine("  "+c.getDbname().toUpperCase(), c.getAccession(), 12, this.getLineWidth(), this.getPrintStream());
            StringTools.writeKeyValueLine("  "+REMARK_TAG, d.getRemark(), 12, this.getLineWidth(), this.getPrintStream());
        }
        
        // comments - if any
        if (!rs.getComments().isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (Iterator i = rs.getComments().iterator(); i.hasNext(); ) {
                Comment c = (SimpleComment)i.next();
                sb.append(c.getComment());
                if (i.hasNext()) sb.append("\n");
            }
            StringTools.writeKeyValueLine(COMMENT_TAG, sb.toString(), 12, this.getLineWidth(), this.getPrintStream());
        }
        
        this.getPrintStream().println(FEATURE_TAG+"             Location/Qualifiers");
        // feature_type     location
        for (Iterator i = rs.getFeatureSet().iterator(); i.hasNext(); ) {
            RichFeature f = (RichFeature)i.next();
            StringTools.writeKeyValueLine("     "+f.getTypeTerm().getName(), GenbankLocationParser.writeLocation((RichLocation)f.getLocation()), 21, this.getLineWidth(), ",", this.getPrintStream());
            for (Iterator j = f.getNoteSet().iterator(); j.hasNext(); ) {
                Note n = (Note)j.next();
                // /key="val" or just /key if val==""
                if (n.getValue()==null || n.getValue().equals("")) StringTools.writeKeyValueLine("", "/"+n.getTerm(), 21, this.getLineWidth(), this.getPrintStream());
                else StringTools.writeKeyValueLine("", "/"+n.getTerm().getName()+"=\""+n.getValue()+"\"", 21, this.getLineWidth(), this.getPrintStream());
            }
            // add-in to source feature only db_xref="taxon:xyz" where present
            if (f.getType().equals("source") && tax!=null) {
                StringTools.writeKeyValueLine("", "/db_xref=\"taxon:"+tax.getNCBITaxID()+"\"", 21, this.getLineWidth(), this.getPrintStream());
            }
            // add-in other dbxrefs where present
            for (Iterator j = f.getRankedCrossRefs().iterator(); j.hasNext(); ) {
                RankedCrossRef rcr = (RankedCrossRef)j.next();
                CrossRef cr = rcr.getCrossRef();
                StringTools.writeKeyValueLine("", "/db_xref=\""+cr.getDbname()+":"+cr.getAccession()+"\"", 21, this.getLineWidth(), this.getPrintStream());
            }
        }
        
        if (rs.getAlphabet()==AlphabetManager.alphabetForName("DNA")) {
            // BASE COUNT     1510 a   1074 c    835 g   1609 t
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
            this.getPrintStream().print(BASE_COUNT_TAG+"    ");
            this.getPrintStream().print(aCount + " a   ");
            this.getPrintStream().print(cCount + " c   ");
            this.getPrintStream().print(gCount + " g   ");
            this.getPrintStream().print(tCount + " t    ");
            this.getPrintStream().println(oCount + " others");
        }
        
        this.getPrintStream().println(START_SEQUENCE_TAG);
        // sequence stuff
        Symbol[] syms = (Symbol[])rs.toList().toArray(new Symbol[0]);
        int lines = 0;
        int symCount = 0;
        for (int i = 0; i < syms.length; i++) {
            if (symCount % 60 == 0) {
                if (lines > 0) this.getPrintStream().print("\n"); // newline from previous line
                int lineNum = (lines*60) + 1;
                this.getPrintStream().print(StringTools.leftPad(""+lineNum,9));
                lines++;
            }
            if (symCount % 10 == 0) this.getPrintStream().print(" ");
            try {
                this.getPrintStream().print(tok.tokenizeSymbol(syms[i]));
            } catch (IllegalSymbolException e) {
                throw new RuntimeException("Found illegal symbol: "+syms[i]);
            }
            symCount++;
        }
        this.getPrintStream().print("\n");
        this.getPrintStream().println(END_SEQUENCE_TAG);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultFormat() {
        return GENBANK_FORMAT;
    }
}

