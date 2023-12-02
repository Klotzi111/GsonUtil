package de.klotzi111.util.GsonUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class GsonSerializationHelper {

	public static final Charset JSON_SERIALIZATION_CHARSET = StandardCharsets.UTF_8;

	private GsonSerializationHelper() {
	}

	public static String convertJsonElementToString(JsonElement jsonElement) {
		if (jsonElement.isJsonPrimitive()) {
			return jsonElement.getAsString();
		} else {
			return jsonElement.toString();
		}
	}

	public static byte[] serializeToBytes(Gson gson, JsonElement json) throws IOException {
		try (ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream()) {
			try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteOutputStream, JSON_SERIALIZATION_CHARSET)) {
				gson.toJson(json, outputStreamWriter);
			}
			byteOutputStream.flush();
			return byteOutputStream.toByteArray();
		}
	}

	public static <T> T deserializeFromBytes(Gson gson, byte[] bytes, TypeToken<T> typeToken) throws IOException {
		try (ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes)) {
			try (InputStreamReader inputStreamReader = new InputStreamReader(byteInputStream, JSON_SERIALIZATION_CHARSET)) {
				JsonElement json = JsonParser.parseReader(inputStreamReader);

				T object = gson.fromJson(json, typeToken);
				return object;
			}
		}
	}

	public static <T> T deserializeFromBytes(Gson gson, byte[] bytes, Class<T> clazz) throws IOException {
		return deserializeFromBytes(gson, bytes, TypeToken.get(clazz));
	}

}
