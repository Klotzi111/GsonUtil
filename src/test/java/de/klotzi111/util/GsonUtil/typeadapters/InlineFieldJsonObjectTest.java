package de.klotzi111.util.GsonUtil.typeadapters;

import static de.klotzi111.util.GsonUtil.GsonUtilTestHelper.GSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Objects;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import de.klotzi111.util.GsonUtil.interfaces.inlinefield.InlineField;
import de.klotzi111.util.GsonUtil.interfaces.inlinefield.InlineFieldJsonObject;

public class InlineFieldJsonObjectTest {

	private static class InlineFieldJsonObjectTestObject implements InlineFieldJsonObject {
		@InlineField
		public String value;

		public InlineFieldJsonObjectTestObject(String value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof InlineFieldJsonObjectTestObject)) {
				return false;
			}
			InlineFieldJsonObjectTestObject other = (InlineFieldJsonObjectTestObject) obj;
			return Objects.equals(value, other.value);
		}

	}

	private static class InlineFieldJsonObjectTestObjectComplex implements InlineFieldJsonObject {
		@InlineField
		public DataTestObject value;

		public InlineFieldJsonObjectTestObjectComplex(DataTestObject value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof InlineFieldJsonObjectTestObjectComplex)) {
				return false;
			}
			InlineFieldJsonObjectTestObjectComplex other = (InlineFieldJsonObjectTestObjectComplex) obj;
			return Objects.equals(value, other.value);
		}

	}

	@Test
	public void testInlineFieldJsonObject() {
		InlineFieldJsonObjectTestObject testObject = new InlineFieldJsonObjectTestObject("Test");
		JsonElement json = GSON.toJsonTree(testObject);
		assertEquals(testObject.value, json.getAsString());

		// test deserialize
		InlineFieldJsonObjectTestObject testObjectReadBack = GSON.fromJson(json, InlineFieldJsonObjectTestObject.class);
		assertEquals(testObject, testObjectReadBack);

		// test with more complex data type
		DataTestObject innerData = new DataTestObject("Name", 1);
		InlineFieldJsonObjectTestObjectComplex testObject2 = new InlineFieldJsonObjectTestObjectComplex(innerData);
		JsonElement json2 = GSON.toJsonTree(testObject2);
		JsonElement json2Direct = GSON.toJsonTree(innerData);
		assertEquals(json2Direct, json2);

		// test deserialize
		InlineFieldJsonObjectTestObjectComplex testObject2ReadBack = GSON.fromJson(json2, InlineFieldJsonObjectTestObjectComplex.class);
		assertEquals(testObject2, testObject2ReadBack);
	}

	private static class InlineFieldJsonObjectTestObjectNoAnnotation implements InlineFieldJsonObject {
		@SuppressWarnings("unused")
		public String value;

		public InlineFieldJsonObjectTestObjectNoAnnotation(String value) {
			this.value = value;
		}

	}

	private static class InlineFieldJsonObjectTestObjectTooManyFields implements InlineFieldJsonObject {
		@InlineField
		public String value;
		@SuppressWarnings("unused")
		public String other;

		public InlineFieldJsonObjectTestObjectTooManyFields(String value, String other) {
			this.value = value;
			this.other = other;
		}

	}

	@Test
	public void testInlineFieldJsonObjectAbnormalConditions() {
		InlineFieldJsonObjectTestObjectNoAnnotation testObjectNoAnno = new InlineFieldJsonObjectTestObjectNoAnnotation("Test");
		assertThrows(JsonParseException.class, () -> GSON.toJsonTree(testObjectNoAnno));

		InlineFieldJsonObjectTestObjectTooManyFields testObjectTooManyFields = new InlineFieldJsonObjectTestObjectTooManyFields("Test2", "other");
		assertThrows(JsonParseException.class, () -> GSON.toJsonTree(testObjectTooManyFields));
	}

}
