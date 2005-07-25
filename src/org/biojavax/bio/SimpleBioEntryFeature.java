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



/*

 * SimpleBioEntryFeature.java

 *

 * Created on June 16, 2005, 11:47 AM

 */



package org.biojavax.bio;

import java.util.Iterator;

import java.util.Set;

import org.biojava.bio.Annotatable;

import org.biojava.bio.Annotation;

import org.biojava.bio.seq.FeatureHolder;

import org.biojava.bio.seq.Sequence;

import org.biojava.bio.seq.StrandedFeature;

import org.biojava.bio.seq.impl.SimpleStrandedFeature;

import org.biojava.utils.ChangeForwarder;

import org.biojava.utils.ChangeVetoException;






/**

 * A simple implementation of BioEntryFeature.

 *

 * Equality is inherited from SimpleStrandedFeature.

 *

 * @author Richard Holland

 * @author Mark Schreiber

 */

public class SimpleBioEntryFeature extends SimpleStrandedFeature implements BioEntryFeature {

    

    /**

     * The annotation for this feature.

     */

    private Annotation ann;

    /**

     * The event forwarder for this feature.

     */

    private ChangeForwarder annFor;

    

    /**

     * Creates a new instance of SimpleBioEntryFeature

     * @param sourceSeq The sequence to relate the feature to.

     * @param parent The parent feature holder, if any.

     * @param template The template to construct the feature from.

     */

    public SimpleBioEntryFeature(Sequence sourceSeq, FeatureHolder parent, StrandedFeature.Template template) {

        super(sourceSeq,parent,template);

        // make our annotation a Term->String one

        this.ann = new SimpleBioEntryAnnotation();

        // transfer any existing annotations

        if (template.annotation!=null) {

            Set k = template.annotation.keys();

            for (Iterator i = k.iterator(); i.hasNext(); ) {

                Object key = i.next();

                Object value = template.annotation.getProperty(key);

                try {

                    this.ann.setProperty(key,value);

                } catch (ChangeVetoException c) {

                    throw new IllegalArgumentException(c);

                }

            }

        }

        // construct the forwarder so that it emits Annotatable.ANNOTATION ChangeEvents

        // for the Annotation.PROPERTY events it will listen for

        this.annFor = new ChangeForwarder.Retyper(this, super.getChangeSupport(Annotatable.ANNOTATION), Annotatable.ANNOTATION);

        // connect the forwarder so it listens for Annotation.PROPERTY events

        this.ann.addChangeListener(this.annFor, Annotation.PROPERTY);

    }

    

    /**

     * Should return the associated annotation object.

     *

     * @return an Annotation object, never null

     */

    public Annotation getAnnotation() {

        return this.ann;

    }

    

    /**

     * Sets the source of the feature.

     * @param source the new source.

     * @throws ChangeVetoException If the source is unacceptable.

     */

    public void setSource(String source) throws ChangeVetoException {

        throw new ChangeVetoException("Source can only be set using setSourceTerm");

    }

}

