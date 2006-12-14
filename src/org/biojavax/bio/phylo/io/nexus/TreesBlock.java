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
		 * @param rooted
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
			writer.write(NexusFileParser.NEW_LINE);
		}
		writer.write(" TRANSLATE" + NexusFileParser.NEW_LINE);
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
			writer.write(NexusFileParser.NEW_LINE);
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
			writer.write(";" + NexusFileParser.NEW_LINE);
		}
	}

}
