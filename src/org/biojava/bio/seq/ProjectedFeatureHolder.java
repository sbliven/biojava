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

import java.util.*;
import java.lang.reflect.*;

import org.biojava.utils.*;
import org.biojava.utils.bytecode.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.symbol.*;

/**
 * Helper class for projecting Feature objects into an alternative
 * coordinate system.
 *
 * <p>TODO: Projection onto the opposite strand</p>
 *
 * @author Thomas Down
 * @since 1.1
 */

public class ProjectedFeatureHolder extends AbstractFeatureHolder {
    private final FeatureHolder wrapped;
    private final FeatureHolder parent;
    private final int translate;
    private FeatureHolder projectedFeatures;

    /**
     * Construct a new FeatureHolder which projects a set of features
     * into a new coordinate system.
     *
     * @param fh The set of features to project.
     * @param parent The FeatureHolder which is to act as parent
     *               for the projected features.
     * @param translation The translation to apply to map into
     *                    the projected coordinate system.
     */

    public ProjectedFeatureHolder(FeatureHolder fh, 
				  FeatureHolder parent, 
				  int translation) 
    {
	this.wrapped = fh;
	this.parent = parent;
	this.translate = translation;
    }

    protected FeatureHolder getProjectedFeatures() {
	if (projectedFeatures == null) {
	    SimpleFeatureHolder sfh = new SimpleFeatureHolder();
	    for (Iterator i = wrapped.features(); i.hasNext(); ) {
		Feature f = (Feature) i.next();
		Feature wf = projectFeature(f);
		try {
		    sfh.addFeature(wf);
		} catch (ChangeVetoException cve) {
		    throw new BioError(
				       cve,
			     "Assertion failure: Should be able to manipulate this FeatureHolder"
				       );
		}
	    }
	    projectedFeatures = sfh;
	}
	return projectedFeatures;
    }

    protected Feature projectFeature(Feature f) {
	Class featureClass = f.getClass();
	Class[] fcInterfaces = featureClass.getInterfaces();
	Class featureInterface = Feature.class;
	for (int i = fcInterfaces.length - 1; i >= 0; --i) {
	    if (Feature.class.isAssignableFrom(fcInterfaces[i])) {
		featureInterface = fcInterfaces[i];
		break;
	    }
	}

	Class projectionClass = getProjectionClass(featureInterface);
	Class[] sig = new Class[2];
	sig[0] = featureInterface;
	sig[1] = ProjectedFeatureHolder.class;
	try {
	    Constructor ct = projectionClass.getConstructor(sig);
	    Object[] args = new Object[2];
	    args[0] = f;
	    args[1] = this;
	    return (Feature) ct.newInstance(args);
	} catch (Exception ex) {
	    throw new BioError(ex, "Assertion failed: Couldn't instantiate proxy " + projectionClass.getName());
	}
    }

    public int countFeatures() {
	return wrapped.countFeatures();
    }

    public Iterator features() {
	return getProjectedFeatures().features();
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	return getProjectedFeatures().filter(ff, recurse);
    }

    private static Map _projectionClasses;
    private static int seed = 1;
    private static GeneratedClassLoader loader;

    static {
	_projectionClasses = new HashMap();
	_projectionClasses.put(Feature.class, ProjectedFeatureWrapper.class);
	_projectionClasses.put(StrandedFeature.class, ProjectedStrandedFeatureWrapper.class);
	loader = new GeneratedClassLoader();
    }

