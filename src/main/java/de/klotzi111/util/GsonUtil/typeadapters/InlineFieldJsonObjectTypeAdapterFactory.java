package de.klotzi111.util.GsonUtil.typeadapters;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.*;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;

import de.klotzi111.util.GsonUtil.GsonUtil;
import de.klotzi111.util.GsonUtil.interfaces.inlinefield.InlineField;
import de.klotzi111.util.GsonUtil.interfaces.inlinefield.InlineFieldJsonObject;
import de.klotzi111.util.GsonUtil.reflection.BoundField;
import de.klotzi111.util.GsonUtil.reflection.BoundFieldHelper;
import de.klotzi111.util.GsonUtil.reflection.BoundFieldWithAnnotation;
import de.klotzi111.util.GsonUtil.typeadapter.AbstractEnhancedTypeAdapterFactory;
import de.klotzi111.util.GsonUtil.typeadapter.JsonDeSerializerBundle;

public class InlineFieldJsonObjectTypeAdapterFactory extends AbstractEnhancedTypeAdapterFactory {

	private static class GsonInlineFieldJsonObjectAdapter implements JsonSerializer<InlineFieldJsonObject>, JsonDeserializer<InlineFieldJsonObject> {

		private final ObjectConstructor<InlineFieldJsonObject> objectConstructor;

		private final BoundField inlineFieldDeserialize;
		private final BoundField inlineFieldSerialize;

		public GsonInlineFieldJsonObjectAdapter(ObjectConstructor<InlineFieldJsonObject> objectConstructor, BoundField inlineFieldDeserialize, BoundField inlineFieldSerialize) {
			this.objectConstructor = objectConstructor;
			this.inlineFieldDeserialize = inlineFieldDeserialize;
			this.inlineFieldSerialize = inlineFieldSerialize;
		}

		private static BoundField getBoundFieldForType(Gson gson, TypeToken<?> type, boolean isSerialization) {
			Map<String, BoundField> boundFields = BoundFieldHelper.getBoundFields(gson, type);
			BoundFieldWithAnnotation<InlineField> inlineFieldWithAnnotation = BoundFieldHelper.getSingleFieldWithAnnotation(boundFields, isSerialization, InlineField.class);
			if (!BoundFieldHelper.checkOnlyValidFields(boundFields, isSerialization, inlineFieldWithAnnotation)) {
				throw new JsonParseException(
					"An object implementing \"InlineFieldJsonObject\" must have exactly one field annotated with \"@InlineField\" and must not have other fields that would be de-serialized than key and value fields");
			}
			return inlineFieldWithAnnotation.boundField;
		}

		private static void checkInlineField(BoundField inlineField) {
			// we want to throw the exception if the get an instance of the type to de-/serialize not before when we already check the type
			if (inlineField == null) {
				throw new JsonParseException(
					"An object implementing \"InlineFieldJsonObject\" must have exactly one field annotated with \"@InlineField\" and must not have other fields that would be de-serialized than key and value fields");
			}
		}

		@Override
		public InlineFieldJsonObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonNull()) {
				return null;
			}

			BoundField inlineField = inlineFieldDeserialize;
			checkInlineField(inlineField);

			InlineFieldJsonObject returnObject = objectConstructor.construct();
			try {
				inlineField.fromJsonTree(json, returnObject);
				return returnObject;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new JsonParseException("Could not set field value for fields of type \"" + typeOfT.getTypeName() + "\"", e);
			}
		}

		@Override
		public JsonElement serialize(InlineFieldJsonObject src, Type typeOfSrc, JsonSerializationContext context) {
			if (src == null) {
				return JsonNull.INSTANCE;
			}

			BoundField inlineField = inlineFieldSerialize;
			checkInlineField(inlineField);

			try {
				return inlineField.toJsonTree(src);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new JsonParseException("Could not get field value for fields of type \"" + typeOfSrc.getTypeName() + "\"", e);
			}
		}
	}

	private <T> boolean isApplicableForType(TypeToken<T> type) {
		return InlineFieldJsonObject.class.isAssignableFrom(type.getRawType());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> JsonDeSerializerBundle<T> makeSerializers(Gson gson, TypeToken<T> type) {
		// TODO: allow @InlineField on every Object without this interface?
		if (!isApplicableForType(type)) {
			return null;
		}
		ObjectConstructor<T> objectConstructor = GsonUtil.getObjectConstructor(gson, type);

		BoundField inlineFieldDeserialize = GsonInlineFieldJsonObjectAdapter.getBoundFieldForType(gson, type, false);
		BoundField inlineFieldSerialize = GsonInlineFieldJsonObjectAdapter.getBoundFieldForType(gson, type, true);

		GsonInlineFieldJsonObjectAdapter fd = new GsonInlineFieldJsonObjectAdapter((ObjectConstructor<InlineFieldJsonObject>) objectConstructor, inlineFieldDeserialize, inlineFieldSerialize);
		return (JsonDeSerializerBundle<T>) new JsonDeSerializerBundle<InlineFieldJsonObject>(fd, fd, null);
	}

}
