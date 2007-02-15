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
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Hashtable;

import org.itadaki.fasttextpane.Document;

/**
 * A Document representing a set of literal strings
 */
public final class InformationalDocument extends Document {

	/**
	 * The literal strings to display
	 */
	ArrayList<AttributedString> lines = new ArrayList<AttributedString>();


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.resultpane.Document#getBackground(int)
	 */
	@Override
	public Color getBackground (int paragraphIndex) {
		return null;
	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.resultpane.Document#getParagraph(int)
	 */
	@Override
	public AttributedString getParagraph (int paragraphIndex) {

		try {
			return this.lines.get (paragraphIndex);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}

	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.resultpane.Document#getPlainParagraph(int)
	 */
	@Override
	public String getPlainParagraph(int paragraphIndex) {

		return "";

	}


	/**
	 * Adds a pre-formatted string
	 * 
	 * @param string The string to add
	 */
	public void add (AttributedString string) {

		this.lines.add (string);
		setSize (this.lines.size());

	}


	/**
	 * @param lines
	 */
	public InformationalDocument (String... lines) {

		Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
		map.put (TextAttribute.FONT, new Font ("SansSerif", Font.PLAIN, 14));

		for (String line : lines) {
			if (line.equals("")) {
				line = "\n";
			}
			this.lines.add (new AttributedString (line, map));
		}

		setSize (this.lines.size());

	}


}