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

import org.itadaki.seashell.edict.EdictDictionary;


/**
 * Listener interface allowing interested classes to asynchronously receive information
 * about dictionary availability 
 */
public interface DictionaryStatusListener {

	/**
	 * Indicates that dictionary indexing has started
	 */
	public void dictionariesUnavailable();


	/**
	 * Indicates that a particular dictionary is currently being indexed
	 *
	 * @param dictionary The dictionary being indexed
	 * @param remainingDictionaries The number of remaining dictionaries requiring indexing 
	 */
	public void dictionariesIndexing (EdictDictionary dictionary, int remainingDictionaries);


	/**
	 * Indicates that dictionary indexing has completed
	 */
	public void dictionariesReady();

}
