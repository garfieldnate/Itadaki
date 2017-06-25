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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;

import org.itadaki.seashell.CharacterHandler;
import org.itadaki.seashell.DictionaryException;

/**
 * Wrapper for an EDICT format dictionary
 */
public class EdictDictionary {

	/**
	 * The dictionary's filename
	 */
	private String dictionaryFileName;

	/**
	 * The dictionary data
	 */
	private ByteBuffer dictionary;

	/**
	 * The term index
	 */
	private IntBuffer index;

	/**
	 * The character encoding handler to use to read the dictionary
	 */
	private CharacterHandler characterHandler;

	/**
	 * Returns the dictionary's filename
	 * 
	 * @return The dictionary's filename
	 */
	public String getFilename() {

		return this.dictionaryFileName;

	}

	/**
	 * Accessor for characterHandler
	 * 
	 * @return The characterHandler
	 */
	public CharacterHandler getCharacterHandler() {
		return this.characterHandler;
	}

	/**
	 * Create a searcher for this dictionary
	 * 
	 * @return An EdictSearcher instance
	 * @throws DictionaryException
	 */
	public EdictSearcher searcher() throws DictionaryException {

		if (this.index != null) {
			return new EdictSearcher(this);
		}

		throw new DictionaryException();

	}

	/**
	 * Supply a comparator for term positions within the dictionary
	 * 
	 * @return The comparator
	 */
	public EdictComparator comparator() {

		return new EdictComparator(this.dictionary, this.characterHandler);

	}

	/**
	 * Retrieves the term position referred to by an index entry
	 * 
	 * @param indexPosition
	 *            The index position
	 * @return The term position
	 */
	public Integer getIndexEntry(Integer indexPosition) {

		return this.index.get(indexPosition);

	}

	/**
	 * Retrieves the size of the index
	 * 
	 * @return The size of the index
	 */
	public Integer getIndexSize() {

		return this.index.limit();

	}

	/**
	 * Read an entry from the dictionary
	 * 
	 * @param position
	 *            The entry position
	 * @return The entry as a String
	 * @throws CharacterCodingException
	 */
	public String readEntry(Integer position) throws CharacterCodingException {

		int end;
		for (end = position; this.dictionary.get(end) != '\n'; end++)
			;

		CharsetDecoder decoder = this.characterHandler.getCharsetDecoder();
		this.dictionary.position(position);
		ByteBuffer copy = this.dictionary.slice();
		copy.limit(end - position);
		return decoder.decode(copy).toString();

	}

	/**
	 * Search backwards from a dictionary position to find the start of the
	 * entry
	 * 
	 * @param position
	 *            The position from which to start searching
	 * @return The start of the entry
	 */
	public int findStartOfEntry(int position) {

		byte character;

		do {
			if (position == 0) {
				return 0;
			}
			position--;
			character = this.dictionary.get(position);
		} while (character != '\n');

		return position + 1;

	}

	/**
	 * Test if the given dictionary position is the start of a word
	 * 
	 * @param position
	 *            The position to test
	 * @return true if at the start of a word
	 */
	public boolean isWordStart(int position) {
		if (position == 0) {
			return true;
		}
		byte character = this.dictionary.get(position - 1);
		switch (character) {
		case '\n':
		case ' ':
		case '-':
		case '[':
		case '/':
			return true;
		}
		return false;
	}

	/**
	 * Test if the given dictionary position is the end of a word
	 * 
	 * @param position
	 *            The position to test
	 * @return true if at the end of a word
	 */
	public boolean isWordEnd(int position) {
		if (position >= this.dictionary.limit()) {
			return true;
		}
		byte character = this.dictionary.get(position + 1);
		switch (character) {
		case ' ':
		case '-':
		case ']':
		case '/':
			return true;
		}
		return false;
	}

	/**
	 * Guess the encoding of an EDICT format dictionary
	 * 
	 * @param buffer
	 *            The dictionary data
	 * @return A CharacterHandler appropriate for the dictionary's encoding
	 */
	private static CharacterHandler guessEncoding(ByteBuffer buffer) {

		CharacterHandler handler = null;

		try {
			buffer.rewind();
			handler = new UTF8Handler();
			for (int i = 0; i < 1000; i++) {
				handler.readCharacter(buffer);
			}
			return handler;
		} catch (BufferUnderflowException e) {
			return handler;
		} catch (CharacterCodingException e) {
			// Do nothing
		}

		try {
			buffer.rewind();
			handler = new EUCJPHandler();
			for (int i = 0; i < 1000; i++) {
				handler.readCharacter(buffer);
			}
			return handler;
		} catch (BufferUnderflowException e) {
			return handler;
		} catch (CharacterCodingException e) {
			// Do nothing
		}

		return null;

	}

