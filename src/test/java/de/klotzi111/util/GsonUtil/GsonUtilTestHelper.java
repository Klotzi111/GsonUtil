package de.klotzi111.util.GsonUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.klotzi111.util.GsonUtil.typeadapters.CheckExposeAnnotationExclusionStrategy;
import de.klotzi111.util.GsonUtil.typeadapters.InlineFieldJsonObjectTypeAdapterFactory;
import de.klotzi111.util.GsonUtil.typeadapters.MapJsonObjectTypeAdapterFactory;
import de.klotzi111.util.GsonUtil.typeadapters.SerializationUpdaterTypeAdapterFactory;

public class GsonUtilTestHelper {

	public static final Gson GSON = GsonUtil.getPreConfiguredGsonBuilder(null).create();
	public static final Gson GSON_NO_COMPLEX_MAP_KEY_SERIALIZATION;

	static {
		GSON_NO_COMPLEX_MAP_KEY_SERIALIZATION = createGson();
	}

	private static Gson createGson() {
		GsonBuilder gb = new GsonBuilder()
			.serializeNulls()
			.serializeSpecialFloatingPointValues()
			.addSerializationExclusionStrategy(new CheckExposeAnnotationExclusionStrategy(false))
			.addDeserializationExclusionStrategy(new CheckExposeAnnotationExclusionStrategy(true))
			.setPrettyPrinting();
		// must be first / as early as possible so it gets called first and then calls the delegate
		gb.registerTypeAdapterFactory(new SerializationUpdaterTypeAdapterFactory());
		gb.registerTypeAdapterFactory(new MapJsonObjectTypeAdapterFactory())
			.registerTypeAdapterFactory(new InlineFieldJsonObjectTypeAdapterFactory());
		return gb.create();
	}

	public static String replaceQuotations(String str) {
		return str.replace('\'', '"');
	}

	public static JsonObject getAsJsonObject(String str) {
		return JsonParser.parseString(replaceQuotations(str)).getAsJsonObject();
	}

}
