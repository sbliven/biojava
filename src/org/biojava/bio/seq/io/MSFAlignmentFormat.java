
package org.biojava.bio.seq.io;

import  org.biojava.bio.seq.io.AlignmentFormat;
import  org.biojava.bio.symbol.Alignment;
import  org.biojava.bio.symbol.*;
import  java.io.BufferedReader;
import  gnu.regexp.RE;
import  gnu.regexp.REMatch;
import  org.biojava.bio.seq.*;
import  org.biojava.bio.seq.io.SymbolParser;
import  java.io.FileReader;
import  java.util.Vector;
import  java.util.StringTokenizer;
import  java.util.HashMap;


/**
 * Title: MSFAlignmentFormat
 * Description: Reads a GCG formated MSF file and returns a biojava alignment
 * Description: for alignment format see end of this java file    
 * @author Robin Emig robin.emig@lycosmail.com  jaspercraft@yahoo.com
 * @version 1.0
 */
public class MSFAlignmentFormat
        implements AlignmentFormat {

    /**
     * default constructor does nothing
     */
    public MSFAlignmentFormat () {
    }

    /**
     * main - for testing
     * @param args
     */
    public static void main (String[] args) {
        String filename="";
        if (args.length < 1) {
          System.out.println("please enter a sequence file name");
        }
        else {
            filename = args[0];
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            MSFAlignmentFormat MSFAlignmentFormat1 = new MSFAlignmentFormat();
            MSFAlignmentFormat1.read(reader);

            //print them out for testing
                        /*
                for (currSeqCount = 0; currSeqCount < sequenceNames.size(); currSeqCount++) {
                    System.out.println((String)sequenceNames.get(currSeqCount)
                            + ":" + sequenceData[currSeqCount]);
            }*/

        } catch (Exception E) {System.out.println(E.getMessage());}
    }

    /**
     * read - actually  parses the file
     * @param reader - buffered reader containing a GCG MSF formated file
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
            RE removewhitespace = new RE("[\\s+]");
            REMatch rem = null;
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
                rem = mtc.getMatch(line);
                if (rem == null) {
                    break;
                }               //end of line
                sequenceName = line.substring(rem.getSubStartIndex(1), rem.getSubEndIndex(1)).trim();
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
                    line = removewhitespace.substituteAll(line, "");
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
                    alph = RNATools.getRNA();
                }
                else {          //get DNA alph
                    alph = DNATools.getDNA();
                }
            }
            else {
                alph = ProteinTools.getTAlphabet();
            }
            SymbolParser parse = alph.getParser("token");
            for (currSeqCount = 0; currSeqCount < sequenceNames.size(); currSeqCount++) {
                String sd = null;
                //change gap codons to specified symbols
                sd = sequenceData[currSeqCount].replace('.', '-'); //. internal gap  ~ end gaps
                sd = sd.replace('~', '-');
                 //umm how to deal with Term Signals, this should be fixed with synanoms
                //well actually it cant, some programs use . to represent gaps, really no way to know

                StringBuffer sb = new StringBuffer();
                SymbolList sl = null;
                sequenceDataMap.put((String)sequenceNames.get(currSeqCount),
                        parse.parse(sd));
            }
            return  (new SimpleAlignment(sequenceDataMap));
        } catch (Exception e) {
            System.out.println("MSF format, bad file or some other error " +
                    e.getMessage());
            //  throw (e);
        }
        return  (null);
    }           //end read it
}               //end class


//below is the alignment sample file
/*
!!AA_MULTIPLE_ALIGNMENT 1.0
PileUp of: @seqlist

 Symbol comparison table: GenRunData:blosum62.cmp  CompCheck: 6430

                   GapWeight: 12
             GapLengthWeight: 4

 seqlist.msf  MSF: 167  Type: P  September 5, 1997 15:15  Check: 8487 ..

 Name: 1coha            Len:   167  Check: 7676  Weight:  1.00
 Name: hba_human        Len:   167  Check: 7676  Weight:  1.00
 Name: 2mhba            Len:   167  Check: 8765  Weight:  1.00
 Name: HBA_HORSE        Len:   167  Check: 8735  Weight:  1.00
 Name: hbb_horse        Len:   167  Check: 6585  Weight:  1.00
 Name: hbb_human        Len:   167  Check: 7528  Weight:  1.00
 Name: GLB5_PETMA       Len:   167  Check: 7298  Weight:  1.00
 Name: myg_phyca        Len:   167  Check: 9928  Weight:  1.00
 Name: lgb2_luplu       Len:   167  Check: 4296  Weight:  1.00

//

            1                                                   50
     1coha  ~~~~~~~~~V LSPADKTNVK AAWGKVGAHA GEYGAEALER MFLSFPTTKT
 hba_human  ~~~~~~~~~V LSPADKTNVK AAWGKVGAHA GEYGAEALER MFLSFPTTKT
     2mhba  ~~~~~~~~~V LSAADKTNVK AAWSKVGGHA GEYGAEALER MFLGFPTTKT
 HBA_HORSE  ~~~~~~~~~V LSAADKTNVK AAWSKVGGHA GEYGAEALER MFLGFPTTKT
 hbb_horse  ~~~~~~~~VQ LSGEEKAAVL ALWDKV..NE EEVGGEALGR LLVVYPWTQR
 hbb_human  ~~~~~~~~VH LTPEEKSAVT ALWGKV..NV DEVGGEALGR LLVVYPWTQR
GLB5_PETMA  PIVDTGSVAP LSAAEKTKIR SAWAPVYSTY ETSGVDILVK FFTSTPAAQE
 myg_phyca  ~~~~~~~~~V LSEGEWQLVL HVWAKVEADV AGHGQDILIR LFKSHPETLE
lgb2_luplu  ~~~~~~~~GA LTESQAALVK SSWEEFNANI PKHTHRFFIL VLEIAPAAKD

            51                                                 100
     1coha  YFPHF.DLSH .....GSAQV KGHGKKVADA LTNAVAHVDD ...M..PNAL
 hba_human  YFPHF.DLSH .....GSAQV KGHGKKVADA LTNAVAHVDD ...M..PNAL
     2mhba  YFPHF.DLSH .....GSAQV KAHGKKVGDA LTLAVGHLDD ...L..PGAL
 HBA_HORSE  YFPHF.DLSH .....GSAQV KAHGKKVGDA LTLAVGHLDD ...L..PGAL
 hbb_horse  FFDSFGDLSN PGAVMGNPKV KAHGKKVLHS FGEGVHHLDN ...L..KGTF
 hbb_human  FFESFGDLST PDAVMGNPKV KAHGKKVLGA FSDGLAHLDN ...L..KGTF
GLB5_PETMA  FFPKFKGLTT ADQLKKSADV RWHAERIINA VNDAVASMDD TEKM..SMKL
 myg_phyca  KFDRFKHLKT EAEMKASEDL KKHGVTVLTA LG...AILKK KGHH..EAEL
lgb2_luplu  LFSFLKGTSE VPQ..NNPEL QAHAGKVFKL VYEAAIQLQV TGVVVTDATL

            101                                                150
     1coha  SALSDLHAHK LRVDPVNFKL LSHCLLVTLA AHLPAEFTPA VHASLDKFLA
 hba_human  SALSDLHAHK LRVDPVNFKL LSHCLLVTLA AHLPAEFTPA VHASLDKFLA
     2mhba  SDLSNLHAHK LRVDPVNFKL LSHCLLSTLA VHLPNDFTPA VHASLDKFLS
 HBA_HORSE  SNLSDLHAHK LRVDPVNFKL LSHCLLSTLA VHLPNDFTPA VHASLDKFLS
 hbb_horse  AALSELHCDK LHVDPENFRL LGNVLVVVLA RHFGKDFTPE LQASYQKVVA
 hbb_human  ATLSELHCDK LHVDPENFRL LGNVLVCVLA HHFGKEFTPP VQAAYQKVVA
GLB5_PETMA  RDLSGKHAKS FQVDPQYFKV LAAVIADTVA .........A GDAGFEKLMS
 myg_phyca  KPLAQSHATK HKIPIKYLEF ISEAIIHVLH SRHPGDFGAD AQGAMNKALE
lgb2_luplu  KNLGSVHVSK .GVADAHFPV VKEAILKTIK EVVGAKWSEE LNSAWTIAYD

            151            167
     1coha  SVSTVLTSKY R~~~~~~
 hba_human  SVSTVLTSKY R~~~~~~
     2mhba  SVSTVLTSKY R~~~~~~
 HBA_HORSE  SVSTVLTSKY R~~~~~~
 hbb_horse  GVANALAHKY H~~~~~~
 hbb_human  GVANALAHKY H~~~~~~
GLB5_PETMA  MICILLRSAY ~~~~~~~
 myg_phyca  LFRKDIAAKY KELGYQG
lgb2_luplu  ELAIVIKKEM NDAA~~~
*/

