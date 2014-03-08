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

package test;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;

import org.junit.Test;
import org.itadaki.seashell.SearchMode;
import org.itadaki.seashell.edict.EdictDictionary;
import org.itadaki.seashell.edict.EdictIndexer;
import org.itadaki.seashell.edict.EdictSearchResults;
import org.itadaki.seashell.edict.EdictSearcher;
import org.itadaki.seashell.edict.EUCJPHandler;


/**
 * Tests org.itadaki.seashell.edict.EdictSearcher
 */
public class TestEdictSearcher {

	/**
	 * Test basic search matcher functionality
	 *
	 * @throws Exception
	 */
	@Test
	public void testBasicSearch() throws Exception {

		String testDictionaryString =
			"あああ /aaa/\r\n" +
			"かかか /kkk/\r\n" +
			"さささ /sss/\r\n";

		CharsetEncoder encoder = Charset.forName("EUC-JP").newEncoder();
		ByteBuffer testEdict = encoder.encode (CharBuffer.wrap (testDictionaryString));

		EdictIndexer indexer = new EdictIndexer (testEdict, new EUCJPHandler(), false);
		IntBuffer indexData = indexer.getIndexData();

		EdictSearcher searcher = new EdictDictionary(testEdict, indexData).searcher();

		EdictSearchResults results;
		results = searcher.search ("あ");
		assertEquals (new Integer(3), results.getFirstMatch());
		assertEquals (new Integer(3), results.getLastMatch());

		results = searcher.search ("か");
		assertEquals (new Integer(4), results.getFirstMatch());
		assertEquals (new Integer(4), results.getLastMatch());

		results = searcher.search ("さ");
		assertEquals (new Integer(5), results.getFirstMatch());
		assertEquals (new Integer(5), results.getLastMatch());

		results = searcher.search ("a");
		assertEquals (new Integer(0), results.getFirstMatch());
		assertEquals (new Integer(0), results.getLastMatch());

		results = searcher.search ("k");
		assertEquals (new Integer(1), results.getFirstMatch());
		assertEquals (new Integer(1), results.getLastMatch());

		results = searcher.search ("s");
		assertEquals (new Integer(2), results.getFirstMatch());
		assertEquals (new Integer(2), results.getLastMatch());

		results = searcher.search ("t");
		assertNull (results.getFirstMatch());
		assertNull (results.getLastMatch());

	}


	/**
	 * Test "ANY" position searches
	 *
	 * @throws Exception
	 */
	@Test
	public void testIteratorAny() throws Exception {

		String testDictionaryString =
			"あああ /aaa/\r\n" +
			"かかか /kkk/\r\n" +
			"さささ /sss/\r\n";

		CharsetEncoder encoder = Charset.forName("EUC-JP").newEncoder();
		ByteBuffer testEdict = encoder.encode (CharBuffer.wrap (testDictionaryString));

		EdictIndexer indexer = new EdictIndexer (testEdict, new EUCJPHandler(), false);
		IntBuffer indexData = indexer.getIndexData();

		EdictSearcher searcher = new EdictDictionary(testEdict, indexData).searcher();

		Iterator<Integer> resultsIterator = searcher.search("か").iterator (SearchMode.ANY);
		assertEquals (new Integer(14), resultsIterator.next());
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

	}

	/**
	 * Test "EXACT" position searches
	 *
	 * @throws Exception
	 */
	@Test
	public void testIteratorExact() throws Exception {

		String testDictionaryString =
			"あああ /aaa/\r\n" +
			"かかか /kkk/\r\n" +
			"さささ /sss/\r\n";

		CharsetEncoder encoder = Charset.forName("EUC-JP").newEncoder();
		ByteBuffer testEdict = encoder.encode (CharBuffer.wrap (testDictionaryString));

		EdictIndexer indexer = new EdictIndexer (testEdict, new EUCJPHandler(), false);
		IntBuffer indexData = indexer.getIndexData();

		EdictSearcher searcher = new EdictDictionary(testEdict, indexData).searcher();

		Iterator<Integer> resultsIterator;

		resultsIterator = searcher.search("か").iterator (SearchMode.EXACT);
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

		resultsIterator = searcher.search("かかか").iterator (SearchMode.EXACT);
		assertTrue (resultsIterator.hasNext());
		assertEquals (new Integer(14), resultsIterator.next());
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

	}


