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

	public static final HexBinConverter DEFAULT_HEX_BIN_CONVERTER;

	public static interface HexBinConverter {
		boolean isAvailable();

		String bytesToHex(byte[] bytes);

		byte[] hexToBytes(String hexString);
	}

	private static class BinaryUtilHexBinConverter implements HexBinConverter {
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

		@Override
		public boolean isAvailable() {
			return METHOD_hexToBytes != null && METHOD_bytesToHex != null;
		}

		@Override
		public String bytesToHex(byte[] bytes) {
			try {
				return (String) METHOD_bytesToHex.invokeExact(bytes, 0);
			} catch (Throwable e) {
				throw new JsonParseException("Failed to call \"ByteUtil::bytesToHex\"", e);
			}
		}

		@Override
		public byte[] hexToBytes(String hexString) {
			try {
				return (byte[]) METHOD_hexToBytes.invokeExact(hexString);
			} catch (Throwable e) {
				throw new JsonParseException("Failed to call \"ByteUtil::bytesToHex\"", e);
			}
		}
	}

	private static class CommonsCodecHexBinConverter implements HexBinConverter {
		private static final MethodHandle METHOD_decodeHex;
		private static final MethodHandle METHOD_encodeHexString;

		static {
			MethodHandle METHOD_decodeHex_local = null;
			MethodHandle METHOD_encodeHexString_local = null;
			try {
				Class<?> Hex_class = Class.forName("org.apache.commons.codec.binary.Hex");
				METHOD_decodeHex_local = MethodHandles.publicLookup().findStatic(Hex_class, "decodeHex", MethodType.methodType(byte[].class, String.class));
				METHOD_encodeHexString_local = MethodHandles.publicLookup().findStatic(Hex_class, "encodeHexString", MethodType.methodType(String.class, byte[].class, boolean.class));
			} catch (Exception e) {
				// ignore
			}
			METHOD_decodeHex = METHOD_decodeHex_local;
			METHOD_encodeHexString = METHOD_encodeHexString_local;
		}

		@Override
		public boolean isAvailable() {
			return METHOD_decodeHex != null && METHOD_encodeHexString != null;
		}

		@Override
		public String bytesToHex(byte[] bytes) {
			try {
				return (String) METHOD_encodeHexString.invokeExact(bytes, false);
			} catch (Throwable e) {
				throw new JsonParseException("Failed to call \"Hex::encodeHexString\"", e);
			}
		}

		@Override
		public byte[] hexToBytes(String hexString) {
			try {
				return (byte[]) METHOD_decodeHex.invokeExact(hexString);
			} catch (Throwable e) {
				throw new JsonParseException("Failed to call \"Hex::decodeHex\"", e);
			}
		}
	}

	private static HexBinConverter findHexBinConverter() {
		HexBinConverter converter = null;
		// try BinaryUtil first
		try {
			converter = new BinaryUtilHexBinConverter();
			if (converter.isAvailable()) {
				return converter;
			}
		} catch (Exception e) {
			// ignore
		}
		// then commons-codec
		try {
			converter = new CommonsCodecHexBinConverter();
			if (converter.isAvailable()) {
				return converter;
			}
		} catch (Exception e) {
			// ignore
		}
		return null;
	}

	static {
		DEFAULT_HEX_BIN_CONVERTER = findHexBinConverter();
	}

	public static boolean defaultIsAvailable() {
		return DEFAULT_HEX_BIN_CONVERTER != null;
	}

	private final HexBinConverter hexBinConverter;

	public ByteArrayAsHexTypeAdapterFactory(HexBinConverter hexBinConverter) {
		this.hexBinConverter = hexBinConverter;
	}

	public ByteArrayAsHexTypeAdapterFactory() {
		this(DEFAULT_HEX_BIN_CONVERTER);
	}

	// actual type adapter code start
	private class ByteArrayAsHexAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

		@Override
		public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			String hexString = context.deserialize(json, String.class);
			byte[] bytes = hexBinConverter.hexToBytes(hexString);
			if (bytes == null) {
				String hexStringDisplay = hexString.length() > 100 ? hexString.substring(0, 100) + "..." : hexString;
				throw new JsonParseException("Invalid hex string: " + hexStringDisplay);
			}
			return bytes;
		}

		@Override
		public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
			String hexString = hexBinConverter.bytesToHex(src);
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
