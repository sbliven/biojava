/*
 * put your module comment here
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */

package org.biojava.bio.seq.io;

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
 * @author
 * @version 1.0
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
     * put your documentation comment here
     * @param args
     */
    public static void main (String[] args) {
        String filename;
        if (args.length < 1) {
            filename = "D:\\SimpleMSF.msf";
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
     * @return
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
                sequenceName =
line.substring(mtc.getParenStart(1),mtc.getParenEnd(1));
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
                    line = removewhitespace.subst(line, "",RE.REPLACE_ALL);
                    sequenceData[currSeqCount] = sequenceData[currSeqCount].concat(line);
                    line = reader.readLine();
                    if ((line.trim()).length() == 0) {
                        break;
                    }           //could be an error
                }
                //until you get a line that matches the first sequence
                while ((line != null)
                        && (line.indexOf((String)sequenceNames.get(0))
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
                testString.append(sequenceData[currSeqCount]);
            }
            StringTokenizer st = null;
            st = new StringTokenizer(testString.toString().toLowerCase(), "a");
            agct += st.countTokens();
            st = new StringTokenizer(testString.toString().toLowerCase(), "g");
            agct += st.countTokens();
            st = new StringTokenizer(testString.toString().toLowerCase(), "c");
            agct += st.countTokens();
            st = new StringTokenizer(testString.toString().toLowerCase(), "t");
            agct += st.countTokens();
            st = new StringTokenizer(testString.toString().toLowerCase(), "u");
            agct += st.countTokens();
            //now parse through them and create gapped symbol lists
            HashMap sequenceDataMap = new HashMap();
            Symbol sym = null;
            FiniteAlphabet alph = null;
            if ((agct/testString.length()) > 0.90) {            //if DNA alph
                if (st.countTokens() > 0) {                     //rna alph
                    //get the rna alph
                    alph = DNATools.getDNA();
                }
                else {          //get DNA alph
                    //   Symbol ns=new FundamentalAtomicSymbol("gap",'-',null);
                    //  DNATools.getDNA().addSymbol(ns);
                    alph = DNATools.getDNA();
                }
            }
            else {

                alph = ProteinTools.getTAlphabet();

            }
            SymbolParser parse = alph.getParser("token");
            for (currSeqCount = 0; currSeqCount < sequenceNames.size(); currSeqCount++) {
                String sd = null;
                //change stop codons to specified symbols
                sd = sequenceData[currSeqCount].replace('~',
                                                        '-');              //umm how to deal with Term Signals, this should be fixed with synanyms
                sd = sequenceData[currSeqCount].replace('.',
                                                        '-');              //umm how to deal with Term Signals, this should be fixed with synanyms
                StringBuffer sb = new StringBuffer();
                SymbolList sl = null;
                sequenceDataMap.put((String)sequenceNames.get(currSeqCount),
                                    parse.parse(sd));
            }

            return  (new SimpleAlignment(sequenceDataMap));
        } catch (Exception e) {
            System.out.println("msfofrmat " + e.getMessage());
            //  throw (e);
        }
        return  (null);
    }           //end read it
}               //end class



