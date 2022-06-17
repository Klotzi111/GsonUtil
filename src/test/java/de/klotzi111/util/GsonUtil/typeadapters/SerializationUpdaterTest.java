package de.klotzi111.util.GsonUtil.typeadapters;

import static de.klotzi111.util.GsonUtil.GsonUtilTestHelper.GSON;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import de.klotzi111.util.GsonUtil.interfaces.update.PostDeSerializationUpdater;
import de.klotzi111.util.GsonUtil.interfaces.update.PreSerializationUpdater;

public class SerializationUpdaterTest {

	private static class PostDeSerializationUpdaterTestObject implements PostDeSerializationUpdater {

		public String state = "init";
		public boolean throwException = false;

		@Override
		public void postSerializationUpdate(boolean deserialize) throws Exception {
			if (throwException) {
				throw new IllegalStateException("Test");
			}

			state = deserialize ? "deserialize" : "serialize";
		}

	}

	@Test
	public void testPostDeSerializationUpdater() {
		PostDeSerializationUpdaterTestObject testObject = new PostDeSerializationUpdaterTestObject();
		JsonElement json = GSON.toJsonTree(testObject);

		// json contains still "init" because it is changed afterwards
		assertTrue(json.getAsJsonObject().get("state").getAsString().equals("init"));

		assertTrue(testObject.state.equals("serialize"));

		PostDeSerializationUpdaterTestObject testObjectReadBack = GSON.fromJson(json, PostDeSerializationUpdaterTestObject.class);
		assertTrue(testObjectReadBack.state.equals("deserialize"));

		// now with throwing exception
		testObject.throwException = true;
		JsonParseException exception = assertThrowsExactly(JsonParseException.class, () -> GSON.toJsonTree(testObject));
		assertEquals("Test", exception.getCause().getMessage());
	}

	private static class PreSerializationUpdaterTestObject implements PreSerializationUpdater {

		public String state = "init";
		public boolean throwException = false;

		@Override
		public void preSerializationUpdate() throws Exception {
			if (throwException) {
				throw new IllegalStateException("Test");
			}

			state = "serialize";
		}

	}

	@Test
	public void testPreSerializationUpdater() {
		PreSerializationUpdaterTestObject testObject = new PreSerializationUpdaterTestObject();
		JsonElement json = GSON.toJsonTree(testObject);

		// json now contains "serialize" because it is changed before serialization
		assertTrue(json.getAsJsonObject().get("state").getAsString().equals("serialize"));

		assertTrue(testObject.state.equals("serialize"));

		PreSerializationUpdaterTestObject testObjectReadBack = GSON.fromJson(json, PreSerializationUpdaterTestObject.class);
		assertTrue(testObjectReadBack.state.equals("serialize"));

		// now with throwing exception
		testObject.throwException = true;
		JsonParseException exception = assertThrowsExactly(JsonParseException.class, () -> GSON.toJsonTree(testObject));
		assertEquals("Test", exception.getCause().getMessage());
	}

}
