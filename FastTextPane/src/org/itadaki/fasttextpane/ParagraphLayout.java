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

package org.itadaki.fasttextpane;

import java.awt.Color;
import java.awt.font.TextLayout;
import java.text.AttributedString;
import java.util.List;


/**
 * Represents the pre-calculated layout of a paragraph at the current display width
 */
public class ParagraphLayout {

	/**
	 * Index of the represented paragraph in the Document
	 */
	public Integer paragraphIndex;

	/**
	 * AttributedString representing the paragraph text
	 */
	public AttributedString attributedString;

	/**
	 * Background highlight colour of the paragraph
	 */
	public Color background;

	/**
	 * The layouts for the individual lines of the paragraph
	 */
	public List<LineLayout> lines;

	/**
	 * The view width in pixels for which this layout is valid
	 */
	public int validWidth;

	/**
	 * The height in pixels of the paragraph
	 */
	public float height;

	/**
	 * The hyperlinks embedded in this paragraph
	 */
	public Hyperlink[] hyperlinks;


	/**
	 * Data on a hyperlink contained in a ParagraphLayout
	 */
	public static class Hyperlink {

		/**
		 * The first character index of the hyperlink
		 */
		public int startIndex;

		/**
		 * The last character index of the hyperlink
		 */
		public int endIndex;

		/**
		 * The payload object of the hyperlink
		 */
		public Object value;


		/**
		 * @param startIndex
		 * @param endIndex
		 * @param value
		 */
		public Hyperlink (int startIndex, int endIndex, Object value) {

			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.value = value;

		}

	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		StringBuffer buffer = new StringBuffer();
		buffer.append ("ParagraphLayout:{\n");
		buffer.append ("  .paragraphIndex=" + this.paragraphIndex + "\n");
		buffer.append ("  .background=" + this.background + "\n");
		buffer.append ("  .validWidth=" + this.validWidth + "\n");
		buffer.append ("  .height=" + this.height + "\n");
		buffer.append ("  .lines={\n");
		for (LineLayout line : this.lines) {
			for (TextLayout textLayout : line.textLayouts) {
				buffer.append (textLayout.getCharacterCount() + " ");
			}
			buffer.append ("\n");
		}
		buffer.append ("  }\n");
		buffer.append ("}");
		return buffer.toString();
	}



	/**
	 * @param documentIndex Index of the represented paragraph in the Document
	 * @param attributedString AttributedString representing the paragraph text
	 * @param background Background highlight colour of the paragraph
	 * @param lines The layouts for the individual lines of the paragraph
	 * @param validWidth The view width in pixels for which this layout is valid
	 * @param height The height in pixels of the paragraph
	 * @param hyperlinks The hyperlinks embedded in this paragraph
	 */
	public ParagraphLayout (Integer documentIndex, AttributedString attributedString, Color background, List<LineLayout> lines, int validWidth, float height, Hyperlink[] hyperlinks) {

		this.paragraphIndex = documentIndex;
		this.attributedString = attributedString;
		this.background = background;
		this.lines = lines;
		this.validWidth = validWidth;
		this.height = height;
		this.hyperlinks = hyperlinks;

	}

	
}
