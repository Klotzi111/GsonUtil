package de.klotzi111.util.GsonUtil;

import static de.klotzi111.util.GsonUtil.GsonUtilTestHelper.GSON;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import de.klotzi111.util.GsonUtil.typeadapters.DataTestObject;

public class GsonObjectMapperTest {

	public static class TestObjectWithObjectFields {

		public String name;
		public int data;
		@SerializedName("nameJustForSerialization")
		public DataTestObject testObject;
		public Object someOtherObject;
		@Expose(serialize = false)
		public Object someOtherObjectNotSerialized;

		public TestObjectWithObjectFields(String name, int data, DataTestObject testObject, Object someOtherObject, Object someOtherObjectNotSerialized) {
			this.name = name;
			this.data = data;
			this.testObject = testObject;
			this.someOtherObject = someOtherObject;
			this.someOtherObjectNotSerialized = someOtherObjectNotSerialized;
		}

	}

	@Test
	public void testGsonObjectMapper() throws Exception {
		DataTestObject testObject = new DataTestObject("Hello", 100);
		UUID someOtherObject = UUID.randomUUID();
		AtomicInteger someOtherObjectNotSerialized = new AtomicInteger();
		// this variable is used to ensure that the string instance is the same later when the instance is removed from the map
		String testString = "World";
		TestObjectWithObjectFields testObjectWithObjectFields = new TestObjectWithObjectFields(testString, 420, testObject, someOtherObject, someOtherObjectNotSerialized);

		HashMap<String, Object> map = GsonObjectMapper.convertObjectToMapViaGsonRules(GSON, testObjectWithObjectFields, null, false);
		assertTrue(map.remove("name", testString));
		assertTrue(map.remove("data", 420));
		assertTrue(map.remove("nameJustForSerialization", testObject));
		assertTrue(map.remove("someOtherObject", someOtherObject));
		// this field should not be serialized
		assertNull(map.remove("someOtherObjectNotSerialized"));
		assertEquals(0, map.size());

		JsonObject testObjectWithObjectFieldsAsJsonObject = GSON.toJsonTree(testObjectWithObjectFields).getAsJsonObject();
		HashMap<String, String> stringMap = GsonObjectMapper.convertJsonObjectToStringMap(testObjectWithObjectFieldsAsJsonObject);
		assertEquals(testString, stringMap.remove("name"));
		assertEquals("420", stringMap.remove("data"));
		// this will not be the value of someOtherObject.toString()
		// because convertJsonObjectToStringMap works on JsonElement basis
		// this means it will be toString() of a JsonObject with name and data field
		assertEquals(GSON.toJsonTree(testObject).toString(), stringMap.remove("nameJustForSerialization"));
		assertEquals(someOtherObject.toString(), stringMap.remove("someOtherObject"));
		// this field should not be serialized
		assertNull(stringMap.remove("someOtherObjectNotSerialized"));
		assertEquals(0, stringMap.size());
	}

}
