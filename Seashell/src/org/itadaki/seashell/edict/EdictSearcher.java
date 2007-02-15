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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;


/**
 * EDICT binary searcher
 */
public class EdictSearcher {

	/**
	 * The dictionary to search
	 */
	private EdictDictionary dictionary;


	/**
	 * Find potential matches for a query
	 *
	 * @param query The query string
	 * @return Search results object
	 */
	public EdictSearchResults search (String query) {

		CharsetEncoder encoder = this.dictionary.getCharacterHandler().getCharsetEncoder();
		ByteBuffer encodedQuery = null;
		try {
			encodedQuery = encoder.encode (CharBuffer.wrap (query));
		} catch (CharacterCodingException e) {
			// If we can't encode it we can't search for it here
			// TODO some sort of exception
			return null;
		}

		try {

			EdictComparator comparator = this.dictionary.comparator();

			int start = 0;
			int end = this.dictionary.getIndexSize() - 1;
			int match = -1;

			do {
				int current = start + ((end - start) / 2);

				int character = comparator.compareLeft (encodedQuery, this.dictionary.getIndexEntry (current));
				if (character > 0) {
					start = current + 1;
				} else if (character < 0) {
					end = current - 1;
				} else {
					match = current;
				}
			} while ((start <= end) && (match == -1));

			if (match != -1) {
				end = this.dictionary.getIndexSize() - 1;
				int min = match;
				int max = match;
				while ((min > 0) && (comparator.compareLeft (encodedQuery, this.dictionary.getIndexEntry (min - 1)) == 0)) {
					min--;
				}
				while ((max < end) && (comparator.compareLeft (encodedQuery, this.dictionary.getIndexEntry (max + 1)) == 0)) {
					max++;
				}

				return new EdictSearchResults (this.dictionary, encodedQuery, min, max);
			}

		} catch (CharacterCodingException e) {
			// Shouldn't happen. If any entries of the dictionary were broken, the term index should omit all terms from that entry
			e.printStackTrace();
		}

		return new EdictSearchResults (this.dictionary, encodedQuery, null, null);

		
	}


	/**
	 * @param dictionary The dictionary to search 
	 */
	public EdictSearcher (EdictDictionary dictionary) {

		this.dictionary = dictionary;

	}

}
