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

package CircLocTest;

import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;

/**
 * This program was developed to test the CircularLocation class. Most of the
 * functionality is contained in the TestFrame class which also creates and
 * displays the test gui. The program can be used to test several of the
 * methods of CircularLocation. Any abberations should be reported to the
 * biojava list or to the author (mark_s@sanger.otago.ac.nz).
 *
 * The program also demonstrates how CircularLocations can interact with
 * other locations (specifically RangeLocations) and some of the casting tricks
 * that are required to make this work. Ideally all locations on a
 * CircularSequence should be of a circular type however it is conceivable that
 * the ideal may not be realized in all situations.
 */
public class CircularLocationTester {
  boolean packFrame = false;

  //Construct the application
  public CircularLocationTester() {
    TestFrame frame = new TestFrame();
    //Validate frames that have preset sizes
    //Pack frames that have useful preferred size info, e.g. from their layout
    if (packFrame) {
      frame.pack();
    }
    else {
      frame.validate();
    }
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.setVisible(true);
  }

  //Main method
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(new MetalLookAndFeel());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    new CircularLocationTester();
  }
}

