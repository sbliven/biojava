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

package org.biojavax.bio.seq.io;

import org.biojava.bio.seq.io.ParseException;
import org.biojavax.Comment;


/**
 *
 * @author Richard Holland
 */
public class UniProtCommentParser {
    
    /**
     * Creates a new instance of UniProtCommentParser.
     */
    public UniProtCommentParser() {
    }
    
    // the prefix for comments
    private static final String PREFIX = "-!-";
    
    /**
     * A name for a comment type.
     */
    public static final String BIOPHYSICOCHEMICAL_PROPERTIES = "BIOPHYSICOCHEMICAL PROPERTIES";
    
    /**
     * A name for a comment type.
     */
    public static final String DATABASE = "DATABASE";
    
    /**
     * A name for a comment type.
     */
    public static final String MASS_SPECTROMETRY = "MASS SPECTROMETRY";
    
    /**
     * A name for a comment type.
     */
    public static final String ALTERNATIVE_PRODUCTS = "ALTERNATIVE PRODUCTS";
    
    /**
     * A name for a comment type.
     */
    public static final String INTERACTION = "INTERACTION";
    
    /**
     * A name for a comment type.
     */
    public static final String PTM = "PTM";
    
    /**
     * Parses the comment string from the given comment and populates
     * the internal fields appropriately.  If the comment is not a
     * UniProt comment (does not start with -!-) then an exception is
     * thrown.
     * @param c the comment to parse.
     * @throws ParseException if the comment was not parseable.
     */
    public void parseComment(Comment c) throws ParseException {
        this.parseComment(c.getComment());
    }
    
