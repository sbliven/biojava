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
import org.biojavax.DocRefAuthor;
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
import org.biojavax.SimpleDocRefAuthor;
import org.biojavax.SimpleNote;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.taxa.NCBITaxon;
import org.biojavax.bio.taxa.SimpleNCBITaxon;
import org.biojavax.ontology.ComparableTerm;
import org.biojavax.utils.StringTools;
import org.biojavax.utils.XMLTools;
import org.xml.sax.Attributes;
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
public class INSDseqFormat extends RichSequenceFormat.BasicFormat {
    
    /**
     * The name of this format
     */
    public static final String INSDSEQ_FORMAT = "INSDseq";
    
    protected static final String INSDSEQS_GROUP_TAG = "INSDSet";
    protected static final String INSDSEQ_TAG = "INSDSeq";
    
    protected static final String LOCUS_TAG = "INSDSeq_locus";
    protected static final String LENGTH_TAG = "INSDSeq_length";
    protected static final String TOPOLOGY_TAG = "INSDSeq_topology";
    protected static final String STRANDED_TAG = "INSDSeq_strandedness";
    protected static final String MOLTYPE_TAG = "INSDSeq_moltype";
    protected static final String DIVISION_TAG = "INSDSeq_division";
    protected static final String UPDATE_DATE_TAG = "INSDSeq_update-date";
    protected static final String CREATE_DATE_TAG = "INSDSeq_create-date";
    protected static final String UPDATE_REL_TAG = "INSDSeq_update-release";
    protected static final String CREATE_REL_TAG = "INSDSeq_create-release";
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
    protected static final String CONSORTIUM_TAG = "INSDReference_consortium";
    
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
    
    // reference line
    protected static final Pattern refp = Pattern.compile("^(\\d+)\\s*(\\(bases\\s+(\\d+)\\s+to\\s+(\\d+)\\)|\\(sites\\))?");
    // dbxref line
    protected static final Pattern dbxp = Pattern.compile("^(\\S+?):(\\S+)$");
    
    /**
     * Implements some INSDseq-specific terms.
     */
    public static class Terms extends RichSequence.Terms {
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
        
        try {
            DefaultHandler m_handler = new INSDseqHandler(this,symParser,rlistener,ns);
            return XMLTools.readXMLChunk(reader, m_handler, INSDSEQ_TAG);
        } catch (ParserConfigurationException e) {
            throw new ParseException(e);
        } catch (SAXException e) {
            throw new ParseException(e);
        }
    }
    
    private PrintWriter pw;
    private XMLWriter xml;
    
    /**
     * {@inheritDoc}
     */
    public void beginWriting() throws IOException {
        // make an XML writer
        pw = new PrintWriter(this.getPrintStream());
        xml = new PrettyXMLWriter(pw);
        xml.printRaw("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        xml.printRaw("<!DOCTYPE INSDSeq PUBLIC \"-//EMBL-EBI//INSD INSDSeq/EN\" \"http://www.ebi.ac.uk/dtd/INSD_INSDSeq.dtd\">");
        xml.openTag(INSDSEQS_GROUP_TAG);
    }
    
    /**
     * {@inheritDoc}
     */
    public void finishWriting() throws IOException {
        xml.closeTag(INSDSEQS_GROUP_TAG);
        pw.flush();
    }
    
