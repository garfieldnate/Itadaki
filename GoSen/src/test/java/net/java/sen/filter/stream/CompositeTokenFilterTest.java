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

package net.java.sen.filter.stream;

import net.java.sen.StringTagger;
import net.java.sen.dictionary.Morpheme;
import net.java.sen.dictionary.Token;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static net.java.sen.SenTestUtils.compareTokens;
import static net.java.sen.SenTestUtils.getStringTagger;


/**
 * Test Composite Token filter
 */
public class CompositeTokenFilterTest {

	/**
	 * Number composite
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testCompositeFilter1() throws IOException {

		String testString = "１１０";

		Token[] testTokens = new Token[] {
				new Token ("１１０", 8330, 0, 3, new Morpheme ("名詞-数", "*", "*", "１１０", new String[]{"イチイチゼロ"}, new String[]{"イチイチゼロ"}, null))
		};


		StringTagger tagger = getStringTagger();
		CompositeTokenFilter filter = new CompositeTokenFilter();
		filter.readRules (new BufferedReader (new StringReader ("名詞-数 名詞-数 名詞-数記号")));
		tagger.addFilter (filter);

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Number composite
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testCompositeFilter2() throws IOException {

		String testString = "ロンドン０１７１ー１２３４５６７";

		Token[] testTokens = new Token[] {
				new Token ("ロンドン", 3038, 0, 4, new Morpheme ("名詞-固有名詞-地域-一般", "*", "*", "ロンドン", new String[]{"ロンドン"}, new String[]{"ロンドン"}, null)),
				new Token ("０１７１", 26464, 4, 4, new Morpheme ("名詞-数", "*", "*", "０１７１", new String[]{"ゼロイチナナイチ"}, new String[]{"ゼロイチナナイチ"}, null)),
				new Token ("ー", 40061, 8, 1, new Morpheme ("未知語", null, null, "*", new String[]{}, new String[]{}, null)),
				new Token ("１２３４５６７", 322469, 9, 7, new Morpheme ("名詞-数", "*", "*", "１２３４５６７", new String[]{"イチニサンヨンゴロクナナ"}, new String[]{"イチニサンヨンゴロクナナ"}, null)),
		};


		StringTagger tagger = getStringTagger();
		CompositeTokenFilter filter = new CompositeTokenFilter();
		filter.readRules (new BufferedReader (new StringReader ("名詞-数 名詞-数 名詞-数記号")));
		tagger.addFilter (filter);

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


}
