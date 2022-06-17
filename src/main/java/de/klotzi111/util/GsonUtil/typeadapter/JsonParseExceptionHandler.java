package de.klotzi111.util.GsonUtil.typeadapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

@FunctionalInterface
public interface JsonParseExceptionHandler {

	/**
	 * This method is called when a {@link JsonParseException} occurs during parsing the {@link JsonReader} to a {@link JsonElement} when deserializing.
	 * If a Exception is thrown from this method the deserializer will not be called and the exception is given up the call stack.
	 * So you can swallow the exception here and continue deserialization with null as {@link JsonElement}.
	 *
	 * @param exception
	 * @throws JsonParseException
	 */
	public void handle(JsonParseException exception) throws JsonParseException;

}
