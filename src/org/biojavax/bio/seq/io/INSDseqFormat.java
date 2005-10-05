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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.ParseException;
import org.biojava.bio.seq.io.SeqIOListener;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.xml.PrettyXMLWriter;
import org.biojava.utils.xml.XMLWriter;
import org.biojavax.Comment;
import org.biojavax.CrossRef;
import org.biojavax.DocRef;
import org.biojavax.Namespace;
import org.biojavax.Note;
import org.biojavax.RankedCrossRef;
import org.biojavax.RankedDocRef;
import org.biojavax.RichAnnotation;
import org.biojavax.SimpleCrossRef;
import org.biojavax.SimpleDocRef;
import org.biojavax.SimpleRankedCrossRef;
import org.biojavax.SimpleRankedDocRef;
import org.biojavax.SimpleRichAnnotation;
import org.biojavax.RichObjectFactory;
import org.biojavax.SimpleNote;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.taxa.NCBITaxon;
import org.biojavax.bio.taxa.SimpleNCBITaxon;
import org.biojavax.ontology.ComparableTerm;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Format reader for INSDseq files. This version of INSDseq format will generate
 * and write RichSequence objects. Loosely Based on code from the old, deprecated,
 * org.biojava.bio.seq.io.GenbankXmlFormat object.
 *
 * Understands http://www.ebi.ac.uk/embl/Documentation/DTD/INSDSeq_v1.3.dtd.txt
 *
 * @author Alan Li (code based on his work)
 * @author Richard Holland
 */
public class INSDseqFormat implements RichSequenceFormat {
    
    /**
     * The name of this format
     */
    public static final String INSDSEQ_FORMAT = "INSDseq";
    
    public static final String INSDSEQS_GROUP_TAG = "INSDSet";
    public static final String INSDSEQ_TAG = "INSDSeq";
    
    protected static final String LOCUS_TAG = "INSDSeq_locus";
    protected static final String LENGTH_TAG = "INSDSeq_length";
    protected static final String TOPOLOGY_TAG = "INSDSeq_topology";
    protected static final String STRANDED_TAG = "INSDSeq_strandedness";
    protected static final String MOLTYPE_TAG = "INSDSeq_moltype";
    protected static final String DIVISION_TAG = "INSDSeq_division";
    protected static final String UPDATE_DATE_TAG = "INSDSeq_update-date";
    protected static final String CREATE_DATE_TAG = "INSDSeq_create-date";
    protected static final String DEFINITION_TAG = "INSDSeq_definition";
    protected static final String DATABASE_XREF_TAG = "INSDSeq_database-reference";
    
    protected static final String ACCESSION_TAG = "INSDSeq_primary-accession";
    protected static final String ACC_VERSION_TAG = "INSDSeq_accession-version";
    protected static final String SECONDARY_ACCESSIONS_GROUP_TAG = "INSDSeq_secondary-accessions";
    protected static final String SECONDARY_ACCESSION_TAG = "INSDSecondary-accn";
    
    protected static final String KEYWORDS_GROUP_TAG = "INSDSeq_keywords";
    protected static final String KEYWORD_TAG = "INSDKeyword";
    
    protected static final String SOURCE_TAG = "INSDSeq_source";
    protected static final String ORGANISM_TAG = "INSDSeq_organism";
    protected static final String TAXONOMY_TAG = "INSDSeq_taxonomy";
    
    protected static final String REFERENCES_GROUP_TAG = "INSDSeq_references";
    protected static final String REFERENCE_TAG = "INSDReference";
    protected static final String REFERENCE_LOCATION_TAG = "INSDReference_reference";
    protected static final String TITLE_TAG = "INSDReference_title";
    protected static final String JOURNAL_TAG = "INSDReference_journal";
    protected static final String PUBMED_TAG = "INSDReference_pubmed";
    protected static final String MEDLINE_TAG = "INSDReference_medline";
    protected static final String REMARK_TAG = "INSDReference_remark";
    protected static final String AUTHORS_GROUP_TAG = "INSDReference_authors";
    protected static final String AUTHOR_TAG = "INSDAuthor";
    
    protected static final String COMMENT_TAG = "INSDSeq_comment";
    
    protected static final String FEATURES_GROUP_TAG = "INSDSeq_feature-table";
    protected static final String FEATURE_TAG = "INSDFeature";
    protected static final String FEATURE_KEY_TAG = "INSDFeature_key";
    protected static final String FEATURE_LOC_TAG = "INSDFeature_location";
    protected static final String FEATUREQUALS_GROUP_TAG = "INSDFeature_quals";
    protected static final String FEATUREQUAL_TAG = "INSDQualifier";
    protected static final String FEATUREQUAL_NAME_TAG = "INSDQualifier_name";
    protected static final String FEATUREQUAL_VALUE_TAG = "INSDQualifier_value";
    
    protected static final String SEQUENCE_TAG = "INSDSeq_sequence";
    protected static final String CONTIG_TAG = "INSDSeq_contig";
    
