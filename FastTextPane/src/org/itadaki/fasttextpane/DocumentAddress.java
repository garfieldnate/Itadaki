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

import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;


/**
 * Describes the address of a character within a Document 
 */
public class DocumentAddress implements Cloneable, Comparable<DocumentAddress> {

	/**
	 * The document the address lies within
	 */
	private Document document;

	/**
	 * The paragraph index within the Document
	 */
	private int paragraphIndex;

	/**
	 * The character index within the paragraph
	 */
	private int characterIndex;


	
	/**
	 * @return the document
	 */
	public Document getDocument() {
		return this.document;
	}


	/**
	 * Gets the paragraph index within the Document
	 *
	 * @return The paragraph index
	 */
	public int getParagraphIndex() {
		return this.paragraphIndex;
	}


	/**
	 * Gets the character index within the paragraph
	 *
	 * @return The character index
	 */
	public int getCharacterIndex() {
		return this.characterIndex;
	}

	
	/**
	 * Gets the line/character coordinates from a pixel position
	 *
	 * @param layout The layoutstate to examine 
	 * @param x The pixel X position
	 * @param y The pixel Y position
	 * @return The paragraph/character coordinates
	 */
	static DocumentAddress create (Layout layout, int x, int y) {
	
		float paragraphY1 = layout.getFirstParagraphTop();
	
		for (ParagraphLayout paragraphLayout : layout.getParagraphLayoutList()) {
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

						float startX = lineLayout.leftIndent;
						int startCharacterIndex = 0;
						for (TextLayout textLayout : lineLayout.textLayouts) {
							float endX = startX + textLayout.getAdvance();
							if ((x >= startX) && (x < endX)) {
								TextHitInfo characterHitInfo = textLayout.hitTestChar (x - startX, y - lineY1);
								
								DocumentAddress address = new DocumentAddress (paragraphLayout.paragraphIndex, startCharacterIndex + characterHitInfo.getCharIndex());
								return address;
							}
							startCharacterIndex += textLayout.getCharacterCount();
							startX = endX;
						}
					}
				}
				lineY1 += lineLayout.height;
			}
			paragraphY1 += paragraphLayout.height;
		}
	
		return null;
	
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DocumentAddress clone() {

		// Only fundamental types, so plain Object.clone() will do
		try {
			return (DocumentAddress) super.clone();
		} catch (CloneNotSupportedException e) {
			// Can't happen
			e.printStackTrace();
		}
		return null;

	}


	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo (DocumentAddress other) {

		if (other.paragraphIndex != this.paragraphIndex) {
			return this.paragraphIndex - other.paragraphIndex;
		}

		if (other.characterIndex != this.characterIndex) {
			return this.characterIndex - other.characterIndex;
		}

		return 0;

	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object object) {

		DocumentAddress other = (DocumentAddress) object;
		return (compareTo (other) == 0);

	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DocumentAddress:{" + this.paragraphIndex + ":" + this.characterIndex + "}";
	}


	/**
	 * @param documentIndex 
	 * @param entryCharacterIndex 
	 */
	public DocumentAddress (int documentIndex, int entryCharacterIndex) {

		this.paragraphIndex = documentIndex;
		this.characterIndex = entryCharacterIndex;

	}


}
