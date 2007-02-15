/*
 * Copyright (C) 2002-2007
 * Takashi Okamoto <tora@debian.org>
 * Tsuyoshi Fukui <fukui556@oki.com>
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

package examples;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import net.java.sen.StringTagger;
import net.java.sen.SenFactory;
import net.java.sen.dictionary.Token;

/**
 * Demonstrates use of StringTagger
 */
public class StringTaggerDemo {

	/**
	 * Main method
	 *
	 * @param args Command line arguments: &lt;configuration&gt;
	 */
	public static void main(String args[]) {

		try {

			if (args.length != 1) {
				System.err.println("usage: java StringTaggerDemo <configuration>");
				System.exit(1);
			}

			String configFilename = args[0];
			StringTagger tagger = SenFactory.getStringTagger(configFilename);

			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Please input Japanese sentence:");

			String line;
			while ((line = reader.readLine()) != null) {
				List<Token> tokens = tagger.analyze(line);
				for (Token token : tokens) {
					System.out.println(
							token.toString() + "\t(" + token.getMorpheme().getBasicForm() + ")" + "\t" +
							token.getMorpheme().getPartOfSpeech() + "(" + token.getStart() + "," + token.end() + "," + token.getLength() + ")\t" +
							token.getMorpheme().getReadings().get(0) + "\t" +
							token.getMorpheme().getPronunciations().get(0)
					);
				}
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

}
