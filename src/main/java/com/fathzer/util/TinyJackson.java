package com.fathzer.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** A simplified version of Jackson Databind that only works on beans (default constructor + set and get accessors).
 * <br>Jackson is cool, very easy to use, but fat, very fat (more than 4MB).
 * <br>Json from json.org is light, but has nothing to convert json to an object (except JSONObject).
 * <br>This class, adds bean parsing to the org.json library. Additionally, it provides you with method to convert beans and arrays
 * to JSONObject and JSONArray (json.org ones have strange naming convention: for instance an object's attribute named 'aChar' is transformed
 * in a JSON attribute named 'AChar'. 
 * <br><br>Current limitations:
 * <li>All attributes of bean should be present in JSON</li>
 * <li>The following types are not supported as bean's attributes: byte, short</li>
 * </ul>
 */
public class TinyJackson {
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface JsonIgnore {
	    public boolean key() default true;
	}
	
	private TinyJackson() {
		super();
	}

	public static <T> T toObject(JSONObject json, Class<T> tClass) {
		try {
			final T result = tClass.getConstructor().newInstance();
			final Field[] fields = tClass.getDeclaredFields();
			for (Field field : fields) {
				if (field.getAnnotation(JsonIgnore.class)==null) {
					final Class<?> attrClass = field.getType();
					final String name = field.getName();
					final Method method = tClass.getMethod(getSetMethodName(name), attrClass);
					final Object value = getValue(json, attrClass, name);
					method.invoke(result, value);
				}
			}
			return result;
		} catch (ReflectiveOperationException | IllegalArgumentException e) {
			throw new JSONException(e);
		}
	}
	
	private static Object getValue(JSONObject json, Class<?> attrClass, String name) {
		if (attrClass.isArray()) {
			return toArray(json.getJSONArray(name), attrClass.getComponentType());
		} else if (attrClass.equals(String.class)) {
			return json.getString(name);
		} else if (attrClass.equals(int.class)) {
			return json.getInt(name);
		} else if (attrClass.equals(long.class)) {
			return json.getLong(name);
		} else if (attrClass.equals(float.class)) {
			return json.getFloat(name);
		} else if (attrClass.equals(double.class)) {
			return json.getDouble(name);
		} else if (attrClass.equals(boolean.class)) {
			return json.getBoolean(name);
		} else if (attrClass.equals(char.class)) {
			final Object obj = json.get(name);
			if (obj instanceof Character c) {
				return c.charValue();
			} else if (obj instanceof String string) {
				if (string.length()!=1) {
					throw new IllegalArgumentException(name+ "attribute length != 1");
				}
				return string.charAt(0);
			} else {
				throw new IllegalArgumentException("Unexpected type "+obj.getClass()+" for char attribute "+name);
			}
		} else {
			// java object
			return toObject(json.getJSONObject(name), attrClass);
		}
	}

	private static Object getValue(JSONArray json, Class<?> attrClass, int index) {
		if (attrClass.isArray()) {
			//TODO To be tested
			return toArray(json.getJSONArray(index), attrClass.getComponentType());
		} else if (attrClass.equals(String.class)) {
			return json.getString(index);
		} else if (attrClass.equals(int.class)) {
			return json.getInt(index);
		} else if (attrClass.equals(long.class)) {
			return json.getLong(index);
		} else if (attrClass.equals(float.class)) {
			return json.getFloat(index);
		} else if (attrClass.equals(double.class)) {
			return json.getDouble(index);
		} else if (attrClass.equals(boolean.class)) {
			return json.getBoolean(index);
		} else if (attrClass.equals(char.class)) {
			final String string = json.getString(index);
			if (string.length()!=1) {
				throw new IllegalArgumentException(index+ "attribute length != 1");
			}
			return string.charAt(0);
		} else {
			// java object
			return toObject(json.getJSONObject(index), attrClass);
		}
	}

	private static String getSetMethodName(String attrName) {
		return "set"+attrName.substring(0,1).toUpperCase()+attrName.substring(1);
	}

	private static String getGetMethodName(Field field) {
		final String attrName = field.getName();
		final String prefix = field.getType().equals(boolean.class) ? "is" : "get";
		return prefix+attrName.substring(0,1).toUpperCase()+attrName.substring(1);
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(JSONArray json, Class<T> toClass) {
		T[] result = (T[]) Array.newInstance(toClass, json.length());
		for (int i=0;i<result.length;i++) {
			result[i] = (T) getValue(json, toClass, i);
		}
		return result;
	}
	
	public static JSONObject toJSONObject(Object obj) {
		final JSONObject result = new JSONObject();
		try {
			final Class<?> tClass = obj.getClass();
			final Field[] fields = tClass.getDeclaredFields();
			for (Field field : fields) {
				if (field.getAnnotation(JsonIgnore.class)==null) {
					final Class<?> attrClass = field.getType();
					final String name = field.getName();
					final Method method = tClass.getMethod(getGetMethodName(field));
					final Object attr = method.invoke(obj);
					if (attrClass.equals(String.class) || attrClass.equals(boolean.class) || attrClass.equals(int.class) || attrClass.equals(long.class)
							 || attrClass.equals(float.class) || attrClass.equals(double.class) || attrClass.equals(char.class)) {
						result.put(name, attr);
					} else if (attrClass.isArray()) {
						result.put(name, toJSONArray((Object[])attr));
					} else {
						result.put(name, toJSONObject(attr));
					}
				}
			}
		} catch (ReflectiveOperationException | IllegalArgumentException e) {
			throw new JSONException(e);
		}
		return result;
	}
	
	public static <T> JSONArray toJSONArray(T[] array) {
		final JSONArray result = new JSONArray();
		final Class<?> elClass = array.getClass().componentType();
		for (T element : array) {
			if (elClass.equals(String.class) || elClass.equals(boolean.class) || elClass.equals(int.class) || elClass.equals(long.class)
						 || elClass.equals(float.class) || elClass.equals(double.class) || elClass.equals(char.class)) {
				result.put(element);
			} else if (elClass.isArray()) {
				result.put(toJSONArray((Object[])element));
			} else {
				result.put(toJSONObject(element));
			}
		}
		return result;
	}
}