    /**
     * Parses the comment string from the given comment and populates
     * the internal fields appropriately. If the comment is not a
     * UniProt comment (does not start with -!-) then an exception is
     * thrown.
     * @param c the comment to parse.
     * @throws ParseException if the comment was not parseable.
     */
    public void parseComment(String c) throws ParseException {
        if (!isParseable(c)) throw new ParseException("Comment is not a UniProt structured comment");
        // do the parsing here.
        c = c.trim().replaceAll("\\s+", " "); // replace all multi-spaces and newlines with single spaces
        // our comment is now one long string, -!- TYPE: [prefix: key=value; | key=value; | text]
        c = c.substring(PREFIX.length()+1); // chomp "-!- "
        String type = c.substring(0,c.indexOf(':')); // find type
        this.setCommentType(type); // remember type
        c = c.substring(c.indexOf(':')+1); // chomp type and colon
        
        // what we have left is the [prefix: key=value; | key=value; | text.] section
        if (this.getCommentType().equals(BIOPHYSICOCHEMICAL_PROPERTIES)) {
            /*
CC   -!- BIOPHYSICOCHEMICAL PROPERTIES:
CC       Absorption:
CC         Abs(max)=xx nm;
CC         Note=free_text;
CC       Kinetic parameters:
CC         KM=xx unit for substrate [(free_text)];
CC         Vmax=xx unit enzyme [free_text];
CC         Note=free_text;
CC       pH dependence:
CC         free_text;
CC       Redox potential:
CC         free_text;
CC       Temperature dependence:
CC         free_text;
             */
        } else if (this.getCommentType().equals(DATABASE)) {
            /*
CC   -!- DATABASE: NAME=Text[; NOTE=Text][; WWW="Address"][; FTP="Address"].
             */
            c = c.substring(0,c.length()-1); // chomp trailing dot
            String[] parts = c.split(";");
            for (int i = 0; i < parts.length; i++) {
                String key = parts[0].trim();
                String value = parts[1].trim();
                if (key.equals("NAME")) this.setDatabaseName(value);
                else if (key.equals("NOTE")) this.setNote(value);
                else if (key.equals("WWW") || key.equals("FTP")) this.setUri(value);
            }
            if (this.getDatabaseName()==null) throw new ParseException("Database name is missing");
        } else if (this.getCommentType().equals(MASS_SPECTROMETRY)) {
            /*
CC   -!- MASS SPECTROMETRY: MW=XXX[; MW_ERR=XX]; METHOD=XX; RANGE=XX-XX[ (Name)]; NOTE={Free text (Ref.n)|Ref.n}.
             */
            c = c.substring(0,c.length()-1); // chomp trailing dot
            String[] parts = c.split(";");
            for (int i = 0; i < parts.length; i++) {
                String key = parts[0].trim();
                String value = parts[1].trim();
                if (key.equals("MW")) this.setMolecularWeight(Integer.parseInt(value));
                else if (key.equals("MW_ERR")) this.setMolWeightError(new Integer(value));
                else if (key.equals("METHOD")) this.setMolWeightMethod(value);
                else if (key.equals("RANGE")) {
                    if (value.indexOf(' ')>-1) value = value.substring(0, value.indexOf(' ')); // drop name
                    String[] locs = value.split("-");
                    this.setMolWeightRangeStart(Integer.parseInt(locs[0]));
                    this.setMolWeightRangeEnd(Integer.parseInt(locs[1]));
                } else if (key.equals("NOTE")) this.setNote(value);
            }
            if (this.getMolWeightMethod()==null) throw new ParseException("Method is missing");
            if (this.getNote()==null) throw new ParseException("Note is missing");
        } else if (this.getCommentType().equals(INTERACTION)) {
            /*
CC   -!- INTERACTION:
CC       {{SP_Ac:identifier[ (xeno)]}|Self}; NbExp=n; IntAct=IntAct_Protein_Ac, IntAct_Protein_Ac;
             */
        } else if (this.getCommentType().equals(ALTERNATIVE_PRODUCTS)) {
            /*
CC   -!- ALTERNATIVE PRODUCTS:
CC       Event=Alternative promoter;
CC         Comment=Free text;
CC       Event=Alternative splicing; Named isoforms=n;
CC         Comment=Optional free text;
CC       Name=Isoform_1; Synonyms=Synonym_1[, Synonym_n];
CC         IsoId=Isoform_identifier_1[, Isoform_identifier_n]; Sequence=Displayed;
CC         Note=Free text;
CC       Name=Isoform_n; Synonyms=Synonym_1[, Synonym_n];
CC         IsoId=Isoform_identifier_1[, Isoform_identifier_n]; Sequence=VSP_identifier_1 [, VSP_identifier_n];
CC         Note=Free text;
CC       Event=Alternative initiation;
CC         Comment=Free text;
             */
        } else {
            // all others are just free text.
            this.setText(c);
        }
        
        // all done
    }
    
    /**
     * Returns true if the comment may be parseable (starts with -!-).
     * @param c the comment to check.
     * @return true if it starts with -!-, false otherwise.
     */
    public static boolean isParseable(Comment c) {
        return isParseable(c.getComment());
    }
    
    /**
     * Returns true if the comment may be parseable (starts with -!-).
     * @param c the comment to check.
     * @return true if it starts with -!-, false otherwise.
     */
    public static boolean isParseable(String c) {
        return c.trim().startsWith(PREFIX);
    }
    
