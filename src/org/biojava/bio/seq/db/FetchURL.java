/**

 * This class stores base URL and some cgi arguments for 

 * accessing web-based sequences in NCBI and Swiss-prot, pubmed articles and locuslinks.

 */



package org.biojava.bio.seq.db;



import java.net.*;



public class FetchURL
{
	String baseURL;
	String db;//database name
	String rettype;//return type
	String retmode;//return mode 
	
	/**

	 * Constructs a fetchURL object based on the database name 

	 * and specified return format of sequence.

	 */
	public FetchURL(String databaseName, String format) 
	{	
		if (databaseName.trim().equalsIgnoreCase("genbank")
			||databaseName.trim().equalsIgnoreCase("nucleotide"))
		{
			db = "nucleotide";	
		//	rettype = format;
		//	retmode = format;
		}
		if (databaseName.trim().equalsIgnoreCase("genpept")
			||databaseName.trim().equalsIgnoreCase("protein"))
		{
			db = "protein";
			rettype = format;
			retmode = format;
		}
		if (databaseName.trim().equalsIgnoreCase("swiss-prot"))
		{
			db="swiss-prot";
		}
		if (databaseName.trim().equalsIgnoreCase("pubmed"))
		{
			db="pubmed";
			rettype = "abstract";
			retmode = format;
		}
		if (databaseName.trim().equalsIgnoreCase("locuslink"))
		{
			db="locuslink";
		}
	}
	
	public String getbaseURL()
	{
		if (db.equalsIgnoreCase("Genbank")||db.equalsIgnoreCase("nucleotide")
			||db.equalsIgnoreCase("Genpept")||db.equalsIgnoreCase("protein")
			||db.equalsIgnoreCase("pubmed"))
			baseURL="http://www.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?";
		else if (db.equalsIgnoreCase("Swiss-prot"))
			baseURL="http://us.expasy.org/cgi-bin/get-sprot-raw.pl?";
		else if (db.equalsIgnoreCase("LocusLink"))
			baseURL="http://www.ncbi.nlm.nih.gov/LocusLink/LocRpt.cgi?";
		
		return baseURL;	
	}
	
	//get the database name
	public String getDB()
	{
		return ("db="+db);	
	}
	
	//get the return format and type
	public String getReturnFormat()
	{
		return ("rettype="+rettype+"&retmode="+retmode);	
	}
}
