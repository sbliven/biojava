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
package org.biojavax.bio.phylo.io.nexus;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;

/**
 * Represents Nexus trees blocks.
 * 
 * @author Richard Holland
 * @author Tobias Thierer
 * @author Jim Balhoff
 * @since 1.6
 */
public class TreesBlock extends NexusBlock.Abstract {

	/**
	 * A constant representing the name of Trees blocks.
	 */
	public static final String TREES_BLOCK = "TREES";

	private Map translations = new LinkedHashMap();

	private List comments = new ArrayList();

	private Map trees = new LinkedHashMap();

	private UndirectedGraph<String, DefaultEdge> jgrapht =  new Pseudograph<String, DefaultEdge>(DefaultEdge.class);


	/**
	 * A simple representation of a Newick tree as a single string.
	 */
	public static class NewickTreeString {
		private String rootType;

		private String treeString;

		private boolean starred;

		/**
		 * Make the tree (un)rooted.
		 * 
		 * @param rootType
		 *            'U' for unrooted, 'R' for rooted, <tt>null</tt> for
		 *            unsure.
		 */
		public void setRootType(final String rootType) {
			this.rootType = rootType;
		}

		/**
		 * Set the Newick string describing the tree.
		 */
		public void setTreeString(final String treeString) {
			this.treeString = treeString;
		}

		/**
		 * Sets whether this tree has a star before it's name.
		 * 
		 * @param starred
		 *            <tt>true</tt> if it has one.
		 */
		public void setStarred(boolean starred) {
			this.starred = starred;
		}

		/**
		 * Tests whether this tree has a star before it's name.
		 * 
		 * @return starred <tt>true</tt> if it has one.
		 */
		public boolean isStarred() {
			return this.starred;
		}

		/**
		 * See if the tree is rooted.
		 * 
		 * @return 'U' for unrooted, 'R' for rooted, <tt>null</tt> for unsure.
		 */
		public String getRootType() {
			return this.rootType;
		}

		/**
		 * Get the Newick string describing the tree.
		 * 
		 * @return the tree string.
		 */
		public String getTreeString() {
			return this.treeString;
		}
	}

	/**
	 * Delegates to NexusBlock.Abstract constructor using TreesBlock.TREES_BLOCK
	 * as the name.
	 */
	public TreesBlock() {
		super(TreesBlock.TREES_BLOCK);
	}

	/**
	 * Add a translation.
	 * 
	 * @param label
	 *            the label to add.
	 * @param taxa
	 *            the taxa name this label will represent.
	 */
	public void addTranslation(final String label, final String taxa) {
		this.translations.put(label, taxa);
	}

	/**
	 * Removes the given translation.
	 * 
	 * @param label
	 *            the label to remove.
	 */
	public void removeTranslation(final String label) {
		this.translations.remove(label);
	}

	/**
	 * Checks to see if we contain the given translation.
	 * 
	 * @param label
	 *            the label to check for.
	 * @return <tt>true</tt> if we already contain it.
	 */
	public boolean containsTranslation(final String label) {
		return this.translations.containsKey(label);
	}

	/**
	 * Get the translations added so far.
	 * 
	 * @return the translations added so far.
	 */
	public Map getTranslations() {
		return this.translations;
	}

	/**
	 * Adds a tree.
	 * 
	 * @param label
	 *            the label to give the tree.
	 * @param tree
	 *            the tree to add.
	 */
	public void addTree(final String label, final NewickTreeString tree) {
		this.trees.put(label, tree);
	}

	/**
	 * Removes a tree.
	 * 
	 * @param label
	 *            the label to remove.
	 */
	public void removeTree(final String label) {
		this.trees.remove(label);
	}

	/**
	 * Checks to see if we contain the given tree.
	 * 
	 * @param label
	 *            the label to check for.
	 * @return <tt>true</tt> if we already contain it.
	 */
	public boolean containsTree(final String label) {
		return this.trees.containsKey(label);
	}

	/**
	 * Returns all trees.
	 * 
	 * @return all the selected trees.
	 */
	public Map getTrees() {
		return this.trees;
	}


	/**
	 * Returns a tree for given label
         */
     	public Object getTree(final String label) {
		return this.trees.get(label);
	}

	/**
	 * Add a tree, converting JGraphT to NewickString
        */

