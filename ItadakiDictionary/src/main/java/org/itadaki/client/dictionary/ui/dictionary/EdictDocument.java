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

package org.itadaki.client.dictionary.ui.dictionary;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.nio.charset.CharacterCodingException;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.itadaki.fasttextpane.Document;
import org.itadaki.seashell.edict.EdictDictionary;


/**
 * A Document for consumption by FastTextView that formats EDICT-format dictionary entries
 */
public class EdictDocument extends Document {

	/**
	 * Result font size
	 * TODO make configurable
	 */
	private static final int FONT_SIZE = 16;

	/**
	 * The paragraphs comprising the result set 
	 */
	List<Paragraph> paragraphs = new ArrayList<Paragraph>();

	/**
	 * The case-folded search query to highlight
	 */
	private String foldedSearchQuery;


	/**
	 * Base class for paragraphs
	 */
	private abstract class Paragraph {

		/**
		 * The AttributedString representing the paragraph text
		 *
		 * @return The paragraph text
		 */
		public abstract AttributedString getAttributedString();

		/**
		 * The String representing the paragraph text
		 *
		 * @return The paragraph text
		 */
		public abstract String getPlainString();

		/**
		 * Gets the paragraph background highlight colour
		 *
		 * @return The background colour
		 */
		public abstract Color getHighlight();

	}


	/**
	 * Paragraph containing highlighted header text
	 */
	private class HeaderParagraph extends Paragraph {

		/**
		 * The header text
		 */
		private String header;


		/* (non-Javadoc)
		 * @see org.takadb.itadaki.ui.dictionary.EdictDocument.Paragraph#getAttributedString()
		 */
		@Override
		public AttributedString getAttributedString() {

			Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
			map.put (TextAttribute.FONT, new Font ("SansSerif", Font.BOLD, FONT_SIZE));
			map.put (TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);

			AttributedString attributedString = new AttributedString (this.header, map);

			return attributedString;

		}


		/* (non-Javadoc)
		 * @see org.takadb.itadaki.ui.dictionary.EdictDocument.Paragraph#getPlainString()
		 */
		@Override
		public String getPlainString() {

			return this.header + "\n";

		}


		/* (non-Javadoc)
		 * @see org.takadb.itadaki.ui.dictionary.EdictDocument.Paragraph#getHighlight()
		 */
		@Override
		public Color getHighlight() {
			return null;
		}


		/**
		 * @param header The header text
		 */
		public HeaderParagraph (String header) {

			this.header = header;

		}

	}


	/**
	 * A paragraph representing a blank line
	 */
	private class BlankLineParagraph extends Paragraph {

		/* (non-Javadoc)
		 * @see org.takadb.itadaki.ui.dictionary.EdictDocument.Paragraph#getAttributedString()
		 */
		@Override
		public AttributedString getAttributedString() {

			Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
			map.put (TextAttribute.FONT, new Font ("SansSerif", Font.PLAIN, FONT_SIZE));

			AttributedString attributedString = new AttributedString (" ", map);

			return attributedString;

		}


		/* (non-Javadoc)
		 * @see org.takadb.itadaki.ui.dictionary.EdictDocument.Paragraph#getPlainString()
		 */
		@Override
		public String getPlainString() {

			return "\n";

		}


		/* (non-Javadoc)
		 * @see org.takadb.itadaki.ui.dictionary.EdictDocument.Paragraph#getHighlight()
		 */
		@Override
		public Color getHighlight() {
			return null;
		}
		
	}


	/**
	 * A paragraph representing an EDICT-format dictionary entry
	 */
	private class ResultParagraph extends Paragraph {

		/**
		 * The dictionary that contains the entry
		 */
		private EdictDictionary dictionary;

		/**
		 * The index of the entry in the dictionary
		 */
		private int entryIndex;

		/**
		 * The background highlight colour
		 */
		private Color highlight;


		/**
		 * @param dictionary The dictionary that contains the entry
		 * @param documentIndex The index of the entry in the dictionary 
		 * @param highlight The background highlight colour
		 */
		public ResultParagraph (EdictDictionary dictionary, int documentIndex, Color highlight) {
			this.dictionary = dictionary;
			this.entryIndex = documentIndex;
			this.highlight = highlight;
		}


