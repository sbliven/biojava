package symbol;


/**
 * Allows the processing of a large fasta file by breaking the 
 * matrix into smaller chunks.
 * @Author Andy Hammer, University of Utah
 * @Date 02 Jan 2003
 * PatternDemo is an extension of AlignmentDemo.java
 * PatternDemo also deals with Substitutions, Insertions, and Deletions (SIDs)
 * To run the program:
 * >java PatternDemo inputFile.fasta somePattern Sub Ins Del
 * Example:
 * >java PatternDemo myDNA.fasta CCCTGA 1 0 0
 * will search the fasta file myDNA for the pattern CCCTGA allowing
 * one mismatch, zero insertinons, and zero deletions.
 */

import java.io.*;
import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;

public class PatternDemo {

  public PatternDemo(Sequence fileSeq, SymbolList motif, String sub, String ins, String del) throws Exception {
    this.fileSeq=fileSeq;
    this.motif=motif;
    maxSub=Integer.parseInt(sub);
    maxIns=Integer.parseInt(ins);
    maxDel=Integer.parseInt(del);
    seqTitle = fileSeq.getName();
    fileSeqSB = new StringBuffer();
    motifSB = new StringBuffer();
    fileSeqLength=fileSeq.length();
    fileSeqStart=1;
    fileSeqEnd=fileSeqLength;
    motifLength=motif.length();
    backStep=motifLength+maxIns;
    matchScore=0;//similarity Score
    subScore=0;//substitution Score
    insScore=0;//insertion Score
    delScore=0;//deletion Score
    score=0;
    match=false;    
	  sink=0;
    hitCount=0; 
    path = new ArrayList(); 
  }//End PatternDemo constructor

  //Fills a matrix by computing similarity s(sub, motif) via the DP algorithm
  public void fillMatrix() throws BioException{
    //load last column examined
    for(int i=0; i<=motifLength; i++){
      matrix[0][i]=lastColumn[i];
    }
    //System.out.println("Filled Matrix with symbols "+fileSeqStart+" to "+fileSeqEnd);
    for(int y=1; y<=motifLength; y++){
      for(int x=1; x<=fileSeqEnd-fileSeqStart+1; x++){
        //System.out.println("fileSeqStart="+fileSeqStart);
        //System.out.println("fileSeqLength="+fileSeqLength);
        //System.out.println("fileSeqEnd="+fileSeqEnd);
        //System.out.println("x,y="+x+","+y);
        Symbol A = fileSeq.symbolAt(x+fileSeqStart-1);
        Symbol B = motif.symbolAt(y);
        if(compare(A, B)){
          score=matrix[x-1][y-1].getScore();
          score++;
          match=true;
        }else{
          score=matrix[x-1][y-1].getScore()-1;
          match=false;
        }
        if(matrix[x-1][y].getScore()-1>score){
          score=matrix[x-1][y].getScore()-1;
          match=false;
        }
        if(matrix[x][y-1].getScore()-1>score){
          score=matrix[x][y-1].getScore()-1;
          match=false; 
        }
        if(0>score){
          score=0;
          match=false;
        }
        matrix[x][y] = new MatrixValue(score, match);
      }
    }
    //printMatrix(2);
  }//End fillMatrix()
  
  //compares two dna symbols including abiguity symbols
  private static boolean compare(Symbol symbolA, Symbol symbolB)
      throws BioException {
    FiniteAlphabet A = ((FiniteAlphabet) symbolA.getMatches());
    FiniteAlphabet B = ((FiniteAlphabet) symbolB.getMatches());
    Iterator ia = A.iterator();
    int hits=0;
    while(ia.hasNext()){
      Symbol s = (Symbol)ia.next();
      if((B.contains(s))){
        return true;
      }
    }
    return false;
  }//End compare()

