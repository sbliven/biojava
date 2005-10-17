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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import org.biojava.bio.proteomics.MassCalc;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.ParseException;
import org.biojava.bio.seq.io.SeqIOListener;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolPropertyTable;
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
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.Position;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.io.UniProtCommentParser.Event;
import org.biojavax.bio.seq.io.UniProtCommentParser.Interaction;
import org.biojavax.bio.seq.io.UniProtCommentParser.Isoform;
import org.biojavax.bio.taxa.NCBITaxon;
import org.biojavax.ontology.ComparableOntology;
import org.biojavax.ontology.ComparableTerm;
import org.biojavax.ontology.SimpleComparableOntology;
import org.biojavax.utils.CRC64Checksum;
import org.biojavax.utils.StringTools;
import org.biojavax.utils.XMLTools;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Format reader for UniProtXML files. This version of UniProtXML format will generate
 * and write RichSequence objects. Loosely Based on code from the old, deprecated,
 * org.biojava.bio.seq.io.GenbankXmlFormat object.
 *
 * Understands http://www.ebi.uniprot.org/support/docs/uniprot.xsd
 *
 * @author Alan Li (code based on his work)
 * @author Richard Holland
 */
public class UniProtXMLFormat extends RichSequenceFormat.BasicFormat {
    
    /**
     * The name of this format
     */
    public static final String UNIPROTXML_FORMAT = "UniProtXML";
    
    protected static final String ENTRY_GROUP_TAG = "uniprot";
    protected static final String ENTRY_TAG = "entry";
    protected static final String ENTRY_NAMESPACE_ATTR = "dataset";
    protected static final String ENTRY_CREATED_ATTR = "created";
    protected static final String ENTRY_UPDATED_ATTR = "modified";
    protected static final String COPYRIGHT_TAG = "copyright";
    
    protected static final String ACCESSION_TAG = "accession";
    protected static final String NAME_TAG = "name";
    protected static final String TEXT_TAG = "text";
    
    protected static final String REF_ATTR = "ref";
    protected static final String TYPE_ATTR = "type";
    protected static final String KEY_ATTR = "key";
    protected static final String ID_ATTR = "id";
    protected static final String EVIDENCE_ATTR = "evidence";
    protected static final String VALUE_ATTR = "value";
    
    protected static final String PROTEIN_TAG = "protein";
    protected static final String PROTEIN_TYPE_ATTR = "type";
    
    protected static final String DOMAIN_TAG = "domain";
    protected static final String COMPONENT_TAG = "component";
    protected static final String GENE_TAG = "gene";
    protected static final String ORGANISM_TAG = "organism";
    protected static final String DBXREF_TAG = "dbReference";
    protected static final String PROPERTY_TAG = "property";
    protected static final String LINEAGE_TAG = "lineage";
    protected static final String TAXON_TAG = "taxon";
    protected static final String GENELOCATION_TAG = "geneLocation";
    
    protected static final String REFERENCE_TAG = "reference";
    protected static final String CITATION_TAG = "citation";
    protected static final String TITLE_TAG = "title";
    protected static final String EDITOR_LIST_TAG = "editorList";
    protected static final String AUTHOR_LIST_TAG = "authorList";
    protected static final String PERSON_TAG = "person";
    protected static final String CONSORTIUM_TAG = "consortium";
    protected static final String LOCATOR_TAG = "locator";
    protected static final String RP_LINE_TAG = "scope";
    protected static final String RC_LINE_TAG = "source";
    protected static final String RC_SPECIES_TAG = "species";
    protected static final String RC_TISSUE_TAG = "tissue";
    protected static final String RC_TRANSP_TAG = "transposon";
    protected static final String RC_STRAIN_TAG = "strain";
    protected static final String RC_PLASMID_TAG = "plasmid";
    
    protected static final String COMMENT_TAG = "comment";
    protected static final String COMMENT_NAME_ATTR = "name";
    protected static final String COMMENT_MASS_ATTR = "mass";
    protected static final String COMMENT_ERROR_ATTR = "error";
    protected static final String COMMENT_METHOD_ATTR = "method";
    protected static final String COMMENT_STATUS_ATTR = "status";
    protected static final String COMMENT_LOCTYPE_ATTR = "locationType";
    
    protected static final String COMMENT_ABSORPTION_TAG = "absorption";
    protected static final String COMMENT_ABS_MAX_TAG = "max";
    protected static final String COMMENT_KINETICS_TAG = "kinetics";
    protected static final String COMMENT_KIN_KM_TAG = "KM";
    protected static final String COMMENT_KIN_VMAX_TAG = "VMax";
    protected static final String COMMENT_PH_TAG = "phDependence";
    protected static final String COMMENT_REDOX_TAG = "redoxPotential";
    protected static final String COMMENT_TEMPERATURE_TAG = "temperatureDependence";
    protected static final String COMMENT_LINK_TAG = "link";
    protected static final String COMMENT_LINK_URI_ATTR = "uri";
    protected static final String COMMENT_EVENT_TAG = "event";
    protected static final String COMMENT_EVENT_ISOFORMS_ATTR = "namedIsoforms";
    protected static final String COMMENT_ISOFORM_TAG = "isoform";
    protected static final String COMMENT_ISOFORM_ID_TAG = "id";
    protected static final String COMMENT_ISOFORM_NAME_TAG = "name";
    protected static final String COMMENT_INTERACTANT_TAG = "interactant";
    protected static final String COMMENT_INTERACT_INTACT_ATTR = "intactId";
    protected static final String COMMENT_INTERACT_ID_TAG = "id";
    protected static final String COMMENT_INTERACT_LABEL_TAG = "label";
    protected static final String COMMENT_ORGANISMS_TAG = "organismsDiffer";
    protected static final String COMMENT_EXPERIMENTS_TAG = "experiments";
    protected static final String COMMENT_TEXT_TAG = "text";
    
    protected static final String NOTE_TAG = "note";
    protected static final String KEYWORD_TAG = "keyword";
    
    protected static final String FEATURE_TAG = "feature";
    protected static final String FEATURE_STATUS_ATTR = "status";
    protected static final String FEATURE_DESC_ATTR = "description";
    protected static final String FEATURE_ORIGINAL_TAG = "original";
    protected static final String FEATURE_VARIATION_TAG = "variation";
    
    protected static final String EVIDENCE_TAG = "evidence";
    protected static final String EVIDENCE_CATEGORY_ATTR = "category";
    protected static final String EVIDENCE_ATTRIBUTE_ATTR = "attribute";
    protected static final String EVIDENCE_DATE_ATTR = "date";
    
    protected static final String LOCATION_TAG = "location";
    protected static final String LOCATION_SEQ_ATTR = "sequence";
    protected static final String LOCATION_BEGIN_TAG = "begin";
    protected static final String LOCATION_END_TAG = "end";
    protected static final String LOCATION_POSITION_ATTR = "position";
    protected static final String LOCATION_STATUS_ATTR = "status";
    
    protected static final String SEQUENCE_TAG = "sequence";
    protected static final String SEQUENCE_LENGTH_ATTR = "length";
    protected static final String SEQUENCE_MASS_ATTR = "mass";
    protected static final String SEQUENCE_CHECKSUM_ATTR = "checksum";
    protected static final String SEQUENCE_MODIFIED_ATTR = "modified";
    
    // RP line parser
    protected static final Pattern rppat = Pattern.compile("SEQUENCE OF (\\d+)-(\\d+)");
    // Ontology for uniprot keywords (because they have identifiers, aaargh...)
    protected ComparableOntology uniprotKWOnto = (ComparableOntology)RichObjectFactory.getObject(SimpleComparableOntology.class, new Object[]{"uniprot_kw"});
    
    /**
     * Implements some UniProtXML-specific terms.
     */
    public static class Terms extends RichSequence.Terms {
        private static ComparableTerm UNIPROTXML_TERM = null;
        private static ComparableTerm PROTEINTYPE_TERM = null;
        private static ComparableTerm EVIDENCE_CATEGORY_TERM = null;
        private static ComparableTerm EVIDENCE_TYPE_TERM = null;
        private static ComparableTerm EVIDENCE_DATE_TERM = null;
        private static ComparableTerm EVIDENCE_ATTR_TERM = null;
        private static ComparableTerm FEATURE_STATUS_TERM = null;
        private static ComparableTerm FEATURE_REF_TERM = null;
        private static ComparableTerm FEATURE_ORIGINAL_TERM = null;
        private static ComparableTerm FEATURE_VARIATION_TERM = null;
        private static ComparableTerm LOCATION_SEQUENCE_TERM = null;
        
        public static final String CONTAINS_PREFIX = "Contains:";
        public static final String INCLUDES_PREFIX = "Includes:";
        
        public static final String GENENAME_KEY = "primary";
        public static final String GENESYNONYM_KEY = "synonym";
        public static final String ORDLOCNAME_KEY = "ordered locus";
        public static final String ORFNAME_KEY = "ORF";
        
        public static final String NCBI_TAXON_KEY = "NCBI Taxonomy";
        public static final String COMMON_NAME_KEY = "common";
        public static final String FULL_NAME_KEY = "full";
        public static final String SCIENTIFIC_NAME_KEY = "scientific";
        public static final String SYNONYM_NAME_KEY = "synonym";
        public static final String ABBREV_NAME_KEY = "abbreviation";
        
        public static final String LOC_FUZZY_START_KEY = "less than";
        public static final String LOC_FUZZY_END_KEY = "greater than";
        
        /**
         * Getter for the UniProtXML term
         * @return The UniProtXML Term
         */
        public static ComparableTerm getUniProtXMLTerm() {
            if (UNIPROTXML_TERM==null) UNIPROTXML_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("UniProtXML");
            return UNIPROTXML_TERM;
        }
        
