package org.biojava.bio.seq.io;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import java.io.*;
import java.util.*;

/**
 * This class implements the AlignmentFormat interface to read FASTA alignments.
 * It is modeled after the MSFAlignmentFormat class.
 *
 * @author Nimesh Singh
 */

public class FastaAlignmentFormat implements AlignmentFormat {
    //Constants
    public static final int DNA = 1;
    public static final int PROTEIN = 2;

    public FastaAlignmentFormat() {
    }

    /**
     * Reads an alignment in FASTA format.
     */
    public Alignment read(BufferedReader br) {
        try {
            SequenceIterator seqs = null;
            br.mark(200);
            String line = br.readLine();
            line = br.readLine();
            br.reset();

            for (int i = 0; i < line.length(); i++) {
                if (Character.toUpperCase(line.charAt(i)) == 'F' ||
                    Character.toUpperCase(line.charAt(i)) == 'L' ||
                    Character.toUpperCase(line.charAt(i)) == 'I' ||
                    Character.toUpperCase(line.charAt(i)) == 'P' ||
                    Character.toUpperCase(line.charAt(i)) == 'Q' ||
                    Character.toUpperCase(line.charAt(i)) == 'E') {
                        seqs = SeqIOTools.readFastaProtein(br);
                }
            }
            if (seqs == null) {
                seqs = SeqIOTools.readFastaDNA(br);
            }

            Map seqMap = new HashMap();
            Sequence curSeq = null;
            while (seqs.hasNext()) {
                curSeq = seqs.nextSequence();
                seqMap.put(curSeq.getName(), curSeq);
            }

            return new SimpleAlignment(seqMap);
        } catch (Exception e) {
            System.err.println("FastaAlignmentFormat.read -- " + e.getMessage());
        }
        return null;
    }

    /**
     * Writes out the alignment to an FASTA file.
     */
    public void write(OutputStream os, Alignment align, int fileType) throws Exception {
        PrintStream out = new PrintStream(os);
        Iterator labels = align.getLabels().listIterator();
        Object curLabel = null;
        SymbolList curSeq = null;
        int lineWidth = 60;
        SymbolTokenization toke = null;

        if (fileType == DNA) {
            toke = DNATools.getDNA().getTokenization("token");
        }
        else if (fileType == PROTEIN) {
            toke = ProteinTools.getTAlphabet().getTokenization("token");
        }
        else {
            System.out.println("FastaAlignment.write -- File type not recognized.");
            return;
        }

        while (labels.hasNext()) {
            curLabel = labels.next();
            curSeq = align.symbolListForLabel(curLabel);

            out.print(">");
	    out.println(curLabel);

            for (int pos = 1; pos <= curSeq.length(); pos += lineWidth) {
	        int end = Math.min(pos + lineWidth - 1, curSeq.length());
	        out.println(curSeq.subStr(pos, end));
	    }
        }
    } //end write

    public void writeDna(OutputStream os, Alignment align) throws Exception {
        write(os, align, DNA);
    }

    public void writeProtein(OutputStream os, Alignment align) throws Exception {
        write(os, align, PROTEIN);
    }
}