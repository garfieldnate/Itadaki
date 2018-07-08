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

package org.itadaki.seashell;
import java.nio.IntBuffer;
import java.util.Comparator;

import org.junit.Test;
import org.itadaki.seashell.IntegerIndexBuilder;

import static org.junit.Assert.assertEquals;

/**
 * Tests org.itadaki.seashell.edict.IntegerIndexBuilder
 */
public class IntegerIndexBuilderTest {

	/**
	 * Asserts the equality in length, contents and order of two int[] arrays
	 *
	 * @param expected The expected array
	 * @param actual The actual array
	 */
	private void assertEqualsIntArray (int expected[], int actual[]) {

		assertEquals (expected.length, actual.length);

		for (int i = 0; i < expected.length; i++) {
			assertEquals (expected[i], actual[i]);
		}

	}


	/**
	 * Converts indices 0..limit() of an IntBuffer to an int[] array
	 *
	 * @param buffer The buffer to convert
	 * @return The buffer's contents as an int[] array
	 */
	private int[] exactArrayFromIntBuffer (IntBuffer buffer) {

		buffer.rewind();
		int limit = buffer.limit();
		int exactArray[] = new int[limit];
		System.arraycopy (buffer.array(), 0, exactArray, 0, limit);
		return exactArray;

	}


	/**
	 * Creates a Comparator for String dictionaries referenced by their Integer indices
	 *
	 * @param dictionary The String dictionary
	 * @return The Comparator
	 */
	private Comparator<Integer> createIntegerComparator (final String[] dictionary) {

		return new Comparator<Integer>() {

			@Override
			public int compare(Integer term1Address, Integer term2Address) {
				return dictionary[term1Address].compareTo (dictionary[term2Address]);
			}

		};

	}


	/**
	 * Test fixture for testing standard Integer-indexed String dictionaries
	 *
	 * @param dictionary The String dictionary
	 * @param intExpectedIndex The expected sorted index
	 * @param intIndex The actual sorted index
	 */
	private void indexBuilderTestFixture (final String[] dictionary, int[] intExpectedIndex, int[] intIndex) {

		Comparator<Integer> comparator = createIntegerComparator (dictionary);

		IntegerIndexBuilder builder = new IntegerIndexBuilder(comparator);
		builder.add (intIndex);
		int[] intSortedIndex = exactArrayFromIntBuffer (builder.getSortedIndex());

		assertEqualsIntArray (intExpectedIndex, intSortedIndex);

	}


	/**
	 * Test sorting a single entry
	 */
	@Test
	public void testOne() {

		final String dictionary[] = { "foo" };
		int intIndex[] = { 0 };
		int intExpectedIndex[] = { 0 };

		indexBuilderTestFixture(dictionary, intExpectedIndex, intIndex);

	}


	/**
	 * Test sorting two entries
	 */
	@Test
	public void testTwo() {

		final String dictionary[] = { "foo", "bar" };
		int intIndex[] = { 0, 1 };
		int intExpectedIndex[] = { 1, 0 };

		indexBuilderTestFixture(dictionary, intExpectedIndex, intIndex);

	}


	/**
	 * Test sorting five entries
	 */
	@Test
	public void testFive() {

		final String dictionary[] = { "mary", "had", "a", "little", "lamb" };
		int intIndex[] = { 0, 1, 2, 3, 4 };
		int intExpectedIndex[] = { 2, 1, 4, 3, 0 };

		indexBuilderTestFixture(dictionary, intExpectedIndex, intIndex);

	}


	/**
	 * Test sorting five previous-sorted entries
	 */
	@Test
	public void testAlreadySorted() {

		final String dictionary[] = { "mary", "had", "a", "little", "lamb" };
		int intIndex[] = { 2, 1, 4, 3, 0 };
		int intExpectedIndex[] = { 2, 1, 4, 3, 0 };

		indexBuilderTestFixture(dictionary, intExpectedIndex, intIndex);

	}


}
