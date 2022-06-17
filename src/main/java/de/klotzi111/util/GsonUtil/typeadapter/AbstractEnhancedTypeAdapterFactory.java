package de.klotzi111.util.GsonUtil.typeadapter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.WeakHashMap;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public abstract class AbstractEnhancedTypeAdapterFactory implements TypeAdapterFactory {

	private static final class GsonContextImpl implements JsonSerializationContext, JsonDeserializationContext {

		private static final Map<Gson, GsonContextImpl> GSON_CONTEXT_IMPL_CACHE = new WeakHashMap<>();

		public static GsonContextImpl fromGson(Gson gson) {
			GsonContextImpl context = GSON_CONTEXT_IMPL_CACHE.get(gson);
			if (context == null) {
				context = new GsonContextImpl(gson);
				GSON_CONTEXT_IMPL_CACHE.put(gson, context);
			}
			return context;
		}

		private final Gson gson;

		private GsonContextImpl(Gson gson) {
			this.gson = gson;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <R> R deserialize(JsonElement json, Type typeOfT) throws JsonParseException {
			return (R) gson.fromJson(json, typeOfT);
		}

		@Override
		public JsonElement serialize(Object src) {
			return gson.toJsonTree(src);
		}

		@Override
		public JsonElement serialize(Object src, Type typeOfSrc) {
			return gson.toJsonTree(src, typeOfSrc);
		}
	}

	private static final class FullControlSerializationTypeAdapter<T> extends TypeAdapter<T> {

		private final GsonContextImpl context;
		private final TypeToken<T> typeToken;
		private final TypeAdapter<T> delegate;
		private final JsonDeSerializerBundle<T> serializerBundle;

		public FullControlSerializationTypeAdapter(Gson gson, TypeToken<T> typeToken, TypeAdapter<T> delegate, JsonDeSerializerBundle<T> serializerBundle) {
			this.context = GsonContextImpl.fromGson(gson);
			this.typeToken = typeToken;
			this.delegate = delegate;
			this.serializerBundle = serializerBundle;
		}

		@Override
		public T read(JsonReader in) throws IOException {
			if (serializerBundle.deserializer == null) {
				return delegate.read(in);
			}
			JsonElement value = null;
			try {
				value = Streams.parse(in);
			} catch (JsonParseException e) {
				if (serializerBundle.exceptionHandler != null) {
					serializerBundle.exceptionHandler.handle(e);
				} else {
					throw e;
				}
			}
			return serializerBundle.deserializer.deserialize(value, typeToken.getType(), context);
		}

		@Override
		public void write(JsonWriter out, T value) throws IOException {
			if (serializerBundle.serializer == null) {
				delegate.write(out, value);
				return;
			}
			JsonElement tree = serializerBundle.serializer.serialize(value, typeToken.getType(), context);
			if (tree != null) {
				Streams.write(tree, out);
			}
		}

	}

	@Override
	public final <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		JsonDeSerializerBundle<T> serializerBundle = makeSerializers(gson, type);
		if (serializerBundle == null) {
			return null;
		}
		TypeAdapter<T> delegate = null;
		if (serializerBundle.serializer == null || serializerBundle.deserializer == null) {
			// we only need the delegate when either serializer or deserializer is not set
			delegate = getDelegate(gson, type);
		}
		return new FullControlSerializationTypeAdapter<T>(gson, type, delegate, serializerBundle);
	}

	public final <T> TypeAdapter<T> getDelegate(Gson gson, TypeToken<T> type) {
		return gson.getDelegateAdapter(this, type);
	}

	/**
	 *
	 * @param <T>
	 * @param gson
	 * @param type
	 * @return the bundle with serializer and deserializer. Or null if the factory does not supply a type adapter for that type
	 */
	protected abstract <T> JsonDeSerializerBundle<T> makeSerializers(Gson gson, TypeToken<T> type);

}
