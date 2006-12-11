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

package org.biojavax.bio.seq;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.NucleotideTools;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.seq.io.EMBLFormat;
import org.biojavax.bio.seq.io.EMBLxmlFormat;
import org.biojavax.bio.seq.io.FastaFormat;
import org.biojavax.bio.seq.io.GenbankFormat;
import org.biojavax.bio.seq.io.INSDseqFormat;
import org.biojavax.bio.seq.io.RichSequenceBuilderFactory;
import org.biojavax.bio.seq.io.RichSequenceFormat;
import org.biojavax.bio.seq.io.RichStreamReader;
import org.biojavax.bio.seq.io.RichStreamWriter;
import org.biojavax.bio.seq.io.UniProtFormat;
import org.biojavax.bio.seq.io.UniProtXMLFormat;
import org.biojavax.ontology.ComparableTerm;

/**
 * A rich sequence is a combination of a org.biojavax.bio.Bioentry
 * and a Sequence. It inherits and merges the methods
 * of both. The RichSequence is based on the BioSQL model and
 * provides a richer array of methods to access information than Sequence
 * does. Whenever possible RichSequence should be used in preference
 * to Sequence.
 * @author Mark Schreiber
 * @author Richard Holland
 * @since 1.5
 */
public interface RichSequence extends BioEntry,Sequence {
    
    public static final ChangeType SYMLISTVERSION = new ChangeType(
            "This sequences's symbollist version has changed",
            "org.biojavax.bio.seq.RichSequence",
            "SYMLISTVERSION"
            );
    
    public static final ChangeType CIRCULAR = new ChangeType(
            "This sequences's circularity has changed",
            "org.biojavax.bio.seq.RichSequence",
            "CIRCULAR"
            );
    
    /**
     * The version of the associated symbol list. Note the use of an object
     * for the value means that it can be nulled.
     * @return  the version
     */
    public Double getSeqVersion();
    
    /**
     * Sets the version of the associated symbol list. Note the use of an object
     * for the value means that it can be nulled.
     * @param seqVersion the version to set.
     * @throws ChangeVetoException if it doesn't want to change.
     */
    public void setSeqVersion(Double seqVersion) throws ChangeVetoException;
    
    /**
     * The features for this sequence.
     * @return a set of RichFeature objects.
     */
    public Set getFeatureSet();
    
    /**
     * Sets the features of this sequence. Note that it is not checked to see if
     * the features actually belong to this sequence, you'd best check that yourself
     * and make changes using feature.setParent() if necessary.
     * @param features the features to assign to this sequence, replacing all others.
     * Must be a set of RichFeature objects.
     * @throws ChangeVetoException if they could not be assigned.
     */
    public void setFeatureSet(Set features) throws ChangeVetoException;
    
    /**
     * Circularises the <code>Sequence</code>. The circular length can then be said to be the
     * length of the sequence itself.
     * @param circular set to true if you want it to be circular
     * @throws ChangeVetoException if the change is blocked. Some implementations
     *   may choose not to support circularisation and should throw an exception here.
     *   Some implementations may only support this method for certain Alphabets.
     */
    public void setCircular(boolean circular) throws ChangeVetoException;
    
    /**
     * Is the sequence circular? Circularity has implications for work with locations
     * and any coordinate work eg symbolAt(int i).
     * Classes that allow it should test this method when working with coordinates or
     * locations / features.
     * @return true if the this is circular else false.
     */
    public boolean getCircular();
    
    /**
     * A special function that returns the SymbolList that this RichSequence is based around.
     * This should _not_ be the RichSequence object itself, as this function is used to perform
     * actions on the symbol list without referring to the RichSequence object directly.
     * @return the internal SymbolList of the RichSequence, NOT the RichSequence object itself.
     */
    public SymbolList getInternalSymbolList();
    
    /**
     * Stores a number of useful terms used across many sequence formats for consistency's sake.
     */
    public static class Terms {
        private static ComparableTerm SEC_ACCESSION_TERM = null;
        private static ComparableTerm KEYWORDS_TERM = null;
        private static ComparableTerm DATE_CREATED_TERM = null;
        private static ComparableTerm DATE_UPDATED_TERM = null;
        private static ComparableTerm DATE_ANNOTATED_TERM = null;
        private static ComparableTerm REL_CREATED_TERM = null;
        private static ComparableTerm REL_UPDATED_TERM = null;
        private static ComparableTerm REL_ANNOTATED_TERM = null;
        private static ComparableTerm MOLTYPE_TERM = null;
        private static ComparableTerm STRANDED_TERM = null;
        private static ComparableTerm ORGANELLE_TERM = null;
        private static ComparableTerm GENENAME_TERM = null;
        private static ComparableTerm GENESYNONYM_TERM = null;
        private static ComparableTerm ORDLOCNAME_TERM = null;
        private static ComparableTerm ORFNAME_TERM = null;
        private static ComparableTerm SPECIES_TERM = null;
        private static ComparableTerm STRAIN_TERM = null;
        private static ComparableTerm TISSUE_TERM = null;
        private static ComparableTerm TRANSPOSON_TERM = null;
        private static ComparableTerm PLASMID_TERM = null;
        private static ComparableTerm DATACLASS_TERM = null;
        private static ComparableTerm FTID_TERM = null;
        private static ComparableTerm FEATUREDESC_TERM = null;
        private static ComparableTerm COPYRIGHT_TERM = null;
        
        public static String SPECIES_KEY = "SPECIES";
        public static String STRAIN_KEY = "STRAIN";
        public static String TISSUE_KEY = "TISSUE";
        public static String TRANSPOSON_KEY = "TRANSPOSON";
        public static String PLASMID_KEY = "PLASMID";
        
        /**
         * Holds a reference to the key that must be used to store PubMed references.
         */
        public static final String PUBMED_KEY = "PUBMED";
        
