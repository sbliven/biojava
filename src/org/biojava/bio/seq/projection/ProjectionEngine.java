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

package org.biojava.bio.seq.projection;

import java.util.*;
import java.lang.reflect.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.utils.bytecode.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.das.*;

/**
 * Factory for proxy objects which project BioJava features
 * into alternate coordinate systems.  This class binds
 * together a feature and a <code>ProjectionContext</code>
 * object, and returns the resulting projected feature.
 * New feature-projection wrapper classes are generated
 * automatically as they are required.
 *
 * @author Thomas Down
 * @since 1.2
 */

public class ProjectionEngine {
    /**
     * The standard projection engine object.
     */

    public static final ProjectionEngine DEFAULT;

    static {
	DEFAULT = new ProjectionEngine();
    }

    private ProjectionEngine() {
	super();
    }

    private Map _projectionClasses;
    private int seed = 1;
    private PEClassLoader loader;
    private Instantiator instantiator;

    private static class PEClassLoader extends GeneratedClassLoader {
	public PEClassLoader(ClassLoader parent) {
	    super(parent);
	}

	public Class loadClassMagick(String name)
	    throws ClassNotFoundException
	{
	    try {
		InputStream is = getResourceAsStream(name.replace('.', '/') + ".class");
		byte[] buffer = new byte[10000];
		int len = 0;
		int read = 0;
		while (read >= 0) {
		    read = is.read(buffer, len, buffer.length - len);
		    if (read > 0) {
			len += read;
		    }
		}
		is.close();
		Class c = defineClass(name, buffer, 0, len);
		resolveClass(c);

		return c;
	    } catch (Exception ex) {
		throw new ClassNotFoundException(ex.toString());
	    }
		    
	}
    }

    /**
     * Internal helper class.
     */

    public static interface Instantiator {
	public Object newInstance(Constructor c, Object[] args) throws Exception;
    }

    /**
     * Internal helper class.
     */

    public static class InstantiatorImpl implements Instantiator {
	public Object newInstance(Constructor c, Object[] args) throws Exception {
	    return c.newInstance(args);
	}
    }

    {
	loader = new PEClassLoader(ProjectionEngine.class.getClassLoader());
	_projectionClasses = new HashMap();
	try {
	    _projectionClasses.put(Feature.class, loader.loadClassMagick("org.biojava.bio.seq.projection.ProjectedFeature"));
	    _projectionClasses.put(StrandedFeature.class, loader.loadClassMagick("org.biojava.bio.seq.projection.ProjectedStrandedFeature"));

	    instantiator = (Instantiator) loader.loadClassMagick("org.biojava.bio.seq.projection.ProjectionEngine$InstantiatorImpl").newInstance();
	} catch (Exception ex) {
	    throw new BioError(ex, "Assertion failure: can't initialize projection system");
	}
    }

    /**
     * Return a projection of Feature <code>f</code> into the system
     * defined by a given ProjectionContext.  The returned object
     * will implement the same Feature interface (sub-interface of
     * <code>Feature</code> as the underlying feature, and will also
     * implement the <code>Projection</code> interface.
     */

    public Feature projectFeature(Feature f, ProjectionContext ctx) {
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
	sig[1] = ProjectionContext.class;
	try {
	    Constructor ct = projectionClass.getConstructor(sig);
	    Object[] args = new Object[2];
	    args[0] = f;
	    args[1] = ctx;
	    return (Feature) instantiator.newInstance(ct, args);
	} catch (Exception ex) {
	    throw new BioError(ex, "Assertion failed: Couldn't instantiate proxy " + projectionClass.getName());
	}
    }

    private Class getProjectionClass(Class face) {
	Class projection = (Class) _projectionClasses.get(face);
	if (projection == null) {
	    try {
		Class baseClass = ProjectedFeature.class;
		if (StrandedFeature.class.isAssignableFrom(face))
		    baseClass = ProjectedStrandedFeature.class;
		
		StringTokenizer st = new StringTokenizer(face.getName(), ".");
		String faceName = st.nextToken();
		while (st.hasMoreTokens())
		    faceName = st.nextToken();
		
		CodeClass baseClassC = IntrospectedCodeClass.forClass(baseClass);
		CodeClass faceClassC = IntrospectedCodeClass.forClass(face);

		GeneratedCodeClass pclass = new GeneratedCodeClass(
                        "org.biojava.bio.seq.projection.XProjection_" + faceName + "_" + (seed++),
			baseClassC,
		        new CodeClass[] { faceClassC },
			CodeUtils.ACC_PUBLIC | CodeUtils.ACC_SUPER
                );

		List baseInitArgsList = new ArrayList();
		baseInitArgsList.add(baseClass == ProjectedStrandedFeature.class ?
				     IntrospectedCodeClass.forClass(StrandedFeature.class) :
				     IntrospectedCodeClass.forClass(Feature.class));
		baseInitArgsList.add(IntrospectedCodeClass.forClass(ProjectionContext.class));
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
									     ProjectionContext.class
									                         )},
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
			       rtype == CodeUtils.TYPE_BYTE ||
			       rtype == CodeUtils.TYPE_BOOLEAN) {
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
}
