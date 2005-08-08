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

package	org.biojava.bio.seq.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.biojava.bio.seq.Feature;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.ParseErrorEvent;
import org.biojava.utils.ParseErrorListener;
import org.biojavax.CrossRef;
import org.biojavax.DocRef;
import org.biojavax.Note;
import org.biojavax.RankedCrossRef;
import org.biojavax.RankedDocRef;
import org.biojavax.SimpleCrossRef;
import org.biojavax.SimpleDocRef;
import org.biojavax.SimpleRankedCrossRef;
import org.biojavax.SimpleRankedDocRef;
import org.biojavax.SimpleRichAnnotation;
import org.biojavax.bio.db.RichObjectFactory;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.SimpleRichLocation;
import org.biojavax.bio.seq.io.RichSeqIOListener;
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
        implements SequenceFormat,
        Serializable,
        org.biojava.utils.ParseErrorListener,
        org.biojava.utils.ParseErrorSource {
    public static final String DEFAULT = "GENBANK";
    
    protected static final String LOCUS_TAG = "LOCUS";
    protected static final String SIZE_TAG = "SIZE";
    protected static final String STRAND_NUMBER_TAG = "STRANDS";
    protected static final String TYPE_TAG = "TYPE";
    protected static final String CIRCULAR_TAG = "CIRCULAR";
    protected static final String DIVISION_TAG = "DIVISION";
    protected static final String DATE_TAG = "MDAT";
    
    protected static final String ACCESSION_TAG = "ACCESSION";
    protected static final String VERSION_TAG = "VERSION";
    protected static final String GI_TAG = "GI";
    protected static final String KEYWORDS_TAG = "KW";
    protected static final String DEFINITION_TAG = "DEFINITION";
    protected static final String SOURCE_TAG = "SOURCE";
    protected static final String ORGANISM_TAG = "ORGANISM";
    protected static final String REFERENCE_TAG = "REFERENCE";
    protected static final String COORDINATE_TAG = "COORDINATE";
    protected static final String REF_ACCESSION_TAG = "";
    protected static final String AUTHORS_TAG = "AUTHORS";
    protected static final String TITLE_TAG = "TITLE";
    protected static final String JOURNAL_TAG = "JOURNAL";
    protected static final String PUBMED_TAG = "PUBMED";
    protected static final String MEDLINE_TAG = "MEDLINE";
    protected static final String COMMENT_TAG = "COMMENT";
    protected static final String FEATURE_TAG = "FEATURES";
    protected static final String BASE_COUNT_TAG = "BASE";
    protected static final String FEATURE_FLAG = "FT";
    protected static final String START_SEQUENCE_TAG = "ORIGIN";
    protected static final String END_SEQUENCE_TAG = "//";
    
    protected static final String FEATURE_LINE_PREFIX = "     ";
    
    private static final ComparableTerm ACCESSION_TERM =
            RichObjectFactory.getDefaultOntology().getOrCreateTerm("ACCESSION");
    private static final ComparableTerm KEYWORDS_TERM =
            RichObjectFactory.getDefaultOntology().getOrCreateTerm("KEYWORDS");
    private static final ComparableTerm MODIFICATION_DATE_TERM =
            RichObjectFactory.getDefaultOntology().getOrCreateTerm("MDAT");
    private static final ComparableTerm GENBANK_SOURCE_TERM =
            RichObjectFactory.getDefaultOntology().getOrCreateTerm("GenBank");
    
    private Vector mListeners = new Vector();
    private boolean elideSymbols = false;
    
    /**
     * Reads a sequence from the specified reader using the Symbol
     * parser and Sequence Factory provided. The sequence read in must
     * be in Genbank format.
     *
     * @return boolean True if there is another sequence in the file; false
     * otherwise
     */
    public boolean readSequence(BufferedReader reader,
            SymbolTokenization symParser,
            SeqIOListener listener)
            throws IllegalSymbolException, IOException, ParseException {
        String line;
        boolean hasAnotherSequence    = true;
        boolean hasInternalWhitespace = false;
        if (listener instanceof RichSeqIOListener) {
            RichSeqIOListener rlistener = (RichSeqIOListener)listener;
            // Do it the proper way
            rlistener.startSequence();
            
            rlistener.setNamespace(RichObjectFactory.getDefaultNamespace());
            
            // Get an ordered list of key->value pairs in array-tuples
            String sectionKey = null;
            NCBITaxon tax = null;
            String organism = null;
            do {
                List section = this.readSection(reader);
                sectionKey = ((String[])section.get(0))[0];
                // process section-by-section
                if (sectionKey.equals(LOCUS_TAG)) {
                    String loc = ((String[])section.get(0))[1];
                    String regex = "^(\\S+)\\s+\\d+\\s+bp\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)$";
                    Pattern p = Pattern.compile(regex);
                    Matcher m = p.matcher(loc);
                    if (m.matches()) {
                        if (!symParser.getAlphabet().getName().equals(m.group(2)))
                            throw new ParseException("Genbank alphabet does not match expected alphabet in parser");
                        rlistener.setName(m.group(1));
                        rlistener.setDivision(m.group(3));
                        rlistener.addSequenceProperty(MODIFICATION_DATE_TERM,m.group(4));
                    } else {
                        throw new ParseException("Bad locus line found: "+loc);
                    }
                } else if (sectionKey.equals(DEFINITION_TAG)) {
                    rlistener.setDescription(((String[])section.get(0))[1]);
                } else if (sectionKey.equals(ACCESSION_TAG)) {
                    // if multiple accessions, store only first as accession,
                    // and store rest in annotation
                    String[] accs = ((String[])section.get(0))[1].split("\\s+");
                    rlistener.setAccession(accs[0].trim());
                    for (int i = 1; i < accs.length; i++) {
                        rlistener.addSequenceProperty(ACCESSION_TERM,accs[i].trim());
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
                    rlistener.addSequenceProperty(KEYWORDS_TERM, ((String[])section.get(0))[1]);
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
                    for (int i = 1; i < section.size(); i++) {
                        String key = ((String[])section.get(i))[0];
                        String val = ((String[])section.get(i))[1];
                        if (key.equals("AUTHORS")) authors = val;
                        if (key.equals("TITLE")) title = val;
                        if (key.equals("JOURNAL")) journal = val;
                        if (key.equals("MEDLINE")) medline = val;
                        if (key.equals("PUBMED")) pubmed = val;
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
                        // assign the docref to the bioentry
                        RankedDocRef rdr = new SimpleRankedDocRef(dr, new Integer(ref_start), new Integer(ref_end), ref_rank);
                        rlistener.setRankedDocRef(rdr);
                    } catch (ChangeVetoException e) {
                        throw new ParseException(e);
                    }
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
                                    String accession = m.group(2);
                                    if (dbname.equals("taxon")) {
                                        // Set the Taxon instead of a dbxref
                                        tax = (NCBITaxon)RichObjectFactory.getObject(SimpleNCBITaxon.class, new Object[]{Integer.valueOf(accession)});
                                        rlistener.setTaxon(tax);
                                        try {
                                            if (organism!=null) tax.addName(NCBITaxon.SCIENTIFIC,organism);
                                        } catch (ChangeVetoException e) {
                                            throw new ParseException(e);
                                        }
                                    } else {
                                        try {
                                            CrossRef cr = (CrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{dbname, accession});
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
                            // do location parsing - populate blocks
                            Set blocks = new HashSet();
                            RichLocation l = new SimpleRichLocation(0,0,0);
                            EmblLikeLocationParser p = new EmblLikeLocationParser("dummy");
                            Feature.Template ftempl = new Feature.Template();
                            try {
                                ftempl = p.parseLocation(val, ftempl);
                                int rank = 0;
                                for (Iterator b = ftempl.location.blockIterator(); b.hasNext(); ) {
                                    Location l2 = (Location)b.next();
                                    RichLocation rl2 = new SimpleRichLocation(l2.getMin(),l2.getMax(),rank++);
                                    blocks.add(rl2);
                                }
                                l.setBlocks(blocks);
                            } catch (Exception e) {
                                throw new ParseException("Bad location: "+val);
                            }
                            // end previous feature
                            if (seenAFeature) rlistener.endFeature();
                            // start next one, with lots of lovely info in it
                            RichFeature.Template templ = new RichFeature.Template();
                            templ.annotation = new SimpleRichAnnotation();
                            templ.location = l;
                            templ.sourceTerm = GENBANK_SOURCE_TERM;
                            templ.typeTerm = RichObjectFactory.getDefaultOntology().getOrCreateTerm(key);
                            templ.featureRelationshipSet = new HashSet();
                            templ.rankedCrossRefs = new HashSet();
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
        } else {
            // Do it the legacy way
            
            GenbankContext ctx = new GenbankContext(symParser, listener);
            ctx.addParseErrorListener(this);
            ctx.setElideSymbols(this.getElideSymbols());
            
            listener.startSequence();
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(END_SEQUENCE_TAG)) {
                    // To close the StreamParser encapsulated in the
                    // GenbankContext object
                    ctx.processLine(line);
                    
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
                    
                    listener.endSequence();
                    return hasAnotherSequence;
                }
                ctx.processLine(line);
            }
            
            throw new IOException("Premature end of stream for GENBANK");
        }
    }
    
    private List readSection(BufferedReader br) throws ParseException {
        List section = new ArrayList();
        String line;
        String currKey = null;
        StringBuffer currVal = new StringBuffer();
        boolean done = false;
        int linecount = 0;
        String regex = "^(\\s{0,8}|\\s{21})(\\S+?)(\\s{1,7}|=)(.*)$";  // <=8sp+word+1-7sp+value OR 21sp+word+=+value
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
                        currKey = m.group(2);
                        currVal = new StringBuffer();
                        currVal.append(m.group(4).trim());
                    } else {
                        line = line.trim();
                        // concatted line or SEQ START/END line?
                        if (line.equals(START_SEQUENCE_TAG) || line.equals(END_SEQUENCE_TAG)) currKey = line;
                        else {
                            currVal.append(" "); // space in between lines - can be removed later
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
        writeSequence(seq, getDefaultFormat(), os);
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
    public void writeSequence(Sequence seq, String format, PrintStream os) throws IOException {
        SeqFileFormer former;
        
        if (seq instanceof RichSequence) {
            // Do it the clever way
            
            // Genbank only - why use the others there when you can just do a translate on the sequence?
            if (!(
                    format.equalsIgnoreCase("GENBANK") ||
                    format.equalsIgnoreCase("GENPEPT") ||
                    format.equalsIgnoreCase("REFSEQ:PROTEIN")
                    ))
                throw new IllegalArgumentException("Unknown format: "+format);
            
            RichSequence rs = (RichSequence)seq;
            Set notes = rs.getNoteSet();
            SymbolTokenization tok;
            try {
                tok = rs.getAlphabet().getTokenization("token");
            } catch (Exception e) {
                throw new RuntimeException("Unable to get alphabet tokenizer",e);
            }
            
            // locus(name) + length + alpha + div + date line
            
            // definition line
            this.writeWrappedLine("DEFINITION", 12, rs.getDescription(), os);
            
            // accession line
            String accession = rs.getAccession();
            String accessions = accession;
            for (Iterator n = notes.iterator(); n.hasNext(); ) {
                Note nt = (Note)n.next();
                if (nt.getTerm().equals(ACCESSION_TERM)) accessions = accessions+" "+nt.getValue();
            }
            this.writeWrappedLine("ACCESSION", 12, accessions, os);
            
            // version + gi line
            String version = accession+"."+rs.getVersion();
            if (rs.getIdentifier()!=null) version = version + "  GI:"+rs.getIdentifier();
            this.writeWrappedLine("VERSION", 12, version, os);
            
            // keywords line
            String keywords = null;
            for (Iterator n = notes.iterator(); n.hasNext(); ) {
                Note nt = (Note)n.next();
                if (nt.getTerm().equals(KEYWORDS_TERM)) {
                    if (keywords==null) keywords = nt.getValue();
                    else keywords = keywords+" "+nt.getValue();
                }
            }
            if (keywords==null) keywords =".";
            this.writeWrappedLine("KEYWORDS", 12, keywords, os);
            
            // source line (from taxon)
            //   organism line
            NCBITaxon tax = rs.getTaxon();
            if (tax!=null) {
                String[] sciNames = (String[])tax.getNames(NCBITaxon.SCIENTIFIC).toArray(new String[0]);
                if (sciNames.length>0) {
                    this.writeWrappedLine("SOURCE", 12, ""+sciNames[0], os);
                    this.writeWrappedLine("  ORGANISM", 12, ""+sciNames[0], os);
                }
            }
            
            // references - rank (bases x to y)
            for (Iterator r = rs.getRankedDocRefs().iterator(); r.hasNext(); ) {
                RankedDocRef rdr = (RankedDocRef)r.next();
                DocRef d = rdr.getDocumentReference();
                this.writeWrappedLine("REFERENCE", 12, rdr.getRank()+"  (bases "+rdr.getStart()+" to "+rdr.getEnd()+")", os);
                if (d.getAuthors()!=null) this.writeWrappedLine("  AUTHORS", 12, d.getAuthors(), os);
                this.writeWrappedLine("  TITLE", 12, d.getTitle(), os);
                this.writeWrappedLine("  JOURNAL", 12, d.getLocation(), os);
                if (d.getAuthors()!=null) this.writeWrappedLine("  AUTHORS", 12, d.getAuthors(), os);
                CrossRef c = d.getCrossref();
                if (c!=null) this.writeWrappedLine("  "+c.getDbname().toUpperCase(), 12, c.getAccession(), os);
            }
            
            os.println("FEATURES             Location/Qualifiers");
            // feature_type     location
            //      /key="val"
            //      add-in to source db_xref="taxon:xyz"
            //      add-in from seqfeat_dbxref where necessary
            
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
            
            os.println("ORIGIN");
            // sequence stuff
            Symbol[] syms = (Symbol[])rs.toList().toArray(new Symbol[0]);
            int fullLine = rs.length() / 60;
            int partLine = rs.length() % 60;
            int lineCount = fullLine;
            if (partLine > 0) lineCount++;
            int lineLens[] = new int[lineCount];
            // All lines are 60, except last (if present)
            Arrays.fill(lineLens, 60);
            if (partLine > 0) lineLens[lineCount - 1] = partLine;
            // Prepare line 80 characters wide, sequence is subset of this
            char [] emptyLine = new char [80];
            StringBuffer sq = new StringBuffer();
            StringBuffer ub = new StringBuffer();
            for (int i = 0; i < lineLens.length; i++) {
                sq.setLength(0);
                ub.setLength(0);
                // How long is this chunk?
                int len = lineLens[i];
                // Prep the whitespace
                Arrays.fill(emptyLine, ' ');
                sq.append(emptyLine);
                // Prepare a Symbol array same length as chunk
                Symbol[] sa = new Symbol[len];
                // Get symbols and format into blocks of tokens
                System.arraycopy(syms, i * 60, sa, 0, len);
                for (int j = 0; j < sa.length; j++) {
                    try {
                        ub.append(tok.tokenizeSymbol(sa[j]));
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to set symbol at position "+(i*60)+j,e);
                    }
                    if ((j + 1) % 10 == 0) ub.append(' ');
                }
                String blocks = ub.toString();
                sq.replace(10, blocks.length() + 10, blocks);
                // Calculate the running residue count and add to the line
                String count = Integer.toString((i * 60) + 1);
                sq.replace((9 - count.length()), 9, count);
                // Print formatted sequence line
                os.println(sq);
            }
            os.println("//");
        } else {
            // Do it the old-fashioned boring way
            
            if (format.equalsIgnoreCase("GENBANK"))
                former = new GenbankFileFormer();
            else if (format.equalsIgnoreCase("GENPEPT"))
                former = new GenpeptFileFormer();
            else if (format.equalsIgnoreCase("REFSEQ:PROTEIN"))
                former = new ProteinRefSeqFileFormer();
            else
                throw new IllegalArgumentException("Unknown format '"
                        + format
                        + "'");
            former.setPrintStream(os);
            
            SeqIOEventEmitter emitter =
                    new SeqIOEventEmitter(GenEmblPropertyComparator.INSTANCE,
                    GenEmblFeatureComparator.INSTANCE);
            
            emitter.getSeqIOEvents(seq, former);
        }
    }
    
    private void writeWrappedLine(String key, int indent, String text, PrintStream os) throws IOException {
        text = text.trim();
        StringBuffer b = new StringBuffer();
        b.append(key);
        // right-pad key to indent size
        while (b.length()<indent) b.append(" "); // yuck!
        // write out chunks of text up to linesize
        int textsize = 80-indent;
        int line = 0;
        while (line*textsize < text.length()) {
            if (line>0) for (int i = 0; i < indent; i++) b.append(" "); // yuck!
            int start = line*textsize;
            int end = Math.min(start+textsize,text.length());
            b.append(text.substring(start,end));
            line++;
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
        return DEFAULT;
    }
    
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
    public synchronized void removeParseErrorListener(
            ParseErrorListener theListener) {
        if (mListeners.contains(theListener) == true) {
            mListeners.removeElement(theListener);
        }
    }
    
    /**
     * This method determines the behaviour when a bad line is processed.
     * Some options are to log the error, throw an exception, ignore it
     * completely, or pass the event through.
     * <P>
     * This method should be overwritten when different behavior is desired.
     *
     * @param theEvent The event that contains the bad line and token.
     */
    public void BadLineParsed(org.biojava.utils.ParseErrorEvent theEvent) {
        notifyParseErrorEvent(theEvent);
    }
    
// Protected methods
    /**
     * Passes the event on to all the listeners registered for ParseErrorEvents.
     *
     * @param theEvent The event to be handed to the listeners.
     */
    protected void notifyParseErrorEvent(ParseErrorEvent theEvent) {
        Vector listeners;
        synchronized(this) {
            listeners = (Vector)mListeners.clone();
        }
        
        int lnrCount = listeners.size();
        for (int index = 0; index < lnrCount; index++) {
            ParseErrorListener client = (ParseErrorListener)listeners.elementAt(index);
            client.BadLineParsed(theEvent);
        }
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
}

