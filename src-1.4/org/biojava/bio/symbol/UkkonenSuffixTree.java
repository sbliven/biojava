package org.biojava.bio.symbol;

import java.util.*;

/**
 * Suffix Tree using Ukkonen's algorithm. Not quite industrial strenght yet, but it's getting there.
 *
 * A couple of little quirks:
 * It sometimes link to a leaf for the last character of a sequence, when adding several sequences. It doesn't seem to have any real consequence, but I don't really understand what could make it do that.
 * It has 2 list of sequences for searching and building, because it only expects 2 numbers when searching for a string. I'm planning on changing it relatively soon.
 * I'm trying to implement a nice and quick way to get back previous results, but it's not working that well, as it takes longer to check if we have the same query than to just compute the result.
 *
 * @author francois
 * @version $Revision$
 */
public class UkkonenSuffixTree{

  public static final char DEFAULT_TERM_CHAR ='$';
  private char terminationChar;

  InternalNode root;

  public static final int TO_A_LEAF = -1;
  private int e;

  /** Heads-up people, this one is a bit tricky:
   * This can contain either one long string, or a series of sequences (SequenceList). SequenceList is all well and good, but it's slower than a string.
   * So when we're building the tree and accessing it all the time, we're using it as a string.
   * For the rest of the time, the code to search the tree expects the searched word to be in sequences, at which point it's easier to use SequenceList (add the word into it).
   * It might be worth it to go and change the walkTo method to avoid this, but I'll see later into it.
   *
   * The algorithm is no longer linear in construction time if we don't have a String there.
   */
  private CharSequence sequences;

  /** List of all sequences, @see SequenceList
   *
   */
  private SequenceList sequenceList;

  /** Has the frequencies of the symbols in this tree.
   */
  private double[] freqs;

  /** Describes the rule that needs to be applied after walking down a tree. Put as a class variable because it can only return a single object (and I don't want to extend Node any further.
   * rule 1: ended up at a leaf.
   * rule 2: need to extend an internalNode.
   * rule 3: would split an edge.
   * rule 4: ended up in the middle of an edge.
   * rule 5: ended up at an InternalNode
   *
   * Rule 5 counts as rule 4 when adding a sequence, but the rules are also used to when searching the tree.
   */
  private int rule;

  /** Shows the position of the subseq left that would go inside the edge
   */
  private int rule3Position;

  public static void main (String[] args){
    UkkonenSuffixTree tree= new UkkonenSuffixTree();
    int i;
    
    //else assume the the argument is the sequence itself.
    for (i=0;i<args.length;i++)
      tree.addSequence(args[i],("args"+((i==0)?"":Integer.toString(i))),false);

    //tree.test(tree.root);
    //tree.test2();
  }

  public void test(InternalNode root){
    Iterator iterator = root.children.values().iterator();
    Edge y;
    Node x;
    int i=1;
    System.out.println("\nthis node has "+root.children.size()+" children.");

    while (iterator.hasNext()){
      y = (Edge)iterator.next();
      System.out.print(i+" Edge label: "+y.getEdgeLabel());
      x = y.getChild();

      if (x instanceof Leaf)
	System.out.println(" to a leaf "+((Leaf)x).leafPositions[0][0]);
      else
	test((InternalNode)x);
      i++;
    }
  }
  public void test2(){
    int i=1;
    Leaf[] leaves;

    System.out.println("How many positions: "+root.getCount());
    leaves = root.getLeaves();
    for (i=0;i<leaves.length;i++) {
      System.out.println("Leaf: "+leaves[i].leafPositions[0][0]+" "+leaves[i].getNoPositions()+" "+leaves[i].getPathLabel());
    } // end of for ()
    System.out.println("total nodes: "+buildNodeVector(root, new Vector()).size());
  }

  /** Initializes a new <code>UkkonenSuffixTree</code> instance.
   */
  public UkkonenSuffixTree(){
    terminationChar = DEFAULT_TERM_CHAR;
    root = new InternalNode(this);
    e=0;
    freqs = null;
    sequenceList = new SequenceList();
    sequences = sequenceList;
  }

  public UkkonenSuffixTree(String seqs){
    this();
    buildTree(seqs);
  }

  /** returns the root of the tree
   * @return the root of the tree.
   */
  public InternalNode getRoot(){return root;}

  public int getE(){return e;}

  /** Get the sequences used to build this tree.
   * @return the sequences used to build this tree.
   */
  public String getSequences(){return sequences.toString();}

  /** Just prints out some information about the current strings
   */
  public void showSequences(){
    CharSequence charSeq;
    Sequence seq;
    int i;
    System.out.println("List of sequences");
    for (i=0;i<getNoSequences();i++){
      charSeq = (CharSequence)sequenceList.elementAt(i);
      if (charSeq instanceof Sequence){
	seq = (Sequence)charSeq;
	System.out.println("Seq "+i+": "+seq.name+" length "+seq.length());
      }
      else
	System.out.println("Seq "+i+": length "+charSeq.length());
    }
  }

