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

package org.itadaki.client.dictionary.ui.options;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 * Table cell renderer for a colour value
 */
public class ColourCellRenderer extends JLabel implements TableCellRenderer, ListCellRenderer {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Cell border when unselected
	 */
	Border unselectedBorder = null;

	/**
	 * Cell border when selected
	 */
	Border selectedBorder = null;


	/* TableCellRenderer interface */

	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent (JTable table, Object colour, boolean isSelected, boolean hasFocus, int row, int column) {

		if (this.unselectedBorder == null) {
			this.unselectedBorder = BorderFactory.createCompoundBorder (
					BorderFactory.createMatteBorder (5, 5, 5, 5, Color.WHITE), // should be table.getBackground() - but this is grey rather than L&F white ?
					BorderFactory.createLineBorder (Color.BLACK)
			);
		}

		if (this.selectedBorder == null) {
			this.selectedBorder = BorderFactory.createCompoundBorder (
					BorderFactory.createMatteBorder (5, 5, 5, 5, table.getSelectionBackground()),
					BorderFactory.createLineBorder (Color.BLACK)
			);
		}

		if (colour == null) {
			colour = Color.WHITE;
		}
		setBackground ((Color)colour);
		setBorder (isSelected ? this.selectedBorder : this.unselectedBorder);

		return this;
	}


	/* ListCellRenderer interface */

	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent (JList list, Object colour, int index, boolean isSelected, boolean cellHasFocus) {

		if (this.unselectedBorder == null) {
			this.unselectedBorder = BorderFactory.createCompoundBorder (
					BorderFactory.createMatteBorder (5, 5, 5, 5, Color.WHITE), // should be table.getBackground() - but this is grey rather than L&F white ?
					BorderFactory.createLineBorder (Color.BLACK)
			);
		}

		if (this.selectedBorder == null) {
			this.selectedBorder = BorderFactory.createCompoundBorder (
					BorderFactory.createMatteBorder (5, 5, 5, 5, list.getSelectionBackground()),
					BorderFactory.createLineBorder (Color.BLACK)
			);
		}

		if (colour == null) {
			colour = Color.WHITE;
		}
		setBackground ((Color)colour);
		setBorder (isSelected ? this.selectedBorder : this.unselectedBorder);

		return this;

	}


	/**
	 * Default constructor
	 */
	public ColourCellRenderer() {

		setPreferredSize (new Dimension (20, 20));
		setOpaque (true);

	}

}