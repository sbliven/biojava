/**
 * Factory for generating walkers that are customised to particuar feature
 * visitors.
 *
 * @author Matthew Pocock
 */
package org.biojava.bio.seq.filter;

import org.biojava.bio.seq.FilterUtils;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.BioException;
import org.biojava.utils.bytecode.*;
import org.biojava.utils.AssertionFailure;

import java.util.*;
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
  private final List filtersWithParents;

  private WalkerFactory() {
    walkers = new HashMap();
    classLoader = new GeneratedClassLoader(this.getClass().getClassLoader());
    filtersWithParents = new ArrayList();
  }

  public synchronized void addFilterWithParent(Class filterClass) {
    filtersWithParents.add(filterClass);
  }

  public synchronized Walker getWalker(Visitor visitor)
  throws BioException {
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

  private Class generateWalker(Class visitorClass)
  throws BioException {
    try {
      String vcn = visitorClass.getName().replaceAll("\\.", "");
      String walkerClassName = "scratch_.walker_" + vcn;

      CodeClass c_Void = CodeUtils.TYPE_VOID;
      CodeClass c_Visitor = IntrospectedCodeClass.forClass(Visitor.class);
      CodeClass c_ourVisitor = IntrospectedCodeClass.forClass(visitorClass);
      CodeClass c_Walker = IntrospectedCodeClass.forClass(Walker.class);
      CodeClass c_WalkerBase = IntrospectedCodeClass.forClass(Object.class);
      CodeClass c_FeatureFilter = IntrospectedCodeClass.forClass(FeatureFilter.class);

      // make a class
      GeneratedCodeClass walkerClass = new GeneratedCodeClass(
              walkerClassName,
              c_WalkerBase,
              new CodeClass[]{c_Walker},
              CodeUtils.ACC_PUBLIC | CodeUtils.ACC_SUPER);

      // constructor - no args, forward to SUPER
      CodeMethod m_WalkerBase_init = c_WalkerBase.getConstructor(CodeUtils.EMPTY_LIST);
      GeneratedCodeMethod init = walkerClass.createMethod("<init>",
                                                          CodeUtils.TYPE_VOID,
                                                          CodeUtils.EMPTY_LIST,
                                                          CodeUtils.ACC_PUBLIC);
      InstructionVector initIV = new InstructionVector();
      initIV.add(ByteCode.make_aload(init.getThis()));
      initIV.add(ByteCode.make_invokespecial(m_WalkerBase_init));
      initIV.add(ByteCode.make_return());
      walkerClass.setCodeGenerator(init, initIV);

      // get all visitor methods
      Method[] methods = visitorClass.getMethods();
      List visitorMeths = new ArrayList();
      Class retClass = null;

      // make a set of all handlers and get the return type used
      // barf if the return type is not consistent
      for(int mi = 0; mi < methods.length; mi++) {
        Method method = methods[mi];

        Class ret = method.getReturnType();
        Class[] args = method.getParameterTypes();

        // is this one of our classes?
        if(args.length > 0) {
          Class arg0 = args[0];
          String methName = method.getName();
          String arg0Name = arg0.getName();

          // If arg0 is inner class, strip off outer-class name to make our name
          int doli = arg0Name.lastIndexOf("$");
          if(doli >= 0) {
            arg0Name = arg0Name.substring(doli+1);
          }

          // drop the leading captial
          arg0Name = arg0Name.substring(0).toLowerCase() +
                  arg0Name.substring(1);

          // we have a naming match?
          if(arg0Name.equals(methName)) {
            // check argument 0 is a feature filter
            if(FeatureFilter.class.isInstance(arg0)) {
              // we have a live one.
              // check that the return type is good
              if(retClass == null) {
                retClass = ret;
              } else {
                if(retClass != ret) {
                  throw new BioException(
                          "Return type of all methods must agree. " +
                          "We were expecting: " + retClass.getName() +
                          " but found: " + ret.getName());
                }
              }

              // if there are other args, make sure they match the return type
              for(int ai = 1; ai < args.length; ai++) {
                Class argI = args[ai];
                if(argI != retClass) {
                  throw new BioException(
                          "The first argument to a handler method must be a filter. " +
                          "All subsequent arguments must match the return type.  In: " +
                          method);
                }
              }
            }
          }

          // OK - this looks like a good handler - add it to our list
          visitorMeths.add(method);
        }
      }

      // sort by filter class - most derived first
      Collections.sort(visitorMeths,  new Comparator() {
        public int compare(Object o1, Object o2) {
          Method m1 = (Method) o1; Class c1 = m1.getParameterTypes()[0];
          Method m2 = (Method) o2; Class c2 = m2.getParameterTypes()[0];
          if(c1.isInstance(c2)) return +1;
          if(c2.isInstance(c1)) return -1;
          return 0;
        }
      });

      // now let's implement our dispatcher
      CodeClass c_retClass = IntrospectedCodeClass.forClass(retClass);
      CodeClass[] walkParams = new CodeClass[]{ c_FeatureFilter, c_Visitor};
      CodeMethod m_Walker_walk = c_Walker.getMethod("walk", walkParams);

      GeneratedCodeMethod walk = walkerClass.createMethod(
              m_Walker_walk.getName(),
              m_Walker_walk.getReturnType(),
              walkParams,
              new String[]{"filter", "visitor"},
              m_Walker_walk.getModifiers());
      InstructionVector walkIV = new InstructionVector();
      LocalVariable lv_this = walk.getThis();
      LocalVariable lv_filter = walk.getVariable("filter");
      LocalVariable lv_visitor = walk.getVariable("visitor");

      // local variables for the return values of wrapped invocatins
      List wrappedLVs = new ArrayList();

      // firstly, we should call And, Or, Not, etc., wrapped filters
      //
      // These are all listed in filtersWithParents.
      for(Iterator fwpi = filtersWithParents.iterator(); fwpi.hasNext(); ) {
        InstructionVector wfiv = new InstructionVector();

        // find the methods that get the wrapped
        Class filtClass = (Class) fwpi.next();
        CodeClass c_ourFilter = IntrospectedCodeClass.forClass(filtClass);

        Method[] filtMeth = filtClass.getMethods();
        Iterator lvi = wrappedLVs.iterator();

        for(int mi = 0; mi < filtMeth.length; mi++) {
          Method m = filtMeth[mi];

          // no args, returns a filter
          if(m.getParameterTypes().length == 0 &&
                  FeatureFilter.class.isInstance(m.getReturnType())) {
            CodeMethod m_getFilter = IntrospectedCodeClass.forMethod(m);

            LocalVariable lv;
            if(lvi.hasNext()) {
              lv = (LocalVariable) lvi.next();
            } else {
              lv = new LocalVariable(c_retClass);
              wrappedLVs.add(lv);
            }

            // res_i = this.walk(
            //       ((c_ourFilter) filter).m_getFilter(),
            //       visitor );
            walkIV.add(ByteCode.make_aload(lv_this));
            walkIV.add(ByteCode.make_aload(lv_filter));
            walkIV.add(ByteCode.make_checkcast(c_ourFilter));
            walkIV.add(ByteCode.make_invokevirtual(m_getFilter));
            walkIV.add(ByteCode.make_aload(lv_visitor));
            walkIV.add(ByteCode.make_invokevirtual(m_Walker_walk));

            if(c_retClass != CodeUtils.TYPE_VOID) {
              walkIV.add(ByteCode.make_astore(lv));
            }
          }
        }

        // if (filter intanceof orFilter ) {
        //   do the above block
        //  }
        walkIV.add(ByteCode.make_aload(lv_filter));
        walkIV.add(ByteCode.make_instanceof(c_ourFilter));
        walkIV.add(ByteCode.make_iconst(1));
        walkIV.add(new IfExpression(
                ByteCode.op_ifeq,
                walkIV,
                CodeUtils.DO_NOTHING));
        walkIV.add(wfiv);
      }

      return classLoader.defineClass(walkerClass);
    } catch (CodeException ce) {
      throw new AssertionFailure("Unable to generate walker code", ce);
    } catch (NoSuchMethodException nsme) {
      throw new AssertionFailure("Unable to generate walker code", nsme);
    }
  }
}