		/* (non-Javadoc)
		 * @see org.takadb.itadaki.ui.dictionary.EdictDocument.Paragraph#getAttributedString()
		 */
		@Override
		public AttributedString getAttributedString() {

			String entryString = getPlainString();
			Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
			map.put (TextAttribute.FONT, new Font ("SansSerif", Font.PLAIN, FONT_SIZE));

			AttributedString attributedString = new AttributedString (entryString, map);
			ResultMarshaller.highlightResult (entryString, attributedString, EdictDocument.this.foldedSearchQuery);

			return attributedString;

		}


		/* (non-Javadoc)
		 * @see org.takadb.itadaki.ui.dictionary.EdictDocument.Paragraph#getPlainString()
		 */
		@Override
		public String getPlainString() {

			try {
				String entryString = this.dictionary.readEntry(this.entryIndex).trim();
				entryString = entryString.replaceFirst ("\\(P\\)/$", "");

				StringBuilder builder = new StringBuilder();
				String[] fragments = entryString.split("/");
				if (fragments.length >= 2) {

					builder.append (fragments[0]);
					for (int i = 1; i < fragments.length; i++) {
						String fragment = fragments[i];
						builder.append (fragment.replaceAll ("\\(([0-9]+)\\)", "[$1]"));
						if (i < (fragments.length - 1)) {
							if (fragments[i+1].matches (".*\\([0-9]+\\).*")) {
								builder.append (". ");
							} else {
								builder.append ("; ");
							}
						} else {
							builder.append (".");
						}
					}
					builder.append ("\n");
				} else {
					// Shouldn't happen
					// TODO something sensible
				}
				return builder.toString();

			} catch (CharacterCodingException e) {

				return "\n";

			}

		}


		/* (non-Javadoc)
		 * @see org.takadb.itadaki.ui.dictionary.EdictDocument.Paragraph#getHighlight()
		 */
		@Override
		public Color getHighlight() {
			return this.highlight;
		}

	}


	/**
	 * Adds dictionary entries to the result set
	 *
	 * @param dictionary The dictionary that contains the entry
	 * @param highlight The background highlight colour
	 * @param entryIndices The indices of the entries in the dictionary 
	 */
	public void addResults (EdictDictionary dictionary, Color highlight, List<Integer> entryIndices) {
		synchronized (this) {
			for (Integer entryIndex : entryIndices) {
				this.paragraphs.add (new ResultParagraph (dictionary, entryIndex, highlight));
			}
		}
	}


	/**
	 * Adds blank lines to the result set
	 *
	 * @param numLines The number of blank lines to add
	 */
	public void addBlankLines (int numLines) {

		synchronized (this) {
			for (int i = 0; i < numLines; i++) {
				this.paragraphs.add (new BlankLineParagraph());
			}
		}

	}


	/**
	 * Adds a header line to the result set
	 *
	 * @param header The header text
	 */
	public void addHeader (String header) {

		synchronized (this) {
			this.paragraphs.add (new HeaderParagraph (header));
		}

	}


	/**
	 * Signals to listeners that the result set has expanded
	 */
	public void signalSizeChange() {
		
		int size;
		synchronized (this) {
			size = this.paragraphs.size();
		}
		setSize (size);

	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.resultpane.Document#getBackground(int)
	 */
	@Override
	public Color getBackground (int paragraphIndex) {

		synchronized (this) {
			return this.paragraphs.get(paragraphIndex).getHighlight();
		}

	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.resultpane.Document#getParagraph(int)
	 */
	@Override
	public AttributedString getParagraph (int paragraphIndex) {

		synchronized (this) {
			try {
				Paragraph entry = this.paragraphs.get (paragraphIndex);
				return entry.getAttributedString();
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		}

	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.resultpane.Document#getPlainParagraph(int)
	 */
	@Override
	public String getPlainParagraph(int paragraphIndex) {

		synchronized (this) {
			try {
				Paragraph entry = this.paragraphs.get (paragraphIndex);
				return entry.getPlainString();
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		}

	}


	/**
	 * @param foldedSearchQuery The case-folded search query text
	 */
	public EdictDocument (String foldedSearchQuery) {

		this.foldedSearchQuery = foldedSearchQuery;

	}


}