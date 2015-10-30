/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */

package com.googlecode.psiprobe.tools;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * The Class ReflectiveAccessor.
 *
 * @author Mark Lewis
 */
public class ReflectiveAccessor implements Accessor {

  /** The reflection factory. */
  private static Object reflectionFactory;
  
  /** The new field accessor. */
  private static Method newFieldAccessor;
  
  /** The get. */
  private static Method get;

  /**
   * Instantiates a new reflective accessor.
   *
   * @throws ClassNotFoundException the class not found exception
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   * @throws NoSuchMethodException the no such method exception
   */
  ReflectiveAccessor() throws ClassNotFoundException, InstantiationException,
      IllegalAccessException, NoSuchMethodException {

    init();
  }

  public Object get(Object obj, Field field) {
    try {
      Object fieldAccessor = getFieldAccessor(field);
      if (fieldAccessor != null) {
        return get.invoke(fieldAccessor, new Object[] {obj});
      }
    } catch (Exception ex) {
      // ignore
    }
    return null;
  }

  /**
   * Gets the field accessor.
   *
   * @param field the field
   * @return the field accessor
   * @throws IllegalAccessException the illegal access exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws InvocationTargetException the invocation target exception
   */
  private static Object getFieldAccessor(Field field) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {

    if (newFieldAccessor.getParameterTypes().length == 1) {
      return newFieldAccessor.invoke(reflectionFactory, new Object[] {field});
    } else {
      return newFieldAccessor.invoke(reflectionFactory, new Object[] {field, Boolean.TRUE});
    }
  }

  /**
   * Inits the.
   *
   * @throws ClassNotFoundException the class not found exception
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   * @throws NoSuchMethodException the no such method exception
   */
  private static final void init() throws ClassNotFoundException, InstantiationException,
      IllegalAccessException, NoSuchMethodException {

    String vmVendor = System.getProperty("java.vm.vendor");
    if (vmVendor != null
        && (vmVendor.indexOf("Sun Microsystems") != -1 || vmVendor.indexOf("Apple Computer") != -1
            || vmVendor.indexOf("Apple Inc.") != -1 || vmVendor.indexOf("IBM Corporation") != -1)) {

      reflectionFactory = getReflectionFactory();
      String vmVer = System.getProperty("java.runtime.version");
      Class[] paramTypes;
      if (vmVer.startsWith("1.4")) {
        paramTypes = new Class[] {Field.class};
      } else {
        paramTypes = new Class[] {Field.class, Boolean.TYPE};
      }
      newFieldAccessor = reflectionFactory.getClass().getMethod("newFieldAccessor", paramTypes);
      get = newFieldAccessor.getReturnType().getMethod("get", new Class[] {Object.class});
    }
  }

  /**
   * Gets the reflection factory.
   *
   * @return the reflection factory
   * @throws ClassNotFoundException the class not found exception
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  private static Object getReflectionFactory() throws ClassNotFoundException,
      InstantiationException, IllegalAccessException {

    Class getReflectionFactoryActionClass =
        Class.forName("sun.reflect.ReflectionFactory$GetReflectionFactoryAction");
    PrivilegedAction getReflectionFactoryAction =
        (PrivilegedAction) getReflectionFactoryActionClass.newInstance();
    return AccessController.doPrivileged(getReflectionFactoryAction);
  }

}
