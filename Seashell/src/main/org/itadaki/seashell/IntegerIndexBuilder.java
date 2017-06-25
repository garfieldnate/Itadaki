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

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.IntBuffer;
import java.util.Comparator;
import java.util.Random;


/**
 * Index builder for dictionaries term-indexed by Integer
 */
public class IntegerIndexBuilder {

	/**
	 * Initial size of index buffer (in ints)
	 */
	private static int BUFFER_SIZE = 1000000;

	/**
	 * Pseudorandom source for the quicksort
	 */
	private Random random = new Random();

	/**
	 * The index buffer
	 */
	private IntBuffer intIndex;

	/**
	 * The Comparator to use in sorting the index
	 */
	private Comparator<Integer> comparator;


	/**
	 * Add unsorted entries to the index buffer
	 *
	 * @param entries
	 */
	public void add (int... entries) {

		try {
			this.intIndex.put (entries);
		} catch (BufferOverflowException e) {
			IntBuffer newIndex;
			// TODO catch failed allocation
			newIndex = IntBuffer.allocate ((int) (this.intIndex.capacity() * 1.5));
			int position = this.intIndex.position();
			this.intIndex.rewind();
			newIndex.put (this.intIndex);
			newIndex.position (position);
			newIndex.put (entries);
			this.intIndex = newIndex;
		}

	}


	/**
	 * Sort and return the completed index
	 *
	 * @return The sorted index
	 */
	public IntBuffer getSortedIndex() {

		int intIndexSize = this.intIndex.position();

		this.intIndex.limit (intIndexSize);		

		try {
			sortIndex (this.intIndex.array(), 0, intIndexSize - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.intIndex.rewind();

		return this.intIndex;

	}


	/**
	 * Sort the index using a recursive random-split quicksort
	 *
	 * @param intArray The index
	 * @param start The start of the region to sort
	 * @param end The end of the region to sort
	 * @throws IOException
	 */
	private void sortIndex (int intArray[], int start, int end) throws IOException {

		if ((end - start) >= 1) {

			int pivot = start + this.random.nextInt (end - start + 1);
			int pivotValue = intArray[pivot];
			intArray[pivot] = intArray[start];

			int k = start + 1;
			for (int i = k; i <= end; i++) {
				if (this.comparator.compare (pivotValue, intArray[i]) > 0) {
					if (i > k) {
						int temp1 = intArray[i];
						intArray[i] = intArray[k];
						intArray[k] = temp1;
					}
					k++;
				}
			}
			k--;

			intArray[start] = intArray[k];
			intArray[k] = pivotValue;

			sortIndex (intArray, start, k - 1);
			sortIndex (intArray, k + 1, end);

		}

	}


	/**
	 * @param comparator The Comparator to use in sorting the index
	 */
	public IntegerIndexBuilder (Comparator<Integer> comparator) {

		this.comparator = comparator;

		this.intIndex = IntBuffer.allocate (IntegerIndexBuilder.BUFFER_SIZE);

	}

}
