/*
 * put your module comment here
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.biojava.bio.seq.io;

import  org.biojava.bio.seq.io.AlignmentFormat;
import  org.biojava.bio.symbol.Alignment;
import  org.biojava.bio.symbol.*;
import  java.io.BufferedReader;
import  org.apache.regexp.*;
import  org.biojava.bio.seq.*;
import  org.biojava.bio.seq.io.*;
import  java.lang.Integer;
import  java.io.*;
import  java.util.*;


/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:
 * @version 1.0
 * @author Guoneng Zhong <travelgz@yahoo.com>
 */
public class MSFAlignmentFormat
        implements AlignmentFormat {
    private static final boolean DEBUGPRINT = false;

    /**
     * put your documentation comment here
     */
    public MSFAlignmentFormat () {
    }

    /**
     * used to quick test the code
     * @param args
     */
    public static void main (String[] args) {
        String filename;
        if (args.length < 1) {
            filename = "SimpleMSF.msf";         //change to your favorite
        }
        else {
            filename = args[0];
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            MSFAlignmentFormat MSFAlignmentFormat1 = new MSFAlignmentFormat();
            MSFAlignmentFormat1.read(reader);
        } catch (Exception E) {}
    }

    /**
     * put your documentation comment here
     * @param reader
     * @return Alignment
     */
    public Alignment read (BufferedReader reader) {
        Vector sequenceNames = new Vector();
        String sequenceName = null;
        String sequenceData[] = null;
        int startOfData = 0;                    //the start of the sequence data in the line
        int currSeqCount = 0;                   //which sequence data you are currently trying to get
        try {
            RE mtc = new RE("Name:\\s+(.*)\\s+Len:");
            RE removewhitespace = new RE("\\s");
            // REMatch rem = null;
            String line = reader.readLine();
            //parse past header
            while (line.indexOf("..") == -1) {
                line = reader.readLine();
            }
            line = reader.readLine();           //read blank
            //read each name (between Name:   and Len:
            line = reader.readLine();
            while ((line.indexOf("//") == -1) && ((line.trim()).length() !=
                    0)) {
                mtc.match(line);
                sequenceName = line.substring(mtc.getParenStart(1), mtc.getParenEnd(1));
                if (sequenceName == null) {
                    break;
                }               //end of sequence names
                //sequenceName = line.substring(rem.getSubStartIndex(1),
                //                              rem.getSubEndIndex(1));
                if ((line.trim()).length() == 0) {
                    break;
                }
                sequenceNames.add(sequenceName);
                line = reader.readLine();
                //System.out.println( sequenceName);
            }
            sequenceData = new String[sequenceNames.size()];
            for (int it = 0; it < sequenceNames.size(); it++) {
                sequenceData[it] = new String();
            }
            //until you get a line that matches the first sequence
            while ((line.indexOf((String)sequenceNames.get(0)) == -1))          // || (   (line.trim()) .length()>0  )    )
            {
                line = reader.readLine();
            }
            //now you on the first line of the sequence data
            while (line != null) {
                for (currSeqCount = 0; currSeqCount < sequenceNames.size(); currSeqCount++) {
                    if (line.indexOf((String)sequenceNames.get(currSeqCount))
                            == -1) {
                        break;
                    }           //error
                    startOfData = line.indexOf((String)sequenceNames.get(currSeqCount))
                            + ((String)sequenceNames.get(currSeqCount)).length();
                    line = (line.substring(startOfData));
                    line = removewhitespace.subst(line, "", RE.REPLACE_ALL);
                    sequenceData[currSeqCount] = sequenceData[currSeqCount].concat(line);
                    line = reader.readLine();
                    if ((line.trim()).length() == 0) {
                        break;
                    }           //could be an error
                }
                //until you get a line that matches the first sequence
                while ((line != null) && (line.indexOf((String)sequenceNames.get(0))
                        == -1))                 // || (   (line.trim()) .length()>0  )    )
                {
                    line = reader.readLine();
                }
            }
            //print them out for testing
            if (DEBUGPRINT) {
                for (currSeqCount = 0; currSeqCount < sequenceNames.size(); currSeqCount++) {
                    System.out.println((String)sequenceNames.get(currSeqCount)
                            + ":" + sequenceData[currSeqCount]);
                }
            }
            //check DNA, RNA or Prot
            StringBuffer testString = new StringBuffer();
            int agct = 0;
            for (currSeqCount = 0; currSeqCount < sequenceNames.size(); currSeqCount++) {
                String seqLine = sequenceData[currSeqCount];
                RE removeDots = new RE("(\\.|\\-|\\*)");
                seqLine = removeDots.subst(seqLine,"",RE.REPLACE_ALL);
                testString.append(seqLine.toUpperCase());
            }
            agct += count(testString,new char[]{'A','C','T','U','G'});
            //now parse through them and create gapped symbol lists
            HashMap sequenceDataMap = new HashMap();
            Symbol sym = null;
            FiniteAlphabet alph = null;
            double ratio = ((double)agct)/((double)testString.length());
            if (ratio > 0.90) {            //if DNA alph
                if (count(testString,'U') > count(testString,'C')) {                     //rna alph
                    //get the rna alph
                    alph = RNATools.getRNA();
                }
                else {          //get DNA alph
                    alph = DNATools.getDNA();
                }
            }
            else {
                alph = ProteinTools.getTAlphabet();
            }
            SymbolTokenization parse = alph.getTokenization("token");
            for (currSeqCount = 0; currSeqCount < sequenceNames.size(); currSeqCount++) {
                String sd = null;
                //change stop codons to specified symbols
                sd = sequenceData[currSeqCount].replace('~', '-');              //sometimes this is a term signal not a gap
                sd = sequenceData[currSeqCount].replace('.', '-');              //sometimes this is a term signal not a gap
                StringBuffer sb = new StringBuffer();
                SymbolList sl = null;
                sequenceDataMap.put((String)sequenceNames.get(currSeqCount),
                        new SimpleSymbolList(parse, sd));
            }
            return  (new SimpleAlignment(sequenceDataMap));
        } catch (Exception e) {
            System.err.println("MSFFormatReader " + e.getMessage());
            //  throw (e);
        }
        return  (null);
    }           //end read it

    /**
     * @author guoneng <travelgz@yahoo.com>
     * returns the number of times given character appears
     * @param line the line whose characters you are comparing against
     * @param ch character used for the search
     * @return int number of times ch appears
     */
    private static int count(StringBuffer line,char ch){
        return count(line,new char[]{ch});
    }

    /**
     * @author guoneng <travelgz@yahoo.com>
     * returns the number of times the given set of characters appear in the line
     * @param line the line whose characters you are comparing against
     * @param ch array of characters used for the search
     * @return int number of times ch appears
     */
    private static int count(StringBuffer line,char[] ch){
        if(ch.length==0) return 0;

        int count = 0;
        for(int i=0;i<line.length();i++){
            // is the character at i in the given char array?
            for(int j=0;j<ch.length;j++){
                if(line.charAt(i)==ch[j]){
                    count++;
                    break;
                }
            }
        }
        return count;
    }
}               //end class