  /** returns the current number of sequences in the tree.
   * @return the number of sequences in the tree.
   */
  public int getNoSequences(){return sequenceList.size();}

  /** Get an iterator of all nodes.
   *
   * @return an iterator that can go through all the Nodes of the tree.
   */
  public Iterator getNodeIterator(){
    return getNodeIterator(root);
  }

  /**
   * Get an iterator of all nodes under the given node.
   *
   * @param subTreeRoot root of the subtree whose nodes we want to iterate.
   * @return an iterator that goes to all of the nodes under the given Node.
   */
  public Iterator getNodeIterator(InternalNode subTreeRoot){
    return buildNodeVector(subTreeRoot, new Vector()).iterator();
  }

  /** Recursively builds a vector with all of the nodes in it.
   * @param root the root of the tree over the nodes we want to put in the vector
   * @param nodeVector the vector in which to add the nodes
   * @return the vector after the nodes have been added.
   */
  private Vector buildNodeVector(InternalNode root, Vector nodeVector){
    Iterator iterator = root.children.values().iterator();
    Edge tempEdge;
    nodeVector.add(root);
    while (iterator.hasNext()){
      tempEdge = (Edge)iterator.next();
      if (tempEdge.getChild() instanceof InternalNode) {
	buildNodeVector((InternalNode)tempEdge.getChild(), nodeVector);
      } // end of if ()
      else {
	nodeVector.add(tempEdge.getChild());
      } // end of else
    }
    return nodeVector;
  }

  /** Returns true if a word ends up at a node or in the middle of an edge
   *
   *
   * @param word the word to be checked
   * @return true if a word ends up at a node or in the middle of an edge
   */
  boolean isWordAtNode(CharSequence word){
    return getNode(word)!=null;
  }

  /** Returns the node that a word ends up at. Null if it ends at an edge, or isn't in the tree.
   *
   * It also looks if this query has been made recently, in which case it simply gets the result of that query.
   *
   * This is not well implemented. The copying of the sequences String will probably be a bit expensive to do, and quite unnecessary. I suggest either changing sequences to a StringBuffer, or changing walkTo to accept a String.
   *
   * @param word the word ending at the node we want.
   * @return the Node at the end of this word, null if the word ends at an edge or isn't in the tree.
   */
  Node getNode(CharSequence word){
    TreePart answer = getTreePart(word);
    return (rule==5||rule==1)?(Node)answer:null;
  }


  /** Returns the edge that a word ends up at. Null if it ends at a node, or isn't in the tree.
   *
   * It also performs a lookup to see if this matches a recent query. At which point it simply fetches it instead of computing it.
   *
   * This is not well implemented. The copying of the sequences String will probably be a bit expensive to do, and quite unnecessary. I suggest either changing sequences to a StringBuffer, or changing walkTo to accept a String.
   *
   * @param word the word ending at the edge we want.
   * @return the Edge at the end of this word, null if the word ends at a Node or isn't in the tree.
   */
  Edge getEdge(CharSequence word){
    TreePart answer = getTreePart(word);
    return (rule==4)?
      (Edge)answer:
      null;
  }

  /** Walks down the tree following a given charSequence
   *
   * @param word the sequence we're walking down to.
   * @return the treePart this would lead us to.
   */
  TreePart getTreePart(CharSequence word){
    TreePart answer;
    int wordNo = getNoSequences();
    int oldLength = sequences.length();

    //this shouldn't happen, but better safe than sorry.
    //it's only true if we're in tree-building mode, at which point we shouldn't be accessing this method.
    if (sequences!=sequenceList){
      System.out.println("Sequences != sequenceList in getTreepart!?!");
      sequences = sequenceList;
    }

    /*answer  = getFromRecentLookups(word);
      if (answer!=null&& answer instanceof Node)
      return (Node)answer;
    */

    answer =walkTo(root, word, 0, word.length());

    return answer;
  }

  /** Returns the number of characters in the tree. This doesn't count end-of-words characters ($), unless they were explicitely put in (and as I already told you not to do). This is used for stats purposes, and most users would appreciate getting good stats about their data.
   *
   * @return the total number of symbols in the tree.
   */
  int getNoCharacters(){return sequenceList.length()-sequenceList.size();}

  /** Returns the terminationChar used in this tree.
   *
   * @return the terminationChar of this tree.
   */
  public char getTerminationChar(){return terminationChar;}

  public void buildTree(String seqs){
    addSequence(seqs,"unnamed", false);
  }

