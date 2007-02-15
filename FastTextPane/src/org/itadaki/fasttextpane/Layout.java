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

import java.awt.Dimension;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.List;


/**
 * Unified layout state for a FastTextView
 */
class Layout {

	/**
	 * The Document this Layout is a view upon
	 */
	private Document document;

	/**
	 * Currently visible paragraph layout
	 */
	private List<ParagraphLayout> paragraphLayoutList;

	/**
	 * The size of the calculated layout
	 */
	private Dimension size;

	/**
	 * The Document position of this layout
	 */
	private LayoutPosition layoutPosition;

	/**
	 * The laid out Y coordinate of the top of the first visible paragraph
	 */
	private float firstParagraphTop;

	/**
	 * The last visible paragraph found during layout
	 */
	private int layoutLastParagraphIndex;

	/**
	 * True if layout found the document is larger than the available display
	 * area
	 */
	private boolean overflowing;

	
	/**
	 * @return the document
	 */
	public Document getDocument() {
		return this.document;
	}


	/**
	 * @return the paragraphLayoutList
	 */
	public List<ParagraphLayout> getParagraphLayoutList() {
		return this.paragraphLayoutList;
	}


	/**
	 * @return the size
	 */
	public Dimension getSize() {
		return this.size;
	}


	/**
	 * @return the layoutPosition
	 */
	public LayoutPosition getLayoutPosition() {
		return this.layoutPosition;
	}


	/**
	 * @return the firstParagraphTop
	 */
	public float getFirstParagraphTop() {
		return this.firstParagraphTop;
	}


	/**
	 * @return the layoutLastParagraphIndex
	 */
	public int getLayoutLastParagraphIndex() {
		return this.layoutLastParagraphIndex;
	}


	/**
	 * @return the overflowing
	 */
	public boolean isOverflowing() {
		return this.overflowing;
	}


	/**
	 * @param document 
	 * @param paragraphLayoutList
	 * @param layoutPosition 
	 * @param size 
	 * @param firstParagraphTop 
	 * @param layoutLastParagraphIndex
	 * @param overflowing
	 */
	public Layout (Document document, List<ParagraphLayout> paragraphLayoutList, LayoutPosition layoutPosition, Dimension size, float firstParagraphTop, int layoutLastParagraphIndex, boolean overflowing) {

		this.document = document;
		this.paragraphLayoutList = paragraphLayoutList;
		this.layoutPosition = layoutPosition;
		this.size = size;
		this.firstParagraphTop = firstParagraphTop;
		this.layoutLastParagraphIndex = layoutLastParagraphIndex;
		this.overflowing = overflowing;

	}


	/**
	 * Blank Layout constructor
	 */
	public Layout() {

		this (null, new ArrayList<ParagraphLayout>(), new LayoutPosition (0, 0), new Dimension (0, 0), 0, 0, false);

	}


	/**
	 * Gets the value, if any, of the hyperlink at the given coordinates
	 *
	 * @param x The X coordinate
	 * @param y The Y coordinate
	 * @return The hyperlink value, or null
	 */
	public Object getHyperlinkValue (int x, int y) {

		float paragraphY1 = this.firstParagraphTop;
		
		for (ParagraphLayout paragraphLayout : this.paragraphLayoutList) {
			float lineY1 = paragraphY1;
			for (LineLayout lineLayout : paragraphLayout.lines) {
				if (lineLayout.textLayouts.length > 0) {
					if ((y >= lineY1) && (y <= lineY1 + lineLayout.height)) {

						if (x < lineLayout.leftIndent) {
							return null;
						}
						if (x > (lineLayout.maxExtent)) {
							return null;
						}

						int characterIndex = -1;
						float startX = lineLayout.leftIndent;
						int startCharacterIndex = 0;
						for (TextLayout textLayout : lineLayout.textLayouts) {
							float endX = startX + textLayout.getAdvance();
							if ((x >= startX) && (x < endX)) {
								TextHitInfo characterHitInfo = textLayout.hitTestChar (x - startX, y - lineY1);
								
								characterIndex = lineLayout.startCharacterIndex + startCharacterIndex + characterHitInfo.getCharIndex();
							}
							startCharacterIndex += textLayout.getCharacterCount();
							startX = endX;
						}

						if (startX == -1) {
							return null;
						}

						for (ParagraphLayout.Hyperlink hyperlink : paragraphLayout.hyperlinks) {
							if ((characterIndex >= hyperlink.startIndex) && (characterIndex <= hyperlink.endIndex)) {
								return hyperlink.value;
							}
						}

					}
				}
				lineY1 += lineLayout.height;
			}
			paragraphY1 += paragraphLayout.height;
		}
	
		return null;
	}


}
