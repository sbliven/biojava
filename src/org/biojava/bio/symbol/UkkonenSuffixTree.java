package org.biojava.bio.symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.io.SymbolTokenization;

/**
 * <p>
 * A suffix tree is an efficient method for encoding the frequencies
 * of motifs in a sequence.  They are sometimes used to quickly screen
 * for similar sequences.  For instance, all motifs of length up to
 * 2 in the sequence <code>AAGT</code> could be encoded as:
 * </p>
 *
 * <pre>
 * root(4)
 * |
 * A(2)--------G(1)-----T(1)
 * |           |
 * A(1)--G(1)  T(1)
 * </pre>
 *<p>
 * It supports addition of elements both as String and SymbolList. They should
 * not be mixed together. The strings are also terminated internally, so it is
 * possible to go and add more than one string to the suffix tree.
 *</p>
 *<p>
 * Some more work need to be done on how data should be generated from this
 * class. If you need something that's not in there, please e-mail the list at
 * biojava-dev@biojava.org and I'll add it in there.
 *<\p>
 * @author Francois Pepin
 * @version $Revision$
 */
public class UkkonenSuffixTree{

  public static final char DEFAULT_TERM_CHAR ='$';
  private char terminationChar;

  SuffixNode root;

  public static final int TO_A_LEAF = -1;
  private int e;

  private String sequences;

  private FiniteAlphabet alpha;

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


  /** Initializes a new <code>UkkonenSuffixTree</code> instance.
   */
  public UkkonenSuffixTree(){
    terminationChar = DEFAULT_TERM_CHAR;
    root = new SuffixNode();
    e=0;
    sequences = "";
    alpha=null;
  }

  public UkkonenSuffixTree(String seqs){
    this();
    addSequence(seqs, "unnamed", false);
  }

  public UkkonenSuffixTree(FiniteAlphabet alpha){
    this();
    this.alpha=alpha;
  }


  /** Makes a string out of a SymbolList, this string should only be used for
   * internal or testing purposes, as it will necessarily consist of visible
   * characters. It basically works by assigning a given character to every symbol.
   * @param list a <code>SymbolList</code> to be converted to a string.
   * @return a <code>String</code> representation of the SymbolList
   * @exception IllegalSymbolException if an error occurs
   */
  public String symbolListToString(SymbolList list) throws IllegalSymbolException{
    try {
      FiniteAlphabet alpha = (FiniteAlphabet)list.getAlphabet();
      SymbolTokenization tok = alpha.getTokenization("token");
      return tok.tokenizeSymbolList(list);
    } catch (BioException be) {
      throw new IllegalSymbolException(be.toString());
    }
  }

  /** Converts a string that came from symbolListToString back to a SymbolList.
   * This should only be called for internal or testing purposes. The behavior
   * is not defined if the string contains an unknown character.
   * @param string a <code>String</code> to be converted to SymbolList
   * @return a <code>SymbolList</code> representation of the string above.
   */
  public SymbolList stringToSymbolList(String string){
    try {
      SymbolTokenization tok = alpha.getTokenization("token");
      return new SimpleSymbolList(tok, string);
    } catch (BioException be) {
      System.err.println("No alphabet known to the SuffixTree.");
      return null;
    }
  }

  public void addSymbolList(SymbolList list, String name, boolean doNotTerminate) throws IllegalSymbolException{
    String seq = symbolListToString(list);
    
    if(!doNotTerminate)
      seq = seq+terminationChar;
    addPreppedSequence(seq);
  }

  