  //prints a LxL matrix where L is motifLength, starting at s
  public void printMatrix(int s){
    int start=s;
    //First line with fileSeq
    System.out.print(" ");
    for(int y=s; y<s+motifLength; y++){
      System.out.print(" "+ fileSeq.subStr(y, y));
    }
    System.out.println();
    //Second line of all zeros
    System.out.print(" ");
    for(int y=s; y<s+motifLength; y++){
      System.out.print(" " + String.valueOf(matrix[y][0].getScore()));
    }
    System.out.println();
    //Third line to end of motif and matrix values
    for(int y=1; y<=motifLength; y++){
      System.out.print(motif.subStr(y, y));
      for(int x=s; x<s+motifLength; x++){
        System.out.print(" "+matrix[x][y].getScore());
      }
      System.out.println();
    }
    
  }//End printMatrix()

  // x & y are matrix coordinates
  public boolean backtrack(int x, int y){
    if(sidFailure())
      return false;
    if(initialVector(x,y))
      return true;
    if(matrix[x][y].getMatch()){
      matchScore++;
			if(backtrack(x-1, y-1)){
        path.add("MATCH:"+x+","+y);//these coords are from matrix, not fileSeq
				return true;
			}
    }else{//backtrack along all 3 vetices and keep best Pathscore
      return probePaths(x,y);
    }
	 return false;
  }//End backtrack()
  
  public void resetPath(Collection c){
    path.clear();
    path.addAll(c);
  }
  
  //returns true if any SID value has been exceeded.
  public boolean sidFailure(){
    if(subScore>maxSub){
      subScore--;
      return true;
    }else if(insScore>maxIns){
      insScore--;
      return true;
    }else if(delScore>maxDel){
      delScore--;
      return true;
    }else
      return false;    
  }
  
  //returns true if zero of x or y axis of matrix has been reached
  public boolean initialVector(int x, int y){
    if(x==0&&y==0){
      return true;
    }else if(x==0){
      path.add("DEL:"+x+","+y);
      delScore++;
      return true;
    }else if(y==0){
      return true;
    }else 
      return false;    
  }
  
  public boolean probePaths(int x, int y){
    //initialize some local variables
    ArrayList originalPath=new ArrayList();
    ArrayList subPath=new ArrayList();
    ArrayList insPath=new ArrayList();
    ArrayList delPath=new ArrayList();
    int S=subScore; int I=insScore; int D=delScore; int M=matchScore;
    int subPathScore=ONEMILLION; int insPathScore=ONEMILLION; int delPathScore=ONEMILLION;
    int subMatches=0; int insMatches=0; int delMatches=0;
    originalPath.addAll(path);
    
    subScore++;
    if(backtrack(x-1, y-1)){
      subPathScore = subScore + 2*insScore + 2*delScore;
      subMatches = matchScore;
    }
    subPath.addAll(path);
    resetPath(originalPath);
    subScore=S; insScore=I; delScore=D; matchScore=M;
    
    insScore++;
    if(backtrack(x-1, y)){
      insPathScore = subScore + 2*insScore + 2*delScore;
      insMatches = matchScore;
    }
    insPath.addAll(path);
    resetPath(originalPath);    
    subScore=S; insScore=I; delScore=D; matchScore=M;     
          
    delScore++;
    if(backtrack(x, y-1)){
      delPathScore = subScore + 2*insScore + 2*delScore;
      delMatches = matchScore;
    }
    delPath.addAll(path);
    resetPath(originalPath);     
    subScore=S; insScore=I; delScore=D; matchScore=M;
    
    if(subPathScore==ONEMILLION&&insPathScore==ONEMILLION&&delPathScore==ONEMILLION){
      //all three paths failed
      return false;  
    }
    if(subMatches>insMatches&&subMatches>delMatches){
      path.addAll(subPath);
      path.add("SUB:"+x+","+y);
      updateScores();
      return true;        
    }else if(insMatches>subMatches&&insMatches>delMatches){
      path.addAll(insPath);
      path.add("INS:"+x+","+y);
      updateScores();
      return true;        
    }else if(delMatches>subMatches&&delMatches>insMatches){
      path.addAll(delPath);
      path.add("DEL:"+x+","+y);
      updateScores();
      return true;        
    }else if(subPathScore!=ONEMILLION&&subPathScore<=insPathScore&&subPathScore<=delPathScore){
      path.addAll(subPath);
      path.add("SUB:"+x+","+y);
      updateScores();
      return true;
    }else if(insPathScore!=ONEMILLION&&insPathScore<=subPathScore&&insPathScore<=delPathScore){
      path.addAll(insPath);
      path.add("INS:"+x+","+y);
      updateScores();
      return true;
    }else if(delPathScore!=ONEMILLION&&delPathScore<=subPathScore&&delPathScore<=insPathScore){
      path.addAll(delPath);
      path.add("DEL:"+x+","+y);
      updateScores();
      return true;
    }
    return false;    
  }//End probePaths
  