        /**
         * Holds a reference to the key that must be used to store Medline references.
         */
        public static final String MEDLINE_KEY = "MEDLINE";
        
        /**
         * Holds a reference to the key that must be used to store DOI references.
         */
        public static final String DOI_KEY = "DOI";
        
        /**
         * Getter for the secondary/tertiary/additional accession term
         * @return A Term that represents the secondary accession tag
         */
        public static ComparableTerm getAdditionalAccessionTerm() {
            if (SEC_ACCESSION_TERM==null) SEC_ACCESSION_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("acc");
            return SEC_ACCESSION_TERM;
        }
        
        /**
         * Getter for the keyword term
         * @return a Term that represents the Keyword tag
         */
        public static ComparableTerm getKeywordTerm() {
            if (KEYWORDS_TERM==null) KEYWORDS_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("kw");
            return KEYWORDS_TERM;
        }
        
        /**
         * Getter for the date created term
         * @return a Term
         */
        public static ComparableTerm getDateCreatedTerm() {
            if (DATE_CREATED_TERM==null) DATE_CREATED_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("cdat");
            return DATE_CREATED_TERM;
        }
        
        /**
         * Getter for the date updated term
         * @return a Term
         */
        public static ComparableTerm getDateUpdatedTerm() {
            if (DATE_UPDATED_TERM==null) DATE_UPDATED_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("udat");
            return DATE_UPDATED_TERM;
        }
        
        /**
         * Getter for the date annotated term
         * @return a Term
         */
        public static ComparableTerm getDateAnnotatedTerm() {
            if (DATE_ANNOTATED_TERM==null) DATE_ANNOTATED_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("adat");
            return DATE_ANNOTATED_TERM;
        }
        
        /**
         * Getter for the release created term
         * @return a Term
         */
        public static ComparableTerm getRelCreatedTerm() {
            if (REL_CREATED_TERM==null) REL_CREATED_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("crel");
            return REL_CREATED_TERM;
        }
        
        /**
         * Getter for the release updated term
         * @return a Term
         */
        public static ComparableTerm getRelUpdatedTerm() {
            if (REL_UPDATED_TERM==null) REL_UPDATED_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("urel");
            return REL_UPDATED_TERM;
        }
        
        /**
         * Getter for the release annotated term
         * @return a Term
         */
        public static ComparableTerm getRelAnnotatedTerm() {
            if (REL_ANNOTATED_TERM==null) REL_ANNOTATED_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("arel");
            return REL_ANNOTATED_TERM;
        }
        
        /**
         * getter for the MolType term
         * @return a Term that represents the molecule type
         */
        public static ComparableTerm getMolTypeTerm() {
            if (MOLTYPE_TERM==null) MOLTYPE_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("moltype");
            return MOLTYPE_TERM;
        }
        
        /**
         * Getter for the Strand term
         * @return a Term that represents the Strand tag
         */
        public static ComparableTerm getStrandedTerm() {
            if (STRANDED_TERM==null) STRANDED_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("stranded");
            return STRANDED_TERM;
        }
        
        /**
         * Getter for the Organelle term
         * @return a Term that represents the Organelle tag
         */
        public static ComparableTerm getOrganelleTerm() {
            if (ORGANELLE_TERM==null) ORGANELLE_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("organelle");
            return ORGANELLE_TERM;
        }
        
        /**
         * Getter for the GeneName term
         * @return The GeneName Term
         */
        public static ComparableTerm getGeneNameTerm() {
            if (GENENAME_TERM==null) GENENAME_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("gene_name");
            return GENENAME_TERM;
        }
        
        /**
         * Getter for the GeneSynonym term
         * @return The GeneSynonym Term
         */
        public static ComparableTerm getGeneSynonymTerm() {
            if (GENESYNONYM_TERM==null) GENESYNONYM_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("gene_synonym");
            return GENESYNONYM_TERM;
        }
        
        /**
         * Getter for the OrderedLocusName term
         * @return The OrderedLocusName Term
         */
        public static ComparableTerm getOrderedLocusNameTerm() {
            if (ORDLOCNAME_TERM==null) ORDLOCNAME_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("gene_ordloc");
            return ORDLOCNAME_TERM;
        }
        
        /**
         * Getter for the ORFName term
         * @return The ORFName Term
         */
        public static ComparableTerm getORFNameTerm() {
            if (ORFNAME_TERM==null) ORFNAME_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("gene_orf");
            return ORFNAME_TERM;
        }
        
        /**
         * Getter for the Strain term
         * @return The Strain Term
         */
        public static ComparableTerm getStrainTerm() {
            if (STRAIN_TERM==null) STRAIN_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("strain");
            return STRAIN_TERM;
        }
        
        /**
         * Getter for the Species term
         * @return The Species Term
         */
        public static ComparableTerm getSpeciesTerm() {
            if (SPECIES_TERM==null) SPECIES_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("species");
            return SPECIES_TERM;
        }
        
        /**
         * Getter for the Tissue term
         * @return The Tissue Term
         */
        public static ComparableTerm getTissueTerm() {
            if (TISSUE_TERM==null) TISSUE_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("tissue");
            return TISSUE_TERM;
        }
        
        /**
         * Getter for the Transposon term
         * @return The Transposon Term
         */
        public static ComparableTerm getTransposonTerm() {
            if (TRANSPOSON_TERM==null) TRANSPOSON_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("transposon");
            return TRANSPOSON_TERM;
        }
        