  /**
   * Add a sequence into the tree. If there are more sequences, they should be separated by a terminationChar ($ by default). If none exist, it is assumed that there is only 1 continuous sequence to be added.
   * @param seq the sequence/sequences to be added into the tree.
   * @param doNotTerminate whether we should terminate the sequence if it's non-terminated.
   */
  public void addSequence(String seq, String name, boolean doNotTerminate){
    int i;
    int start, end;
    Vector toBeAdded = new Vector();
    Iterator iterator;
    Sequence newSeq;
    String subseq;

    if (seq==null&&seq.length()==0)
      return;

    //freqs are no longer reliable. I'll make it to update it as it goes along, but at this point it'll have to do.
    freqs=null;

    //terminate the String if it's not terminated.
    if(!doNotTerminate&&seq.charAt(seq.length()-1)!=terminationChar)
      seq = seq+terminationChar;

    //count how many termination Chars in in.
    start =0;
    for (i=0;seq.indexOf(terminationChar, i)!=-1;i=seq.indexOf(terminationChar, i)+1){
      end=seq.indexOf(terminationChar, i);
      toBeAdded.add(seq.substring(start,end+1));
    }

    iterator = toBeAdded.iterator();
    i=0;
    while (iterator.hasNext()){
      subseq=(String)iterator.next();
      if (i==0)
	newSeq = new Sequence(name, subseq);
      else
	newSeq = new Sequence(name+i, subseq);
      addPreppedSequence(newSeq);
      i++;
    }
  }


  /** Add a single sequence into the tree.
   *
   * @param seq a <code>String</code> value
   */
  private void addPreppedSequence(Sequence seq){
    int i, k;
    int j=0;
    int lastExtension;
    int nextWalk=0;
    String gamma;
    InternalNode v;
    Node oldNode=null, newNode;
    Node currentNode;
    TreePart tempTreePart=null;
    Edge tempEdge;

    //Puts i at the end of the previous sequences
    i = sequences.length();
    lastExtension = sequences.length();
    j=i;


    //concatenates the new sequence to the previous ones.
    sequenceList.add(seq);

    //Uses the sequences as a string for tree building.
    sequences = sequenceList.toString();

    currentNode = root;
    //phase i
    for (; i<sequences.length();i++){
      //System.out.println("Phase "+i);

      e+=1;
      //extension j;
      for (;j<=i;j++){
	//System.out.println("extension "+j);

	//reset a couple of things...
	newNode = null;

	//find first node v at or above s[j-1,i] that is root or has a suffixLink
	while (currentNode!=root&&currentNode.getSuffixLink()==null)
	  currentNode = currentNode.getTopEdge().getParent();


	//if currentNode is root: walk down to s[j,i]
	if (root==currentNode)
	  tempTreePart=walkTo(root,sequences, j,i+1);
	else{

	  //else gamma = prolongation of s[j-1,i] from v
	  k= j+currentNode.getSuffixLink().getPathLabel().length();
	  //go to suffixLink(v) and walk down gamma
	  currentNode = currentNode.getSuffixLink();
	  tempTreePart=walkTo((InternalNode)currentNode,sequences,k,i+1);
	}


	if(tempTreePart instanceof Node)
	  currentNode = (Node)tempTreePart;
	else
	  currentNode= ((Edge)tempTreePart).getParent();
	//done with walk down the tree


	if (rule==1)
	  ((Leaf)currentNode).addPosition(j);
	if (rule==2)
	  newNode = doRule2((InternalNode)currentNode,i,j);
	if (rule==3){
	  newNode = doRule3((Edge)tempTreePart,i,j);
	  //we would've walked all the way here if it had existed
	  currentNode = ((Edge)tempTreePart).getChild();
	}

	if (oldNode!=null){
	  if(currentNode instanceof Leaf){
	    System.out.println("Linking to a leaf, but don't worry too much about it.");
	    //System.out.println("rule "+rule+" in phase "+i+" extension "+j+" out of "+sequences.length());
	    currentNode = currentNode.getTopEdge().getParent();
	  }
	  oldNode.suffixLink = currentNode;
	}
	oldNode = newNode;
	newNode = null;

	//System.out.println("rule: "+rule);

	if (rule==4||rule==5){
	  break;
	}

      } //end extension j

    } //end phase i
    finishAddition(i, j);

    //put back the sequenceList back into sequences.
    sequences = sequenceList;
  }


  /** Finish the addition of a sequence:
   * 1- add the last leaves.
   * 2- puts real values to all of the leaves.
   * ...
   */
  private void finishAddition(int endOfSeq, int lastExtension){
    Leaf[] leaves=getRoot().getLeaves();
    int i;

    for (i=0;i<leaves.length;i++)
      leaves[i].finalizePositions(endOfSeq);
  }

