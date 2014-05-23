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
 * Describes the address of a single selection hit within the Document. Two
 * SelectionAddresses are processed together to make a SelectionRange 
 */
public class SelectionAddress implements Cloneable, Comparable<SelectionAddress> {

	/**
	 * The index within the Document of the paragraph in which the selection lies
	 */
	private int paragraphIndex;

	/**
	 * The selected character index within the paragraph
	 */
	private int characterIndex;

	/**
	 * true if the selection is from the leading edge of the character; false if on the trailing edge
	 */
	private boolean leading;


	/**
	 * Gets the selected character index within the paragraph
	 *
	 * @return The character index
	 */
	public int getCharacterIndex() {
		return this.characterIndex;
	}


	
	/**
	 * Gets the index within the Document of the selected paragraph
	 *
	 * @return The paragraph index
	 */
	public int getParagraphIndex() {
		return this.paragraphIndex;
	}


	/**
	 * Determines if the selection was made on the leading or trailing edge of the character
	 *
	 * @return true if the selection was on the leading edge of the character; false if on the trailing edge
	 */
	public boolean isLeading() {

		return this.leading;

	}


	/**
	 * Gets the line/character coordinates from a pixel position
	 *
	 * @param layout The layoutstate to examine 
	 * @param x The pixel X position
	 * @param y The pixel Y position
	 * @param oppositeAddress The address of the other end of the selection, or null
	 * @return The paragraph/line/character coordinates
	 */
	static SelectionAddress create (Layout layout, int x, int y, SelectionAddress oppositeAddress) {
	
		int paragraphIndex = 0;
		int characterIndex = 0;
	
		float paragraphY1 = layout.getFirstParagraphTop();
	
		for (ParagraphLayout paragraphLayout : layout.getParagraphLayoutList()) {
			paragraphIndex = paragraphLayout.paragraphIndex;
			float lineY1 = paragraphY1;
			characterIndex = 0;
			for (LineLayout lineLayout : paragraphLayout.lines) {
				if ((y >= lineY1) && (y <= lineY1 + lineLayout.height)) {

					if (x < lineLayout.leftIndent) {
						SelectionAddress address = new SelectionAddress (paragraphIndex, characterIndex, false);
						if ((oppositeAddress != null) && (address.compareTo (oppositeAddress) > 0)) {
							return address;
						}
						characterIndex = lineLayout.startCharacterIndex;
						address = new SelectionAddress (paragraphIndex, characterIndex, true);
						return address;
						
					}

					characterIndex = lineLayout.startCharacterIndex;

					int textX = lineLayout.leftIndent;
					for (TextLayout textLayout : lineLayout.textLayouts) {
						if ((x >= textX) && (x < (textX + textLayout.getAdvance()))) {
							TextHitInfo characterHitInfo = textLayout.hitTestChar (x - textX, y - lineY1);
							SelectionAddress address = new SelectionAddress (paragraphIndex, Math.min (lineLayout.endCharacterIndex, characterIndex + characterHitInfo.getCharIndex()), characterHitInfo.isLeadingEdge());
							return address;
						}
						textX += textLayout.getAdvance();
						characterIndex += textLayout.getCharacterCount();
					}

					return new SelectionAddress (paragraphIndex, lineLayout.endCharacterIndex, false);
				}

				characterIndex = lineLayout.endCharacterIndex;

				lineY1 += lineLayout.height;
			}
			paragraphY1 += paragraphLayout.height;
		}

		return new SelectionAddress (paragraphIndex, characterIndex, false);

	}


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public SelectionAddress clone() {

		// Only fundamental types, so plain Object.clone() will do
		try {
			return (SelectionAddress) super.clone();
		} catch (CloneNotSupportedException e) {
			// Can't happen
			e.printStackTrace();
		}
		return null;

	}


	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo (SelectionAddress other) {

		if (other.paragraphIndex != this.paragraphIndex) {
			return this.paragraphIndex - other.paragraphIndex;
		}

		if (other.characterIndex != this.characterIndex) {
			return this.characterIndex - other.characterIndex;
		}

		if (other.leading != this.leading) {
			return this.leading ? 1 : -1;
		}

		return 0;

	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object object) {

		SelectionAddress other = (SelectionAddress) object;
		return (compareTo (other) == 0);

	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SelectionAddress:{" + this.paragraphIndex + ":" + this.characterIndex + ":" + (this.leading ? 0 : 1) + "}";
	}


	/**
	 * @param paragraphIndex 
	 * @param characterIndex 
	 * @param leading 
	 */
	public SelectionAddress (int paragraphIndex, int characterIndex, boolean leading) {

		this.paragraphIndex = paragraphIndex;
		this.characterIndex = characterIndex;
		this.leading = leading;

	}


}