        /**
         * Getter for the Plasmid term
         * @return The plasmid Term
         */
        public static ComparableTerm getPlasmidTerm() {
            if (PLASMID_TERM==null) PLASMID_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("plasmid");
            return PLASMID_TERM;
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
         * Getter for the FTId term
         * @return The FTId Term
         */
        public static ComparableTerm getFTIdTerm() {
            if (FTID_TERM==null) FTID_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("feature_id");
            return FTID_TERM;
        }
        
        /**
         * Getter for the FeatureDesc term
         * @return The FeatureDesc Term
         */
        public static ComparableTerm getFeatureDescTerm() {
            if (FEATUREDESC_TERM==null) FEATUREDESC_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("feature_desc");
            return FEATUREDESC_TERM;
        }
        
        /**
         * Getter for the copyright term
         * @return The copyright Term
         */
        public static ComparableTerm getCopyrightTerm() {
            if (COPYRIGHT_TERM==null) COPYRIGHT_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("copyright");
            return COPYRIGHT_TERM;
        }
    }
    
    /**
     * Some useful tools for working with RichSequence objects.
     * @since 1.5
     */
    public static class Tools {
        
        // because we are static we don't want any instances
        private Tools() {}
        
        /**
         * Create a new RichSequence in the default namespace.
         * @param name The name for the sequence. Will also be used 
         * for the accession.
         * @param seqString The sequence string
         * @param alpha The <CODE>Alphabet</CODE> for the sequence
         * @throws org.biojava.bio.BioException If the symbols in <CODE>seqString</CODE> are
         * not valid in <CODE>alpha</CODE>
         * @return A new <CODE>RichSequence</CODE>. All versions are 1 or 1.0 
         */
        public static RichSequence createRichSequence(String name, String seqString, Alphabet alpha) throws BioException{
            SymbolList syms = new SimpleSymbolList(alpha.getTokenization("token"), seqString);
            return createRichSequence(name, syms);
        }
        
        /**
         * Create a new RichSequence in the specified namespace.
         * @return A new <CODE>RichSequence</CODE>. All versions are 1 or 1.0
         * @param namespace the namespace to create the sequence in. A singleton
         * <CODE>Namespace</CODE> will be created or retrieved as appropriate.
         * @param name The name for the sequence. Will also be used 
         * for the accession.
         * @param seqString The sequence string
         * @param alpha The <CODE>Alphabet</CODE> for the sequence
         * @throws org.biojava.bio.BioException If the symbols in <CODE>seqString</CODE> are
         * not valid in <CODE>alpha</CODE>
         */
        public static RichSequence createRichSequence(String namespace, String name, String seqString, Alphabet alpha) throws BioException{
            SymbolList syms = new SimpleSymbolList(alpha.getTokenization("token"), seqString);
            Namespace ns = (Namespace)RichObjectFactory.getObject(
                             SimpleNamespace.class, 
                             new Object[]{namespace}
                          );
            return createRichSequence(ns, name, syms);
        }
        
        /**
         * Create a new RichSequence in the specified namespace.
         * @return A new <CODE>RichSequence</CODE>. All versions are 1 or 1.0
         * @param ns The namespace to create the sequence in.
         * @param name The name for the sequence. Will also be used 
         * for the accession.
         * @param seqString The sequence string
         * @param alpha The <CODE>Alphabet</CODE> for the sequence
         * @throws org.biojava.bio.BioException If the symbols in <CODE>seqString</CODE> are
         * not valid in <CODE>alpha</CODE>
         */
        public static RichSequence createRichSequence(Namespace ns, String name, String seqString, Alphabet alpha) throws BioException{
            SymbolList syms = new SimpleSymbolList(alpha.getTokenization("token"), seqString);
            return createRichSequence(ns, name, syms);
        }
        
        /**
         * Create a new RichSequence in the default namespace.
         * @return A new <CODE>RichSequence</CODE>. All versions are 1 or 1.0
         * @param syms The symbols to add to the sequence.
         * @param name The name for the sequence. Will also be used 
         * for the accession.
         */
        public static RichSequence createRichSequence(String name, SymbolList syms){
            Namespace ns = RichObjectFactory.getDefaultNamespace();
            return createRichSequence(ns, name, syms);
        }
        
        /**
         * Create a new RichSequence in the specified namespace.
         * @return A new <CODE>RichSequence</CODE>. All versions are 1 or 1.0
         * @param ns the namespace to create the sequence in.
         * @param syms The symbols to add to the sequence.
         * @param name The name for the sequence. Will also be used 
         * for the accession.
         */
        public static RichSequence createRichSequence(Namespace ns, String name, SymbolList syms){
            return new SimpleRichSequence(ns, name, name, 1, syms, new Double(1.0));
        }
        
        /**
         * Boldly attempts to convert a <CODE>Sequence</CODE> into a <CODE>RichSequence</CODE>. <CODE>Sequence</CODE>s
         * will be assigned to the default namespace. The accession will be
         * assumed to be the name of the old sequence.
         * The version of the sequence will be set to 0 and the seqversion set
         * to 0.0. <CODE>Feature</CODE>s are converted to <CODE>RichFeature</CODE>s.
         * The old <CODE>Annotation</CODE> bundle is converted to a <CODE>RichAnnotation</CODE>
         * @param s The <CODE>Sequence</CODE> to enrich
         * @throws ChangeVetoException if <CODE>s</CODE> is locked or the conversion fails.
         * @return a new <CODE>RichSequence</CODE>
         */
        public static RichSequence enrich(Sequence s) throws ChangeVetoException {
            if (s instanceof RichSequence) return (RichSequence)s;
            String name = s.getName();
            RichSequence rs = new SimpleRichSequence(
                    RichObjectFactory.getDefaultNamespace(),
                    name==null?"UnknownName":name,
                    name==null?"UnknownAccession":name,
                    0,
                    s,
                    new Double(0.0));
            // Transfer features
            for (Iterator i = s.features(); i.hasNext(); ) {
                Feature f = (Feature)i.next();
                try {
                    rs.createFeature(f.makeTemplate());
                } catch (BioException e) {
                    throw new ChangeVetoException("They hates us!",e);
                }
            }
            // Transfer annotations
            for (Iterator i = s.getAnnotation().keys().iterator(); i.hasNext(); ) {
                Object key = i.next();
                Object value = s.getAnnotation().getProperty(key);
                rs.getAnnotation().setProperty(key,value);
            }
            return rs;
        }
        