  /**
   * Walk down the tree. If subStart==subStop, the root is returned with a rule 5.
   *
   * @param from the root of the subtree we're walking down
   * @param subSource the source of the sequence we're walking down to.
   * @param subStart the first position of the subsequence in subSource
   * @param subStop the last position of the subsequence in subSource +1, such that subSource.subSequence(subStart,subStop) gives the original subsequence.
   * @return the parent Node if we arrive inside a edge, else the Node that we've arrived at.
   */
  public TreePart walkTo(InternalNode from, CharSequence subSource, int subStart, int subStop){
    InternalNode currentNode;
    Node arrivedAt;
    boolean canGoDown = true;
    Edge currentEdge=null;
    TreePart maybe;
    //System.out.println("start "+subStart+" stop "+subStop);
    //System.out.println("our sequences "+sequences);

    //System.out.println("walking to "+sequences.subSequence(subStart,subStop));
    //System.out.println("from "+from.getPathLabel());

    currentNode = from;
    arrivedAt = from;

    rule = 0;

    if (subStart==subStop){
      rule=5;
      return root;
    }

    while(canGoDown){
      currentEdge = currentNode.getEdge(subSource.charAt(subStart));
      if (currentEdge==null){
	canGoDown = false;
	rule = 2;
	break;
      }
      subStart += currentEdge.length();

      //Did we arrive at the middle of an edge?
      if (subStart>=subStop){
	subStart -= currentEdge.length();

	if (sequences.charAt(currentEdge.getStart()+subStop-subStart-1)==subSource.charAt(subStop-1)){
	  //is this the last character of the Edge arriving at a leaf?
	  if (currentEdge.getStart()+subStop-subStart==currentEdge.getStop()){

	    if (currentEdge.getChild() instanceof Leaf)
	      rule = 1;
	    else
	      rule = 5;
	    arrivedAt = currentEdge.getChild();
	  }
	  else
	    rule = 4;
	}
	else{
	  rule = 3;
	  rule3Position = subStart;
	}
	canGoDown = false;
	break;
      }

      //System.out.println("walked to "+sequences.substring(subStart,subStop));

      arrivedAt = currentEdge.getChild();
      if (arrivedAt instanceof Leaf){
	System.out.println("walking down a leaf...");
	System.out.println("walking "+sequences.subSequence(subStart,subStop));
	System.out.println("child path label: "+arrivedAt.getPathLabel());
      }

      currentNode = (InternalNode)arrivedAt;

    }//end while(canGoDown)


    if (rule==4||rule==3)
      return currentEdge;
    return arrivedAt;
  }

  /**
   * Apply rule 2: creating an edge off an internal node and putting a leaf at the end.
   *
   * @param toBeExtended the node where we're adding the new leaf
   * @param splittingPos the position of the character causing the split
   * @param suffixStart the starting position of the suffix whose leaf is to be created.
   */
  public Node doRule2(InternalNode toBeExtended, int splittingPos, int suffixStart){
    return toBeExtended.addLeafEdge(new Character(sequences.charAt(splittingPos)), suffixStart, splittingPos);
  }

  public Node doRule3(Edge toBeSplit, int endOfSubSeq, int suffixStart){
    return toBeSplit.splitEdge(endOfSubSeq, sequences.charAt(endOfSubSeq),
			       toBeSplit.getStart()+endOfSubSeq-rule3Position,
			       suffixStart);
  }

  /** Get the frequences of the each DNA character in a given sequence.
   * Checks if a count already exists and if so send that one.
   * Adding a new sequence voids the count, so if the frequencies exist, we can trust them.
   * Non-DNA characters are ignored, as is the case of the symbols;
   * The values are as given:
   * [0]:any (N,n,-) = 1 (so the statistics can ignore it).
   * [1]: A
   * [2]: C
   * [3]: G
   * [4]: T
   *
   * @return the frequency of each symbol above divided by the total number of those symbols in the sequence.
   */
  public double[] getDNAFrequencies(){
    int [] count= new int[4];
    double[] freqs = new double[5];
    final int a =0;
    final int c =1;
    final int g =2;
    final int t =3;
    int i, total=0;

    if (this.freqs!=null)
      return this.freqs;


    for (i=0;i<4;i++)
      count[i]=0;

    for (i=0; i<sequences.length();i++)
      switch (sequences.charAt(i)){
      case 'A':
      case 'a':
	count[a]++;
	break;
      case 'C':
      case 'c':
	count[c]++;
	break;
      case 'G':
      case 'g':
	count[g]++;
	break;
      case 'T':
      case 't':
	count[t]++;
	break;
      } //end switch
    for (i=0;i<4;i++)
      total+=count[i];

    freqs[0]=1;
    for (i=1;i<5;i++)
      freqs[i]=(double)count[i-1]/total;
    this.freqs=freqs;
    return freqs;
  }

