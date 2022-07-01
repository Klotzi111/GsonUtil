package de.klotzi111.util.GsonUtil.typeadapters;

import static de.klotzi111.util.GsonUtil.GsonUtilTestHelper.GSON;
import static de.klotzi111.util.GsonUtil.GsonUtilTestHelper.getAsJsonObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Objects;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.klotzi111.util.GsonUtil.interfaces.inlinefield.InlineField;
import de.klotzi111.util.GsonUtil.interfaces.inlinefield.InlineFieldJsonObject;
import de.klotzi111.util.GsonUtil.interfaces.required.Required;

public class RequiredTest {

	private static class RequiredTestObject {
		@Required
		public String name;
		@Required
		public int value;

		public RequiredTestObject(String name, int value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, value);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof RequiredTestObject)) {
				return false;
			}
			RequiredTestObject other = (RequiredTestObject) obj;
			return Objects.equals(name, other.name) && value == other.value;
		}

	}

	private static class RequiredTestObjectWithInline implements InlineFieldJsonObject {
		@Required
		@InlineField
		public int value;

		public RequiredTestObjectWithInline(int value) {
			this.value = value;
		}

	}

	@Test
	public void testRequired() {
		RequiredTestObject testObject = new RequiredTestObject("Test", 11);
		JsonElement json = GSON.toJsonTree(testObject);

		// test deserialize
		RequiredTestObject testObjectReadBack = GSON.fromJson(json, RequiredTestObject.class);
		assertEquals(testObject, testObjectReadBack);

		// with null property value
		JsonObject testJson = getAsJsonObject("{'name':null,'value':99}");
		assertEquals(new RequiredTestObject(null, 99), GSON.fromJson(testJson, RequiredTestObject.class));

		// test deserialize with missing json property
		assertThrows(JsonParseException.class, () -> {
			GSON.fromJson(getAsJsonObject("{}"), RequiredTestObject.class);
		});

		assertThrows(JsonParseException.class, () -> {
			GSON.fromJson(getAsJsonObject("{'name':'Hi'}"), RequiredTestObject.class);
		});

		assertThrows(JsonParseException.class, () -> {
			GSON.fromJson(getAsJsonObject("{'value':99}"), RequiredTestObject.class);
		});

		// test class that also implements value inlining interface
		RequiredTestObjectWithInline testObjectInline = new RequiredTestObjectWithInline(11);
		JsonElement inlineJson = GSON.toJsonTree(testObjectInline);
		assertEquals(testObjectInline.value, inlineJson.getAsInt());
	}

}