        /**
         * <p>Creates a new sequence from a subregion of another sequence. The sequence is not a view.
         * The sequence can be given a new Namespace, Accession, Name, Identifier etc. or you can
         * copy over the old values. For unique identification in databases we recommend you change
         * at least the name and identifier.</p>
         * <p>The new sequence will retain all features that are fully contained by the new
         *  subsequence, the note set (annotation), Taxon, and
         * description, modified to reflect the subsequence as follows:
         * <pre>
         *      seq.setDescription("subsequence ("+from+":"+to+") of "+s.getDescription());
         * </pre>
         * No other properties are copied.
         * @param newVersion the new version number
         * @param seqVersion the new sequence version
         * @param s the original <code>RichSequence</code>.
         * @param from the 1st subsequence coordinate (inclusive)
         * @param to the last subsequence coordinate (inclusive)
         * @param newNamespace the new <code>Namespace</code>
         * @param newName the new name
         * @param newAccession the new accession number
         * @param newIdentifier the new identifier
         * @throws java.lang.IndexOutOfBoundsException if <CODE>from</CODE> or <CODE>to</CODE> lie outside of the 
         * bounds of <CODE>s</CODE>.
         * @return A new <CODE>RichSequence</CODE>
         */
        public static RichSequence subSequence(RichSequence s,
                                               int from, 
                                               int to, 
                                               Namespace newNamespace,
                                               String newName,
                                               String newAccession,
                                               String newIdentifier,
                                               int newVersion,
                                               Double seqVersion)
        throws IndexOutOfBoundsException {
            SymbolList symList = s.subList(from, to);
            SimpleRichSequence seq = new SimpleRichSequence(
                    newNamespace, 
                    newName, 
                    newAccession, 
                    newVersion, 
                    symList, 
                    seqVersion);
            RichLocation subLoc = new SimpleRichLocation(
                    new SimplePosition(from), new SimplePosition(to), 0);
            try{
                //copy features if appropriate
                for(Iterator i = s.features(); i.hasNext();){
                    RichFeature f = (RichFeature)i.next();
                    
                    if(subLoc.contains(f.getLocation())){
                        RichFeature.Template templ = 
                                (RichFeature.Template)f.makeTemplate();
                        
                        //change the location
                        Position min = new SimplePosition(
                                templ.location.getMin() -from +1);
                        Position max = new SimplePosition(
                                templ.location.getMax() -from +1);
                        templ.location = new SimpleRichLocation(
                                min, max, 0);
                        
                        seq.createFeature(templ);
                    }
                    
                }
                
                //copy other cruft
                if(s.getNoteSet() != null) seq.setNoteSet(s.getNoteSet());
                if(s.getTaxon() !=null) seq.setTaxon(s.getTaxon());
                if(s.getDescription() != null){
                    seq.setDescription("subsequence ("+from+":"+to+") of "+s.getDescription());
                }
                if(s.getDivision() != null){
                  seq.setDivision(s.getDivision());
                }
            }catch(ChangeVetoException ex){
                throw new BioError(ex); //something is rotten in Denmark!
            }catch(BioException ex){
                throw new BioError(ex); //something is rotten in Denmark!
            }
            
            return seq;
        }
    }
    
    /**
     * A set of convenience methods for handling common file formats.
     * @author Mark Schreiber
     * @author Richard Holland
     * @since 1.5
     */
    public final class IOTools {
        
        private static RichSequenceBuilderFactory factory =
                RichSequenceBuilderFactory.THRESHOLD;
        
        // This can't be instantiated.
        private IOTools() {}
        
        /**
         * Register a new format with IOTools for auto-guessing.
         * @param formatClass   the <code>RichSequenceFormat</code> object to register.
         */
        public static void registerFormat(Class formatClass) {
            Object o;
            try {
                o = formatClass.newInstance();
            } catch (Exception e) {
                throw new BioError(e);
            }
            if (!(o instanceof RichSequenceFormat)) throw new BioError("Class "+formatClass+" is not an implementation of RichSequenceFormat!");
            formatClasses.add(formatClass);
        }
        // Private reference to the formats we know about.
        private static List formatClasses = new ArrayList();
        
