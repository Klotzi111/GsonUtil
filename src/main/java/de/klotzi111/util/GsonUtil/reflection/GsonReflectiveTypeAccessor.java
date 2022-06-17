package de.klotzi111.util.GsonUtil.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.reflect.ReflectionHelper;
import com.google.gson.reflect.TypeToken;

import de.klotzi111.util.GsonUtil.GsonUtil;

/**
 * The code of this class is from {@link com.google.gson.internal.bind.ReflectiveTypeAdapterFactory} at Gson version 2.9.0 but with additions and made publicly visible
 * TODO: Update the code in here when the original code changes
 */
public class GsonReflectiveTypeAccessor {
	public final Gson gson;
	private final ConstructorConstructor constructorConstructor;
	private final FieldNamingStrategy fieldNamingPolicy;
	private final Excluder excluder;

	private static final Map<Gson, GsonReflectiveTypeAccessor> GSON_REFLECTIVE_TYPE_ACCESSOR_CACHE = new WeakHashMap<>();

	public static GsonReflectiveTypeAccessor fromGson(Gson gson) {
		GsonReflectiveTypeAccessor accessor = GSON_REFLECTIVE_TYPE_ACCESSOR_CACHE.get(gson);
		if (accessor == null) {
			accessor = new GsonReflectiveTypeAccessor(gson);
			GSON_REFLECTIVE_TYPE_ACCESSOR_CACHE.put(gson, accessor);
		}
		return accessor;
	}

	@SuppressWarnings("deprecation")
	protected GsonReflectiveTypeAccessor(Gson gson) {
		this.gson = gson;
		constructorConstructor = GsonUtil.getConstructorConstructor(gson);
		fieldNamingPolicy = gson.fieldNamingStrategy();
		excluder = gson.excluder();
	}

	public boolean excludeField(Field f, boolean serialize) {
		return excludeField(f, serialize, excluder);
	}

	public static boolean excludeField(Field f, boolean serialize, Excluder excluder) {
		return !excluder.excludeClass(f.getType(), serialize) && !excluder.excludeField(f, serialize);
	}

	/**
	 *
	 * @param f
	 *            the field
	 * @return first element holds the default name
	 */
	public List<String> getFieldNames(Field f) {
		SerializedName annotation = f.getAnnotation(SerializedName.class);
		if (annotation == null) {
			String name = fieldNamingPolicy.translateName(f);
			return Collections.singletonList(name);
		}

		String serializedName = annotation.value();
		String[] alternates = annotation.alternate();
		if (alternates.length == 0) {
			return Collections.singletonList(serializedName);
		}

		List<String> fieldNames = new ArrayList<String>(alternates.length + 1);
		fieldNames.add(serializedName);
		Collections.addAll(fieldNames, alternates);
		return fieldNames;
	}

	private BoundField createBoundField(
		final Gson context, final Field field, final String name,
		final TypeToken<?> fieldType, boolean serialize, boolean deserialize, boolean isFirstFieldName) {
		JsonAdapter annotation = field.getAnnotation(JsonAdapter.class);
		TypeAdapter<?> mapped = null;
		if (annotation != null) {
			mapped = GsonUtil.getJsonAdapterAnnotationTypeAdapter(constructorConstructor, context, fieldType, annotation);
		}
		final boolean jsonAdapterPresent = mapped != null;
		if (mapped == null) {
			mapped = context.getAdapter(fieldType);
		}

		TypeAdapter<?> serializeTypeAdapter = jsonAdapterPresent ? mapped : GsonUtil.createTypeAdapterRuntimeTypeWrapper(gson, mapped, fieldType.getType());
		return new BoundField(field, fieldType, jsonAdapterPresent, mapped, serializeTypeAdapter, name, isFirstFieldName, serialize, deserialize);
	}

	public Map<String, BoundField> getBoundFields(TypeToken<?> type, Class<?> raw) {
		Map<String, BoundField> result = new LinkedHashMap<String, BoundField>();
		if (raw.isInterface()) {
			return result;
		}

		Type declaredType = type.getType();
		while (raw != Object.class) {
			Field[] fields = raw.getDeclaredFields();
			for (Field field : fields) {
				boolean serialize = excludeField(field, true);
				boolean deserialize = excludeField(field, false);
				if (!serialize && !deserialize) {
					continue;
				}
				ReflectionHelper.makeAccessible(field);
				Type fieldType = $Gson$Types.resolve(type.getType(), raw, field.getGenericType());
				List<String> fieldNames = getFieldNames(field);
				BoundField previous = null;
				for (int i = 0, size = fieldNames.size(); i < size; ++i) {
					String name = fieldNames.get(i);
					boolean isFirstFieldName = i == 0;
					if (i != 0) {
						serialize = false; // only serialize the default name
					}
					BoundField boundField = createBoundField(gson, field, name,
						TypeToken.get(fieldType), serialize, deserialize, isFirstFieldName);
					BoundField replaced = result.put(name, boundField);
					if (previous == null) {
						previous = replaced;
					}
				}
				if (previous != null) {
					throw new IllegalArgumentException(declaredType
						+ " declares multiple JSON fields named " + previous.serializationName);
				}
			}
			type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
			raw = type.getRawType();
		}
		return result;
	}

}
