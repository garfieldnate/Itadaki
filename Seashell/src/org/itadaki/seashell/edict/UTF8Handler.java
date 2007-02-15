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
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import org.itadaki.seashell.CharacterHandler;


/**
 * UTF-8 character handler
 */
public class UTF8Handler implements CharacterHandler {

	/* (non-Javadoc)
	 * @see org.takadb.itadaki.dictionary.CharacterHandler#foldCharacterCase(int)
	 */
	public int foldCharacterCase (int character) {

		if ((character >= 'A') && (character <= 'Z')) {
			// Alphabetic upper case to lower case
			character |= 0x20;
		} else if ((character & 0xff00) == 0xa500) {
			// Katakana -> hiragana
			character -= 96;
		}
	
		return character;
	
	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.dictionary.CharacterHandler#readCharacter(java.nio.ByteBuffer)
	 */
	public int readCharacter (ByteBuffer buffer) throws CharacterCodingException {

		int character = buffer.get() & 0xff;

		// 7-bit plain boring ASCII
		if ((character & 0x80) == 0x00) {        // 7 bits {7}         0xxxxxxx
			return character;
		}

		int length;

		// Find the length of the multi-byte sequence
		if ((character & 0xe0) == 0xc0) {        // 11 bits {5-6}      110xxxxx 10xxxxxx
			character = character & 0x1f;        //                    00011111
			length = 2;
		} else if ((character & 0xf0) == 0xe0) { // 17 bits {5-6-6}    1110xxxx 10xxxxxx 10xxxxxx
			character = character & 0x0f;        //                    00001111
			length = 3;
		} else if ((character & 0xf8) == 0xf0) { // 23 bits {5-6-6-6}  11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
			character = character & 0x07;        //                    00000111
			length = 4;
		} else {
			// 5+ byte sequences forbidden by RFC 3629
			throw new CharacterCodingException();
		}

		// Read the multi-byte sequence
		for (int i = 2; i <= length; i++) {

			int subCharacter = buffer.get();

			// If we don't have 10xxxxxx we're in trouble
			if ((subCharacter & 0xc0) != 0x80) {
				throw new CharacterCodingException();
			}

			character = (character << 6) | (subCharacter & 0x3f);
		}

		// Detect invalid characters (byte order marks and surrogate pair code points)
		if ((character == 0xfffe) || (character == 0xffff) || ((character >= 0xd800) && (character <= 0xdfff))) {
			throw new CharacterCodingException();
		}

		// Detect invalid encodings (using more bytes than needed). Required by RFC 3629
		if (
				   (length == 2) && ((character & 0xFFFFFF80) == 0) // 00000000 00000000 00000000 0xxxxxxx forbidden (& 0xFFFFFF80)
				|| (length == 3) && ((character & 0xFFFFF800) == 0) // 00000000 00000000 00000xxx xxxxxxxx forbidden (& 0xFFFFF800)
				|| (length == 4) && ((character & 0xFFFE0000) == 0) // 00000000 0000000x xxxxxxxx xxxxxxxx forbidden (& 0xFFFE0000)
		   )
		{
			throw new CharacterCodingException();
		}
		return character;

	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.dictionary.CharacterHandler#getCharsetDecoder()
	 */
	public CharsetDecoder getCharsetDecoder() {
		return Charset.forName("UTF8").newDecoder();
	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.dictionary.CharacterHandler#getCharsetEncoder()
	 */
	public CharsetEncoder getCharsetEncoder() {
		return Charset.forName("UTF8").newEncoder();
	}

}
