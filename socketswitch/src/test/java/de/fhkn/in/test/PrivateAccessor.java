package de.fhkn.in.test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Assert;

/**
 * Provides access to private members in classes.
 */
public class PrivateAccessor {
	public static Object getPrivateField(Object o, String fieldName) {
		// Check we have valid arguments
		Assert.assertNotNull(o);
		Assert.assertNotNull(fieldName);
		// Go and find the private field...
		final Field fields[] = o.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; ++i) {
			if (fieldName.equals(fields[i].getName())) {
				try {
					fields[i].setAccessible(true);
					return fields[i].get(o);
				} catch (IllegalAccessException ex) {
					Assert.fail("IllegalAccessException accessing " + fieldName);
				}
			}
		}
		Assert.fail("Field '" + fieldName + "' not found");
		return null;
	}
	public static Method getPrivateMethod(Object o, String methodName) {
		// Check we have valid arguments
		Assert.assertNotNull(o);
		Assert.assertNotNull(methodName);
		// Go and find the private field...
		final Method[] methods = o.getClass().getDeclaredMethods();
		for (int i = 0; i < methods.length; ++i) {
			if (methods.equals(methods[i].getName())) {
					methods[i].setAccessible(true);
					//invoke it
					return methods[i];
			}
		}
		Assert.fail("Method '" + methodName + "' not found");
		return null;
	}
}