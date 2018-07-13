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

package org.itadaki.client.dictionary.ui.dictionary;

import org.itadaki.seashell.SearchMode;


/**
 * Listener interface for classes interested in ResultMarshaller state
 */
public interface ResultMarshallerListener {

	/**
	 * Indicates that a new search Document has begun
	 */
	public void resultsStarted();


	/**
	 * Indicates that no further results will be added to the active search Document
	 */
	public void resultsEnded();

	
	/**
	 * Indicates that results have been cleared
	 */
	public void resultsCleared();


	/**
	 * Indicates that the active search Document has created a new result section
	 *
	 * @param resultSection The section that has been created
	 * @param documentIndex The Document index of the new section
	 */
	public void resultSectionCreated (SearchMode resultSection, int documentIndex);


	/**
	 * Indicates that the active search has yielded new results
	 *
	 * @param resultCount The new count of search results
	 */
	public void resultCountUpdate (int resultCount);


}