    /**
     * Generates a comment string based on the current values of the
     * internal fields.
     * @return the comment string representing the current settings.
     * @throws ParseException if the current settings do not allow the
     * creation of a correct comment string.
     */
    public String getCommentText() throws ParseException {
        StringBuffer sb = new StringBuffer();
        sb.append(PREFIX);
        sb.append(" ");
        sb.append(this.getCommentType());
        sb.append(": ");
        
        // output the specifics
        if (this.getCommentType().equals(BIOPHYSICOCHEMICAL_PROPERTIES)) {
            /*
CC   -!- BIOPHYSICOCHEMICAL PROPERTIES:
CC       Absorption:
CC         Abs(max)=xx nm;
CC         Note=free_text;
CC       Kinetic parameters:
CC         KM=xx unit for substrate [(free_text)];
CC         Vmax=xx unit enzyme [free_text];
CC         Note=free_text;
CC       pH dependence:
CC         free_text;
CC       Redox potential:
CC         free_text;
CC       Temperature dependence:
CC         free_text;
             */
        } else if (this.getCommentType().equals(DATABASE)) {
            if (this.getDatabaseName()==null) throw new ParseException("Database name is missing");
            /*
CC   -!- DATABASE: NAME=Text[; NOTE=Text][; WWW="Address"][; FTP="Address"].
             */
            sb.append("NAME=");
            sb.append(this.getDatabaseName());
            if (this.getNote()!=null) {
                sb.append("; NOTE=");
                sb.append(this.getNote());
            }
            if (this.getUri()!=null) {
                sb.append("; ");
                if (this.getUri().startsWith("ftp")) sb.append(" FTP=");
                else sb.append(" WWW=");
                sb.append(this.getUri());
            }
            sb.append(".");
        } else if (this.getCommentType().equals(MASS_SPECTROMETRY)) {
            /*
CC   -!- MASS SPECTROMETRY: MW=XXX[; MW_ERR=XX]; METHOD=XX; RANGE=XX-XX[ (Name)]; NOTE={Free text (Ref.n)|Ref.n}.
             */
            sb.append("MW=");
            sb.append(""+this.getMolecularWeight());
            if (this.getMolWeightError()!=null) {
                sb.append("; MW_ERR=");
                sb.append(""+this.getMolWeightError());
            }
            sb.append("; METHOD=");
            sb.append(this.getMolWeightMethod());
            sb.append("; RANGE=");
            sb.append(""+this.getMolWeightRangeStart());
            sb.append("-");
            sb.append(""+this.getMolWeightRangeEnd());
            sb.append("; NOTE=");
            sb.append(this.getNote());
            sb.append(".");
        } else if (this.getCommentType().equals(INTERACTION)) {
            /*
CC   -!- INTERACTION:
CC       {{SP_Ac:identifier[ (xeno)]}|Self}; NbExp=n; IntAct=IntAct_Protein_Ac, IntAct_Protein_Ac;
             */
        } else if (this.getCommentType().equals(ALTERNATIVE_PRODUCTS)) {
            /*
CC   -!- ALTERNATIVE PRODUCTS:
CC       Event=Alternative promoter;
CC         Comment=Free text;
CC       Event=Alternative splicing; Named isoforms=n;
CC         Comment=Optional free text;
CC       Name=Isoform_1; Synonyms=Synonym_1[, Synonym_n];
CC         IsoId=Isoform_identifier_1[, Isoform_identifier_n]; Sequence=Displayed;
CC         Note=Free text;
CC       Name=Isoform_n; Synonyms=Synonym_1[, Synonym_n];
CC         IsoId=Isoform_identifier_1[, Isoform_identifier_n]; Sequence=VSP_identifier_1 [, VSP_identifier_n];
CC         Note=Free text;
CC       Event=Alternative initiation;
CC         Comment=Free text;
             */
        } else {
            // just append free text for all others.
            sb.append(this.getText());
        }
        
        // return it
        return sb.toString();
    }
    
    /**
     * Holds value of property commentType.
     */
    private String commentType;
    
    /**
     * Getter for property commentType.
     * @return Value of property commentType.
     */
    public String getCommentType() {
        
        return this.commentType;
    }
    
    /**
     * Setter for property commentType.
     * @param commentType New value of property commentType.
     */
    public void setCommentType(String commentType) {
        
        this.commentType = commentType;
    }
    
    /**
     * Holds value of property text.
     */
    private String text;
    
    /**
     * Getter for property text.
     * @return Value of property text.
     */
    public String getText() {
        
        return this.text;
    }
    
    /**
     * Setter for property text.
     * @param text New value of property text.
     */
    public void setText(String text) {
        
        this.text = text;
    }
    
