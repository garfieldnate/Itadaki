package net.java.sen.examples;/*
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

import net.java.sen.SenFactory;
import net.java.sen.StreamTagger;
import net.java.sen.StringTagger;
import net.java.sen.dictionary.Token;
import net.java.sen.filter.stream.CommentFilter;
import net.java.sen.filter.stream.CompositeTokenFilter;
import net.java.sen.filter.stream.CompoundWordFilter;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Demonstrate the use of Filters
 */
public class FilterDemo {

	/**
	 * CompositeTokenFilter rules
	 */
	private static String compositeRules = "";

	/**
	 * CommentFilter rules
	 */
	private static String commentRules = "";

	/**
	 * True if a CompoundWordFilter is to be used, otherwise false
	 */
	private static boolean useCompoundFilter = false;

	/**
	 * CompoundWordFilter rules
	 */
	private static String compoundFile = null;


	/**
	 * Main method
	 *
	 * @param args Command line arguments: &lt;filename&gt; &lt;charset&gt;
	 */
	public static void main(String args[]) {

		try {

			if (args.length != 2) {
				System.err.println("usage: java ProcessorDemo <filename> <charset>");
				System.exit(1);
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), args[1]));
			String confPath = System.getProperty("sen.home") + System.getProperty("file.separator") + "conf/sen-processor.xml";
			StringTagger stringTagger = SenFactory.getStringTagger(confPath);
			StreamTagger tagger = new StreamTagger(stringTagger, reader);
			readConfig(confPath);

			if (useCompoundFilter) {
				CompoundWordFilter filter = new CompoundWordFilter(compoundFile);
				tagger.addFilter(filter);
			}

			if (compositeRules != null && !compositeRules.equals("")) {
				CompositeTokenFilter filter = new CompositeTokenFilter();
				filter.readRules(new BufferedReader(new StringReader(compositeRules)));
				tagger.addFilter(filter);
			}

			if (commentRules != null && !commentRules.equals("")) {
				CommentFilter filter = new CommentFilter();
				filter.readRules(new BufferedReader(new StringReader(commentRules)));
				tagger.addFilter(filter);
			}

			while (tagger.hasNext()) {
				Token token = tagger.next();
				System.out.println(
						token.getSurface() + "\t" +
						token.getMorpheme().getPartOfSpeech() + "\t" +
						token.getStart() + "\t" +
						token.end() + "\t" +
						token.getCost() + "\t" +
						token.getMorpheme().getAdditionalInformation()
				);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	/**
	 * Read the filter configuration from a file
	 *
	 * @param confFile The file to read from
	 */
	private static void readConfig(String confFile) {

		String parentDirectory = new File(confFile).getParentFile().getParent();
		String separator = System.getProperty("file.separator");

		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(confFile));
			NodeList nl = doc.getFirstChild().getChildNodes();

			for (int i = 0; i < nl.getLength(); i++) {
				org.w3c.dom.Node n = nl.item(i);
				if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
					String nn = n.getNodeName();
					String value = n.getFirstChild().getNodeValue();

					if (nn.equals("composit")) {
						compositeRules += value + "\n";
					}
					if (nn.equals("compound")) {
						// 構成語 (as constituent words) or 複合語 (as compounds)
						if (value.equals("構成語")) {
							useCompoundFilter = true;
						}
					}
					if (nn.equals("remark")) {
						commentRules += value + "\n";
					}
					if (nn.equals("dictionary")) {
						// read nested tag in <dictinary>
						NodeList dnl = n.getChildNodes();
						for (int j = 0; j < dnl.getLength(); j++) {
							org.w3c.dom.Node dn = dnl.item(j);
							if (dn.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {

								String dnn = dn.getNodeName();
								if (dn.getFirstChild() == null) {
									throw new IllegalArgumentException("element '" + dnn + "' is empty");
								}
								String dvalue = dn.getFirstChild().getNodeValue();

								if (dnn.equals("compound")) {
									compoundFile = parentDirectory + separator + dvalue;
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {

			throw new IllegalArgumentException(e.getMessage());

		}

	}

}
