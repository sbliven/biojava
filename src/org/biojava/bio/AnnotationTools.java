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

package org.biojava.bio;

import java.util.*;
import org.biojava.utils.*;

/**
 * <code>AnnotationTools</code> is a set of static utility methods for
 * manipulating <code>Annotation</code>s.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a> (docs).
 */
public final class AnnotationTools {
    /**
     * <code>allIn</code> returns a new <code>Annotation</code>
     * containing only those values in the <code>Annotation</code>
     * argument which are of a type specified by the
     * <code>AnnotationType</code>.
     *
     * @param annotation an <code>Annotation</code> to scan.
     * @param annType an <code>AnnotationType</code>.
     *
     * @return an <code>Annotation</code>.
     */
    public static Annotation allIn(Annotation annotation, AnnotationType annType) {
        Annotation res;
        if (annotation instanceof SmallAnnotation) {
            res = new SmallAnnotation();
        } else {
            res = new SimpleAnnotation();
        }

        for (Iterator i = annType.getProperties().iterator(); i.hasNext();) {
            Object tag = i.next();
            try {
                res.setProperty(tag, annotation.getProperty(tag));
            } catch (ChangeVetoException cve) {
                throw new BioError(cve, "Assertion Failure: Can't alter an annotation");
            }
        }

        return res;
    }

    /**
     * <code>allOut</code> returns a new <code>Annotation</code>
     * containing only those values in the <code>Annotation</code>
     * argument which are <strong>not</strong> of a type specified by
     * the <code>AnnotationType</code>.
     *
     * @param annotation an <code>Annotation</code>.
     * @param annType an <code>AnnotationType</code>.
     *
     * @return an <code>Annotation</code> value.
     */
    public static Annotation allOut(Annotation annotation, AnnotationType annType) {
        Annotation res;
        if (annotation instanceof SmallAnnotation) {
            res = new SmallAnnotation();
        } else {
            res = new SimpleAnnotation();
        }

        Set props = annType.getProperties();
        for (Iterator i = annotation.keys().iterator(); i.hasNext();) {
            Object tag = i.next();
            if (! props.contains(tag)) {
                try {
                    res.setProperty(tag, annotation.getProperty(tag));
                } catch (ChangeVetoException cve) {
                    throw new BioError(cve, "Assertion Failure: Can't alter an annotation");
                }
            }
        }

        return res;
    }
}