    /**
     * Holds value of property databaseName.
     */
    private String databaseName;
    
    /**
     * Getter for property databaseName.
     * @return Value of property databaseName.
     */
    public String getDatabaseName() {
        
        return this.databaseName;
    }
    
    /**
     * Setter for property databaseName.
     * @param databaseName New value of property databaseName.
     */
    public void setDatabaseName(String databaseName) {
        
        this.databaseName = databaseName;
    }
    
    /**
     * Holds value of property note.
     */
    private String note;
    
    /**
     * Getter for property note.
     * @return Value of property note.
     */
    public String getNote() {
        
        return this.note;
    }
    
    /**
     * Setter for property note.
     * @param note New value of property note.
     */
    public void setNote(String note) {
        
        this.note = note;
    }
    
    /**
     * Holds value of property uri.
     */
    private String uri;
    
    /**
     * Getter for property uri.
     * @return Value of property uri.
     */
    public String getUri() {
        
        return this.uri;
    }
    
    /**
     * Setter for property uri.
     * @param uri New value of property uri.
     */
    public void setUri(String uri) {
        
        this.uri = uri;
    }
    
    /**
     * Holds value of property molecularWeight.
     */
    private int molecularWeight;
    
    /**
     * Getter for property molecularWeight.
     * @return Value of property molecularWeight.
     */
    public int getMolecularWeight() {
        
        return this.molecularWeight;
    }
    
    /**
     * Setter for property molecularWeight.
     * @param molecularWeight New value of property molecularWeight.
     */
    public void setMolecularWeight(int molecularWeight) {
        
        this.molecularWeight = molecularWeight;
    }
    
    /**
     * Holds value of property molWeightError.
     */
    private Integer molWeightError;
    
    /**
     * Getter for property molWeightError.
     * @return Value of property molWeightError.
     */
    public Integer getMolWeightError() {
        
        return this.molWeightError;
    }
    
    /**
     * Setter for property molWeightError.
     * @param molWeightError New value of property molWeightError.
     */
    public void setMolWeightError(Integer molWeightError) {
        
        this.molWeightError = molWeightError;
    }
    
    /**
     * Holds value of property molWeightRangeStart.
     */
    private int molWeightRangeStart;
    
    /**
     * Getter for property molWeightRangeStart.
     * @return Value of property molWeightRangeStart.
     */
    public int getMolWeightRangeStart() {
        
        return this.molWeightRangeStart;
    }
    
    /**
     * Setter for property molWeightRangeStart.
     * @param molWeightRangeStart New value of property molWeightRangeStart.
     */
    public void setMolWeightRangeStart(int molWeightRangeStart) {
        
        this.molWeightRangeStart = molWeightRangeStart;
    }
    
    /**
     * Holds value of property molWeightRangeEnd.
     */
    private int molWeightRangeEnd;
    
    /**
     * Getter for property molWeightRangeEnd.
     * @return Value of property molWeightRangeEnd.
     */
    public int getMolWeightRangeEnd() {
        
        return this.molWeightRangeEnd;
    }
    
    /**
     * Setter for property molWeightRangeEnd.
     * @param molWeightRangeEnd New value of property molWeightRangeEnd.
     */
    public void setMolWeightRangeEnd(int molWeightRangeEnd) {
        
        this.molWeightRangeEnd = molWeightRangeEnd;
    }
    
    /**
     * Holds value of property molWeightMethod.
     */
    private String molWeightMethod;
    
    /**
     * Getter for property molWeightMethod.
     * @return Value of property molWeightMethod.
     */
    public String getMolWeightMethod() {
        
        return this.molWeightMethod;
    }
    
    /**
     * Setter for property molWeightMethod.
     * @param molWeightMethod New value of property molWeightMethod.
     */
    public void setMolWeightMethod(String molWeightMethod) {
        
        this.molWeightMethod = molWeightMethod;
    }
}
