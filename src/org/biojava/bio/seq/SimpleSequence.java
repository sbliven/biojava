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
import java.lang.reflect.*;
import java.util.*;

/**
 * A no-frills implementation of Sequence.
 * <P>
 * It implements the ResidueList portion of Sequence by extending
 * SimpleResideList. This should probably be changed to delegation to allow
 * custom ResidueList implementations to be wrapped.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */
public class SimpleSequence extends SimpleResidueList 
                            implements Sequence, MutableFeatureHolder 
{
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
  throws SeqException {
    for(Iterator i = templateClassToFeatureClass.iterator(); i.hasNext(); ) {
      TemplateFeature tf = (TemplateFeature) i.next();
      if(tf.templateClass.isInstance(template)) {
        try {
          Object [] args = { parent, template };
          return (Feature) tf.con.newInstance(args);
        } catch (InstantiationException ie) {
          throw new SeqException(ie);
        } catch (IllegalAccessException iae) {
          throw new SeqException(iae);
        } catch (InvocationTargetException ite) {
          throw new SeqException(ite);
        }
      }
    }
    throw new SeqException("Could not find feature associated with " + template);
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
  
  private FeatureFactory fFact;
  private String urn;
  private String name;
  private Annotation annotation;
  private MutableFeatureHolder featureHolder;

  
  protected MutableFeatureHolder getFeatureHolder() {
    if(featureHolder == null)
      featureHolder = new SimpleMutableFeatureHolder();
    return featureHolder;
  }

  protected boolean featureHolderAllocated() {
    return featureHolder != null;
  }

  public FeatureFactory getFeatureFactory() {
    return fFact;
  }
  
  public void setFeatureFactory(FeatureFactory fFact) {
    this.fFact = fFact;
  }

  public String getURN() {
    return urn;
  }

  public void setURN(String urn) {
    this.urn = urn;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Annotation getAnnotation() {
    if(annotation == null)
      annotation = new SimpleAnnotation();
    return annotation;
  }

  public int countFeatures() {
    if(featureHolderAllocated())
      return getFeatureHolder().countFeatures();
    return 0;
  }

  public Iterator features() {
    if(featureHolderAllocated())
      return getFeatureHolder().features();
    return Collections.EMPTY_LIST.iterator();
  }

  public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
    if(featureHolderAllocated())
      return getFeatureHolder().filter(ff, recurse);
    return FeatureHolder.EMPTY_FEATURE_HOLDER;
  }

  public Feature createFeature(MutableFeatureHolder fh, Feature.Template template)
  throws SeqException {
    Feature f = createFeatureFromTemplate(this, template);
    if(fh == this) {
      fh = this.getFeatureHolder();
    }
    fh.addFeature(f);
    return f;
  }

    public void addFeature(Feature f) {
	throw new UnsupportedOperationException();
    }

    public void removeFeature(Feature f) {
	featureHolder.removeFeature(f);
    }

  /**
   * Create a SimpleSequence with the residues and alphabet of res, and the
   * sequence properties listed.
   *
   * @param res the ResidueList to wrap as a sequence
   * @param urn the URN
   * @param name the name - should be unique if practical
   * @param annotation the annotation object to use or null
   */
  public SimpleSequence(ResidueList res, String urn, String name, Annotation annotation) {
    super(res);
    setURN(urn);
    setName(name);
    this.annotation = annotation;
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
