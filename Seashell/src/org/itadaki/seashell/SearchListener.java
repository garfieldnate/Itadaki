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

import java.util.List;

import org.itadaki.seashell.edict.EdictDictionary;


/**
 * Listener interface to receive the results of a running org.takadb.itadaki.dictionary.AsynchronousSearcher
 */
public interface SearchListener {

	/**
	 * Called when a new search is started
	 * 
	 * @param searchQuery The search query 
	 */
	public void searchStarted (final String searchQuery);


	/**
	 * Called to present a block of search results
	 * 
	 * @param mode The mode of the search results
	 * @param dictionary The dictionary from which the results arose
	 * @param resultIndices The indices of the search results in the given dictionary
	 *
	 */
	public void searchResults (final SearchMode mode, final EdictDictionary dictionary, List<Integer> resultIndices);


	/**
	 * Called when a search ends normally
	 */
	public void searchFinished();

	/**
	 * Called when a search is terminated before ending normally.
	 * Note that if a search is not running when an abort is requested, this can
	 * be called without searchStarted() having occurred
	 */
	public void searchAborted();

}
