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
import org.biojava.bio.seq.ProteinTools;

/**
 * This class contains functions accessing sequences in swiss-prot.
 *
 * @author Lei Lai
 * @author Matthew Pocock
 */
public class SwissprotSequenceDB
{
  private static SequenceFormat format;
  private static String DBName="swiss-prot";
  private boolean IOExceptionFound=false;
  
  static 
  {
    SequenceFormat format = new EmblLikeFormat();
  }
  
  protected SequenceFormat getSequenceFormat() 
  {
    return format;
  }
  
  protected Alphabet getAlphabet() 
  {
    return ProteinTools.getTAlphabet();
  }

  protected URL getAddress (String id) throws MalformedURLException
  {
	String defaultReturnFormat="";
	FetchURL seqURL = new FetchURL(DBName, defaultReturnFormat);
	String baseurl = seqURL.getbaseURL();
	
	String url = baseurl+id;
	
    return new URL (url);
  }
 
  public String getName() 
  {
    return DBName;
  }
  
  public Sequence getSequence(String id) throws BioException 
  {
    try 
	{
	  IOExceptionFound=false;
      URL queryURL = getAddress(id);//achieve URL based on ID      
    //  System.err.println("query is "+ queryURL.toString());
      SequenceFormat sFormat = getSequenceFormat();//get incoming sequence format
      SequenceBuilder sbuilder = new SimpleSequenceBuilder();//create a sequence builder
	  SequenceBuilderFactory sFact=new SwissprotProcessor.Factory(SimpleSequenceBuilder.FACTORY);
      Alphabet alpha = getAlphabet();//get alphabet
      SymbolTokenization rParser = alpha.getTokenization("token");//get SymbolTokenization
      System.err.println("got data from "+ queryURL);
	  DataInputStream in=new DataInputStream(queryURL.openStream());
	  BufferedReader reader = new BufferedReader (new InputStreamReader (in));
	  SequenceIterator seqI= SeqIOTools.readSwissprot(reader);
      return seqI.nextSequence();
    } 
	catch ( Exception e )
	{
	  System.out.println (e.toString());
	  IOExceptionFound=true;
	  return null;
    } 
  }
  
  public boolean checkIOException()
  {
	return IOExceptionFound;
  }
}
