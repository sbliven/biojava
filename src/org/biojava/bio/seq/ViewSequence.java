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

package org.biojava.bio.seq;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

import java.util.*;
import java.lang.reflect.*;

/**
 * A view onto another Sequence object.  This class allows new
 * features and annotations to be overlayed onto an existing
 * Sequence without modifying it.
 *
 * <p>This currently uses feature realization code cut and
 * pasted from SimpleSequence.  We need to sort out feature
 * realization a bit better...</p>
 *
 * @author Thomas Down
 */

public class ViewSequence implements Sequence, MutableFeatureHolder {
    /**
     * Delegate Sequence.
     */
    
    private Sequence seqDelegate;

    /**
     * FeatureHolder support
     */

    private MergeFeatureHolder exposedFeatures;
    private MutableFeatureHolder addedFeatures;

    /**
     * IDs
     */

    private String name;
    private String urn;

    /**
     * Our annotation.
     */

    private Annotation anno;

    /**
     * Construct a view onto an existing sequence.
     */

    public ViewSequence(Sequence seq) {
	seqDelegate = seq;
	addedFeatures = new SimpleMutableFeatureHolder();
	exposedFeatures = new MergeFeatureHolder();
	exposedFeatures.addFeatureHolder(seqDelegate);
	exposedFeatures.addFeatureHolder(addedFeatures);

	name = seqDelegate.getName();  // Is this sensible?
	urn = seqDelegate.getURN();
	if (urn.indexOf('?') >= 0)
	    urn = urn + "&view=" + hashCode();
	else
	    urn = urn + "?view=" + hashCode();

	anno = new OverlayAnnotation(seqDelegate.getAnnotation());
    }

    //
    // We implement SymbolList by delegation
    //

    public Alphabet getAlphabet() {
	return seqDelegate.getAlphabet();
    }

    public Iterator iterator() {
	return seqDelegate.iterator();
    }

    public int length() {
	return seqDelegate.length();
    }

    public String seqString() {
	return seqDelegate.seqString();
    }

    public String subStr(int start, int end) {
	return seqDelegate.subStr(start, end);
    }

    public SymbolList subList(int start, int end) {
	return seqDelegate.subList(start, end);
    }

    public Symbol symbolAt(int indx) {
	return seqDelegate.symbolAt(indx);
    }

    public List toList() {
	return seqDelegate.toList();
    }

    //
    // ID methods -- we have our own.
    //

    public String getURN() {
	return urn;
    }

    public String getName() {
	return name;
    }

    //
    // Basic FeatureHolder methods -- delegate to exposedFeatures
    //

    public int countFeatures() {
	return exposedFeatures.countFeatures();
    }

    public Iterator features() {
	return exposedFeatures.features();
    }

    public FeatureHolder filter(FeatureFilter fc, boolean recurse) {
	return exposedFeatures.filter(fc, recurse);
    }

    //
    // MutableFeatureHolder methods -- delegate to addedFeatures
    //

    public void addFeature(Feature f) {
	addedFeatures.addFeature(f);
    }

    /**
     * Remove a feature from this sequence.  <strong>NOTE:</strong> This
     * method will only succeed for features which were added to this
     * ViewSequence.  Trying to remove a Feature from the underlying
     * sequence will cause an IllegalArgumentException.  I think this
     * is the correct behaviour.
     */

    public void removeFeature(Feature f) {
	addedFeatures.removeFeature(f);
    }

    //
    // Get our annotation
    //

    public Annotation getAnnotation() {
	return anno;
    }

    //
    // Feature realization stuff, C+P from SimpleSequence.
    //

    public Feature createFeature(MutableFeatureHolder fh, Feature.Template template)
	throws BioException 
    {
	if (fh != this) {
	    if (! (fh instanceof Feature))
		throw new BioException("fh must be the ListSequence or one of its features.");
	    if (! (containsRecurse(addedFeatures, (Feature) fh)))
		throw new BioException("fh is not a child which has been added to this ListSequence");
	}
	Feature f = createFeatureFromTemplate(this, template);
	fh.addFeature(f);
	return f;
    }

    private static boolean containsRecurse(FeatureHolder fh, Feature f) {
	for (Iterator i = fh.features(); i.hasNext(); ) {
	    if (i.next() == f)
		return true;
	}
	
	for (Iterator i = fh.features(); i.hasNext(); ) {
	    if (containsRecurse((FeatureHolder) i.next(), f))
		return true;
	}
	return false;
    }

  /**
   * List of magic for working out what types of features to create.
   */
  protected static List templateClassToFeatureClass;
  
  /**
   * Add some magic to boot-strap in a feature impl for a template type.
   *
   * @param tf  the TemplateFeature describing the feature impl & template type
   */
  public static void addFeatureImplementation(TemplateFeature tf) {
    templateClassToFeatureClass.add(0, tf);
  }
  
  /**
   * Create a feature of the apropreate type for a template.
   *
   * @param parent   the parent Sequence
   * @param template the Feature.Template that defines what to make
   */
  protected static Feature createFeatureFromTemplate(Sequence parent, Feature.Template template)
  throws BioException {
    for(Iterator i = templateClassToFeatureClass.iterator(); i.hasNext(); ) {
      TemplateFeature tf = (TemplateFeature) i.next();
      if(tf.templateClass.isInstance(template)) {
        try {
          Object [] args = { parent, template };
          return (Feature) tf.con.newInstance(args);
        } catch (InstantiationException ie) {
          throw new BioException(ie);
        } catch (IllegalAccessException iae) {
          throw new BioException(iae);
        } catch (InvocationTargetException ite) {
          throw new BioException(ite);
        }
      }
    }
    throw new BioException("Could not find feature associated with " + template);
  }
  
  /**
   * Initialize the magical feature factory stuff.
   * <P>
   * This is bruit-force - it knows what features to use. Later on, this should
   * be moved out into a preference file.
   */
  static {
    templateClassToFeatureClass = new ArrayList();

    try {
	addFeatureImplementation(new TemplateFeature(
						     Feature.Template.class, SimpleFeature.class
						     ));
	addFeatureImplementation(new TemplateFeature(
						     StrandedFeature.Template.class, SimpleStrandedFeature.class
						     ));
    } catch (Exception ex) {
	throw new BioError("Couldn't register feature impls");
    }
  }

    public static class TemplateFeature {
	public Class templateClass;
	public Class featureClass;
	public Constructor con;
    
	public TemplateFeature(Class templateClass, Class featureClass)
	    throws NoSuchMethodException 
	{
	    Class[] conTypes = new Class[2];
	    conTypes[0] = Sequence.class;
	    conTypes[1] = templateClass;

	    this.templateClass = templateClass;
	    this.featureClass = featureClass;
	    this.con = featureClass.getConstructor(conTypes);
	}
    }
}
