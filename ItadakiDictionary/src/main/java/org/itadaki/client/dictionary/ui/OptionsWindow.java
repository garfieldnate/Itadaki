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

package org.itadaki.client.dictionary.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import org.itadaki.client.dictionary.settings.Settings;
import org.itadaki.client.dictionary.ui.options.AddDictionaryAction;
import org.itadaki.client.dictionary.ui.options.CancelAction;
import org.itadaki.client.dictionary.ui.options.ColourCellRenderer;
import org.itadaki.client.dictionary.ui.options.DictionariesTableModel;
import org.itadaki.client.dictionary.ui.options.MoveDownAction;
import org.itadaki.client.dictionary.ui.options.MoveUpAction;
import org.itadaki.client.dictionary.ui.options.OKAction;
import org.itadaki.client.dictionary.ui.options.PaddedLabelTableCellRenderer;
import org.itadaki.client.dictionary.ui.options.RemoveDictionaryAction;


/**
 * Modal global options window
 */
public class OptionsWindow extends JDialog {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Default constructor
	 * @param parent JFrame to be modal upon
	 */
	public OptionsWindow (JFrame parent) {

		super (parent, "Options", true);

		JPanel panel = new JPanel();
		panel.setBorder (new EmptyBorder (10, 6, 10, 6));

		panel.setLayout (new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.fill = GridBagConstraints.BOTH;

		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 4;

		DictionariesTableModel dictionariesTableModel = new DictionariesTableModel (Settings.getInstance().getDictionarySettings());
		JTable dictionariesTable = new JTable (dictionariesTableModel);
		dictionariesTable.setShowGrid (false);
		dictionariesTable.setCellSelectionEnabled (false);
		dictionariesTable.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
		dictionariesTable.setRowSelectionAllowed (true);
		dictionariesTable.getColumnModel().getColumn(0).setCellRenderer (new ColourCellRenderer());
		dictionariesTable.getColumnModel().getColumn(1).setCellRenderer (new PaddedLabelTableCellRenderer());
		dictionariesTable.getColumnModel().getColumn(2).setCellRenderer (new PaddedLabelTableCellRenderer());
		dictionariesTable.getColumnModel().getColumn(0).setPreferredWidth(60);
		dictionariesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		dictionariesTable.getColumnModel().getColumn(2).setPreferredWidth(210);
		dictionariesTable.setIntercellSpacing (new Dimension (0, 0));
		dictionariesTable.setRowHeight (dictionariesTable.getRowHeight() + 10);

		JComboBox colourCombo = new JComboBox (Settings.getPossibleHighlightColours());
		colourCombo.setRenderer (new ColourCellRenderer());
		dictionariesTable.getColumnModel().getColumn(0).setCellEditor (new DefaultCellEditor (colourCombo));

		JScrollPane dictionariesPane = new JScrollPane (dictionariesTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		dictionariesPane.getViewport().setBackground (Color.WHITE);

		panel.add (dictionariesPane, constraints);

		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.NORTH;

		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.insets = new Insets (0, 10, 10, 0);

		JButton addButton = new JButton (new AddDictionaryAction (this, dictionariesTableModel));
		panel.add (addButton, constraints);


		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets (0, 10, 10, 0);

		JButton moveUpButton = new JButton (new MoveUpAction (dictionariesTable, dictionariesTableModel));
		panel.add (moveUpButton, constraints);


		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.gridx = 1;
		constraints.gridy = 2;
		constraints.gridheight = 1;
		constraints.insets = new Insets (0, 10, 10, 0);

		JButton moveDownButton = new JButton (new MoveDownAction (dictionariesTable, dictionariesTableModel));
		panel.add (moveDownButton, constraints);


		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.gridx = 1;
		constraints.gridy = 3;
		constraints.gridheight = 1;
		constraints.insets = new Insets (0, 10, 10, 0);

		JButton removeButton = new JButton (new RemoveDictionaryAction (dictionariesTable, dictionariesTableModel));
		panel.add (removeButton, constraints);


		JPanel okCancelPanel = new JPanel (new GridBagLayout());

		constraints.anchor = GridBagConstraints.EAST;
		constraints.fill = GridBagConstraints.NONE;

		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets (0, 10, 0, 0);
		
		JButton cancelButton = new JButton (new CancelAction (this));
		okCancelPanel.add (cancelButton, constraints);


		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets (0, 10, 0, 0);

		JButton okButton = new JButton (new OKAction (this, dictionariesTableModel));
		okCancelPanel.add (okButton, constraints);


		constraints.anchor = GridBagConstraints.EAST;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;
		constraints.insets = new Insets (10, 0, 0, 0);

		panel.add (okCancelPanel, constraints);

		add (panel);

		setSize (600, 400);
		setVisible (true);

	}

}