    /**
     * {@inheritDoc}
     */
    public void	writeSequence(Sequence seq, PrintStream os) throws IOException {
        if (this.getPrintStream()==null) this.setPrintStream(this.getPrintStream());
        this.writeSequence(seq, RichObjectFactory.getDefaultNamespace());
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeSequence(Sequence seq, String format, PrintStream os) throws IOException {
        if (this.getPrintStream()==null) this.setPrintStream(this.getPrintStream());
        if (!format.equals(this.getDefaultFormat())) throw new IllegalArgumentException("Unknown format: "+format);
        this.writeSequence(seq, RichObjectFactory.getDefaultNamespace());
    }
    
    /**
     * {@inheritDoc}
     * Namespace is ignored as INSDseq has no concept of it.
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
        List accessions = new ArrayList();
        List kws = new ArrayList();
        String stranded = null;
        String udat = null;
        String cdat = null;
        String urel = null;
        String crel = null;
        String moltype = rs.getAlphabet().getName();
        for (Iterator i = notes.iterator(); i.hasNext();) {
            Note n = (Note)i.next();
            if (n.getTerm().equals(Terms.getStrandedTerm())) stranded=n.getValue();
            else if (n.getTerm().equals(Terms.getDateUpdatedTerm())) udat=n.getValue();
            else if (n.getTerm().equals(Terms.getDateCreatedTerm())) cdat=n.getValue();
            else if (n.getTerm().equals(Terms.getRelUpdatedTerm())) urel=n.getValue();
            else if (n.getTerm().equals(Terms.getRelCreatedTerm())) crel=n.getValue();
            else if (n.getTerm().equals(Terms.getMolTypeTerm())) moltype=n.getValue();
            else if (n.getTerm().equals(Terms.getAdditionalAccessionTerm())) accessions.add(n.getValue());
            else if (n.getTerm().equals(Terms.getKeywordTerm())) kws.add(n.getValue());
        }
        
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
        
        xml.openTag(UPDATE_DATE_TAG);
        xml.print(udat);
        xml.closeTag(UPDATE_DATE_TAG);
        
        xml.openTag(CREATE_DATE_TAG);
        xml.print(cdat==null?udat:cdat);
        xml.closeTag(CREATE_DATE_TAG);
        
        if (urel!=null) {
            xml.openTag(UPDATE_REL_TAG);
            xml.print(urel);
            xml.closeTag(UPDATE_REL_TAG);
        }
        
        if (crel!=null) {
            xml.openTag(CREATE_REL_TAG);
            xml.print(crel);
            xml.closeTag(CREATE_REL_TAG);
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
            xml.openTag(SOURCE_TAG);
            xml.print(tax.getDisplayName());
            xml.closeTag(SOURCE_TAG);
            
            xml.openTag(ORGANISM_TAG);
            xml.print(tax.getDisplayName().split("\\(")[0].trim());
            xml.closeTag(ORGANISM_TAG);
            
            xml.openTag(TAXONOMY_TAG);
            String h = tax.getNameHierarchy();
            xml.print(h.substring(0, h.length()-1)); // chomp dot
            xml.closeTag(TAXONOMY_TAG);
        }
        
        // references - rank (bases x to y)
        if (!rs.getRankedDocRefs().isEmpty()) {
            xml.openTag(REFERENCES_GROUP_TAG);
            for (Iterator r = rs.getRankedDocRefs().iterator(); r.hasNext();) {
                xml.openTag(REFERENCE_TAG);
                
                RankedDocRef rdr = (RankedDocRef)r.next();
                DocRef d = rdr.getDocumentReference();
                Integer rstart = rdr.getStart();
                if (rstart==null) rstart = new Integer(1);
                Integer rend = rdr.getEnd();
                if (rend==null) rend = new Integer(rs.length());
                
                xml.openTag(REFERENCE_LOCATION_TAG);
                xml.print(rdr.getRank()+"  (bases "+rstart+" to "+rend+")");
                xml.closeTag(REFERENCE_LOCATION_TAG);
                
                xml.openTag(AUTHORS_GROUP_TAG);
                Set auths = d.getAuthorSet();
                for (Iterator i = auths.iterator(); i.hasNext(); ) {
                    DocRefAuthor a = (DocRefAuthor)i.next();
                    if (!a.isConsortium()) {
                        xml.openTag(AUTHOR_TAG);
                        xml.print(a.getName());
                        xml.closeTag(AUTHOR_TAG);
                        i.remove();
                    }
                }
                xml.closeTag(AUTHORS_GROUP_TAG);
                if (!auths.isEmpty()) { // only consortia left in the set now
                    DocRefAuthor a = (DocRefAuthor)auths.iterator().next(); // take the first one only
                    xml.openTag(CONSORTIUM_TAG);
                    xml.print(a.getName());
                    xml.closeTag(CONSORTIUM_TAG);
                }
                
                if (d.getTitle()!=null) {
                    xml.openTag(TITLE_TAG);
                    xml.print(d.getTitle());
                    xml.closeTag(TITLE_TAG);
                }
                
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
                
                xml.closeTag(REFERENCE_TAG);
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
                    if (n.getTerm().getName().equals("translation")) {
                        String[] lines = StringTools.wordWrap(n.getValue(), "\\s+", this.getLineWidth());
                        for (int k = 0; k < lines.length; k++) xml.println(lines[k]);
                    } else {
                        xml.print(n.getValue());
                    }
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
        String[] lines = StringTools.wordWrap(rs.seqString(), "\\s+", this.getLineWidth());
        for (int i = 0; i < lines.length; i ++) xml.println(lines[i]);
        xml.closeTag(SEQUENCE_TAG);
        
        xml.closeTag(INSDSEQ_TAG);
        
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
        
        private RichSequenceFormat parent;
        private SymbolTokenization symParser;
        private RichSeqIOListener rlistener;
        private Namespace ns;
        private StringBuffer m_currentString;
        
        private NCBITaxon tax;
        private String organism;
        private String accession;
        private RichFeature.Template templ;
        private String currFeatQual;
        private String currRefLocation;
        private List currRefAuthors;
        private String currRefTitle;
        private String currRefJournal;
        private String currRefMedline;
        private String currRefPubmed;
        private String currRefRemark;
        
        // construct a new handler that will populate the given list of sequences
        private INSDseqHandler(RichSequenceFormat parent,
                SymbolTokenization symParser,
                RichSeqIOListener rlistener,
                Namespace ns) {
            this.parent = parent;
            this.symParser = symParser;
            this.rlistener = rlistener;
            this.ns = ns;
            this.m_currentString = new StringBuffer();
        }
        
        // process an opening tag
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals(INSDSEQ_TAG)) {
                try {
                    rlistener.startSequence();
                    if (ns==null) ns=RichObjectFactory.getDefaultNamespace();
                    rlistener.setNamespace(ns);
                } catch (ParseException e) {
                    throw new SAXException(e);
                }
            } else if (qName.equals(REFERENCE_TAG) && !this.parent.getElideReferences()) {
                currRefLocation = null;
                currRefAuthors = new ArrayList();
                currRefTitle = null;
                currRefJournal = null;
                currRefMedline = null;
                currRefPubmed = null;
                currRefRemark = null;
            } else if (qName.equals(FEATURE_TAG) && !this.parent.getElideFeatures()) {
                templ = new RichFeature.Template();
                templ.annotation = new SimpleRichAnnotation();
                templ.sourceTerm = Terms.getINSDseqTerm();
                templ.featureRelationshipSet = new TreeSet();
                templ.rankedCrossRefs = new TreeSet();
            }
        }
        
        // process a closing tag - we will have read the text already
        public void endElement(String uri, String localName, String qName) throws SAXException {
            String val = this.m_currentString.toString().trim();
            
            try {
                if (qName.equals(LOCUS_TAG))
                    rlistener.setName(val);
                else if (qName.equals(ACCESSION_TAG)) {
                    accession = val;
                    rlistener.setAccession(accession);
                } else if (qName.equals(ACC_VERSION_TAG)) {
                    String parts[] = val.split("\\.");
                    accession = parts[0];
                    rlistener.setAccession(accession);
                    if (parts.length>1) rlistener.setVersion(Integer.parseInt(parts[1]));
                } else if (qName.equals(SECONDARY_ACCESSION_TAG)) {
                    rlistener.addSequenceProperty(Terms.getAdditionalAccessionTerm(),val);
                } else if (qName.equals(DIVISION_TAG)) {
                    rlistener.setDivision(val);
                } else if (qName.equals(MOLTYPE_TAG)) {
                    rlistener.addSequenceProperty(Terms.getMolTypeTerm(),val);
                } else if (qName.equals(UPDATE_DATE_TAG)) {
                    rlistener.addSequenceProperty(Terms.getDateUpdatedTerm(),val);
                } else if (qName.equals(UPDATE_REL_TAG)) {
                    rlistener.addSequenceProperty(Terms.getRelUpdatedTerm(),val);
                } else if (qName.equals(CREATE_DATE_TAG)) {
                    rlistener.addSequenceProperty(Terms.getDateCreatedTerm(),val);
                } else if (qName.equals(CREATE_REL_TAG)) {
                    rlistener.addSequenceProperty(Terms.getRelCreatedTerm(),val);
                } else if (qName.equals(STRANDED_TAG)) {
                    rlistener.addSequenceProperty(Terms.getStrandedTerm(),val);
                } else if (qName.equals(TOPOLOGY_TAG)) {
                    if ("circular".equals(val)) rlistener.setCircular(true);
                } else if (qName.equals(DEFINITION_TAG)) {
                    rlistener.setDescription(val);
                } else if (qName.equals(KEYWORD_TAG)) {
                    rlistener.addSequenceProperty(Terms.getKeywordTerm(), val);
                } else if (qName.equals(COMMENT_TAG) && !this.parent.getElideComments()) {
                    rlistener.setComment(val);
                } else if (qName.equals(DATABASE_XREF_TAG)) {
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
                    RankedCrossRef rcrossRef = new SimpleRankedCrossRef(crossRef, 1);
                    rlistener.setRankedCrossRef(rcrossRef);
                } else if (qName.equals(SEQUENCE_TAG) && !this.parent.getElideSymbols()) {
                    try {
                        SymbolList sl = new SimpleSymbolList(symParser,
                                val.replaceAll("\\s+","").replaceAll("[\\.|~]","-"));
                        rlistener.addSymbols(symParser.getAlphabet(),
                                (Symbol[])(sl.toList().toArray(new Symbol[0])),
                                0, sl.length());
                    } catch (Exception e) {
                        throw new ParseException(e);
                    }
                } else if (qName.equals(CONTIG_TAG))
                    throw new SAXException("Cannot handle contigs yet");
                
                
                else if (qName.equals(REFERENCE_LOCATION_TAG) && !this.parent.getElideReferences()) {
                    currRefLocation = val;
                } else if (qName.equals(AUTHOR_TAG) && !this.parent.getElideReferences()) {
                    currRefAuthors.add(new SimpleDocRefAuthor(val,false,false));
                } else if (qName.equals(CONSORTIUM_TAG) && !this.parent.getElideReferences()) {
                    currRefAuthors.add(new SimpleDocRefAuthor(val,true,false));
                } else if (qName.equals(TITLE_TAG) && !this.parent.getElideReferences()) {
                    currRefTitle = val;
                } else if (qName.equals(JOURNAL_TAG) && !this.parent.getElideReferences()) {
                    currRefJournal = val;
                } else if (qName.equals(MEDLINE_TAG) && !this.parent.getElideReferences()) {
                    currRefMedline = val;
                } else if (qName.equals(PUBMED_TAG) && !this.parent.getElideReferences()) {
                    currRefPubmed = val;
                } else if (qName.equals(REMARK_TAG) && !this.parent.getElideReferences() && !this.parent.getElideComments()) {
                    currRefRemark = val;
                } else if (qName.equals(REFERENCE_TAG) && !this.parent.getElideReferences()) {
                    int ref_rank;
                    int ref_start = -999;
                    int ref_end = -999;
                    Matcher m = refp.matcher(currRefLocation);
                    if (m.matches()) {
                        ref_rank = Integer.parseInt(m.group(1));
                        if(m.group(2) != null){
                            if (m.group(3)!= null)
                                ref_start = Integer.parseInt(m.group(3));
                            
                            if(m.group(4) != null)
                                ref_end = Integer.parseInt(m.group(4));
                        }
                    } else {
                        throw new ParseException("Bad reference line found: "+currRefLocation);
                    }
                    // create the pubmed crossref and assign to the bioentry
                    CrossRef pcr = null;
                    if (currRefPubmed!=null) {
                        pcr = (CrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{Terms.PUBMED_KEY, currRefPubmed, new Integer(0)});
                        RankedCrossRef rpcr = new SimpleRankedCrossRef(pcr, 0);
                        rlistener.setRankedCrossRef(rpcr);
                    }
                    // create the medline crossref and assign to the bioentry
                    CrossRef mcr = null;
                    if (currRefMedline!=null) {
                        mcr = (CrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{Terms.MEDLINE_KEY, currRefMedline, new Integer(0)});
                        RankedCrossRef rmcr = new SimpleRankedCrossRef(mcr, 0);
                        rlistener.setRankedCrossRef(rmcr);
                    }
                    // create the docref object
                    try {
                        DocRef dr = (DocRef)RichObjectFactory.getObject(SimpleDocRef.class,new Object[]{currRefAuthors,currRefJournal, new Integer(0)});
                        if (currRefTitle!=null) dr.setTitle(currRefTitle);
                        // assign either the pubmed or medline to the docref - medline gets priority
                        if (mcr!=null) dr.setCrossref(mcr);
                        else if (pcr!=null) dr.setCrossref(pcr);
                        // assign the remarks
                        dr.setRemark(currRefRemark);
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
                
                
                else if (qName.equals(FEATURE_KEY_TAG) && !this.parent.getElideFeatures()) {
                    templ.typeTerm = RichObjectFactory.getDefaultOntology().getOrCreateTerm(val);
                } else if (qName.equals(FEATURE_LOC_TAG) && !this.parent.getElideFeatures()) {
                    String tidyLocStr = val.replaceAll("\\s+","");
                    templ.location = GenbankLocationParser.parseLocation(ns, accession, tidyLocStr);
                    rlistener.startFeature(templ);
                } else if (qName.equals(FEATUREQUAL_NAME_TAG) && !this.parent.getElideFeatures()) {
                    if (currFeatQual!=null) {
                        rlistener.addFeatureProperty(RichObjectFactory.getDefaultOntology().getOrCreateTerm(currFeatQual),null);
                    }
                    currFeatQual = val;
                } else if (qName.equals(FEATUREQUAL_VALUE_TAG) && !this.parent.getElideFeatures()) {
                    if (currFeatQual.equals("db_xref")) {
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
                    } else if (currFeatQual.equals("organism")) {
                        try {
                            organism = val;
                            if (tax!=null) tax.addName(NCBITaxon.SCIENTIFIC,organism);
                        } catch (ChangeVetoException e) {
                            throw new ParseException(e);
                        }
                    } else {
                        if (currFeatQual.equals("translation")) {
                            // strip spaces from sequence
                            val = val.replaceAll("\\s+","");
                        }
                        rlistener.addFeatureProperty(RichObjectFactory.getDefaultOntology().getOrCreateTerm(currFeatQual),val);
                    }
                    currFeatQual = null;
                } else if (qName.equals(FEATURE_TAG) && !this.parent.getElideFeatures()) {
                    rlistener.endFeature();
                }
                
                
                else if (qName.equals(INSDSEQ_TAG))
                    rlistener.endSequence();
            } catch (ParseException e) {
                throw new SAXException(e);
            }
            
            // drop old string
            this.m_currentString.setLength(0);
        }
        
        // process text inside tags
        public void characters(char[] ch, int start, int length) {
            this.m_currentString.append(ch, start, length);
        }
    }
}

