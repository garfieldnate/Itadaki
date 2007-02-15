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
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import org.itadaki.seashell.CharacterHandler;


/**
 * EUC-JP character handler
 */
public class EUCJPHandler implements CharacterHandler {

	/* (non-Javadoc)
	 * @see org.takadb.itadaki.dictionary.CharacterHandler#foldCharacterCase(int)
	 */
	public int foldCharacterCase (int character) {
	
		if ((character >= 'A') && (character <= 'Z')) {
			// Alphabetic upper case to lower case
			character |= 0x20;
		} else if ((character & 0xff00) == 0xa500) {
			// Katakana -> hiragana
			character &= 0xfeff;
		}
	
		return character;
	
	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.dictionary.CharacterHandler#readCharacter(java.nio.ByteBuffer)
	 */
	public int readCharacter (ByteBuffer buffer) {
	
		int character = buffer.get() & 0xff;
	
		if (character > 127) {
			if (character == 0x8f) {
				character = (character << 8) + (buffer.get() & 0xff); 
			}
			character = (character << 8) + (buffer.get() & 0xff);
		}
	
		return character;
	
	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.dictionary.CharacterHandler#getCharsetDecoder()
	 */
	public CharsetDecoder getCharsetDecoder() {
		return Charset.forName("EUC-JP").newDecoder();
	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.dictionary.CharacterHandler#getCharsetEncoder()
	 */
	public CharsetEncoder getCharsetEncoder() {
		return Charset.forName("EUC-JP").newEncoder();
	}

}
