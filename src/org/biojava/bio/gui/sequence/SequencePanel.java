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

package org.biojava.bio.gui.sequence;

import java.util.*;
import java.beans.*;
import java.io.Serializable;
import java.lang.reflect.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;

import org.biojava.utils.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.gui.sequence.*;

import java.util.List; // usefull trick to 'hide' javax.swing.List

/**
 * A panel that visualy displays a Sequence.
 * <P>
 * A SequencePanel can either display the sequence from left-to-right
 * (HORIZONTAL) or from top-to-bottom (VERTICAL). It has an associated scale
 * which is the number of pixels per symbol. It also has a lines property that
 * controls how to wrap the sequence off one end and onto the other.
 * <P>
 * Each line in the SequencePanel is broken down into a list of strips,
 * each rendered by an individual SequenceRenderer object.
 * You could add a SequenceRenderer that draws on genes, another that
 * draws repeats and another that prints out the DNA sequence. They are
 * responsible for rendering their view of the sequence in the place that the
 * SequencePanel positions them.  
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */
public class SequencePanel extends JComponent implements SwingConstants, SequenceRenderContext {
  private SymbolList sequence;
  private int direction;
  private double scale;
  private int lines;
  private int spacer; 
  
  private SequenceRenderContext.Border leadingBorder;
  private SequenceRenderContext.Border trailingBorder;

  private List views;
  private List lineInfos = new ArrayList();
  private double[] offsets;
  private double alongDim = 0.0;
  private double acrossDim = 0.0;
  private int symbolsPerLine = 0;

  private RendererMonitor theMonitor;
  private ChangeListener layoutListener = new ChangeAdapter() {
    public void postChange(ChangeEvent ce) {
      resizeAndValidate();
    }
  };
  private ChangeListener repaintListener = new ChangeAdapter() {
    public void postChange(ChangeEvent ce) {
      repaint();
    }
  };

  /**
   * Initializer.
   */

  {
    views = new ArrayList();
    direction = HORIZONTAL;
    scale = 12.0;
    lines = 1;
    spacer = 0;

    theMonitor = new RendererMonitor();
    leadingBorder = new SequenceRenderContext.Border();
    trailingBorder = new SequenceRenderContext.Border();
//    leadingBorder.addPropertyChangeListener(theMonitor);
//    trailingBorder.addPropertyChangeListener(theMonitor);
  }

  /**
   * Create a new SeqeuncePanel.
   */
  public SequencePanel() {
    super();
    if(getFont() == null) {
      setFont(new Font("Times New Roman", Font.PLAIN, 12));
    }
    this.addPropertyChangeListener(theMonitor);
  }
  
  /**
   * Set the SymboList to be rendered. This symbol list will be passed onto the
   * SequenceRenderer instances registered with this SequencePanel.
   *
   * @param s  the SymboList to render
   */
  public void setSequence(SymbolList s) {
    System.out.println("Setting sequence");
    SymbolList oldSequence = sequence;
    if(oldSequence != null) {
      oldSequence.removeChangeListener(layoutListener);
    }
    this.sequence = s;
    sequence.addChangeListener(layoutListener);
    
    resizeAndValidate();
    firePropertyChange("sequence", oldSequence, s);
  }

  /**
   * Retrieve the currently rendered SymbolList
   *
   * @return  the current SymbolList
   */
  public SymbolList getSequence() {
    return sequence;
  }

  /**
   * Set the direction that this SequencePanel renders in. The direction can be
   * one of HORIZONTAL or VERTICAL. Once the direction is set, the display will
   * redraw. HORIZONTAL represents left-to-right rendering. VERTICAL represents
   * AceDB-style vertical rendering.
   *
   * @param dir  the new rendering direction
   */
  public void setDirection(int dir) 
  throws IllegalArgumentException {
    if(dir != HORIZONTAL && dir != VERTICAL) {
      throw new IllegalArgumentException(
        "Direction must be either HORIZONTAL or VERTICAL"
      );
    }
    int oldDirection = direction;
    direction = dir;
    resizeAndValidate();
    firePropertyChange("direction", oldDirection, direction);
  }

