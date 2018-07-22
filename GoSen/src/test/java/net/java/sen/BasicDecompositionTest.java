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
 * Tests basic string analysis
 */
public class BasicDecompositionTest {

	/**
	 * Tests string decomposition
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testBlankDecomposition() throws IOException {

		String testString = "";

		Token[] testTokens = new Token[] {};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Tests string decomposition
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testDecomposition1() throws IOException {

		String testString = "本来は、貧困層の女性や子供に医療保護を提供するために創設された制度である、アメリカ低所得者医療援助制度が、今日では、その予算の約３分の１を老人に費やしている。";

		Token[] testTokens = new Token[] {
				new Token ("本来", 3195, 0, 2, new Morpheme ("名詞-副詞可能", "*", "*", "本来", new String[]{"ホンライ"}, new String[]{"ホンライ"}, null)),
				new Token ("は", 4125, 2, 1, new Morpheme ("助詞-係助詞", "*", "*", "は", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("、", 4676, 3, 1, new Morpheme ("記号-読点", "*", "*", "、", new String[]{"、"}, new String[]{"、"}, null)),
				new Token ("貧困", 7944, 4, 2, new Morpheme ("名詞-一般", "*", "*", "貧困", new String[]{"ヒンコン"}, new String[]{"ヒンコン"}, null)),
				new Token ("層", 10887, 6, 1, new Morpheme ("名詞-接尾-一般", "*", "*", "層", new String[]{"ソウ"}, new String[]{"ソー"}, null)),
				new Token ("の", 11477, 7, 1, new Morpheme ("助詞-連体化", "*", "*", "の", new String[]{"ノ"}, new String[]{"ノ"}, null)),
				new Token ("女性", 13503, 8, 2, new Morpheme ("名詞-一般", "*", "*", "女性", new String[]{"ジョセイ"}, new String[]{"ジョセイ"}, null)),
				new Token ("や", 15060, 10, 1, new Morpheme ("助詞-並立助詞", "*", "*", "や", new String[]{"ヤ"}, new String[]{"ヤ"}, null)),
				new Token ("子供", 17172, 11, 2, new Morpheme ("名詞-一般", "*", "*", "子供", new String[]{"コドモ"}, new String[]{"コドモ"}, null)),
				new Token ("に", 18024, 13, 1, new Morpheme ("助詞-格助詞-一般", "*", "*", "に", new String[]{"ニ"}, new String[]{"ニ"}, null)),
				new Token ("医療", 20985, 14, 2, new Morpheme ("名詞-一般", "*", "*", "医療", new String[]{"イリョウ"}, new String[]{"イリョー"}, null)),
				new Token ("保護", 23887, 16, 2, new Morpheme ("名詞-サ変接続", "*", "*", "保護", new String[]{"ホゴ"}, new String[]{"ホゴ"}, null)),
				new Token ("を", 24670, 18, 1, new Morpheme ("助詞-格助詞-一般", "*", "*", "を", new String[]{"ヲ"}, new String[]{"ヲ"}, null)),
				new Token ("提供", 27282, 19, 2, new Morpheme ("名詞-サ変接続", "*", "*", "提供", new String[]{"テイキョウ"}, new String[]{"テイキョー"}, null)),
				new Token ("する", 27769, 21, 2, new Morpheme ("動詞-自立", "サ変・スル", "基本形", "する", new String[]{"スル"}, new String[]{"スル"}, null)),
				new Token ("ため", 29191, 23, 2, new Morpheme ("名詞-非自立-副詞可能", "*", "*", "ため", new String[]{"タメ"}, new String[]{"タメ"}, null)),
				new Token ("に", 29755, 25, 1, new Morpheme ("助詞-格助詞-一般", "*", "*", "に", new String[]{"ニ"}, new String[]{"ニ"}, null)),
				new Token ("創設", 32875, 26, 2, new Morpheme ("名詞-サ変接続", "*", "*", "創設", new String[]{"ソウセツ"}, new String[]{"ソーセツ"}, null)),
				new Token ("さ", 33362, 28, 1, new Morpheme ("動詞-自立", "サ変・スル", "未然レル接続", "する", new String[]{"サ"}, new String[]{"サ"}, null)),
				new Token ("れ", 33413, 29, 1, new Morpheme ("動詞-接尾", "一段", "連用形", "れる", new String[]{"レ"}, new String[]{"レ"}, null)),
				new Token ("た", 33661, 30, 1, new Morpheme ("助動詞", "特殊・タ", "基本形", "た", new String[]{"タ"}, new String[]{"タ"}, null)),
				new Token ("制度", 36364, 31, 2, new Morpheme ("名詞-一般", "*", "*", "制度", new String[]{"セイド"}, new String[]{"セイド"}, null)),
				new Token ("で", 37412, 33, 1, new Morpheme ("助詞-格助詞-一般", "*", "*", "で", new String[]{"デ"}, new String[]{"デ"}, null)),
				new Token ("ある", 38980, 34, 2, new Morpheme ("動詞-自立", "五段・ラ行", "基本形", "ある", new String[]{"アル"}, new String[]{"アル"}, null)),
				new Token ("、", 40599, 36, 1, new Morpheme ("記号-読点", "*", "*", "、", new String[]{"、"}, new String[]{"、"}, null)),
				new Token ("アメリカ", 43063, 37, 4, new Morpheme ("名詞-固有名詞-地域-国", "*", "*", "アメリカ", new String[]{"アメリカ"}, new String[]{"アメリカ"}, null)),
				new Token ("低", 46260, 41, 1, new Morpheme ("接頭詞-名詞接続", "*", "*", "低", new String[]{"テイ"}, new String[]{"テイ"}, null)),
				new Token ("所得", 48683, 42, 2, new Morpheme ("名詞-一般", "*", "*", "所得", new String[]{"ショトク"}, new String[]{"ショトク"}, null)),
				new Token ("者", 50299, 44, 1, new Morpheme ("名詞-接尾-一般", "*", "*", "者", new String[]{"シャ", "モノ"}, new String[]{"シャ", "モノ"}, null)),
				new Token ("医療", 53410, 45, 2, new Morpheme ("名詞-一般", "*", "*", "医療", new String[]{"イリョウ"}, new String[]{"イリョー"}, null)),
				new Token ("援助", 56468, 47, 2, new Morpheme ("名詞-サ変接続", "*", "*", "援助", new String[]{"エンジョ"}, new String[]{"エンジョ"}, null)),
				new Token ("制度", 59310, 49, 2, new Morpheme ("名詞-一般", "*", "*", "制度", new String[]{"セイド"}, new String[]{"セイド"}, null)),
				new Token ("が", 60204, 51, 1, new Morpheme ("助詞-格助詞-一般", "*", "*", "が", new String[]{"ガ"}, new String[]{"ガ"}, null)),
				new Token ("、", 61185, 52, 1, new Morpheme ("記号-読点", "*", "*", "、", new String[]{"、"}, new String[]{"、"}, null)),
				new Token ("今日", 63746, 53, 2, new Morpheme ("名詞-副詞可能", "*", "*", "今日", new String[]{"キョウ", "コンニチ"}, new String[]{"キョー", "コンニチ"}, null)),
				new Token ("で", 65050, 55, 1, new Morpheme ("助詞-格助詞-一般", "*", "*", "で", new String[]{"デ"}, new String[]{"デ"}, null)),
				new Token ("は", 65725, 56, 1, new Morpheme ("助詞-係助詞", "*", "*", "は", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("、", 66276, 57, 1, new Morpheme ("記号-読点", "*", "*", "、", new String[]{"、"}, new String[]{"、"}, null)),
				new Token ("その", 67904, 58, 2, new Morpheme ("連体詞", "*", "*", "その", new String[]{"ソノ"}, new String[]{"ソノ"}, null)),
				new Token ("予算", 70188, 60, 2, new Morpheme ("名詞-一般", "*", "*", "予算", new String[]{"ヨサン"}, new String[]{"ヨサン"}, null)),
				new Token ("の", 70858, 62, 1, new Morpheme ("助詞-連体化", "*", "*", "の", new String[]{"ノ"}, new String[]{"ノ"}, null)),
				new Token ("約", 72855, 63, 1, new Morpheme ("接頭詞-数接続", "*", "*", "約", new String[]{"ヤク"}, new String[]{"ヤク"}, null)),
				new Token ("３", 73816, 64, 1, new Morpheme ("名詞-数", "*", "*", "３", new String[]{"サン"}, new String[]{"サン"}, null)),
				// TODO: no, that's wrong
                new Token ("分の", 75945, 65, 2, new Morpheme ("名詞-接尾-助数詞", "*", "*", "分の", new String[]{"ブンノ"}, new String[]{"ブンノ"}, null)),
				new Token ("１", 77677, 67, 1, new Morpheme ("名詞-数", "*", "*", "１", new String[]{"イチ"}, new String[]{"イチ"}, null)),
				new Token ("を", 79879, 68, 1, new Morpheme ("助詞-格助詞-一般", "*", "*", "を", new String[]{"ヲ"}, new String[]{"ヲ"}, null)),
				new Token ("老人", 83022, 69, 2, new Morpheme ("名詞-一般", "*", "*", "老人", new String[]{"ロウジン"}, new String[]{"ロージン"}, null)),
				new Token ("に", 83874, 71, 1, new Morpheme ("助詞-格助詞-一般", "*", "*", "に", new String[]{"ニ"}, new String[]{"ニ"}, null)),
				new Token ("費やし", 87070, 72, 3, new Morpheme ("動詞-自立", "五段・サ行", "連用形", "費やす", new String[]{"ツイヤシ"}, new String[]{"ツイヤシ"}, null)),
				new Token ("て", 87413, 75, 1, new Morpheme ("助詞-接続助詞", "*", "*", "て", new String[]{"テ"}, new String[]{"テ"}, null)),
				new Token ("いる", 87653, 76, 2, new Morpheme ("動詞-非自立", "一段", "基本形", "いる", new String[]{"イル"}, new String[]{"イル"}, null)),
				new Token ("。", 88064, 78, 1, new Morpheme ("記号-句点", "*", "*", "。", new String[]{"。"}, new String[]{"。"}, null))

		};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Tests string decomposition
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testDecomposition2() throws IOException {

		String testString = "麻薬の密売は根こそぎ絶やさなければならない";

		Token[] testTokens = new Token[] {
				new Token ("麻薬", 3554, 0, 2, new Morpheme ("名詞-一般", "*", "*", "麻薬", new String[]{"マヤク"}, new String[]{"マヤク"}, null)),
				new Token ("の", 4224, 2, 1, new Morpheme ("助詞-連体化", "*", "*", "の", new String[]{"ノ"}, new String[]{"ノ"}, null)),
				new Token ("密売", 8123, 3, 2, new Morpheme ("名詞-サ変接続", "*", "*", "密売", new String[]{"ミツバイ"}, new String[]{"ミツバイ"}, null)),
				new Token ("は", 9248, 5, 1, new Morpheme ("助詞-係助詞", "*", "*", "は", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("根こそぎ", 12933, 6, 4, new Morpheme ("副詞-一般", "*", "*", "根こそぎ", new String[]{"ネコソギ"}, new String[]{"ネコソギ"}, null)),
				new Token ("絶やさ", 16791, 10, 3, new Morpheme ("動詞-自立", "五段・サ行", "未然形", "絶やす", new String[]{"タヤサ"}, new String[]{"タヤサ"}, null)),
                new Token ("なけれ", 17286, 13, 3, new Morpheme ("助動詞", "特殊・ナイ", "仮定形", "ない", new String[]{"ナケレ"}, new String[]{"ナケレ"}, null)),
				new Token ("ば", 17286, 16, 1, new Morpheme ("助詞-接続助詞", "*", "*", "ば", new String[]{"バ"}, new String[]{"バ"}, null)),
				new Token ("なら", 17969, 17, 2, new Morpheme ("動詞-非自立", "五段・ラ行", "未然形", "なる", new String[]{"ナラ"}, new String[]{"ナラ"}, null)),
				new Token ("ない", 18064, 19, 2, new Morpheme ("助動詞", "特殊・ナイ", "基本形", "ない", new String[]{"ナイ"}, new String[]{"ナイ"}, null))
		};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Tests string decomposition
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testDecomposition3() throws IOException {

		String testString = "魔女狩大将マシュー・ホプキンス。";

		Token[] testTokens = new Token[] {
				new Token ("魔女", 3864, 0, 2, new Morpheme ("名詞-一般", "*", "*", "魔女", new String[]{"マジョ"}, new String[]{"マジョ"}, null)),
				new Token ("狩", 8565, 2, 1, new Morpheme ("名詞-一般", "*", "*", "狩", new String[]{"カリ"}, new String[]{"カリ"}, null)),
				new Token ("大将", 12916, 3, 2, new Morpheme ("名詞-一般", "*", "*", "大将", new String[]{"タイショウ"}, new String[]{"タイショー"}, null)),
                new Token ("マシュー", 43720, 5, 4, new Morpheme ("未知語", null, null, "*", new String[]{}, new String[]{}, null)),
                new Token ("・", 45399, 9, 1, new Morpheme ("記号-一般", "*", "*", "・", new String[]{"・"}, new String[]{"・"}, null)),
                new Token ("ホプキンス", 76438, 10, 5, new Morpheme ("未知語", null, null, "*", new String[]{}, new String[]{}, null)),
				new Token ("。", 77496, 15, 1, new Morpheme ("記号-句点", "*", "*", "。", new String[]{"。"}, new String[]{"。"}, null))
		};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Tests string decomposition
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testDecomposition4() throws IOException {

		String testString = "これは本ではない";

		Token[] testTokens = new Token[] {
				new Token ("これ", 1851, 0, 2, new Morpheme ("名詞-代名詞-一般", "*", "*", "これ", new String[]{"コレ"}, new String[]{"コレ"}, null)),
				new Token ("は", 2448, 2, 1, new Morpheme ("助詞-係助詞", "*", "*", "は", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("本", 5179, 3, 1, new Morpheme ("名詞-一般", "*", "*", "本", new String[]{"ホン", "モト"}, new String[]{"ホン", "モト"}, null)),
				new Token ("で", 6467, 4, 1, new Morpheme ("助動詞", "特殊・ダ", "連用形", "だ", new String[]{"デ"}, new String[]{"デ"}, null)),
				new Token ("は", 6976, 5, 1, new Morpheme ("助詞-係助詞", "*", "*", "は", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("ない", 7096, 6, 2, new Morpheme ("助動詞", "特殊・ナイ", "基本形", "ない", new String[]{"ナイ"}, new String[]{"ナイ"}, null))
		};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}

    /**
     * Test the tokenizer that is able to recognize a Latin-1 accented character as a proper Latin character,
     * which will not handle as a separator.
     *
     * @throws IOException
     */
    @Test
    public void testLatinAccentedCharacter() throws IOException {
        StringTagger tagger = SenFactory.getStringTagger(SenTestUtils.DIC_PATH);

        String strTest = "mündlichen";

        Token[] expectedTokens = new Token[] {
            new Token ("mündlichen", 31057, 0, 10, new Morpheme ("未知語", null, null, "*", new String[]{}, new String[]{}, null))
        };

        List<Token> analyzedTokens = tagger.analyze(strTest);
        compareTokens (expectedTokens, analyzedTokens);
    }

    /**
     * Test the tokenizer that a half-width Katakana character handles properly.
     *
     * @throws IOException
     */
    @Test
    public void testKatakanaString() throws IOException {
        StringTagger tagger = SenFactory.getStringTagger(SenTestUtils.DIC_PATH);

        String strTest = "ッﾊﾞサ";

        Token[] expectedTokens = new Token[] {
            new Token ("ッﾊﾞサ", 31057, 0, 4, new Morpheme ("未知語", null, null, "*", new String[]{}, new String[]{}, null))
        };

        List<Token> analyzedTokens = tagger.analyze(strTest);
        compareTokens (expectedTokens, analyzedTokens);
    }
}