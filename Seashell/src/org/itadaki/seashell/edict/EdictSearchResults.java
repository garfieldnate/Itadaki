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
import java.util.Iterator;

import org.itadaki.seashell.SearchMode;


/**
 * Encapsulates a set of search result matches
 */
public class EdictSearchResults {

	/**
	 * The dictionary that search results refer to
	 */
	private EdictDictionary dictionary;

	/**
	 * Query leading to this result set
	 */
	private ByteBuffer encodedQuery;

	/**
	 * First matching index of result set 
	 */
	private Integer firstMatch;

	/**
	 * Last matching index of result set
	 */
	private Integer lastMatch;

	
	/**
	 * Retrieve first matching index
	 *
	 * @return The first matching index
	 */
	public Integer getFirstMatch() {
		return this.firstMatch;
	}

	
	/**
	 * Retrieve last matching index
	 *
	 * @return The last matching index
	 */
	public Integer getLastMatch() {
		return this.lastMatch;
	}


	/**
	 * Create Iterator over matching entry indices
	 * @param searchMode Dictionary search mode
	 *
	 * @return An iterator with the specified SearchMode
	 */
	public Iterator<Integer> iterator (SearchMode searchMode) {

		return new EdictIterator (this.dictionary, this.encodedQuery, this.firstMatch, this.lastMatch, searchMode);

	}

	/**
	 * @param dictionary The dictionary the results came from
	 * @param encodedQuery The search query as a buffer in the same encoding as the dictionary
	 * @param firstMatch The first potential match
	 * @param lastMatch The last potential match
	 */
	public EdictSearchResults (EdictDictionary dictionary, ByteBuffer encodedQuery, Integer firstMatch, Integer lastMatch) {

		this.dictionary = dictionary;
		this.encodedQuery = encodedQuery;
		this.firstMatch = firstMatch;
		this.lastMatch = lastMatch;

	}

	
}