  /**
   * Retrieve the current rendering direction.
   *
   * @return the rendering direction (one of HORIZONTAL and VERTICAL)
   */
  public int getDirection() {
    return direction;
  }

  /**
   * Set the number of pixles to leave blank between each block of sequence
   * information.
   * <P>
   * If the SeqeuncePanel chooses to display the sequence information split
   * across multiple lines, then the spacer parameter indicates how many pixles
   * will seperate each line.
   *
   * @param spacer  the number of pixles seperating each line of sequence
   * information
   */
  public void setSpacer(int spacer) {
    int oldSpacer = this.spacer;
    this.spacer = spacer;
    resizeAndValidate();
    firePropertyChange("spacer", oldSpacer, spacer);
  }
  
  /**
   * Retrieve the current spacer value
   *
   * @return the number of pixles between each line of sequence information
   */
  public int getSpacer() {
    return spacer;
  }
  
  /**
   * Set the scale.
   * <P>
   * The scale parameter is interpreted as the number of pixles per symbol. This
   * may take on a wide range of values - for example, to render the symbols as
   * text, you will need a scale of > 8, where as to render chromosome 1 you
   * will want a scale &lt; 0.00000001
   *
   * @param scale the new pixles-per-symbol ratio
   */
  public void setScale(double scale) {
    double oldScale = this.scale;
    this.scale = scale;
    resizeAndValidate();
    firePropertyChange("scale", oldScale, scale);
  }

  /**
   * Retrieve the current scale.
   *
   * @return the number of pixles used to render one symbol
   */
  public double getScale() {
    return scale;
  }

  /**
   * Set the absolute number of lines that the sequence will be rendered on. If
   * this is set to 0, then the number of lines will be calculated according to
   * how many lines will be needed to render the sequence in the currently
   * available space. If it is set to any positive non-zero value, the sequence
   * will be rendered using that many lines, and the SequencePanel will request
   * enough space to accomplish this.
   *
   * @param lines  the number of lines to split the sequence information over
   */
  public void setLines(int lines) {
    int oldLines = this.lines;
    this.lines = lines;
    resizeAndValidate();
    firePropertyChange("lines", oldLines, lines);
  }
  
  /**
   * Retrieve the number of lines that the sequence will be rendered over.
   *
   * @return the current number of lines (0 if autocalculated)
   */
  public int getLines() {
    return lines;
  }
  
  /**
   * Retrieve the object that encapsulates the leading border area - the space
   * before sequence information is rendered.
   *
   * @return a SequenceRenderContext.Border instance
   */
  public SequenceRenderContext.Border getLeadingBorder() {
    return leadingBorder;
  }
  
  /**
   * Retrieve the object that encapsulates the trailing border area - the space
   * after sequence information is rendered.
   *
   * @return a SequenceRenderContext.Border instance
   */
  public SequenceRenderContext.Border getTrailingBorder() {
    return trailingBorder;
  }
  
