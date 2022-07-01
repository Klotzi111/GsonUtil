package de.klotzi111.util.GsonUtil.typeadapters;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.*;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;

import de.klotzi111.util.GsonUtil.GsonUtil;
import de.klotzi111.util.GsonUtil.interfaces.map.MapJsonObject;
import de.klotzi111.util.GsonUtil.interfaces.map.MapKey;
import de.klotzi111.util.GsonUtil.interfaces.map.MapValue;
import de.klotzi111.util.GsonUtil.reflection.BoundField;
import de.klotzi111.util.GsonUtil.reflection.BoundFieldHelper;
import de.klotzi111.util.GsonUtil.reflection.BoundFieldWithAnnotation;
import de.klotzi111.util.GsonUtil.typeadapter.AbstractEnhancedTypeAdapterFactory;
import de.klotzi111.util.GsonUtil.typeadapter.JsonDeSerializerBundle;

public class MapJsonObjectTypeAdapterFactory extends AbstractEnhancedTypeAdapterFactory {

	private static class GsonMapJsonObjectAdapter implements JsonSerializer<Collection<MapJsonObject>>, JsonDeserializer<Collection<MapJsonObject>> {

		private final Gson gson;
		private final boolean complexMapKeySerialization;
		private final ObjectConstructor<Collection<MapJsonObject>> collectionConstructor;
		private final boolean unknownParameterType;
		private final ObjectConstructor<MapJsonObject> objectConstructor;
		private final TypeAdapter<Collection<?>> delegate;

		public GsonMapJsonObjectAdapter(Gson gson, boolean complexMapKeySerialization, ObjectConstructor<Collection<MapJsonObject>> collectionConstructor,
			boolean unknownParameterType, ObjectConstructor<MapJsonObject> objectConstructor, TypeAdapter<Collection<?>> delegate) {
			this.gson = gson;
			this.complexMapKeySerialization = complexMapKeySerialization;
			this.collectionConstructor = collectionConstructor;
			this.unknownParameterType = unknownParameterType;
			this.objectConstructor = objectConstructor;
			this.delegate = delegate;
		}

		private static final Map<TypeToken<?>, KeyAndValueFieldBundle> KEY_AND_VALUE_FIELD_FOR_TYPE_CACHE = new HashMap<>();

		private KeyAndValueFieldBundle getKeyAndValueFieldBundleForType(TypeToken<?> type, boolean isSerialization) {
			KeyAndValueFieldBundle keyAndValueField = KEY_AND_VALUE_FIELD_FOR_TYPE_CACHE.get(type);
			if (keyAndValueField == null) {
				Map<String, BoundField> boundFields = BoundFieldHelper.getBoundFields(gson, type);
				BoundFieldWithAnnotation<MapKey> keyFieldWithAnnotation = BoundFieldHelper.getSingleFieldWithAnnotation(boundFields, isSerialization, MapKey.class);
				BoundFieldWithAnnotation<MapValue> valueFieldWithAnnotation = BoundFieldHelper.getSingleFieldWithAnnotation(boundFields, isSerialization, MapValue.class);
				if (!BoundFieldHelper.checkOnlyValidFields(boundFields, isSerialization, keyFieldWithAnnotation, valueFieldWithAnnotation)) {
					throw new JsonParseException(
						"An object implementing \"MapJsonObject\" must have exactly one field annotated with \"@MapKey\" and one with\"@MapValue\" and must not have other fields that would be de-serialized than key and value fields");
				}
				boolean allowDuplicateKeys = keyFieldWithAnnotation.annotation.allowDuplicateKeys();
				keyAndValueField = new KeyAndValueFieldBundle(keyFieldWithAnnotation.boundField, allowDuplicateKeys, valueFieldWithAnnotation.boundField);
				KEY_AND_VALUE_FIELD_FOR_TYPE_CACHE.put(type, keyAndValueField);
			}
			return keyAndValueField;
		}

		@Override
		public Collection<MapJsonObject> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonNull()) {
				return null;
			}

			if (unknownParameterType) {
				throw new JsonParseException("This type adapter can not deserialize this json data because the given type to deserialize to does not tell what type the collection paramter has");
			}

			// must be exactly one parameter because of type token check
			Type parameterType = ((ParameterizedType) typeOfT).getActualTypeArguments()[0];
			KeyAndValueFieldBundle keyAndValueField = getKeyAndValueFieldBundleForType(TypeToken.get(parameterType), false);

			if (!json.isJsonObject()) {
				throw new JsonParseException("The json element for an object implementing \"MapJsonObject\" must be an json object");
			}

			Map<JsonElement, JsonElement> jsonDeserMap = context.deserialize(json, new TypeToken<LinkedHashMap<JsonElement, JsonElement>>() {}.getType());

			Collection<MapJsonObject> collection = collectionConstructor.construct();
			try {
				for (Entry<JsonElement, JsonElement> entry : jsonDeserMap.entrySet()) {
					MapJsonObject returnObject = objectConstructor.construct();

					// thats a bit inperformant because we parse the JsonReader to a JsonElement and now do it back but it should be fine. Gson does that itself in some places
					keyAndValueField.keyField.fromJsonTree(entry.getKey(), returnObject);
					keyAndValueField.valueField.fromJsonTree(entry.getValue(), returnObject);

					collection.add(returnObject);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new JsonParseException("Could not set field value for fields of type \"" + parameterType.getTypeName() + "\"", e);
			}
			return collection;
		}

		private static class KeyAndValueFieldBundle {
			public final BoundField keyField;
			public final boolean allowDuplicateKeys;
			public final BoundField valueField;

