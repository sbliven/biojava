package org.biojava.bio.program.ssbind;

import java.util.*;
import org.biojava.bio.*;
import org.biojava.utils.*;

public class AnnotationFactory {
    public static Annotation makeAnnotation(Map m) {
	if (m.size() == 0) {
	    return Annotation.EMPTY_ANNOTATION;
	} else {
	    Annotation anno;
	    if (m.size() < 15) {
		anno = new SmallAnnotation();
	    } else {
		anno = new SimpleAnnotation();
	    }

	    try {
		for (Iterator mei = m.entrySet().iterator(); mei.hasNext(); ) {
		    Map.Entry me = (Map.Entry) mei.next();
		    anno.setProperty(me.getKey(), me.getValue());
		}
	    } catch (ChangeVetoException cve) {
		throw new BioError(cve, "Assert failed: couldn't modify newly created Annotation");
	    }

	    return anno;
	}
    }
}
