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
import java.util.Vector;
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
import org.biojava.utils.ParseErrorListener;
import org.biojavax.Comment;
import org.biojavax.CrossRef;
import org.biojavax.DocRef;
import org.biojavax.Note;
import org.biojavax.RankedCrossRef;
import org.biojavax.RankedDocRef;
import org.biojavax.SimpleComment;
import org.biojavax.SimpleCrossRef;
import org.biojavax.SimpleDocRef;
import org.biojavax.SimpleRankedCrossRef;
import org.biojavax.SimpleRankedDocRef;
import org.biojavax.SimpleRichAnnotation;
import org.biojavax.bio.db.RichObjectFactory;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.taxa.NCBITaxon;
import org.biojavax.bio.taxa.SimpleNCBITaxon;
import org.biojavax.ontology.ComparableTerm;

/**
 * Format reader for GenBank files. Converted from the old style io to
 * the new by working from <code>EmblLikeFormat</code>.
 *
 * @author Thomas Down
 * @author Thad	Welch
 * Added GenBank header	info to	the sequence annotation. The ACCESSION header
 * tag is not included.	Stored in sequence.getName().
 * @author Greg	Cox
 * @author Keith James
 * @author Matthew Pocock
 * @author Ron Kuhn
 * Implemented nice RichSeq stuff.
 * @author Richard Holland
 */