			public KeyAndValueFieldBundle(BoundField keyField, boolean allowDuplicateKeys, BoundField valueField) {
				this.keyField = keyField;
				this.allowDuplicateKeys = allowDuplicateKeys;
				this.valueField = valueField;
			}
		}

		@Override
		public JsonElement serialize(Collection<MapJsonObject> src, Type typeOfSrc, JsonSerializationContext context) {
			if (src == null) {
				return JsonNull.INSTANCE;
			}

			Map<Object, JsonElement> jsonSerMap = new LinkedHashMap<>();
			// iterate as Object here because we might get a Collection that has specified the parameter type and/or its values are not all of correct type
			for (Object object : src) {
				if (!MapJsonObject.class.isAssignableFrom(object.getClass())) {
					if (unknownParameterType) {
						// here we assume that the given list was not meant to be value inlined map
						// so we call the delegate
						return delegate.toJsonTree(src);
					} else {
						throw new JsonParseException("A value of the collection that specified its parameter type to be \"MapJsonObject\" is not of that type");
					}
				}
				// we need to get the key and value field for every real value type because there might be objects which's class is not always the same for all values and those might be invalid as well
				TypeToken<?> objectTypeToken = TypeToken.get(object.getClass());
				KeyAndValueFieldBundle keyAndValueField = getKeyAndValueFieldBundleForType(objectTypeToken, true);

				MapJsonObject gsonMapJsonObject = (MapJsonObject) object;
				try {
					Object keyFieldValueJson = complexMapKeySerialization ? keyAndValueField.keyField.toJsonTree(gsonMapJsonObject) : String.valueOf(keyAndValueField.keyField.getValue(gsonMapJsonObject));
					JsonElement valueFieldValueJson = keyAndValueField.valueField.toJsonTree(gsonMapJsonObject);
					JsonElement replacedJsonElement = jsonSerMap.put(keyFieldValueJson, valueFieldValueJson);
					if (replacedJsonElement != null) {
						if (!keyAndValueField.allowDuplicateKeys) {
							throw new JsonSyntaxException("duplicate key: " + keyFieldValueJson);
						}
						// else we do NOT throw an duplicate key exception because original gson does also not throw on duplicate keys when serializing
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new JsonParseException("Could not get field value for fields of type \"" + objectTypeToken.toString() + "\"", e);
				}
			}
			return context.serialize(jsonSerMap, complexMapKeySerialization ? MAP_JSON_ELEMENT_TO_JSON_ELEMENT_TYPE_TOKEN.getType() : MAP_STRING_TO_JSON_ELEMENT_TYPE_TOKEN.getType());
		}
	}

	private static final TypeToken<Object> OBJECT_TYPE_TOKEN = TypeToken.get(Object.class);
	private static final TypeToken<MapJsonObject> MAP_JSON_OBJECT_TYPE_TOKEN = TypeToken.get(MapJsonObject.class);
	private static final TypeToken<Map<JsonElement, JsonElement>> MAP_JSON_ELEMENT_TO_JSON_ELEMENT_TYPE_TOKEN = new TypeToken<Map<JsonElement, JsonElement>>() {};
	private static final TypeToken<Map<String, JsonElement>> MAP_STRING_TO_JSON_ELEMENT_TYPE_TOKEN = new TypeToken<Map<String, JsonElement>>() {};

	@SuppressWarnings("deprecation")
	private <T> TypeToken<?> isApplicableForType(TypeToken<T> type) {
		Type nativeType = type.getType();
		// this code check whether the type is a Collection and the parameter type is of instance MapJsonObject: Collection<MapJsonObject>
		if (nativeType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = ((ParameterizedType) nativeType);
			if (Collection.class.isAssignableFrom(type.getRawType())) {
				Type[] params = parameterizedType.getActualTypeArguments();
				if (params.length == 1) {
					Type parameterType = params[0];
					if (MAP_JSON_OBJECT_TYPE_TOKEN.isAssignableFrom(parameterType)) {
						return TypeToken.get(parameterType);
					}
				}
			}
		} else if (nativeType instanceof Class) {
			// we land here when an implementation of Collection is serialized directly via gson without suppling the type via TypeToken
			// so we get runtime types here
			Class<?> nativeTypeClass = (Class<?>) nativeType;
			if (Collection.class.isAssignableFrom(nativeTypeClass)) {
				return OBJECT_TYPE_TOKEN;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> JsonDeSerializerBundle<T> makeSerializers(Gson gson, TypeToken<T> type) {
		// TODO: allow @MapKey and @MapValue on every Object without this interface?
		TypeToken<?> parameterType = isApplicableForType(type);
		if (parameterType == null) {
			return null;
		}

		boolean unknownParameterType = parameterType == OBJECT_TYPE_TOKEN;
		ObjectConstructor<T> collectionConstructor = GsonUtil.getObjectConstructor(gson, type);
		ObjectConstructor<?> objectConstructor = unknownParameterType ? null : GsonUtil.getObjectConstructor(gson, parameterType);
		boolean complexMapKeySerialization = GsonUtil.getComplexMapKeySerialization(gson);
		TypeAdapter<T> delegate = unknownParameterType ? getDelegate(gson, type) : null;

		GsonMapJsonObjectAdapter fd = new GsonMapJsonObjectAdapter(gson, complexMapKeySerialization, (ObjectConstructor<Collection<MapJsonObject>>) collectionConstructor,
			unknownParameterType, (ObjectConstructor<MapJsonObject>) objectConstructor, (TypeAdapter<Collection<?>>) delegate);
		// we can not deserialize if we do not know the real type of the collection parameter and probably the wanted parameter type was incompatible anyways so we do not return an deserializer
		return (JsonDeSerializerBundle<T>) new JsonDeSerializerBundle<Collection<MapJsonObject>>(unknownParameterType ? null : fd, fd, null);
	}

}