        /**
         * Guess which format a stream is then attempt to read it.
         * @param stream the <code>BufferedInputStream</code> to attempt to read.
         * @param seqFactory a factory used to build a <code>RichSequence</code>
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then
         *              <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code>
         *              over each sequence in the file
         * @throws IOException in case the stream is unrecognisable or problems occur in reading it.
         */
        public static RichSequenceIterator readStream(BufferedInputStream stream, RichSequenceBuilderFactory seqFactory,
                Namespace ns) throws IOException {
            for (Iterator i = formatClasses.iterator(); i.hasNext(); ) {
                Class formatClass = (Class)i.next();
                RichSequenceFormat format;
                try {
                    format = (RichSequenceFormat)formatClass.newInstance();
                } catch (Exception e) {
                    throw new BioError(e);
                }
                if (format.canRead(stream)) {
                    SymbolTokenization sTok = format.guessSymbolTokenization(stream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                    return new RichStreamReader(br, format, sTok, seqFactory, ns);
                }
            }
            throw new IOException("Could not recognise format of stream.");
        }
        
        /**
         * Guess which format a stream is then attempt to read it.
         * @return a <code>RichSequenceIterator</code>
         *              over each sequence in the file
         * @param stream the <code>BufferedInputStream</code> to attempt to read.
         * @param ns a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then
         *              <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @throws java.io.IOException If the file cannot be read.
         */
        public static RichSequenceIterator readStream(BufferedInputStream stream, Namespace ns) throws IOException {
            return readStream(stream, factory, ns);
        }
        
        /**
         * Guess which format a file is then attempt to read it.
         * @param file  the <code>File</code> to attempt to read.
         * @param seqFactory a factory used to build a <code>RichSequence</code>
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then
         *              <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code>
         *              over each sequence in the file
         * @throws IOException in case the file is unrecognisable or problems occur in reading it.
         */
        public static RichSequenceIterator readFile(File file, RichSequenceBuilderFactory seqFactory,
                Namespace ns) throws IOException {
            for (Iterator i = formatClasses.iterator(); i.hasNext(); ) {
                Class formatClass = (Class)i.next();
                RichSequenceFormat format;
                try {
                    format = (RichSequenceFormat)formatClass.newInstance();
                } catch (Exception e) {
                    throw new BioError(e);
                }
                if (format.canRead(file)) {
                    SymbolTokenization sTok = format.guessSymbolTokenization(file);
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    return new RichStreamReader(br, format, sTok, seqFactory, ns);
                }
            }
            throw new IOException("Could not recognise format of file: "+file.getName());
        }
        
        /**
         * Guess which format a file is then attempt to read it.
         * @return a <code>RichSequenceIterator</code>
         *              over each sequence in the file
         * @param file the <code>File</code> to attempt to read.
         * @param ns a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then
         *              <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @throws java.io.IOException If the file cannot be read.
         */
        public static RichSequenceIterator readFile(File file, Namespace ns) throws IOException {
            return readFile(file, factory, ns);
        }
        
        /**
         * Read a fasta file.
         * @param br    the <code>BufferedReader<code> to read data from
         * @param sTok  a <code>SymbolTokenization</code> that understands the sequences
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then
         *              <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code>
         *              over each sequence in the fasta file
         */
        public static RichSequenceIterator readFasta(
                BufferedReader br, SymbolTokenization sTok, Namespace ns) {
            return new RichStreamReader(br,
                    new FastaFormat(),
                    sTok,
                    factory,
                    ns);
        }
        
        /**
         * Read a fasta file building a custom type of <code>RichSequence</code>.
         * For example, use <code>RichSequenceBuilderFactory.FACTORY</code>
         * to emulate <code>readFasta(BufferedReader, SymbolTokenization)</code>
         * and <code>RichSequenceBuilderFactory.PACKED</code> to force all symbols
         * to be encoded using bit-packing.
         * @param br the <code>BufferedReader</code> to read data from
         * @param sTok a <code>SymbolTokenization</code> that understands the sequences
         * @param seqFactory a factory used to build a <code>RichSequence</code>
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readFasta(
                BufferedReader br,
                SymbolTokenization sTok,
                RichSequenceBuilderFactory seqFactory,
                Namespace ns) {
            return new RichStreamReader(
                    br,
                    new FastaFormat(),
                    sTok,
                    seqFactory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an FASTA-format stream of DNA sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readFastaDNA(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new FastaFormat(),
                    getDNAParser(),
                    factory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an FASTA-format stream of RNA sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readFastaRNA(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new FastaFormat(),
                    getRNAParser(),
                    factory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an FASTA-format stream of Protein sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readFastaProtein(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new FastaFormat(),
                    getProteinParser(),
                    factory,
                    ns);
        }
        
        
        /**
         * Read a GenBank file using a custom type of SymbolList. For example,
         * use RichSequenceBuilderFactory.FACTORY to emulate readFasta(BufferedReader,
         * SymbolTokenization) and RichSequenceBuilderFactory.PACKED to force all
         * symbols to be encoded using bit-packing.
         * @param br the <code>BufferedReader</code> to read data from
         * @param sTok a <code>SymbolTokenization</code> that understands the sequences
         * @param seqFactory a factory used to build a <code>SymbolList</code>
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readGenbank(
                BufferedReader br,
                SymbolTokenization sTok,
                RichSequenceBuilderFactory seqFactory,
                Namespace ns) {
            return new RichStreamReader(
                    br,
                    new GenbankFormat(),
                    sTok,
                    seqFactory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an GenBank-format stream of DNA sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readGenbankDNA(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new GenbankFormat(),
                    getDNAParser(),
                    factory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an GenBank-format stream of RNA sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readGenbankRNA(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new GenbankFormat(),
                    getRNAParser(),
                    factory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an GenBank-format stream of Protein sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readGenbankProtein(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new GenbankFormat(),
                    getProteinParser(),
                    factory,
                    ns);
        }
        
        
        
        
        /**
         * Read a INSDseq file using a custom type of SymbolList. For example,
         * use RichSequenceBuilderFactory.FACTORY to emulate readFasta(BufferedReader,
         * SymbolTokenization) and RichSequenceBuilderFactory.PACKED to force all
         * symbols to be encoded using bit-packing.
         * @param br the <code>BufferedReader</code> to read data from
         * @param sTok a <code>SymbolTokenization</code> that understands the sequences
         * @param seqFactory a factory used to build a <code>SymbolList</code>
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readINSDseq(
                BufferedReader br,
                SymbolTokenization sTok,
                RichSequenceBuilderFactory seqFactory,
                Namespace ns) {
            return new RichStreamReader(
                    br,
                    new INSDseqFormat(),
                    sTok,
                    seqFactory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an INSDseq-format stream of DNA sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readINSDseqDNA(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new INSDseqFormat(),
                    getDNAParser(),
                    factory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an INSDseq-format stream of RNA sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readINSDseqRNA(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new INSDseqFormat(),
                    getRNAParser(),
                    factory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an INSDseq-format stream of Protein sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readINSDseqProtein(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new INSDseqFormat(),
                    getProteinParser(),
                    factory,
                    ns);
        }
        
        
        
        
        /**
         * Read a EMBLxml file using a custom type of SymbolList. For example,
         * use RichSequenceBuilderFactory.FACTORY to emulate readFasta(BufferedReader,
         * SymbolTokenization) and RichSequenceBuilderFactory.PACKED to force all
         * symbols to be encoded using bit-packing.
         * @param br the <code>BufferedReader</code> to read data from
         * @param sTok a <code>SymbolTokenization</code> that understands the sequences
         * @param seqFactory a factory used to build a <code>SymbolList</code>
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readEMBLxml(
                BufferedReader br,
                SymbolTokenization sTok,
                RichSequenceBuilderFactory seqFactory,
                Namespace ns) {
            return new RichStreamReader(
                    br,
                    new EMBLxmlFormat(),
                    sTok,
                    seqFactory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an EMBLxml-format stream of DNA sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readEMBLxmlDNA(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new EMBLxmlFormat(),
                    getDNAParser(),
                    factory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an EMBLxml-format stream of RNA sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readEMBLxmlRNA(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new EMBLxmlFormat(),
                    getRNAParser(),
                    factory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an EMBLxml-format stream of Protein sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readEMBLxmlProtein(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new EMBLxmlFormat(),
                    getProteinParser(),
                    factory,
                    ns);
        }
        
        /**
         * Read a EMBL file using a custom type of SymbolList. For example,
         * use RichSequenceBuilderFactory.FACTORY to emulate readFasta(BufferedReader,
         * SymbolTokenization) and RichSequenceBuilderFactory.PACKED to force all
         * symbols to be encoded using bit-packing.
         * @param br the <code>BufferedReader</code> to read data from
         * @param sTok a <code>SymbolTokenization</code> that understands the sequences
         * @param seqFactory a factory used to build a <code>SymbolList</code>
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readEMBL(
                BufferedReader br,
                SymbolTokenization sTok,
                RichSequenceBuilderFactory seqFactory,
                Namespace ns) {
            return new RichStreamReader(
                    br,
                    new EMBLFormat(),
                    sTok,
                    seqFactory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an EMBL-format stream of DNA sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readEMBLDNA(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new EMBLFormat(),
                    getDNAParser(),
                    factory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an EMBL-format stream of RNA sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readEMBLRNA(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new EMBLFormat(),
                    getRNAParser(),
                    factory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an EMBL-format stream of Protein sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readEMBLProtein(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new EMBLFormat(),
                    getProteinParser(),
                    factory,
                    ns);
        }
        
        
        /**
         * Read a UniProt file using a custom type of SymbolList. For example,
         * use RichSequenceBuilderFactory.FACTORY to emulate readFasta(BufferedReader,
         * SymbolTokenization) and RichSequenceBuilderFactory.PACKED to force all
         * symbols to be encoded using bit-packing.
         * @param br the <code>BufferedReader</code> to read data from
         * @param sTok a <code>SymbolTokenization</code> that understands the sequences
         * @param seqFactory a factory used to build a <code>SymbolList</code>
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readUniProt(
                BufferedReader br,
                SymbolTokenization sTok,
                RichSequenceBuilderFactory seqFactory,
                Namespace ns) {
            return new RichStreamReader(
                    br,
                    new UniProtFormat(),
                    sTok,
                    seqFactory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an UniProt-format stream of RNA sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readUniProt(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new UniProtFormat(),
                    getProteinParser(),
                    factory,
                    ns);
        }
        
        
        /**
         * Read a UniProt XML file using a custom type of SymbolList. For example,
         * use RichSequenceBuilderFactory.FACTORY to emulate readFasta(BufferedReader,
         * SymbolTokenization) and RichSequenceBuilderFactory.PACKED to force all
         * symbols to be encoded using bit-packing.
         * @param br the <code>BufferedReader</code> to read data from
         * @param sTok a <code>SymbolTokenization</code> that understands the sequences
         * @param seqFactory a factory used to build a <code>SymbolList</code>
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readUniProtXML(
                BufferedReader br,
                SymbolTokenization sTok,
                RichSequenceBuilderFactory seqFactory,
                Namespace ns) {
            return new RichStreamReader(
                    br,
                    new UniProtXMLFormat(),
                    sTok,
                    seqFactory,
                    ns);
        }
        
        /**
         * Iterate over the sequences in an UniProt XML-format stream of RNA sequences.
         * @param br the <code>BufferedReader</code> to read data from
         * @param ns    a <code>Namespace</code> to load the sequences into. Null implies that it should
         *              use the namespace specified in the file. If no namespace is
         *              specified in the file, then <code>RichObjectFactory.getDefaultNamespace()</code>
         *              is used.
         * @return      a <code>RichSequenceIterator</code> over each sequence in the fasta file
         */
        public static RichSequenceIterator readUniProtXML(BufferedReader br, Namespace ns) {
            return new RichStreamReader(br,
                    new UniProtXMLFormat(),
                    getProteinParser(),
                    factory,
                    ns);
        }
        
        
        /**
         * Writes <CODE>Sequence</CODE>s from a <code>SequenceIterator</code> to an <code>OutputStream </code>in
         * Fasta Format.  This makes for a useful format filter where a
         * <code>StreamReader</code> can be sent to the <code>RichStreamWriter</code> after formatting.
         * @param os The stream to write fasta formatted data to
         * @param in The source of input <CODE>RichSequence</CODE>s
         * @param ns a <code>Namespace</code> to write the <CODE>RichSequence</CODE>s to. <CODE>Null</CODE> implies that it should
         * use the namespace specified in the individual sequence.
         * @throws java.io.IOException if there is an IO problem
         */
        public static void writeFasta(OutputStream os, SequenceIterator in, Namespace ns)
        throws IOException {
            RichStreamWriter sw = new RichStreamWriter(os,new FastaFormat());
            sw.writeStream(in,ns);
        }
        
        /**
         * Writes a single <code>Sequence</code> to an <code>OutputStream</code> in Fasta format.
         * @param os the <code>OutputStream</code>.
         * @param seq the <code>Sequence</code>.
         * @param ns a <code>Namespace</code> to write the sequences to. Null implies that it should
         *              use the namespace specified in the individual sequence.
         * @throws java.io.IOException if there is an IO problem
         */
        public static void writeFasta(OutputStream os, Sequence seq, Namespace ns)
        throws IOException {
            writeFasta(os, new SingleRichSeqIterator(seq),ns);
        }
        
        
        /**
         * Writes sequences from a <code>SequenceIterator</code> to an <code>OutputStream </code>in
         * GenBank Format.  This makes for a useful format filter where a
         * <code>StreamReader</code> can be sent to the <code>RichStreamWriter</code> after formatting.
         * @param os The stream to write fasta formatted data to
         * @param in The source of input Sequences
         * @param ns a <code>Namespace</code> to write the sequences to. Null implies that it should
         *              use the namespace specified in the individual sequence.
         * @throws java.io.IOException if there is an IO problem
         */
        public static void writeGenbank(OutputStream os, SequenceIterator in, Namespace ns)
        throws IOException {
            RichStreamWriter sw = new RichStreamWriter(os,new GenbankFormat());
            sw.writeStream(in,ns);
        }
        
        /**
         * Writes a single <code>Sequence</code> to an <code>OutputStream</code> in GenBank format.
         * @param os the <code>OutputStream</code>.
         * @param seq the <code>Sequence</code>.
         * @param ns a <code>Namespace</code> to write the sequences to. Null implies that it should
         *              use the namespace specified in the individual sequence.
         * @throws java.io.IOException if there is an IO problem
         */
        public static void writeGenbank(OutputStream os, Sequence seq, Namespace ns)
        throws IOException {
            writeGenbank(os, new SingleRichSeqIterator(seq),ns);
        }
        
        
        /**
         * Writes sequences from a <code>SequenceIterator</code> to an <code>OutputStream </code>in
         * INSDseq Format.  This makes for a useful format filter where a
         * <code>StreamReader</code> can be sent to the <code>RichStreamWriter</code> after formatting.
         * @param os The stream to write fasta formatted data to
         * @param in The source of input Sequences
         * @param ns a <code>Namespace</code> to write the sequences to. Null implies that it should
         *              use the namespace specified in the individual sequence.
         * @throws java.io.IOException if there is an IO problem
         */
        public static void writeINSDseq(OutputStream os, SequenceIterator in, Namespace ns)
        throws IOException {
            RichStreamWriter sw = new RichStreamWriter(os,new INSDseqFormat());
            sw.writeStream(in,ns);
        }
        
        /**
         * Writes a single <code>Sequence</code> to an <code>OutputStream</code> in INSDseq format.
         * @param os the <code>OutputStream</code>.
         * @param seq the <code>Sequence</code>.
         * @param ns a <code>Namespace</code> to write the sequences to. Null implies that it should
         *              use the namespace specified in the individual sequence.
         * @throws java.io.IOException if there is an IO problem
         */
        public static void writeINSDseq(OutputStream os, Sequence seq, Namespace ns)
        throws IOException {
            writeINSDseq(os, new SingleRichSeqIterator(seq),ns);
        }
        
        
        /**
         * Writes sequences from a <code>SequenceIterator</code> to an <code>OutputStream </code>in
         * EMBLxml Format.  This makes for a useful format filter where a
         * <code>StreamReader</code> can be sent to the <code>RichStreamWriter</code> after formatting.
         * @param os The stream to write fasta formatted data to
         * @param in The source of input Sequences
         * @param ns a <code>Namespace</code> to write the sequences to. Null implies that it should
         *              use the namespace specified in the individual sequence.
         * @throws java.io.IOException if there is an IO problem
         */
        public static void writeEMBLxml(OutputStream os, SequenceIterator in, Namespace ns)
        throws IOException {
            RichStreamWriter sw = new RichStreamWriter(os,new EMBLxmlFormat());
            sw.writeStream(in,ns);
        }
        
        /**
         * Writes a single <code>Sequence</code> to an <code>OutputStream</code> in EMBLxml format.
         * @param os the <code>OutputStream</code>.
         * @param seq the <code>Sequence</code>.
         * @param ns a <code>Namespace</code> to write the sequences to. Null implies that it should
         *              use the namespace specified in the individual sequence.
         * @throws java.io.IOException if there is an IO problem
         */
        public static void writeEMBLxml(OutputStream os, Sequence seq, Namespace ns)
        throws IOException {
            writeEMBLxml(os, new SingleRichSeqIterator(seq),ns);
        }
        
        /**
         * Writes sequences from a <code>SequenceIterator</code> to an <code>OutputStream </code>in
         * EMBL Format.  This makes for a useful format filter where a
         * <code>StreamReader</code> can be sent to the <code>RichStreamWriter</code> after formatting.
         * @param os The stream to write fasta formatted data to
         * @param in The source of input Sequences
         * @param ns a <code>Namespace</code> to write the sequences to. Null implies that it should
         *              use the namespace specified in the individual sequence.
         * @throws java.io.IOException if there is an IO problem
         */
        public static void writeEMBL(OutputStream os, SequenceIterator in, Namespace ns)
        throws IOException {
            RichStreamWriter sw = new RichStreamWriter(os,new EMBLFormat());
            sw.writeStream(in,ns);
        }
        
        /**
         * Writes a single <code>Sequence</code> to an <code>OutputStream</code> in EMBL format.
         * @param os the <code>OutputStream</code>.
         * @param seq the <code>Sequence</code>.
         * @param ns a <code>Namespace</code> to write the sequences to. Null implies that it should
         *              use the namespace specified in the individual sequence.
         * @throws java.io.IOException if there is an IO problem
         */
        public static void writeEMBL(OutputStream os, Sequence seq, Namespace ns)
        throws IOException {
            writeEMBL(os, new SingleRichSeqIterator(seq),ns);
        }
        
        
        /**
         * Writes sequences from a <code>SequenceIterator</code> to an <code>OutputStream </code>in
         * UniProt Format.  This makes for a useful format filter where a
         * <code>StreamReader</code> can be sent to the <code>RichStreamWriter</code> after formatting.
         * @param os The stream to write fasta formatted data to
         * @param in The source of input Sequences
         * @param ns a <code>Namespace</code> to write the sequences to. Null implies that it should
         *              use the namespace specified in the individual sequence.
         * @throws java.io.IOException if there is an IO problem
         */
        public static void writeUniProt(OutputStream os, SequenceIterator in, Namespace ns)
        throws IOException {
            RichStreamWriter sw = new RichStreamWriter(os,new UniProtFormat());
            sw.writeStream(in,ns);
        }
        
        /**
         * Writes a single <code>Sequence</code> to an <code>OutputStream</code> in UniProt format.
         * @param os the <code>OutputStream</code>.
         * @param seq the <code>Sequence</code>.
         * @param ns a <code>Namespace</code> to write the sequences to. Null implies that it should
         *              use the namespace specified in the individual sequence.
         * @throws java.io.IOException if there is an IO problem
         */
        public static void writeUniProt(OutputStream os, Sequence seq, Namespace ns)
        throws IOException {
            writeUniProt(os, new SingleRichSeqIterator(seq),ns);
        }
        
        
        /**
         * Writes sequences from a <code>SequenceIterator</code> to an <code>OutputStream </code>in
         * UniProt XML Format.  This makes for a useful format filter where a
         * <code>StreamReader</code> can be sent to the <code>RichStreamWriter</code> after formatting.
         * @param os The stream to write fasta formatted data to
         * @param in The source of input Sequences
         * @param ns a <code>Namespace</code> to write the sequences to. Null implies that it should
         *              use the namespace specified in the individual sequence.
         * @throws java.io.IOException if there is an IO problem
         */
        public static void writeUniProtXML(OutputStream os, SequenceIterator in, Namespace ns)
        throws IOException {
            RichStreamWriter sw = new RichStreamWriter(os,new UniProtXMLFormat());
            sw.writeStream(in,ns);
        }
        
        /**
         * Writes a single <code>Sequence</code> to an <code>OutputStream</code> in UniProt XML format.
         * @param os the <code>OutputStream</code>.
         * @param seq the <code>Sequence</code>.
         * @param ns a <code>Namespace</code> to write the sequences to. Null implies that it should
         *              use the namespace specified in the individual sequence.
         * @throws java.io.IOException if there is an IO problem
         */
        public static void writeUniProtXML(OutputStream os, Sequence seq, Namespace ns)
        throws IOException {
            writeUniProtXML(os, new SingleRichSeqIterator(seq),ns);
        }
        
        
        /**
         * Creates a DNA symbol tokenizer.
         * @return a <code>SymbolTokenization</code> for parsing DNA.
         */
        public static SymbolTokenization getDNAParser() {
            try {
                return DNATools.getDNA().getTokenization("token");
            } catch (BioException ex) {
                throw new BioError("Assertion failing:"
                        + " Couldn't get DNA token parser",ex);
            }
        }
        
        /**
         * Creates a RNA symbol tokenizer.
         * @return a <code>SymbolTokenization</code> for parsing RNA.
         */
        public static SymbolTokenization getRNAParser() {
            try {
                return RNATools.getRNA().getTokenization("token");
            } catch (BioException ex) {
                throw new BioError("Assertion failing:"
                        + " Couldn't get RNA token parser",ex);
            }
        }
        
        /**
         * Creates a nucleotide symbol tokenizer.
         * @return a <code>SymbolTokenization</code> for parsing nucleotides.
         */
        public static SymbolTokenization getNucleotideParser() {
            try {
                return NucleotideTools.getNucleotide().getTokenization("token");
            } catch (BioException ex) {
                throw new BioError("Assertion failing:"
                        + " Couldn't get nucleotide token parser",ex);
            }
        }
        
        /**
         * Creates a protein symbol tokenizer.
         * @return a <code>SymbolTokenization</code> for parsing protein.
         */
        public static SymbolTokenization getProteinParser() {
            try {
                return ProteinTools.getTAlphabet().getTokenization("token");
            } catch (BioException ex) {
                throw new BioError("Assertion failing:"
                        + " Couldn't get PROTEIN token parser",ex);
            }
        }
        
        /**
         * Used to iterate over a single rich sequence
         */
        public static final class SingleRichSeqIterator implements RichSequenceIterator {
            
            private RichSequence seq;
            
            /**
             * Creates an iterator over a single sequence.
             * @param seq the sequence to iterate over.
             */
            public SingleRichSeqIterator(Sequence seq) {
                try {
                    if (seq instanceof RichSequence) this.seq = (RichSequence)seq;
                    else this.seq = RichSequence.Tools.enrich(seq);
                } catch (ChangeVetoException e) {
                    throw new RuntimeException("Unable to enrich sequence",e);
                }
            }
            
            /**
             * {@inheritDoc}
             * @return true if another <CODE>RichSequence</CODE> is available
             */
            public boolean hasNext() {
                return seq != null;
            }
            
            /**
             * {@inheritDoc}
             * @return a <CODE>RichSequence</CODE>
             */
            public Sequence nextSequence() {
                return this.nextRichSequence();
            }
            
            /**
             * {@inheritDoc}
             * @return a <CODE>RichSequence</CODE>
             */
            public BioEntry nextBioEntry() {
                return this.nextRichSequence();
            }
            
            /**
             * {@inheritDoc}
             * @return a <CODE>RichSequence</CODE>
             */
            public RichSequence nextRichSequence() {
                RichSequence seq = this.seq;
                this.seq = null;
                return seq;
            }
        }
    }
}
