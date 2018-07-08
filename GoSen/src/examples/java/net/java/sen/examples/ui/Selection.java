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

package net.java.sen.examples.ui;


/**
 * Represents a selection within an AnalysisPanel
 */
public class Selection implements Cloneable {

	/**
	 * Token index of the selection's start
	 */
	public int startTokenIndex = -1;

	/**
	 * Character index within the starting token
	 */
	public int startCharacterIndex = -1;

	/**
	 * Token index of the selection's end
	 */
	public int endTokenIndex = -1;

	/**
	 * Character index within the ending token
	 */
	public int endCharacterIndex = -1;


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {

		if (!(other instanceof Selection)) {
			return false;
		}

		Selection otherSelection = (Selection)other;

		if (
				   (this.startTokenIndex == otherSelection.startTokenIndex)
				&& (this.startCharacterIndex == otherSelection.startCharacterIndex)
				&& (this.endTokenIndex == otherSelection.endTokenIndex)
				&& (this.endCharacterIndex == otherSelection.endCharacterIndex)
		   )
		{
			return true;
		}

		return false;

	}


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Selection clone() {
		// No objects so default shallow clone is fine
		try {
			return (Selection) super.clone();
		} catch (CloneNotSupportedException e) {
			// Can't happen
			e.printStackTrace();
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return this.startTokenIndex + ":" + this.startCharacterIndex + " - " + this.endTokenIndex + ":" + this.endCharacterIndex;

	}


	/**
	 * @param startTokenIndex The starting token index
	 * @param startCharacterIndex The starting character index
	 * @param endTokenIndex The ending token index
	 * @param endCharacterIndex The ending character index
	 */
	public Selection(int startTokenIndex, int startCharacterIndex, int endTokenIndex, int endCharacterIndex) {
		this.startTokenIndex = startTokenIndex;
		this.startCharacterIndex = startCharacterIndex;
		this.endTokenIndex = endTokenIndex;
		this.endCharacterIndex = endCharacterIndex;
	}

}
