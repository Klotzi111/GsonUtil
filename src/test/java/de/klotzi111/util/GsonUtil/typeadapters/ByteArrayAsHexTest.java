package de.klotzi111.util.GsonUtil.typeadapters;

import static de.klotzi111.util.GsonUtil.GsonUtilTestHelper.GSON;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;

public class ByteArrayAsHexTest {

	private static final Random RANDOM = new Random(-340563807);

	private static byte[] generateByteArray(Random random, int count) {
		byte[] bytes = new byte[count];
		random.nextBytes(bytes);
		return bytes;
	}

	@Test
	public void testByteArrayAsHex() {
		Assumptions.assumeTrue(ByteArrayAsHexTypeAdapterFactory.canBeUsed(), "ByteArrayAsHexTypeAdapterFactory can not be used. ByteUtil is not present");

		byte[] bytes = generateByteArray(RANDOM, 50);
		JsonElement jsonElement = GSON.toJsonTree(bytes);
		String hexString = jsonElement.getAsString();
		assertEquals(bytes.length * 2, hexString.length());
		byte[] bytesReadBack = GSON.fromJson(hexString, byte[].class);
		assertArrayEquals(bytes, bytesReadBack);
	}

}
