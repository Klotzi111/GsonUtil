package de.klotzi111.util.GsonUtil.typeadapters;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Type;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import de.klotzi111.util.GsonUtil.typeadapter.AbstractEnhancedTypeAdapterFactory;
import de.klotzi111.util.GsonUtil.typeadapter.JsonDeSerializerBundle;

/**
 * This type adapter uses my 'ByteUtil' class.
 * But this class's source can easily be copied and adopted to other hex <-> byte[] libraries.
 */
public class ByteArrayAsHexTypeAdapterFactory extends AbstractEnhancedTypeAdapterFactory {

	private static final MethodHandle METHOD_hexToBytes;
	private static final MethodHandle METHOD_bytesToHex;

	static {
		MethodHandle METHOD_hexToBytes_local = null;
		MethodHandle METHOD_bytesToHex_local = null;
		try {
			Class<?> ByteUtil_class = Class.forName("de.klotzi111.util.BinaryUtil.ByteUtil");
			METHOD_hexToBytes_local = MethodHandles.publicLookup().findStatic(ByteUtil_class, "hexToBytes", MethodType.methodType(byte[].class, String.class));
			METHOD_bytesToHex_local = MethodHandles.publicLookup().findStatic(ByteUtil_class, "bytesToHex", MethodType.methodType(String.class, byte[].class, int.class));
		} catch (Exception e) {
			// ignore
		}
		METHOD_hexToBytes = METHOD_hexToBytes_local;
		METHOD_bytesToHex = METHOD_bytesToHex_local;
	}

	public static boolean canBeUsed() {
		return METHOD_hexToBytes != null && METHOD_bytesToHex != null;
	}

	// actual type adapter code start
	private static class ByteArrayAsHexAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

		@Override
		public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			String hexString = context.deserialize(json, String.class);

			// + call hexToBytes
			byte[] bytes;
			try {
				bytes = (byte[]) METHOD_hexToBytes.invokeExact(hexString);
			} catch (Throwable e) {
				throw new JsonParseException("Failed to call \"bytesToHex\"", e);
			}
			// - call hexToBytes

			if (bytes == null) {
				String hexStringDisplay = hexString.length() > 100 ? hexString.substring(0, 100) + "..." : hexString;
				throw new JsonParseException("Invalid hex string: " + hexStringDisplay);
			}
			return bytes;
		}

		@Override
		public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
			// + call bytesToHex
			String hexString;
			try {
				hexString = (String) METHOD_bytesToHex.invokeExact(src, 0);
			} catch (Throwable e) {
				throw new JsonParseException("Failed to call \"bytesToHex\"", e);
			}
			// - call bytesToHex

			return context.serialize(hexString);
		}
	}

	private <T> boolean isApplicableForType(TypeToken<T> type) {
		return byte[].class.isAssignableFrom(type.getRawType());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> JsonDeSerializerBundle<T> makeSerializers(Gson gson, TypeToken<T> type) {
		if (!isApplicableForType(type)) {
			return null;
		}
		ByteArrayAsHexAdapter fd = new ByteArrayAsHexAdapter();
		return (JsonDeSerializerBundle<T>) new JsonDeSerializerBundle<byte[]>(fd, fd, null);
	}
}
