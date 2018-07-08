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


/**
 * Exception indicating a problem instantiating a dictionary
 */
public class DictionaryException extends Exception {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;

	
	/**
	 * Constructor wrapping another exception
	 * 
	 * @param e
	 */
	public DictionaryException (Exception e) {

		super (e);

	}


	/**
	 * Default constructor 
	 */
	public DictionaryException () {

		super();

	}
	
}