        /**
         * Getter for the protein type term
         * @return The protein type Term
         */
        public static ComparableTerm getProteinTypeTerm() {
            if (PROTEINTYPE_TERM==null) PROTEINTYPE_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("protein_type");
            return PROTEINTYPE_TERM;
        }
        
        /**
         * Getter for the evidence category term
         * @return The evidence category Term
         */
        public static ComparableTerm getEvidenceCategoryTerm() {
            if (EVIDENCE_CATEGORY_TERM==null) EVIDENCE_CATEGORY_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("evidence_category");
            return EVIDENCE_CATEGORY_TERM;
        }
        
        /**
         * Getter for the evidence type term
         * @return The evidence type Term
         */
        public static ComparableTerm getEvidenceTypeTerm() {
            if (EVIDENCE_TYPE_TERM==null) EVIDENCE_TYPE_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("evidence_type");
            return EVIDENCE_TYPE_TERM;
        }
        
        /**
         * Getter for the evidence date term
         * @return The evidence date Term
         */
        public static ComparableTerm getEvidenceDateTerm() {
            if (EVIDENCE_DATE_TERM==null) EVIDENCE_DATE_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("evidence_date");
            return EVIDENCE_DATE_TERM;
        }
        
        /**
         * Getter for the evidence attr term
         * @return The evidence attr Term
         */
        public static ComparableTerm getEvidenceAttrTerm() {
            if (EVIDENCE_ATTR_TERM==null) EVIDENCE_ATTR_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("evidence_attr");
            return EVIDENCE_ATTR_TERM;
        }
        
        /**
         * Getter for the feature ref term
         * @return The feature ref Term
         */
        public static ComparableTerm getFeatureRefTerm() {
            if (FEATURE_REF_TERM==null) FEATURE_REF_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("feature_ref");
            return FEATURE_REF_TERM;
        }
        
        /**
         * Getter for the feature status term
         * @return The feature status Term
         */
        public static ComparableTerm getFeatureStatusTerm() {
            if (FEATURE_STATUS_TERM==null) FEATURE_STATUS_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("feature_status");
            return FEATURE_STATUS_TERM;
        }
        
        /**
         * Getter for the feature original term
         * @return The feature original Term
         */
        public static ComparableTerm getFeatureOriginalTerm() {
            if (FEATURE_ORIGINAL_TERM==null) FEATURE_ORIGINAL_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("feature_original");
            return FEATURE_ORIGINAL_TERM;
        }
        
        /**
         * Getter for the feature variation term
         * @return The feature variation Term
         */
        public static ComparableTerm getFeatureVariationTerm() {
            if (FEATURE_VARIATION_TERM==null) FEATURE_VARIATION_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("feature_variation");
            return FEATURE_VARIATION_TERM;
        }
        
        /**
         * Getter for the location seq term
         * @return The location seq Term
         */
        public static ComparableTerm getLocationSequenceTerm() {
            if (LOCATION_SEQUENCE_TERM==null) LOCATION_SEQUENCE_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("locseq");
            return LOCATION_SEQUENCE_TERM;
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
            rlistener.startSequence();
            DefaultHandler m_handler = new UniProtXMLHandler(this,symParser,rlistener,ns);
            boolean hasMore=XMLTools.readXMLChunk(reader, m_handler, ENTRY_TAG);
            // deal with copyright chunk
            reader.mark(1000);
            String line = reader.readLine();
            reader.reset();
            if (line.contains("<"+COPYRIGHT_TAG)) XMLTools.readXMLChunk(reader, m_handler, COPYRIGHT_TAG);
            // all done!
            rlistener.endSequence();
            return hasMore;
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
        xml.openTag(ENTRY_GROUP_TAG);
        xml.attribute("xmlns","http://uniprot.org/uniprot");
        xml.attribute("xmlns","xsi","http://www.w3.org/2001/XMLSchema-instancee");
        xml.attribute("xsi","schemaLocation","http://uniprot.org/uniprot http://www.uniprot.org/support/docs/uniprot.xsd");
    }
    
