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
package org.biojava.bio.seq.io;

import java.io.*;
import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.apache.regexp.RE;

/**
 * Title:        File2Biojava
 * Description:  This project will take in a file of any common bioinformatics
 *               file formats and convert it to the appropriate Biojava object.
 *               It will also convert the Biojava object back into the corresponding
 *               file format.
 * Copyright:    Copyright (c) 2002
 * Company:      Maxygen
 * @author Nimesh Singh
 * @version 1.0
 */

public class SeqAlignReadWrite {
  //constants representing different file types
  public static final int FASTADNA = 1;
  public static final int FASTAPROTEIN = 2;
  public static final int EMBL = 3;
  public static final int GENBANK = 4;
  public static final int SWISSPROT = 5;
  public static final int GENPEPT = 6;
  public static final int MSFDNA = 7;
  public static final int FASTA = 8;
  public static final int FASTAALIGN = 9;
  public static final int MSFPROTEIN = 10;

  public SeqAlignReadWrite() {
  }

  /**
   * Attempts to guess the filetype of a file given the name
   */
  public static int guessFileType(String fileName) throws Exception {
    //First tries by matching an extension
    if ((new RE(".*\\u002eem.*")).match(fileName)) {
      return EMBL;
    }
    else if ((new RE(".*\\u002egb.*")).match(fileName)) {
      return GENBANK;
    }
    else if ((new RE(".*\\u002esp.*")).match(fileName)) {
      return SWISSPROT;
    }
    else if ((new RE(".*\\u002egp.*")).match(fileName)) {
      return GENPEPT;
    }
    else if ((new RE(".*\\u002efa.*")).match(fileName)) {
      return guessFastaType(fileName);
    }
    else if ((new RE(".*\\u002emsf.*")).match(fileName)) {
      return guessMsfType(fileName);
    }

    //Reads the file to guess based on content
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    String line1 = br.readLine();
    String line2 = br.readLine();

    if (line1.startsWith(">")) {
      return guessFastaType(fileName);
    }
    else if (line1.startsWith("PileUp")) {
      return guessMsfType(fileName);
    }
    else if (line1.startsWith("!!AA_MULTIPLE_ALIGNMENT")) {
      return MSFPROTEIN;
    }
    else if (line1.startsWith("!!NA_MULTIPLE_ALIGNMENT")) {
      return MSFDNA;
    }
    else if (line1.startsWith("ID")) {
      for (int i = 0; i < line1.length(); i++) {
        if (Character.toUpperCase(line1.charAt(i)) == 'P' &&
            Character.toUpperCase(line1.charAt(i+1)) == 'R' &&
            Character.toUpperCase(line1.charAt(i+2)) == 'T') {
          return SWISSPROT;
        }
      }
      return EMBL;
    }
    else if (line1.toUpperCase().startsWith("LOCUS")) {
      for (int i = 0; i < line1.length(); i++) {
        if (Character.toUpperCase(line1.charAt(i)) == 'A' &&
            Character.toUpperCase(line1.charAt(i+1)) == 'A') {
          return GENPEPT;
        }
      }
      return GENBANK;
    }
    else if (line1.length() >= 45 &&
             line1.substring(19, 45).equalsIgnoreCase("GENETIC SEQUENCE DATA BANK")) {
      return guessGenType(fileName);
    }
    else {
      System.out.println("guessFileType -- Could not guess file type.");
      return 0;
    }
  }

  /**
   * Helper function for guessFileName.
   */
  private static int guessFastaType(String fileName) throws Exception {
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    String line = br.readLine();
    line = br.readLine();
    for (int i = 0; i < line.length(); i++) {
        if (Character.toUpperCase(line.charAt(i)) == 'F' ||
            Character.toUpperCase(line.charAt(i)) == 'L' ||
            Character.toUpperCase(line.charAt(i)) == 'I' ||
            Character.toUpperCase(line.charAt(i)) == 'P' ||
            Character.toUpperCase(line.charAt(i)) == 'Q' ||
            Character.toUpperCase(line.charAt(i)) == 'E') {
          return FASTAPROTEIN;
        }
      }
      return FASTADNA;
  }

  /**
   * Helper function for guessFileName.
   */
  private static int guessMsfType(String fileName) throws Exception {
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    String line = br.readLine();
    if (line.startsWith("!!NA_MULTIPLE_ALIGNMENT")) {
      return MSFDNA;
    }
    else if (line.startsWith("!!AA_MULTIPLE_ALIGNMENT")) {
      return MSFPROTEIN;
    }
    else {
      while (line.indexOf("Type: ") == -1) {
        line = br.readLine();
      }
      int typeIndex = line.indexOf("Type: ") + 6;
      if (line.startsWith("N")) {
        return MSFDNA;
      }
      else if (line.startsWith("P")) {
        return MSFPROTEIN;
      }
      else {
        System.out.println("guessFileType -- Could not guess file type.");
        return 0;
      }
    }
  }

