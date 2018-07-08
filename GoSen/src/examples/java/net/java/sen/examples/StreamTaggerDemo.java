/*
 * Copyright (C) 2002-2007
 * Takashi Okamoto <tora@debian.org>
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
package net.java.sen.examples;

import net.java.sen.SenFactory;
import net.java.sen.StreamTagger;
import net.java.sen.StringTagger;
import net.java.sen.dictionary.Morpheme;
import net.java.sen.dictionary.Token;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Demonstrates use of StreamTagger
 */
public class StreamTaggerDemo {

	/**
	 * Main method
	 *
	 * @param args Command line arguments: &lt;configuration&gt; &lt;filename&gt; &lt;charset&gt;
	 */
	public static void main(String args[]) {

		try {

			if (args.length != 3) {
				System.err.println("usage: java StreamTaggerDemo <configuration> <filename> <charset>");
				System.exit(1);
			}

			String configFilename = args[0];
			String inputFilename = args[1];
			String charset = args[2];

			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilename), charset));
			StringTagger stringTagger = SenFactory.getStringTagger(configFilename);
			StreamTagger tagger = new StreamTagger(stringTagger, reader);

			while (tagger.hasNext()) {
				Token token = tagger.next();
				Morpheme morpheme = token.getMorpheme();
				String reading = (token.getMorpheme().getReadings().size() > 0) ? morpheme.getReadings().get(0) : "null";
				String pronunciation = (morpheme.getPronunciations().size() > 0) ? morpheme.getPronunciations().get(0) : "null";
				System.out.println(
						token.toString() + "\t(" + morpheme.getBasicForm() + ")" + "\t" +
						morpheme.getPartOfSpeech() + "(" + token.getStart() + "," + token.end()	+ "," + token.getLength() + ")\t" +
						reading + "\t" +
						pronunciation
				);
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

}
