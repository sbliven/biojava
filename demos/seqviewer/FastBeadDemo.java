
package seqviewer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
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
 * <code>FastBeadDemo</code> is just the same as
 * <code>BeadDemo</code>, except that it uses
 * <code>TranslatedSequencePanel</code> as its
 * <code>SequenceRenderContext</code>. If you run the two side-by-side
 * you should see the speed difference.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class FastBeadDemo
{
    private static final int INITIAL_SCALE = 30;

    private JFrame     frame;
    private JButton    horiz;
    private JButton    vert;
    private JLabel     scaleLabel;
    private JSlider    scale;

    private static JScrollBar              bar;
    private static TranslatedSequencePanel seqPanel;

    public FastBeadDemo()
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
        scale      = new JSlider(SwingConstants.HORIZONTAL, 1, 100, INITIAL_SCALE);

	controlBox.add(Box.createHorizontalGlue());
	controlBox.add(horiz);
	controlBox.add(Box.createHorizontalStrut(10));
	controlBox.add(vert);
	controlBox.add(Box.createHorizontalGlue());
	controlBox.add(scaleLabel);
	controlBox.add(Box.createHorizontalStrut(5));
	controlBox.add(scale);
	controlBox.add(Box.createHorizontalGlue());

        seqPanel.setBorder(BorderFactory.createLineBorder(Color.black));

	frame.getContentPane().add(controlBox, BorderLayout.NORTH);
	frame.getContentPane().add(seqPanel,   BorderLayout.CENTER);

        bar = new JScrollBar(SwingConstants.HORIZONTAL);
        frame.getContentPane().add(bar, BorderLayout.SOUTH);
        bar.addAdjustmentListener(new BarListener());

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
            java.lang.System.err.println("Usage: FastBeadDemo <EMBL file>");
            java.lang.System.exit(0);
        }

        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(argv[0]));
            SequenceIterator seqi = SeqIOTools.readEmbl(reader);
            
	    seqPanel = new TranslatedSequencePanel();

            if (seqi.hasNext())
            {
                Sequence seq = seqi.nextSequence();

		Feature source = (Feature)
		    seq.filter(new FeatureFilter.ByType("source"), false)
		    .features().next();
		seq.removeFeature(source);

                seqPanel.setSequence(seq);

		// Magic number stolen from EmblViewer
                seqPanel.setScale(Math.exp(-INITIAL_SCALE / 7.0) * 20.0);
                seqPanel.setDirection(TranslatedSequencePanel.HORIZONTAL);

                seqPanel.addSequenceViewerListener(new SequenceViewerListener()
                    {
                        public void mouseClicked(SequenceViewerEvent sve)
                        {
                            System.out.println(sve.getMouseEvent().getPoint() + "\t" + sve.getSource());
                            Object t = sve.getTarget();
                            for (Iterator ri = sve.getPath().iterator(); ri.hasNext();)
                            {
                                SequenceRenderer sr = (SequenceRenderer) ri.next();
                                System.out.println("\t" + sr);
                            }

                            if (t instanceof FeatureHolder)
                            {
                                FeatureHolder fh = (FeatureHolder) t;
                                for (Iterator fi = fh.features(); fi.hasNext();)
                                {
                                    Feature f = (Feature) fi.next();
                                    System.out.println("\t" + f.getType() + "\t: " + f.getLocation());
                                }
                            }
                        }

                        public void mousePressed(SequenceViewerEvent sve)
                        {
                            // System.err.println("sve press: " + sve);
                        }

                        public void mouseReleased(SequenceViewerEvent sve)
                        {
                            // System.err.println("sve release: " + sve);
                        }
                    });

		OptimizableFilter  cds = new FeatureFilter.ByType("CDS");
		OptimizableFilter mrna = new FeatureFilter.ByType("mRNA");

		OptimizableFilter  rep = new FeatureFilter.ByType("repeat_region");
		OptimizableFilter misc = new FeatureFilter.ByType("misc_feature");
		OptimizableFilter  rev =
		    new FeatureFilter.StrandFilter(StrandedFeature.NEGATIVE);

		// Green ellipse, Y-displacement 22
                EllipticalBeadRenderer erGreen =
		    new EllipticalBeadRenderer(10.0f, 21.0f,
					       Color.black, Color.green,
					       new BasicStroke(), 2.0f);
		// Red ellipse, Y-displacement 22
		EllipticalBeadRenderer erRed =
		    new EllipticalBeadRenderer(10.0f, 22.0f,
					       Color.black, Color.red,
					       new BasicStroke(), 2.0f);

		// White rectangle, no Y-displacement
		RectangularBeadRenderer rrWhite =
		    new RectangularBeadRenderer(10.0f, 0.0f,
						Color.black, Color.white,
						new BasicStroke());

		// Blue rectangle, Y-displacement 11
		RectangularBeadRenderer rrBlue =
		    new RectangularBeadRenderer(10.0f, 11.0f,
						Color.black, Color.blue,
						new BasicStroke());

		// Yellow rectangle, Y-displacement 11
		RectangularBeadRenderer rrYellow =
		    new RectangularBeadRenderer(10.0f, 11.0f,
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

	FastBeadDemo demo = new FastBeadDemo();

        bar.setValues(0, 10, 0, seqPanel.getSequence().length());
    }

    private class SliderListener implements ChangeListener
    {
	public void stateChanged(ChangeEvent ce)
	{
	    JSlider source = (JSlider) ce.getSource();
	    if (! source.getValueIsAdjusting())
	    {
		// val is between 1 and 100
		int val = source.getValue();
		double s = Math.exp(-val / 7.0) * 20;

		seqPanel.setScale(s);

                int seqVisible = seqPanel.getVisibleSymbolCount() -
                    seqPanel.getSymbolTranslation();
                int   seqTotal = seqPanel.getSequence().length();

                int extent = Math.min(seqVisible, seqTotal - 1);

                int barMin = bar.getMinimum();
                int barMax = bar.getMaximum();
                int barVal = bar.getValue();

                if ((extent + barVal) >= barMax)
                    barVal = Math.max(0, barMax - extent);

                bar.setValues(barVal, extent, 0, barMax);
	    }
	}
    }

    private class BarListener implements AdjustmentListener
    {
        public void adjustmentValueChanged(AdjustmentEvent ae)
        {
            JScrollBar source = (JScrollBar) ae.getAdjustable();

            int translation = source.getValue();

            seqPanel.setSymbolTranslation(translation);
        }
    }
}
