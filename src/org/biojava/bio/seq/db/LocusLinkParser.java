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
/**This locus link parser has been temporary placed in the package org.biojava.bio.seq.db
 * It might not be the right place. 
 * This is a html parser.
 * It parses the locus link page on NCBI using open source code from kizna.com.
 */

package org.biojava.bio.seq.db;

import com.kizna.html.HTMLNode;
import com.kizna.html.HTMLRemarkNode;
import com.kizna.html.HTMLStringNode;
import com.kizna.html.scanners.HTMLLinkScanner;
import com.kizna.html.tags.*;
import java.util.*;
import java.io.*;

import com.kizna.html.*;

/**
 * @author Lei Lai
 * @author Matthew Pocock
 */
public class LocusLinkParser
{
	private String nucleotideIDs = "";//take all nucleotide ids into one string
	private String proteinIDs = "";//take all protein ids into one string
	private String omimIDs = ""; //take all omimIDs into one string
	private String unigeneIDs = "";//take all UniGeneIDs into one string
	private boolean formatChange = false;//check if the format of target page changes
	private String formatChangeHappened 
		= "The file format has changed or exception has been thrown.";
	private String[] formatChangeHapp = new String[1];	

	//constructor
	public LocusLinkParser (String locusLinkID)
	{
		nucleotideIDs="";
		proteinIDs="";
		omimIDs="";
		unigeneIDs = "";
		formatChange=false;
		parse (locusLinkID);
	}
	
	//All protein ids will be returned as a single string.
	public String getProteinIDs()
	{
		if (formatChange)
			return formatChangeHappened;
		else 
			return proteinIDs;
	}
	
	//All nucleotide ids will be returned as a single string.
	public String getNucleotideIDs()
	{
		if (formatChange)
			return formatChangeHappened;
		else 
			return nucleotideIDs;	
	}
	
	//All omim ids will be returned as a single string.
	public String getOmimIDs()
	{
		if (formatChange)
			return formatChangeHappened;
		else 
			return omimIDs;
	}
	
	public String getUniGeneIDs()
	{
		if (formatChange)
			return formatChangeHappened;
		else
			return unigeneIDs;
	}
	
