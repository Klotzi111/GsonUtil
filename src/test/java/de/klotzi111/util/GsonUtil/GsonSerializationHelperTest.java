package de.klotzi111.util.GsonUtil;

import static de.klotzi111.util.GsonUtil.GsonUtilTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import de.klotzi111.util.GsonUtil.typeadapters.DataTestObject;

public class GsonSerializationHelperTest {

	@Test
	public void testGsonSerializationHelper() throws Exception {
		String jsonString = replaceQuotations("{'name':'Hello','data':100}");
		DataTestObject testObject = new DataTestObject("Hello", 100);

		JsonObject jsonObject = getAsJsonObject(jsonString);
		JsonObject testObjectAsJsonObject = GSON.toJsonTree(testObject).getAsJsonObject();
		assertEquals(jsonObject, testObjectAsJsonObject);
		// this string will not be generated using Gson instance because of that the string will not be pretty printed
		String elementToString = GsonSerializationHelper.convertJsonElementToString(jsonObject);
		assertEquals(jsonString, elementToString);

		// this string will be pretty printed and so does not match the string from above
		String gsonPrintedJsonString = GSON.toJson(jsonObject);
		assertNotEquals(jsonString, gsonPrintedJsonString);

		byte[] jsonStringBytes = gsonPrintedJsonString.getBytes(GsonSerializationHelper.JSON_SERIALIZATION_CHARSET);
		byte[] jsonSerializedToBytes = GsonSerializationHelper.serializeToBytes(GSON, jsonObject);
		assertArrayEquals(jsonStringBytes, jsonSerializedToBytes);

		DataTestObject testObjectDeserializedFromBytes = GsonSerializationHelper.deserializeFromBytes(GSON, jsonSerializedToBytes, DataTestObject.class);
		assertEquals(testObject, testObjectDeserializedFromBytes);
	}

}
