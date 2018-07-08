/*
 * Copyright (C) 2006-2007
 * Matt Francis <asbel@neosheffield.co.uk>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.itadaki.seashell.edict;

import org.itadaki.seashell.CharacterHandler;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;


/**
 * Tests org.itadaki.seashell.edict.EdictIndexer
 */
public class EdictIndexerTest {

	/**
	 * Visually show diagram of indices for each tested entry (debug setting)
	 */
	private final static boolean debugShowEntryDiagram = false;

	/**
	 * List indices for the tested dictionary (debug setting)
	 */
	private final static boolean debugIndices = false;


	/**
	 * Asserts the equality in length and contents (but not order) of an int[] array and an IntBuffer from 0..limit()
	 *
	 * @param intArray The expected array
	 * @param intBuffer The actual IntBuffer
	 * @param sortBufferIndices The IntBuffer will be sorted numerically before comparison if true
	 */
	protected static void assertIntArrayEqualsIntBuffer (int[] intArray, IntBuffer intBuffer, boolean sortBufferIndices) {

		intBuffer.rewind();
		int intBufferArray[] = new int[intBuffer.limit()];
		intBuffer.get (intBufferArray, 0, intBuffer.limit());
		if (sortBufferIndices) {
			Arrays.sort (intBufferArray);
		}

		assertEquals (intBufferArray.length, intArray.length);

		for (int i = 0; i < intArray.length; i++) {
			assertEquals (intArray[i], intBufferArray[i]);
		}

	}


	/**
	 * Visually show diagram of indices for each tested entry (debug setting)
	 *
	 * @param testEdict The dictionary source data
	 * @param index The sorted index (although the sorting is not relevant here)
	 */
	protected static void debugShowIndices (ByteBuffer testEdict, IntBuffer index) {

		StringBuffer stars = new StringBuffer();
		for (int k = 0; k < testEdict.limit(); k++) {
			stars.append (' ');
		}
		for (int n = 0; n < index.limit(); n++) {
			stars.setCharAt (index.get(n), '*');
		}

		int lineStart = 0;

		for (int j = 0; j < testEdict.limit(); j++) {
			char c = (char)testEdict.get(j);
			if ((c == 10) || (c == 13)) {
				System.out.print ('@');
				if (c == 10) {
					System.out.println();
					System.out.println (stars.substring (lineStart, j));
					lineStart = j + 1;
				}
			} else if (c > 127) {
				System.out.print ('#');
			} else {
				System.out.print (c);
			}
		}

	}


	/**
	 * List indices for the tested dictionary (debug setting)
	 *
	 * @param index The index
	 * @param sort If true, sort the index numerically before displaying
	 */
	protected static void debugListIndices (IntBuffer index, boolean sort) {
		index.rewind();
		int indexCopy[] = new int[index.limit()];
		index.get (indexCopy, 0, index.limit());
		if (sort) {
			Arrays.sort (indexCopy);
		}

		for (int m = 0; m < indexCopy.length; m++) {
			System.out.print ("" + indexCopy[m] + " ");
		}
		System.out.println();
	}


	/**
	 * Display the indexed terms of a dictionary in index order
	 *
	 * @param testEdict The test dictionary
	 * @param index The sorted index
	 * @throws CharacterCodingException
	 */
	protected static void debugPrintDictionaryTerms (ByteBuffer testEdict, IntBuffer index) throws CharacterCodingException {

		for (int i = 0; i < index.limit(); i++) {
			int start = index.get (i);
			int end;
			testEdict.position (start);

			CharacterHandler handler = new EUCJPHandler();

			int character;
			do {
				end = testEdict.position();
				character = handler.readCharacter (testEdict);
			} while ((character != ']') && (character != ' ') && (character != '/'));

			testEdict.position (start);
			ByteBuffer termBuffer = testEdict.slice();
			termBuffer.limit (end - start);
			CharsetDecoder decoder = Charset.forName("EUC-JP").newDecoder();

			System.out.println ("" + start + " " + decoder.decode (termBuffer).toString());
		}

	}


	/**
	 * Test fixture for dictionary term decomposition
	 *
	 * @param testDictionaryString The test dictionary as a Java String
	 * @param expectedIndices The expected EUC-JP byte indices (NOTE: NOT String character indices) of the indexed terms
	 * @param sortDictionaryIndices Sort dictionary indices before comparison
	 * @throws CharacterCodingException
	 */
	protected static void indexerTestFixture  (String testDictionaryString, int[] expectedIndices, boolean sortDictionaryIndices) throws CharacterCodingException {

		CharsetEncoder encoder = Charset.forName("EUC-JP").newEncoder();
		ByteBuffer testEdict = encoder.encode (CharBuffer.wrap (testDictionaryString));

		EdictIndexer indexer = new EdictIndexer (testEdict, new EUCJPHandler(), false);
		IntBuffer indexData = indexer.getIndexData();

		if (EdictIndexerTest.debugShowEntryDiagram) {
			debugShowIndices (testEdict, indexData);
			debugListIndices (indexData, true);
			System.out.println();
		}

		if (EdictIndexerTest.debugIndices) {
			debugPrintDictionaryTerms (testEdict, indexData);
			System.out.println();
		}

		assertIntArrayEqualsIntBuffer (expectedIndices, indexData, sortDictionaryIndices);

	}


