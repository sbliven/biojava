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

import org.biojava.utils.*;
import java.awt.*;
import java.awt.geom.*;
import java.text.*;
import java.awt.font.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;


/**
 * Renderer which draws a track of sequence with a textual label.
 *
 * <p>
 * <strong>Experimental:</strong> This should probably delegate the label-drawing to a little
 * LabelRenderer interface, and have the option of rendering trailing as well as leading labels.
 * </p>
 *
 * @author Kalle Naslund
 * @author Thomas Down
 * @since 1.3
 */


public class LabelledSequenceRenderer extends AbstractChangeable implements SequenceRenderer {
    // keeps track if this label renderer is selected or not
    boolean             selected        =   true;
    // the sequence we are rendering the label for
    SequenceRenderer    seqRend         =   null;
    // the area the label occupies
    double              width           =   200;
    double              depth           =   100;
    // list that contains the text our lable should display
    List                labels          =   new ArrayList();
    // the area that mouse clicks should be caught on, set it to no area in the beginning
    Rectangle           mouseClickArea  =   new Rectangle( 0, 0, -1, -1 );
    // the render forwarder needed to handle ChangeEvents from the encapsuled renderer correctly
    private transient   ChangeForwarder rendererForwarder = null;
    // the ChangeEvents this renderer can emit, due to certain things
    public static final ChangeType RENDERER  = new ChangeType(  "The SequenceRenderer has been added or removed", 
                                                                "SequenceLabelRenderer",
                                                                "RENDERER",
                                                                SequenceRenderContext.LAYOUT );
    
    public static final ChangeType VALUES   = new ChangeType(   "The label value has changed",
                                                                "SequenceLabelRenderer",
                                                                "VALUES",
                                                                SequenceRenderContext.REPAINT );
    
    
    protected ChangeSupport generateChangeSupport() {
        ChangeSupport cs    =   super.generateChangeSupport();
        rendererForwarder = new SequenceRenderer.RendererForwarder( this, cs );
        if( ( seqRend != null ) && ( seqRend instanceof Changeable ) ) {
            Changeable c = ( Changeable ) seqRend;
            c.addChangeListener( rendererForwarder, SequenceRenderContext.REPAINT );
            c.addChangeListener( rendererForwarder, SequenceRenderContext.LAYOUT );  // do i need this ?
        }
        
        return cs;
    }
    
     /**
      * Creates new LabelledSequenceRenderer with default width and depth;
      */

    public LabelledSequenceRenderer() {
    }                           
    
    /**
     * Creates new LabelledSequenceRenderer with the specified width and depth.
     */
    public LabelledSequenceRenderer( double minWidth, double minDepth ) {
        width   =   minWidth;
        depth   =   minDepth;
    }
    
    /**
     * Set the child renderer responsible for drawing the contents of this track
     */
    
    public void setRenderer( SequenceRenderer sR ) throws ChangeVetoException { // should transmit LAYOUT event
        if( hasListeners() ) {
            ChangeEvent     ce  =   new ChangeEvent( this, RENDERER, sR, seqRend );
            ChangeSupport   cs  =   getChangeSupport( RENDERER );
        
            synchronized( cs ) {
                cs.firePreChangeEvent( ce );
            
                if( ( seqRend != null ) && ( seqRend instanceof Changeable ) ) {
                    Changeable c = ( Changeable )seqRend;
                    c.removeChangeListener( rendererForwarder );
                }
            
                seqRend = sR;
            
                if( seqRend instanceof Changeable ) {
                    Changeable c = ( Changeable )seqRend;
                    c.addChangeListener( rendererForwarder );
                }
            
                cs.firePostChangeEvent( ce );
            }
        }
        else {
            seqRend = sR;
        }
        
    
    }
                
    /**
     * Add a piece of text to this renderer's label
     */            
                
    public void addLabelString( String text ) throws ChangeVetoException {   // should trigger REPAINT, as currently no layout change can be triggered by more text
        if( hasListeners() ) {                                               // LAYOUT if i later let text affect the size of the renderer etc
            ChangeSupport   cs  =   getChangeSupport( VALUES );
            ChangeEvent     ce  =   new ChangeEvent( this, VALUES, text );
            
            synchronized( cs ) {
                cs.firePreChangeEvent( ce );
                labels.add( text );
                cs.firePostChangeEvent( ce );
            }
        
        }
        else {
            labels.add( text );                 
        }
    }
    
    /**
     * Remove a piece of text from the label
     */
    