public class GenbankFormat
        implements RichSequenceFormat {
    public static final String DEFAULT_FORMAT = "GENBANK";
    
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
    
    private static ComparableTerm ACCESSION_TERM = null;
    public static ComparableTerm getAccessionTerm() {
        if (ACCESSION_TERM==null) ACCESSION_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("ACCESSION");
        return ACCESSION_TERM;
    }   
    private static ComparableTerm KERYWORDS_TERM = null;
    private static ComparableTerm getKeywordsTerm() {
        if (KERYWORDS_TERM==null) KERYWORDS_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("KEYWORDS");
        return KERYWORDS_TERM;
    }   
    private static ComparableTerm MODIFICATION_TERM = null; 
    private static ComparableTerm getModificationTerm() {
        if (MODIFICATION_TERM==null) MODIFICATION_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("MDAT");
        return MODIFICATION_TERM;
    }   
    private static ComparableTerm STRANDED_TERM = null; 
    private static ComparableTerm getStrandedTerm() {
        if (STRANDED_TERM==null) STRANDED_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("STRANDED");
        return STRANDED_TERM;
    }   
    private static ComparableTerm GENBANK_TERM = null; 
    private static ComparableTerm getGenBankTerm() {
        if (GENBANK_TERM==null) GENBANK_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("GenBank");
        return GENBANK_TERM;
    }   
    
    private boolean elideSymbols = false;
    
    /**
     * The line width for output.
     */
    protected int lineWidth = 80;
    
    /**
     * Retrive the current line width.
     *
     * @return the line width
     */
    public int getLineWidth() {
        return lineWidth;
    }
    
    /**
     * Set the line width.
     * <p>
     * When writing, the lines of sequence will never be longer than the line
     * width.
     *
     * @param width the new line width
     */
    public void setLineWidth(int width) {
        this.lineWidth = width;
    }
    
    public boolean readSequence(BufferedReader reader,
            SymbolTokenization symParser,
            SeqIOListener listener)
            throws IllegalSymbolException, IOException, ParseException {
        if (!(listener instanceof RichSeqIOListener)) throw new IllegalArgumentException("Only accepting RichSeqIOListeners today");
        return this.readRichSequence(reader,symParser,(RichSeqIOListener)listener);
    }
    
    /**
     * Reads a sequence from the specified reader using the Symbol
     * parser and Sequence Factory provided. The sequence read in must
     * be in Genbank format.
     *
     * @return boolean True if there is another sequence in the file; false
     * otherwise
     */
    public boolean readRichSequence(BufferedReader reader,
            SymbolTokenization symParser,
            RichSeqIOListener rlistener)
            throws IllegalSymbolException, IOException, ParseException {
        String line;
        boolean hasAnotherSequence    = true;
        boolean hasInternalWhitespace = false;
        
        rlistener.startSequence();
        
        rlistener.setNamespace(RichObjectFactory.getDefaultLocalNamespace());
        
        // Get an ordered list of key->value pairs in array-tuples
        String sectionKey = null;
        NCBITaxon tax = null;
        String organism = null;
        String accession = null;
        do {
            List section = this.readSection(reader);
            sectionKey = ((String[])section.get(0))[0];
            
            // process section-by-section
            if (sectionKey.equals(LOCUS_TAG)) {
                String loc = ((String[])section.get(0))[1];
                String regex = "^(\\S+)\\s+\\d+\\s+bp\\s+([dms]s-)?(\\S+)\\s+(circular|linear)?\\s+(\\S+)\\s+(\\S+)$";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(loc);
                if (m.matches()) {
                    if (!symParser.getAlphabet().getName().equals(m.group(3)))
                        throw new ParseException("Genbank alphabet does not match expected alphabet in parser");
                    rlistener.setName(m.group(1));
                    rlistener.setDivision(m.group(5));
                    rlistener.addSequenceProperty(getModificationTerm(),m.group(6));
                    // Optional extras
                    String stranded = m.group(2);
                    String circular = m.group(4);
                    if (stranded!=null) rlistener.addSequenceProperty(getStrandedTerm(),stranded);
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
                    rlistener.addSequenceProperty(getAccessionTerm(),accs[i].trim());
                }
            } else if (sectionKey.equals(VERSION_TAG)) {
                String ver = ((String[])section.get(0))[1];
                String regex = "^(\\S+?)\\.(\\d+)\\s+GI:(\\S+)$";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(ver);
                if (m.matches()) {
                    rlistener.setVersion(Integer.parseInt(m.group(2)));
                    rlistener.setIdentifier(m.group(3));
                } else {
                    throw new ParseException("Bad version line found: "+ver);
                }
            } else if (sectionKey.equals(KEYWORDS_TAG)) {
                rlistener.addSequenceProperty(getKeywordsTerm(), ((String[])section.get(0))[1]);
            } else if (sectionKey.equals(SOURCE_TAG)) {
                // ignore - can get all this from the first feature
            } else if (sectionKey.equals(REFERENCE_TAG)) {
                // first line of section has rank and location
                int ref_rank;
                int ref_start;
                int ref_end;
                String ref = ((String[])section.get(0))[1];
                String regex = "^(\\d+)\\s+\\(bases\\s+(\\d+)\\s+to\\s+(\\d+)\\)$";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(ref);
                if (m.matches()) {
                    ref_rank = Integer.parseInt(m.group(1));
                    ref_start = Integer.parseInt(m.group(2));
                    ref_end = Integer.parseInt(m.group(3));
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
                    if (key.equals("AUTHORS")) authors = val;
                    if (key.equals("TITLE")) title = val;
                    if (key.equals("JOURNAL")) journal = val;
                    if (key.equals("MEDLINE")) medline = val;
                    if (key.equals("PUBMED")) pubmed = val;
                    if (key.equals("REMARK")) authors = val;
                }
                // create the pubmed crossref and assign to the bioentry
                CrossRef pcr = null;
                if (pubmed!=null) {
                    pcr = (CrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{"PUBMED", pubmed});
                    RankedCrossRef rpcr = new SimpleRankedCrossRef(pcr, 0);
                    rlistener.setRankedCrossRef(rpcr);
                }
                // create the medline crossref and assign to the bioentry
                CrossRef mcr = null;
                if (medline!=null) {
                    mcr = (CrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{"MEDLINE", medline});
                    RankedCrossRef rmcr = new SimpleRankedCrossRef(mcr, 0);
                    rlistener.setRankedCrossRef(rmcr);
                }
                // create the docref object
                try {
                    DocRef dr = (DocRef)RichObjectFactory.getObject(SimpleDocRef.class,new Object[]{authors,journal});
                    if (title!=null) dr.setTitle(title);
                    // assign either the pubmed or medline to the docref
                    if (pcr!=null) dr.setCrossref(pcr);
                    else if (mcr!=null) dr.setCrossref(mcr);
                    // assign the remarks
                    dr.setRemark(remark);
                    // assign the docref to the bioentry
                    RankedDocRef rdr = new SimpleRankedDocRef(dr, new Integer(ref_start), new Integer(ref_end), ref_rank);
                    rlistener.setRankedDocRef(rdr);
                } catch (ChangeVetoException e) {
                    throw new ParseException(e);
                }
            } else if (sectionKey.equals(COMMENT_TAG)) {
                // Set up some comments
                rlistener.setComment(((String[])section.get(0))[1]);
            } else if (sectionKey.equals(FEATURE_TAG)) {
                // starting from second line of input, start a new feature whenever we come across
                // a value that does not start with "
                boolean seenAFeature = false;
                for (int i = 1 ; i < section.size(); i++) {
                    String key = ((String[])section.get(i))[0];
                    String val = ((String[])section.get(i))[1];
                    if (key.startsWith("/")) {
                        key = key.substring(1); // strip leading slash
                        val = val.trim().replaceAll("\"",""); // strip quotes
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
                        templ.sourceTerm = getGenBankTerm();
                        templ.typeTerm = RichObjectFactory.getDefaultOntology().getOrCreateTerm(key);
                        templ.featureRelationshipSet = new TreeSet();
                        templ.rankedCrossRefs = new TreeSet();
                        String tidyLocStr = val.replaceAll("\\s+","");
                        templ.location = GenbankLocationParser.parseLocation(RichObjectFactory.getDefaultLocalNamespace(), accession, tidyLocStr);
                        rlistener.startFeature(templ);
                        seenAFeature = true;
                    }
                }
                if (seenAFeature) rlistener.endFeature();
            } else if (sectionKey.equals(BASE_COUNT_TAG)) {
                // ignore - can calculate from sequence content later if needed
            } else if (sectionKey.equals(START_SEQUENCE_TAG) && !this.elideSymbols) {
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
    
    private List readSection(BufferedReader br) throws ParseException {
        List section = new ArrayList();
        String line;
        String currKey = null;
        StringBuffer currVal = new StringBuffer();
        boolean done = false;
        int linecount = 0;
        
        //s0-8 word s1-7 value
        //s21 word = value
        String regex = "^(\\s{0,8}(\\S+?)\\s{1,7}(.*)|\\s{21}(\\S+?)=(.*))$";
        Pattern p = Pattern.compile(regex);
        try {
            while (!done) {
                br.mark(160);
                line = br.readLine();
                if (line==null || (line.charAt(0)!=' ' && linecount++>0)) {
                    // dump out last part of section
                    section.add(new String[]{currKey,currVal.toString()});
                    br.reset();
                    done = true;
                } else {
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        // new key
                        if (currKey!=null) section.add(new String[]{currKey,currVal.toString()});
                        currKey = m.group(2)==null?m.group(4):m.group(2);
                        currVal = new StringBuffer();
                        currVal.append((m.group(2)==null?m.group(5):m.group(3)).trim());
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
    
    public void	writeSequence(Sequence seq, PrintStream os)
    throws IOException {
        if (!(seq instanceof RichSequence)) throw new IllegalArgumentException("Sorry, only RichSequence objects accepted");
        this.writeRichSequence((RichSequence)seq, os);
    }
    
    public void	writeRichSequence(RichSequence seq, PrintStream os)
    throws IOException {
        writeRichSequence(seq, getDefaultFormat(), os);
    }
    
    public void writeSequence(Sequence seq, String format, PrintStream os) throws IOException {
        if (!(seq instanceof RichSequence)) throw new IllegalArgumentException("Sorry, only RichSequence objects accepted");
        this.writeRichSequence((RichSequence)seq, format, os);
    }
    
    /**
     * <code>writeSequence</code> writes a sequence to the specified
     * <code>PrintStream</code>, using the specified format.
     *
     * @param seq a <code>Sequence</code> to write out.
     * @param format a <code>String</code> indicating which sub-format
     * of those available from a particular
     * <code>SequenceFormat</code> implemention to use when
     * writing.
     * @param os a <code>PrintStream</code> object.
     *
     * @exception IOException if an error occurs.
     * @deprecated use writeSequence(Sequence seq, PrintStream os)
     */
    public void writeRichSequence(RichSequence rs, String format, PrintStream os) throws IOException {
        
        // Genbank only really - others are treated identically for now
        if (!(
                format.equalsIgnoreCase("GENBANK") ||
                format.equalsIgnoreCase("GENPEPT") ||
                format.equalsIgnoreCase("REFSEQ:PROTEIN")
                ))
            throw new IllegalArgumentException("Unknown format: "+format);
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
        String mdat = "";
        for (Iterator i = notes.iterator(); i.hasNext(); ) {
            Note n = (Note)i.next();
            if (n.getTerm().equals(getStrandedTerm())) stranded=n.getValue();
            else if (n.getTerm().equals(getModificationTerm())) mdat=n.getValue();
            else if (n.getTerm().equals(getAccessionTerm())) accessions = accessions+" "+n.getValue();
        }
        
        // locus(name) + length + alpha + div + date line
        StringBuffer locusLine = new StringBuffer();
        locusLine.append(RichSequenceFormat.Tools.rightPad(rs.getName(),10));
        locusLine.append(RichSequenceFormat.Tools.leftPad(""+rs.length(),7));
        locusLine.append(" bp ");
        locusLine.append(RichSequenceFormat.Tools.leftPad(stranded,3));
        locusLine.append(RichSequenceFormat.Tools.rightPad(rs.getAlphabet().getName(),6));
        locusLine.append(RichSequenceFormat.Tools.rightPad(rs.getCircular()?"circular":"",10));
        locusLine.append(RichSequenceFormat.Tools.rightPad(rs.getDivision()==null?"":rs.getDivision(),10));
        locusLine.append(mdat);
        this.writeWrappedLine(LOCUS_TAG, 12, locusLine.toString(), os);
        
        // definition line
        this.writeWrappedLine(DEFINITION_TAG, 12, rs.getDescription(), os);
        
        // accession line
        this.writeWrappedLine(ACCESSION_TAG, 12, accessions, os);
        
        // version + gi line
        String version = accession+"."+rs.getVersion();
        if (rs.getIdentifier()!=null) version = version + "  GI:"+rs.getIdentifier();
        this.writeWrappedLine(VERSION_TAG, 12, version, os);
        
        // keywords line
        String keywords = null;
        for (Iterator n = notes.iterator(); n.hasNext(); ) {
            Note nt = (Note)n.next();
            if (nt.getTerm().equals(getKeywordsTerm())) {
                if (keywords==null) keywords = nt.getValue();
                else keywords = keywords+" "+nt.getValue();
            }
        }
        if (keywords==null) keywords =".";
        this.writeWrappedLine(KEYWORDS_TAG, 12, keywords, os);
        
        // source line (from taxon)
        //   organism line
        NCBITaxon tax = rs.getTaxon();
        if (tax!=null) {
            String[] sciNames = (String[])tax.getNames(NCBITaxon.SCIENTIFIC).toArray(new String[0]);
            if (sciNames.length>0) {
                this.writeWrappedLine(SOURCE_TAG, 12, sciNames[0], os);
                this.writeWrappedLine("  "+ORGANISM_TAG, 12, sciNames[0], os);
            }
        }
        
        // references - rank (bases x to y)
        for (Iterator r = rs.getRankedDocRefs().iterator(); r.hasNext(); ) {
            RankedDocRef rdr = (RankedDocRef)r.next();
            DocRef d = rdr.getDocumentReference();
            this.writeWrappedLine(REFERENCE_TAG, 12, rdr.getRank()+"  (bases "+rdr.getStart()+" to "+rdr.getEnd()+")", os);
            if (d.getAuthors()!=null) this.writeWrappedLine("  "+AUTHORS_TAG, 12, d.getAuthors(), os);
            this.writeWrappedLine("  "+TITLE_TAG, 12, d.getTitle(), os);
            this.writeWrappedLine("  "+JOURNAL_TAG, 12, d.getLocation(), os);
            CrossRef c = d.getCrossref();
            if (c!=null) this.writeWrappedLine("  "+c.getDbname().toUpperCase(), 12, c.getAccession(), os);
            if (d.getRemark()!=null) this.writeWrappedLine("  "+REMARK_TAG, 12, d.getRemark(), os);
        }
        
        // comments - if any
        if (!rs.getComments().isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (Iterator i = rs.getComments().iterator(); i.hasNext(); ) {
                Comment c = (SimpleComment)i.next();
                sb.append(c.getComment());
                if (i.hasNext()) sb.append("\n");
            }
            this.writeWrappedLine(COMMENT_TAG, 12, sb.toString(), os);
        }
        
        os.println("FEATURES             Location/Qualifiers");
        // feature_type     location
        for (Iterator i = rs.getFeatureSet().iterator(); i.hasNext(); ) {
            RichFeature f = (RichFeature)i.next();
            this.writeWrappedLocationLine("     "+f.getTypeTerm().getName(), 21, GenbankLocationParser.writeLocation((RichLocation)f.getLocation()), os);
            for (Iterator j = f.getNoteSet().iterator(); j.hasNext(); ) {
                Note n = (Note)j.next();
                // /key="val"
                this.writeWrappedLine("",21,"/"+n.getTerm().getName()+"=\""+n.getValue()+"\"", os);
            }
            // add-in to source feature only db_xref="taxon:xyz" where present
            if (f.getType().equals("source") && tax!=null) {
                this.writeWrappedLine("",21,"/db_xref=\"taxon:"+tax.getNCBITaxID()+"\"", os);
            }
            // add-in other dbxrefs where present
            for (Iterator j = f.getRankedCrossRefs().iterator(); j.hasNext(); ) {
                RankedCrossRef rcr = (RankedCrossRef)j.next();
                CrossRef cr = rcr.getCrossRef();
                this.writeWrappedLine("",21,"/db_xref=\""+cr.getDbname()+":"+cr.getAccession()+"\"", os);
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
            os.print("BASE COUNT    ");
            os.print(aCount + " a   ");
            os.print(cCount + " c   ");
            os.print(gCount + " g   ");
            os.print(tCount + " t    ");
            os.println(oCount + " others");
        }
        
        os.println(START_SEQUENCE_TAG);
        // sequence stuff
        Symbol[] syms = (Symbol[])rs.toList().toArray(new Symbol[0]);
        int lines = 0;
        int symCount = 0;
        for (int i = 0; i < syms.length; i++) {
            if (symCount % 60 == 0) {
                if (lines > 0) os.print("\n"); // newline from previous line
                int lineNum = (lines*60) + 1;
                os.print(RichSequenceFormat.Tools.leftPad(""+lineNum,9));
                lines++;
            }
            if (symCount % 10 == 0) os.print(" ");
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
    
    private void writeWrappedLine(String key, int indent, String text, PrintStream os) throws IOException {
        this._writeWrappedLine(key,indent,text,os,"\\s");
    }
    
    private void writeWrappedLocationLine(String key, int indent, String text, PrintStream os) throws IOException {
        this._writeWrappedLine(key,indent,text,os,",");
    }
    
    private void _writeWrappedLine(String key, int indent, String text, PrintStream os, String sep) throws IOException {
        text = text.trim();
        StringBuffer b = new StringBuffer();
        b.append(RichSequenceFormat.Tools.rightPad(key, indent));
        String[] lines = RichSequenceFormat.Tools.writeWordWrap(text, sep, this.getLineWidth()-indent);
        for (int i = 0; i<lines.length; i++) {
            if (i==0) b.append(lines[i]);
            else b.append(RichSequenceFormat.Tools.leftIndent(lines[i],indent));
            // print line before continuing to next one
            os.println(b.toString());
            b.setLength(0);
        }
    }
    
    /**
     * <code>getDefaultFormat</code> returns the String identifier for
     * the default format.
     *
     * @return a <code>String</code>.
     * @deprecated
     */
    public String getDefaultFormat() {
        return DEFAULT_FORMAT;
    }
    
    public boolean getElideSymbols() {
        return elideSymbols;
    }
    
    /**
     * Use this method to toggle reading of sequence data. If you're only
     * interested in header data set to true.
     * @param elideSymbols set to true if you don't want the sequence data.
     */
    public void setElideSymbols(boolean elideSymbols) {
        this.elideSymbols = elideSymbols;
    }
    
    
    private Vector mListeners = new Vector();
    
    /**
     * Adds a parse error listener to the list of listeners if it isn't already
     * included.
     *
     * @param theListener Listener to be added.
     */
    public synchronized void addParseErrorListener(ParseErrorListener theListener) {
        if (mListeners.contains(theListener) == false) {
            mListeners.addElement(theListener);
        }
    }
    
    /**
     * Removes a parse error listener from the list of listeners if it is
     * included.
     *
     * @param theListener Listener to be removed.
     */
    public synchronized void removeParseErrorListener(ParseErrorListener theListener) {
        if (mListeners.contains(theListener) == true) {
            mListeners.removeElement(theListener);
        }
    }
}

