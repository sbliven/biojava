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
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;

/**
 * Base-class for listeners that pass filtered events onto another listener.
 *
 * @author Thomas Down
 * @since 1.1
 */

public class SeqIOTools  {
    /**
     * This can't be instantiated.
     */

    private SeqIOTools() {
    }

    private static SymbolParser getDNAParser() {
	try {
	    return DNATools.getDNA().getParser("token");
	} catch (BioException ex) {
	    throw new BioError(ex, "Assertion failing: Couldn't get DNA token parser");
	}
    }

    private static SymbolParser getProteinParser() {
	try {
	    return DNATools.getDNA().getParser("token");
	} catch (BioException ex) {
	    throw new BioError(ex, "Assertion failing: Couldn't get PROTEIN token parser");
	}
    }

    private static SequenceBuilderFactory _emblBuilderFactory;

    /** 
     * Get a default SequenceBuilderFactory for handling EMBL
     * files.
     */

    public static SequenceBuilderFactory getEmblBuilderFactory() {
	if (_emblBuilderFactory == null) {
	    _emblBuilderFactory = new EmblProcessor.Factory(SimpleSequenceBuilder.FACTORY);
	}
	return _emblBuilderFactory;
    }

    /**
     * Iterate over the sequences in an EMBL-format stream.
     */

    public static SequenceIterator readEmbl(BufferedReader br) {
	return new StreamReader(br,
				new EmblLikeFormat(),
				getDNAParser(),
				getEmblBuilderFactory());
    }

    private static SequenceBuilderFactory _genbankBuilderFactory;

    /** 
     * Get a default SequenceBuilderFactory for handling GenBank
     * files.
     */

    public static SequenceBuilderFactory getGenbankBuilderFactory() {
	if (_genbankBuilderFactory == null) {
	    _genbankBuilderFactory = new GenbankProcessor.Factory(SimpleSequenceBuilder.FACTORY);
	}
	return _genbankBuilderFactory;
    }

    /**
     * Iterate over the sequences in an GenBAnk-format stream.
     */

    public static SequenceIterator readGenbank(BufferedReader br) {
	return new StreamReader(br,
				new GenbankFormat(),
				getDNAParser(),
				getGenbankBuilderFactory());
    }

    private static SequenceBuilderFactory _fastaBuilderFactory;

    /** 
     * Get a default SequenceBuilderFactory for handling FASTA
     * files.
     */

    public static SequenceBuilderFactory getFastaBuilderFactory() {
	if (_fastaBuilderFactory == null) {
	    _fastaBuilderFactory = new FastaDescriptionLineParser.Factory(SimpleSequenceBuilder.FACTORY);
	}
	return _fastaBuilderFactory;
    }

    /**
     * Iterate over the sequences in an FASTA-format stream of DNA sequences.
     */

    public static SequenceIterator readFastaDNA(BufferedReader br) {
	return new StreamReader(br,
				new FastaFormat(),
				getDNAParser(),
				getFastaBuilderFactory());
    }

    /**
     * Iterate over the sequences in an FASTA-format stream of Protein sequences.
     */

    public static SequenceIterator readFastaProtein(BufferedReader br) {
	return new StreamReader(br,
				new FastaFormat(),
				getProteinParser(),
				getFastaBuilderFactory());
    }
}
