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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.Comparator;

import org.itadaki.seashell.CharacterHandler;

/**
 * Comparator for Edict style dictionaries
 */
public class EdictComparator implements Comparator<Integer> {

	/**
	 * First view of the dictionary
	 */
	private ByteBuffer edictCopy1;

	/**
	 * Second view of the dictionary
	 */
	private ByteBuffer edictCopy2;

	/**
	 * The character encoding handler to use to read the dictionary
	 */
	private CharacterHandler characterHandler;


	/**
	 * Compare data in two EDICT style dictionaries
	 *
	 * @param dictionary1 First dictionary 
	 * @param dictionary2 Second dictionary
	 * @param characterHandler The character encoding handler to use to read the dictionary
	 * @param position1 Position to start comparison in first dictionary
	 * @param position2 Position to start comparison in second dictionary
	 * @param maxBytes Maximum number of bytes to compare. A match is considered exact on reaching this number
	 * @return -1 (less than), 0 (exact), 1 (more than)
	 * @throws CharacterCodingException 
	 */
	public static int staticCompare (ByteBuffer dictionary1, ByteBuffer dictionary2, CharacterHandler characterHandler,
	                                 Integer position1, Integer position2, Integer maxBytes) throws CharacterCodingException
	{

		dictionary1.position (position1);
		dictionary2.position (position2);

		int character1, character2;
		try {
			// This is sufficient, if not necessarily optimal
			int end = (int) Math.min (Integer.MAX_VALUE, (long) position1 + (long) maxBytes);
			while (dictionary1.position() < end) {
				character1 = characterHandler.foldCharacterCase (characterHandler.readCharacter (dictionary1));
				character2 = characterHandler.foldCharacterCase (characterHandler.readCharacter (dictionary2));
				if (character1 < character2) {
					return -1;
				} else if (character1 > character2) {
					return 1;
				}
			}

		} catch (BufferUnderflowException ex) {
			if (dictionary1.hasRemaining()) {
				return 1;
			} else if (dictionary2.hasRemaining()) {
				return -1;
			}
		}

		return 0;

	}


	/**
	 * Left match a literal ByteBuffer to a position in the dictionary. On
	 * matching the number of bytes in the query, the match is exact (even
	 * if the dictionary field continues)
	 *
	 * @param query The encoded substring to compare (in the same encoding as the dictionary)
	 * @param position2 The position in the dictionary to compare to
	 * @return -1 (less than), 0 (exact), 1 (more than)
	 * @throws CharacterCodingException 
	 */
	public int compareLeft (ByteBuffer query, Integer position2) throws CharacterCodingException {

		return staticCompare (query, this.edictCopy2, this.characterHandler, 0, position2, query.limit());

	}

	/**
	 * Left match two dictionary terms. On reaching a non-term character on the left, the search is exact
	 *
	 * @param position1 Position of the term to compare from
	 * @param position2 Position of the term to compare from
	 * @return -1 (less than), 0 (exact), 1 (more than)
	 * @throws CharacterCodingException 
	 */
	public int compareLeftTerm (Integer position1, Integer position2) throws CharacterCodingException {

		this.edictCopy1.position (position1);
		this.edictCopy2.position (position2);

		int character1, character2;
		try {
			// This is sufficient, if not necessarily optimal
			while (true) {
				character1 = this.characterHandler.foldCharacterCase (this.characterHandler.readCharacter (this.edictCopy1));
				switch (character1) {
					case ' ':
					case '-':
					case ']':
					case '/':
						return 0;
				}
				character2 = this.characterHandler.foldCharacterCase (this.characterHandler.readCharacter (this.edictCopy2));
				if (character1 < character2) {
					return -1;
				} else if (character1 > character2) {
					return 1;
				}
			}
	
		} catch (BufferUnderflowException ex) {
			if (this.edictCopy1.hasRemaining()) {
				return 1;
			} else if (this.edictCopy2.hasRemaining()) {
				return -1;
			}
		}
	
		return 0;

	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare (Integer position1, Integer position2) {

		try {
			if (position1 == position2) {
				return 0;
			}

			return staticCompare (this.edictCopy1, this.edictCopy2, this.characterHandler, position1, position2, Integer.MAX_VALUE);
		} catch (Exception e) {
			throw new IllegalArgumentException (e);
		}

	}


	/**
	 * @param edict The dictionary that will be compared within
	 * @param characterHandler The character encoding handler to use to read the dictionary
	 */
	public EdictComparator (ByteBuffer edict, CharacterHandler characterHandler) {
		this.characterHandler = characterHandler;
		this.edictCopy1 = edict.duplicate();
		this.edictCopy2 = edict.duplicate();			
	}

}