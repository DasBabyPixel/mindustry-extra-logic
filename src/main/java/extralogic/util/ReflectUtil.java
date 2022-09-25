package extralogic.util;

import java.lang.reflect.Field;

public class ReflectUtil {

	public static void setField(Field field, Object owner, Object value) {
		try {
			field.set(owner, value);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getField(Field field, Object owner, Class<T> type) {
		try {
			if (type != null) {
				return type.cast(field.get(owner));
			}
			return (T) field.get(owner);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Field findField(Class<?> clazz, String field, boolean access) {
		try {
			Field f = clazz.getDeclaredField(field);
			if (access)
				f.setAccessible(true);
			return f;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
