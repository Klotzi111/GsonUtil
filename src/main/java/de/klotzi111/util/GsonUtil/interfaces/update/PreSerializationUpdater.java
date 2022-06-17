package de.klotzi111.util.GsonUtil.interfaces.update;

/**
 * This interface's method is called before an object implementing this interface is serialized to json.
 */
@FunctionalInterface
public interface PreSerializationUpdater extends SerializationUpdater {

	/**
	 * This method can throw an exception to indicate that the object is invalid an should fail serialization
	 *
	 * @throws Exception
	 */
	public void preSerializationUpdate() throws Exception;
}
