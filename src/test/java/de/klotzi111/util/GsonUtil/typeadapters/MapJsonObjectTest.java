package de.klotzi111.util.GsonUtil.typeadapters;

import static de.klotzi111.util.GsonUtil.GsonUtilTestHelper.GSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.*;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import de.klotzi111.util.GsonUtil.interfaces.map.MapJsonObject;
import de.klotzi111.util.GsonUtil.interfaces.map.MapKey;
import de.klotzi111.util.GsonUtil.interfaces.map.MapValue;

public class MapJsonObjectTest {

	private static abstract class AbstractMapJsonObjectTestObject implements MapJsonObject {
		@MapKey
		public String key;

		public AbstractMapJsonObjectTestObject(String key) {
			this.key = key;
		}

		public abstract JsonObject getDataAsJsonObject();

		@Override
		public int hashCode() {
			return Objects.hash(key, getDataAsJsonObject());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof AbstractMapJsonObjectTestObject)) {
				return false;
			}
			AbstractMapJsonObjectTestObject other = (AbstractMapJsonObjectTestObject) obj;
			return Objects.equals(key, other.key) && Objects.equals(getDataAsJsonObject(), other.getDataAsJsonObject());
		}

	}

	private static class MapJsonObjectTestObject extends AbstractMapJsonObjectTestObject {

		@MapValue
		public DataTestObject data;

		public MapJsonObjectTestObject(String key, DataTestObject data) {
			super(key);
			this.data = data;
		}

		@Override
		public JsonObject getDataAsJsonObject() {
			return GSON.toJsonTree(data).getAsJsonObject();
		}

	}

	private static class MapJsonObjectTestObjectRaw extends AbstractMapJsonObjectTestObject {

		@MapValue
		public JsonObject data;

		public MapJsonObjectTestObjectRaw(String key, JsonObject data) {
			super(key);
			this.data = data;
		}

		@Override
		public JsonObject getDataAsJsonObject() {
			return data;
		}

	}

	@Test
	public void testMapJsonObject() {
		List<AbstractMapJsonObjectTestObject> mapAsList = new ArrayList<>();
		MapJsonObjectTestObject testObject = new MapJsonObjectTestObject("Test1", new DataTestObject("dataTest1", 10));
		mapAsList.add(testObject);
		MapJsonObjectTestObject testObject2 = new MapJsonObjectTestObject("Test2", new DataTestObject("dataTest2", 20));
		mapAsList.add(testObject2);
		MapJsonObjectTestObjectRaw testObjectRaw = new MapJsonObjectTestObjectRaw("Test3", GSON.toJsonTree(new DataTestObject("dataTest3", 30)).getAsJsonObject());
		mapAsList.add(testObjectRaw);
		JsonElement json = GSON.toJsonTree(mapAsList, new TypeToken<List<MapJsonObjectTestObject>>() {}.getType());

		// test that serialization from runtime type also works correctly with the type adapter
		JsonElement jsonViaRuntimeType = GSON.toJsonTree(mapAsList);
		assertEquals(json, jsonViaRuntimeType);

		// deserialize as map and compare values
		Map<String, JsonObject> mapReadBack = GSON.fromJson(json, new TypeToken<LinkedHashMap<String, JsonObject>>() {}.getType());
		assertEquals(mapAsList.size(), mapReadBack.size());
		int i = 0;
		for (Entry<String, JsonObject> entry : mapReadBack.entrySet()) {
			AbstractMapJsonObjectTestObject testObjectFromList = mapAsList.get(i++);
			MapJsonObjectTestObjectRaw compareObject = new MapJsonObjectTestObjectRaw(entry.getKey(), entry.getValue());
			assertEquals(testObjectFromList, compareObject);
		}

		// also deserialize back to list
		List<MapJsonObjectTestObject> mapAsListReadBack = GSON.fromJson(json, new TypeToken<List<MapJsonObjectTestObject>>() {}.getType());
		assertEquals(mapAsList, mapAsListReadBack);
	}

	private static class MapJsonObjectTestObjectNoAnnotation implements MapJsonObject {
		@MapKey
		public String key;
		@SuppressWarnings("unused")
		public String value;

		public MapJsonObjectTestObjectNoAnnotation(String key, String value) {
			this.key = key;
			this.value = value;
		}

	}

	private static class MapJsonObjectTestObjectTooManyFields implements MapJsonObject {
		@MapKey
		public String key;
		@MapValue
		public String value;
		@MapValue
		public String other;

		public MapJsonObjectTestObjectTooManyFields(String key, String value, String other) {
			this.key = key;
			this.value = value;
			this.other = other;
		}

	}

	@Test
	public void testMapJsonObjectAbnormalConditions() {
		List<AbstractMapJsonObjectTestObject> mapAsList = new ArrayList<>();
		MapJsonObjectTestObject testObject = new MapJsonObjectTestObject("Test1", new DataTestObject("dataTest1", 10));
		mapAsList.add(testObject);

		// this should throw an exception because now we have two times the same key and we have not explicitly said on the annotation that duplicate keys are allowed
		List<AbstractMapJsonObjectTestObject> mapAsListCopy = new ArrayList<>(mapAsList);
		MapJsonObjectTestObject testObjectDupe = new MapJsonObjectTestObject("Test1", new DataTestObject("dataTest4", 40));
		mapAsListCopy.add(testObjectDupe);
		assertThrows(JsonParseException.class, () -> GSON.toJsonTree(mapAsListCopy));

		// this should give us an json array because we did not explicitly say what type we want to serialize so it must be determined at runtime
		// and because we have now an object in the list that is not of the required interface type the type adapter should fallback to the delegate and that is the normal array serialization
		List<Object> mapAsListCopy2 = new ArrayList<>(mapAsList);
		String randomOtherObject = "Test";
		mapAsListCopy2.add(randomOtherObject);
		JsonElement json = GSON.toJsonTree(mapAsListCopy2);
		assertEquals(2, json.getAsJsonArray().size());

		// this should throw an exception because we explicitly say what type we want but the actual value type is incompatible to the list's parameter type declaration
		assertThrows(JsonParseException.class, () -> GSON.toJsonTree(mapAsListCopy2, new TypeToken<List<MapJsonObjectTestObject>>() {}.getType()));

		// invalid classes
		List<MapJsonObjectTestObjectNoAnnotation> testObjectNoAnno = Arrays.asList(new MapJsonObjectTestObjectNoAnnotation("Test", "value"));
		assertThrows(JsonParseException.class, () -> GSON.toJsonTree(testObjectNoAnno));

		List<MapJsonObjectTestObjectTooManyFields> testObjectTooManyFields = Arrays.asList(new MapJsonObjectTestObjectTooManyFields("Test2", "value", "other"));
		assertThrows(JsonParseException.class, () -> GSON.toJsonTree(testObjectTooManyFields));
	}

}