    public void removeLabelString( String text ) throws ChangeVetoException { 
        if( hasListeners() ) {
            ChangeSupport   cs  =   getChangeSupport( VALUES );
            ChangeEvent     ce  =   new ChangeEvent( this, VALUES, null, text );
            
            synchronized ( cs ) {
                cs.firePreChangeEvent( ce );
                labels.remove( text );
                cs.firePostChangeEvent( ce );
            }
        }
        else {
            labels.remove( text );                          // should handle exceptions i guess ?
        }
    }
    
    public void toggleSelectionStatus() throws ChangeVetoException {                   // should throw repaint i guess
        boolean     newStatus;
        
        if( selected ) {
            newStatus = false;
        } 
        else {
            newStatus = true;
        }
        
        if( hasListeners() ) {
            ChangeSupport   cs  =   getChangeSupport( VALUES );
            ChangeEvent     ce  =   new ChangeEvent( this, VALUES );  // i cant just pass the bools here, i need to convert to objects 
        
            synchronized( cs ) {
                cs.firePreChangeEvent( ce );
                selected = newStatus;
                cs.firePostChangeEvent( ce );
            }
        }
        else {
            selected = newStatus;
        }
    }
    
    // SequenceRenderer interface implemented here
    public double getMinimumLeader( SequenceRenderContext sRC ) { 
        return width + seqRend.getMinimumLeader( sRC );
    }
    
    
    public double getMinimumTrailer( SequenceRenderContext sRC ) {
        return seqRend.getMinimumTrailer( sRC );  
    }
    
    
    public double getDepth( SequenceRenderContext sRC ) {
        return Math.max( depth , seqRend.getDepth( sRC ) );
    }
    
    
    public SequenceViewerEvent processMouseEvent( org.biojava.bio.gui.sequence.SequenceRenderContext sRC, java.awt.event.MouseEvent mE, java.util.List path ) {

        SequenceViewerEvent sVE;
        
        path.add( this );
        
        if( mouseClickArea.contains( mE.getX(), mE.getY() ) ){
            sVE = new SequenceViewerEvent( this, this, 1, mE, path );
        }
        else {
            sVE = seqRend.processMouseEvent( sRC, mE, path );
        }
        
        return sVE;
    }
    
    
    public void paint( Graphics2D g, SequenceRenderContext sRC ) {
        Paint           selectedFill        = Color.yellow;
        Paint           textColor           = Color.black;
        Paint           notSelectedFill     = g.getBackground();
        Stroke          beadStroke          = new BasicStroke();
        
        Shape           originalClip        = g.getClip();
        AffineTransform originalTransform   = g.getTransform();
        
        Rectangle2D         labelArea       = new Rectangle2D.Double();
        Font                font            = sRC.getFont();
        FontRenderContext   fRC             = g.getFontRenderContext();


        if( sRC.getDirection() == sRC.VERTICAL ) {
            // rotate the drawing 
            g.rotate( - java.lang.Math.PI / 2 );
            g.translate( -width, 0 );
            // set the area we should grab mouseclicks in, this could most
            // likely be cached in som way
            mouseClickArea.setRect( 0.0 , -width , depth, width );
        } 
        else {
            // set the area that we grab mouseclicks in
            mouseClickArea.setRect( -width, 0.0, width, depth );
        }
        
        // all below this is the same for vertical or horizontal drawing, as i am
        // just using the built in transformation stuff to get text and stuff
        // at the right possition, as its the simple way of drawing text
        
        // set cliping zone so we only can draw in our label area
        labelArea.setRect( -width , 0.0, width, depth );
        g.clip( labelArea );
        
                          //RoundRectangle2D labelBackground   =   new RoundRectangle2D.Double( 5.0, 5.0, width-25, depth-25 , 15.0, 15.0);
        // darw the label area
        if( selected ) {
            g.setPaint( selectedFill );
        } else {
            g.setPaint( notSelectedFill );
        }
        g.fill( labelArea );
        g.setPaint( textColor );
        
        // draw text
        float   drawPosY    = 3; // initial positions for text placement
        float   drawPosX    = (float) (5 - width);
            
        // iterate over each string of the label
        for( Iterator labelIterator = labels.iterator() ; labelIterator.hasNext() ; ) {
           
            // get the current string, and setup the drawign crap for it
            String      currentString   =   ( String )labelIterator.next();
            TextLayout  tLayout         =   new TextLayout( currentString, font, fRC );
        
            drawPosY += tLayout.getAscent();
            tLayout.draw( g, drawPosX, drawPosY );
            drawPosY += ( tLayout.getDescent() +    tLayout.getLeading()  );
        }       
     
        //g.setTransform( originalTransform );
        // reset the clip and transformation so the next renderer can draw its things
        
        g.setTransform( originalTransform );
        g.setClip( originalClip );

        // have the renderer render
        seqRend.paint( g, sRC );
    }      
}

    
    

