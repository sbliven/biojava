package org.biojava.bio.seq.db;

import java.net.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;

public class NCBISequenceDB
extends WebSequenceDB {
  private String server;
  private String CGI;
  private SequenceFormat format;
  private String dataBase;
  private Alphabet alpha;
  private String formatName;

  public static final String DB_NUCLEOTIDE = "nucleotide";
  public static final String DB_PROTEIN = "protein";


  /**
   * Default constructor, querys the Genbank nucleotide database on "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi"
   * and retrieves sequences in FastaFormat.
   */
  public NCBISequenceDB(){
    this("http://www.ncbi.nlm.nih.gov/","entrez/query.fcgi",DB_NUCLEOTIDE,new FastaFormat());
  }

  /**
   * Parameterized constructor
   * @param database must be one of "nucleotide" or "protein" (use the static DB fields)
   * @param format must be one of <code>GenbankFormat</code> or <code>FastaFormat</code>
   * @throws BioRuntimeException if the database or format is invalid
   */
  public NCBISequenceDB(String database, SequenceFormat format){
    this("http://www.ncbi.nlm.nih.gov/","entrez/query.fcgi",database,format);
  }

  /**
   * Parameterized constructor
   * @param server eg "http://www.ncbi.nlm.nih.gov/"
   * @param CGI eg "entrez/query.fcgi"
   * @param database must be one of "nucleotide" or "protein" (use the static DB fields)
   * @param format must be one of <code>GenbankFormat</code> or <code>FastaFormat</code>
   * @throws BioRuntimeException if the database or format is invalid
   */
  public NCBISequenceDB(String server, String CGI, String database, SequenceFormat format)
        throws BioRuntimeException{

    this.server = server;
    this.CGI = CGI;
    try {
      setDatabase(database);
    }
    catch (BioException ex) {
      throw new BioRuntimeException(
          "Database format must be one of {nucleotide, protein}");
    }
    try {
      setSequenceFormat(format);
    }
    catch (BioException ex) {
      throw new BioRuntimeException(
          "SequenceFormat object must be one of {FastaFormat, GenbankFormat}");
    }

  }


  public String getDataBase(){
    return dataBase;
  }

  /**
   *
   * @param dataBase must be one of "nucleotide" or "protein" (use the static DB fields)
   * @throws BioException if an unknown database name is used.
   */
  public void setDatabase(String dataBase) throws BioException{
    if (dataBase == DB_NUCLEOTIDE) {

      this.dataBase = DB_NUCLEOTIDE;
      this.alpha = DNATools.getDNA();

    }
    else if (dataBase == DB_PROTEIN) {

      this.dataBase = DB_PROTEIN;
      this.alpha = ProteinTools.getAlphabet();

    }
    else {

      throw new BioException(
          "Database format must be one of {nucleotide, protein}");

    }

  }

  public SequenceFormat getSequenceFormat() {
    return format;
  }

  /**
   *
   * @param format must be one of <code>FastaFormat</code> or <code>GenbankFormat</code>
   * @throws BioException if an unknown <code>SequenceFormat</code> is used
   */
  public void setSequenceFormat(SequenceFormat format) throws BioException{

    if (format instanceof FastaFormat ) {
      this.format = format;
      this.formatName = "FASTA";
    }
    else if(format instanceof GenbankFormat){
      this.format = format;
      if (alpha == DNATools.getDNA()) {
        this.formatName = "GenBank";
      }
      else {
        this.formatName = "GenPept";
      }

    }
    else {
      throw new BioException("Only Genbank and FASTA formats currently supported");
    }
  }

  protected Alphabet getAlphabet() {
    return alpha;
  }

  protected URL getAddress(String uid)
  throws MalformedURLException {
    String query = "cmd=text&db="+dataBase+"&uid="+uid+"&dopt="+formatName;

    return new URL(server + CGI + "?" + query);
  }

  public String getName() {
    return "NCBI-Genbank";
  }
}
