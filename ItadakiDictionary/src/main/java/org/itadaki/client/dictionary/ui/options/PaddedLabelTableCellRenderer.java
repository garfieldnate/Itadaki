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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 * A String TableCellRenderer with extra internal padding
 */
public class PaddedLabelTableCellRenderer implements TableCellRenderer {

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


	public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		if (this.unselectedBorder == null) {
			this.unselectedBorder = BorderFactory.createMatteBorder (5, 5, 5, 5, Color.WHITE); // should be table.getBackground() - but this is grey rather than L&F white ?
		}

		if (this.selectedBorder == null) {
			this.selectedBorder = BorderFactory.createMatteBorder (5, 5, 5, 5, table.getSelectionBackground());
		}

		JLabel label = new JLabel ((String)value);
		label.setBorder (isSelected ? this.selectedBorder : this.unselectedBorder);
		label.setForeground (isSelected ? table.getSelectionForeground() : table.getForeground());
		label.setOpaque (true);
		// Why is this necessary? Most peculiar
		label.setBackground (isSelected ? new Color (table.getSelectionBackground().getRGB()) : Color.WHITE);

		return label;

	}
	
}