	/**
	 * Check if a file appears to be in EDICT format
	 * 
	 * @param dictionaryFileName
	 *            File to test
	 * @param decoder
	 *            Charset decoder to use
	 * @return true if the file format seems correct; false otherwise
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static boolean testFileFormat(String dictionaryFileName,
			CharsetDecoder decoder) throws FileNotFoundException, IOException {

		BufferedReader formatTestReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(dictionaryFileName),
						decoder));

		int i = 0;
		String line = null;
		do {
			line = formatTestReader.readLine();
			if ((line != null)
					&& !line.matches("^[^ ]{1,} ?(\\[[^]]*\\] )?/.*/$")) {
				formatTestReader.close();
				return false;
			}
			i++;
		} while ((i < 10) && (line != null));

		formatTestReader.close();
		return true;
	}

	/**
	 * Check if a file appears to be in EDICT format
	 * 
	 * @param dictionaryFileName
	 *            File to test
	 * @return true if the file format seems correct; false otherwise
	 */
	public static boolean testFileFormat(String dictionaryFileName) {

		FileChannel dictionaryChannel;

		try {
			RandomAccessFile dictionaryFile = new RandomAccessFile(
					dictionaryFileName, "r");
			dictionaryChannel = dictionaryFile.getChannel();
			ByteBuffer dictionary = dictionaryChannel.map(
					FileChannel.MapMode.READ_ONLY, 0,
					(int) dictionaryChannel.size());
			CharacterHandler handler = guessEncoding(dictionary);
			testFileFormat(dictionaryFileName, handler.getCharsetDecoder());
			dictionaryFile.close();
		} catch (Exception e) {
			return false;
		}

		return true;

	}

	/**
	 * Indicates if the dictionary has an index file
	 * 
	 * @return True if the index file exists, false otherwise
	 */
	private boolean indexExists() {

		File indexFile = new File(this.dictionaryFileName + ".iidx");
		return indexFile.exists();

	}

	/**
	 * Indicates if the dictionary's index is loaded
	 * 
	 * @return True if the index is loaded, false otherwise
	 */
	public boolean hasIndex() {

		return !(this.index == null);

	}

	/**
	 * Creates a new index file and attaches it to the dictionary
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void createIndex() throws FileNotFoundException, IOException {

		File temporaryIndexFile = new File(this.dictionaryFileName
				+ ".iidx.tmp");
		EdictIndexer indexer = new EdictIndexer(this.dictionary,
				this.characterHandler);
		IntBuffer indexBuffer = indexer.getIndexData();
		RandomAccessFile tempIndexFile = new RandomAccessFile(temporaryIndexFile, "rw");
		FileChannel indexChannel = tempIndexFile.getChannel();
		IntBuffer fileIndexBuffer = indexChannel.map(
				FileChannel.MapMode.READ_WRITE, 0, indexBuffer.limit() * 4)
				.asIntBuffer();
		fileIndexBuffer.put(indexBuffer);
		indexChannel.close();
		tempIndexFile.close();

		File indexFile = new File(this.dictionaryFileName + ".iidx");
		if (indexFile.exists() && !indexFile.delete()) {
			throw new IOException("Could not delete old index file");
		}
		if (!temporaryIndexFile.renameTo(indexFile)) {
			// renameTo often fails on Windows, so try copying and deleting
			// instead
			System.err
					.println("Renaming temporary index file failed; copying instead");
			copyFile(temporaryIndexFile, indexFile);
			if (!temporaryIndexFile.delete()) {
				System.err.println("Could not delete temporary index file ("
						+ temporaryIndexFile + ")");
			}
		}

		loadIndex();

	}

	/**
	 * Copy a file. This might work when {@link File#renameTo(File)} fails.
	 * 
	 * @param source
	 *            File to copy from
	 * @param dest
	 *            File to copy to
	 * @throws IOException
	 *             if copying fails
	 */
	private static void copyFile(File source, File dest) throws IOException {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(source);
			output = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
		} finally {
			input.close();
			output.close();
		}
	}

	/**
	 * Loads an existing index file
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadIndex() throws FileNotFoundException, IOException {

		RandomAccessFile indexFile = new RandomAccessFile(new File(
				this.dictionaryFileName + ".iidx"), "r");
		FileChannel indexChannel = indexFile.getChannel();
		this.index = indexChannel.map(FileChannel.MapMode.READ_ONLY, 0,
				(int) indexChannel.size()).asIntBuffer();
		indexFile.close();

	}

	/**
	 * Wraps existing dictionary and index buffers
	 * 
	 * @param dictionary
	 *            The dictionary data
	 * @param index
	 *            The term index data
	 */
	public EdictDictionary(ByteBuffer dictionary, IntBuffer index) {

		this.characterHandler = new EUCJPHandler();

		this.dictionary = dictionary;
		this.index = index;

	}

	/**
	 * Load the EDICT EUC-JP or UTF8 format dictionary with the given filename
	 * 
	 * @param dictionaryFileName
	 *            The dictionary to load
	 * 
	 * @throws DictionaryException
	 */
	public EdictDictionary(String dictionaryFileName)
			throws DictionaryException {

		FileChannel dictionaryChannel;
		try {

			this.dictionaryFileName = dictionaryFileName;

			dictionaryChannel = new RandomAccessFile(dictionaryFileName, "r")
					.getChannel();
			this.dictionary = dictionaryChannel.map(
					FileChannel.MapMode.READ_ONLY, 0,
					(int) dictionaryChannel.size());

			this.characterHandler = guessEncoding(this.dictionary);
			if (this.characterHandler == null) {
				throw new DictionaryException();
			}

			if (!testFileFormat(dictionaryFileName,
					this.characterHandler.getCharsetDecoder())) {
				throw new DictionaryException();
			}

			if (indexExists()) {
				loadIndex();
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new DictionaryException(e);
		}

	}

}