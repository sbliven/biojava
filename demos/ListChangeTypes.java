import java.util.*;
import java.lang.reflect.*;
import org.biojava.utils.*;

/**
 * Simple utility to print the known ChangeTypes for any
 * Changeable BioJava class.
 *
 * @author Thomas Down
 */

public class ListChangeTypes {
    public static void main(String[] args) throws Exception {
	String clazzName;
	try {
	    clazzName = args[0];
	} catch (IndexOutOfBoundsException ex) {
	    System.err.println("Usage: ListChangeTypes <className>");
	    return;
	}

	Class clazz = ListChangeTypes.class.getClassLoader().loadClass(clazzName);
	if (!Changeable.class.isAssignableFrom(clazz)) {
	    System.err.println(clazzName + " is not Changeable");
	    return;
	}

	Set ct = ChangeType.getChangeTypes(clazz);
	if (ct.size() > 0) {
	    System.out.println("ChangeTypes in " + clazzName + ":");
	    System.out.println();
	    for (Iterator i = ct.iterator(); i.hasNext(); ) {
		ChangeType change = (ChangeType) i.next();
		Field f = change.getField();
		System.out.println(f.getDeclaringClass().getName() + "." + f.getName() + ":");
		System.out.println("    " + change.getName());
	    }
	} else {
	    System.out.println("Changeable `" + clazzName + "' has no defined ChangeTypes");
	}
    }
}
