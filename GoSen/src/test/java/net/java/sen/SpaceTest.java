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

package net.java.sen;

import net.java.sen.dictionary.Morpheme;
import net.java.sen.dictionary.Token;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static net.java.sen.SenTestUtils.compareTokens;
import static net.java.sen.SenTestUtils.getStringTagger;


/**
 * Tests space skipping during string analysis
 */
public class SpaceTest {

	/**
	 * Leading space
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testLeadingSpace1() throws IOException {

		String testString = " これはテストだ";

		Token[] testTokens = new Token[] {
				new Token ("これ", 1851, 1, 2, new Morpheme ("名詞-代名詞-一般", "*", "*", "これ", new String[]{"コレ"}, new String[]{"コレ"}, null)),
				new Token ("は", 2448, 3, 1, new Morpheme ("助詞-係助詞", "*", "*", "は", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("テスト", 5787, 4, 3, new Morpheme ("名詞-サ変接続", "*", "*", "テスト", new String[]{"テスト"}, new String[]{"テスト"}, null)),
				new Token ("だ", 7301, 7, 1, new Morpheme ("助動詞", "特殊・ダ", "基本形", "だ", new String[]{"ダ"}, new String[]{"ダ"}, null))
		};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Leading space
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testLeadingSpace2() throws IOException {

		String testString = " 	これはテストだ";

		Token[] testTokens = new Token[] {
				new Token ("これ", 1851, 2, 2, new Morpheme ("名詞-代名詞-一般", "*", "*", "これ", new String[]{"コレ"}, new String[]{"コレ"}, null)),
				new Token ("は", 2448, 4, 1, new Morpheme ("助詞-係助詞", "*", "*", "は", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("テスト", 5787, 5, 3, new Morpheme ("名詞-サ変接続", "*", "*", "テスト", new String[]{"テスト"}, new String[]{"テスト"}, null)),
				new Token ("だ", 7301, 8, 1, new Morpheme ("助動詞", "特殊・ダ", "基本形", "だ", new String[]{"ダ"}, new String[]{"ダ"}, null))
		};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Trailing space
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testTrailingSpace1() throws IOException {

		String testString = "これはテストだ ";

		Token[] testTokens = new Token[] {
				new Token ("これ", 1851, 0, 2, new Morpheme ("名詞-代名詞-一般", "*", "*", "これ", new String[]{"コレ"}, new String[]{"コレ"}, null)),
				new Token ("は", 2448, 2, 1, new Morpheme ("助詞-係助詞", "*", "*", "は", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("テスト", 5787, 3, 3, new Morpheme ("名詞-サ変接続", "*", "*", "テスト", new String[]{"テスト"}, new String[]{"テスト"}, null)),
				new Token ("だ", 7301, 6, 1, new Morpheme ("助動詞", "特殊・ダ", "基本形", "だ", new String[]{"ダ"}, new String[]{"ダ"}, null))
		};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Trailing space
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testTrailingSpace2() throws IOException {

		String testString = "これはテストだ 	";

		Token[] testTokens = new Token[] {
				new Token ("これ", 1851, 0, 2, new Morpheme ("名詞-代名詞-一般", "*", "*", "これ", new String[]{"コレ"}, new String[]{"コレ"}, null)),
				new Token ("は", 2448, 2, 1, new Morpheme ("助詞-係助詞", "*", "*", "は", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("テスト", 5787, 3, 3, new Morpheme ("名詞-サ変接続", "*", "*", "テスト", new String[]{"テスト"}, new String[]{"テスト"}, null)),
				new Token ("だ", 7301, 6, 1, new Morpheme ("助動詞", "特殊・ダ", "基本形", "だ", new String[]{"ダ"}, new String[]{"ダ"}, null))
		};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Embedded space
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testEmbeddedSpace1() throws IOException {

		String testString = "これは テストだ";

		Token[] testTokens = new Token[] {
				new Token ("これ", 1851, 0, 2, new Morpheme ("名詞-代名詞-一般", "*", "*", "これ", new String[]{"コレ"}, new String[]{"コレ"}, null)),
				new Token ("は", 2448, 2, 1, new Morpheme ("助詞-係助詞", "*", "*", "は", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("テスト", 5787, 4, 3, new Morpheme ("名詞-サ変接続", "*", "*", "テスト", new String[]{"テスト"}, new String[]{"テスト"}, null)),
				new Token ("だ", 7301, 7, 1, new Morpheme ("助動詞", "特殊・ダ", "基本形", "だ", new String[]{"ダ"}, new String[]{"ダ"}, null))
		};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Embedded space
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testEmbeddedSpace2() throws IOException {

		String testString = "これは 	テストだ";

		Token[] testTokens = new Token[] {
				new Token ("これ", 1851, 0, 2, new Morpheme ("名詞-代名詞-一般", "*", "*", "これ", new String[]{"コレ"}, new String[]{"コレ"}, null)),
				new Token ("は", 2448, 2, 1, new Morpheme ("助詞-係助詞", "*", "*", "は", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("テスト", 5787, 5, 3, new Morpheme ("名詞-サ変接続", "*", "*", "テスト", new String[]{"テスト"}, new String[]{"テスト"}, null)),
				new Token ("だ", 7301, 8, 1, new Morpheme ("助動詞", "特殊・ダ", "基本形", "だ", new String[]{"ダ"}, new String[]{"ダ"}, null))
		};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Multiple space
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testMultipleSpace() throws IOException {

		String testString = "  	 これ は 	テストだ		 ";

		Token[] testTokens = new Token[] {
				new Token ("これ", 1851, 4, 2, new Morpheme ("名詞-代名詞-一般", "*", "*", "これ", new String[]{"コレ"}, new String[]{"コレ"}, null)),
				new Token ("は", 2448, 7, 1, new Morpheme ("助詞-係助詞", "*", "*", "は", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("テスト", 5787, 10, 3, new Morpheme ("名詞-サ変接続", "*", "*", "テスト", new String[]{"テスト"}, new String[]{"テスト"}, null)),
				new Token ("だ", 7301, 13, 1, new Morpheme ("助動詞", "特殊・ダ", "基本形", "だ", new String[]{"ダ"}, new String[]{"ダ"}, null))
		};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


}
