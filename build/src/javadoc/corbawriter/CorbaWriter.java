package javadoc.corbawriter;

import java.io.*;
import com.sun.javadoc.*;

public class CorbaWriter extends Doclet {
  public static File destRoot;
  
  public static int optionLength(String option) {
    if(option.equals("-d")) {
      return 2;
    }
    return -1;
  }
  
  public static boolean validOptions(String[][] options, DocErrorReporter reporter) {
    for(int i = 0; i < options.length; i++) {
      String [] option = options[i];
      if(option[0].equals("-d")) {
        destRoot = new File(option[1]);
      }
    }
    return true;
  }
  
  public static boolean start(RootDoc root) {
    ClassDoc[] classes = root.classes();
    for(int i = 0; i < classes.length; i++) {
      ClassDoc cd = classes[i];
      if(cd.typeName().startsWith("_")) {
        continue;
      }
      String implBase = "_" + cd.typeName() + "ImplBase";
      ClassDoc implDoc = cd.findClass(implBase);
      if(implDoc == null) {
        continue;
      }
      
      try {
        writeTie(cd, implDoc);
        writeOperations(cd, implDoc);
      } catch (Exception e) {
        System.out.println("Unable to process files");
        e.printStackTrace(System.out);
        return false;
      }
    }
    return true;
  }
  
  public static void writeTie(ClassDoc corbaInterface, ClassDoc implBase)
  throws Exception {
    String tieClassName = "_" + corbaInterface.typeName() + "_Tie";
    String operatorsClassName = "_" + corbaInterface.typeName() + "_Operations";
    
    File packagePath = createPackagePath(corbaInterface.containingPackage().name());
    packagePath.mkdirs();
    File tieFile = new File(packagePath, tieClassName + ".java");
    tieFile.createNewFile();
    PrintWriter out = new PrintWriter(new FileOutputStream(tieFile));
    System.out.println("Writing " + tieFile);
    
    out.println("package " +
                corbaInterface.containingPackage().name() +
                ";\n");
    PackageDoc [] importPackages = corbaInterface.importedPackages();
    for(int i = 0; i < importPackages.length; i++) {
      out.println("import " + importPackages[i].name() + ".*;");
    }
    
    // start class
    out.println("\npublic class " + tieClassName +
                " extends " + implBase.typeName() + " {");
    // standard class header - servant
    out.println("  public " + operatorsClassName + " servant;");
    out.println("  public " + tieClassName + "(" + operatorsClassName +
                " servant) {");
    out.println("    this.servant = servant;");
    out.println("  }");
    
    // write delegate methods for all methods defined by corba interface
    writeWrapperFuncs(out, corbaInterface);
    
    // end class
    out.println("}");
    
    out.flush();
    out.close();
  }
  
  public static ClassDoc isCorba(ClassDoc cd) {
    String implBase = "_" + cd.typeName() + "ImplBase";
    ClassDoc implDoc = cd.findClass(implBase);
    return implDoc;
  }
  
  public static void writeWrapperFuncs(PrintWriter out, ClassDoc cd) {
    ClassDoc [] interfaces = cd.interfaces();
    for(int i = 0; i < interfaces.length; i++) {
      ClassDoc implDoc = isCorba(interfaces[i]);
      if(implDoc != null) {
        writeWrapperFuncs(out, interfaces[i]);
      }
    }

    MethodDoc [] methods = cd.methods();
    for(int j = 0; j < methods.length; j++) {
      MethodDoc method = methods[j];
      out.print("\n  public " + method.returnType() + " " +
                method.name() + "(");
      Parameter [] params = method.parameters();
      for(int k = 0; k < params.length; k++) {
        out.print(params[k].toString());
        if(k < params.length-1) {
          out.print(", ");
        }
      }
      out.print(")");
      ClassDoc [] exceptions = method.thrownExceptions();
      if(exceptions.length > 0) {
        out.print(" throws ");
        for(int k = 0; k < exceptions.length; k++) {
          out.print(exceptions[k].qualifiedName());
          if(k < exceptions.length-1) {
            out.print(", ");
          }
        }
      }
      out.println(" {");
      out.print("    ");
      if(!method.returnType().typeName().equals("void")) {
        out.print("return ");
      }
      out.print("servant." + method.name() + "(this");
      for(int k = 0; k < params.length; k++) {
        out.print(", " + params[k].name());
      }
      out.println(");");
      out.println("  }");
    }
  }
  
  public static void writeOperations(ClassDoc corbaInterface, ClassDoc implBase)
  throws Exception {
    ClassDoc [] interfaces = corbaInterface.interfaces();
    String extraParam =
      corbaInterface.typeName().substring(0, 1).toLowerCase() +
      corbaInterface.typeName().substring(1);
    String operatorsClassName = "_" + corbaInterface.typeName() + "_Operations";

    File packagePath = createPackagePath(corbaInterface.containingPackage().name());
    packagePath.mkdirs();
    File operFile = new File(packagePath, operatorsClassName + ".java");
    operFile.createNewFile();
    PrintWriter out = new PrintWriter(new FileOutputStream(operFile));
    System.out.println("Writing " + operFile);
    
    out.println("package " +
                corbaInterface.containingPackage().name() +
                 ";\n");
    PackageDoc [] importPackages = corbaInterface.importedPackages();
    for(int i = 0; i < importPackages.length; i++) {
      out.println("import " + importPackages[i].name() + ".*;");
    }
    
    // start class
    out.print("\npublic interface " + operatorsClassName);
    boolean doesInherit = false;
    boolean isFirst = true;
    for(int i = 0; i < interfaces.length; i++) {
      if(isCorba(interfaces[i]) != null) {
        if(doesInherit == false) {
          doesInherit = true;
          out.print(" extends ");
        }
        if(isFirst) {
          isFirst = false;
        } else {
          out.print(", ");
        }
        out.print(interfaces[i].containingPackage().name() +
                  "._" + interfaces[i].typeName() + "_Operations");
      }
    }
    out.println(" {");

    MethodDoc [] methods = corbaInterface.methods();
    for(int j = 0; j < methods.length; j++) {
      MethodDoc method = methods[j];
      out.print("\n  public " + method.returnType() + " " +
                method.name() + "(org.omg.CORBA.portable.ObjectImpl " +
                extraParam);
      Parameter [] params = method.parameters();
      for(int k = 0; k < params.length; k++) {
        out.print(", " + params[k].toString());
      }
      out.print(")");
      ClassDoc [] exceptions = method.thrownExceptions();
      if(exceptions.length > 0) {
        out.print(" throws ");
        for(int k = 0; k < exceptions.length; k++) {
          out.print(exceptions[k].qualifiedName());
          if(k < exceptions.length-1) {
            out.print(", ");
          }
        }
      }
      out.println(";");
    }
    
    out.println("}");
    
    out.flush();
    out.close();
  }
  
  public static File createPackagePath(String packageName) {
    StringBuffer sb = new StringBuffer(packageName);
    for(int i = 0; i < sb.length(); i++) {
      if(sb.charAt(i) == '.') {
        sb.replace(i, i+1, System.getProperty("file.separator"));
      }
    }
    File pathFile = new File(destRoot, sb.toString());
    return pathFile;
  }
}

