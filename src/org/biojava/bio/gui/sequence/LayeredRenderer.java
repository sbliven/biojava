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

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.gui.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

public class LayeredRenderer {
  public static final LayeredRenderer INSTANCE = new LayeredRenderer();
  
  public double getDepth(
    List srcL,
    int min, int max,
    List renderers
  ) {
    if(srcL.size() != renderers.size()) {
      throw new IllegalArgumentException(
        "srcL and renderers must be the same size: " +
        srcL.size() + ":" + renderers.size()
      );
    }
    double depth = 0.0;
    Iterator srcI = srcL.iterator();
    Iterator i = renderers.iterator();
    while(srcI.hasNext() && i.hasNext()) {
      SequenceRenderContext src = (SequenceRenderContext) srcI.next();
      SequenceRenderer sRend = (SequenceRenderer) i.next();
      depth += sRend.getDepth(src, min, max);
    }
    return depth;
  }
  
  public double getMinimumLeader(List srcL, List renderers) {
    if(srcL.size() != renderers.size()) {
      throw new IllegalArgumentException(
        "srcL and renderers must be the same size: " +
        srcL.size() + ":" + renderers.size()
      );
    }
    double max = 0.0;
    Iterator srcI = srcL.iterator();
    Iterator i = renderers.iterator();
    while(srcI.hasNext() && i.hasNext()) {
      SequenceRenderContext src = (SequenceRenderContext) srcI.next();
      SequenceRenderer sRend = (SequenceRenderer) i.next();
      max = Math.max(max, sRend.getMinimumLeader(src));
    }
    return max;
  }
  
  public double getMinimumTrailer(List srcL, List renderers) {
    if(srcL.size() != renderers.size()) {
      throw new IllegalArgumentException(
        "srcL and renderers must be the same size: " +
        srcL.size() + ":" + renderers.size()
      );
    }
    double max = 0.0;
    Iterator srcI = srcL.iterator();
    Iterator i = renderers.iterator();
    while(srcI.hasNext() && i.hasNext()) {
      SequenceRenderContext src = (SequenceRenderContext) srcI.next();
      SequenceRenderer sRend = (SequenceRenderer) i.next();
      max = Math.max(max, sRend.getMinimumTrailer(src));
    }
    return max;
  }
  
  public void paint(
    Graphics2D g,
    List srcL,
    int min, int max,
    List renderers
  ) {
    if(srcL.size() != renderers.size()) {
      throw new IllegalArgumentException(
        "srcL and renderers must be the same size: " +
        srcL.size() + ":" + renderers.size()
      );
    }

    double offset = 0.0;
    
    Iterator srcI = srcL.iterator();
    Iterator i = renderers.iterator();
    while(srcI.hasNext() && i.hasNext()) {
      SequenceRenderContext src = (SequenceRenderContext) srcI.next();
      SequenceRenderer sRend = (SequenceRenderer) i.next();
      int dir = src.getDirection();
      
      if(dir == src.HORIZONTAL) {
        g.translate(0.0, offset);
      } else {
        g.translate(offset, 0.0);
      }
      
      sRend.paint(g, src, min, max);

      if(dir == src.HORIZONTAL) {
        g.translate(0.0, -offset);
      } else {
        g.translate(-offset, 0.0);
      }
      
      offset += sRend.getDepth(src, min, max);
    }
  }
}