	/**
	 * Head, no reading, 1 meaning
	 *
	 * @throws Exception
	 */
	@Test
	public void testDecomposition1_0_1() throws Exception {

		String testEntry = "ちいさい /little/\r\n";
		int[] expectedIndices = { 0, 2, 4, 10 };

		indexerTestFixture (testEntry, expectedIndices, true);

	}


	/**
	 * Head, no reading, 2 meanings
	 *
	 * @throws Exception
	 */
	@Test
	public void testDecomposition1_0_2() throws Exception {

		String testEntry = "ちいさい /little/small/\r\n";
		int[] expectedIndices = { 0, 2, 4, 10, 17 };

		indexerTestFixture (testEntry, expectedIndices, true);

	}


	/**
	 * Head, no reading, 3 meanings
	 *
	 * @throws Exception
	 */
	@Test
	public void testDecomposition1_0_3() throws Exception {

		String testEntry = "ちいさい /little/small/mini/\r\n";
		int[] expectedIndices = { 0, 2, 4, 10, 17, 23 };

		indexerTestFixture (testEntry, expectedIndices, true);

	}

	/**
	 * Head, reading, 1 meaning
	 *
	 * @throws Exception
	 */
	@Test
	public void testDecomposition1_1_1() throws Exception {

		String testEntry = "小さい [ちいさい] /little/\r\n";
		int[] expectedIndices = { 0, 2, 4, 8, 10, 19 };

		indexerTestFixture (testEntry, expectedIndices, true);

	}


	/**
	 * Head, reading, 2 meanings
	 *
	 * @throws Exception
	 */
	@Test
	public void testDecomposition1_1_2() throws Exception {

		String testEntry = "小さい [ちいさい] /little/small/\r\n";
		int[] expectedIndices = { 0, 2, 4, 8, 10, 19, 26 };

		indexerTestFixture (testEntry, expectedIndices, true);

	}


	/**
	 * Head, reading, 3 meanings
	 *
	 * @throws Exception
	 */
	@Test
	public void testDecomposition1_1_3() throws Exception {

		String testEntry = "小さい [ちいさい] /little/small/mini/\r\n";
		int[] expectedIndices = { 0, 2, 4, 8, 10, 19, 26, 32 };

		indexerTestFixture (testEntry, expectedIndices, true);

	}


	/**
	 * Head, no reading, 1 meaning with attribute
	 *
	 * @throws Exception
	 */
	@Test
	public void testDecomposition1_0_1attribute() throws Exception {

		String testEntry = "ちいさい /(attr) little/\r\n";
		int[] expectedIndices = { 0, 2, 4, 17 };

		indexerTestFixture (testEntry, expectedIndices, true);

	}


	/**
	 * Head, no reading, 1 meaning with space
	 *
	 * @throws Exception
	 */
	@Test
	public void testDecomposition1_0_1multispace() throws Exception {

		String testEntry = "ちいさい /little tiny/\r\n";
		int[] expectedIndices = { 0, 2, 4, 10, 17 };

		indexerTestFixture (testEntry, expectedIndices, true);

	}


	/**
	 * Head, no reading, 1 meaning with dash
	 *
	 * @throws Exception
	 */
	@Test
	public void testDecomposition1_0_1multidash() throws Exception {

		String testEntry = "ちいさい /little-tiny/\r\n";
		int[] expectedIndices = { 0, 2, 4, 10, 17 };

		indexerTestFixture (testEntry, expectedIndices, true);

	}


	/**
	 * Head, no reading, 1 meaning with space and dash
	 *
	 * @throws Exception
	 */
	@Test
	public void testDecomposition1_0_1attributeSpaceDash() throws Exception {

		String testEntry = "ちいさい /(attr) little-tiny mini/\r\n";
		int[] expectedIndices = { 0, 2, 4, 17, 24, 29 };

		indexerTestFixture (testEntry, expectedIndices, true);

	}


	/**
	 * Alphabetic entries < 3 chars
	 *
	 * @throws Exception
	 */
	@Test
	public void testShortAlpha() throws Exception {

		String testEntry = "あ /a b c d e-f/\r\n";
		int[] expectedIndices = { 0 };

		indexerTestFixture (testEntry, expectedIndices, false);

	}


	/**
	 * Alphabetic case folding
	 *
	 * @throws Exception
	 */
	@Test
	public void testAlphaCaseFolding() throws Exception {

		String testEntry = "ちいさい /alpha BRAVO charlie DELTA/\r\n";
		int[] expectedIndices = { 10, 16, 22, 30, 2, 4, 0 };

		indexerTestFixture (testEntry, expectedIndices, false);

	}


	/**
	 * Kana case folding
	 *
	 * @throws Exception
	 */
	@Test
	public void testKanaCaseFolding() throws Exception {

		String testEntry = "あ /a/\r\nカ /b/\r\nさ /c/\r\nタ /d/\r\n";
		int[] expectedIndices = { 0, 8, 16, 24 };

		indexerTestFixture (testEntry, expectedIndices, false);

	}

}