  /** Provides a string to says where a position is
   * It outputs the name of the String and then the starting positions
   * @param pos a position in the full sequences
   * @return the sequence it belongs to and it's position relative to that sequence.
   */
  public String positionToString(int pos){
    Sequence seq = getSequence(pos);
    if (seq!=null)
      return "Position "+(pos-seq.start)+" in sequence "+seq.name;
    return "String unknown for position "+pos;
  }

/** Gives out the Sequence that a specific position is part of. If no sequence is found, it returns null.
 *
 * @param pos a position in the string with all sequences
 * @return the Sequence it's part of.
 */
  public Sequence getSequence(int pos){
    CharSequence maybe = sequenceList.getSequence(pos);
    if (maybe instanceof Sequence)
      return (Sequence) maybe;
    else
      return new Sequence("unnamed", maybe);

  }
}

/** A Node in a the SuffixTree
 *
 * @author Francois Pepin
 * @version 1.0
 */
class Node implements TreePart{
  static final int E = -1;
  UkkonenSuffixTree tree;
  Edge topEdge;
  Node suffixLink;
  final boolean isLeaf = false;


  public Node (UkkonenSuffixTree tree){
    this.tree = tree;
  }

  public Node (UkkonenSuffixTree tree, Edge topEdge){
    this(tree);
    this.topEdge = topEdge;
  }

  /** Returns the topEdge of this Node.
   * @return the topEdge of this Node.
   */
  public Edge getTopEdge(){return topEdge;}

  public void setTopEdge(Edge topEdge){this.topEdge=topEdge;}

  /** Returns the suffixLink of this Node.
   * @return the suffixLink of this Node.
   */
  public Node getSuffixLink(){return suffixLink;}

  public void setSuffixLink(Node sl){suffixLink = sl;}

  public UkkonenSuffixTree getTree(){return tree;}

  /** Gets the path label to that Node by iterating up to the root.
   * This is never called in practice because both Leaf and InternalNode have better implementations that supersede this one.
   *
   * If it ever gets deleted, make sure that this method signature does exist (maybe putting Node as an abstract class).
   *
   * @return the path label to that Node.
   */
  String getPathLabel(){
    StringBuffer pathLabel = new StringBuffer();
    InternalNode current=null;
    int i=1;
    Edge upEdge= this.getTopEdge();

    System.out.println("At old Path Label");

    if (this==tree.getRoot())
      return "";
    pathLabel.insert(0,upEdge.getEdgeLabel());
    current = upEdge.getParent();


    while (current!=tree.getRoot()){
      i++;
      upEdge = current.getTopEdge();
      pathLabel.insert(0,upEdge.getEdgeLabel());
      current = upEdge.getParent();
    }
    return pathLabel.toString();
  }


  /** Returns the depth of this node in the tree. Root has depth of 1.
   *
   * @return the depth of this node in the tree. Root has a depth of 1.
   */
  public int getDepth(){
    InternalNode goingUp;
    int i=1;

    if (this==getTree().getRoot())
      return i;

    goingUp = getTopEdge().getParent();
    i++;

    while (goingUp!=getTree().getRoot()){
      goingUp = goingUp.getTopEdge().getParent();
      i++;
    }

    return i;
  }

  /** Get all of the leaves that are below this Node.
   *
   * @return an array of the leaves under this Node.
   */
  Leaf[] getLeaves(){
    Iterator iterator;
    Vector leaves = new Vector();
    Node tempNode;
    Leaf[] results;
    int i=0;

    if (this instanceof Leaf){
      results = new Leaf[1];
      results[0] = (Leaf)this;
      return results;
    }

    iterator= tree.getNodeIterator((InternalNode)(this));

    while (iterator.hasNext()){
      tempNode = (Node)iterator.next();
      if (tempNode instanceof Leaf)
	leaves.add((Leaf)tempNode);
    }

    iterator = leaves.iterator();
    results = new Leaf[leaves.size()];
    while (iterator.hasNext()){
      results[i]=(Leaf)iterator.next();
      i++;
    }
    return results;
  }

  public TreeSet getLeavesNoOverlap(){
    return getLeavesNoOverlap(getLeaves(),getPathLabel().length());
  }

  /** Returns the count of this Node: how many positions start with this path label. If only 1 sequence was entered, then this should be equals to getLeaves().length.
   *
   * @return how many times the path label exist in the sequences in the tree.
   */
  public int getCount(){return	getCount(getLeaves());}

  /** Counts the number of positions in the list of leaves below. If the positions are within 'length' of each others, only 1 of the overlaps is counted.
   */
  public int getCountNoOverlap(){return getCountNoOverlap(getLeaves(),getPathLabel().length());}

