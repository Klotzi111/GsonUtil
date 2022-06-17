package de.klotzi111.util.GsonUtil.typeadapters;

import static de.klotzi111.util.GsonUtil.GsonUtilTestHelper.GSON;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Objects;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Expose;

public class ExposeAnnotationExclusionStrategyTest {

	private static class ExposeAnnotationExclusionStrategyTestObject {
		public String always;
		@Expose
		public String always2;
		@Expose(serialize = false)
		public String noSerialize;
		@Expose(deserialize = false)
		public String noDeserialize;
		@Expose(serialize = false, deserialize = false)
		public String never;

		public ExposeAnnotationExclusionStrategyTestObject(String always, String always2, String noSerialize, String noDeserialize, String never) {
			this.always = always;
			this.always2 = always2;
			this.noSerialize = noSerialize;
			this.noDeserialize = noDeserialize;
			this.never = never;
		}

		@Override
		public int hashCode() {
			return Objects.hash(always, always2, never, noDeserialize, noSerialize);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof ExposeAnnotationExclusionStrategyTestObject)) {
				return false;
			}
			ExposeAnnotationExclusionStrategyTestObject other = (ExposeAnnotationExclusionStrategyTestObject) obj;
			return Objects.equals(always, other.always) && Objects.equals(always2, other.always2) && Objects.equals(never, other.never) && Objects.equals(noDeserialize, other.noDeserialize)
				&& Objects.equals(noSerialize, other.noSerialize);
		}

	}

	@Test
	public void testExposeAnnotationExclusionStrategy() {
		// test serialize
		ExposeAnnotationExclusionStrategyTestObject testObject = new ExposeAnnotationExclusionStrategyTestObject("1", "2", "3", "4", "5");
		JsonObject json = GSON.toJsonTree(testObject).getAsJsonObject();
		assertEquals(3, json.size());
		assertEquals("1", json.get("always").getAsString());
		assertEquals("2", json.get("always2").getAsString());
		assertEquals("4", json.get("noDeserialize").getAsString());

		// test deserialize
		// add back the not serialized values
		json.add("noSerialize", new JsonPrimitive("3"));
		json.add("never", new JsonPrimitive("4"));
		ExposeAnnotationExclusionStrategyTestObject testObjectReadBack = GSON.fromJson(json, ExposeAnnotationExclusionStrategyTestObject.class);
		ExposeAnnotationExclusionStrategyTestObject testObjectCompare = new ExposeAnnotationExclusionStrategyTestObject("1", "2", "3", null, null);
		assertEquals(testObjectCompare, testObjectReadBack);
	}

}
