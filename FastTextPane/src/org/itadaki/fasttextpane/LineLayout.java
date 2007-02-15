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

/**
 * Class representing a single laid-out line with an indent
 */
class LineLayout {

	/**
	 * Left indent of the line
	 */
	public int leftIndent;

	/**
	 * Right indent of the line
	 */
	public int maxExtent;

	/**
	 * Text layout for the line
	 */
	public TextLayout[] textLayouts;

	/**
	 * Foreground colours of the line segments
	 */
	public Color[] foregrounds;

	/**
	 * First character index of the line in the underlying paragraph
	 */
	public int startCharacterIndex;

	/**
	 * Last character index of the line in the underlying paragraph
	 */
	public int endCharacterIndex;

	/**
	 * Height in pixels of the line
	 */
	public float height;


	/**
	 * @param leftIndent Left indent of the line
	 * @param maxExtent Right indent of the line
	 * @param height Height in pixels of the line
	 * @param textLayouts Text layout segments for the line
	 * @param foregrounds Foreground colours of the line segments
	 * @param startCharacterIndex Last character index of the line in the underlying paragraph 
	 * @param endCharacterIndex First character index of the line in the underlying paragraph
	 */
	public LineLayout (int leftIndent, int maxExtent, float height, TextLayout[] textLayouts, Color[] foregrounds, int startCharacterIndex, int endCharacterIndex) {

		this.leftIndent = leftIndent;
		this.maxExtent = maxExtent;
		this.height = height;
		this.textLayouts = textLayouts;
		this.foregrounds = foregrounds;
		this.startCharacterIndex = startCharacterIndex;
		this.endCharacterIndex = endCharacterIndex;

	}

}