  /**
   * Returns the count of this Node: how many positions start with this path label. If only 1 sequence was entered, then this should be equals to getLeaves().length.
   *
   * @param leaves the list of leaves to count from
   * @return how many times this subsequence happens in the tree
   */
  static public int getCount(Leaf[] leaves){
    int count=0;
    int i;
    for (i=0;i<leaves.length;i++)
      count+=leaves[i].getNoPositions();
    return count;
  }

  /** Counts the number of positions in the list of leaves below. If the positions are within 'length' of each others, only 1 of the overlaps is counted.
   *
   * @param leaves the list of leaves to count from
   * @param length how far apart the positions need to be to avoid an overlap
   * @return the number of leaves that happen in the tree without overlaps.
   */
  public static int getCountNoOverlap(Leaf[] leaves, int length){
    return getLeavesNoOverlap(leaves,length).size();
  }

  /** Gets the of positions in the list of leaves below. If the positions are within 'length' of each others, only 1 of the overlaps is counted.
   *
   * @param leaves the list of leaves to count from
   * @param length how far apart the positions need to be to avoid an overlap
   * @return the leaves that happen in the tree without overlaps.
   */
  public static TreeSet getLeavesNoOverlap(Leaf[] leaves, int length){
    int current;
    int i,j;
    int lastOne;
    int leafPositions[][];
    TreeSet positions = new TreeSet();
    Iterator iterator;

    //get all positions
    for (i=0;i<leaves.length;i++){
      leafPositions=leaves[i].getLeafPositions();
      for (j=0;j<leaves[i].getNoPositions();j++)
	positions.add(new Integer(leafPositions[0][j]));
    }

    iterator= positions.iterator();

    //get the first one out to initialize things.
    if (iterator.hasNext()){
      lastOne=((Integer)iterator.next()).intValue();
    }
    else
      return null;

    while (iterator.hasNext()){
      current=((Integer)iterator.next()).intValue();
      //check for overlap
      if (lastOne+length<=current){
	lastOne=current;
      }
      else
	iterator.remove();
    }
    return positions;
  }

}

class Leaf extends Node{
  final boolean isLeaf = true;
  /** The positions of each suffix that end in this leaf
   * leafPositions[0][i] is the starting position of the ith suffix ending at this leaf.
   * leafPositions[1][i] is the ending position of the ith suffix ending at this leaf.
   */
  int[][] leafPositions;
  int noPositions;

  public Leaf(UkkonenSuffixTree tree, Edge topEdge, int position) {
    super(tree, topEdge);
    leafPositions = new int[2][];
    leafPositions[0] = new int[getTree().getNoSequences()];
    leafPositions[1] = new int[getTree().getNoSequences()];
    noPositions=1;
    leafPositions[0][0]=position;
    leafPositions[1][0]=UkkonenSuffixTree.TO_A_LEAF;
  }


  /** Returns the suffix of the first position that ends at this leaf.
   * @return The string of the path from the root to this leaf.
   */
  public String getPathLabel(){
    return tree.getSequences().substring(leafPositions[0][0], leafPositions[1][0]);
  }

  /** returns the number of positions ending at this leaf.
   * @return the number of positions ending at this leaf.
   */
  public int getNoPositions(){return noPositions;}

  /** Doubles the size of the arrays to add more positions to the leaf.
   */
  private void makeMorePositions(){
    int[] newStart = new int[noPositions*2];
    int[] newStop = new int[noPositions*2];

    System.arraycopy(leafPositions[0], 0, newStart, 0, leafPositions[0].length);
    System.arraycopy(leafPositions[1], 0, newStop, 0, leafPositions[1].length);
    leafPositions[0]=newStart;
    leafPositions[1]=newStop;
  }

  /** Indicates that an additional sequence ends at this leaf.
   * @param startPos an <code>int</code> value
   */
  public void addPosition(int startPos){
    if (leafPositions[0].length<=noPositions)
      makeMorePositions();
    leafPositions[0][noPositions]=startPos;
    leafPositions[1][noPositions]=UkkonenSuffixTree.TO_A_LEAF;
    noPositions++;
  }

  /** Goes through all of the positions and go terminate all unfinalized ones.
   * It also makes an the end of the edge coming in absolute.
   */
  public void finalizePositions(int endOfSequence){
    int i;
    if (topEdge.getStop()==endOfSequence){
      topEdge.setStop(endOfSequence);
      for (i=0;i<noPositions;i++)
	if (leafPositions[1][i]==UkkonenSuffixTree.TO_A_LEAF)
	  leafPositions[1][i]=endOfSequence;
    }
  }

  public int[][] getLeafPositions(){return leafPositions;}
}