  /**
   * Add a sequence into the tree. If there are more sequences, they should be separated by a terminationChar ($ by default). If none exist, it is assumed that there is only 1 continuous sequence to be added.
   * @param seq the sequence/sequences to be added into the tree.
   * @param doNotTerminate whether we should terminate the sequence if it's non-terminated.
   */
  public void addSequence(String seq, String name, boolean doNotTerminate){
    int i;
    int start, end;
    ArrayList toBeAdded = new ArrayList();
    Iterator iterator;
    String subseq;

    if (seq==null||seq.length()==0)
      return;

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
      addPreppedSequence(subseq);
      i++;
    }
  }

  /** Add a single sequence into the tree.
   *
   * @param seq a <code>String</code> value
   */
  private void addPreppedSequence(String seq){
    int i, gammaStart;
    int j=0;
    SuffixNode oldNode=null, newNode;
    SuffixNode currentNode;
    boolean canLinkJump = false;

    //Puts i at the end of the previous sequences
    i = sequences.length();
    j=i;

    sequences= sequences.toString()+seq.toString();

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
	while (currentNode!=root&&currentNode.suffixLink==null&&canLinkJump)
	  currentNode = currentNode.parent;

	if (root==currentNode){
          currentNode=jumpTo(root,sequences,j,i+1);
        }else{
          if (canLinkJump)
            currentNode = currentNode.suffixLink;
          gammaStart = j+getPathLength(currentNode);

          currentNode = jumpTo(currentNode,sequences,gammaStart,i+1);
	}

	if (rule==1)
	  addPositionToLeaf(j, currentNode);
	if (rule==2)
          doRule2(currentNode,i,j);
	if (rule==3){
	  newNode=doRule3(currentNode,i,j);
	  currentNode=newNode;
	}

	if (rule==1||rule==4||rule==5)
	  currentNode = currentNode.parent;

	if (oldNode!=null){
	  if (currentNode.isTerminal())
	    currentNode=currentNode.parent;

	  oldNode.suffixLink=currentNode;

	}
	oldNode=newNode;
	newNode=null;

        if (rule==1||rule==4||rule==5){
	  oldNode=null;
          canLinkJump=false;
          break;
	}else
          canLinkJump=true;
        

      }//for phase i
    }//for extension j
    finishAddition();
  }
  
  /** This method is used to walk down the tree, from a given node. The
   * <code>rule</code> variable can be used to check where the walk
   *  stopped. Note that rule 3 means that the string used to walk down the
   *  tree does not match (which is a bit different from the construction
   *  where rule 3 implies that only the last character doesn't match.
   *<p>
   *  The String is encoded as a substring of a given source. This is done to
   *  avoid replicating the string. To send walk down the string
   *  <code>x</code> from the root, one would call walkTo(root,x,0,x.length()).
   *
   * @param starting the root of the subtree we're walking down form.
   * @param source a superstring that contains the string we're using to
   * walking down. source.subtring(from,to) should give the string we're
   * walking down from.
   * @param from the start position (inclusive) of the target string in the
   * source.
   * @param to the end position (exclusive) of the target string in the node.
   * @return a <code>SuffixNode</code> that the walk stopped at. If the walk
   * stopped inside an edge. (check the rule variable to see where it stopped).
   */
  public SuffixNode walkTo(SuffixNode starting, String source, int from, int to){
    SuffixNode currentNode;
    SuffixNode arrivedAt;
    String edgeLabel;
    
    
    currentNode=starting;
    arrivedAt=starting;
    while (from<to){
      arrivedAt=(SuffixNode)currentNode.children.get(
                                                     new Character(source.charAt(from)));
      if (arrivedAt==null){
	from=to;
	arrivedAt=currentNode;
	rule=2;
	break;
      }

      edgeLabel = getEdgeLabel(arrivedAt);
      if (edgeLabel.length()>=to-from){
        if (edgeLabel.equals(source.substring(from,to))){
          //rule 1 or 5, 
          if (arrivedAt.isTerminal())
            rule=1;
          else
            rule=5;
        }
        if (edgeLabel.substring(0, to-from).equals(source.substring(from,to)))
          rule=4;
        else
          rule=3;
        from=to;
      } else if (source.substring(from,from+edgeLabel.length())
                 .equals(edgeLabel)) {
        from+=edgeLabel.length();
        currentNode=arrivedAt;
      }
      
      else{
        rule=3;
        from=to;
      }
    }
    
    return arrivedAt;
    
  }
    
  
  
  
  /**
   * Just like walkTo, but faster when used during tree construction, as it
   * assumes that a mismatch can only occurs with the last character of the
   * target string.
   *
   * @param starting the root of the subtree we're walking down form.
   * @param source a superstring that contains the string we're using to
   * walking down. source.subtring(from,to) should give the string we're
   * walking down from.
   * @param from the start position (inclusive) of the target string in the
   * source.
   * @param to the end position (exclusive) of the target string in the node.
   * @return a <code>SuffixNode</code> that the walk stopped at. If the walk
   * stopped inside an edge. (check the rule variable to see where it
   * stopped).
   */
  public SuffixNode jumpTo(SuffixNode starting, String source, int from, int to){
    SuffixNode currentNode;
    SuffixNode arrivedAt;
    boolean canGoDown = true;
    int edgeLength;
    int original=from;
    SuffixNode originalNode=starting;
    int i=0;
    
    
    currentNode=starting;
    arrivedAt=starting;

    rule=0;

    if (from==to){
      rule=5;
      return starting;
    }


    while (canGoDown){
      //    if (source.substring(from, to).equals("CAGCG"))
      //  System.out.println(to+" here to "+source.substring(from, to)+" "+(i++));
      
      if (currentNode.isTerminal()){
      	System.out.println("ARRGH! at "+source.substring(original, to)+
                           "("+from+","+original+","+to+
                           ") from "+getLabel(originalNode));
        //Something truly awful happened if this line is ever reached.
        //This bug should be dead, but it it came back from the dead a couple
        //of times already.
      }
        
      arrivedAt=(SuffixNode)currentNode.children.get(
                                                     new Character(source.charAt(from)));
      if (arrivedAt==null){
	canGoDown=false;
	arrivedAt=currentNode;
	rule=2;
	break;
      }

      edgeLength = getEdgeLength(arrivedAt);
      if (edgeLength>=to-from){
	int before = currentNode.labelEnd+to-from+1;
	int after = getPathEnd(arrivedAt)-getEdgeLength(arrivedAt)+to-from-1;
	if (sequences.charAt(after)==
	    source.charAt(to-1)){
	  if (getEdgeLength(arrivedAt)==to-from){
	    if (arrivedAt.isTerminal())
	      rule=1;
	    else
	      rule=5;
	  }
	  else
	    rule=4;
	}
	else
	  rule=3;
	canGoDown=false;
	break;
      }
      from+=edgeLength;
      currentNode=arrivedAt;

    }//while canGoDOwn

    return arrivedAt;
  }

  /******************************************************************
   * Tree navigation methods
   ******************************************************************/

  protected int getEdgeLength(SuffixNode child){
    int parentLength, childLength;
    SuffixNode parent;
    if (child==root)
      return 0;
    parent=child.parent;
    parentLength = getPathLength(parent);
    childLength = getPathLength(child);
    if (childLength-parentLength<=0){
      
      System.out.println("negative length "+(childLength-parentLength));

      System.out.println(getLabel(child)+","+getLabel(parent));
    }
    
    return childLength-parentLength;
  }

  protected String getEdgeLabel(SuffixNode child){
    return sequences.substring(
                               child.labelStart+
                               (getPathLength(child)-getEdgeLength(child)),
                               (child.labelEnd==TO_A_LEAF)?
                               e:child.labelEnd);
  }
  
  
  protected int getPathLength(SuffixNode node){
    return getPathEnd(node)-node.labelStart;
  }

  protected int getPathEnd(SuffixNode node){
    return node.labelEnd==TO_A_LEAF?e:node.labelEnd;
  }

  protected String getLabel(SuffixNode node){
    if (node==root)
      return "root";
    else
      return sequences.substring(
                                 node.labelStart,
                                 (node.labelEnd==TO_A_LEAF)?e:node.labelEnd).toString();
  }


  protected ArrayList getAllNodes(SuffixNode root, ArrayList list, boolean leavesOnly){
    Iterator iterator;
    if (list==null)
      list= new ArrayList();
    if (!leavesOnly||(leavesOnly&&root.isTerminal()))
      list.add(root);
    if (!root.isTerminal()){
      iterator = root.children.values().iterator();
      while (iterator.hasNext())
	list=getAllNodes((SuffixNode)iterator.next(), list, leavesOnly);
    }

    return list;
  }

  public void printTree(){
    ArrayList allNodes = getAllNodes(root, null, false);
    for (int i=0;i<allNodes.size();i++){
      SuffixNode node = (SuffixNode)allNodes.get(i);
      if (node==root)
        System.out.println("root");
      else
        System.out.println("node "+i+" label "+getLabel(node)+" attached to "+getLabel(node.parent));
    }
  }
  
  
  public SuffixNode getRoot(){return root;}

  /******************************************************************
   * End Tree Navigation Methods
   ******************************************************************/


  /******************************************************************
   * Tree modification methods
   ******************************************************************/
  private void addPositionToLeaf(int pos, SuffixNode leaf){
    int[] moreLabels;
    if (leaf.additionalLabels==null)
      leaf.additionalLabels = new int[]{pos};
    else{
      moreLabels = new int[leaf.additionalLabels.length+1];
      System.arraycopy(leaf.additionalLabels, 0, moreLabels, 0, leaf.additionalLabels.length);
      moreLabels[moreLabels.length-1]=pos;
      leaf.additionalLabels=moreLabels;
    }

  }

  private void doRule2(SuffixNode parent, int splittingPos, int suffixStart){
    //int number = getAllNodes(root, null, false).size();
    SuffixNode leaf = new SuffixNode (parent, suffixStart);

    parent.children.put(new Character(sequences.charAt(splittingPos)), leaf);
    //System.out.println("rule 2: "+sequences.charAt(splittingPos)+" from "+getLabel(parent)+ " to "+getLabel(leaf));

  }

  private SuffixNode doRule3(SuffixNode child, int splittingPos, int suffixStart){
    //      return toBeSplit.splitEdge(endOfSubSeq, sequences.charAt(endOfSubSeq),
    //			       toBeSplit.getStart()+endOfSubSeq-rule3Position,
    //			       suffixStart);
    //int number = getAllNodes(root, null, false).size();
    SuffixNode parent = child.parent;
    SuffixNode middle= new SuffixNode(parent,suffixStart,splittingPos);
    Character x=new Character(
                              sequences.charAt(child.labelStart+getPathLength(child)-getEdgeLength(child)));

    //System.out.println(parent.children.get(x)==child);

    Character y=new Character(sequences.charAt(
                                               child.labelStart
                                               +getPathLength(child)-getEdgeLength(child)
                                               +getEdgeLength(middle)
                                               ));

    parent.children.remove(x);
    parent.children.put(x,middle);

    middle.children.put(y,child);
    child.parent=middle;
    //System.out.println("rule 3: "+sequences.charAt(splittingPos)+" between "+getLabel(parent)+" and "+getLabel(child) + " Addition made:"+(number==getAllNodes(root, null,false).size()-1));
    doRule2(middle,splittingPos,suffixStart);
    return middle;
  }

  private void finishAddition(){
    SuffixNode leaf;
    ArrayList leaves = getAllNodes(root, null, true);
    for (int i=0;i<leaves.size();i++){
      leaf = (SuffixNode)leaves.get(i);
      if (leaf.labelEnd==TO_A_LEAF)
	leaf.labelEnd=e;
    }

  }
  /******************************************************************
   * end Tree modification methods
   ******************************************************************/

  class SuffixNode {
    static final int A_LEAF=-1;
    SuffixNode parent;
    SuffixNode suffixLink;
    int labelStart, labelEnd;
    HashMap children;
    int[] additionalLabels;

    /** Creates a root
     */
    public SuffixNode(){
      parent=null;
      suffixLink=null;
      labelStart=0;
      labelEnd=0;
      children=new HashMap();
      additionalLabels=null;
    }

    /** creates a leaf
     * @param parent the parent node
     * @param position the starting value of the suffix
     */
    public SuffixNode(SuffixNode parent, int position){
      this();
      this.parent=parent;
      labelStart=position;
      labelEnd = A_LEAF;
      children=null;
      //checkParent(this);
    }

    /** creates an internal node
     * @param parent the parent of this node
     * @param labelStart the starting point of the path label
     * @param labelStop the ending point of the path label
     */
    public SuffixNode(SuffixNode parent, int labelStart, int labelStop){
      this();
      this.parent=parent;
      this.labelStart=labelStart;
      this.labelEnd=labelStop;
      //checkParent(this);
    }
    
    
    public boolean isTerminal(){return children==null;}
  }

  /** This is simply a debugging method to check that a node was created
   *  normally. it doesn't return anything but prints to System.err if a bad
   * addition was made.
   *
   * @param child a <code>SuffixNode</code> value
   */
  private void checkParent(SuffixNode child){
    SuffixNode parent=child.parent;
    String parentLabel=getLabel(parent);
    String label =getLabel(child);

    if (parentLabel.equals("root"))
        parentLabel="";
    
    if (parentLabel.length()>=label.length()||!parentLabel.equals(label.substring(0,parentLabel.length())))
    {
      System.err.println("bad addition on rule "+rule);
      System.err.println(parentLabel+" against "+ label);
      System.err.println("child ("+child.labelStart+","+((child.labelEnd==-1)?e:child.labelEnd)+")");
      System.err.println("parent ("+parent.labelStart+","+parent.labelEnd+")");

    }
  }

  public boolean subStringExists(String str)
  {
    walkTo(root, str, 0, str.length());
    return (rule==1||rule==4||rule==5);
  }
  
}
