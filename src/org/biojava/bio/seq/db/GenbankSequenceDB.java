package org.biojava.bio.seq.db;

import java.net.*;
import java.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.BioError;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.SequenceIterator;

/**
 * This class contains functions accessing DNA sequences in Genbank format.
 */
public class GenbankSequenceDB
{
  private static SequenceFormat format;//return format of the sequence
  private static String DBName="Genbank";//predefined the database name -- Genbank
  private boolean IOExceptionFound=false;//check if IOException is found
  private boolean ExceptionFound=false;//check if any exception is found
  
  static 
  {
    SequenceFormat format = new GenbankFormat();
  }
  
  protected SequenceFormat getSequenceFormat() 
  {
    return format;
  }
  
  protected Alphabet getAlphabet() 
  {
    return DNATools.getDNA();
  }
  
  /**
   * Get the URL object for locating sequence object using eutils.
   * The default value of the return format of the sequence object is text.
   **/
  protected URL getAddress (String id) throws MalformedURLException
  {
	String defaultReturnFormat="text";
	FetchURL seqURL = new FetchURL(DBName, defaultReturnFormat);
	String baseurl = seqURL.getbaseURL();
	String db = seqURL.getDB();
	//String returnFormat = seqURL.getReturnFormat();
	
	String url = baseurl+db+"&id="+id;
	
    return new URL (url);
  }
  
  /**
   * Get the URL object for locating sequence object using eutils.
   * User could specify the return format of the sequence object.
   */
  protected URL getAddress(String id, String format) throws MalformedURLException
  {   
	FetchURL seqURL = new FetchURL(DBName, format);
	String baseurl = seqURL.getbaseURL();
	if (!(baseurl.equalsIgnoreCase("")))
		baseurl = seqURL.getbaseURL();
	String db = seqURL.getDB();
//	String returnFormat = seqURL.getReturnFormat();
//	String url = baseurl+db+"&"+returnFormat+"&id="+id;
	String url = baseurl+db+"&id="+id;
    return new URL (url);
  }
  
  public String getName() 
  {
    return DBName;
  }
  
  public Sequence getSequence(String id) throws Exception 
  {
    try 
	{
	  IOExceptionFound=false;
	  ExceptionFound=false;
      URL queryURL = getAddress(id);//get URL based on ID      
    //  System.err.println("query is "+ queryURL.toString());
      SequenceFormat sFormat = getSequenceFormat();//get incoming sequence format
      SequenceBuilder sbuilder = new SimpleSequenceBuilder();//create a sequence builder
	  SequenceBuilderFactory sFact=new GenbankProcessor.Factory(SimpleSequenceBuilder.FACTORY);
      Alphabet alpha = getAlphabet();//get alphabet
      SymbolTokenization rParser = alpha.getTokenization("token");//get SymbolTokenization
      System.err.println("got data from "+ queryURL);
	  DataInputStream in=new DataInputStream(queryURL.openStream());
	  BufferedReader reader = new BufferedReader (new InputStreamReader (in));
	  SequenceIterator seqI= SeqIOTools.readGenbank(reader);
      return seqI.nextSequence();
    } 
	catch ( Exception e )
	{
	  System.out.println ("Exception found in GenbankSequenceDB -- getSequence");
      System.out.println (e.toString());
	  ExceptionFound=true;
	  IOExceptionFound=true;
	  return null;
    } 
  }
  
  public boolean checkIOException()
  {
	return IOExceptionFound;  
  }
  
  public boolean checkException()
  {
	return ExceptionFound;  
  }
}
