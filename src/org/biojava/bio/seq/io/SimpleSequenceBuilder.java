package org.biojava.bio.seq.io;

import java.util.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;

/**
 * Basic SequenceBuilder implementation which accumulates all
 * notified information and creates a SimpleSequence.
 *
 * @author Thomas Down
 * @version 1.1 [newio proposal]
 */

public class SimpleSequenceBuilder implements SequenceBuilder {
    public final static SequenceBuilderFactory FACTORY = new SSBFactory();

    private static class SSBFactory implements SequenceBuilderFactory, Serializable {
	private SSBFactory() {
	}

	public SequenceBuilder makeSequenceBuilder() {
	    return new SimpleSequenceBuilder();
	}

	private Object writeReplace() throws ObjectStreamException {
	    try {
		return new StaticMemberPlaceHolder(SimpleSequenceBuilder.class.getField("FACTORY"));
	    } catch (NoSuchFieldException nsfe) {
		throw new NotSerializableException(nsfe.getMessage());
	    }
	}
    }

    //
    // State
    //

    private String name;
    private String uri;
    private ChunkedSymbolListBuilder slBuilder;
    private Annotation annotation;

    private Set rootFeatures;
    private List featureStack;

    {
	annotation = new SimpleAnnotation();
	rootFeatures = new HashSet();
	featureStack = new ArrayList();
	slBuilder = new ChunkedSymbolListBuilder();
    }

    //
    // SeqIOListener
    //

    public void startSequence() {
    }

    public void endSequence() {
    }

    public void setName(String name) {
	this.name = name;
    }

    public void setURI(String uri) {
	this.uri = uri;
    }

    public void addSymbols(Alphabet alpha, Symbol[] syms, int pos, int len)
        throws IllegalAlphabetException
    {
	slBuilder.addSymbols(alpha, syms, pos, len);
    }

    /**
     * Add an annotation-bundle entry to the sequence.  If the annotation key
     * isn't currently defined, the value is added directly.  Otherwise:
     *
     * <ul>
     * <li> If the current value is a string, this is replaced by a
     *      concatenation of the current value, a space, and a string
     *      representation of the new value. </li>
     * <li> If the current value implements the Collection interface,
     *      the new value is added to that collection. </li>
     * <li> Otherwise, the current value is replaced by a List object
     *      containing the old value then the new value in that order. </li>
     * </ul>
     */

    public void addSequenceProperty(String key, Object value) {
	if (value == null)
	    return;

	Object oldValue = null;
	Object newValue = value;

	try {
	    oldValue = annotation.getProperty(key);
	} catch (NoSuchElementException ex) {}

	if (oldValue != null) {
	    if (oldValue instanceof String) {
		newValue = ((String) oldValue) + " " + newValue.toString();
	    } else {
		if (oldValue instanceof Collection) {
		    ((Collection) oldValue).add(newValue);
		    newValue = oldValue;
		} else {
		    List nvList = new ArrayList();
		    nvList.add(oldValue);
		    nvList.add(newValue);
		    newValue = nvList;
		}
	    }
	} 

	try {
	    annotation.setProperty(key, value);
	} catch (ChangeVetoException ex) {
	    throw new BioError(ex, "Annotation should be modifiable");
	}
    }

    public void startFeature(Feature.Template templ) {
	TemplateWithChildren t2 = new TemplateWithChildren();
	t2.template = templ;
	int stackSize = featureStack.size();
	if (stackSize == 0) {
	    rootFeatures.add(t2);
	} else {
	    TemplateWithChildren parent = (TemplateWithChildren) featureStack.get(stackSize - 1);
	    if (parent.children == null)
		parent.children = new HashSet();
	    parent.children.add(t2);
	}
	featureStack.add(t2);
    }

    public void addFeatureProperty(String key, Object value) {
    }

    public void endFeature() {
	if (featureStack.size() == 0)
	    throw new BioError("Assertion failed: Not within a feature");
	featureStack.remove(featureStack.size() - 1);
    }

    public Sequence makeSequence() {
	SymbolList symbols = slBuilder.makeSymbolList();
	Sequence seq = new SimpleSequence(symbols, uri, name, annotation);
	try {
	    for (Iterator i = rootFeatures.iterator(); i.hasNext(); ) {
		TemplateWithChildren twc = (TemplateWithChildren) i.next();
		Feature f = seq.createFeature(twc.template);
		if (twc.children != null) {
		    makeChildFeatures(f, twc.children);
		}
	    }
	} catch (Exception ex) {
	    throw new BioError(ex, "Couldn't create feature");
	}
	return seq;
    }

    private void makeChildFeatures(Feature parent, Set children) 
        throws Exception
    {
	for (Iterator i = children.iterator(); i.hasNext(); ) {
	    TemplateWithChildren twc = (TemplateWithChildren) i.next();
	    Feature f = parent.createFeature(twc.template);
	    if (twc.children != null) {
		makeChildFeatures(f, twc.children);
	    }
	}
    }

    private static class TemplateWithChildren {
	Feature.Template template;
	Set children;
    }
}
