
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.biojava.bio.gui.sequence.*;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.OptimizableFilter;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.bio.symbol.RangeLocation;

/**
 * <p><code>BeadDemo</code> demonstrates various some of the
 * <code>FeatureRender</code>s which extend
 * <code>AbstractBeadFeatureRenderer</code>. These render features in
 * various shapes, colours and positions, but all within the same
 * track of a <code>MultiLineRenderer</code>. They were really
 * designed to render protein domains, but may be used for any
 * features. By selecting various paints
 * (e.g. <code>TexturePaint</code>s made from loaded images), border
 * colours and shapes, a wide range of effects may be achieved.</p>
 *
 * <p>Note that while the <code>ZiggyFeatureRenderer</code>s occupy
 * one track of the <code>MultiLineRenderer</code> each,
 * <strong>all</strong> the features below are in the same track (but
 * have different Y-axis displacements). To dispay them all on the
 * same line, just set the displacement to 0.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class BeadDemo
{
    private static final int INITIAL_SCALE = 25;

    private JFrame      frame;
    private JButton     horiz;
    private JButton     vert;
    private JLabel      scaleLabel;
    private JSlider     scale;
    private JScrollPane seqScroll;

    private static SequencePanel seqPanel;

    public BeadDemo()
    {
        initComponents();
    }

    private void initComponents()
    {
	frame      = new JFrame();
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.getContentPane().setLayout(new BorderLayout());

	Box controlBox = Box.createHorizontalBox();

        horiz      = new JButton("Horizontal");
        vert       = new JButton("Vertical");
        scaleLabel = new JLabel("Scale");
        scale      = new JSlider(SwingConstants.HORIZONTAL, 1, 50, INITIAL_SCALE);
        seqScroll  = new JScrollPane(seqPanel);

	controlBox.add(Box.createHorizontalGlue());
	controlBox.add(horiz);
	controlBox.add(Box.createHorizontalStrut(10));
	controlBox.add(vert);
	controlBox.add(Box.createHorizontalGlue());
	controlBox.add(scaleLabel);
	controlBox.add(Box.createHorizontalStrut(5));
	controlBox.add(scale);
	controlBox.add(Box.createHorizontalGlue());

	frame.getContentPane().add(controlBox, BorderLayout.NORTH);
	frame.getContentPane().add(seqScroll, BorderLayout.CENTER);

        horiz.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent ae)
		{
		    horizActionPerformed(ae);
		}
	    });

        vert.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent ae)
		{
		    vertActionPerformed(ae);
		}
	    });

	scale.addChangeListener(new SliderListener());

	frame.setSize(800, 200);
	frame.setVisible(true);
    }

    private void vertActionPerformed(ActionEvent ae)
    {
        seqPanel.setDirection(SequencePanel.VERTICAL);
    }

    private void horizActionPerformed(ActionEvent ae)
    {
        seqPanel.setDirection(SequencePanel.HORIZONTAL);
    }

    public static void main(java.lang.String[] argv)
    {
        if (argv.length != 1)
        {
            java.lang.System.err.println("Usage: BeadDemo <EMBL file>");
            java.lang.System.exit(0);
        }

        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(argv[0]));
            SequenceIterator seqi = SeqIOTools.readEmbl(reader);
            
	    seqPanel = new SequencePanel();

            if (seqi.hasNext())
            {
                Sequence seq = seqi.nextSequence();

		Feature source = (Feature)
		    seq.filter(new FeatureFilter.ByType("source"), false)
		    .features().next();
		seq.removeFeature(source);

                seqPanel.setSequence(seq);
                seqPanel.setRange(new RangeLocation(1, seq.length()));

		// Magic number stolen from EmblViewer
                seqPanel.setScale(Math.exp(-INITIAL_SCALE / 7.0) * 20.0);
                seqPanel.setDirection(SequencePanel.HORIZONTAL);

		OptimizableFilter  cds = new FeatureFilter.ByType("CDS");
		OptimizableFilter mrna = new FeatureFilter.ByType("mRNA");

		OptimizableFilter  rep = new FeatureFilter.ByType("repeat_region");
		OptimizableFilter misc = new FeatureFilter.ByType("misc_feature");
		OptimizableFilter  rev =
		    new FeatureFilter.StrandFilter(StrandedFeature.NEGATIVE);

		// Green ellipse, Y-displacement 30
                EllipticalBeadRenderer erGreen =
		    new EllipticalBeadRenderer(10.0f, 20.0f,
					       Color.black, Color.green,
					       new BasicStroke(), 2.0f);
		// Red ellipse, Y-displacement 30
		EllipticalBeadRenderer erRed =
		    new EllipticalBeadRenderer(10.0f, 20.0f,
					       Color.black, Color.red,
					       new BasicStroke(), 2.0f);

		// White rectangle, no Y-displacement
		RectangularBeadRenderer rrWhite =
		    new RectangularBeadRenderer(10.0f, 0.0f,
						Color.black, Color.white,
						new BasicStroke());

		// Blue rectangle, Y-displacement 10
		RectangularBeadRenderer rrBlue =
		    new RectangularBeadRenderer(10.0f, 10.0f,
						Color.black, Color.blue,
						new BasicStroke());

		// Yellow rectangle, Y-displacement 10
		RectangularBeadRenderer rrYellow =
		    new RectangularBeadRenderer(10.0f, 10.0f,
						Color.black, Color.yellow,
						new BasicStroke());

		// Render these with standard ziggys
		FeatureRenderer ziggyCDS = new ZiggyFeatureRenderer();
		FeatureRenderer ziggyRNA = new ZiggyFeatureRenderer();

		// white renderer delegates misc_feature to green renderer
		rrWhite.setDelegateRenderer(misc, erGreen);
		// white renderer delegates repeat_region to blue renderer
		rrWhite.setDelegateRenderer(rep, rrBlue);

		// green renderer delegates features on reverse strand to red renderer
		erGreen.setDelegateRenderer(rev, erRed);
		// blue renderer delegates features on reverse strand to yellow renderer
		rrBlue.setDelegateRenderer(rev, rrYellow);

		FeatureBlockSequenceRenderer fbrCDS = new FeatureBlockSequenceRenderer();
                FeatureBlockSequenceRenderer fbrRNA = new FeatureBlockSequenceRenderer();
		FeatureBlockSequenceRenderer  other = new FeatureBlockSequenceRenderer();

		fbrCDS.setFeatureRenderer(ziggyCDS);
		fbrRNA.setFeatureRenderer(ziggyRNA);
                other.setFeatureRenderer(rrWhite);

                MultiLineRenderer multi = new MultiLineRenderer();
                multi.addRenderer(new FilteringRenderer(fbrCDS, cds, false));
		multi.addRenderer(new FilteringRenderer(fbrRNA, mrna, false));
		multi.addRenderer(other);
                multi.addRenderer(new SymbolSequenceRenderer());
                multi.addRenderer(new RulerRenderer());
                seqPanel.setRenderer(multi);
	    }
	}
        catch (java.lang.Throwable t)
        {
            t.printStackTrace();
        }

	BeadDemo demo = new BeadDemo();
    }

    private class SliderListener implements ChangeListener
    {
	public void stateChanged(ChangeEvent ce)
	{
	    JSlider source = (JSlider) ce.getSource();
	    if (! source.getValueIsAdjusting())
	    {
		// val is between 1 and 50
		int val = source.getValue();
		double s = Math.exp(-val / 7.0) * 20.0;

		seqPanel.setScale(s);
	    }
	}
    }
}
