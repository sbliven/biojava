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

package org.biojava.utils.xml;

import java.util.*;
import java.beans.*;
import org.w3c.dom.*;

/**
 * Construct java beans from XML elements
 *
 * @author Thomas Down
 */

public class XMLBeans {
    public final static XMLBeans INSTANCE;

    static {
	INSTANCE = new XMLBeans();
    }

    protected XMLBeans() {
    }

    public Object instantiateBean(Element bel) 
        throws AppException
    {
	return instantiateBean(bel, ClassLoader.getSystemClassLoader(), Collections.EMPTY_MAP);
    }

    public Object instantiateBean(Element bel, ClassLoader cloader, Map beanRefs) 
        throws AppException
    {
	String cl = bel.getAttribute("jclass");
	if (cl == null)
	    throw new AppException("No jclass attribute");

	Object bean = null;

	try {
	    Class clazz = cloader.loadClass(cl);
	    bean = clazz.newInstance();
	    configureBean(bean, bel, beanRefs);
	    if (bean instanceof Initializable)
		((Initializable) bean).init();   // FIXME
	} catch (ClassNotFoundException ex) {
	    throw new AppException("Couldn't load bean class " + cl);
        } catch (ClassCastException ex) {
	    throw new AppException("Does not implement AppBean: " + cl);
	} catch (InstantiationException ex) {
	    throw new AppException("Couldn't intantiate bean " + cl);
	} catch (IllegalAccessException ex) {
	    throw new AppException("Couldn't access constructor for bean " + cl);
	}

	return bean;
    }

    private void configureBean(Object bean, Element el, Map refs) 
        throws AppException
    {
	Class clazz = bean.getClass();

	Node child = el.getFirstChild();
	while (child != null) {
	    if (child instanceof Element) {
		Element echild = (Element) child;
		String tag = echild.getTagName();
		if (tag.equals("string")) {
		    String name = echild.getAttribute("name");
		    String value = echild.getAttribute("value");
		    setProp(clazz, bean, name, value, value.getClass());
		} else if (tag.equals("bean")) {
		    String name = echild.getAttribute("name");
		    String ref = echild.getAttribute("ref");
		    Object targ = null;
		    if (ref != null) {
			targ = refs.get(ref);
		    } else {
			targ = instantiateBean(echild);
		    }
		    setProp(clazz, bean, name, targ, targ.getClass());
		} else if (tag.equals("int")) {
		    String name = echild.getAttribute("name");
		    String value = echild.getAttribute("value");
		    try {
			int val = Integer.parseInt(value);
			setProp(clazz, bean, name, new Integer(val), Integer.TYPE);
		    } catch (NumberFormatException ex) {
			throw new AppException("Invalid int: " + value);
		    }
		} else if (tag.equals("double")) {
		    String name = echild.getAttribute("name");
		    String value = echild.getAttribute("value");
		    try {
			double val = Double.parseDouble(value);
			setProp(clazz, bean, name, new Double(val), Double.TYPE);
		    } catch (NumberFormatException ex) {
			throw new AppException("Invalid double: " + value);
		    }
		} else if (tag.equals("boolean")) {
		    String name = echild.getAttribute("name");
		    String value = echild.getAttribute("value");
		    Boolean val = Boolean.valueOf(value);
		    setProp(clazz, bean, name, val, Boolean.TYPE);
		} else if (tag.equals("child")) {
		    if (! (bean instanceof Collection))
			throw new AppException("Only Collections can have children");
		    Object childBean = instantiateBean(echild);
		    ((Collection) bean).add(childBean);
		}
	    }
	    child = child.getNextSibling();
	}
    }

    private void setProp(Class clazz, Object bean, String prop, Object value, Class ourType) 
        throws AppException
    {
	BeanInfo bi = null;

	try {
	    bi = Introspector.getBeanInfo(clazz);
	} catch (IntrospectionException ex) {
	    throw new AppException("Couldn't introspect class " + bean.getClass().getName());
	}
	PropertyDescriptor[] descs = bi.getPropertyDescriptors();
	for (int i = 0; i < descs.length; ++i) {
	    if (descs[i].getName().equals(prop)) {
		PropertyDescriptor desc = descs[i];
		if (! desc.getPropertyType().isAssignableFrom(ourType)) {
		    throw new AppException("Property " + prop + " is not assignable from " + ourType.getName());
		}
		Object[] obj = new Object[1];
		obj[0] = value;
		try {
		    desc.getWriteMethod().invoke(bean, obj);
		} catch (Exception ex) {
		    throw new AppException("Invocation failed");
		}
		return;
	    }
	}
	throw new AppException("Couldn't find property " + prop + " in class " + clazz.getName());
    }
}
 
