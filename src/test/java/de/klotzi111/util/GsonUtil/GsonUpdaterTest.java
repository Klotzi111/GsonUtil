package de.klotzi111.util.GsonUtil;

import static de.klotzi111.util.GsonUtil.GsonUtilTestHelper.GSON;
import static de.klotzi111.util.GsonUtil.GsonUtilTestHelper.getAsJsonObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.Objects;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import de.klotzi111.util.GsonUtil.typeadapters.DataTestObject;

public class GsonUpdaterTest {

	@Test
	public void testGsonUpdater() throws Exception {
		DataTestObject testObject = new DataTestObject("Test", 1);

		// different ways to get to an JsonObject can be used to update the object
		// this just ensures that this object is serialized and deserialized as expected
		JsonObject updateJsonDirect = getAsJsonObject("{'name':'Hello','data':100}");
		DataTestObject updateObject = new DataTestObject("Hello", 100);
		JsonObject updateJson = GSON.toJsonTree(updateObject).getAsJsonObject();
		assertEquals(updateJsonDirect, updateJson);
		assertEquals(updateObject, GSON.fromJson(updateJsonDirect, DataTestObject.class));
		//

		GsonUpdater.updateObjectMerge(GSON, testObject, updateJson, false, true);
		assertNotSame(updateObject, testObject);
		assertEquals(updateObject, testObject);

		updateJsonDirect = getAsJsonObject("{}");
		GsonUpdater.updateObjectMerge(GSON, testObject, updateJsonDirect, false, true);
		assertNotSame(updateObject, testObject);
		assertEquals(updateObject, testObject);

		updateJsonDirect = getAsJsonObject("{'data':-99}");
		GsonUpdater.updateObjectMerge(GSON, testObject, updateJsonDirect, false, true);
		updateObject.data = -99;
		assertNotSame(updateObject, testObject);
		assertEquals(updateObject, testObject);
	}

	public static class SpecialTestObject {

		@Expose(deserialize = false)
		public String name;
		@SerializedName(value = "data", alternate = {"value", "v"})
		public int data;

		public SpecialTestObject(String name, int data) {
			this.name = name;
			this.data = data;
		}

		@Override
		public int hashCode() {
			return Objects.hash(data, name);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof SpecialTestObject)) {
				return false;
			}
			SpecialTestObject other = (SpecialTestObject) obj;
			return data == other.data && Objects.equals(name, other.name);
		}

	}

	@Test
	public void testGsonUpdaterSpecialCases() throws Exception {
		SpecialTestObject testObject = new SpecialTestObject("Test", 1);

		{
			// name should not be set/deserialized because we set that to false in the @Expose annotation
			JsonObject updateJson = getAsJsonObject("{'name':'Hello','data':100}");
			GsonUpdater.updateObjectMerge(GSON, testObject, updateJson, false, true);
			SpecialTestObject expectedObject = new SpecialTestObject("Test", 100);
			assertEquals(expectedObject, testObject);
		}

		{
			// test with alternative name
			// last value will be taken because it overwrites values before
			JsonObject updateJson = getAsJsonObject("{'value':200,'v':300}");
			GsonUpdater.updateObjectMerge(GSON, testObject, updateJson, false, true);
			SpecialTestObject expectedObject = new SpecialTestObject("Test", 300);
			assertEquals(expectedObject, testObject);
		}

	}

}
