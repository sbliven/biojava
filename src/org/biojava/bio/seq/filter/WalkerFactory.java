/**
 * Factory for generating walkers that are customised to particuar feature
 * visitors.
 *
 * @author Matthew Pocock
 */
package org.biojava.bio.seq.filter;

import org.biojava.bio.seq.FilterUtils;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.utils.bytecode.*;
import org.biojava.utils.AssertionFailure;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;

public class WalkerFactory {
  private static WalkerFactory ourInstance;

  public synchronized static WalkerFactory getInstance() {
    if (ourInstance == null) {
      ourInstance = new WalkerFactory();
    }
    return ourInstance;
  }

  private final Map walkers;
  private final GeneratedClassLoader classLoader;

  private WalkerFactory() {
    walkers = new HashMap();
    classLoader = new GeneratedClassLoader(this.getClass().getClassLoader());
  }

  public synchronized Walker getWalker(Visitor visitor) {
    Class walkerClass = (Class) walkers.get(visitor.getClass());

    if(walkerClass == null) {
      walkers.put(visitor.getClass(),
                  walkerClass = generateWalker(visitor.getClass()));
    }

    try {
      return (Walker) walkerClass.newInstance();
    } catch (InstantiationException ie) {
      throw new AssertionFailure("Could not instantiate walker for class: " +
                                 walkerClass,
                                 ie);
    } catch (IllegalAccessException iae) {
      throw new AssertionFailure("Could not instantiate walker for class: " +
                                 walkerClass,
                                 iae);
    }
  }

  private Class generateWalker(Class visitorClass) {
    try {
      String vcn = visitorClass.getName().replaceAll("\\.", "");
      String walkerClassName = "scratch_.walker_" + vcn;

      CodeClass c_Void = CodeUtils.TYPE_VOID;
      CodeClass c_Object = CodeUtils.TYPE_OBJECT;
      CodeClass c_Visitor = IntrospectedCodeClass.forClass(Visitor.class);
      CodeClass c_ourVisitor = IntrospectedCodeClass.forClass(visitorClass);
      CodeClass c_Walker = IntrospectedCodeClass.forClass(Walker.class);
      CodeClass c_FeatureFilter = IntrospectedCodeClass.forClass(FeatureFilter.class);

      // make a class
      GeneratedCodeClass walkerClass = new GeneratedCodeClass(
              walkerClassName,
              c_Object,
              new CodeClass[]{c_Walker},
              CodeUtils.ACC_PUBLIC | CodeUtils.ACC_SUPER);

      // constructor - no args, forward to SUPER
      CodeMethod m_Object_init = c_Object.getConstructor(CodeUtils.EMPTY_LIST);
      GeneratedCodeMethod init = walkerClass.createMethod("<init>",
                                                          CodeUtils.TYPE_VOID,
                                                          CodeUtils.EMPTY_LIST,
                                                          CodeUtils.ACC_PUBLIC);
      InstructionVector initIV = new InstructionVector();
      initIV.add(ByteCode.make_aload(init.getThis()));
      initIV.add(ByteCode.make_invokespecial(m_Object_init));
      initIV.add(ByteCode.make_return());
      walkerClass.setCodeGenerator(init, initIV);

      // get all visitor methods
      Method[] methods = visitorClass.getMethods();

      return classLoader.defineClass(walkerClass);
    } catch (CodeException ce) {
      throw new AssertionFailure("Unable to generate walker code", ce);
    } catch (NoSuchMethodException nsme) {
      throw new AssertionFailure("Unable to generate walker code", nsme);
    }
  }
}

