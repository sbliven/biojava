/**
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
 * notified information.  Subclass this to implement specific
 * Sequence implementations.
 *
 * @author Thomas Down
 * @author David Huen (modified SimpleSequence to make this)
 * @version 1.2 [newio proposal]
 */

public abstract class SequenceBuilderBase implements SequenceBuilder {
    public static Object ERROR_FEATURES_PROPERTY 
      = SequenceBuilderBase.class + "ERROR_FEATURES_PROPERTY";

    //
    // State
    //

    protected String name;
    protected String uri;
    protected Annotation annotation;

    private Set rootFeatures;
    private List featureStack;
    
    protected Sequence seq;

    {
	annotation = new SimpleAnnotation();
	rootFeatures = new HashSet();
	featureStack = new ArrayList();
//	slBuilder = new ChunkedSymbolListBuilder();
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

    public abstract void addSymbols(Alphabet alpha, Symbol[] syms, int pos, int len)
        throws IllegalAlphabetException;

    /**
     * Add an annotation-bundle entry to the sequence.  If the annotation key
     * isn't currently defined, the value is added directly.  Otherwise:
     *
     * <ul>
     * <li> If the current value implements the Collection interface,
     *      the new value is added to that collection. </li>
     * <li> Otherwise, the current value is replaced by a List object
     *      containing the old value then the new value in that order. </li>
     * </ul>
     */
    public void addSequenceProperty(Object key, Object value) {
      addProperty(annotation, key, value);
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

    /**
     * Add an annotation-bundle entry to the feature. If the annotation key
     * isn't currently defined, the value is added directly. Otherwise:
     *
     * <ul>
     * <li> If the current value implements the Collection interface,
     *      the new value is added to that collection. </li>
     * <li> Otherwise, the current value is replaced by a List object
     *      containing the old value then the new value in that order. </li>
     * </ul>
     */
    public void addFeatureProperty(Object key, Object value)
    throws ParseException {
      try {
        int stackSize = featureStack.size();

        TemplateWithChildren top =
        (TemplateWithChildren) featureStack.get(stackSize - 1);

        addProperty(top.template.annotation, key, value);
      } catch (IndexOutOfBoundsException ioobe) {
        throw new ParseException(
          ioobe,
          "Attempted to add annotation to a feature when no startFeature " +
          "had been invoked"
        );
      }
    }

    public void endFeature() {
	if (featureStack.size() == 0)
	    throw new BioError("Assertion failed: Not within a feature");
	featureStack.remove(featureStack.size() - 1);
    }

    public Sequence makeSequence() {
      //	SymbolList symbols = slBuilder.makeSymbolList();
      //	Sequence seq = new SimpleSequence(symbols, uri, name, annotation);
      try {
        for (Iterator i = rootFeatures.iterator(); i.hasNext(); ) {
          TemplateWithChildren twc = (TemplateWithChildren) i.next();
          try {
            Feature f = seq.createFeature(twc.template);
            if (twc.children != null) {
              makeChildFeatures(f, twc.children);
            }
          } catch (Exception e) {
            // fixme: we should do something more sensible with this error
            e.printStackTrace();
            Set errFeatures;
            Annotation ann = seq.getAnnotation();
            if(ann.containsProperty(ERROR_FEATURES_PROPERTY)) {
              errFeatures = (Set) ann.getProperty(ERROR_FEATURES_PROPERTY);
            } else {
              ann.setProperty(
                ERROR_FEATURES_PROPERTY,
                errFeatures = new HashSet()
              );
            }
            errFeatures.add(twc);
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

    protected void addProperty(Annotation ann, Object key, Object value) {
	if (value == null)
	    return;

	Object oldValue = null;
	Object newValue = value;

	try {
	    oldValue = ann.getProperty(key);
	} catch (NoSuchElementException ex) {}

	if (oldValue != null) {
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

	try {
	    ann.setProperty(key, newValue);
	} catch (ChangeVetoException ex) {
	    throw new BioError(ex, "Annotation should be modifiable");
	}
    }

    private static class TemplateWithChildren {
	Feature.Template template;
	Set children;
    }
}