	public void addTree(final String label, UndirectedGraph<String, DefaultEdge> treegraph) {
	
		final NewickTreeString tree = new NewickTreeString();
		String temp = treegraph.toString();
		String [] tokens = null; 
		
		tokens = temp.split("\\[");             // extract the tree string part from JGraphT
		temp = tokens[2];
		tokens = temp.split("\\]");	
		temp = tokens[0];
		
		temp = temp.replace("{", "");
		temp = temp.replace("}", "");
		temp = temp.replace(" ", "");
		tokens = temp.split(",");               // parse all vertices and store it in the string array tokens
		temp = "";
	
		int len = tokens.length;
		for(int i = 0 ; i < len; i = i + 4){
			if( tokens[i].matches("p[0-9]") == false && tokens[i+3].matches("p[0-9]")== false){
				temp = "("+tokens[i]+ ", "+ tokens[i+3] + ")";
				for(int j = i +4; j < len; j++){

					if(tokens[j].equals(tokens[i+1]) && tokens[j].equals(tokens[i+2])){
						tokens[j] = temp;
					}
				}
			}
		}
		
		//System.out.println(temp);
		tree.setTreeString(temp);                           //set TreeString for the tree
		this.trees.put(label, tree);                        //add Tree to the Map with given label
	}
	
	
	/**
	 * gets the given Newick String tree by label, converts it to JGraphT, and returns it.
         */
	public UndirectedGraph<String, DefaultEdge> getTreeAsJGraphT(final String label) {
	
	String temp, v1, v2, v3;
	String [] tokens;
	int len = 0, p_index=0; 
	Object s_temp1, s_temp2, s_temp3;
	TreesBlock.NewickTreeString t = new TreesBlock.NewickTreeString();
	Stack stack = new Stack();
	
	t = (TreesBlock.NewickTreeString) this.trees.get(label);
	//System.out.println(t.getTreeString());

	temp = t.getTreeString();
	len = temp.length();                  
	tokens = temp.split("");             
	temp = "";
	
	for(int i = 0; i<= len; i++){
		if(tokens[i].equals("(")){
			p_index++;
		}
	}
	System.out.println(p_index);

	for(int i = 0; i <= len; i++)               // How to generate Tree Objects for JGraphT 
	{
		System.out.println(tokens[i]);
		if( tokens[i].equals(",") ){          // 1. push into stack if it is a word, or comma
			stack.push("p" + p_index);
			p_index--;
		}else if ( tokens[i].equals("(") || tokens[i].equals(" ") ){    // 2. ignore "(" or " "
			// ignore		
		}else if(tokens[i].equals(")")){	  // 3. pop 3 elements if you see ")"
								
			try{
				s_temp3 = stack.pop();    // 4. If you have two species, add them as vertices
				v3 = s_temp3.toString();  // 5. and push "p[number]" to stack  
									
				try{
					s_temp2 = stack.pop();
					v2 = s_temp2.toString();
			
					try{
						s_temp1 = stack.pop();
						v1 = s_temp1.toString();
									
						this.jgrapht.addVertex(v1);
						this.jgrapht.addVertex(v2);
						this.jgrapht.addVertex(v3);	
						this.jgrapht.addEdge(v1,v2);
						this.jgrapht.addEdge(v2,v3);		
									
						stack.push(v2);
					}catch(EmptyStackException e){}
				}catch(EmptyStackException e){}												
			}catch(EmptyStackException e){}
								
		}else{
			// if it is a letter, concatenate for the name, and push it to the stack
 								
			if(tokens[i].equals(" ")){
				//temp = temp + "_";
			}else if(tokens[i+1].equals("(") || tokens[i+1].equals(")") || tokens[i+1].equals(",")) {
				temp = temp + tokens[i];
				stack.push(temp);
				temp = "";
			}else{
				temp = temp + tokens[i];
			}
		}
	}			
			
	System.out.println(jgrapht.toString());

		return this.jgrapht;
	}

      
	/************************************************************************************************/
	/**
	 * Adds a comment.
	 * 
	 * @param comment
	 *            the comment to add.
	 */
	public void addComment(final NexusComment comment) {
		this.comments.add(comment);
	}

	/**
	 * Removes a comment.
	 * 
	 * @param comment
	 *            the comment to remove.
	 */
	public void removeComment(final NexusComment comment) {
		this.comments.remove(comment);
	}

	/**
	 * Returns all comments.
	 * 
	 * @return all the selected comments.
	 */
	public List getComments() {
		return this.comments;
	}

	protected void writeBlockContents(Writer writer) throws IOException {
		for (final Iterator i = this.comments.iterator(); i.hasNext();) {
			((NexusComment) i.next()).writeObject(writer);
			writer.write(NexusFileFormat.NEW_LINE);
		}
		writer.write(" TRANSLATE" + NexusFileFormat.NEW_LINE);
		for (final Iterator i = this.translations.entrySet().iterator(); i
				.hasNext();) {
			final Map.Entry entry = (Map.Entry) i.next();
			writer.write('\t');
			this.writeToken(writer, "" + entry.getKey());
			writer.write('\t');
			this.writeToken(writer, "" + entry.getValue());
			if (i.hasNext())
				writer.write(',');
			else
				writer.write(';');
			writer.write(NexusFileFormat.NEW_LINE);
		}
		for (final Iterator i = this.trees.entrySet().iterator(); i.hasNext();) {
			final Map.Entry entry = (Map.Entry) i.next();
			final NewickTreeString treeStr = (NewickTreeString) entry
					.getValue();
			writer.write(" TREE ");
			if (treeStr.isStarred())
				writer.write("* ");
			this.writeToken(writer, "" + entry.getKey());
			writer.write('=');
			if (treeStr.getRootType() != null)
				writer.write("[" + treeStr.getRootType() + "]");
			this.writeToken(writer, treeStr.getTreeString());
			writer.write(";" + NexusFileFormat.NEW_LINE);
		}
	}

}
