package example;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionHelper {

	public static void set(Class<?> clazz, String fieldName, Object object, Object value) {
		try {
			Field f = clazz.getDeclaredField(fieldName);
			f.setAccessible(true);
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
			f.set(object, value);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