	//The protein ids will be returned as an array of strings.
	public String[] getProteinIDs_array()
	{
		StringTokenizer st = new StringTokenizer (proteinIDs, "\t");
		int numTokens = st.countTokens();
		
		if (numTokens==0)
		{
			formatChangeHapp[0]
				="The file format has changed or exception has been thrown.";
			return formatChangeHapp;
		}		
		else 
		{
			String[] proteinIDs_array = new String[numTokens];
			for (int i=0; i<numTokens; i++)
			{
				proteinIDs_array[i]=st.nextToken();
			}
			return proteinIDs_array;
		}
	}
	//The nucleotide ids will be returned as an array of strings.
	public String[] getNucleotideIDs_array()
	{
		StringTokenizer st = new StringTokenizer (nucleotideIDs, "\t");
		int numTokens = st.countTokens();
		if (numTokens==0)
		{
			formatChangeHapp[0]
				="The file format has changed or exception has been thrown.";
			return formatChangeHapp;
		}		
		else 
		{
			String[] nucleotideIDs_array = new String[numTokens];
			for (int i=0; i<numTokens; i++)
			{
				nucleotideIDs_array[i]=st.nextToken();
			}
			return nucleotideIDs_array;
		}
	}
	//The omim ids will be returned as an array of strings.
	public String[] getOmimIDs_array()
	{
		StringTokenizer st = new StringTokenizer (omimIDs, "\t");
		int numTokens = st.countTokens();
		if (numTokens==0)
		{
			formatChangeHapp[0]
				="The file format has changed or exception has been thrown.";
			return formatChangeHapp;
		}		
		else 
		{
			String[] omimIDs_array = new String[numTokens];
			for (int i=0; i<numTokens; i++)
			{
				omimIDs_array[i]=st.nextToken();
			}
			return omimIDs_array;
		}
	}
	
	
	/**This is the actual parser. It will check if the format of the html changed first.
	 * The target html file (locus link) is arranged by a sets of tables. 
	 * The main idea behind this parser is to parse each table individually.
	 * At this stage, we only retrieve html nodes from three sets of tables,
	 * which are NCBI Reference Sequence (RefSeq), GenBank Sequences and Additional Links.
	 **/
	private void parse(String locusLinkID)
	{
		FetchURL llURL = new FetchURL("LocusLink", "");
		
		String baseurl = llURL.getbaseURL();
		if (!(baseurl.equalsIgnoreCase("")))
			baseurl = llURL.getbaseURL();
		String url = baseurl+"l="+locusLinkID;		
		System.out.println (url);
	
		HTMLParser parser=null;
	
		parser = new HTMLParser(url);
		/*
		while (parser.checkIOException())
		{
			System.out.println ("............found IOException.........request again.........");
			parser = new HTMLParser(url);
			System.out.println (url);
		}
		*/
		//Put all string nodes in the string allStringNodes
		String allStringNodes = "";
		//Put all remark nodes in the string allRemarkNodes (comments)
		String allRemarkNodes = "";
		
		for (Enumeration e = parser.elements();e.hasMoreElements();) 
		{
			HTMLNode node = (HTMLNode)e.nextElement();
			if (node instanceof HTMLStringNode) 
			{
				HTMLStringNode stringNode = (HTMLStringNode)node;
				allStringNodes = allStringNodes + stringNode.getText().trim()+"\t";
			}
			if (node instanceof HTMLRemarkNode)
			{
				HTMLRemarkNode remarkNode = (HTMLRemarkNode)node;
				allRemarkNodes = allRemarkNodes + remarkNode.getText().trim()+"\t";
			}
		}
		
		StringTokenizer st = new StringTokenizer (allStringNodes, "\t");
		StringTokenizer refSeq = new StringTokenizer (allStringNodes, "\t");
		StringTokenizer addLinks = new StringTokenizer (allStringNodes, "\t");
		StringTokenizer formatCheck = new StringTokenizer (allRemarkNodes, "\t");
		
		int numTokens = st.countTokens();
		int numRemarkNodes = formatCheck.countTokens();
		
		//check if the format of the html file has been changed.
		int flag=0;
		for (int i=0; i<numRemarkNodes; i++) 
		{
			if (formatCheck.nextToken().trim().equalsIgnoreCase("RefSeq Block")) 
			{
				flag=flag+1;
				String tokenDeli = formatCheck.nextToken();
				while (!tokenDeli.trim().equalsIgnoreCase("GenBank Block"))
				{
					tokenDeli = formatCheck.nextToken();
					if (tokenDeli.trim().equalsIgnoreCase("To Top"))
						break;
				}
				if (tokenDeli.trim().equalsIgnoreCase("GenBank Block"))
					flag=flag+1;
				String tokenDeli_2 = formatCheck.nextToken();
				while (!tokenDeli_2.trim().equalsIgnoreCase("Additional Links Block"))
				{
					tokenDeli_2 = formatCheck.nextToken();
					if (tokenDeli_2.trim().equalsIgnoreCase("To Top"))
						break;
				}
				if (tokenDeli_2.trim().equalsIgnoreCase("Additional Links Block"))
					flag = flag+1;
				break;
			} 
		}
		if (flag!=3)
			formatChange = true;
		else 
			formatChange = false;
		
		//get genbank sequences section
		for (int i=0; i<numTokens; i++)
		{
			if (st.nextToken().trim().equalsIgnoreCase("GenBank Sequences"))
			{
				for (int j=0; j<6; j++)
				{
					st.nextToken();	
				}
				//tell whether the next token is nucleotide id or the next section 
				//("Additonal Links" or "To Top")
				String tokenDeli=st.nextToken();
				while (!tokenDeli.trim().equalsIgnoreCase("Additional Links")
						 &&!tokenDeli.trim().equalsIgnoreCase("To Top"))
				{
					if (!tokenDeli.trim().equalsIgnoreCase("&nbsp;"))
						nucleotideIDs = nucleotideIDs + "Genbank: "+tokenDeli+"\t";
					st.nextToken();
					String next=st.nextToken();
					if (!next.trim().equalsIgnoreCase("&nbsp;"))
						proteinIDs = proteinIDs + "Genpept: "+next+"\t";
					st.nextToken(); 
					st.nextToken();
					tokenDeli = st.nextToken();
				}
				break;
			}
		}
		
		//get NCBI Reference Sequences section
		for (int i=0; i<numTokens; i++)
		{
			if (refSeq.nextToken().trim().equalsIgnoreCase
				("NCBI Reference Sequences&nbsp;(RefSeq)"))
			{
				String tokenDeli = refSeq.nextToken();
				while (!tokenDeli.trim().equalsIgnoreCase("GenBank Sequences")
					   &&!tokenDeli.trim().equalsIgnoreCase("Additional Links")
					   &&!tokenDeli.trim().equalsIgnoreCase("To Top"))
				{
					String next = refSeq.nextToken();
					if (next.trim().equalsIgnoreCase("mRNA:"))
						nucleotideIDs = nucleotideIDs + "RefSeq: "+refSeq.nextToken()+"\t";
					if (next.trim().equalsIgnoreCase("Protein:"))
						proteinIDs = proteinIDs + "RefSeq: "+refSeq.nextToken()+"\t";
					if (next.trim().equalsIgnoreCase("GenBank Source:"))
						nucleotideIDs = nucleotideIDs + "Genbank: "+refSeq.nextToken()+"\t";
					if (next.trim().equalsIgnoreCase("Model mRNA:"))
						nucleotideIDs = nucleotideIDs + "RefSeq: "+refSeq.nextToken()+"\t";
					if (next.trim().equalsIgnoreCase("Model Protein:"))
						proteinIDs = proteinIDs + "RefSeq: "+refSeq.nextToken()+"\t";
					tokenDeli=next;
				}
				break;
			}
		}
		
		//get additional links section
		for (int i=0; i<numTokens; i++)
		{
			if (addLinks.nextToken().trim().equalsIgnoreCase("Additional Links"))
			{
				String tokenDeli = addLinks.nextToken();
				while (!tokenDeli.trim().equalsIgnoreCase("To Top"))
				{
					String next = addLinks.nextToken();
					if (next.trim().equalsIgnoreCase("UniGene:"))
					{
						unigeneIDs = unigeneIDs + addLinks.nextToken()+"\t";
					}
					if (next.trim().equalsIgnoreCase("Omim:"))
						omimIDs = omimIDs + addLinks.nextToken()+"\t";
					tokenDeli=next;
				}
				break;
			}
		}
	}
}