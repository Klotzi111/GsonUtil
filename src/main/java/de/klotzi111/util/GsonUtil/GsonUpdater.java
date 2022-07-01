package de.klotzi111.util.GsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import de.klotzi111.util.GsonUtil.component.ConflictStrategy;
import de.klotzi111.util.GsonUtil.component.GsonComponentExtender;
import de.klotzi111.util.GsonUtil.component.MergeStrategy;
import de.klotzi111.util.GsonUtil.exception.ExceptionCollectionException;
import de.klotzi111.util.GsonUtil.exception.JsonObjectExtensionConflictException;
import de.klotzi111.util.GsonUtil.reflection.BoundField;
import de.klotzi111.util.GsonUtil.reflection.BoundFieldHelper;

public class GsonUpdater {

	public static void updateObjectRaw(Gson gson, Object target, Map<String, BoundField> boundFields, List<BoundField> requiredFields, JsonObject json, boolean failFast) throws Exception {
		List<BoundField> requiredFieldsLeft = requiredFields == null ? new ArrayList<>() : new ArrayList<>(requiredFields);
		List<Throwable> exceptions = new ArrayList<>();
		for (Entry<String, JsonElement> entry : json.entrySet()) {
			BoundField field = boundFields.get(entry.getKey());
			if (field == null || !field.deserialized) {
				// if a key has no applicable field or it should not be deserialized we just skip it
				continue;
			}
			try {
				field.fromJsonTree(entry.getValue(), target);

				requiredFieldsLeft.remove(field);
			} catch (Exception e) {
				if (failFast) {
					throw e;
				}
				// something failed. Continue but remember exception
				exceptions.add(e);
			}
		}
		if (requiredFieldsLeft.size() > 0) {
			// not all required fields where used
			List<String> missingRequiredFieldNames = requiredFieldsLeft.stream().map(field -> field.serializationName).collect(Collectors.toList());
			Exception e = new JsonParseException("Some required fields where not used: " + String.join(", ", missingRequiredFieldNames));
			if (failFast) {
				throw e;
			}
			exceptions.add(e);
		}
		if (exceptions.size() > 0) {
			throw new ExceptionCollectionException(exceptions);
		}
	}

	/**
	 * Updates the {@code target} object from the {@code json} object using the fields of the type {@code typeToUpdateFor}. Only fields existing in {@code json} are affected.
	 * This method simply overwrites the values in the {@code target} object if an appropriate field is in the {@code json}.
	 * This method behaves like the default object deserialization from gson ({@link ReflectiveTypeAdapterFactory.Adapter}) but only for fields that exist for the type {@code typeToUpdateFor}, with an directly supplied object instance and enhanced exception control.
	 * <br>
	 * If this method throws an {@link ExceptionCollectionException} the {@code target} object has gotten as much updates as possible and the exceptions for failed fields are in the {@link ExceptionCollectionException}.
	 *
	 * @param gson
	 *            the gson instance
	 * @param target
	 *            the object to be updated
	 * @param typeToUpdateFor
	 *            the type from which the fields that are being used for overwrite are collected
	 * @param json
	 *            the {@link JsonObject} with the updated values
	 * @param failFast
	 *            whether this method should immediately fail and throw the exception when one occurs
	 * @throws ExceptionCollectionException
	 * @throws Exception
	 */
	public static void updateObjectRaw(Gson gson, Object target, TypeToken<?> typeToUpdateFor, JsonObject json, boolean failFast) throws Exception {
		Map<String, BoundField> boundFields = BoundFieldHelper.getBoundFields(gson, typeToUpdateFor);
		updateObjectRaw(gson, target, boundFields, null, json, failFast);
	}

	/**
	 * Updates the {@code target} object from the {@code json} object. Only fields existing in {@code json} are affected.
	 * This method simply overwrites the values in the {@code target} object if an appropriate field is in the {@code json}.
	 * This method behaves like the default object deserialization from gson ({@link ReflectiveTypeAdapterFactory.Adapter}) but with an directly supplied object instance and enhanced exception control.
	 * <br>
	 * If this method throws an {@link ExceptionCollectionException} the {@code target} object has gotten as much updates as possible and the exceptions for failed fields are in the {@link ExceptionCollectionException}.
	 *
	 * @param gson
	 *            the gson instance
	 * @param target
	 *            the object to be updated
	 * @param json
	 *            the {@link JsonObject} with the updated values
	 * @param failFast
	 *            whether this method should immediately fail and throw the exception when one occurs
	 * @throws ExceptionCollectionException
	 * @throws Exception
	 *
	 * @see #updateObjectRaw(Gson, Object, TypeToken, JsonObject, boolean)
	 */
	public static void updateObjectRaw(Gson gson, Object target, JsonObject json, boolean failFast) throws Exception {
		updateObjectRaw(gson, target, TypeToken.get(target.getClass()), json, failFast);
	}

	/**
	 * Updates {@code target} object using deep merge from the {@code json} object. (This leads to: Only fields existing in {@code json} are affected.)
	 * This method first serializes the {@code target} object to a {@link JsonObject} then deep merges that with the given {@code json} then deserializes the values back to the required type and finally sets the {@code target} object field values.
	 * <br>
	 * If this method throws an {@link ExceptionCollectionException} the {@code target} object has gotten as much updates as possible and the exceptions for failed fields are in the exception.
	 *
	 * @param gson
	 *            the gson instance
	 * @param target
	 *            the object to be updated
	 * @param json
	 *            the {@link JsonObject} with the updated values
	 * @param ignoreUpdateNullValues
	 *            whether null values in the update {@code json} should be ignored
	 * @param failFast
	 *            whether this method should immediately fail and throw the exception when one occurs
	 * @throws ExceptionCollectionException
	 * @throws Exception
	 *
	 * @see updateObjectRaw
	 */
	public static void updateObjectMerge(Gson gson, Object target, JsonObject json, boolean ignoreUpdateNullValues, boolean failFast) throws Exception {
		JsonElement targetJsonElement = gson.toJsonTree(target);
		if (!targetJsonElement.isJsonObject()) {
			throw new IllegalArgumentException("target does not serialize to json object");
		}
		JsonObject targetJson = targetJsonElement.getAsJsonObject();

		try {
			ConflictStrategy conflictStrategy = ignoreUpdateNullValues ? ConflictStrategy.NON_NULL_PREFER_SECOND_OBJ : ConflictStrategy.PREFER_SECOND_OBJ;
			GsonComponentExtender.extendJsonObject(targetJson, new MergeStrategy(false, true), conflictStrategy, json);
		} catch (JsonObjectExtensionConflictException e) {
			// should never happen because we do not use throw exception conflict strategy
		}

		updateObjectRaw(gson, target, targetJson, failFast);
	}

}
