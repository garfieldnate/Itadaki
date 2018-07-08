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

package org.itadaki.seashell.tools;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.itadaki.seashell.DictionaryException;
import org.itadaki.seashell.edict.EdictDictionary;


/**
 * Tool to build indexes for EDICT format dictionaries
 */
public class Indexer {

	/**
	 * Main method
	 * 
	 * @param args &lt;dictionary file&gt;
	 * @throws DictionaryException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String args[]) throws DictionaryException, FileNotFoundException, IOException {

		EdictDictionary dictionary = new EdictDictionary(args[0]);
		dictionary.createIndex();

	}

}