    /**
     * {@inheritDoc}
     */
    public void finishWriting() throws IOException {
        xml.closeTag(ENTRY_GROUP_TAG);
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
     * Namespace is ignored as UniProtXML has no concept of it.
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
        
        int key = 1;
        
        Set notes = rs.getNoteSet();
        List accessions = new ArrayList();
        List kws = new ArrayList();
        String stranded = null;
        String cdat = null;
        String udat = null;
        String adat = null;
        String copyright = null;
        String proteinType = null;
        String moltype = rs.getAlphabet().getName();
        Map genenames = new TreeMap();
        Map genesynonyms = new TreeMap();
        Map orfnames = new TreeMap();
        Map ordlocnames = new TreeMap();
        Set evidenceIDs = new TreeSet();
        Map evcats = new TreeMap();
        Map evtypes = new TreeMap();
        Map evdates = new TreeMap();
        Map evattrs = new TreeMap();
        Map speciesRecs = new TreeMap();
        Map strainRecs = new TreeMap();
        Map tissueRecs = new TreeMap();
        Map transpRecs = new TreeMap();
        Map plasmidRecs = new TreeMap();
        for (Iterator i = notes.iterator(); i.hasNext();) {
            Note n = (Note)i.next();
            if (n.getTerm().equals(Terms.getStrandedTerm())) stranded=n.getValue();
            else if (n.getTerm().equals(Terms.getDateCreatedTerm())) cdat=n.getValue();
            else if (n.getTerm().equals(Terms.getDateUpdatedTerm())) udat=n.getValue();
            else if (n.getTerm().equals(Terms.getDateAnnotatedTerm())) adat=n.getValue();
            else if (n.getTerm().equals(Terms.getMolTypeTerm())) moltype=n.getValue();
            else if (n.getTerm().equals(Terms.getAdditionalAccessionTerm())) accessions.add(n.getValue());
            else if (n.getTerm().equals(Terms.getKeywordTerm())) {
                ComparableTerm t = this.uniprotKWOnto.getOrCreateTerm(n.getValue());
                try {
                    if (t.getIdentifier()==null || t.getIdentifier().equals("")) t.setIdentifier("UNKNOWN");
                } catch (ChangeVetoException ce) {
                    IOException e = new IOException("Failed to assign keyword identifier");
                    e.initCause(ce);
                    throw e;
                }
                kws.add(t);
            } else if (n.getTerm().equals(Terms.getCopyrightTerm())) copyright=n.getValue();
            else if (n.getTerm().equals(Terms.getProteinTypeTerm())) proteinType=n.getValue();
            // use the nasty hack to split the reference rank away from the actual value in this field
            else if (n.getTerm().equals(Terms.getGeneNameTerm()))  {
                String ref = n.getValue();
                int colon = ref.indexOf(':');
                Integer refID = new Integer(0);
                if (colon>=1) refID = new Integer(ref.substring(0,colon));
                genenames.put(refID, ref.substring(colon+1)); // map of id -> string as only one name per gene
            } else if (n.getTerm().equals(Terms.getGeneSynonymTerm())) {
                String ref = n.getValue();
                int colon = ref.indexOf(':');
                Integer refID = new Integer(0);
                if (colon>=1) refID = new Integer(ref.substring(0,colon));
                if (genesynonyms.get(refID)==null) genesynonyms.put(refID, new ArrayList());
                ((List)genesynonyms.get(refID)).add(ref.substring(colon+1));
            } else if (n.getTerm().equals(Terms.getOrderedLocusNameTerm())) {
                String ref = n.getValue();
                int colon = ref.indexOf(':');
                Integer refID = new Integer(0);
                if (colon>=1) refID = new Integer(ref.substring(0,colon));
                if (ordlocnames.get(refID)==null) ordlocnames.put(refID, new ArrayList());
                ((List)ordlocnames.get(refID)).add(ref.substring(colon+1));
            } else if (n.getTerm().equals(Terms.getORFNameTerm())) {
                String ref = n.getValue();
                int colon = ref.indexOf(':');
                Integer refID = new Integer(0);
                if (colon>=1) refID = new Integer(ref.substring(0,colon));
                if (orfnames.get(refID)==null) orfnames.put(refID, new ArrayList());
                ((List)orfnames.get(refID)).add(ref.substring(colon+1));
            }
            // use the nasty hack to split the reference rank away from the actual value in this field
            else if (n.getTerm().equals(Terms.getEvidenceCategoryTerm()))  {
                String ref = n.getValue();
                int colon = ref.indexOf(':');
                Integer refID = new Integer(0);
                if (colon>=1) refID = new Integer(ref.substring(0,colon));
                evcats.put(refID, ref.substring(colon+1)); // map of id -> string as only one name per gene
                evidenceIDs.add(refID);
            } else if (n.getTerm().equals(Terms.getEvidenceTypeTerm()))  {
                String ref = n.getValue();
                int colon = ref.indexOf(':');
                Integer refID = new Integer(0);
                if (colon>=1) refID = new Integer(ref.substring(0,colon));
                evtypes.put(refID, ref.substring(colon+1)); // map of id -> string as only one name per gene
                evidenceIDs.add(refID);
            } else if (n.getTerm().equals(Terms.getEvidenceDateTerm()))  {
                String ref = n.getValue();
                int colon = ref.indexOf(':');
                Integer refID = new Integer(0);
                if (colon>=1) refID = new Integer(ref.substring(0,colon));
                evdates.put(refID, ref.substring(colon+1)); // map of id -> string as only one name per gene
                evidenceIDs.add(refID);
            } else if (n.getTerm().equals(Terms.getEvidenceAttrTerm()))  {
                String ref = n.getValue();
                int colon = ref.indexOf(':');
                Integer refID = new Integer(0);
                if (colon>=1) refID = new Integer(ref.substring(0,colon));
                evattrs.put(refID, ref.substring(colon+1)); // map of id -> string as only one name per gene
                evidenceIDs.add(refID);
            }
            // use the nasty hack to split the reference rank away from the actual value in this field
            // we'll end up with a bunch in key 0 for those which did not come from us. We ignore these for now.
            else if (n.getTerm().equals(Terms.getSpeciesTerm())) {
                String ref = n.getValue();
                int colon = ref.indexOf(':');
                Integer refID = new Integer(0);
                if (colon>=1) refID = new Integer(ref.substring(0,colon));
                if (speciesRecs.get(refID)==null) speciesRecs.put(refID, new ArrayList());
                ((List)speciesRecs.get(refID)).add(ref.substring(colon+1));
            } else if (n.getTerm().equals(Terms.getStrainTerm()))  {
                String ref = n.getValue();
                int colon = ref.indexOf(':');
                Integer refID = new Integer(0);
                if (colon>=1) refID = new Integer(ref.substring(0,colon));
                if (strainRecs.get(refID)==null) strainRecs.put(refID, new ArrayList());
                ((List)strainRecs.get(refID)).add(ref.substring(colon+1));
            } else if (n.getTerm().equals(Terms.getTissueTerm()))  {
                String ref = n.getValue();
                int colon = ref.indexOf(':');
                Integer refID = new Integer(0);
                if (colon>=1) refID = new Integer(ref.substring(0,colon));
                if (tissueRecs.get(refID)==null) tissueRecs.put(refID, new ArrayList());
                ((List)tissueRecs.get(refID)).add(ref.substring(colon+1));
            } else if (n.getTerm().equals(Terms.getTransposonTerm()))  {
                String ref = n.getValue();
                int colon = ref.indexOf(':');
                Integer refID = new Integer(0);
                if (colon>=1) refID = new Integer(ref.substring(0,colon));
                if (transpRecs.get(refID)==null) transpRecs.put(refID, new ArrayList());
                ((List)transpRecs.get(refID)).add(ref.substring(colon+1));
            } else if (n.getTerm().equals(Terms.getPlasmidTerm()))  {
                String ref = n.getValue();
                int colon = ref.indexOf(':');
                Integer refID = new Integer(0);
                if (colon>=1) refID = new Integer(ref.substring(0,colon));
                if (plasmidRecs.get(refID)==null) plasmidRecs.put(refID, new ArrayList());
                ((List)plasmidRecs.get(refID)).add(ref.substring(colon+1));
            }
        }
        
        xml.openTag(ENTRY_TAG);
        xml.attribute(ENTRY_NAMESPACE_ATTR,ns.getName());
        xml.attribute(ENTRY_CREATED_ATTR,cdat);
        xml.attribute(ENTRY_UPDATED_ATTR,(adat==null?cdat:adat)); // annotation update
        
        xml.openTag(ACCESSION_TAG);
        xml.print(rs.getAccession());
        xml.closeTag(ACCESSION_TAG);
        
        xml.openTag(NAME_TAG);
        xml.print(rs.getName());
        xml.closeTag(NAME_TAG);
        
        xml.openTag(PROTEIN_TAG);
        if (proteinType!=null) xml.attribute(TYPE_ATTR,proteinType);
        String desc = rs.getDescription().trim(); // this is only going to make sense if it was a UniProt seq to start with
        desc = desc.substring(0, desc.length()-1); // chomp trailing dot
        String[] parts = desc.split("\\[");
        for (int j = 0 ; j < parts.length; j++) {
            if (parts[j].startsWith(Terms.CONTAINS_PREFIX)) {
                // contains section
                String chunk = parts[j].substring(Terms.CONTAINS_PREFIX.length()+1).trim();
                chunk = chunk.substring(0, chunk.length()-1); // chomp trailing ]
                String[] moreparts = chunk.split(";");
                for (int k = 0; k < moreparts.length; k++) {
                    xml.openTag(DOMAIN_TAG);
                    String[] names = moreparts[k].split("\\(");
                    for (int l = 0; l < names.length; l++) {
                        String name = names[l].trim();
                        if (l>0) name = name.substring(0,name.length()-1); // chomp trailing )
                        xml.openTag(NAME_TAG);
                        xml.print(name);
                        xml.closeTag(NAME_TAG);
                    }
                    xml.closeTag(DOMAIN_TAG);
                }
            } else if (parts[j].startsWith(Terms.INCLUDES_PREFIX)) {
                // includes section
                String chunk = parts[j].substring(Terms.INCLUDES_PREFIX.length()+1).trim();
                chunk = chunk.substring(0, chunk.length()-1); // chomp trailing ]
                String[] moreparts = chunk.split(";");
                for (int k = 0; k < moreparts.length; k++) {
                    xml.openTag(COMPONENT_TAG);
                    String[] names = moreparts[k].split("\\(");
                    for (int l = 0; l < names.length; l++) {
                        String name = names[l].trim();
                        if (l>0) name = name.substring(0,name.length()-1); // chomp trailing )
                        xml.openTag(NAME_TAG);
                        xml.print(name);
                        xml.closeTag(NAME_TAG);
                    }
                    xml.closeTag(COMPONENT_TAG);
                }
            } else {
                // plain names
                String[] names = parts[j].split("\\(");
                for (int l = 0; l < names.length; l++) {
                    String name = names[l].trim();
                    if (l>0) name = name.substring(0,name.length()-1); // chomp trailing )
                    xml.openTag(NAME_TAG);
                    xml.print(name);
                    xml.closeTag(NAME_TAG);
                }
            }
        }
        xml.closeTag(PROTEIN_TAG);
        
        // gene line
        for (Iterator i = genenames.keySet().iterator(); i.hasNext(); ) {
            Integer geneid = (Integer)i.next();
            String genename = (String)genenames.get(geneid);
            List synonyms = (List)genesynonyms.get(geneid);
            List orfs = (List)orfnames.get(geneid);
            List ordlocs = (List)ordlocnames.get(geneid);
            
            xml.openTag(GENE_TAG);
            
            xml.openTag(NAME_TAG);
            xml.attribute(TYPE_ATTR,Terms.GENENAME_KEY);
            xml.print(genename);
            xml.closeTag(NAME_TAG);
            
            if (synonyms!=null) {
                for (Iterator j = synonyms.iterator(); j.hasNext(); ) {
                    xml.openTag(NAME_TAG);
                    xml.attribute(TYPE_ATTR,Terms.GENESYNONYM_KEY);
                    xml.print((String)j.next());
                    xml.closeTag(NAME_TAG);
                }
            }
            if (ordlocs!=null) {
                for (Iterator j = synonyms.iterator(); j.hasNext(); ) {
                    xml.openTag(NAME_TAG);
                    xml.attribute(TYPE_ATTR,Terms.ORDLOCNAME_KEY);
                    xml.print((String)j.next());
                    xml.closeTag(NAME_TAG);
                }
            }
            if (orfs!=null) {
                for (Iterator j = synonyms.iterator(); j.hasNext(); ) {
                    xml.openTag(NAME_TAG);
                    xml.attribute(TYPE_ATTR,Terms.ORFNAME_KEY);
                    xml.print((String)j.next());
                    xml.closeTag(NAME_TAG);
                }
            }
            
            xml.closeTag(GENE_TAG);
        }
        
        // source line (from taxon)
        //   organism line
        NCBITaxon tax = rs.getTaxon();
        if (tax!=null) {
            xml.openTag(ORGANISM_TAG);
            xml.attribute(KEY_ATTR,""+(key++));
            
            for (Iterator i = tax.getNameClasses().iterator(); i.hasNext(); ) {
                String nameclass = (String)i.next();
                String ournameclass = Terms.COMMON_NAME_KEY;
                if (nameclass.equals(Terms.FULL_NAME_KEY)) ournameclass = NCBITaxon.EQUIVALENT;
                else if (nameclass.equals(Terms.SCIENTIFIC_NAME_KEY)) ournameclass = NCBITaxon.SCIENTIFIC;
                else if (nameclass.equals(Terms.SYNONYM_NAME_KEY)) ournameclass = NCBITaxon.SYNONYM;
                else if (nameclass.equals(Terms.ABBREV_NAME_KEY)) ournameclass = NCBITaxon.ACRONYM;
                for (Iterator j = tax.getNames(nameclass).iterator(); j.hasNext(); ) {
                    xml.openTag(NAME_TAG);
                    xml.attribute(TYPE_ATTR,ournameclass);
                    xml.print((String)j.next());
                    xml.closeTag(NAME_TAG);
                }
            }
            
            xml.openTag(DBXREF_TAG);
            xml.attribute(KEY_ATTR,""+(key++));
            xml.attribute(TYPE_ATTR,Terms.NCBI_TAXON_KEY);
            xml.attribute(ID_ATTR,""+tax.getNCBITaxID());
            xml.closeTag(DBXREF_TAG);
            
            String h = tax.getNameHierarchy();
            h = h.substring(0, h.length()-1); // chomp dot
            String[] hierarch = h.split(";");
            xml.openTag(LINEAGE_TAG);
            for (int j = 0; j < hierarch.length; j++) {
                xml.openTag(TAXON_TAG);
                xml.print(hierarch[j].trim());
                xml.closeTag(TAXON_TAG);
            }
            xml.closeTag(LINEAGE_TAG);
            
            xml.closeTag(ORGANISM_TAG);
        }
        
        // docrefs
        for (Iterator i = rs.getRankedDocRefs().iterator(); i.hasNext(); ) {
            RankedDocRef rdr = (RankedDocRef)i.next();
            DocRef dr = rdr.getDocumentReference();
            
            xml.openTag(REFERENCE_TAG);
            xml.attribute(KEY_ATTR,""+(key++));
            
            xml.openTag(CITATION_TAG);
            xml.attribute(TYPE_ATTR,"journal article");
            
            if (dr.getTitle()!=null) {
                xml.openTag(TITLE_TAG);
                xml.print(dr.getTitle());
                xml.closeTag(TITLE_TAG);
            }
            
            List auths = dr.getAuthorList();
            xml.openTag(EDITOR_LIST_TAG);
            for (Iterator j = auths.iterator(); j.hasNext(); ) {
                DocRefAuthor a = (DocRefAuthor)j.next();
                if (a.isEditor()) {
                    if (a.isConsortium()) {
                        xml.openTag(CONSORTIUM_TAG);
                        xml.print(a.getName());
                        xml.closeTag(CONSORTIUM_TAG);
                    } else {
                        xml.openTag(PERSON_TAG);
                        xml.print(a.getName());
                        xml.closeTag(PERSON_TAG);
                    }
                    j.remove();
                }
            }
            xml.closeTag(EDITOR_LIST_TAG);
            xml.openTag(AUTHOR_LIST_TAG);
            for (Iterator j = auths.iterator(); j.hasNext(); ) {
                DocRefAuthor a = (DocRefAuthor)j.next();
                if (a.isConsortium()) {
                    xml.openTag(CONSORTIUM_TAG);
                    xml.print(a.getName());
                    xml.closeTag(CONSORTIUM_TAG);
                } else {
                    xml.openTag(PERSON_TAG);
                    xml.print(a.getName());
                    xml.closeTag(PERSON_TAG);
                }
            }
            xml.closeTag(AUTHOR_LIST_TAG);
            
            xml.openTag(LOCATOR_TAG);
            xml.print(dr.getLocation());
            xml.closeTag(LOCATOR_TAG);
            
            CrossRef cr = dr.getCrossref();
            if (cr!=null) {
                xml.openTag(DBXREF_TAG);
                xml.attribute(TYPE_ATTR,cr.getDbname());
                xml.attribute(ID_ATTR,cr.getAccession());
                xml.attribute(KEY_ATTR,""+(key++));
                if (!cr.getNoteSet().isEmpty()) {
                    for (Iterator j = cr.getNoteSet().iterator(); j.hasNext(); ) {
                        Note n = (Note)j.next();
                        xml.openTag(PROPERTY_TAG);
                        xml.attribute(TYPE_ATTR,n.getTerm().getName());
                        xml.attribute(VALUE_ATTR,n.getValue());
                        xml.closeTag(PROPERTY_TAG);
                    }
                }
                xml.closeTag(DBXREF_TAG);
            }
            
            // RP
            xml.openTag(RP_LINE_TAG);
            xml.print(dr.getRemark());
            xml.closeTag(RP_LINE_TAG);
            // Print out ref position if present
            if (rdr.getStart()!=null && rdr.getEnd()!=null && !rppat.matcher(dr.getRemark()).matches()) {
                xml.openTag(RP_LINE_TAG);
                xml.print("SEQUENCE OF "+rdr.getStart()+"-"+rdr.getEnd()+".");
                xml.closeTag(RP_LINE_TAG);
            }
            
            // RC
            xml.openTag(RC_LINE_TAG);
            Integer rank = new Integer(rdr.getRank());
            if (speciesRecs.get(rank)!=null) {
                for (Iterator j = ((List)speciesRecs.get(rank)).iterator(); j.hasNext(); ) {
                    xml.openTag(RC_SPECIES_TAG);
                    xml.print((String)j.next());
                    xml.closeTag(RC_SPECIES_TAG);
                }
            }
            if (strainRecs.get(rank)!=null) {
                for (Iterator j = ((List)strainRecs.get(rank)).iterator(); j.hasNext(); ) {
                    xml.openTag(RC_STRAIN_TAG);
                    xml.print((String)j.next());
                    xml.closeTag(RC_STRAIN_TAG);
                }
            }
            if (tissueRecs.get(rank)!=null) {
                for (Iterator j = ((List)tissueRecs.get(rank)).iterator(); j.hasNext(); ) {
                    xml.openTag(RC_TISSUE_TAG);
                    xml.print((String)j.next());
                    xml.closeTag(RC_TISSUE_TAG);
                }
            }
            if (transpRecs.get(rank)!=null) {
                for (Iterator j = ((List)transpRecs.get(rank)).iterator(); j.hasNext(); ) {
                    xml.openTag(RC_TRANSP_TAG);
                    xml.print((String)j.next());
                    xml.closeTag(RC_TRANSP_TAG);
                }
            }
            if (plasmidRecs.get(rank)!=null) {
                for (Iterator j = ((List)plasmidRecs.get(rank)).iterator(); j.hasNext(); ) {
                    xml.openTag(RC_PLASMID_TAG);
                    xml.print((String)j.next());
                    xml.closeTag(RC_PLASMID_TAG);
                }
            }
            xml.closeTag(RC_LINE_TAG);
            
            xml.closeTag(CITATION_TAG);
            xml.closeTag(REFERENCE_TAG);
        }
        
        // comments
        for (Iterator i = rs.getComments().iterator(); i.hasNext(); ) {
            // use UniProtCommentParser to convert each text comment from string to object
            // do not print unconvertible ones (eg. no -!- on text)
            Comment c = (Comment)i.next();
            if (UniProtCommentParser.isParseable(c)) {
                // otherwise parse and display appropriately
                UniProtCommentParser ucp = new UniProtCommentParser();
                try {
                    ucp.parseComment(c);
                } catch (ParseException ce) {
                    IOException e = new IOException("Failed to parse comment when outputting");
                    e.initCause(ce);
                    throw e;
                }
                String type = ucp.getCommentType();
                String xtype = type.toLowerCase();
                if (type.equals(UniProtCommentParser.PTM)) xtype = "posttranslational modification";
                else if (type.equals(UniProtCommentParser.DATABASE)) xtype = "online information";
                
                xml.openTag(COMMENT_TAG);
                xml.attribute(TYPE_ATTR,xtype);
                
                // database comment
                if (type.equals(UniProtCommentParser.DATABASE)) {
                    xml.attribute(COMMENT_NAME_ATTR,ucp.getDatabaseName());
                    
                    xml.openTag(COMMENT_LINK_TAG);
                    xml.attribute(COMMENT_LINK_URI_ATTR,ucp.getUri());
                    xml.closeTag(COMMENT_LINK_TAG);
                }
                // mass spec
                else if (type.equals(UniProtCommentParser.MASS_SPECTROMETRY)) {
                    xml.attribute(COMMENT_MASS_ATTR,""+ucp.getMolecularWeight());
                    if (ucp.getMolWeightError()!=null) xml.attribute(COMMENT_ERROR_ATTR,""+ucp.getMolWeightError());
                    xml.attribute(COMMENT_METHOD_ATTR,""+ucp.getMolWeightMethod());
                    
                    xml.openTag(LOCATION_TAG);
                    xml.openTag(LOCATION_BEGIN_TAG);
                    xml.attribute(LOCATION_POSITION_ATTR,""+ucp.getMolWeightRangeStart());
                    xml.closeTag(LOCATION_BEGIN_TAG);
                    xml.openTag(LOCATION_END_TAG);
                    xml.attribute(LOCATION_POSITION_ATTR,""+ucp.getMolWeightRangeEnd());
                    xml.closeTag(LOCATION_END_TAG);
                    xml.closeTag(LOCATION_TAG);
                }
                // interaction
                else if (type.equals(UniProtCommentParser.INTERACTION)) {
                    // UniProt flat allows for multiple interactions per comment, but
                    // UniProtXML only allows for a single one. So, we have to open/close
                    // and write additional comments as necessary.
                    for (Iterator j = ucp.getInteractions().iterator(); j.hasNext(); ) {
                        // process comment
                        Interaction interact = (Interaction)j.next();
                        
                        xml.openTag(COMMENT_INTERACTANT_TAG);
                        xml.attribute(COMMENT_INTERACT_INTACT_ATTR,interact.getFirstIntActID());
                        xml.closeTag(COMMENT_INTERACTANT_TAG);
                        
                        xml.openTag(COMMENT_INTERACTANT_TAG);
                        xml.attribute(COMMENT_INTERACT_INTACT_ATTR,interact.getSecondIntActID());
                        xml.openTag(COMMENT_INTERACT_ID_TAG);
                        xml.print(interact.getID());
                        xml.closeTag(COMMENT_INTERACT_ID_TAG);
                        if (interact.getLabel()!=null) {
                            xml.openTag(COMMENT_INTERACT_LABEL_TAG);
                            xml.print(interact.getLabel());
                            xml.closeTag(COMMENT_INTERACT_LABEL_TAG);
                        }
                        xml.closeTag(COMMENT_INTERACTANT_TAG);
                        
                        xml.openTag(COMMENT_ORGANISMS_TAG);
                        xml.print(interact.isOrganismsDiffer()?"true":"false");
                        xml.closeTag(COMMENT_ORGANISMS_TAG);
                        
                        xml.openTag(COMMENT_EXPERIMENTS_TAG);
                        xml.print(""+interact.getNumberExperiments());
                        xml.closeTag(COMMENT_EXPERIMENTS_TAG);
                        
                        // if has next, close and open next comment tag
                        if (j.hasNext()) {
                            xml.closeTag(COMMENT_TAG);
                            xml.openTag(COMMENT_TAG);
                            xml.attribute(TYPE_ATTR,xtype);
                        }
                    }
                }
                // alternative products
                else if (type.equals(UniProtCommentParser.ALTERNATIVE_PRODUCTS)) {
                    for (Iterator j = ucp.getEvents().iterator(); j.hasNext(); ) {
                        Event event = (Event)j.next();
                        xml.openTag(COMMENT_EVENT_TAG);
                        xml.attribute(TYPE_ATTR,event.getType().toLowerCase());
                        if (event.getType().equals("Alternative splicing")) xml.attribute(COMMENT_EVENT_ISOFORMS_ATTR,""+event.getNamedIsoforms());
                        xml.print(event.getComment());
                        xml.closeTag(COMMENT_EVENT_TAG);
                    }
                    for (Iterator j = ucp.getIsoforms().iterator(); j.hasNext(); ) {
                        Isoform isoform = (Isoform)j.next();
                        xml.openTag(COMMENT_ISOFORM_TAG);
                        for (Iterator k = isoform.getIsoIDs().iterator(); k.hasNext(); ) {
                            xml.openTag(COMMENT_ISOFORM_ID_TAG);
                            xml.print((String)k.next());
                            xml.closeTag(COMMENT_ISOFORM_ID_TAG);
                        }
                        for (Iterator k = isoform.getNames().iterator(); k.hasNext(); ) {
                            xml.openTag(COMMENT_ISOFORM_NAME_TAG);
                            xml.print((String)k.next());
                            xml.closeTag(COMMENT_ISOFORM_NAME_TAG);
                        }
                        xml.openTag(SEQUENCE_TAG);
                        xml.attribute(TYPE_ATTR,isoform.getSequenceType().toLowerCase());
                        if (isoform.getSequenceType().equals("Described")) {
                            xml.attribute(REF_ATTR,isoform.getSequenceRef());
                        }
                        xml.closeTag(SEQUENCE_TAG);
                        xml.openTag(NOTE_TAG);
                        xml.print(isoform.getNote());
                        xml.closeTag(NOTE_TAG);
                    }
                }
                // biophysicoblahblah stuff
                else if (type.equals(UniProtCommentParser.BIOPHYSICOCHEMICAL_PROPERTIES)) {
                    if (ucp.getAbsorptionNote()!=null) {
                        xml.openTag(COMMENT_ABSORPTION_TAG);
                        xml.openTag(COMMENT_ABS_MAX_TAG);
                        xml.print(ucp.getAbsorptionMax());
                        xml.closeTag(COMMENT_ABS_MAX_TAG);
                        xml.openTag(COMMENT_TEXT_TAG);
                        xml.print(ucp.getAbsorptionNote());
                        xml.closeTag(COMMENT_TEXT_TAG);
                        xml.closeTag(COMMENT_ABSORPTION_TAG);
                    }
                    if (ucp.getKineticsNote()!=null) {
                        xml.openTag(COMMENT_KINETICS_TAG);
                        for (Iterator j = ucp.getKMs().iterator(); j.hasNext(); ) {
                            xml.openTag(COMMENT_KIN_KM_TAG);
                            xml.print((String)j.next());
                            xml.closeTag(COMMENT_KIN_KM_TAG);
                        }
                        for (Iterator j = ucp.getVMaxes().iterator(); j.hasNext(); ) {
                            xml.openTag(COMMENT_KIN_VMAX_TAG);
                            xml.print((String)j.next());
                            xml.closeTag(COMMENT_KIN_VMAX_TAG);
                        }
                        xml.openTag(COMMENT_TEXT_TAG);
                        xml.print(ucp.getKineticsNote());
                        xml.closeTag(COMMENT_TEXT_TAG);
                        xml.closeTag(COMMENT_KINETICS_TAG);
                    }
                    if (ucp.getPHDependence()!=null) {
                        xml.openTag(COMMENT_PH_TAG);
                        xml.print(ucp.getPHDependence());
                        xml.closeTag(COMMENT_PH_TAG);
                    }
                    if (ucp.getRedoxPotential()!=null) {
                        xml.openTag(COMMENT_REDOX_TAG);
                        xml.print(ucp.getRedoxPotential());
                        xml.closeTag(COMMENT_REDOX_TAG);
                    }
                    if (ucp.getTemperatureDependence()!=null) {
                        xml.openTag(COMMENT_TEMPERATURE_TAG);
                        xml.print(ucp.getTemperatureDependence());
                        xml.closeTag(COMMENT_TEMPERATURE_TAG);
                    }
                }
                // all other comments
                else {
                    
                    // TODO : other comment types here
                    
                    xml.openTag(COMMENT_TEXT_TAG);
                    xml.print(ucp.getText());
                    xml.closeTag(COMMENT_TEXT_TAG);
                }
                
                // finish comment up
                if (ucp.getNote()!=null) {
                    xml.openTag(NOTE_TAG);
                    xml.print(ucp.getNote());
                    xml.closeTag(NOTE_TAG);
                }
                
                xml.closeTag(COMMENT_TAG);
            }
        }
        
        // xrefs
        for (Iterator i = rs.getRankedCrossRefs().iterator(); i.hasNext(); ) {
            RankedCrossRef rcr = (RankedCrossRef)i.next();
            CrossRef cr = rcr.getCrossRef();
            
            xml.openTag(DBXREF_TAG);
            String dbname = cr.getDbname();
            xml.attribute(TYPE_ATTR,dbname);
            dbname = dbname.toUpperCase(); // for comparison's sake
            xml.attribute(ID_ATTR,cr.getAccession());
            xml.attribute(KEY_ATTR,""+(key++));
            if (!cr.getNoteSet().isEmpty()) {
                int acccount = 2;
                for (Iterator j = cr.getNoteSet().iterator(); j.hasNext(); ) {
                    Note n = (Note)j.next();
                    if (!n.getValue().equals("-") && n.getTerm().equals(Terms.getAdditionalAccessionTerm())) {
                        xml.openTag(PROPERTY_TAG);
                        String name = n.getTerm().getName();
                        if (acccount==2) {
                            // SECONDARY IDENTIFIER
                            if (dbname.equals("HIV") ||
                                    dbname.equals("INTERPRO") ||
                                    dbname.equals("PANTHER") ||
                                    dbname.equals("PFAM") ||
                                    dbname.equals("PIR") ||
                                    dbname.equals("PRINTS") ||
                                    dbname.equals("PRODOM") ||
                                    dbname.equals("REBASE") ||
                                    dbname.equals("SMART") ||
                                    dbname.equals("TIGRFAMS")) {
                                // the secondary identifier is the entry name.
                                name = "entry name";
                            } else if (dbname.equals("PDB")) {
                                // the secondary identifier is the structure determination method, which is controlled vocabulary that currently includes: X-ray(for X-ray crystallography), NMR(for NMR spectroscopy), EM(for electron microscopy and cryo-electron diffraction), Fiber(for fiber diffraction), IR(for infrared spectroscopy), Model(for predicted models) and Neutron(for neutron diffraction).
                                name = "structure determination method";
                            } else if (dbname.equals("DICTYBASE") ||
                                    dbname.equals("ECOGENE") ||
                                    dbname.equals("FLYBASE") ||
                                    dbname.equals("HGNC") ||
                                    dbname.equals("MGI") ||
                                    dbname.equals("RGD") ||
                                    dbname.equals("SGD") ||
                                    dbname.equals("STYGENE") ||
                                    dbname.equals("SUBTILIST") ||
                                    dbname.equals("WORMBASE") ||
                                    dbname.equals("ZFIN")) {
                                // the secondary identifier is the gene designation. If the gene designation is not available, a dash('-') is used.
                                name = "gene designation";
                            } else if (dbname.equals("GO")) {
                                // the second identifier is a 1-letter abbreviation for one of the 3 ontology aspects, separated from the GO term by a column. If the term is longer than 46 characters, the first 43 characters are indicated followed by 3 dots('...'). The abbreviations for the 3 distinct aspects of the ontology are P(biological Process), F(molecular Function), and C(cellular Component).
                                name = "term";
                            } else if (dbname.equals("HAMAP")) {
                                // the secondary identifier indicates if a domain is 'atypical' and/or 'fused', otherwise the field is empty('-').
                                name = "domain";
                            } else if (dbname.equals("ECO2DBASE")) {
                                // the secondary identifier is the latest release number or edition of the database that has been used to derive the cross-reference.
                                name = "release number";
                            } else if (dbname.equals("SWISS-2DPAGE") ||
                                    dbname.equals("HSC-2DPAGE")) {
                                // the secondary identifier is the species or tissue of origin.
                                name = "organism name";
                            } else if (dbname.equals("ENSEMBL")) {
                                // the secondary identifier is the species of origin.
                                name = "organism name";
                            } else if (dbname.equals("PIRSF")) {
                                // the secondary identifier is the protein family name.
                                name = "protein family name";
                            } else if (dbname.equals("AARHUS") ||
                                    dbname.equals("GHENT-2DPAGE")) {
                                // the secondary identifier is either 'IEF' (for isoelectric focusing) or 'NEPHGE' (for non-equilibrium pH gradient electrophoresis).
                                name = "secondary identifier";
                            } else if (dbname.equals("WORMPEP")) {
                                // the secondary identifier is a number attributed by the C.elegans genome-sequencing project to that protein.
                                name = "C.elegans number";
                            } else if (dbname.equals("AGD") ||
                                    dbname.equals("ANU-2DPAGE") ||
                                    dbname.equals("COMPLUYEAST-2DPAGE") ||
                                    dbname.equals("ECHOBASE") ||
                                    dbname.equals("GENEDB_SPOMBE") ||
                                    dbname.equals("GERMONLINE") ||
                                    dbname.equals("GLYCOSUITEDB") ||
                                    dbname.equals("GRAMENE") ||
                                    dbname.equals("H-INVDB") ||
                                    dbname.equals("INTACT") ||
                                    dbname.equals("LEGIOLIST") ||
                                    dbname.equals("LEPROMA") ||
                                    dbname.equals("LISTILIST") ||
                                    dbname.equals("MAIZEDB") ||
                                    dbname.equals("MEROPS") ||
                                    dbname.equals("MIM") ||
                                    dbname.equals("MYPULIST") ||
                                    dbname.equals("OGP") ||
                                    dbname.equals("PHCI-2DPAGE") ||
                                    dbname.equals("PHOSSITE") ||
                                    dbname.equals("PHOTOLIST") ||
                                    dbname.equals("PMMA-2DPAGE") ||
                                    dbname.equals("RAT-HEART-2DPAGE") ||
                                    dbname.equals("REACTOME") ||
                                    dbname.equals("SAGALIST") ||
                                    dbname.equals("SIENA-2DPAGE") ||
                                    dbname.equals("TAIR") ||
                                    dbname.equals("TIGR") ||
                                    dbname.equals("TRANSFAC") ||
                                    dbname.equals("TUBERCULIST")) {
                                // the secondary identifier is not used and a dash('-') is stored in that field.
                                // should never get here - I hope!
                            } else if (dbname.equals("HSSP")) {
                                // the secondary identifier is the entry name of the PDB structure related to that of the entry in which the HSSP cross-reference is present.
                                name = "entry name";
                            } else if (dbname.equals("GENEFARM")) {
                                // the secondary identifier is the gene family identifier. If the gene family identifier is not available, a dash('-') is used.
                                name = "gene family";
                            } else if (dbname.equals("SMR")) {
                                // the secondary identifier indicates the range(s) relevant to the structure model(s).
                                name = "range";
                            } else if (dbname.equals("EMBL") ||
                                    dbname.equals("DDBJ") ||
                                    dbname.equals("GENBANK")) {
                                // PROTEIN_ID; STATUS_IDENTIFIER; MOLECULE_TYPE
                                name = "protein id";
                            } else if (dbname.equals("PROSITE")) {
                                // ENTRY_NAME; STATUS.
                                name = "entry name";
                            }
                        } else if (acccount==3) {
                            // TERTIARY IDENTIFIER
                            if (dbname.equals("HAMAP") ||
                                    dbname.equals("PANTHER") ||
                                    dbname.equals("PFAM") ||
                                    dbname.equals("PIRSF") ||
                                    dbname.equals("PRODOM") ||
                                    dbname.equals("SMART") ||
                                    dbname.equals("TIGRFAMS")) {
                                // the tertiary identifier is the number of hits found in the sequence.
                                name = "number of hits";
                            } else if (dbname.equals("GO")) {
                                // the tertiary identifier is a 3-character GO evidence code. The meaning of the evidence codes is: IDA=inferred from direct assay, IMP=inferred from mutant phenotype, IGI=inferred from genetic interaction, IPI=inferred from physical interaction, IEP=inferred from expression pattern, TAS=traceable author statement, NAS=non-traceable author statement, IC=inferred by curator, ISS=inferred from sequence or structural similarity.
                                name = "evidence";
                            } else if (dbname.equals("PDB")) {
                                // the tertiary identifier indicates the chain(s) and the corresponding range, of which the structure has been determined. If the range is unknown, a dash is given rather than the range positions(e.g. 'A/B=-.'), if the chains and the range is unknown, a dash is used.
                                name = "chains";
                            } else if (dbname.equals("EMBL") ||
                                    dbname.equals("DDBJ") ||
                                    dbname.equals("GENBANK")) {
                                // PROTEIN_ID; STATUS_IDENTIFIER; MOLECULE_TYPE
                                name = "status identifier";
                            } else if (dbname.equals("PROSITE")) {
                                // ENTRY_NAME; STATUS.
                                name = "status";
                            }          
                        } else {
                            // QUATERNARY AND ADDITIONAL 
                            if (dbname.equals("EMBL") ||
                                    dbname.equals("DDBJ") ||
                                    dbname.equals("GENBANK")) {
                                // PROTEIN_ID; STATUS_IDENTIFIER; MOLECULE_TYPE
                                name = "molecule type";
                            }   
                        }
                        xml.attribute(TYPE_ATTR,name);
                        xml.attribute(VALUE_ATTR,n.getValue());
                        xml.closeTag(PROPERTY_TAG);
                        acccount++;
                    }
                }
            }
            xml.closeTag(DBXREF_TAG);
        }
        
        // keywords
        for (Iterator j = kws.iterator(); j.hasNext(); ) {
            ComparableTerm t = (ComparableTerm)j.next();
            xml.openTag(KEYWORD_TAG);
            xml.attribute(ID_ATTR,t.getIdentifier());
            xml.print(t.getName());
            xml.closeTag(KEYWORD_TAG);
        }
        
        // features
        for (Iterator i = rs.getFeatureSet().iterator(); i.hasNext(); ) {
            RichFeature f = (RichFeature)i.next();
            String descr = null;
            String ftid = null;
            String ref = null;
            String status = null;
            String original = null;
            String locseq = null;
            List variation = new ArrayList();
            for (Iterator j = f.getNoteSet().iterator(); j.hasNext(); ) {
                Note n = (Note)j.next();
                if (n.getTerm().equals(Terms.getFTIdTerm())) ftid = n.getValue();
                else if (n.getTerm().equals(Terms.getFeatureDescTerm())) descr = n.getValue();
                else if (n.getTerm().equals(Terms.getFeatureStatusTerm())) status = n.getValue();
                else if (n.getTerm().equals(Terms.getFeatureRefTerm())) ref = n.getValue();
                else if (n.getTerm().equals(Terms.getFeatureOriginalTerm())) original = n.getValue();
                else if (n.getTerm().equals(Terms.getFeatureVariationTerm())) variation.add(n.getValue());
                else if (n.getTerm().equals(Terms.getLocationSequenceTerm())) locseq = n.getValue();
            }
            
            xml.openTag(FEATURE_TAG);
            
            xml.attribute(TYPE_ATTR,f.getTypeTerm().getName()); // TODO : need to translate from UniProt flatfile format names?
            if (ftid!=null) xml.attribute(ID_ATTR,ftid);
            if (descr!=null) xml.attribute(FEATURE_DESC_ATTR,descr);
            if (ref!=null) xml.attribute(REF_ATTR,ref);
            if (status!=null) xml.attribute(FEATURE_STATUS_ATTR,status);
            if (original!=null) {
                xml.openTag(FEATURE_ORIGINAL_TAG);
                xml.print(original.trim());
                xml.closeTag(FEATURE_ORIGINAL_TAG);
            }
            for (Iterator j = variation.iterator(); j.hasNext(); ) {
                xml.openTag(FEATURE_VARIATION_TAG);
                xml.print(((String)j.next()).trim());
                xml.closeTag(FEATURE_VARIATION_TAG);
            }
            
            xml.openTag(LOCATION_TAG);
            if (locseq!=null) xml.attribute(LOCATION_SEQ_ATTR,locseq.trim());
            RichLocation rl = (RichLocation)f.getLocation();
            if (rl.getMinPosition().equals(rl.getMaxPosition())) {
                // point position
                xml.attribute(LOCATION_POSITION_ATTR,""+rl.getMin());
            } else {
                // range position
                // begin
                xml.openTag(LOCATION_BEGIN_TAG);
                Position begin = rl.getMinPosition();
                if (begin.getFuzzyStart()) xml.attribute(LOCATION_STATUS_ATTR,"less than");
                else if (begin.getFuzzyEnd()) xml.attribute(LOCATION_STATUS_ATTR,"greater than");
                xml.attribute(LOCATION_POSITION_ATTR,""+begin.getStart());
                xml.closeTag(LOCATION_BEGIN_TAG);
                // end
                xml.openTag(LOCATION_END_TAG);
                Position end = rl.getMaxPosition();
                if (end.getFuzzyStart()) xml.attribute(LOCATION_STATUS_ATTR,"less than");
                else if (end.getFuzzyEnd()) xml.attribute(LOCATION_STATUS_ATTR,"greater than");
                xml.attribute(LOCATION_POSITION_ATTR,""+end.getEnd());
                xml.closeTag(LOCATION_END_TAG);
            }
            xml.closeTag(LOCATION_TAG);
            
            xml.closeTag(FEATURE_TAG);
        }
        
        // evidence
        for (Iterator i = evidenceIDs.iterator(); i.hasNext(); ) {
            Integer evidenceID = (Integer)i.next();
            String cat = (String)evcats.get(evidenceID);
            String type = (String)evtypes.get(evidenceID);
            String date = (String)evdates.get(evidenceID);
            String attr = (String)evattrs.get(evidenceID);
            
            xml.openTag(EVIDENCE_TAG);
            xml.attribute(KEY_ATTR,""+(key++));
            xml.attribute(EVIDENCE_CATEGORY_ATTR,cat);
            xml.attribute(EVIDENCE_DATE_ATTR,date);
            xml.attribute(TYPE_ATTR,type);
            if (attr!=null) xml.attribute(EVIDENCE_ATTRIBUTE_ATTR,attr);
            xml.closeTag(EVIDENCE_TAG);
        }
        
        // sequence
        int mw = 0;
        try {
            mw = (int)MassCalc.getMass(rs, SymbolPropertyTable.AVG_MASS, false);
        } catch (IllegalSymbolException e) {
            throw new RuntimeException("Found illegal symbol", e);
        }
        CRC64Checksum crc = new CRC64Checksum();
        String seqstr = rs.seqString();
        crc.update(seqstr.getBytes(),0,seqstr.length());
        xml.openTag(SEQUENCE_TAG);
        xml.attribute(SEQUENCE_LENGTH_ATTR,""+rs.length());
        xml.attribute(SEQUENCE_MASS_ATTR,""+mw);
        xml.attribute(SEQUENCE_CHECKSUM_ATTR,""+crc);
        xml.attribute(SEQUENCE_MODIFIED_ATTR,(udat==null?cdat:udat)); // sequence update
        String[] lines = StringTools.wordWrap(rs.seqString(), "\\s+", this.getLineWidth());
        for (int i = 0; i < lines.length; i ++) xml.println(lines[i]);
        xml.closeTag(SEQUENCE_TAG);
        
        // close entry
        xml.closeTag(ENTRY_TAG);
        
        // copyright (if present)
        if (copyright!=null) {
            xml.openTag(COPYRIGHT_TAG);
            xml.println(copyright);
            xml.closeTag(COPYRIGHT_TAG);
        }
        
        pw.flush();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultFormat() {
        return UNIPROTXML_FORMAT;
    }
    
// SAX event handler for parsing http://www.ebi.uniprot.org/support/docs/uniprot.xsd
    private class UniProtXMLHandler extends DefaultHandler {
        
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
        private Map currNames = new TreeMap();
        private int currRefStart;
        private int currRefEnd;
        private int currRefRank;
        private int currLocBrackets;
        private int currLocElemBrackets;
        private StringBuffer currLocStr;
        private String currBaseType;
        private String currBaseExtent;
        private boolean firstBase; // oooh err!
        private boolean firstLocationElement;
        private List currDBXrefs = new ArrayList();
        private List currComments = new ArrayList();
        private Map currQuals = new LinkedHashMap();
        
        // construct a new handler that will populate the given list of sequences
        private UniProtXMLHandler(RichSequenceFormat parent,
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
            /*
            if (qName.equals(ENTRY_TAG)) {
                try {
                    if (ns==null) ns=RichObjectFactory.getDefaultNamespace();
                    rlistener.setNamespace(ns);
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String name = attributes.getQName(i).trim();
                        String val = attributes.getValue(i).trim();
                        if (val.equals("")) continue;
                        if (name.equals(ENTRY_ACCESSION_ATTR)) {
                            accession = val;
                            rlistener.setAccession(accession);
                        } else if (name.equals(ENTRY_NAME_ATTR)) rlistener.setName(val);
                        else if (name.equals(ENTRY_DIVISION_ATTR)) rlistener.setDivision(val);
                        else if (name.equals(ENTRY_CREATED_ATTR)) rlistener.addSequenceProperty(Terms.getDateCreatedTerm(),val);
                        else if (name.equals(ENTRY_UPDATED_ATTR)) rlistener.addSequenceProperty(Terms.getDateUpdatedTerm(),val);
                        else if (name.equals(ENTRY_RELCREATED_ATTR)) rlistener.addSequenceProperty(Terms.getRelCreatedTerm(),val);
                        else if (name.equals(ENTRY_RELUPDATED_ATTR)) rlistener.addSequenceProperty(Terms.getRelUpdatedTerm(),val);
                        else if (name.equals(ENTRY_VER_ATTR)) rlistener.setVersion(Integer.parseInt(val));
                    }
                    currNames.clear();
                    currComments.clear();
                    currDBXrefs.clear();
                } catch (ParseException e) {
                    throw new SAXException(e);
                }
            }
             
            else if (qName.equals(REFERENCE_TAG) && !this.parent.getElideReferences()) {
                currRefLocation = null;
                currRefAuthors = new ArrayList();
                currRefTitle = null;
                currRefStart = -999;
                currRefEnd = -999;
                currRefRank = 0;
                currDBXrefs.clear();
                currComments.clear();
            } else if (qName.equals(REFERENCE_POSITION_TAG) && !this.parent.getElideReferences()) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    String name = attributes.getQName(i).trim();
                    String val = attributes.getValue(i).trim();
                    if (val.equals("")) continue;
                    if (name.equals(REF_POS_BEGIN_ATTR)) currRefStart = Integer.parseInt(val);
                    else if (name.equals(REF_POS_END_ATTR)) currRefEnd = Integer.parseInt(val);
                }
            } else if (qName.equals(CITATION_TAG) && !this.parent.getElideReferences()) {
                StringBuffer currRef = new StringBuffer();
                int attrCount = 0;
                for (int i = 0; i < attributes.getLength(); i++) {
                    String name = attributes.getQName(i).trim();
                    String val = attributes.getValue(i).trim();
                    if (val.equals("")) continue;
                    if (name.equals(CITATION_ID_ATTR)) currRefRank = Integer.parseInt(val);
                    // combine everything else into a fake reference to use if locator is a no-show
                    else if (!name.equals(CITATION_TYPE_ATTR)) {
                        if (attrCount!=0) currRef.append(" ");
                        else attrCount = 1;
                        currRef.append(val);
                    }
                }
                currRefLocation = currRef.toString();
            }
             
            else if (qName.equals(DBREFERENCE_TAG)) {
                String db = null;
                String primary = null;
                String secondary = null;
                for (int i = 0; i < attributes.getLength(); i++) {
                    String name = attributes.getQName(i).trim();
                    String val = attributes.getValue(i).trim();
                    if (val.equals("")) continue;
                    if (name.equals(DBREF_DB_ATTR)) db = val;
                    else if (name.equals(DBREF_PRIMARY_ATTR)) primary = val;
                    else if (name.equals(DBREF_SEC_ATTR)) secondary = val;
                }
                CrossRef dbx = (CrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{db, primary, new Integer(0)});
                if (secondary!=null) {
                    Note note = new SimpleNote(Terms.getAdditionalAccessionTerm(),secondary,0);
                    try {
                        ((RichAnnotation)dbx.getAnnotation()).addNote(note);
                    } catch (ChangeVetoException ce) {
                        SAXException pe = new SAXException("Could not annotate identifier terms");
                        pe.initCause(ce);
                        throw pe;
                    }
                }
                currDBXrefs.add(dbx);
            }
             
            else if (qName.equals(FEATURE_TAG) && !this.parent.getElideFeatures()) {
                templ = new RichFeature.Template();
                templ.annotation = new SimpleRichAnnotation();
                templ.sourceTerm = Terms.getUniProtXMLTerm();
                templ.featureRelationshipSet = new TreeSet();
                templ.rankedCrossRefs = new TreeSet();
                for (int i = 0; i < attributes.getLength(); i++) {
                    String name = attributes.getQName(i).trim();
                    String val = attributes.getValue(i).trim();
                    if (val.equals("")) continue;
                    if (name.equals(FEATURE_NAME_ATTR)) {
                        templ.typeTerm = RichObjectFactory.getDefaultOntology().getOrCreateTerm(val);
                    }
                }
                currLocStr = new StringBuffer();
                currDBXrefs.clear();
                currQuals.clear();
            } else if (qName.equals(QUALIFIER_TAG) && !this.parent.getElideFeatures()) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    String name = attributes.getQName(i).trim();
                    String val = attributes.getValue(i).trim();
                    if (val.equals("")) continue;
                    if (name.equals(QUALIFIER_NAME_ATTR)) currFeatQual = val;
                }
            } else if (qName.equals(LOCATION_TAG) && !this.parent.getElideFeatures()) {
                currLocBrackets = 0;
                for (int i = 0; i < attributes.getLength(); i++) {
                    String name = attributes.getQName(i).trim();
                    String val = attributes.getValue(i).trim();
                    if (val.equals("")) continue;
                    if (name.equals(LOCATION_TYPE_ATTR) && !val.equals("single")) {
                        // open a bracket just in case
                        currLocStr.append(val);
                        currLocStr.append("(");
                        currLocBrackets++;
                    } else if (name.equals(LOCATION_COMPL_ATTR) && val.equals("true")) {
                        currLocStr.append("complement");
                        currLocStr.append("(");
                        currLocBrackets++;
                    }
                }
                firstLocationElement = true;
            } else if (qName.equals(LOCATION_ELEMENT_TAG) && !this.parent.getElideFeatures()) {
                String currAcc = null;
                String currVer = null;
                if (!firstLocationElement) currLocStr.append(",");
                for (int i = 0; i < attributes.getLength(); i++) {
                    String name = attributes.getQName(i).trim();
                    String val = attributes.getValue(i).trim();
                    if (val.equals("")) continue;
                    if (name.equals(LOCATION_COMPL_ATTR) && val.equals("true")) {
                        currLocStr.append("complement");
                        currLocStr.append("(");
                        currLocElemBrackets++;
                    } else if (name.equals(LOC_ELEMENT_ACC_ATTR)) currAcc = val;
                    else if (name.equals(LOC_ELEMENT_VER_ATTR)) currVer = val;
                }
                if (currAcc!=null) {
                    currLocStr.append(currAcc);
                    if (currVer!=null) {
                        currLocStr.append(".");
                        currLocStr.append(currVer);
                    }
                    currLocStr.append(":");
                }
                firstBase = true;
            } else if (qName.equals(BASEPOSITION_TAG) && !this.parent.getElideFeatures()) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    String name = attributes.getQName(i).trim();
                    String val = attributes.getValue(i).trim();
                    if (val.equals("")) continue;
                    if (name.equals(BASEPOSITION_TYPE_ATTR)) currBaseType = val;
                    else if (name.equals(BASEPOSITION_EXTENT_ATTR)) currBaseExtent = val;
                }
            }
             
            else if (qName.equals(SEQUENCE_TAG)) {
                try {
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String name = attributes.getQName(i).trim();
                        String val = attributes.getValue(i).trim();
                        if (val.equals("")) continue;
                        if (name.equals(SEQUENCE_TYPE_ATTR)) {
                            rlistener.addSequenceProperty(Terms.getMolTypeTerm(),val);
                        } else if (name.equals(SEQUENCE_VER_ATTR)) {
                            rlistener.setSeqVersion(val);
                        } else if (name.equals(SEQUENCE_TOPOLOGY_ATTR) && val.equals("circular")) {
                            rlistener.setCircular(true);
                        }
                    }
                } catch (ParseException e) {
                    SAXException pe = new SAXException("Could not set sequence properties");
                    pe.initCause(e);
                    throw pe;
                }
            }
             */
        }
        
        // process a closing tag - we will have read the text already
        public void endElement(String uri, String localName, String qName) throws SAXException {
            /*
            String val = this.m_currentString.toString().trim();
             
            try {
                if (qName.equals(SEC_ACC_TAG)) {
                    rlistener.addSequenceProperty(Terms.getAdditionalAccessionTerm(),val);
                } else if (qName.equals(DESC_TAG)) {
                    rlistener.setDescription(val);
                } else if (qName.equals(KEYWORD_TAG)) {
                    rlistener.addSequenceProperty(Terms.getKeywordTerm(), val);
                } else if (qName.equals(COMMENT_TAG)) {
                    currComments.add(val);
                }
             
                else if (qName.equals(TITLE_TAG)) {
                    currRefTitle = val;
                } else if (qName.equals(AUTHOR_TAG)) {
                    currRefAuthors.add(val);
                } else if (qName.equals(LOCATOR_TAG)) {
                    currRefLocation = val;
                } else if (qName.equals(REFERENCE_TAG) && !this.parent.getElideReferences()) {
                    // authors
                    String authors = "";
                    for (Iterator j = currRefAuthors.iterator(); j.hasNext();) {
                        authors+=(String)j.next();
                        if (j.hasNext()) authors+=", ";
                    }
                    // do the crossrefs
                    int k = 0;
                    CrossRef useForDocRef = null;
                    for (Iterator j = currDBXrefs.iterator(); j.hasNext();) {
                        CrossRef dbx = (CrossRef)j.next();
                        RankedCrossRef rdbx = new SimpleRankedCrossRef(dbx, k++);
                        rlistener.setRankedCrossRef(rdbx);
                        if (useForDocRef==null) useForDocRef = dbx;
                        else {
                            // medline gets priority, then pubmed - if multiple, use last
                            if (dbx.getDbname().equals(Terms.MEDLINE_KEY) || (dbx.getDbname().equals(Terms.PUBMED_KEY) && !useForDocRef.getDbname().equals(Terms.MEDLINE_KEY))) {
                                useForDocRef = dbx;
                            }
                        }
                    }
                    // do the comment - will only be one, if any
                    String currRefRemark = null;
                    if (currComments.size()>0) currRefRemark = (String)currComments.iterator().next();
                    // create the docref object
                    try {
                        DocRef dr = (DocRef)RichObjectFactory.getObject(SimpleDocRef.class,new Object[]{authors,currRefLocation});
                        if (currRefTitle!=null) dr.setTitle(currRefTitle);
                        // assign the pubmed or medline to the docref - medline gets priority
                        if (useForDocRef!=null) dr.setCrossref(useForDocRef);
                        // assign the remarks
                        dr.setRemark(currRefRemark);
                        // assign the docref to the bioentry
                        RankedDocRef rdr = new SimpleRankedDocRef(dr,
                                (currRefStart != -999 ? new Integer(currRefStart) : null),
                                (currRefEnd != -999 ? new Integer(currRefEnd) : null),
                                currRefRank);
                        rlistener.setRankedDocRef(rdr);
                    } catch (ChangeVetoException e) {
                        throw new ParseException(e);
                    }
                    currDBXrefs.clear();
                    currComments.clear();
                }
             
                else if (qName.equals(LOCATION_TAG) && !this.parent.getElideFeatures()) {
                    while (currLocBrackets-->0) currLocStr.append(")"); // close the location groups
                    String tidyLocStr = currLocStr.toString().replaceAll("\\s+","");
                    templ.location = GenbankLocationParser.parseLocation(ns, accession, tidyLocStr);
                } else if (qName.equals(LOCATION_ELEMENT_TAG) && !this.parent.getElideFeatures()) {
                    while (currLocElemBrackets-->0) currLocStr.append(")"); // close the location groups
                    firstLocationElement = false;
                } else if (qName.equals(BASEPOSITION_TAG) && !this.parent.getElideFeatures()) {
                    if (!firstBase) currLocStr.append("..");
                    // left angle bracket, right angle bracket, simple, fuzzy
                    if (currBaseType.equals("<")) {
                        currLocStr.append("<");
                        currLocStr.append(val);
                    } else if (currBaseType.equals(">")) {
                        currLocStr.append(val);
                        currLocStr.append(">");
                    } else if (currBaseType.equals("simple")) {
                        currLocStr.append(val);
                    } else if (currBaseType.equals("fuzzy")) {
                        int refVal = Integer.parseInt(val);
                        int extentVal = Integer.parseInt(currBaseExtent);
                        if (refVal + extentVal < refVal) {
                            currLocStr.append("(");
                            currLocStr.append(""+extentVal);
                            currLocStr.append(".");
                            currLocStr.append(""+refVal);
                            currLocStr.append(")");
                        } else {
                            currLocStr.append("(");
                            currLocStr.append(""+refVal);
                            currLocStr.append(".");
                            currLocStr.append(""+extentVal);
                            currLocStr.append(")");
                        }
                    }
                    firstBase = false;
                } else if (qName.equals(QUALIFIER_TAG) && !this.parent.getElideFeatures()) {
                    currQuals.put(currFeatQual,val);
                } else if (qName.equals(FEATURE_TAG) && !this.parent.getElideFeatures()) {
                    // start the feature
                    rlistener.startFeature(templ);
                    // assign qualifiers
                    for (Iterator j = currQuals.keySet().iterator(); j.hasNext(); ) {
                        String qualName = (String)j.next();
                        String qualVal = (String)currQuals.get(qualName);
                        if (qualName.equals("translation")) {
                            // strip spaces from sequence
                            qualVal = qualVal.replaceAll("\\s+","");
                        }
                        rlistener.addFeatureProperty(RichObjectFactory.getDefaultOntology().getOrCreateTerm(qualName),qualVal);
                    }
                    // do the crossrefs
                    int k = 0;
                    for (Iterator j = currDBXrefs.iterator(); j.hasNext();) {
                        CrossRef dbx = (CrossRef)j.next();
                        RankedCrossRef rdbx = new SimpleRankedCrossRef(dbx, k++);
                        try {
                            rlistener.getCurrentFeature().addRankedCrossRef(rdbx);
                        } catch (ChangeVetoException ce) {
                            throw new ParseException(ce);
                        }
                    }
                    // end the feature
                    rlistener.endFeature();
                    currDBXrefs.clear();
                }
             
                else if (qName.equals(TAXID_TAG)) {
                    tax = (NCBITaxon)RichObjectFactory.getObject(SimpleNCBITaxon.class, new Object[]{Integer.valueOf(val)});
                    rlistener.setTaxon(tax);
                    for (Iterator j = currNames.keySet().iterator(); j.hasNext(); ) {
                        String nameClass = (String)j.next();
                        Set nameSet = (Set)currNames.get(nameClass);
                        try {
                            for (Iterator k = nameSet.iterator(); k.hasNext(); ) {
                                String name = (String)k.next();
                                tax.addName(nameClass,name);
                            }
                        } catch (ChangeVetoException ce) {
                            throw new ParseException(ce);
                        }
                    }
                    currNames.clear();
                } else if (qName.equals(SCINAME_TAG)) {
                    try {
                        if (tax==null) {
                            if (!currNames.containsKey(NCBITaxon.SCIENTIFIC)) currNames.put(NCBITaxon.SCIENTIFIC,new TreeSet());
                            ((Set)currNames.get(NCBITaxon.SCIENTIFIC)).add(val);
                        } else {
                            tax.addName(NCBITaxon.SCIENTIFIC,val);
                        }
                    } catch (ChangeVetoException ce) {
                        throw new ParseException(ce);
                    }
                } else if (qName.equals(COMNAME_TAG)) {
                    try {
                        if (tax==null) {
                            if (!currNames.containsKey(NCBITaxon.COMMON)) currNames.put(NCBITaxon.COMMON,new TreeSet());
                            ((Set)currNames.get(NCBITaxon.COMMON)).add(val);
                        } else {
                            tax.addName(NCBITaxon.COMMON,val);
                        }
                    } catch (ChangeVetoException ce) {
                        throw new ParseException(ce);
                    }
                }
             
                else if (qName.equals(SEQUENCE_TAG) && !this.parent.getElideSymbols()) {
                    try {
                        SymbolList sl = new SimpleSymbolList(symParser,
                                val.replaceAll("\\s+","").replaceAll("[\\.|~]","-"));
                        rlistener.addSymbols(symParser.getAlphabet(),
                                (Symbol[])(sl.toList().toArray(new Symbol[0])),
                                0, sl.length());
                    } catch (Exception e) {
                        throw new ParseException(e);
                    }
                }
             
                else if (qName.equals(ENTRY_TAG)) {
                    // do the comments
                    for (Iterator j = currComments.iterator(); j.hasNext();) {
                        rlistener.setComment((String)j.next());
                    }
                    // do the crossrefs
                    int k = 0;
                    CrossRef useForDocRef = null;
                    for (Iterator j = currDBXrefs.iterator(); j.hasNext();) {
                        CrossRef dbx = (CrossRef)j.next();
                        RankedCrossRef rdbx = new SimpleRankedCrossRef(dbx, k++);
                        rlistener.setRankedCrossRef(rdbx);
                        if (useForDocRef==null) useForDocRef = dbx;
                        else {
                            // medline gets priority, then pubmed - if multiple, use last
                            if (dbx.getDbname().equals(Terms.MEDLINE_KEY) || (dbx.getDbname().equals(Terms.PUBMED_KEY) && !useForDocRef.getDbname().equals(Terms.MEDLINE_KEY))) {
                                useForDocRef = dbx;
                            }
                        }
                    }
                    // end the sequence
                    currComments.clear();
                    currDBXrefs.clear();
                }
             
            } catch (ParseException e) {
                throw new SAXException(e);
            }
             
            // drop old string
            this.m_currentString.setLength(0);
             */
        }
        
        // process text inside tags
        public void characters(char[] ch, int start, int length) {
            this.m_currentString.append(ch, start, length);
        }
    }
}