  /**
   * Paint this component.
   * <P>
   * This calls the paint method of each SequenceRenderer registered with this
   * SequncePanel after setting up the graphics apropreately.
   */
  public void paintComponent(Graphics g) {
    if(sequence == null) {
      return;
    }
    
    Graphics2D g2 = (Graphics2D) g;
    Rectangle2D currentClip = g2.getClip().getBounds2D();
    double minPos;
    double maxPos;
    if(direction == HORIZONTAL) {
      minPos = currentClip.getMinY();
      maxPos = currentClip.getMaxY();
    } else {
      minPos = currentClip.getMinX();
      maxPos = currentClip.getMaxX();
    }
    System.out.println("Clipping in range: " + minPos + "-" + maxPos);
    
    int minOffset = Arrays.binarySearch(offsets, minPos);
    if(minOffset < 0) {
      minOffset = -minOffset - 1;
    }
    System.out.println("minOffset for " + minPos + " is " + minOffset);
    double minCoord = (minOffset == 0) ? 0.0 : offsets[minOffset-1];
    int minP = 1 + (int) ((double) minOffset * symbolsPerLine);
    System.out.println("minP: " + minP);
    
    Rectangle2D.Double clip = new Rectangle2D.Double();
    if (direction == HORIZONTAL) {
        clip.width = alongDim;
        clip.height = acrossDim;
        g2.translate(leadingBorder.getSize(), minCoord);
    } else {
        clip.width = acrossDim;
        clip.height = alongDim;
        g2.translate(minCoord, leadingBorder.getSize());
    }
    
    LineInfo currentLI;
    int min = minP;
    for(int l = minOffset; l < lineInfos.size(); l++) {
      int max = Math.min(min + symbolsPerLine - 1, sequence.length());
      currentLI = (LineInfo) lineInfos.get(l);
      
      System.out.println("Painting " + min + ".." + max);
      if (direction == HORIZONTAL) {
          clip.x = l * alongDim - leadingBorder.getSize();
          clip.y = 0.0;
      } else {
          clip.x = 0.0;
          clip.y = l * alongDim - leadingBorder.getSize();
      }
      
      for (Iterator i = views.iterator(); i.hasNext(); ) {
        SequenceRenderer r = (SequenceRenderer) i.next();
        double depth = currentLI.getDepth(r);
        if (direction == HORIZONTAL) {
            clip.height = depth;
        } else {
            clip.width = depth;
        }
        
        Shape oldClip = g2.getClip();
        g2.clip(clip);
        r.paint(g2, this, min, max);
        g2.setClip(oldClip);
        g2.draw(clip);

        if (direction == HORIZONTAL) {
            g2.translate(0.0, depth);
        } else {
            g2.translate(depth, 0.0);
        }
      }
      if (direction == HORIZONTAL) {
          g2.translate(-alongDim, spacer);
      } else {
          g2.translate(spacer, -alongDim);
      }
      
      min += symbolsPerLine;

      if(offsets[l] > maxPos) {
        System.out.println("Stopping as " + offsets[l] + " is larger than "
        + maxPos);
        break;
      }
    }
  }

  public void addRenderer(SequenceRenderer r) {
    if(r instanceof Changeable) {
      Changeable c = (Changeable) r;
      c.addChangeListener(layoutListener, SequenceRenderContext.LAYOUT);
      c.addChangeListener(repaintListener, SequenceRenderContext.REPAINT);
    }
    views.add(r);
    resizeAndValidate();
  }

  public double sequenceToGraphics(int seqPos) {
    return ((double) (seqPos-1) * scale);
  }

  public int graphicsToSequence(double gPos) {
    return (int) (gPos / scale) + 1;
  }