class InternalNode extends Node{
  boolean isRoot;
  final boolean isLeaf = false;
  HashMap children;
  private int pathLabelStart;
  private int pathLabelStop;

  /** Constructor for the root
   * @param tree the SuffixTree it belongs to
   */
  public InternalNode (UkkonenSuffixTree tree){
    this(tree, null);
    isRoot = true;
    pathLabelStart = 0;
    pathLabelStop = 0;
  }

  /** Constructor for a non-root internal node
   * @param tree the suffix tree it belongs to.
   * @param topEdge the topEdge of the node.
   */
  public InternalNode (UkkonenSuffixTree tree, Edge topEdge){
    super(tree, topEdge);
    children = new HashMap();
    isRoot = false;
  }

  /** InternalNode constructor that sets the pathLabel
   *
   * @param tree the suffix tree it belongs to
   * @param topEdge the edge leading to this node
   * @param start the position of the start of the path label in the sequences
   * @param stop the position of the send of the path label in the sequences
   */
  public InternalNode (UkkonenSuffixTree tree, Edge topEdge, int start, int stop){
    this(tree,topEdge);
    setPathLabel(start,stop);
  }

  public void setPathLabel(int start, int stop){
    pathLabelStart = start;
    pathLabelStop = stop;
  }

  public String getPathLabel(){
    //this should only happen if we are at the root
    if (pathLabelStart==-1){
      return super.getPathLabel();
    }

    return tree.getSequences().substring(pathLabelStart,pathLabelStop);
  }

  /** returns the edge coming down of this node that has starts with a specific character
   * @param branch the first char of the edge
   * @return the edge that starts with this character, or null is none exist.
   */
  public Edge getEdge (Character branch){
    return (Edge)children.get(branch);
  }

  /** returns the edge coming down of this node that has starts with a specific character
   * @param branch the first char of the edge
   * @return the edge that starts with this character, or null is none exist.
   */
  public Edge getEdge (char branch){return getEdge(new Character(branch)); }


  /** Add a leaf to an internal node
   * @param branch a <code>Character</code> value
   * @param i an <code>int</code> value
   * @return a <code>Leaf</code> value
   */
  public Leaf addLeafEdge (Character branch, int leafStart, int edgeStart){
    Leaf leaf = new Leaf(tree, null, leafStart);
    Edge edge =  new Edge(this, leaf, edgeStart,UkkonenSuffixTree.TO_A_LEAF);
    leaf.setTopEdge(edge);
    children.put(branch, edge);
    return leaf;
  }

  public void addEdge (Edge edge){
    children.put(new Character(tree.getSequences().charAt(edge.getStart())),edge);
  }
}

/**
 * An edge in the SuffixTree.
 *
 * The position labels work in the same way as String.substring: the starting position is inclusive but the ending position is exlusive.
 *
 * @author Francois Pepin
 * @version $Revision$
 */
class Edge implements TreePart{
  private InternalNode parent;
  private Node child;
  private int labelStart;
  private int labelStop;

  public Edge(InternalNode parent, Node child, int labelStart, int labelStop){
    this.parent = parent;
    this.child = child;
    this.labelStart = labelStart;
    this.labelStop = labelStop;
  }

  /** Apply a rule 3, split an edge to go and put a node in the middle so you can apply a rule 2 from it (to put an additional leaf from a node):
   *
   * @param splittingPos The position of the the character that's causing the split (first char of the newEdge)
   * @param splittingChar The character causing the split
   * @param splittedPos the position of the character that is the first char in the downEdge (should be between labelStart and labelStop)
   * @return the leaf that just got created.
   */
  public Leaf splitEdge(int splittingPos, char splittingChar, int splittedPos, int suffixStart){
    InternalNode middleNode = new InternalNode (parent.getTree(), this, suffixStart,splittingPos);
    Edge downEdge = new Edge(middleNode, child, splittedPos, labelStop);

    middleNode.addEdge(downEdge);
    //change current edge to connect to newNode
    labelStop = splittedPos;
    child.setTopEdge(downEdge);
    child = middleNode;
    return middleNode.addLeafEdge(new Character(splittingChar), suffixStart, splittingPos);
  }

  public String getEdgeLabel(String sequences){
    return sequences.substring(labelStart, labelStop);
  }

  public Node getChild(){return child;}

  public InternalNode getParent(){return parent;}

  public int getStart(){return labelStart;}

  /** Use this to get the end of the label of this edge. Do not access labelStop directly, as you need to see if it's a leaf of not before knowing what the current value is.
   * If the child node is a leaf, then it returns the value of the current phase.
   *
   * @return the end position of the current label.
   */
  public int getStop(){
    return  labelStop==UkkonenSuffixTree.TO_A_LEAF?
      parent.getTree().getE():
      labelStop;
  }

