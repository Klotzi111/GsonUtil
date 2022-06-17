package de.klotzi111.util.GsonUtil.interfaces.update;

/**
 * This interface's method is called after an object implementing this interface is serialized/deserialized to/from json.
 */
@FunctionalInterface
public interface PostDeSerializationUpdater extends SerializationUpdater {

	/**
	 * This method can throw an exception to indicate that the object is invalid an should fail serialization/deserialization
	 *
	 * @param deserialize
	 *            {@code true} when this object was just deserialized. {@code false} if it was just serialized
	 * @throws Exception
	 */
	public void postSerializationUpdate(boolean deserialize) throws Exception;
}
