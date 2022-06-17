package de.klotzi111.util.GsonUtil.typeadapter;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

/**
 * All values in this class can be null. If they are null they are not called (obviously) and the default action is done.
 * <br>
 * For {@code deserializer} and {@code serializer} this means that the delegate type adapter factory is called.
 *
 * @param <T>
 */
public class JsonDeSerializerBundle<T> {

	public final JsonDeserializer<T> deserializer;
	public final JsonSerializer<T> serializer;
	public final JsonParseExceptionHandler exceptionHandler;

	public JsonDeSerializerBundle(JsonDeserializer<T> deserializer, JsonSerializer<T> serializer, JsonParseExceptionHandler exceptionHandler) {
		this.deserializer = deserializer;
		this.serializer = serializer;
		this.exceptionHandler = exceptionHandler;
	}

}
