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

import java.util.List; // useful trick to 'hide' javax.swing.List

/**
 * A panel that displays a Sequence.
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
 * @author David Huen
 */
public class SequencePanel
extends JComponent
implements SwingConstants,
SequenceRenderContext,
Changeable {
  private static final double FUDGE_OFFSET = 50.0;
  public static final ChangeType RENDERER = new ChangeType(
    "The renderer for this SequencePanel has changed",
    "org.biojava.bio.gui.sequence.SequencePanel",
    "RENDERER",
    SequenceRenderContext.LAYOUT
  );

  private Sequence sequence;
  private RangeLocation range;
  private int direction;
  private double scale;
  private double pixelOffset;
  
  private SequenceRenderContext.Border leadingBorder;
  private SequenceRenderContext.Border trailingBorder;

  private SequenceRenderer renderer;
  private RendererMonitor theMonitor;

  private transient ChangeSupport changeSupport = null;
  
  private SequenceViewerSupport svSupport = new SequenceViewerSupport();
  private MouseListener mouseListener = new MouseAdapter() {
    public void mouseClicked(MouseEvent me) {
      if(!isActive()) {
        return;
      }

      setGraphicsOrigin(FUDGE_OFFSET-sequenceToGraphics(range.getMin()));

      int [] dist = calcDist();
      me.translatePoint(+dist[0], +dist[1]);
      SequenceViewerEvent sve = renderer.processMouseEvent(
        SequencePanel.this,
        me,
        new ArrayList()
      );
      me.translatePoint(-dist[0], -dist[1]);
      svSupport.fireMouseClicked(sve);
    }
    
    public void mousePressed(MouseEvent me) {
      if(!isActive()) {
        return;
      }

      setGraphicsOrigin(FUDGE_OFFSET-sequenceToGraphics(range.getMin()));

      int [] dist = calcDist();
      me.translatePoint(+dist[0], +dist[1]);
      SequenceViewerEvent sve = renderer.processMouseEvent(
        SequencePanel.this,
        me,
        new ArrayList()
      );
      me.translatePoint(-dist[0], -dist[1]);
      svSupport.fireMousePressed(sve);
    }
    
    public void mouseReleased(MouseEvent me) {
      if(!isActive()) {
        return;
      }

      setGraphicsOrigin(FUDGE_OFFSET-sequenceToGraphics(range.getMin()));

      int [] dist = calcDist();
      me.translatePoint(+dist[0], +dist[1]);
      SequenceViewerEvent sve = renderer.processMouseEvent(
        SequencePanel.this,
        me,
        new ArrayList()
      );
      me.translatePoint(-dist[0], -dist[1]);
      svSupport.fireMouseReleased(sve);
    }
  };
  public void addSequenceViewerListener(SequenceViewerListener svl) {
    svSupport.addSequenceViewerListener(svl);
  }
  public void removeSequenceViewerListener(SequenceViewerListener svl) {
    svSupport.removeSequenceViewerListener(svl);
  }

  private SequenceViewerMotionSupport svmSupport = new SequenceViewerMotionSupport();
  private MouseMotionListener mouseMotionListener = new MouseMotionListener() {
    public void mouseDragged(MouseEvent me) {
      if(!isActive()) {
        return;
      }

      setGraphicsOrigin(FUDGE_OFFSET-sequenceToGraphics(range.getMin()));

      int [] dist = calcDist();
      me.translatePoint(+dist[0], +dist[1]);
      SequenceViewerEvent sve = renderer.processMouseEvent(
        SequencePanel.this,
        me,
        new ArrayList()
      );
      me.translatePoint(-dist[0], -dist[1]);
      svmSupport.fireMouseDragged(sve);
    }
    
    public void mouseMoved(MouseEvent me) {
      if(!isActive()) {
        return;
      }

      setGraphicsOrigin(FUDGE_OFFSET-sequenceToGraphics(range.getMin()));

      int [] dist = calcDist();
      me.translatePoint(+dist[0], +dist[1]);
      SequenceViewerEvent sve = renderer.processMouseEvent(
        SequencePanel.this,
        me,
        new ArrayList()
      );
      me.translatePoint(-dist[0], -dist[1]);
      svmSupport.fireMouseMoved(sve);
    }
  };
  public void addSequenceViewerMotionListener(SequenceViewerMotionListener svml) {
    svmSupport.addSequenceViewerMotionListener(svml);
  }
  public void removeSequenceViewerMotionListener(SequenceViewerMotionListener svml) {
    svmSupport.removeSequenceViewerMotionListener(svml);
  }
  
  protected boolean hasChangeListeners() {
    return changeSupport != null;
  }
  
  protected ChangeSupport getChangeSupport(ChangeType ct) {
    if(changeSupport == null) {
      changeSupport = new ChangeSupport();
    }
    
    return changeSupport;
  }
  
  public void addChangeListener(ChangeListener cl) {
    addChangeListener(cl, ChangeType.UNKNOWN);
  }
  
  public void addChangeListener(ChangeListener cl, ChangeType ct) {
    ChangeSupport cs = getChangeSupport(ct);
    synchronized(cs) {
      cs.addChangeListener(cl);
    }
  }
  
  public void removeChangeListener(ChangeListener cl) {
    removeChangeListener(cl, ChangeType.UNKNOWN);
  }
  
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {
    ChangeSupport cs = getChangeSupport(ct);
    synchronized(cs) {
      cs.removeChangeListener(cl);
    }
  }

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
    direction = HORIZONTAL;
    scale = 12.0;
    pixelOffset = 0.0;

    theMonitor = new RendererMonitor();
    leadingBorder = new SequenceRenderContext.Border();
    trailingBorder = new SequenceRenderContext.Border();
  }

  /**
   * Create a new SequencePanel.
   */
  public SequencePanel() {
    super();
    if(getFont() == null) {
      setFont(new Font("serif", Font.PLAIN, 12));
    }
    this.addPropertyChangeListener(theMonitor);
    this.addMouseListener(mouseListener);
    this.addMouseMotionListener(mouseMotionListener);
  }
  
  /**
   * Set the SymboList to be rendered. This symbol list will be passed onto the
   * SequenceRenderer instances registered with this SequencePanel.
   *
   * @param s  the SymboList to render
   */
  public void setSequence(Sequence s) {
    SymbolList oldSequence = sequence;
    if(oldSequence != null) {
      oldSequence.removeChangeListener(layoutListener);
    }
    this.sequence = s;
    if(s != null) {
      sequence.addChangeListener(layoutListener);
    }
    
    resizeAndValidate();
    firePropertyChange("sequence", oldSequence, s);
  }

  public Sequence getSequence() {
    return sequence;
  }
  
  /**
   * Retrieve the currently rendered SymbolList
   *
   * @return  the current SymbolList
   */
  public SymbolList getSymbols() {
    return sequence;
  }
  
  public FeatureHolder getFeatures() {
    return sequence;
  }

  public void setRange(RangeLocation range) {
    RangeLocation oldRange = this.range;
    this.range = range;
    resizeAndValidate();
    firePropertyChange("range", oldRange, range);
  }
  
  public RangeLocation getRange() {
    return this.range;
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
   * Set the scale.
   * <P>
   * The scale parameter is interpreted as the number of pixels per symbol. This
   * may take on a wide range of values - for example, to render the symbols as
   * text, you will need a scale of > 8, where as to render chromosome 1 you
   * will want a scale &lt; 0.00000001
   *
   * @param scale the new pixels-per-symbol ratio
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
   * @return the number of pixels used to render one symbol
   */
  public double getScale() {
    return scale;
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
   * This calls the paint method of the currently registered SequenceRenderer
   * after setting up the graphics appropriately.
   */
  public synchronized void paintComponent(Graphics g) {
    if(!isActive()) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g;
    AffineTransform oldTransform = g2.getTransform();
    Rectangle2D currentClip = g2.getClip().getBounds2D();
    
    // do a transform to offset drawing to the neighbourhood of zero.
    // the 50 here is pretty arbitrary.  The precise value doesn't matter
    setGraphicsOrigin(FUDGE_OFFSET-sequenceToGraphics(range.getMin()));

    double minAcross = sequenceToGraphics(range.getMin()) -
                       renderer.getMinimumLeader(this);
    double maxAcross = sequenceToGraphics(range.getMax()) + 1 +
                       renderer.getMinimumTrailer(this);
    double alongDim = maxAcross - minAcross;
    double depth = renderer.getDepth(this);
    Rectangle2D.Double clip = new Rectangle2D.Double();
    if (direction == HORIZONTAL) {
      clip.x = minAcross;
      clip.y = 0.0;
      clip.width = alongDim;
      clip.height = depth;
      g2.translate(leadingBorder.getSize() - minAcross, 0.0);
    } else {
      clip.x = 0.0;
      clip.y = minAcross;
      clip.width = depth;
      clip.height = alongDim;
      g2.translate(0.0, leadingBorder.getSize() - minAcross);
    }

    Shape oldClip = g2.getClip();
    g2.clip(clip);
    renderer.paint(g2, this);
    g2.setClip(oldClip);
    g2.setTransform(oldTransform);
  }

  public void setRenderer(SequenceRenderer r)
  throws ChangeVetoException {
    if(hasChangeListeners()) {
      ChangeEvent ce = new ChangeEvent(
        this,
        RENDERER,
        r,
        this.renderer
      );
      ChangeSupport cs = getChangeSupport(RENDERER);
      synchronized(cs) {
        cs.firePreChangeEvent(ce);
        _setRenderer(r);
        cs.firePostChangeEvent(ce);
      }
    } else {
      _setRenderer(r);
    }
    resizeAndValidate();
  }
  
  protected void _setRenderer(SequenceRenderer r) {
    if( (this.renderer != null) && (this.renderer instanceof Changeable) ) {
      Changeable c = (Changeable) this.renderer;
      c.removeChangeListener(layoutListener, SequenceRenderContext.LAYOUT);
      c.removeChangeListener(repaintListener, SequenceRenderContext.REPAINT);
    }

    this.renderer = r;

    if( (r != null) && (r instanceof Changeable) ) {
      Changeable c = (Changeable) r;
      c.addChangeListener(layoutListener, SequenceRenderContext.LAYOUT);
      c.addChangeListener(repaintListener, SequenceRenderContext.REPAINT);
    }
  }

  public double sequenceToGraphics(int seqPos) {
    return ((double) (seqPos-1)) * scale + pixelOffset;
  }

  public int graphicsToSequence(double gPos) {
    return ((int) ((gPos - pixelOffset) / scale)) + 1;
  }
  
  public int graphicsToSequence(Point point) {
    if(direction == HORIZONTAL) {
      return graphicsToSequence(point.getX());
    } else {
      return graphicsToSequence(point.getY());
    }
  }

 public void setGraphicsOrigin(double displacement) {
     // System.out.println("setGraphicsOrigin: " + displacement);  
     this.pixelOffset += displacement;
  }

  public void resizeAndValidate() {
    //System.out.println("resizeAndValidate starting");
    Dimension mind = null;
    Dimension maxd = null;
    
    if(!isActive()) {
      // System.out.println("No sequence");
      // no sequence - collapse down to no size at all
      leadingBorder.setSize(0.0);
      trailingBorder.setSize(0.0);
      mind = maxd = new Dimension(0, 0);
    } else {
      double minAcross = sequenceToGraphics(range.getMin());
      double maxAcross = sequenceToGraphics(range.getMax());
      double maxDropAcross = sequenceToGraphics(range.getMax() - 1);
      double lb = renderer.getMinimumLeader(this);
      double tb = renderer.getMinimumTrailer(this);
      double alongDim =
        (maxAcross - minAcross) +
        lb + tb;
      double alongDropDim = 
	(maxDropAcross - minAcross) + 
	lb + tb;
      double depth = renderer.getDepth(this);
      if(direction == HORIZONTAL) {
	  mind = new Dimension((int) Math.ceil(alongDropDim), (int) Math.ceil(depth));
	  maxd = new Dimension((int) Math.ceil(alongDim), (int) Math.ceil(depth));
      } else {
	  mind = new Dimension((int) Math.ceil(depth), (int) Math.ceil(alongDropDim));
	  maxd = new Dimension((int) Math.ceil(depth), (int) Math.ceil(alongDim));
      }
    }
    
    setMinimumSize(mind);
    setPreferredSize(maxd);
    setMaximumSize(maxd);
    revalidate();
    // System.out.println("resizeAndValidate ending");
  }

  private class RendererMonitor implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent ev) {
      repaint();
    }
  }
  
  protected int [] calcDist() {
    double minAcross = sequenceToGraphics(range.getMin()) -
                       renderer.getMinimumLeader(this);
    int [] dist = new int[2];
    if(direction == HORIZONTAL) {
      dist[0] = (int) minAcross;
      dist[1] = 0;
    } else {
      dist[0] = 0;
      dist[1] = (int) minAcross;
    }
    
    return dist;
  }
  
  protected boolean isActive() {
    return
      (sequence != null) &&
      (renderer != null) &&
      (range != null);
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

    private boolean eq(Object a, Object b) {
	if (a == null || b == null) {
	    return a == b;
	} else {
	    return a.equals(b);
	}
    }
	

    public boolean equals(Object o) {
	if (! (o instanceof SequencePanel)) {
	    return false;
	}

	SequencePanel osp = (SequencePanel) o;
	return (eq(getSymbols(), osp.getSymbols()) && eq(getRange(), osp.getRange()));
    }

    public int hashCode() {
	int hc = 653;
	SymbolList sl = getSymbols();
	if (sl != null) {
	    hc = hc ^ sl.hashCode();
	}

	Location l = getRange();
	if (l != null) {
	    hc = hc ^ l.hashCode();
	}

	return hc;
    }
}

