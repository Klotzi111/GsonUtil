package de.klotzi111.util.GsonUtil;

import java.lang.reflect.*;
import java.util.List;
import java.util.ListIterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import de.klotzi111.util.GsonUtil.typeadapters.*;

// TODO: throw custom exception to be able to catch them
public class GsonUtil {
	private static final Field Gson_constructorConstructor;
	private static final Field Gson_jsonAdapterFactory;
	private static final Method JsonAdapterAnnotationTypeAdapterFactory_getTypeAdapter;
	private static final Field Gson_factories;
	private static final Field Gson_complexMapKeySerialization;
	private static final Method ReflectiveTypeAdapterFactory_getBoundFields;
	private static final Constructor<?> TypeAdapterRuntimeTypeWrapper_constructor;

	static {
		try {
			Gson_constructorConstructor = Gson.class.getDeclaredField("constructorConstructor");
			Gson_constructorConstructor.setAccessible(true);

			Gson_jsonAdapterFactory = Gson.class.getDeclaredField("jsonAdapterFactory");
			Gson_jsonAdapterFactory.setAccessible(true);

			JsonAdapterAnnotationTypeAdapterFactory_getTypeAdapter = JsonAdapterAnnotationTypeAdapterFactory.class.getDeclaredMethod("getTypeAdapter", ConstructorConstructor.class, Gson.class, TypeToken.class,
				JsonAdapter.class);
			JsonAdapterAnnotationTypeAdapterFactory_getTypeAdapter.setAccessible(true);

			Gson_factories = Gson.class.getDeclaredField("factories");
			Gson_factories.setAccessible(true);

			Gson_complexMapKeySerialization = Gson.class.getDeclaredField("complexMapKeySerialization");
			Gson_complexMapKeySerialization.setAccessible(true);

			ReflectiveTypeAdapterFactory_getBoundFields = ReflectiveTypeAdapterFactory.class.getDeclaredMethod("getBoundFields", Gson.class, TypeToken.class, Class.class);
			ReflectiveTypeAdapterFactory_getBoundFields.setAccessible(true);

			Class<?> clazz = Class.forName("com.google.gson.internal.bind.TypeAdapterRuntimeTypeWrapper");
			TypeAdapterRuntimeTypeWrapper_constructor = clazz.getDeclaredConstructor(Gson.class, TypeAdapter.class, Type.class);
			TypeAdapterRuntimeTypeWrapper_constructor.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException | NoSuchMethodException | ClassNotFoundException e) {
			throw new RuntimeException("Failed to load all required reflective accessors", e);
		}
	}

	/**
	 * This is just for convenience in other code to quickly create a Gson instance with all the handy type adapters of this library
	 *
	 * @param typeAdapterFactorys
	 *            type adapter factories that should be added before the type adapters of the library
	 * @return the gson builder
	 */
	public static GsonBuilder getPreConfiguredGsonBuilder(List<TypeAdapterFactory> typeAdapterFactorys) {
		GsonBuilder gb = new GsonBuilder()
			.enableComplexMapKeySerialization()
			.serializeNulls()
			.serializeSpecialFloatingPointValues()
			.addSerializationExclusionStrategy(new CheckExposeAnnotationExclusionStrategy(false))
			.addDeserializationExclusionStrategy(new CheckExposeAnnotationExclusionStrategy(true))
			.setPrettyPrinting();
		// must be first / as early as possible so it gets called first and then calls the delegate
		gb.registerTypeAdapterFactory(new SerializationUpdaterTypeAdapterFactory());
		if (typeAdapterFactorys != null) {
			for (TypeAdapterFactory factory : typeAdapterFactorys) {
				gb.registerTypeAdapterFactory(factory);
			}
		}
		gb.registerTypeAdapterFactory(new MapJsonObjectTypeAdapterFactory())
			.registerTypeAdapterFactory(new InlineFieldJsonObjectTypeAdapterFactory());
		if (ByteArrayAsHexTypeAdapterFactory.canBeUsed()) {
			gb.registerTypeAdapterFactory(new ByteArrayAsHexTypeAdapterFactory());
		}
		return gb;
	}

	public static TypeAdapter<?> getJsonAdapterAnnotationTypeAdapter(ConstructorConstructor constructorConstructor, Gson gson, TypeToken<?> type, JsonAdapter annotation) {
		try {
			JsonAdapterAnnotationTypeAdapterFactory jaa = (JsonAdapterAnnotationTypeAdapterFactory) Gson_jsonAdapterFactory.get(gson);
			// the getTypeAdapter should be static but is not
			return (TypeAdapter<?>) JsonAdapterAnnotationTypeAdapterFactory_getTypeAdapter.invoke(jaa, constructorConstructor, gson, type, annotation);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("Failed to get reflection field", e);
		}
	}

	public static ConstructorConstructor getConstructorConstructor(Gson gson) {
		try {
			return (ConstructorConstructor) Gson_constructorConstructor.get(gson);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Failed to get reflection field", e);
		}
	}

	public static <T> ObjectConstructor<T> getObjectConstructor(Gson gson, TypeToken<T> type) {
		return getConstructorConstructor(gson).get(type);
	}

	public static <T> T createInstance(Gson gson, TypeToken<T> type) {
		return getObjectConstructor(gson, type).construct();
	}

	@SuppressWarnings("unchecked")
	public static ReflectiveTypeAdapterFactory getReflectiveTypeAdapterFactory(Gson gson) {
		try {
			List<TypeAdapterFactory> factories = (List<TypeAdapterFactory>) Gson_factories.get(gson);
			// search backwards because so we find it faster
			ListIterator<TypeAdapterFactory> iter = factories.listIterator(factories.size());
			while (iter.hasPrevious()) {
				TypeAdapterFactory factory = iter.previous();
				if (factory instanceof ReflectiveTypeAdapterFactory) {
					return (ReflectiveTypeAdapterFactory) factory;
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Failed to get reflection field", e);
		}
		return null;
	}

	public static boolean getComplexMapKeySerialization(Gson gson) {
		try {
			return (boolean) Gson_complexMapKeySerialization.get(gson);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Failed to get reflection field", e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> TypeAdapter<T> createTypeAdapterRuntimeTypeWrapper(Gson gson, TypeAdapter<T> delegate, Type type) {
		try {
			return (TypeAdapter<T>) TypeAdapterRuntimeTypeWrapper_constructor.newInstance(gson, delegate, type);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException("Failed to get reflection field", e);
		}
	}

}
