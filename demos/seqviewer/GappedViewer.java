package seqviewer;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.SimpleGappedSequence;
import org.biojava.bio.Annotation;
import org.biojava.bio.gui.sequence.*;
import org.biojava.bio.symbol.RangeLocation;

import javax.swing.*;
import java.awt.*;

/**
 * --class description--
 *
 * @author IHGadmin
 */
public class GappedViewer {
  public static void main(String[] args)
  throws Throwable {
    // create a sequence, add some features and then gap it
    Sequence ourSeq = SequenceTools.createSequence(
            DNATools.createDNA("atcgtacgtatgtatcagtcagattcgtagtcatcgtatctgctga"),
            "aseq",
            "A sequence",
            Annotation.EMPTY_ANNOTATION);
    Feature.Template ftempl = new Feature.Template();
    ftempl.source = "hand_made";
    ftempl.type = "AFeature";
    ftempl.annotation = Annotation.EMPTY_ANNOTATION;

    ftempl.location = new RangeLocation(3, 20);
    ourSeq.createFeature(ftempl);

    ftempl.location = new RangeLocation(35, 44);
    ourSeq.createFeature(ftempl);

    ftempl.location = new RangeLocation(23, 30);
    ourSeq.createFeature(ftempl);

    GappedSequence gSeq = new SimpleGappedSequence(ourSeq);
    gSeq.addGapsInSource(27, 10);

    // create a renderer with a simple feature renderer, a symbol renderer and
    // a ruler and add all of these to a mlr
    SequenceRenderer symRend = new SymbolSequenceRenderer();
    SequenceRenderer ruler = new RulerRenderer();
    SequenceRenderer featRend = new FeatureBlockSequenceRenderer(
            new BasicFeatureRenderer());
    MultiLineRenderer mlr = new MultiLineRenderer();
    mlr.addRenderer(featRend);
    mlr.addRenderer(symRend);
    mlr.addRenderer(ruler);

    // now put this twice into our real renderer, once under a gapped renderer
    // and once directly
    MultiLineRenderer mainRend = new MultiLineRenderer();
    mainRend.addRenderer(mlr);
    mainRend.addRenderer(new GappedRenderer(mlr));

    // we will display this in a window
    JFrame frame = new JFrame("Gapped Viewer Demo");
    frame.setSize(600, 400);

    TranslatedSequencePanel sp = new TranslatedSequencePanel();
    sp.setSequence(gSeq);
    sp.setRenderer(mainRend);
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(sp, BorderLayout.CENTER);

    frame.setVisible(true);
  }
}