	/**
	 * Test "START" position searches
	 *
	 * @throws Exception
	 */
	@Test
	public void testIteratorStart() throws Exception {

		String testDictionaryString =
			"あああ /aaa/\r\n" +
			"かかか /kkk/\r\n" +
			"さささ /sss/\r\n";

		CharsetEncoder encoder = Charset.forName("EUC-JP").newEncoder();
		ByteBuffer testEdict = encoder.encode (CharBuffer.wrap (testDictionaryString));

		EdictIndexer indexer = new EdictIndexer (testEdict, new EUCJPHandler(), false);
		IntBuffer indexData = indexer.getIndexData();

		EdictSearcher searcher = new EdictDictionary(testEdict, indexData).searcher();

		Iterator<Integer> resultsIterator;

		resultsIterator = searcher.search("か").iterator (SearchMode.START);
		assertTrue (resultsIterator.hasNext());
		assertEquals (new Integer(14), resultsIterator.next());
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

		resultsIterator = searcher.search("かか").iterator (SearchMode.START);
		assertTrue (resultsIterator.hasNext());
		assertEquals (new Integer(14), resultsIterator.next());
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

		resultsIterator = searcher.search("かかか").iterator (SearchMode.START);
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

	}

	/**
	 * Test "END" position searches
	 *
	 * @throws Exception
	 */
	@Test
	public void testIteratorEnd() throws Exception {

		String testDictionaryString =
			"あああ /aaa/\r\n" +
			"かたは /kkk/\r\n" +
			"さささ /sss/\r\n";

		CharsetEncoder encoder = Charset.forName("EUC-JP").newEncoder();
		ByteBuffer testEdict = encoder.encode (CharBuffer.wrap (testDictionaryString));

		EdictIndexer indexer = new EdictIndexer (testEdict, new EUCJPHandler(), false);
		IntBuffer indexData = indexer.getIndexData();

		EdictSearcher searcher = new EdictDictionary(testEdict, indexData).searcher();

		Iterator<Integer> resultsIterator;

		resultsIterator = searcher.search("か").iterator (SearchMode.END);
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

		resultsIterator = searcher.search("かた").iterator (SearchMode.END);
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

		resultsIterator = searcher.search("は").iterator (SearchMode.END);
		assertTrue (resultsIterator.hasNext());
		assertEquals (new Integer(14), resultsIterator.next());
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

		resultsIterator = searcher.search("たは").iterator (SearchMode.END);
		assertTrue (resultsIterator.hasNext());
		assertEquals (new Integer(14), resultsIterator.next());
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

		resultsIterator = searcher.search("かたは").iterator (SearchMode.END);
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

	}


	/**
	 * Test "MIDDLE" position searches
	 *
	 * @throws Exception
	 */
	@Test
	public void testIteratorMiddle() throws Exception {

		String testDictionaryString =
			"あああ /aaa/\r\n" +
			"かたは /kkk/\r\n" +
			"さささ /sss/\r\n";

		CharsetEncoder encoder = Charset.forName("EUC-JP").newEncoder();
		ByteBuffer testEdict = encoder.encode (CharBuffer.wrap (testDictionaryString));

		EdictIndexer indexer = new EdictIndexer (testEdict, new EUCJPHandler(), false);
		IntBuffer indexData = indexer.getIndexData();

		EdictSearcher searcher = new EdictDictionary(testEdict, indexData).searcher();

		Iterator<Integer> resultsIterator;

		resultsIterator = searcher.search("か").iterator (SearchMode.MIDDLE);
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

		resultsIterator = searcher.search("かた").iterator (SearchMode.MIDDLE);
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

		resultsIterator = searcher.search("た").iterator (SearchMode.MIDDLE);
		assertTrue (resultsIterator.hasNext());
		assertEquals (new Integer(14), resultsIterator.next());
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

		resultsIterator = searcher.search("は").iterator (SearchMode.MIDDLE);
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

		resultsIterator = searcher.search("たは").iterator (SearchMode.MIDDLE);
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

		resultsIterator = searcher.search("かたは").iterator (SearchMode.MIDDLE);
		assertFalse (resultsIterator.hasNext());
		assertNull (resultsIterator.next());

	}

}
