package de.klotzi111.util.GsonUtil.typeadapters;

import java.lang.reflect.Type;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import de.klotzi111.util.GsonUtil.interfaces.update.PostDeSerializationUpdater;
import de.klotzi111.util.GsonUtil.interfaces.update.PreSerializationUpdater;
import de.klotzi111.util.GsonUtil.interfaces.update.SerializationUpdater;
import de.klotzi111.util.GsonUtil.typeadapter.AbstractEnhancedTypeAdapterFactory;
import de.klotzi111.util.GsonUtil.typeadapter.JsonDeSerializerBundle;

public class SerializationUpdaterTypeAdapterFactory extends AbstractEnhancedTypeAdapterFactory {

	private static class SerializationUpdaterAdapter implements JsonSerializer<SerializationUpdater>, JsonDeserializer<SerializationUpdater> {

		private final TypeAdapter<SerializationUpdater> delegate;

		public SerializationUpdaterAdapter(TypeAdapter<SerializationUpdater> delegate) {
			this.delegate = delegate;
		}

		@Override
		public SerializationUpdater deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			SerializationUpdater updater = delegate.fromJsonTree(json);
			try {
				if (updater instanceof PostDeSerializationUpdater) {
					((PostDeSerializationUpdater) updater).postSerializationUpdate(true);
				}
			} catch (JsonParseException e) {
				throw e;
			} catch (Exception e) {
				throw new JsonParseException("Error while performing post serialization update", e);
			}
			return updater;
		}

		@Override
		public JsonElement serialize(SerializationUpdater src, Type typeOfSrc, JsonSerializationContext context) {
			try {
				if (src instanceof PreSerializationUpdater) {
					((PreSerializationUpdater) src).preSerializationUpdate();
				}
			} catch (JsonParseException e) {
				throw e;
			} catch (Exception e) {
				throw new JsonParseException("Error while performing pre serialization update", e);
			}

			JsonElement json = delegate.toJsonTree(src);

			try {
				if (src instanceof PostDeSerializationUpdater) {
					((PostDeSerializationUpdater) src).postSerializationUpdate(false);
				}
			} catch (JsonParseException e) {
				throw e;
			} catch (Exception e) {
				throw new JsonParseException("Error while performing post serialization update", e);
			}
			return json;
		}
	}

	private <T> boolean isApplicableForType(TypeToken<T> type) {
		Class<?> rawType = type.getRawType();
		return SerializationUpdater.class.isAssignableFrom(rawType)
			&& (PreSerializationUpdater.class.isAssignableFrom(rawType) || PostDeSerializationUpdater.class.isAssignableFrom(rawType));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> JsonDeSerializerBundle<T> makeSerializers(Gson gson, TypeToken<T> type) {
		if (!isApplicableForType(type)) {
			return null;
		}
		TypeAdapter<T> delegate = getDelegate(gson, type);
		SerializationUpdaterAdapter fd = new SerializationUpdaterAdapter((TypeAdapter<SerializationUpdater>) delegate);
		return (JsonDeSerializerBundle<T>) new JsonDeSerializerBundle<SerializationUpdater>(fd, fd, null);
	}

}