  public void resetScores(){
    matchScore=0;
    subScore=0;
    insScore=0;
    delScore=0;  
  }

  public void printScores(){
    System.out.println("SCORES:");
    System.out.println("Matches: "+matchScore);
    System.out.println("Substitutions: "+subScore);
    System.out.println("Insertions: "+insScore);
    System.out.println("Deletions: "+delScore);
    System.out.println();
  }
  
  public int getTotalHits(){
    return hitCount;  
  }
  
  
  public void startBacktrack(){
    int minScore = motifLength-(((maxSub+maxDel)*2)+maxIns);
    for(int i=1; i<=fileSeqEnd-fileSeqStart+1; i++){
      sink=i;
      if(matrix[i][motifLength].getScore()>=minScore){
        //System.out.println("Score "+matrix[i][motifLength].getScore()+" at "+(fileSeqEnd-fileSeqStart+1));
        path.clear();
        resetScores();
        if(backtrack(sink, motif.length())==false){
        }else{
          printHit();
          //printPath();
          arrangeSBs();
          printAlignment();
          //printScores();
        }
      }
    }
  }
  
  public void startSearch()throws Exception{
    matrix = new MatrixValue[X_AXIS+2][motifLength+1];
    lastColumn = new MatrixValue[motifLength+1];
    //fill initial vectors of matrix
    for(int x=0; x<=X_AXIS+1; x++){
      matrix[x][0] = new MatrixValue(0,false);
    }
    for(int y=0; y<=motifLength; y++){
      lastColumn[y] = new MatrixValue(0,false);
    }
    if(fileSeqLength>X_AXIS){
      fileSeqEnd=X_AXIS+1;
    }
    while(fileSeqEnd<=fileSeqLength){
      fillMatrix();
      startBacktrack();
      //refill last column
      if(sink-backStep>0){
        for(int y=1; y<=motifLength; y++){
          lastColumn[y] = matrix[sink-backStep][y];
        }
        setFileSeqIndices();
      }
    }
  }//End startSearch() 
  
  public void setFileSeqIndices(){
    if(fileSeqEnd+X_AXIS < fileSeqLength){
      fileSeqStart+=sink-backStep;
      fileSeqEnd=fileSeqStart+X_AXIS;
    }else if(fileSeqEnd==fileSeqLength){
      fileSeqEnd=fileSeqLength+100;      
    }else{
      fileSeqStart+=sink-backStep;
      fileSeqEnd=fileSeqLength;      
    }
  }
  
  
  public void printAlignment(){
    System.out.println("ALIGNMENT:");
    System.out.println(fileSeqSB.toString());
    System.out.println(motifSB.toString());
    System.out.println();    
  }
  
  //arranges two StringBuffers to output the alignment
  public void arrangeSBs(){
    motifSB.delete(0, motifSB.length());
    fileSeqSB.delete(0, fileSeqSB.length());
    String start = (String)path.get(0);
    //Adjust matrix coords to fileSeq coords.
    start = start.substring(start.indexOf(":")+1, start.indexOf(","));
    int adjStart = Integer.parseInt(start);
    adjStart+=fileSeqStart-1;
    String motifString = motif.seqString();
    String fileSeqString = fileSeq.subStr(adjStart, adjStart+path.size()-1);
    int motifCount=0;
    int fileSeqCount=0;
    Iterator it = path.iterator();
    while(it.hasNext()){
        String tmpString = (String)it.next();
        if(tmpString.startsWith("MATCH")){
          motifSB.append(motifString.charAt(motifCount++));
          fileSeqSB.append(fileSeqString.charAt(fileSeqCount++));
        }
        else if(tmpString.startsWith("SUB")){
          motifSB.append(motifString.charAt(motifCount++));
          fileSeqSB.append(fileSeqString.charAt(fileSeqCount++));
        }
        else if(tmpString.startsWith("INS")){
          motifSB.append("-");
          fileSeqSB.append(fileSeqString.charAt(fileSeqCount++));
          if(motifCount==0)
            motifCount++;
        }
        else if(tmpString.startsWith("DEL")){
          motifSB.append(motifString.charAt(motifCount++));
          fileSeqSB.append("-");
          if(fileSeqCount==0)
            fileSeqCount++;
        }                
    }
  }
  