  /** Sets the end of an edge. Used with leaves when there are multiple sequences.
   * @param labelStop the end of an edge.
   */
  public void setStop(int labelStop){this.labelStop = labelStop;}

  public int length(){return getStop() -labelStart; }

  /** Gets the label of the edge
   *
   * @return the edge label
   */
  String getEdgeLabel(){
    return getParent().getTree().getSequences().substring(labelStart, getStop());
  }
}

interface TreePart{}

/** Just a way to say which are the sequences that we have now.
 *
 * @author Francois Pepin
 * @version 1.0
 */

class Sequence implements CharSequence {

  String name;
  CharSequence seq;
  int start;
  int end;

  public Sequence(String name, CharSequence seq){
    this.name = name;
    this.seq = seq;
  }

  public Sequence(String x, int x1, int x2){}

  public boolean contains(int middle){
    return (start<=middle&&middle<end);
  }


  // implementation of java.lang.CharSequence interface

  /** Returns a string representation of this sequence
   *
   * @return A string representation of this sequence
   */
  public String toString() {
    return seq.toString();
  }

  /** The length of this sequence
   *
   * @return the length of this sequence
   */
  public int length() {
    return seq.length();
  }

  /** Returns the character at a given index (0 being the first character)
   *
   * @param index
   * @return <description>
   */
  public char charAt(int index) {
    return seq.charAt(index);
  }

  /** Returns a new character sequence that is a subsequence of this sequence. The subsequence starts with the character at the specified index and ends with the character at index end - 1. The length of the returned sequence is end - start, so if start == end then an empty sequence is returned.
   *
   * @param int start - the start index, inclusive
   * @param in end - the end index, exclusive
   * @return the specified subsequence
   */
  public CharSequence subSequence(int start, int end){
    return seq.subSequence(start, end);
  }
}

/**
 * Puts all the sequences in a data-structure that can be accessed either as a list of sequences (Vector) or 1 continuous sequence;
 *
 * Note that size() refers to the number of sequences (from Vector) and length() refers to the length of all sequences (from CharSequence)
 *
 * @author Francois Pepin
 * @version $Revision$
 */
class SequenceList extends Vector implements CharSequence{

  //public final char EOS = UkkonenSuffixTree.DEFAULT_TERM_CHAR;

  // implementation of java.lang.CharSequence interface

  /** Overwrites the Vector add method to make sure we're only adding CharSequences
   *
   * @param o1 an <code>Object</code> value
   */
  public boolean add(Object o1){
    return super.add((CharSequence)o1);
  }

  /** returns the sequence that contains a certain position if we take the sequences linearly
   *
   * @param position position we're interested in
   * @return the <code>CharSequence</code> containing this position;
   */
  public CharSequence getSequence(int position){
    int i;
    CharSequence current;
    for (i=0;i<size();i++){
      current = (CharSequence)elementAt(i);
      if (current.length()>position)
	return current;
      else
	position-=current.length();
    }
    throw new IndexOutOfBoundsException();
  }

  /** Returns a string representation of all sequences present, with an EOS between each sequence;
   *
   * @return <description>
   */
  public String toString() {
    int i;
    StringBuffer answer = new StringBuffer();
    for (i=0;i<size();i++){
      answer.append((CharSequence)elementAt(i));
    }

    return answer.toString();
  }

  /** returns the length of all sequences
   *
   * @return the length of all sequences
   */
  public int length() {
    int i;
    int length=0;
    for (i=0;i<size();i++){
      length+= ((CharSequence)elementAt(i)).length();
    }
    return length;
  }


/** Gets the character at given index.
 *
 * @param index the idnex of the given character.
 * @return the specified character
 */
  public char charAt(int index) {
    int i;
    CharSequence current;
    for (i=0;i<length();i++){
      current = (CharSequence)elementAt(i);
      if (current.length()>index)
	return current.charAt(index);
      else
	index-=current.length();
    }
    throw new IndexOutOfBoundsException();
  }


  /** Returns a new character sequence that is a subsequence of this sequence. The subsequence starts with the character at the specified index and ends with the character at index end - 1. The length of the returned sequence is end - start, so if start == end then an empty sequence is returned.
   *
   * It throws an IndexOutOfBoundsException if the start and end cover 2 different sequences, if either is negative or greater than the total sequence length.
   *
   * @param int start - the start index, inclusive
   * @param in end - the end index, exclusive
   * @return the specified subsequence
   */
  public CharSequence subSequence(int start, int end){
    int i;
    CharSequence current;
    for (i=0;i<length();i++){
      current = (CharSequence)elementAt(i);
      if (current.length()>start)
	return current.subSequence(start, end);
      else{
	start-=current.length();
	end  -=current.length();
      }
    }
    throw new IndexOutOfBoundsException();
  }
}