    /**
     * Implements some INSDseq-specific terms.
     */
    public static class Terms extends RichSequenceFormat.Terms {
        private static ComparableTerm INSDSEQ_TERM = null;
        
        /**
         * Getter for the INSDseq term
         * @return The INSDseq Term
         */
        public static ComparableTerm getINSDseqTerm() {
            if (INSDSEQ_TERM==null) INSDSEQ_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("INSDseq");
            return INSDSEQ_TERM;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int getLineWidth() {
        return 0;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setLineWidth(int width) {
        if (width!=0) throw new IllegalArgumentException("XML files don't have widths");
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
    
    // contains all the sequences from the file
    private List sequenceBuffer = null;
    private Iterator seqBufferIterator = null;
    
    /**
     * {@inheritDoc}
     * NOTE: This reads the whole XML file and parses it into an internal
     * buffer, from which sequences are read by subsequent calls to this
     * method.
     */
    public boolean readRichSequence(BufferedReader reader,
            SymbolTokenization symParser,
            RichSeqIOListener rlistener,
            Namespace ns)
            throws IllegalSymbolException, IOException, ParseException {
        
        if (this.sequenceBuffer==null) {
            // load the whole lot into a buffer for now
            this.sequenceBuffer = new ArrayList();
            
            SAXParser m_xmlParser;
            INSDseqHandler m_handler;
            
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            try {
                m_xmlParser = factory.newSAXParser();
            } catch(ParserConfigurationException ex) {
                throw new ParseException(ex);
            } catch(SAXException ex) {
                throw new ParseException(ex);
            }
            
            InputSource source = new InputSource(reader);
            m_handler = new INSDseqHandler(this.sequenceBuffer);
            
            try {
                m_xmlParser.parse(source, m_handler);
            } catch(SAXException ex) {
                throw new ParseException(ex);
            }
            
            this.seqBufferIterator = sequenceBuffer.iterator();
        }
        
        // read the next sequence
        if (this.seqBufferIterator.hasNext()) this.readRichSequence((INSDseq)seqBufferIterator.next(),symParser,rlistener,ns);
        
        // return true if there are more in our buffer
        return this.seqBufferIterator.hasNext();
    }
    
    // converts an INSDseq object into an actual RichSequence object
    private void readRichSequence(INSDseq input,
            SymbolTokenization symParser,
            RichSeqIOListener rlistener,
            Namespace ns) throws IllegalSymbolException, ParseException {
        
        if (input.getContig()!=null) throw new ParseException("Cannot handle contigs yet");
        
        rlistener.startSequence();
        NCBITaxon tax = null;
        String organism = null;
        String accession = null;
                
        if (ns==null) ns=RichObjectFactory.getDefaultNamespace();
        rlistener.setNamespace(ns);
        
        // process in same order as if writing sequence to file
        
        rlistener.setName(input.getLocus().trim());
        if (input.getPrimaryAccession()!=null) {
            accession = input.getPrimaryAccession().trim();
            rlistener.setAccession(accession);
        }
        if (input.getAccessionVersion()!=null) {
            String parts[] = input.getAccessionVersion().trim().split("\\.");
            accession = parts[0];
            rlistener.setAccession(accession);
            if (parts.length>1) rlistener.setVersion(Integer.parseInt(parts[1]));
        }
        if (!input.getSecondaryAccessions().isEmpty()) {
            for (Iterator i = input.getSecondaryAccessions().iterator(); i.hasNext();) {
                rlistener.addSequenceProperty(Terms.getAccessionTerm(),((String)i.next()).trim());
            }
        }
        rlistener.setDivision(input.getDivision().trim());
        rlistener.addSequenceProperty(Terms.getMolTypeTerm(),input.getMoltype().trim());
        rlistener.addSequenceProperty(Terms.getModificationTerm(),input.getUpdateDate().trim());
        if (input.getStrandedness()!=null) rlistener.addSequenceProperty(Terms.getStrandedTerm(),input.getStrandedness().trim());
        if (input.getTopology()!=null && "circular".equals(input.getTopology().trim())) rlistener.setCircular(true);
        if (input.getDefinition()!=null) rlistener.setDescription(input.getDefinition().trim());
        if (!input.getKeywords().isEmpty()) {
            for (Iterator i = input.getKeywords().iterator(); i.hasNext();) {
                rlistener.addSequenceProperty(Terms.getKeywordsTerm(), ((String)i.next()).trim());
            }
        }
        if (input.getComment()!=null) rlistener.setComment(input.getComment().trim());
        if (input.getDatabaseXref()!=null) {
            // database_identifier; primary_identifier; secondary_identifier....
            String[] parts = input.getDatabaseXref().split(";");
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
            RankedCrossRef rcrossRef = new SimpleRankedCrossRef(crossRef, 1);
            rlistener.setRankedCrossRef(rcrossRef);
        }
        if (!input.getReferences().isEmpty()) {
            for (Iterator i = input.getReferences().iterator(); i.hasNext();) {
                INSDseqRef r = (INSDseqRef)i.next();
                // first line of section has rank and location
                int ref_rank;
                int ref_start = -999;
                int ref_end = -999;
                String ref = r.getLocation();
                String regex = "^(\\d+)\\s*(\\(bases\\s+(\\d+)\\s+to\\s+(\\d+)\\)|\\(sites\\))?";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(ref);
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
                String authors = "";
                for (Iterator j = r.getAuthors().iterator(); j.hasNext();) {
                    authors+=(String)j.next();
                    if (j.hasNext()) authors+=", ";
                }
                String title = r.getTitle();
                String journal = r.getJournal();
                String medline = r.getMedline();
                String pubmed = r.getPubmed();
                String remark = r.getRemark();
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
                // create the docref object
                try {
                    DocRef dr = (DocRef)RichObjectFactory.getObject(SimpleDocRef.class,new Object[]{authors,journal});
                    if (title!=null) dr.setTitle(title);
                    // assign either the pubmed or medline to the docref - medline gets priority
                    if (mcr!=null) dr.setCrossref(mcr);
                    else if (pcr!=null) dr.setCrossref(pcr);
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
            }
        }
        if (!input.getFeatures().isEmpty()) {
            for (Iterator i = input.getFeatures().iterator(); i.hasNext();) {
                INSDseqFeat f = (INSDseqFeat)i.next();
                // start next one, with lots of lovely info in it
                RichFeature.Template templ = new RichFeature.Template();
                templ.annotation = new SimpleRichAnnotation();
                templ.sourceTerm = Terms.getINSDseqTerm();
                templ.typeTerm = RichObjectFactory.getDefaultOntology().getOrCreateTerm(f.getKey());
                templ.featureRelationshipSet = new TreeSet();
                templ.rankedCrossRefs = new TreeSet();
                String tidyLocStr = f.getLocation().replaceAll("\\s+","");
                templ.location = GenbankLocationParser.parseLocation(ns, accession, tidyLocStr);
                rlistener.startFeature(templ);
                for (Iterator j = f.getQualifiers().iterator(); j.hasNext();) {
                    INSDseqFeatQual q = (INSDseqFeatQual)j.next();
                    String key = q.getName();
                    String val = q.getValue();
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
                }
                rlistener.endFeature();
            }
        }
        if (input.getSequence()!=null) {
            try {
                SymbolList sl = new SimpleSymbolList(symParser,
                        input.getSequence().trim().replaceAll("\\s+","").replaceAll("[\\.|~]","-"));
                rlistener.addSymbols(symParser.getAlphabet(),
                        (Symbol[])(sl.toList().toArray(new Symbol[0])),
                        0, sl.length());
            } catch (Exception e) {
                throw new ParseException(e);
            }
        }
        
        rlistener.endSequence();
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
     * Namespace is ignored as INSDseq has no concept of it.
     */
    public void	writeSequence(Sequence seq, PrintStream os, Namespace ns) throws IOException {
        this.writeSequence(seq, getDefaultFormat(), os, ns);
    }
    
    /**
     * {@inheritDoc}
     * Namespace is ignored as INSDseq has no concept of it.
     */
    public void writeSequence(Sequence seq, String format, PrintStream os, Namespace ns) throws IOException {
        // INSDseq only really - others are treated identically for now
        if (!(
                format.equalsIgnoreCase(INSDSEQ_FORMAT)
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
        List accessions = new ArrayList();
        List kws = new ArrayList();
        String stranded = null;
        String mdat = null;
        String moltype = rs.getAlphabet().getName();
        for (Iterator i = notes.iterator(); i.hasNext();) {
            Note n = (Note)i.next();
            if (n.getTerm().equals(Terms.getStrandedTerm())) stranded=n.getValue();
            else if (n.getTerm().equals(Terms.getModificationTerm())) mdat=n.getValue();
            else if (n.getTerm().equals(Terms.getMolTypeTerm())) moltype=n.getValue();
            else if (n.getTerm().equals(Terms.getAccessionTerm())) accessions.add(n.getValue());
            else if (n.getTerm().equals(Terms.getKeywordsTerm())) kws.add(n.getValue());
        }
        
        // make an XML writer
        PrintWriter pw = new PrintWriter(os);
        XMLWriter xml = new PrettyXMLWriter(pw);
        xml.printRaw("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        xml.printRaw("<!DOCTYPE INSDSeq PUBLIC \"-//EMBL-EBI//INSD INSDSeq/EN\" \"http://www.ebi.ac.uk/dtd/INSD_INSDSeq.dtd\">");
        
        // send it events based on contents of sequence object
        xml.openTag(INSDSEQS_GROUP_TAG);
        xml.openTag(INSDSEQ_TAG);
        
        xml.openTag(LOCUS_TAG);
        xml.print(rs.getName());
        xml.closeTag(LOCUS_TAG);
        
        xml.openTag(LENGTH_TAG);
        xml.print(""+rs.length());
        xml.closeTag(LENGTH_TAG);
        
        if (stranded!=null) {
            xml.openTag(STRANDED_TAG);
            xml.print(stranded);
            xml.closeTag(STRANDED_TAG);
        }
        
        if (moltype!=null) {
            xml.openTag(MOLTYPE_TAG);
            xml.print(moltype);
            xml.closeTag(MOLTYPE_TAG);
        }
        
        xml.openTag(TOPOLOGY_TAG);
        if (rs.getCircular()) xml.print("circular");
        else xml.print("linear");
        xml.closeTag(TOPOLOGY_TAG);
        
        if (rs.getDivision()!=null) {
            xml.openTag(DIVISION_TAG);
            xml.print(rs.getDivision());
            xml.closeTag(DIVISION_TAG);
        }
        
        if (mdat!=null) {
            xml.openTag(UPDATE_DATE_TAG);
            xml.print(mdat);
            xml.closeTag(UPDATE_DATE_TAG);
            xml.openTag(CREATE_DATE_TAG);
            xml.print(mdat);
            xml.closeTag(CREATE_DATE_TAG);
        }
        
        if (rs.getDescription()!=null) {
            xml.openTag(DEFINITION_TAG);
            xml.print(rs.getDescription());
            xml.closeTag(DEFINITION_TAG);
        }
        
        xml.openTag(ACC_VERSION_TAG);
        xml.print(rs.getAccession()+"."+rs.getVersion());
        xml.closeTag(ACC_VERSION_TAG);
        
        if (!accessions.isEmpty()) {
            xml.openTag(SECONDARY_ACCESSIONS_GROUP_TAG);
            for (Iterator i = accessions.iterator(); i.hasNext(); ) {
                
                xml.openTag(SECONDARY_ACCESSION_TAG);
                xml.print((String)i.next());
                xml.closeTag(SECONDARY_ACCESSION_TAG);
                
            }
            xml.closeTag(SECONDARY_ACCESSIONS_GROUP_TAG);
        }
        
        if (!kws.isEmpty()) {
            xml.openTag(KEYWORDS_GROUP_TAG);
            for (Iterator i = kws.iterator(); i.hasNext(); ) {
                xml.openTag(KEYWORD_TAG);
                xml.print((String)i.next());
                xml.closeTag(KEYWORD_TAG);
            }
            xml.closeTag(KEYWORDS_GROUP_TAG);
        }
        
        NCBITaxon tax = rs.getTaxon();
        if (tax!=null) {
            String[] sciNames = (String[])tax.getNames(NCBITaxon.SCIENTIFIC).toArray(new String[0]);
            if (sciNames.length>0) {
                xml.openTag(SOURCE_TAG);
                xml.print(sciNames[0]);
                xml.closeTag(SOURCE_TAG);
                
                xml.openTag(ORGANISM_TAG);
                xml.print(sciNames[0]);
                xml.closeTag(ORGANISM_TAG);
            }
        }
        
        // references - rank (bases x to y)
        if (!rs.getRankedDocRefs().isEmpty()) {
            xml.openTag(REFERENCES_GROUP_TAG);
            for (Iterator r = rs.getRankedDocRefs().iterator(); r.hasNext();) {
                RankedDocRef rdr = (RankedDocRef)r.next();
                DocRef d = rdr.getDocumentReference();
                Integer rstart = rdr.getStart();
                if (rstart==null) rstart = new Integer(1);
                Integer rend = rdr.getEnd();
                if (rend==null) rend = new Integer(rs.length());
                
                xml.openTag(REFERENCE_LOCATION_TAG);
                xml.print(rdr.getRank()+"  (bases "+rstart+" to "+rend+")");
                xml.closeTag(REFERENCE_LOCATION_TAG);
                
                if (d.getAuthors()!=null) {
                    xml.openTag(AUTHORS_GROUP_TAG);
                    String[] auths = d.getAuthors().split(",");
                    for (int i = 0; i < auths.length; i++) {
                        xml.openTag(AUTHOR_TAG);
                        xml.print(auths[i].trim());
                        xml.closeTag(AUTHOR_TAG);
                    }
                    xml.closeTag(AUTHORS_GROUP_TAG);
                }
                
                xml.openTag(TITLE_TAG);
                xml.print(d.getTitle());
                xml.closeTag(TITLE_TAG);
                
                xml.openTag(JOURNAL_TAG);
                xml.print(d.getLocation());
                xml.closeTag(JOURNAL_TAG);
                
                CrossRef c = d.getCrossref();
                if (c!=null) {
                    if (c.getDbname().equals(Terms.PUBMED_KEY)) {
                        xml.openTag(PUBMED_TAG);
                        xml.print(c.getAccession());
                        xml.closeTag(PUBMED_TAG);
                    } else if (c.getDbname().equals(Terms.MEDLINE_KEY)) {
                        xml.openTag(MEDLINE_TAG);
                        xml.print(c.getAccession());
                        xml.closeTag(MEDLINE_TAG);
                    }
                }
                
                if (d.getRemark()!=null) {
                    xml.openTag(REMARK_TAG);
                    xml.print(d.getRemark());
                    xml.closeTag(REMARK_TAG);
                }
            }
            xml.closeTag(REFERENCES_GROUP_TAG);
        }
        
        if (!rs.getComments().isEmpty()) {
            xml.openTag(COMMENT_TAG);
            for (Iterator i = rs.getComments().iterator(); i.hasNext(); ) xml.println(((Comment)i.next()).getComment());
            xml.closeTag(COMMENT_TAG);
        }
        
        
        // db references - only first one is output
        if (!rs.getRankedCrossRefs().isEmpty()) {
            Iterator r = rs.getRankedCrossRefs().iterator();
            r.next();
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
            
            xml.openTag(DATABASE_XREF_TAG);
            xml.print(sb.toString());
            xml.closeTag(DATABASE_XREF_TAG);
        }
        
        if (!rs.getFeatureSet().isEmpty()) {
            xml.openTag(FEATURES_GROUP_TAG);
            for (Iterator i = rs.getFeatureSet().iterator(); i.hasNext(); ) {
                RichFeature f = (RichFeature)i.next();
                xml.openTag(FEATURE_TAG);
                
                xml.openTag(FEATURE_KEY_TAG);
                xml.print(f.getTypeTerm().getName());
                xml.closeTag(FEATURE_KEY_TAG);
                
                xml.openTag(FEATURE_LOC_TAG);
                xml.print(GenbankLocationParser.writeLocation((RichLocation)f.getLocation()));
                xml.closeTag(FEATURE_LOC_TAG);
                
                xml.openTag(FEATUREQUALS_GROUP_TAG);
                
                for (Iterator j = f.getNoteSet().iterator(); j.hasNext();) {
                    Note n = (Note)j.next();
                    xml.openTag(FEATUREQUAL_TAG);
                    
                    xml.openTag(FEATUREQUAL_NAME_TAG);
                    xml.print(""+n.getTerm().getName());
                    xml.closeTag(FEATUREQUAL_NAME_TAG);
                    
                    xml.openTag(FEATUREQUAL_VALUE_TAG);
                    xml.print(n.getValue());
                    xml.closeTag(FEATUREQUAL_VALUE_TAG);
                    
                    xml.closeTag(FEATUREQUAL_TAG);
                }
                // add-in to source feature only db_xref="taxon:xyz" where present
                if (f.getType().equals("source") && tax!=null) {
                    xml.openTag(FEATUREQUAL_TAG);
                    
                    xml.openTag(FEATUREQUAL_NAME_TAG);
                    xml.print("db_xref");
                    xml.closeTag(FEATUREQUAL_NAME_TAG);
                    
                    xml.openTag(FEATUREQUAL_VALUE_TAG);
                    xml.print("taxon:"+tax.getNCBITaxID());
                    xml.closeTag(FEATUREQUAL_VALUE_TAG);
                    
                    xml.closeTag(FEATUREQUAL_TAG);
                }
                // add-in other dbxrefs where present
                for (Iterator j = f.getRankedCrossRefs().iterator(); j.hasNext();) {
                    RankedCrossRef rcr = (RankedCrossRef)j.next();
                    CrossRef cr = rcr.getCrossRef();
                    xml.openTag(FEATUREQUAL_TAG);
                    
                    xml.openTag(FEATUREQUAL_NAME_TAG);
                    xml.print("db_xref");
                    xml.closeTag(FEATUREQUAL_NAME_TAG);
                    
                    xml.openTag(FEATUREQUAL_VALUE_TAG);
                    xml.print(cr.getDbname()+":"+cr.getAccession());
                    xml.closeTag(FEATUREQUAL_VALUE_TAG);
                    
                    xml.closeTag(FEATUREQUAL_TAG);
                }
                xml.closeTag(FEATUREQUALS_GROUP_TAG);
                
                xml.closeTag(FEATURE_TAG);
            }
            xml.closeTag(FEATURES_GROUP_TAG);
        }
        
        xml.openTag(SEQUENCE_TAG);
        xml.print(rs.seqString());
        xml.closeTag(SEQUENCE_TAG);
        
        xml.closeTag(INSDSEQ_TAG);
        xml.closeTag(INSDSEQS_GROUP_TAG);
        
        pw.flush();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultFormat() {
        return INSDSEQ_FORMAT;
    }
    
    // SAX event handler for parsing http://www.ebi.ac.uk/embl/Documentation/DTD/INSDSeq_v1.3.dtd.txt
    private class INSDseqHandler extends DefaultHandler {
        private INSDseq m_currentSequence;
        private List m_sequences;
        private StringBuffer m_currentString;
        
        // construct a new handler that will populate the given list of sequences
        private INSDseqHandler(List m_sequences) {
            this.m_sequences = m_sequences;
            this.m_currentString = new StringBuffer();
        }
        
        // process an opening tag
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (qName.equals(INSDSEQ_TAG)) {
                this.m_currentSequence = new INSDseq();
                this.m_sequences.add(this.m_currentSequence);
            } else if (qName.equals(REFERENCE_TAG))
                this.m_currentSequence.startReference();
            else if (qName.equals(FEATURE_TAG))
                this.m_currentSequence.startFeature();
            else if (qName.equals(FEATUREQUAL_TAG))
                this.m_currentSequence.getCurrentFeature().startQualifier();
        }
        
        // process a closing tag - we will have read the text already
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals(LOCUS_TAG))
                this.m_currentSequence.setLocus(this.m_currentString.toString().trim());
            else if (qName.equals(STRANDED_TAG))
                this.m_currentSequence.setStrandedness(this.m_currentString.toString().trim());
            else if (qName.equals(MOLTYPE_TAG))
                this.m_currentSequence.setMoltype(this.m_currentString.toString().trim());
            else if (qName.equals(TOPOLOGY_TAG))
                this.m_currentSequence.setTopology(this.m_currentString.toString().trim());
            else if (qName.equals(DIVISION_TAG))
                this.m_currentSequence.setDivision(this.m_currentString.toString().trim());
            else if (qName.equals(UPDATE_DATE_TAG))
                this.m_currentSequence.setUpdateDate(this.m_currentString.toString().trim());
            else if (qName.equals(DEFINITION_TAG))
                this.m_currentSequence.setDefinition(this.m_currentString.toString().trim());
            else if (qName.equals(ACCESSION_TAG))
                this.m_currentSequence.setPrimaryAccession(this.m_currentString.toString().trim());
            else if (qName.equals(ACC_VERSION_TAG))
                this.m_currentSequence.setAccessionVersion(this.m_currentString.toString().trim());
            else if (qName.equals(SECONDARY_ACCESSION_TAG))
                this.m_currentSequence.addSecondaryAccession(this.m_currentString.toString().trim());
            else if (qName.equals(KEYWORD_TAG))
                this.m_currentSequence.addKeyword(this.m_currentString.toString().trim());
            else if (qName.equals(COMMENT_TAG))
                this.m_currentSequence.setComment(this.m_currentString.toString().trim());
            else if (qName.equals(DATABASE_XREF_TAG))
                this.m_currentSequence.setDatabaseXref(this.m_currentString.toString().trim());
            else if (qName.equals(SEQUENCE_TAG))
                this.m_currentSequence.setSequence(this.m_currentString.toString().trim());
            else if (qName.equals(CONTIG_TAG))
                this.m_currentSequence.setContig(this.m_currentString.toString().trim());
            
            else if (qName.equals(REFERENCE_LOCATION_TAG))
                this.m_currentSequence.getCurrentReference().setLocation(this.m_currentString.toString().trim());
            else if (qName.equals(AUTHOR_TAG))
                this.m_currentSequence.getCurrentReference().addAuthor(this.m_currentString.toString().trim());
            else if (qName.equals(TITLE_TAG))
                this.m_currentSequence.getCurrentReference().setTitle(this.m_currentString.toString().trim());
            else if (qName.equals(JOURNAL_TAG))
                this.m_currentSequence.getCurrentReference().setJournal(this.m_currentString.toString().trim());
            else if (qName.equals(MEDLINE_TAG))
                this.m_currentSequence.getCurrentReference().setMedline(this.m_currentString.toString().trim());
            else if (qName.equals(PUBMED_TAG))
                this.m_currentSequence.getCurrentReference().setPubmed(this.m_currentString.toString().trim());
            else if (qName.equals(REMARK_TAG))
                this.m_currentSequence.getCurrentReference().setRemark(this.m_currentString.toString().trim());
            
            else if (qName.equals(FEATURE_KEY_TAG))
                this.m_currentSequence.getCurrentFeature().setKey(this.m_currentString.toString().trim());
            else if (qName.equals(FEATURE_LOC_TAG))
                this.m_currentSequence.getCurrentFeature().setLocation(this.m_currentString.toString().trim());
            else if (qName.equals(FEATUREQUAL_NAME_TAG))
                this.m_currentSequence.getCurrentFeature().getCurrentQualifier().setName(this.m_currentString.toString().trim());
            else if (qName.equals(FEATUREQUAL_VALUE_TAG))
                this.m_currentSequence.getCurrentFeature().getCurrentQualifier().setValue(this.m_currentString.toString().trim());
            
            // drop old string
            this.m_currentString.setLength(0);
        }
        
        // process text inside tags
        public void characters(char[] ch, int start, int length) {
            this.m_currentString.append(ch, start, length);
        }
        
        // return the set of sequences found
        public List getSequences() {
            return this.m_sequences;
        }
    }
    
    // stores a sequence
    private class INSDseq {
        private List refs = new ArrayList();
        private INSDseqRef ref = null;
        private List feats = new ArrayList();
        private INSDseqFeat feat = null;
        public void startReference() {
            this.ref = new INSDseqRef();
            this.refs.add(this.ref);
        }
        public List getReferences() {
            return this.refs;
        }
        public INSDseqRef getCurrentReference() {
            return this.ref;
        }
        public void startFeature() {
            this.feat = new INSDseqFeat();
            this.feats.add(this.feat);
        }
        public List getFeatures() {
            return this.feats;
        }
        public INSDseqFeat getCurrentFeature() {
            return this.feat;
        }
        
        /**
         * Holds value of property locus.
         */
        private String locus;
        
        /**
         * Getter for property locus.
         * @return Value of property locus.
         */
        public String getLocus() {
            
            return this.locus;
        }
        
        /**
         * Setter for property locus.
         * @param locus New value of property locus.
         */
        public void setLocus(String locus) {
            
            this.locus = locus;
        }
        
        /**
         * Holds value of property strandedness.
         */
        private String strandedness;
        
        /**
         * Getter for property strandedness.
         * @return Value of property strandedness.
         */
        public String getStrandedness() {
            
            return this.strandedness;
        }
        
        /**
         * Setter for property strandedness.
         * @param strandedness New value of property strandedness.
         */
        public void setStrandedness(String strandedness) {
            
            this.strandedness = strandedness;
        }
        
        /**
         * Holds value of property moltype.
         */
        private String moltype;
        
        /**
         * Getter for property moltype.
         * @return Value of property moltype.
         */
        public String getMoltype() {
            
            return this.moltype;
        }
        
        /**
         * Setter for property moltype.
         * @param moltype New value of property moltype.
         */
        public void setMoltype(String moltype) {
            
            this.moltype = moltype;
        }
        
        /**
         * Holds value of property topology.
         */
        private String topology;
        
        /**
         * Getter for property topology.
         * @return Value of property topology.
         */
        public String getTopology() {
            
            return this.topology;
        }
        
        /**
         * Setter for property topology.
         * @param topology New value of property topology.
         */
        public void setTopology(String topology) {
            
            this.topology = topology;
        }
        
        /**
         * Holds value of property division.
         */
        private String division;
        
        /**
         * Getter for property division.
         * @return Value of property division.
         */
        public String getDivision() {
            
            return this.division;
        }
        
        /**
         * Setter for property division.
         * @param division New value of property division.
         */
        public void setDivision(String division) {
            
            this.division = division;
        }
        
        /**
         * Holds value of property updateDate.
         */
        private String updateDate;
        
        /**
         * Getter for property updateDate.
         * @return Value of property updateDate.
         */
        public String getUpdateDate() {
            
            return this.updateDate;
        }
        
        /**
         * Setter for property updateDate.
         * @param updateDate New value of property updateDate.
         */
        public void setUpdateDate(String updateDate) {
            
            this.updateDate = updateDate;
        }
        
        /**
         * Holds value of property definition.
         */
        private String definition;
        
        /**
         * Getter for property definition.
         * @return Value of property definition.
         */
        public String getDefinition() {
            
            return this.definition;
        }
        
        /**
         * Setter for property definition.
         * @param definition New value of property definition.
         */
        public void setDefinition(String definition) {
            
            this.definition = definition;
        }
        
        /**
         * Holds value of property primaryAccession.
         */
        private String primaryAccession;
        
        /**
         * Getter for property primaryAccession.
         * @return Value of property primaryAccession.
         */
        public String getPrimaryAccession() {
            
            return this.primaryAccession;
        }
        
        /**
         * Setter for property primaryAccession.
         * @param primaryAccession New value of property primaryAccession.
         */
        public void setPrimaryAccession(String primaryAccession) {
            
            this.primaryAccession = primaryAccession;
        }
        
        /**
         * Holds value of property accessionVersion.
         */
        private String accessionVersion;
        
        /**
         * Getter for property accessionVersion.
         * @return Value of property accessionVersion.
         */
        public String getAccessionVersion() {
            
            return this.accessionVersion;
        }
        
        /**
         * Setter for property accessionVersion.
         * @param accessionVersion New value of property accessionVersion.
         */
        public void setAccessionVersion(String accessionVersion) {
            
            this.accessionVersion = accessionVersion;
        }
        
        /**
         * Holds value of property comment.
         */
        private String comment;
        
        /**
         * Getter for property comment.
         * @return Value of property comment.
         */
        public String getComment() {
            
            return this.comment;
        }
        
        /**
         * Setter for property comment.
         * @param comment New value of property comment.
         */
        public void setComment(String comment) {
            
            this.comment = comment;
        }
        
        /**
         * Holds value of property sequence.
         */
        private String sequence;
        
        /**
         * Getter for property sequence.
         * @return Value of property sequence.
         */
        public String getSequence() {
            
            return this.sequence;
        }
        
        /**
         * Setter for property sequence.
         * @param sequence New value of property sequence.
         */
        public void setSequence(String sequence) {
            
            this.sequence = sequence;
        }
        
        /**
         * Holds value of property contig.
         */
        private String contig;
        
        /**
         * Getter for property contig.
         * @return Value of property contig.
         */
        public String getContig() {
            
            return this.contig;
        }
        
        /**
         * Setter for property contig.
         * @param contig New value of property contig.
         */
        public void setContig(String contig) {
            
            this.contig = contig;
        }
        
        private List secAccs = new ArrayList();
        public void addSecondaryAccession(String secAcc) {
            this.secAccs.add(secAcc);
        }
        public List getSecondaryAccessions() {
            return this.secAccs;
        }
        
        private List kws = new ArrayList();
        public void addKeyword(String kw) {
            this.kws.add(kw);
        }
        public List getKeywords() {
            return this.kws;
        }
        
        /**
         * Holds value of property databaseXref.
         */
        private String databaseXref;
        
        /**
         * Getter for property databaseXref.
         * @return Value of property databaseXref.
         */
        public String getDatabaseXref() {
            
            return this.databaseXref;
        }
        
        /**
         * Setter for property databaseXref.
         * @param databaseXref New value of property databaseXref.
         */
        public void setDatabaseXref(String databaseXref) {
            
            this.databaseXref = databaseXref;
        }
    }
    
    // stores a feature
    private class INSDseqFeat {
        private List quals = new ArrayList();
        private INSDseqFeatQual qual = null;
        public void startQualifier() {
            this.qual = new INSDseqFeatQual();
            this.quals.add(this.qual);
        }
        public List getQualifiers() {
            return this.quals;
        }
        public INSDseqFeatQual getCurrentQualifier() {
            return this.qual;
        }
        
        /**
         * Holds value of property key.
         */
        private String key;
        
        /**
         * Getter for property key.
         * @return Value of property key.
         */
        public String getKey() {
            
            return this.key;
        }
        
        /**
         * Setter for property key.
         * @param key New value of property key.
         */
        public void setKey(String key) {
            
            this.key = key;
        }
        
        /**
         * Holds value of property location.
         */
        private String location;
        
        /**
         * Getter for property location.
         * @return Value of property location.
         */
        public String getLocation() {
            
            return this.location;
        }
        
        /**
         * Setter for property location.
         * @param location New value of property location.
         */
        public void setLocation(String location) {
            
            this.location = location;
        }
    }
    
    // stores a qualifier
    private class INSDseqFeatQual {
        /**
         * Holds value of property name.
         */
        private String name;
        
        /**
         * Getter for property name.
         * @return Value of property name.
         */
        public String getName() {
            
            return this.name;
        }
        
        /**
         * Setter for property name.
         * @param name New value of property name.
         */
        public void setName(String name) {
            
            this.name = name;
        }
        
        /**
         * Holds value of property value.
         */
        private String value;
        
        /**
         * Getter for property value.
         * @return Value of property value.
         */
        public String getValue() {
            
            return this.value;
        }
        
        /**
         * Setter for property value.
         * @param value New value of property value.
         */
        public void setValue(String value) {
            
            this.value = value;
        }
    }
    
    // stores a reference
    private class INSDseqRef {
        private List auths = new ArrayList();
        public void addAuthor(String auth) {
            this.auths.add(auth);
        }
        public List getAuthors() {
            return this.auths;
        }
        
        /**
         * Holds value of property location.
         */
        private String location;
        
        /**
         * Getter for property location.
         * @return Value of property location.
         */
        public String getLocation() {
            
            return this.location;
        }
        
        /**
         * Setter for property location.
         * @param location New value of property location.
         */
        public void setLocation(String location) {
            
            this.location = location;
        }
        
        /**
         * Holds value of property title.
         */
        private String title;
        
        /**
         * Getter for property title.
         * @return Value of property title.
         */
        public String getTitle() {
            
            return this.title;
        }
        
        /**
         * Setter for property title.
         * @param title New value of property title.
         */
        public void setTitle(String title) {
            
            this.title = title;
        }
        
        /**
         * Holds value of property journal.
         */
        private String journal;
        
        /**
         * Getter for property journal.
         * @return Value of property journal.
         */
        public String getJournal() {
            
            return this.journal;
        }
        
        /**
         * Setter for property journal.
         * @param journal New value of property journal.
         */
        public void setJournal(String journal) {
            
            this.journal = journal;
        }
        
        /**
         * Holds value of property medline.
         */
        private String medline;
        
        /**
         * Getter for property medline.
         * @return Value of property medline.
         */
        public String getMedline() {
            
            return this.medline;
        }
        
        /**
         * Setter for property medline.
         * @param medline New value of property medline.
         */
        public void setMedline(String medline) {
            
            this.medline = medline;
        }
        
        /**
         * Holds value of property pubmed.
         */
        private String pubmed;
        
        /**
         * Getter for property pubmed.
         * @return Value of property pubmed.
         */
        public String getPubmed() {
            
            return this.pubmed;
        }
        
        /**
         * Setter for property pubmed.
         * @param pubmed New value of property pubmed.
         */
        public void setPubmed(String pubmed) {
            
            this.pubmed = pubmed;
        }
        
        /**
         * Holds value of property remark.
         */
        private String remark;
        
        /**
         * Getter for property remark.
         * @return Value of property remark.
         */
        public String getRemark() {
            
            return this.remark;
        }
        
        /**
         * Setter for property remark.
         * @param remark New value of property remark.
         */
        public void setRemark(String remark) {
            
            this.remark = remark;
        }
    }
}

