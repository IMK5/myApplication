package test.java;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import java.lang.reflect.Method;

import java.util.Objects;

/**
 *           How to check if two instances of a class have same field values
 */


public class Util {
    public static <T> boolean haveSamePropertyValues (Class<T> type, T t1, T t2)
                throws Exception {

          BeanInfo beanInfo = Introspector.getBeanInfo(type);
          for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
              Method m = pd.getReadMethod();
              Object o1 = m.invoke(t1);
              Object o2 = m.invoke(t2);
              if (!Objects.equals(o1, o2)) {
                  return false;
              }
          }
          return true;
      }
}