    private static Class getProjectionClass(Class face) {
	Class projection = (Class) _projectionClasses.get(face);
	if (projection == null) {
	    try {
		Class baseClass = ProjectedFeatureWrapper.class;
		if (StrandedFeature.class.isAssignableFrom(face))
		    baseClass = ProjectedStrandedFeatureWrapper.class;
		
		StringTokenizer st = new StringTokenizer(face.getName(), ".");
		String faceName = st.nextToken();
		while (st.hasMoreTokens())
		    faceName = st.nextToken();
		
		CodeClass baseClassC = IntrospectedCodeClass.forClass(baseClass);
		CodeClass faceClassC = IntrospectedCodeClass.forClass(face);

		GeneratedCodeClass pclass = new GeneratedCodeClass(
      "org.biojava.bio.seq.impl.Projection_" + faceName + "_" + (seed++),
			baseClassC,
		  new CodeClass[] { faceClassC },
			CodeUtils.ACC_PUBLIC | CodeUtils.ACC_SUPER
    );

		List baseInitArgsList = new ArrayList();
		baseInitArgsList.add(baseClass == ProjectedStrandedFeatureWrapper.class ?
				     IntrospectedCodeClass.forClass(StrandedFeature.class) :
				     IntrospectedCodeClass.forClass(Feature.class));
		baseInitArgsList.add(IntrospectedCodeClass.forClass(ProjectedFeatureHolder.class));
		CodeMethod m_ourBase_init = new SimpleCodeMethod("<init>",
								 baseClassC,
								 CodeUtils.TYPE_VOID,
								 baseInitArgsList,
								 CodeUtils.ACC_PUBLIC);
		CodeMethod m_ourBase_getViewedFeature = new SimpleCodeMethod("getViewedFeature",
									     baseClassC,
									     IntrospectedCodeClass.forClass(Feature.class),
									     new ArrayList(),
									     CodeUtils.ACC_PUBLIC);

		GeneratedCodeMethod init = pclass.createMethod("<init>",
							       IntrospectedCodeClass.forClass(Void.TYPE),
							       new CodeClass[] {
                       faceClassC,
                       IntrospectedCodeClass.forClass(
                         ProjectedFeatureHolder.class
                       )
                     },
							       CodeUtils.ACC_PUBLIC);
		InstructionVector iv = new InstructionVector();
		iv.add(ByteCode.make_aload(init.getThis()));
		iv.add(ByteCode.make_aload(init.getVariable(0)));
		iv.add(ByteCode.make_aload(init.getVariable(1)));
		iv.add(ByteCode.make_invokespecial(m_ourBase_init));
		iv.add(ByteCode.make_return());
		pclass.setCodeGenerator(init, iv);

		for (Iterator methIt = faceClassC.getMethods().iterator(); methIt.hasNext(); ) {
		    CodeMethod faceMethod = (CodeMethod) methIt.next();
		    if (baseClassC.getMethodsByName(faceMethod.getName()).size() > 0) {
			// System.err.println("Skipping defined method " + faceMethod.getName());
			continue;
		    }
		    
		    if (faceMethod.numParameters() > 0) {
			// System.err.println("Method " + faceMethod.getName() + " has params :|");
			continue;
		    }
		    
		    GeneratedCodeMethod proxyMethod = pclass.createMethod(faceMethod.getName(),
									  faceMethod.getReturnType(),
									  CodeUtils.EMPTY_LIST,
									  CodeUtils.ACC_PUBLIC);
		    iv = new InstructionVector();
		    iv.add(ByteCode.make_aload(proxyMethod.getThis()));
		    iv.add(ByteCode.make_invokevirtual(m_ourBase_getViewedFeature));
		    iv.add(ByteCode.make_invokeinterface(faceMethod));
		    Instruction returni = ByteCode.make_areturn();
		    CodeClass rtype = proxyMethod.getReturnType();
		    if (rtype == CodeUtils.TYPE_VOID) {
			returni = ByteCode.make_return();
		    } else if (rtype == CodeUtils.TYPE_INT ||
			       rtype == CodeUtils.TYPE_SHORT ||
			       rtype == CodeUtils.TYPE_CHAR ||
			       rtype == CodeUtils.TYPE_BYTE) {
			returni = ByteCode.make_ireturn();
		    } else if (rtype == CodeUtils.TYPE_LONG) {
			returni = ByteCode.make_lreturn();
		    } else if (rtype == CodeUtils.TYPE_FLOAT) {
			returni = ByteCode.make_freturn();
		    } else if (rtype == CodeUtils.TYPE_DOUBLE) {
			returni = ByteCode.make_dreturn();
		    }
		    iv.add(returni);
		    pclass.setCodeGenerator(proxyMethod, iv);
		}			
	
		projection = loader.defineClass(pclass);
		_projectionClasses.put(face, projection);
	    } catch (CodeException ex) {
		throw new BioError(ex);
	    }
							       
	}
	return projection;
    }

    public Location getProjectedLocation(Location oldLoc) {
	return oldLoc.translate(translate);
    }

    public int getTranslation() {
	return translate;
    }

    public FeatureHolder getParent() {
	return parent;
    }
}
