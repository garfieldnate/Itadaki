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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;


/**
 * Interface for handlers dealing with character encoding-specific issues
 */
public interface CharacterHandler {

	/**
	 * Fold the case of a character for comparison purposes
	 *
	 * @param character The character to fold
	 * @return The folded character
	 */
	public int foldCharacterCase (int character);


	/**
	 * Read a character from the current position of a ByteBuffer, using as many bytes as needed
	 *
	 * @param buffer The ByteBuffer to read from
	 * @return The character
	 * @throws CharacterCodingException 
	 */
	public int readCharacter (ByteBuffer buffer) throws CharacterCodingException;


	/**
	 * Returns a CharsetEncoder suitable for the handled encoding
	 *
	 * @return The encoder
	 */
	public CharsetEncoder getCharsetEncoder();


	/**
	 * Returns a CharsetDecoder suitable for the handled encoding
	 *
	 * @return The decoder
	 */
	public CharsetDecoder getCharsetDecoder();

}