  public void resizeAndValidate() {
    System.out.println("resizeAndValidate starting");
    Dimension d = null;
    int realLines;
    double acrossDim;
    
    if(sequence == null) {
      System.out.println("No sequence");
      // no sequence - collapse down to no size at all
      alongDim = 0.0;
      acrossDim = 0.0;
      realLines = 0;
      leadingBorder.setSize(0.0);
      trailingBorder.setSize(0.0);
      d = new Dimension(0, 0);
    } else {
      System.out.println("Fitting to sequence");

      int width;
      Dimension parentSize = (getParent() != null)
                ? getParent().getSize()
                : new Dimension(500, 400);
      if (direction == HORIZONTAL) {
        width = parentSize.width;
      } else {
        width = parentSize.height;
      }
      
      System.out.println("Initial width: " + width);
      // got a sequence - fit the size according to sequence length & preferred
      // number of lines.
      alongDim = scale * sequence.length();
      System.out.println("alongDim (pixles needed for sequence only): "
      + alongDim);
      acrossDim = 0.0;
      
      double insetBefore = 0.0;
      double insetAfter = 0.0;
      for (Iterator i = views.iterator(); i.hasNext(); ) {
        SequenceRenderer r = (SequenceRenderer) i.next();
        System.out.println("Renderer: " + r);
        insetBefore = Math.max(insetBefore, r.getMinimumLeader(this));
        insetAfter = Math.max(insetAfter, r.getMinimumTrailer(this));
      }
      leadingBorder.setSize(insetBefore);
      trailingBorder.setSize(insetAfter);
      double insets = insetBefore + insetAfter;
      System.out.println("insetBefore: " + insetBefore);
      System.out.println("insetAfter: " + insetAfter);
      
      if(lines > 0) {
        // Fixed number of lines. Calculate width needed to lay out rectangle.
        realLines = lines;
        width = (int) Math.ceil(
          insets +
          alongDim / (double) lines
        );
      } else {
        // Calculated number of lines for a fixed width
        double dWidth = (double) width;
        dWidth -= insets; // leave space for insets
        realLines = (int) Math.ceil(alongDim / (double) width);
        width = (int) Math.ceil(
          insets +
          alongDim / (double) realLines
        );
      }
      
      acrossDim = 0.0;
      symbolsPerLine = (int) Math.ceil((double) width / (double) scale);
      //System.out.println("symbolsPerLine: " + symbolsPerLine);
      //System.out.println("width: " + width);
      //System.out.println("lines: " + lines);
      //System.out.println("realLines: " + realLines);
      if(symbolsPerLine < 1) {
        throw new Error("Pants");
      }
      int min = 1;
      lineInfos.clear();
      while(min <= sequence.length()) {
        //System.out.println("LineInfor for line starting: " + min);
        LineInfo li = new LineInfo();
        int max = min + symbolsPerLine - 1;
        for(Iterator i = views.iterator(); i.hasNext(); ) {
          SequenceRenderer sr = (SequenceRenderer) i.next();
          li.setDepth(sr, sr.getDepth(this, min, max));
        }
        acrossDim += li.getTotalDepth();
        lineInfos.add(li);
        min = max + 1;
      }
      offsets = new double[lineInfos.size()];
      {
        int i = 0;
        double totDepth = 0.0;
        Iterator lii = lineInfos.iterator();
        while(lii.hasNext()) {
          LineInfo li = (LineInfo) lii.next();
          totDepth += li.getTotalDepth();
          totDepth += spacer;
          offsets[i] = totDepth;
          i++;
        }
      }
      
      acrossDim += spacer * (realLines - 1);
      alongDim = /* Math.ceil((double) width); */ symbolsPerLine * scale;
      if (direction == HORIZONTAL) {
        d = new Dimension(
          (int) Math.ceil(alongDim + insetBefore + insetAfter),
          (int) acrossDim
        );
      } else {
        d = new Dimension(
          (int) acrossDim,
          (int) Math.ceil(alongDim + insetBefore + insetAfter)
        );
      }
    }
    
    setMinimumSize(d);
    setPreferredSize(d);
    revalidate();
    System.out.println("resizeAndValidate ending");
  }

  private class RendererMonitor implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent ev) {
      repaint();
    }
  }
  
  public class Border
  implements Serializable, SwingConstants {
    protected final PropertyChangeSupport pcs;
    private double size = 0.0;
    private int alignment = CENTER;
    
    public double getSize() {
      return size;
    }
    
    private void setSize(double size) {
      this.size = size;
    }
    
    public int getAlignment() {
      return alignment;
    }
    
    public void setAlignment(int alignment)
        throws IllegalArgumentException 
    {
	if (alignment == LEADING || alignment == TRAILING || alignment == CENTER) {
	    int old = this.alignment;
	    this.alignment = alignment;
	    pcs.firePropertyChange("alignment", old, alignment);
	} else {
	    throw new IllegalArgumentException(
		  "Alignment must be one of the constants LEADING, TRAILING or CENTER"
            );
	}
    }
    
    private Border() {
      alignment = CENTER;
      pcs = new PropertyChangeSupport(this);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
      pcs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
      pcs.removePropertyChangeListener(listener);
    }
  }
}

