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
import java.util.List;

import javax.swing.table.AbstractTableModel;

import net.java.sen.dictionary.Token;

/**
 * TableModel used to show the results of analysis
 */
public class TokenTableModel extends AbstractTableModel {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Tokens displayed by the table
	 */
	private List<Token> tokens = new ArrayList<Token>();


	/**
	 * Column names
	 */
	private static final String[] columnNames = new String[] {

			"Start",
			"Text",
			"Part Of Speech",
			"Basic Form",
			"Conjugational Type",
			"Conjugational Form",
			"Reading",
			"Pronunciation"

	};


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {

		return columnNames.length;

	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {

		return this.tokens.size();

	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		Token token = this.tokens.get(rowIndex);

		switch (columnIndex) {

			case 0:
				return token.getStart();

			case 1:
				return token.getSurface();

			case 2:
				return token.getMorpheme().getPartOfSpeech();

			case 3:
				return token.getMorpheme().getBasicForm();

			case 4:
				return token.getMorpheme().getConjugationalType();

			case 5:
				return token.getMorpheme().getConjugationalForm();

			case 6:
				List<String> readings = token.getMorpheme().getReadings();
				switch (readings.size()) {
					case 0:
						return "";
					case 1:
						return readings.get(0);
					default:
						String readingText = "";
						for (int i = 0; i < readings.size(); i++) {
							readingText += readings.get(i);
							if (i != (readings.size() - 1)) {
								readingText += " / ";
							}
						}
						return readingText;
				}

			case 7:
				List<String> pronunciations = token.getMorpheme().getPronunciations();
				switch (pronunciations.size()) {
					case 0:
						return "";
					case 1:
						return pronunciations.get(0);
					default:
						String pronunciationText = "";
						for (int i = 0; i < pronunciations.size(); i++) {
							pronunciationText += pronunciations.get(i);
							if (i != (pronunciations.size() - 1)) {
								pronunciationText += " / ";
							}
						}
						return pronunciationText;
				}

		}

		return null;

	}


	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int columnIndex) {

		return columnNames[columnIndex];

	}


	/**
	 * Sets the tokens to display
	 * 
	 * @param tokens The tokens to display
	 */
	public void setTokens (List<Token> tokens) {

		this.tokens = tokens;
		this.fireTableDataChanged();

	}

}