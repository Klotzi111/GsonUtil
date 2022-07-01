package de.klotzi111.util.GsonUtil.typeadapters;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.*;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;

import de.klotzi111.util.GsonUtil.GsonUpdater;
import de.klotzi111.util.GsonUtil.GsonUtil;
import de.klotzi111.util.GsonUtil.interfaces.inlinefield.InlineFieldJsonObject;
import de.klotzi111.util.GsonUtil.interfaces.map.MapJsonObject;
import de.klotzi111.util.GsonUtil.interfaces.required.Required;
import de.klotzi111.util.GsonUtil.reflection.BoundField;
import de.klotzi111.util.GsonUtil.reflection.BoundFieldHelper;
import de.klotzi111.util.GsonUtil.reflection.BoundFieldWithAnnotation;
import de.klotzi111.util.GsonUtil.typeadapter.AbstractEnhancedTypeAdapterFactory;
import de.klotzi111.util.GsonUtil.typeadapter.JsonDeSerializerBundle;

public class RequiredTypeAdapterFactory extends AbstractEnhancedTypeAdapterFactory {

	private static class RequiredAdapter implements JsonDeserializer<Object> {

		private final Gson gson;
		private final ObjectConstructor<Object> objectConstructor;

		private final Map<String, BoundField> boundFields;
		private final List<BoundField> requiredFields;

		public RequiredAdapter(Gson gson, ObjectConstructor<Object> objectConstructor, Map<String, BoundField> boundFields, List<BoundField> requiredFields) {
			this.gson = gson;
			this.objectConstructor = objectConstructor;
			this.boundFields = boundFields;
			this.requiredFields = requiredFields;
		}

		@Override
		public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			// we have at least one field with Required annotation
			if (json.isJsonNull()) {
				return null;
			}

			if (!json.isJsonObject()) {
				throw new JsonParseException("Failed to deserialize type \"" + typeOfT.getTypeName() + "\": Json must be an json object");
			}

			Object returnObject = objectConstructor.construct();
			try {
				GsonUpdater.updateObjectRaw(gson, returnObject, boundFields, requiredFields, json.getAsJsonObject(), true);
				return returnObject;
			} catch (Exception e) {
				throw new JsonParseException("Failed to deserialize type \"" + typeOfT.getTypeName() + "\"", e);
			}
		}
	}

	private <T> boolean isApplicableForType(TypeToken<T> type) {
		Class<?> rawType = type.getRawType();
		boolean fieldValuesInlined = MapJsonObject.class.isAssignableFrom(rawType) || InlineFieldJsonObject.class.isAssignableFrom(rawType);
		if (!(Object.class.isAssignableFrom(rawType) && !fieldValuesInlined)) {
			return false;
		}
		return !rawType.getName().startsWith("java.");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> JsonDeSerializerBundle<T> makeSerializers(Gson gson, TypeToken<T> type) {
		if (!isApplicableForType(type)) {
			return null;
		}

		Map<String, BoundField> boundFields;
		List<BoundField> requiredFieldsOnly;
		try {

			boundFields = BoundFieldHelper.getBoundFields(gson, type);
			List<BoundFieldWithAnnotation<Required>> requiredFields = BoundFieldHelper.getFieldsWithAnnotation(boundFields, false, Required.class);
			requiredFieldsOnly = requiredFields.stream().map(field -> field.boundField).collect(Collectors.toList());
			if (requiredFields.size() == 0) {
				// if the type has no field with Required annotation we would not do anything special so let the default impl handle this
				return null;
			}
		} catch (Exception e) {
			// ignore the error just respond that we can not handle that type
			return null;
		}

		ObjectConstructor<T> objectConstructor = GsonUtil.getObjectConstructor(gson, type);

		RequiredAdapter fd = new RequiredAdapter(gson, (ObjectConstructor<Object>) objectConstructor, boundFields, requiredFieldsOnly);
		return (JsonDeSerializerBundle<T>) new JsonDeSerializerBundle<Object>(fd, null, null);
	}

}