  /**
   * Helper function for guessFileName.
   */
  private static int guessGenType(String fileName) throws Exception {
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    String line = br.readLine();
    while (line.indexOf("LOCUS") == -1) {
      line = br.readLine();
    }
    for (int i = 0; i < line.length(); i++) {
      if (Character.toUpperCase(line.charAt(i)) == 'A' &&
          Character.toUpperCase(line.charAt(i+1)) == 'A') {
        return GENPEPT;
      }
    }
    return GENBANK;
  }

  /**
   * Reads a file and returns the corresponding Biojava object.
   */
  public static Object fileToBiojava(int fileType, BufferedReader br) throws Exception {
    switch (fileType) {
      case MSFDNA:
      case MSFPROTEIN:
      case FASTAALIGN:
        return fileToAlign(fileType, br);
      case FASTADNA:
      case FASTAPROTEIN:
      case EMBL:
      case GENBANK:
      case SWISSPROT:
      case GENPEPT:
        return fileToSeq(fileType, br);
      default:
        System.out.println("fileToBiojava -- File type not recognized.");
        System.exit(0);
        return null;
    }
  }

  /**
   * Converts a file to an Biojava alignment.
   */
  private static Alignment fileToAlign(int fileType, BufferedReader br) throws Exception{
    switch(fileType) {
      case MSFDNA:
      case MSFPROTEIN:
        return (new MSFAlignmentFormat()).read(br);
      case FASTAALIGN:
        //come up with something
        return null;
      default:
        System.out.println("fileToAlign -- File type not recognized.");
        System.exit(0);
        return null;
    }
  }

  /**
   * Converts a file to a Biojava sequence.
   */
  private static SequenceIterator fileToSeq(int fileType, BufferedReader br) {
    switch (fileType) {
      case FASTADNA:
        return SeqIOTools.readFastaDNA(br);
      case FASTAPROTEIN:
        return SeqIOTools.readFastaProtein(br);
      case EMBL:
        return SeqIOTools.readEmbl(br);
      case GENBANK:
        return SeqIOTools.readGenbank(br);
      case SWISSPROT:
        return SeqIOTools.readSwissprot(br);
      case GENPEPT:
        return SeqIOTools.readGenpept(br);
      default:
        System.out.println("fileToSeq -- File type not recognized.");
        System.exit(0);
        return null;
    }
  }

  /**
   * Converts a Biojava object to the given filetype.
   */
  public static void biojavaToFile(int fileType, OutputStream os, Object biojava) throws Exception {
    switch (fileType) {
      case MSFDNA:
      case MSFPROTEIN:
      case FASTAALIGN:
        alignToFile(fileType, os, (Alignment) biojava);
        break;
      case FASTA:
      case FASTADNA:
      case FASTAPROTEIN:
      case EMBL:
      case GENBANK:
      case SWISSPROT:
      case GENPEPT:
        seqToFile(fileType, os, (SequenceIterator) biojava);
        break;
      default:
        System.out.println("biojavaToFile -- File type not recognized.");
        System.exit(0);
    }
  }

  /**
   * Converts a Biojava alignment to the given filetype.
   */
  private static void alignToFile(int fileType, OutputStream os, Alignment align) throws Exception{
    switch(fileType) {
      case MSFDNA:
        (new MSFAlignmentFormat()).writeDna(os, align);
        break;
      case MSFPROTEIN:
        (new MSFAlignmentFormat()).writeProtein(os, align);
        break;
      case FASTAALIGN:
        //come up with something
        break;
      default:
        System.out.println("alignToFile -- File type not recognized.");
        System.exit(0);
    }
  }

  /**
   * Converts a Biojava sequence to the given filetype.
   */
  private static void seqToFile(int fileType, OutputStream os, SequenceIterator seq) throws IOException, BioException {
    switch (fileType) {
      case FASTADNA:
      case FASTAPROTEIN:
      case FASTA:
        SeqIOTools.writeFasta(os, seq);
        break;
      case EMBL:
        SeqIOTools.writeEmbl(os, seq);
        break;
      case SWISSPROT:
        SeqIOTools.writeSwissprot(os, seq);
        break;
      case GENBANK:
        SeqIOTools.writeGenbank(os, seq);
        break;
      case GENPEPT:
        SeqIOTools.writeGenpept(os, seq);
        break;
      default:
        System.out.println("seqToFile -- File type not recognized.");
        System.exit(0);
    }
  }

