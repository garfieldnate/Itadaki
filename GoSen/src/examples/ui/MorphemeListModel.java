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

package examples.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.AbstractListModel;

import net.java.sen.dictionary.Token;
import net.java.sen.util.TextUtil;


/**
 * Table model to display possible morpheme readings
 */
public class MorphemeListModel extends AbstractListModel {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The current reading list
	 */
	private List<MorphemeListEntry> readingList = new ArrayList<MorphemeListEntry>();


	/**
	 * An entry in the potential morpheme list 
	 */
	private static class MorphemeListEntry {

		/**
		 * The morpheme's token
		 */
		public Token token;

		/**
		 * The reading index within the token
		 */
		public int readingIndex;


		/**
		 * @param token The morpheme's token
		 * @param readingIndex The reading index within the token
		 */
		public MorphemeListEntry(Token token, int readingIndex) {

			this.token = token;
			this.readingIndex = readingIndex;

		}

	}


	/**
	 * Gets the reading for a given list index
	 *
	 * @param index The list index
	 * @return The reading at the given index
	 */
	public String getReadingAt(int index) {

		MorphemeListEntry entry = this.readingList.get(index);
		return entry.token.getMorpheme().getReadings().get(entry.readingIndex);

	}


	/**
	 * Gets the surface length covered by the reading at the given list index
	 *
	 * @param index The list index
	 * @return The surface length at the given index
	 */
	public int getSurfaceLengthAt(int index) {

		return this.readingList.get(index).token.getSurface().length();

	}


	/**
	 * Search current entry list for an index with the given reading text and length
	 *
	 * @param readingText The reading text to search for
	 * @param length The length to search for
	 * @return The entry list index found, or -1 if no match was found
	 */
	public int getIndexByReadingAndLength (String readingText, int length) {

		for (int i = 0; i < this.readingList.size(); i++) {
			MorphemeListEntry entry = this.readingList.get(i);
			if (
					   TextUtil.invertKanaCase(entry.token.getMorpheme().getReadings().get(entry.readingIndex)).equals(readingText)
					&& (entry.token.getSurface().length() == length)
			   )
			{
				return i;
			}
		}

		return -1;

	}


	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public Object getElementAt(int index) {

		MorphemeListEntry entry = this.readingList.get(index);

		String elementText = entry.token.getSurface() 
				+ "　（"
				+ TextUtil.invertKanaCase(entry.token.getMorpheme().getReadings().get(entry.readingIndex))
				+ "）";

		return elementText;

	}


	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize() {

		return this.readingList.size();

	}


	/**
	 * Sets the current token set to display readings for
	 * 
	 * @param tokens The tokens to set
	 */
	public void setTokens(List<Token> tokens) {

		// Sort by len(src), src, reading
		Comparator<MorphemeListEntry> readingComparator = new Comparator<MorphemeListEntry>() {

			public int compare(MorphemeListEntry o1, MorphemeListEntry o2) {

				if (o1.token.getSurface().length() != o2.token.getSurface().length()) {
					return o2.token.getSurface().length() - o1.token.getSurface().length();
				}

				int surfaceComparison = o1.token.getSurface().compareTo(o2.token.getSurface());
				if (surfaceComparison != 0) {
					return surfaceComparison;
				}

				return o1.token.getMorpheme().getReadings().get(o1.readingIndex).compareTo(o2.token.getMorpheme().getReadings().get(o2.readingIndex));

			}
			
		};

		TreeSet<MorphemeListEntry> listEntrySet = new TreeSet<MorphemeListEntry>(readingComparator);
		for (Token token: tokens) {
			List<String> readings = token.getMorpheme().getReadings();
			for (int i = 0; i < readings.size(); i++) {
				listEntrySet.add(new MorphemeListEntry(token, i));
			}
		}


		this.readingList = new ArrayList<MorphemeListEntry>(listEntrySet);
		fireContentsChanged(this, 0, this.readingList.size());

	}

}
