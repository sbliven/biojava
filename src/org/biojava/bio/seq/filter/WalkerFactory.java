/**
 * Factory for generating walkers that are customised to particuar feature
 * visitors.
 *
 * @author Matthew Pocock
 */
package org.biojava.bio.seq.filter;

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

          // If arg0 is inner class, strip off outer-class name to make our name
          String methName = method.getName();
          String arg0Name = arg0.getName();
          int doli = arg0Name.lastIndexOf('$');
          if(doli >= 0) {
            arg0Name = arg0Name.substring(doli+1);
          }
          doli = arg0Name.lastIndexOf('.');
          if(doli >= 0) {
            arg0Name = arg0Name.substring(doli+1);
          }

          // drop the leading captial
          arg0Name = arg0Name.substring(0,1).toLowerCase() +
                  arg0Name.substring(1);

          // we have a naming match?
          if(arg0Name.equals(methName)) {
            // check argument 0 is a feature filter

            if(FeatureFilter.class.isAssignableFrom(arg0)) {
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

              // OK - this looks like a good handler - add it to our list
              visitorMeths.add(method);
            }
          }
        }
      }

      // if retclass was never set, it's safe to make it void
      if(retClass == null) {
        retClass = Void.TYPE;
      }

      // sort by filter class - most derived first
      Collections.sort(visitorMeths,  new Comparator() {
        public int compare(Object o1, Object o2) {
          Method m1 = (Method) o1; Class c1 = m1.getParameterTypes()[0];
          Method m2 = (Method) o2; Class c2 = m2.getParameterTypes()[0];
          if(c1.isAssignableFrom(c2)) return +1;
          if(c2.isAssignableFrom(c1)) return -1;
          return 0;
        }
      });

      // now let's implement our dispatcher
      CodeClass c_retClass = IntrospectedCodeClass.forClass(retClass);
      CodeClass[] walkParams = new CodeClass[]{ c_FeatureFilter, c_Visitor};

      GeneratedCodeMethod doWalk = walkerClass.createMethod(
              "doWalk",
              c_retClass,
              walkParams,
              new String[]{"filter", "visitor"},
              CodeUtils.ACC_PUBLIC);
      InstructionVector walkIV = new InstructionVector();
      LocalVariable lv_filter = doWalk.getVariable("filter");
      LocalVariable lv_visitor = doWalk.getVariable("visitor");
      LocalVariable lv_visitor2 = new LocalVariable(c_ourVisitor, "visitor2");
      walkIV.add(ByteCode.make_aload(lv_visitor));
      walkIV.add(ByteCode.make_checkcast(c_ourVisitor));
      walkIV.add(ByteCode.make_astore(lv_visitor2));

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
System.err.println(filtClass);
        Method[] filtMeth = filtClass.getMethods();
        int lvi = 0;

        for(int mi = 0; mi < filtMeth.length; mi++) {
          Method m = filtMeth[mi];

          // no args, returns a filter
          if(m.getParameterTypes().length == 0 &&
                  FeatureFilter.class.isAssignableFrom(m.getReturnType()))
          {
            CodeMethod m_getFilter = IntrospectedCodeClass.forMethod(m);
System.err.println("\t" + m);
            LocalVariable lv;
            if(lvi < wrappedLVs.size()) {
              lv = (LocalVariable) wrappedLVs.get(lvi);
            } else {
              lv = new LocalVariable(c_retClass);
              wrappedLVs.add(lv);
            }

            // res_i = this.walk(
            //       ((c_ourFilter) filter).m_getFilter(),
            //       visitor );
            wfiv.add(ByteCode.make_aload(doWalk.getThis()));
            wfiv.add(ByteCode.make_aload(lv_filter));
            wfiv.add(ByteCode.make_checkcast(c_ourFilter));
            wfiv.add(ByteCode.make_invokevirtual(m_getFilter));
            wfiv.add(ByteCode.make_aload(lv_visitor));
            wfiv.add(ByteCode.make_invokevirtual(doWalk));

            if(c_retClass != CodeUtils.TYPE_VOID) {
              wfiv.add(ByteCode.make_astore(lv));
            }
          }
        }

        // if (filter intanceof ourFilter ) {
        //   do the above block
        // }
        walkIV.add(ByteCode.make_aload(lv_filter));
        walkIV.add(ByteCode.make_instanceof(c_ourFilter));
        walkIV.add(new IfExpression(ByteCode.op_ifne,
                                    wfiv,
                                    CodeUtils.DO_NOTHING));
      }



      // the big if/else/if/else stack goes here - switching on feature filter
      // type for each method using instanceof
      //
      // if(filter instanceof Filt1) {
      //    viewer2.filt1( ((Filt1) filter) );
      //    return;
      // }

      for(Iterator mi = visitorMeths.iterator(); mi.hasNext(); ) {
        Method meth = (Method) mi.next();

        // the viewer method is invoked as:
        //
        //   viewer2.foo( (Foo) filter, ...);
        //
        // if the return value is void, we just return
        // if it is not void, we return the Bar instance it returns

        CodeMethod ourMeth = IntrospectedCodeClass.forMethod(meth);
        CodeClass c_thisFiltType = ourMeth.getParameterType(0);

        InstructionVector bodyIV = new InstructionVector();
        bodyIV.add(ByteCode.make_aload(lv_visitor2));
        bodyIV.add(ByteCode.make_aload(lv_filter));
        bodyIV.add(ByteCode.make_checkcast(c_thisFiltType));
        for(int ai = 1; ai < ourMeth.numParameters(); ai++) {
          bodyIV.add(ByteCode.make_aload((LocalVariable) wrappedLVs.get(ai-1)));
        }
        bodyIV.add(ByteCode.make_invokevirtual(ourMeth));
        bodyIV.add(ByteCode.make_return(doWalk));

        // wrap this in an if(filt instanceof Foo)
        walkIV.add(ByteCode.make_aload(lv_filter));
        walkIV.add(ByteCode.make_instanceof(c_thisFiltType));
        walkIV.add(new IfExpression(ByteCode.op_ifne,
                                    bodyIV,
                                    CodeUtils.DO_NOTHING));
      }

      // return void if we are void,
      // return null if we are meant to return something but no handler was used
      if(c_retClass == CodeUtils.TYPE_VOID) {
        walkIV.add(ByteCode.make_return());
      } else {
        walkIV.add(ByteCode.make_aconst_null());
        walkIV.add(ByteCode.make_areturn());
      }

      walkerClass.setCodeGenerator(doWalk, walkIV);

      // Wire doWalk to walk and if necisary create field
      //
      CodeField f_value = null;
      if(c_retClass != CodeUtils.TYPE_VOID) {
        f_value = walkerClass.createField("value",
                                          CodeUtils.TYPE_OBJECT,
                                          CodeUtils.ACC_PRIVATE);
      }

      { // protect us from leakey locals
        GeneratedCodeMethod walkImpl = walkerClass.createMethod(
                "walk",
                CodeUtils.TYPE_VOID,
                walkParams,
                CodeUtils.ACC_PUBLIC);
        InstructionVector wiIV = new InstructionVector();
        if (c_retClass != CodeUtils.TYPE_VOID) {
          wiIV.add(ByteCode.make_aload(walkImpl.getThis()));
        }
        wiIV.add(ByteCode.make_aload(walkImpl.getThis()));
        wiIV.add(ByteCode.make_aload(walkImpl.getVariable(0)));
        wiIV.add(ByteCode.make_aload(walkImpl.getVariable(1)));
        wiIV.add(ByteCode.make_invokevirtual(doWalk));
        if (c_retClass != CodeUtils.TYPE_VOID) {
          wiIV.add(ByteCode.make_putfield(f_value));
        }
        wiIV.add(ByteCode.make_return());

        walkerClass.setCodeGenerator(walkImpl, wiIV);
      }

      // generate the getValue() method
      { // protect us from leakey locals
        GeneratedCodeMethod getValue = walkerClass.createMethod(
                "getValue",
                CodeUtils.TYPE_OBJECT,
                CodeUtils.EMPTY_LIST,
                CodeUtils.ACC_PUBLIC);
        InstructionVector gvIV = new InstructionVector();
        if (c_retClass == CodeUtils.TYPE_VOID) {
          gvIV.add(ByteCode.make_aconst_null());
        } else {
          gvIV.add(ByteCode.make_aload(getValue.getThis()));
          gvIV.add(ByteCode.make_getfield(f_value));
        }
        gvIV.add(ByteCode.make_areturn());
        walkerClass.setCodeGenerator(getValue, gvIV);
      }

      // constructor - no args, forward to SUPER, intialize field if needed
      { // protect us from leaky locals
        CodeMethod m_WalkerBase_init = c_WalkerBase.getConstructor(CodeUtils.EMPTY_LIST);
        GeneratedCodeMethod init = walkerClass.createMethod("<init>",
                                                            CodeUtils.TYPE_VOID,
                                                            CodeUtils.EMPTY_LIST,
                                                            CodeUtils.ACC_PUBLIC);
        InstructionVector initIV = new InstructionVector();
        initIV.add(ByteCode.make_aload(init.getThis()));
        initIV.add(ByteCode.make_invokespecial(m_WalkerBase_init));
        if (c_retClass != CodeUtils.TYPE_VOID) {
          initIV.add(ByteCode.make_aload(init.getThis()));
          initIV.add(ByteCode.make_aconst_null());
          initIV.add(ByteCode.make_putfield(f_value));
        }
        initIV.add(ByteCode.make_return());
        walkerClass.setCodeGenerator(init, initIV);
      }

      return classLoader.defineClass(walkerClass);
    } catch (CodeException ce) {
      throw new AssertionFailure("Unable to generate walker code", ce);
    } catch (NoSuchMethodException nsme) {
      throw new AssertionFailure("Unable to generate walker code", nsme);
    }
  }
}

