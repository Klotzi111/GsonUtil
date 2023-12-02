package de.klotzi111.util.GsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import de.klotzi111.util.GsonUtil.exception.ExceptionCollectionException;
import de.klotzi111.util.GsonUtil.reflection.BoundField;
import de.klotzi111.util.GsonUtil.reflection.BoundFieldHelper;

public class GsonObjectMapper {

	private GsonObjectMapper() {
	}

	@SuppressWarnings("unchecked")
	public static <T> HashMap<String, Object> convertObjectToMapViaGsonRules(Gson gson, T object, TypeToken<T> typeToken, boolean ignoreSingleExceptions) throws Exception {
		if (typeToken == null) {
			// if the typeToken was not supplied we use the token from the object's class
			typeToken = (TypeToken<T>) TypeToken.get(object.getClass());
		}
		Map<String, BoundField> boundFields = BoundFieldHelper.getBoundFields(gson, typeToken);

		HashMap<String, Object> result = new HashMap<>();
		List<Throwable> exceptions = new ArrayList<>();
		int triedFieldCount = 0;
		for (Entry<String, BoundField> boundFieldEntry : boundFields.entrySet()) {
			BoundField field = boundFieldEntry.getValue();
			if (field == null || !field.serialized) {
				// if a key has no applicable field or it should not be serialized we just skip it
				continue;
			}

			triedFieldCount++;
			try {
				Object value = field.getValue(object);
				result.put(boundFieldEntry.getKey(), value);
			} catch (Exception e) {
				if (!ignoreSingleExceptions) {
					throw e;
				}
				// something failed. Continue but remember exception
				exceptions.add(e);
			}
		}

		if (exceptions.size() == triedFieldCount) {
			// if all fields failed we can throw the exceptions
			throw new ExceptionCollectionException(exceptions);
		}

		return result;
	}

	public static HashMap<String, String> convertJsonObjectToStringMap(JsonObject jsonObject) {
		HashMap<String, String> map = new HashMap<String, String>(jsonObject.size());
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			JsonElement jsonValue = entry.getValue();
			String value = GsonSerializationHelper.convertJsonElementToString(jsonValue);
			map.put(entry.getKey(), value);
		}
		return map;
	}

}
