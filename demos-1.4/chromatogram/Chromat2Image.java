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

package chromatogram;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import org.biojava.bio.chromatogram.Chromatogram;
import org.biojava.bio.chromatogram.ChromatogramFactory;
import org.biojava.bio.chromatogram.UnsupportedChromatogramFormatException;
import org.biojava.bio.chromatogram.graphic.ChromatogramGraphic;

/**
 * A command-line utility for dumping a chromatogram to an image using 
 * <code>javax.imageio</code>.  Demos {@link ChromatogramFactory} and 
 * {@link ChromatogramGraphic}. Run it with no parameters to get help with 
 * command line options.  Requires JDK 1.4.
 */
public class Chromat2Image {
    private static final int OUT_HEIGHT = 240;
    private static final float OUT_HORIZ_SCALE = 2.0f;

    private static final String USAGE = 
        "USAGE:\n"
        + "Chromat2Image chromat-file output-image-file\n"
        + "  chromat-file\n"
        + "    - The chromatogram file from which to create the image\n"
        + "  output-image-file\n"
        + "    - The name of the file to which the chromatogram image\n"
        + "      will be written.  The format will be determined from\n"
        + "      the extension, which must be png or jpg (unless there\n"
        + "      are additional image writers on the classpath).  PNG\n"
        + "      is highly recommended over JPEG due to the discrete-\n"
        + "      tone nature of chromatogram images.\n";

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Invalid args.\n");
            System.err.println(USAGE);
            System.exit(1);
        }

        /* create Chromatogram object */

        File infile = new File(args[0]);
        if (!infile.canRead()) {
            System.err.println("Can't read " + infile);
            System.exit(1);
        }

        Chromatogram c = null;
        try {
            c = ChromatogramFactory.create(infile);
        } catch (UnsupportedChromatogramFormatException ucfe) {
            System.err.println("Unsupported chromatogram format (" + ucfe.getMessage());
            System.exit(1);
        } catch (IOException ioe) {
            System.err.println("Problem reading from " + infile + " (" + ioe.getMessage() + ")");
            System.exit(1);
        }

        /* find appropriate ImageWriter */
        
        String outExt = null;
        int lastdot = args[1].lastIndexOf('.');
        if (lastdot >= 0) {
            outExt = args[1].substring(lastdot+1);
        }
        
        if (outExt == null || outExt.length() == 0) {
            System.err.println("No extension on output file, so will use PNG format");
            outExt = "png";
        }

        Iterator writers = ImageIO.getImageWritersBySuffix(outExt);
        if (!writers.hasNext()) {
            System.err.println("No image writer found for suffix '" + outExt + "'");
            System.exit(1);
        }
        ImageWriter iw = (ImageWriter) writers.next();

        /* build output stream */
        
        File outfile = new File(args[1]);
        FileImageOutputStream out = null;
        try {
            out = new FileImageOutputStream(outfile);
        } catch (FileNotFoundException fnfe) {
            System.err.println("Can't write to " + outfile + " (" + fnfe.getMessage() + ")");
            System.exit(1);
        } catch (IOException ioe) {
            System.err.println("Problem writing to " + outfile + " (" + ioe.getMessage() + ")");
            System.exit(1);
        }

        /* create ChromatogramGraphic */
        
        ChromatogramGraphic gfx = new ChromatogramGraphic(c);
        gfx.setHeight(OUT_HEIGHT);
        gfx.setHorizontalScale(OUT_HORIZ_SCALE);
        // set some options that affect the output
        // turn off filled-in "callboxes"
        gfx.setOption(ChromatogramGraphic.Option.DRAW_CALL_A, Boolean.FALSE);
        gfx.setOption(ChromatogramGraphic.Option.DRAW_CALL_C, Boolean.FALSE);
        gfx.setOption(ChromatogramGraphic.Option.DRAW_CALL_G, Boolean.FALSE);
        gfx.setOption(ChromatogramGraphic.Option.DRAW_CALL_T, Boolean.FALSE);
        gfx.setOption(ChromatogramGraphic.Option.DRAW_CALL_OTHER, Boolean.FALSE);
        // this option controls whether each trace/callbox/etc is scaled/positioned
        // individually, or whether the scaling is done on all shapes at the level
        // of the graphics context
        // enabling this option is recommended for higher-quality output
        gfx.setOption(ChromatogramGraphic.Option.USE_PER_SHAPE_TRANSFORM, Boolean.TRUE);
        
        /* create output image */

        BufferedImage bi = new BufferedImage(
                                   gfx.getWidth(), 
                                   gfx.getHeight(), 
                                   BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setBackground(Color.white);
        g2.clearRect(0, 0, bi.getWidth(), bi.getHeight());

        /* draw chromatogram into image */

        // turn on AA for nicer output
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // the main event
        gfx.drawTo(g2);
        // work-around an OS X bug where sometimes the last Shape drawn
        // doesn't show up in the output
        g2.draw(new java.awt.Rectangle(-10, -10, 5, 5));

        /* write image out */

        iw.setOutput(out);
        try {
            iw.write(bi);
            out.close();
        } catch (IOException ioe) {
            System.err.println("Problem writing to " + outfile + " (" + ioe.getMessage() + ")");
            System.exit(1);
        }

        System.exit(0);
    }
}
