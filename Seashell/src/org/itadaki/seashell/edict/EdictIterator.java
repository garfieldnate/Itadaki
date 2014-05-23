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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.itadaki.seashell.SearchMode;


/**
 * Iterator over EDICT entries
 */
public class EdictIterator implements Iterator<Integer> {

	/**
	 * The dictionary to iterate over
	 */
	private EdictDictionary dictionary;

	/**
	 * Search query 
	 */
	private ByteBuffer encodedQuery;

	/**
	 * Current iterator position
	 */
	private int position;

	/**
	 * Last matching index of result set
	 */
	private int lastMatch;

	/**
	 * Previously returned entries 
	 */
	private Set<Integer> returnedEntries;

	/**
	 * Pre-calculated next result
	 */
	private Integer nextResult;

	/**
	 * Dictionary search mode
	 */
	private SearchMode searchMode;


	/**
	 * Find next result in an entry that has not yet been returned
	 *
	 * @return The next result index
	 */
	private Integer findNextResult() {

		for (; this.position <= this.lastMatch; this.position++) {
			int termPosition = this.dictionary.getIndexEntry (this.position);

			boolean match = false;
			switch (this.searchMode) {
				case ANY:
					match = true;
					break;
				case EXACT:
					if (this.dictionary.isWordStart (termPosition) && this.dictionary.isWordEnd (termPosition + this.encodedQuery.limit() - 1)) {
						match = true;
					}
					break;
				case START:
					if (this.dictionary.isWordStart (termPosition) && !this.dictionary.isWordEnd (termPosition + this.encodedQuery.limit() - 1)) {
						match = true;
					}
					break;
				case END:
					if (!this.dictionary.isWordStart (termPosition) && this.dictionary.isWordEnd (termPosition + this.encodedQuery.limit() - 1)) {
						match = true;
					}
					break;
				case MIDDLE:
					if (!this.dictionary.isWordStart (termPosition) && !this.dictionary.isWordEnd (termPosition + this.encodedQuery.limit() - 1)) {
						match = true;
					}
					break;
			}
			if (match) {
				int entryPosition = this.dictionary.findStartOfEntry (termPosition);
				if (!this.returnedEntries.contains (entryPosition)) {
					this.returnedEntries.add (entryPosition);
					this.position++;
					return entryPosition;
				}
			}
		}

		return null;

	}


	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {

		return this.nextResult != null;

	}


	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Integer next() {

		Integer result = this.nextResult;

		this.nextResult = findNextResult();

		return result;

	}


	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {

		throw new UnsupportedOperationException();

	}


	/**
	 * @param dictionary The dictionary to iterate over
	 * @param encodedQuery The search query as a buffer in the same encoding as the dictionary
	 * @param firstMatch The first match
	 * @param lastMatch The last match
	 * @param searchMode The dictionary search mode
	 */
	public EdictIterator (EdictDictionary dictionary, ByteBuffer encodedQuery, Integer firstMatch, Integer lastMatch, SearchMode searchMode) {

		this.dictionary = dictionary;
		this.encodedQuery = encodedQuery;
		this.searchMode = searchMode;

		if (firstMatch != null) {
			this.position = firstMatch;
			this.lastMatch = lastMatch;
			this.returnedEntries = new HashSet<Integer>();
			this.nextResult = findNextResult();
		}

	}


}
