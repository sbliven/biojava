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

import org.biojava.bio.seq.io.ParseException;

/**
 * Builds Nexus taxa blocks.
 * 
 * @author Richard Holland
 * @author Tobias Thierer
 * @author Jim Balhoff
 * @since 1.6
 */
public class TaxaBlockBuilder extends NexusBlockBuilder.Abstract implements TaxaBlockListener {

	private TaxaBlock block;
	
	private boolean beforeFirstSemi = true;
	private boolean beforeDim = true;
	private boolean beforeLab = false;
	private boolean afterLab = false;
	
	public void addTaxLabel(final String taxLabel) throws ParseException {
		this.block.addTaxLabel(taxLabel);
	}

	public void setDimensionsNTax(final int dimensionsNTax) {
		this.block.setDimensionsNTax(dimensionsNTax);
	}

	protected void addComment(final NexusComment comment) {
		if (this.beforeDim)
			this.block.addPreDimensionComment(comment);
		else if (this.beforeLab)
			this.block.addPreLabelComment(comment);
		else if (this.afterLab)
			this.block.addPostLabelComment(comment);
	}

	protected NexusBlock startBlockObject() {
		this.block = new TaxaBlock();
		return this.block;
	}

	public void endBlock() {
		// Don't care.
	}

	public void endTokenGroup() {
		// Ignore the first one we see.
		if (this.beforeFirstSemi) {
			this.beforeFirstSemi = false;
		}
		else if (this.beforeDim) {
			this.beforeDim = false;
			this.beforeLab = true;
		} else if (this.beforeLab) {
			this.beforeLab = false;
			this.afterLab = true;
		}
	}

}
