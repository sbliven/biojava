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
import java.util.List;

import org.biojava.bio.seq.io.ParseException;

/**
 * Represents Nexus taxa blocks.
 * 
 * @author Richard Holland
 * @author Tobias Thierer
 * @author Jim Balhoff
 * @since 1.6
 */
public class TaxaBlock extends NexusBlock.Abstract {

	/**
	 * A constant representing the name of Taxa blocks.
	 */
	public static final String TAXA_BLOCK = "TAXA";

	private int dimensionsNTax = 0;

	private List taxLabels = new ArrayList();

	private List preDimensionComments = new ArrayList();

	private List preLabelComments = new ArrayList();

	private List postLabelComments = new ArrayList();

	/**
	 * Delegates to NexusBlock.Abstract constructor using TaxaBlock.TAXA as the
	 * name.
	 */
	public TaxaBlock() {
		super(TaxaBlock.TAXA_BLOCK);
	}

	/**
	 * Set the NTAX value.
	 * 
	 * @param dimensionsNTax
	 *            the NTAX value.
	 */
	public void setDimensionsNTax(int dimensionsNTax) {
		this.dimensionsNTax = dimensionsNTax;
	}

	/**
	 * Get the NTAX value.
	 * 
	 * @return the NTAX value.
	 */
	public int getDimensionsNTax() {
		return this.dimensionsNTax;
	}

	/**
	 * Add a TAXLABEL. If it already exists, or is a number that refers to an
	 * index position that already exists, an exception is thrown.
	 * 
	 * @param taxLabel
	 *            the label to add.
	 * @throws ParseException
	 *             if the label cannot be added.
	 */
	public void addTaxLabel(final String taxLabel) throws ParseException {
		if (this.taxLabels.contains(taxLabel))
			throw new ParseException("Duplicate taxa label: " + taxLabel);
		else
			try {
				// Try it as a number to see if it refers to
				// position we already have.
				final int i = Integer.parseInt(taxLabel);
				if (i <= this.taxLabels.size() + 1)
					throw new ParseException("Taxa label " + i
							+ " refers to already extant taxa position");
			} catch (NumberFormatException e) {
				// It is not a number, so ignore.
			} catch (ParseException e) {
				// Throw it.
				throw e;
			}
		this.taxLabels.add(taxLabel);
	}

	/**
	 * Removes the given TAXLABEL.
	 * 
	 * @param taxLabel
	 *            the label to remove.
	 */
	public void removeTaxLabel(final String taxLabel) {
		this.taxLabels.remove(taxLabel);
	}

	/**
	 * Checks to see if we contain the given TAXLABEL.
	 * 
	 * @param taxLabel
	 *            the label to check for.
	 * @return <tt>true</tt> if we already contain it.
	 */
	public boolean containsTaxLabel(final String taxLabel) {
		if (this.taxLabels.contains(taxLabel))
			return true;
		else
			try {
				// Try it as a number to see if it refers to
				// position we already have.
				final int i = Integer.parseInt(taxLabel);
				if (i <= this.taxLabels.size() + 1)
					return true;
			} catch (NumberFormatException e) {
				// It is not a number, so ignore.
			}
		return false;
	}

	/**
	 * Get the TAXLABEL values added so far.
	 * 
	 * @return this labels so far.
	 */
	public List getTaxLabels() {
		return this.taxLabels;
	}

	/**
	 * Adds a comment before the DIMENSIONS tag.
	 * 
	 * @param comment
	 *            the comment to add.
	 */
	public void addPreDimensionComment(final NexusComment comment) {
		this.preDimensionComments.add(comment);
	}

	/**
	 * Removes a comment from before the DIMENSIONS tag.
	 * 
	 * @param comment
	 *            the comment to remove.
	 */
	public void removePreDimensionComment(final NexusComment comment) {
		this.preDimensionComments.remove(comment);
	}

	/**
	 * Returns all comments from before the DIMENSIONS tag.
	 * 
	 * @return all the selected comments.
	 */
	public List getPreDimensionComments() {
		return this.preDimensionComments;
	}

	/**
	 * Adds a comment before the TAXLABELS tag.
	 * 
	 * @param comment
	 *            the comment to add.
	 */
	public void addPreLabelComment(final NexusComment comment) {
		this.preLabelComments.add(comment);
	}

	/**
	 * Removes a comment from before the TAXLABELS tag.
	 * 
	 * @param comment
	 *            the comment to remove.
	 */
	public void removePreLabelComment(final NexusComment comment) {
		this.preLabelComments.remove(comment);
	}

	/**
	 * Returns all comments from before the TAXLABELS tag.
	 * 
	 * @return all the selected comments.
	 */
	public List getPreLabelComments() {
		return this.preLabelComments;
	}

	/**
	 * Adds a comment after the TAXLABELS tag.
	 * 
	 * @param comment
	 *            the comment to add.
	 */
	public void addPostLabelComment(final NexusComment comment) {
		this.postLabelComments.add(comment);
	}

	/**
	 * Removes a comment from after the TAXLABELS tag.
	 * 
	 * @param comment
	 *            the comment to remove.
	 */
	public void removePostLabelComment(final NexusComment comment) {
		this.postLabelComments.remove(comment);
	}

	/**
	 * Returns all comments from after the TAXLABELS tag.
	 * 
	 * @return all the selected comments.
	 */
	public List getPostLabelComments() {
		return this.postLabelComments;
	}

	// Write a list of comments one per line.
	private void writeComments(final Writer writer, final List comments)
			throws IOException {
		for (final Iterator i = comments.iterator(); i.hasNext();) {
			((NexusComment) i.next()).writeObject(writer);
			writer.write(NexusFileParser.NEW_LINE);
		}
	}

	protected void writeBlockContents(Writer writer) throws IOException {
		this.writeComments(writer, this.preDimensionComments);
		writer.write(" DIMENSIONS NTAX=" + this.dimensionsNTax + ";"
				+ NexusFileParser.NEW_LINE);
		this.writeComments(writer, this.preLabelComments);
		writer.write(" TAXLABELS");
		for (final Iterator i = this.taxLabels.iterator(); i.hasNext();) {
			writer.write(' ');
			this.writeToken(writer, (String) i.next());
		}
		writer.write(";" + NexusFileParser.NEW_LINE);
		this.writeComments(writer, this.postLabelComments);
	}

}