  /**
   * Just for testing purposes.
   */
  public static void main(String[] args) throws Exception{
    String files[] = new String[5];
    files[0] = "U:/parsing_testcases/msf/alignx_export.msf";
    files[1] = "U:/parsing_testcases/msf/alignx_export_jalview.msf";
    files[2] = "U:/parsing_testcases/msf/clustalw.msf";
    files[3] = "U:/parsing_testcases/msf/megalign_export.msf";
    files[4] = "U:/parsing_testcases/msf/egcyfp-blahblahblah.msf.txt";
    int outputFileTypes[] = new int[3];
    outputFileTypes[0] = FASTAPROTEIN;
    outputFileTypes[1] = SWISSPROT;
    outputFileTypes[2] = GENPEPT;
    String outputFileNames[] = new String[3];
    outputFileNames[0] = ".fasta.txt";
    outputFileNames[1] = ".sp.txt";
    outputFileNames[2] = ".gp.txt";
    for (int ift = 0; ift < 5; ift++) {
/*      for (int oft = 0; oft < 3; oft++) {
        int fileType = guessFileType(files[ift]);
        BufferedReader br = new BufferedReader(new FileReader(files[ift]));
        System.out.println(br.readLine());
        br = new BufferedReader(new FileReader(files[ift]));
        OutputStream os = new FileOutputStream(files[ift].substring(0, 33) + files[ift].substring(34) + outputFileNames[oft]);
        SequenceIterator seq1 = (SequenceIterator) fileToBiojava(fileType, br);
        Sequence temp = null;
        while (seq1.hasNext()) {
          temp = seq1.nextSequence();
          System.out.println(temp.getName() + " " + temp.getURN());
          Annotation seqAn = temp.getAnnotation();
          for (Iterator i = seqAn.keys().iterator(); i.hasNext(); ) {
            Object key = i.next();
            Object value = seqAn.getProperty(key);
            System.out.println(key.toString() + ": " + value.toString());
          }
        }
        br = new BufferedReader(new FileReader(files[ift]));
        SequenceIterator seqTemp = (SequenceIterator) fileToBiojava(fileType, br);
        br = new BufferedReader(new FileReader(files[ift]));
        seq1 = (SequenceIterator) fileToBiojava(fileType, br);
        biojavaToFile(outputFileTypes[oft], os, seq1);
        os.close();

        BufferedReader br2 = new BufferedReader(new FileReader(files[ift].substring(0,33)+files[ift].substring(34)+outputFileNames[oft]));
        System.out.println(br2.readLine());
        int fileType2 = guessFileType(files[ift].substring(0,33)+files[ift].substring(34)+outputFileNames[oft]);
        br2 = new BufferedReader(new FileReader(files[ift].substring(0,33)+files[ift].substring(34)+outputFileNames[oft]));
        SequenceIterator seq2 = (SequenceIterator) fileToBiojava(fileType2, br2);
        SymbolTokenization Toke = null;
        switch (fileType) {
          case FASTADNA:
          case EMBL:
          case GENBANK:
            Toke = DNATools.getDNA().getTokenization("token");
            break;
          case FASTAPROTEIN:
          case SWISSPROT:
          case GENPEPT:
            Toke = ProteinTools.getTAlphabet().getTokenization("token");
            break;
          default:
            System.out.println("Testing -- filetype not recognized.");
            System.exit(0);
        }
        while (seqTemp.hasNext() && seq2.hasNext()) {
          String seqString2 = Toke.tokenizeSymbolList(seq2.nextSequence());
          System.out.println(seqString2);
          String seqStringTemp = Toke.tokenizeSymbolList(seqTemp.nextSequence());
          System.out.println(seqStringTemp);
          System.out.println("Strings match: " + seqString2.equalsIgnoreCase(seqStringTemp));
          if (!seqString2.equalsIgnoreCase(seqStringTemp)) System.exit(0);
        }
      }
    }
*/
    Iterator iter = DNATools.getDNA().iterator();
    SymbolTokenization toke = DNATools.getDNA().getTokenization("token");
    while (iter.hasNext()) {
      Object sym = iter.next();
      System.out.println(toke.tokenizeSymbol((Symbol) sym) + " " + sym);
    }
    System.out.println("File: " + files[ift]);
    MSFAlignmentFormat msfaf = new MSFAlignmentFormat();
    BufferedReader br3 = new BufferedReader(new FileReader(files[ift]));
    System.out.println(br3.readLine());
    br3 = new BufferedReader(new FileReader(files[ift]));
    Alignment align = msfaf.read(br3);
    System.out.println(align.getLabels().get(1).toString());
    OutputStream os2 = new FileOutputStream(files[ift] + "test.txt");
    msfaf.writeProtein(os2, align);
    }

  }
}