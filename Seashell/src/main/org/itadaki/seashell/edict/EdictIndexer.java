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
import java.nio.IntBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.List;

import org.itadaki.seashell.CharacterHandler;
import org.itadaki.seashell.EntryParsingException;
import org.itadaki.seashell.IntegerIndexBuilder;


/**
 * Index generator for EDict style dictionaries
 */
public class EdictIndexer {

	/**
	 * The index builder for the dictionary
	 */
	private final IntegerIndexBuilder indexBuilder;

	/**
	 * The source data of the dictionary
	 */
	private final ByteBuffer edict;

	/**
	 * Character encoding handler used to read the dictionary
	 */
	private final CharacterHandler characterHandler;

	/**
	 * A comparator for the dictionary
	 */
	private final EdictComparator comparator;


	/**
	 * Reads a character from a ByteBuffer; throws EntryParsingException if it is not the expected character
	 *
	 * @param buffer The buffer to read from
	 * @param expectedCharacter The expected character
	 * @return The character as an int
	 * @throws EntryParsingException
	 * @throws CharacterCodingException 
	 */
	private int expectCharacter (ByteBuffer buffer, int expectedCharacter) throws EntryParsingException, CharacterCodingException {

		int character = this.characterHandler.readCharacter (buffer);
		if (character == expectedCharacter) {
			return character;
		}
		throw new EntryParsingException();

	}

	/**
	 * Checks a character equals an expected character; throws EntryParsingException if it is not the expected character
	 *
	 * @param character The actual character
	 * @param expectedCharacter The expected character
	 * @throws EntryParsingException
	 */
	private static void checkCharacter (int character, int expectedCharacter) throws EntryParsingException {

		if (character != expectedCharacter) {
			throw new EntryParsingException();
		}

	}


	/**
	 * Add a term to the index if there is not already a suitable entry (of which the term is a prefix) in the current entry's cache
	 *
	 * @param termCache The cache of existing entries
	 * @param position The new term's position
	 */
	private void addIndex (List<Integer> termCache, Integer position) {

		for (Integer termIndex : termCache) {
			try {
				if (this.comparator.compareLeftTerm (position, termIndex) == 0) {
					return;
				}
			} catch (CharacterCodingException e) {
				// Shouldn't happen here. Should have already been hit during line scanning
				e.printStackTrace();
			}
		}

		termCache.add (position);
		this.indexBuilder.add (position);

	}


	/**
	 * Retrieves sorted index
	 *
	 * @return The sorted index
	 */
	public IntBuffer getIndexData() {
		return this.indexBuilder.getSortedIndex();
	}


	/**
	 * @param edict The dictionary to index
	 * @param characterHandler The character encoding handler to use to read the dictionary
	 * @param discardHeaderLine Discard first line of dictionary if true
	 */
	public EdictIndexer (ByteBuffer edict, CharacterHandler characterHandler, boolean discardHeaderLine) {

		this.edict = edict;
		this.characterHandler = characterHandler;

		this.comparator = new EdictComparator (edict, characterHandler);
		this.indexBuilder = new IntegerIndexBuilder (this.comparator);


		this.edict.rewind();

		if (discardHeaderLine) {
			while (this.edict.get() != '\n');
		}


		List<Integer> termCache = new ArrayList<Integer>(100);

		int character;
		int position;

		while (this.edict.hasRemaining()) {

			termCache.clear();

			try {

				// Head entry
				position = this.edict.position();
				character = this.characterHandler.readCharacter (this.edict);
				while (character != ' ') {
					addIndex (termCache, position);
					position = this.edict.position();
					character = this.characterHandler.readCharacter (this.edict);
				};

				// Reading (optional)
				character = this.characterHandler.readCharacter (this.edict);
				if (character == '[') {
					position = this.edict.position();
					character = this.characterHandler.readCharacter (this.edict);
					do {
						addIndex (termCache, position);
						position = this.edict.position();
						character = this.characterHandler.readCharacter (this.edict);
					} while (character != ']');
					character = expectCharacter (this.edict, ' ');
					character = expectCharacter (this.edict, '/');
				} else {
					checkCharacter (character, '/');
				}

				// Translation(s)
				int termStart = edict.position();
				int termLength = 0;
				do {

					boolean subFieldOver = false;

					character = this.characterHandler.readCharacter (this.edict);					
					do {
						if (character == '/') {
							termLength = edict.position() - termStart;
							if (termLength > 3) {
								addIndex (termCache, termStart);
							}
							subFieldOver = true;
						} else if ((character == ' ') || (character == '-')) {
							termLength = edict.position() - termStart;
							if (termLength > 3) {
								addIndex (termCache, termStart);
							}
							termStart = edict.position();
							character = this.characterHandler.readCharacter (this.edict);
						} else if (character == '(') {
							do {
								character = this.characterHandler.readCharacter (this.edict);
							} while (character != ')');
							termStart = edict.position();
							character = this.characterHandler.readCharacter (this.edict);
						} else {
							character = this.characterHandler.readCharacter (this.edict);
						}
					} while (subFieldOver == false);
					
					checkCharacter (character, '/');

					termStart = edict.position();
					character = this.characterHandler.readCharacter (this.edict);

				} while ((character != '\r') && (character != '\n'));

				if (character == '\r') {
					character = expectCharacter (this.edict, '\n');
				}

			} catch (EntryParsingException e) {
				System.out.println ("Failed to parse dictionary line");
				while (this.edict.get() != '\n');
			} catch (CharacterCodingException e) {
				System.out.println ("Failed to parse dictionary line");
				while (this.edict.get() != '\n');
			}

		}

	}


	/**
	 * @param edict The dictionary to index
	 * @param characterHandler The character encoding handler to use to read the dictionary 
	 */
	public EdictIndexer (ByteBuffer edict, CharacterHandler characterHandler) {
		this (edict, characterHandler, true);
	}


}
