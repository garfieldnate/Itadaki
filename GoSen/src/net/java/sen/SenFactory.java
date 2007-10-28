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

package net.java.sen;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.java.sen.dictionary.Dictionary;
import net.java.sen.dictionary.Tokenizer;
import net.java.sen.dictionary.Viterbi;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * A factory to manage creation of {@link Viterbi}, {@link StringTagger}, and
 * {@link ReadingProcessor} objects<br><br>
 * 
 * <b>Thread Safety:</b> This class and all its public methods are thread safe.
 * The objects constructed by the factory are <b>NOT</b> thread safe and should
 * not be accessed simultaneously by multiple threads
 */
public class SenFactory {

	/**
	 * Filename of the compiled connection cost data
	 */
	private static final String CONNECTION_COST_DATA_FILENAME = "connectionCost.sen";

	/**
	 * Filename of the compiled part of speech data
	 */
	private static final String PART_OF_SPEECH_DATA_FILENAME = "partOfSpeech.sen";

	/**
	 * Filename of the compiled token data
	 */
	private static final String TOKEN_DATA_FILENAME = "token.sen";

	/**
	 * Filename of the compiled trie data
	 */
	private static final String TRIE_DATA_FILENAME = "trie.sen";

	/**
	 * A cache of pre-loaded tokenizer configurations
	 */
	private static HashMap<String,Configuration> configurationCache = new HashMap<String,Configuration>();


	/**
	 * A Tokenizer configuration
	 */
	private static class Configuration {

		/**
		 * The connection cost matrix filename
		 */
		public String connectionCostFilename = null;

		/**
		 * The part-of-speech data filename
		 */
		public String partOfSpeechDataFilename = null;

		/**
		 * The token data filename
		 */
		public String tokenFilename = null;

		/**
		 * The trie data filename
		 */
		public String trieFilename = null;		

		/**
		 * The string to use for unknown morphemes
		 */
		public String unknownPartOfSpeechDescription = null;

		/**
		 * The Tokenizer's class name
		 */
		public String tokenizerClassName = null;

	}


	/**
	 * Loads a tokenizer configuration file
	 *
	 * @param configurationFilename The filename of the configuration to load
	 * @return The loaded configuration
	 */
	private static Configuration loadConfiguration(String configurationFilename) {

		Configuration configuration = new Configuration();

		String dictionaryVersion = "";

		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			File configurationFile = new File(configurationFilename);
			String parentDirectory = configurationFile.getParent();
			if (parentDirectory == null) {
				parentDirectory = ".";
			}
			String separator = System.getProperty("file.separator");

			configuration.connectionCostFilename = parentDirectory + separator + CONNECTION_COST_DATA_FILENAME;
			configuration.partOfSpeechDataFilename = parentDirectory + separator + PART_OF_SPEECH_DATA_FILENAME;
			configuration.tokenFilename = parentDirectory + separator + TOKEN_DATA_FILENAME;
			configuration.trieFilename = parentDirectory + separator + TRIE_DATA_FILENAME;
			
			Document document = builder.parse(new InputSource(configurationFilename));
			NodeList nodeList = document.getFirstChild().getChildNodes();

			for (int i = 0; i < nodeList.getLength(); i++) {
				org.w3c.dom.Node node = nodeList.item(i);
				if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
					String nodeName = node.getNodeName();
					String nodeValue = node.getFirstChild().getNodeValue();

					if (nodeName.equals("dictionary-version")) {
						dictionaryVersion = nodeValue;
					} else if (nodeName.equals("unknown-pos")) {
						configuration.unknownPartOfSpeechDescription = nodeValue;
					} else if (nodeName.equals("tokenizer")) {
						configuration.tokenizerClassName = nodeValue;
					}

				}
			}


		} catch (Exception e) {

			throw new IllegalArgumentException(e);

		}

		if (!dictionaryVersion.equals("1.0")) {

			throw new IllegalArgumentException("Invalid dictionary version \"" + dictionaryVersion + "\"");
		}

		return configuration;

	}


	/**
	 * Builds a Tokenizer for the given dictionary configuration
	 *
	 * @param configurationFilename The dictionary configuration filename
	 * @return The constructed Tokenizer
	 */
	private static Tokenizer getTokenizer(String configurationFilename) {

		Configuration configuration = null;

		Tokenizer tokenizer = null;


		String canonicalFilename = "";

		try {

			// Load configuration
			synchronized (SenFactory.class) {
	
				canonicalFilename = new File(configurationFilename).getCanonicalPath();
	
				configuration = configurationCache.get(canonicalFilename);
		
				if (configuration == null) {
					configuration = loadConfiguration(canonicalFilename);
					configurationCache.put(canonicalFilename, configuration);
				}
		
			}

		} catch (Exception e) {

			throw new IllegalArgumentException("Failed to load configuration \"" + canonicalFilename + "\"", e);

		}


		try {

			// Create tokenizer
			Class<?> tokenizerClass = Class.forName(configuration.tokenizerClassName);
			Constructor constructor = tokenizerClass.getConstructor(new Class[] { Dictionary.class, String.class });

			Dictionary dictionary = new Dictionary (
					configuration.connectionCostFilename,
					configuration.partOfSpeechDataFilename,
					configuration.tokenFilename,
					configuration.trieFilename
			);

			tokenizer = (Tokenizer) constructor.newInstance(new Object[] { dictionary, configuration.unknownPartOfSpeechDescription });

		} catch (Exception e) {

			throw new IllegalArgumentException("Failed to initialise Tokenizer class \"" + configuration.tokenizerClassName + "\"", e);

		}

		return tokenizer;

	}


	/**
	 * Creates a Viterbi from the given configuration file
	 *
	 * @param configurationFilename The configuration file
	 * @return A Viterbi created from the configuration file
	 */
	public static Viterbi getViterbi(String configurationFilename) {

		Tokenizer tokenizer = getTokenizer(configurationFilename);

		Viterbi viterbi = new Viterbi(tokenizer);

		return viterbi;

	}


	/**
	 * Creates a StringTagger from the given configuration file
	 *
	 * @param configurationFilename The configuration file
	 * @return A StringTagger created from the configuration file
	 */
	public static StringTagger getStringTagger(String configurationFilename) {

		Tokenizer tokenizer = getTokenizer(configurationFilename);

		StringTagger stringTagger = new StringTagger(tokenizer);

		return stringTagger;

	}


	/**
	 * Creates a ReadingProcessor from the given configuration file
	 *
	 * @param configurationFilename The configuration file
	 * @return A StringTagger created from the configuration file
	 */
	public static ReadingProcessor getReadingProcessor(String configurationFilename) {

		Tokenizer tokenizer = getTokenizer(configurationFilename);

		ReadingProcessor processor = new ReadingProcessor(tokenizer);

		return processor;

	}

}
