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

import java.lang.reflect.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * A basic implementation of the <code>Sequence</code> interface.
 * <p>
 * This class now implements all methods in the SymbolList
 * interface by delegating to another SymbolList object.  This
 * avoids unnecessary copying, but means that any changes in
 * the underlying SymbolList will be silently reflected in
 * the SimpleSequence.  In general, SimpleSequences should <em>only</em>
 * be constructed from SymbolLists which are known to be immutable.
 * <p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */
public class SimpleSequence implements Sequence, MutableFeatureHolder 
{
    //
    // This section is for the SymbolList implementation-by-delegation
    //

    /**
     * Delegate SymbolList.
     */

    private SymbolList symList;

    public Alphabet getAlphabet() {
	return symList.getAlphabet();
    }

    public Iterator iterator() {
	return symList.iterator();
    }

    public int length() {
	return symList.length();
    }

    public String seqString() {
	return symList.seqString();
    }

    public String subStr(int start, int end) {
	return symList.subStr(start, end);
    }

    public SymbolList subList(int start, int end) {
	return symList.subList(start, end);
    }

    public Symbol symbolAt(int indx) {
	return symList.symbolAt(indx);
    }

    public List toList() {
	return symList.toList();
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
  throws BioException {
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
   * Create a SimpleSequence with the symbols and alphabet of res, and the
   * sequence properties listed.
   *
   * @param res the SymbolList to wrap as a sequence
   * @param urn the URN
   * @param name the name - should be unique if practical
   * @param annotation the annotation object to use or null
   */
  public SimpleSequence(SymbolList res, String urn, String name, Annotation annotation) {
      symList = res;

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
