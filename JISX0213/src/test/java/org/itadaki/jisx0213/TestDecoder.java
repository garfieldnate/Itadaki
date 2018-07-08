package org.itadaki.jisx0213;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

import static org.junit.Assert.assertEquals;


/**
 * Tests Decoder functionality
 */
public class TestDecoder {

	/**
	 * Test strings
	 */
	private static String[] commonTestStrings = new String[] {

			// ASCII
			"This is a test",

			// Kana
			"あいうえおアイウエオ",

			// Half width kana
			"ｱｲｳｴｵｶｷｸｹｺ",

			// Kanji
			"国際刑事警察機構",

			// JISX 0213
			"濰淼槢皝癋浥濇睎炷濇烤煠",

	};


	/**
	 * Test strings as EUC-JISX0213 data
	 */
	private static short[][] commonTestEUCArrays = new short[][] {

			// "This is a test",
			{ 0x54, 0x68, 0x69, 0x73, 0x20, 0x69, 0x73, 0x20, 0x61, 0x20, 0x74, 0x65, 0x73, 0x74 },

			// あいうえおアイウエオ
			{ 0xa4, 0xa2, 0xa4, 0xa4, 0xa4, 0xa6, 0xa4, 0xa8, 0xa4, 0xaa, 0xa5, 0xa2, 0xa5, 0xa4, 0xa5, 0xa6, 0xa5, 0xa8, 0xa5, 0xaa },

			// ｱｲｳｴｵｶｷｸｹｺ
			{ 0x8e, 0xb1, 0x8e, 0xb2, 0x8e, 0xb3, 0x8e, 0xb4, 0x8e, 0xb5, 0x8e, 0xb6, 0x8e, 0xb7, 0x8e, 0xb8, 0x8e, 0xb9, 0x8e, 0xba },

			// 国際刑事警察機構
			{ 0xb9, 0xf1, 0xba, 0xdd, 0xb7, 0xba, 0xbb, 0xf6, 0xb7, 0xd9, 0xbb, 0xa1, 0xb5, 0xa1, 0xb9, 0xbd },

			// 濰淼槢皝癋浥濇睎炷濇烤煠
			{ 0xf7, 0xba, 0xf6, 0xf6, 0xf6, 0xa2, 0xf8, 0xe2, 0xf8, 0xda, 0xf6, 0xe8, 0xf7, 0xb7, 0xf8, 0xf4, 0xf7, 0xc8, 0xf7, 0xb7,
				0xf7, 0xcb, 0xf7, 0xd8 },

	};


	/**
	 * Convert an array of byte values in shorts to an array of bytes
	 * Used to ease specifying byte arrays in hex
	 *
	 * @param shortArray An array of byte sized values in shorts
	 * @return The input array as an array of bytes
	 */
	private byte[] shortToByteArray (short[] shortArray) {

		byte[] byteArray = new byte[shortArray.length];

		for (int i = 0; i < shortArray.length; i++) {

			byteArray[i] = (byte)(shortArray[i] & 0xff);

		}

		return byteArray;
	}


	/**
	 * Tests basic decoding
	 *
	 * @throws IOException
	 */
	@Test
	public void testBasic() throws IOException {

		short[] rawTestData = new short[] {  0xa4, 0xb3, 0xa4, 0xec, 0xa4, 0xcf, 0xa5, 0xc6, 0xa5, 0xb9, 0xa5, 0xc8, 0xa4, 0xc0 };
		byte[] testData = shortToByteArray (rawTestData);

		String expectedString = "これはテストだ";

		ByteBuffer buffer = ByteBuffer.wrap(testData);

		CharsetDecoder decoder = new EUCJISX0213CharsetDecoder (new EUCJISX0213Charset());
		CharBuffer output = decoder.decode (buffer);

		assertEquals (expectedString, output.toString());

	}


	/**
	 * Test decoding of assorted strings
	 *
	 * @throws Exception
	 */
	@Test
	public void testCommon() throws Exception {

		for (int i = 0; i < commonTestStrings.length; i++) {

			byte[] testData = shortToByteArray (commonTestEUCArrays[i]);

			ByteBuffer buffer = ByteBuffer.wrap(testData);

			CharsetDecoder decoder = new EUCJISX0213CharsetDecoder (new EUCJISX0213Charset());
			CharBuffer output = decoder.decode (buffer);

			assertEquals (commonTestStrings[i], output.toString());

		}

	}


}