  public void printPath(){
    System.out.println("PATH:");
    Iterator it = path.iterator();
    while(it.hasNext()){
      String tmpString=(String)it.next();
      System.out.println(tmpString);
    }    
  }
  
  public void printHit(){
    hitCount++;
    System.out.print(seqTitle);
    String firstPath = (String)path.get(0);
    //Adjust matrix coords to fileSeq coords.
    String start = firstPath.substring(firstPath.indexOf(":")+1, firstPath.indexOf(","));
    int adjStart = Integer.parseInt(start);
    adjStart+=fileSeqStart-1;
    if(firstPath.startsWith("DEL"))
      adjStart++;
    start=String.valueOf(adjStart);
    String end = (String)path.get(path.size()-1);
    end = end.substring(end.indexOf(":")+1, end.indexOf(","));
    int adjEnd = Integer.parseInt(end);
    adjEnd+=fileSeqStart-1;
    end=String.valueOf(adjEnd);
    System.out.println(":["+start+","+end+"]:"+matchScore+"["+subScore+","+insScore+","+delScore+"]");
  }
  
  public void updateScores(){
    Iterator it = path.iterator();
    while(it.hasNext()){
      String tmpString=(String)it.next();
      if(tmpString.startsWith("MATCH")){
        matchScore++;  
      }
      if(tmpString.startsWith("SUB")){
        subScore++;  
      } 
      if(tmpString.startsWith("INS")){
        insScore++;  
      }    
      if(tmpString.startsWith("DEL")){
        delScore++;  
      }             
    }    
  }  
  

  public static void main(String[] args) throws Exception {
    if (args.length < 5) {
      System.err.println("Usage: >java pattern.PatternDemo <fasta file  fileName> <SymbolList motif> <int maxSub> <int maxIns> <int maxDel>" +
                         "\nExample >java pattern.PatternDemo input.fasta atctgat 1 1 1");
    }
    String fileName = args[0];
    FileReader stream = new FileReader(fileName);
    BufferedReader br = new BufferedReader(stream);
    SequenceIterator si = SeqIOTools.readFastaDNA(br);
    SymbolList motif=DNATools.createDNA(args[1]);
    int totalHits=0;
    while(si.hasNext()){
      Sequence sequence = si.nextSequence();
      PatternDemo aMatrix = new PatternDemo(sequence, motif, args[2], args[3], args[4]);
  	  aMatrix.startSearch();
      totalHits+=aMatrix.getTotalHits();
  	  if(!si.hasNext())
        System.out.println("totalHits = "+totalHits);
	  }
  }

  private SymbolList fileSeq;
  private SymbolList motif;
  private StringBuffer fileSeqSB;
  private StringBuffer motifSB;
  private String seqTitle;
  private int fileSeqLength;
  private int fileSeqStart;//start position of current block of file sequence
  private int fileSeqEnd;//end position of current block of file sequence
  private int motifLength;
  private int matchScore;
  private int subScore;
  private int insScore;
  private int delScore;
  private int hitCount;
  private int maxSub;//number of allowable substitutions
  private int maxIns;//number of allowable insertions
  private int maxDel;//number of allowable deletions
  private int sink;//the 1st highest value in the last row of matrix
  private int score;//temp holder for similarity score
  private int backStep;//Distance array needs to back up before loading next block of fileSeq
  private boolean match;//backtrack value
  private ArrayList path;//working copy
  private MatrixValue[][] matrix;
  private MatrixValue[] lastColumn;
  public final static int ONEMILLION = 1000000000;//default value for scores in probePaths
  public final static int X_AXIS = 10000;//x axis size of matrix

}
