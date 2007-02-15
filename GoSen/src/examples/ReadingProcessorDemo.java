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

package examples;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import examples.ui.AnalysisPanel;
import examples.ui.MorphemeListModel;
import examples.ui.Selection;
import examples.ui.SelectionListener;
import examples.ui.TokenTableModel;

import net.java.sen.ReadingProcessor;
import net.java.sen.SenFactory;
import net.java.sen.ReadingProcessor.ReadingResult;
import net.java.sen.dictionary.Reading;
import net.java.sen.dictionary.Token;
import net.java.sen.filter.reading.NumberFilter;
import net.java.sen.util.TextUtil;


/**
 * Swing demonstration of Sen features
 */
public class ReadingProcessorDemo {

	/**
	 * ReadingProcessor used to analyse text
	 */
	private ReadingProcessor readingProcessor;

	/**
	 * The input text field
	 */
	private JTextField inputTextField;

	/**
	 * The token detail table
	 */
	private JTable tokenDetailTable;

	/**
	 * The token table model
	 */
	private TokenTableModel tokenDetailTableModel;

	/**
	 * The analysis panel
	 */
	private AnalysisPanel analysisPanel;

	/**
	 * Manual reading constraint input field
	 */
	private JTextField manualReadingTextField;

	/**
	 * The morpheme reading list
	 */
	private JList morphemeList;

	/**
	 * The currently displayed reading processor result
	 */
	private ReadingResult readingResult;

	/**
	 * <code>true</code> while the token selection is changing
	 */
	private boolean selectionChanging = false;

	/**
	 * List model used to display possible morpheme readings
	 */
	private MorphemeListModel morphemeListModel;


	/**
	 * An Action that skips the selection backwards to the previous token with a
	 * reading, if any
	 */
	private final class PreviousTokenWithReadingAction extends AbstractAction {

		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 1L;


		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
		
			Selection selection = ReadingProcessorDemo.this.analysisPanel.getSelection();
			List<Token> tokens = ReadingProcessorDemo.this.readingResult.getTokens();
			BitSet visibleTokens = ReadingProcessorDemo.this.readingResult.getVisibleTokens();
		
			if (selection != null) {
				for (int i = selection.startTokenIndex - 1; i >= 0; i--) {
					if (visibleTokens.get(i)) {
						Selection newSelection = new Selection(i, 0, i, tokens.get(i).getLength() - 1);
						ReadingProcessorDemo.this.analysisPanel.setSelection(newSelection);
						break;
					}
				}
			}

		}

	}


	/**
	 * An Action that skips the selection forward to the next token with a
	 * reading, if any
	 */
	private final class NextTokenWithReadingAction extends AbstractAction {

		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 1L;


		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
		
			Selection selection = ReadingProcessorDemo.this.analysisPanel.getSelection();
			List<Token> tokens = ReadingProcessorDemo.this.readingResult.getTokens();
			BitSet visibleTokens = ReadingProcessorDemo.this.readingResult.getVisibleTokens();
		
			if (selection != null) {
				for (int i = selection.startTokenIndex + 1; i < tokens.size(); i++) {
					if (visibleTokens.get(i)) {
						Selection newSelection = new Selection(i, 0, i, tokens.get(i).getLength() - 1);
						ReadingProcessorDemo.this.analysisPanel.setSelection(newSelection);
						break;
					}
				}
			}

		}

	}


	/**
	 * An Action that selects the previous possible reading at the current
	 * position,  if any
	 */
	private final class PreviousReadingAction extends AbstractAction {

		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 1L;


		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {

			int selectedIndex = ReadingProcessorDemo.this.morphemeList.getSelectedIndex();
			int nextIndex = Math.max(0, selectedIndex - 1);

			if (selectedIndex != nextIndex) {
				ReadingProcessorDemo.this.morphemeList.setSelectedIndex(nextIndex);
			}

		}

	}


	/**
	 * An Action that selects the next possible reading at the current position,
	 * if any
	 */
	private final class NextReadingAction extends AbstractAction {

		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 1L;


		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {

			int selectedIndex = ReadingProcessorDemo.this.morphemeList.getSelectedIndex();
			int nextIndex = Math.min(ReadingProcessorDemo.this.morphemeListModel.getSize() - 1, selectedIndex + 1);

			if (selectedIndex != nextIndex) {
				ReadingProcessorDemo.this.morphemeList.setSelectedIndex(nextIndex);
			}

		}

	}


	/**
	 * An Action that shifts focus to the custom reading entry
	 */
	private final class CustomReadingAction extends AbstractAction {

		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 1L;


		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {

			ReadingProcessorDemo.this.manualReadingTextField.requestFocusInWindow();
			ReadingProcessorDemo.this.manualReadingTextField.selectAll();

		}

	}


	/**
	 * An Action that moves the ends of the selection in character increments
	 */
	private final class SelectionMotionAction extends AbstractAction {

		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * <code>true</code> to move that start of the action,
		 * <code>false</code> the end
		 */
		private final boolean moveStart;

		/**
		 * <code>true</code> to move left, <code>false</code> right 
		 */
		private final boolean moveLeft;


		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {

			Selection selection = ReadingProcessorDemo.this.analysisPanel.getSelection();
			List<Token> tokens = ReadingProcessorDemo.this.readingResult.getTokens();

			if (selection == null) {
				return;
			}


			int tokenIndex = (this.moveStart) ? selection.startTokenIndex : selection.endTokenIndex;
			int characterIndex = (this.moveStart) ? selection.startCharacterIndex : selection.endCharacterIndex;

			if (this.moveLeft) {
				if ((tokenIndex > 0) || (characterIndex > 0)) {
					characterIndex--;
					if (characterIndex < 0) {
						tokenIndex--;
						characterIndex = tokens.get(tokenIndex).getLength() - 1;
					}
				}
			} else {
				if ((tokenIndex < (tokens.size() - 1)) || (characterIndex < (tokens.get(tokenIndex).getLength() - 1))) {
					characterIndex++;
					if (characterIndex > (tokens.get(tokenIndex).getLength() - 1)) {
						tokenIndex++;
						characterIndex = 0;
					}
				}
			}

			if (this.moveStart) {
				selection.startTokenIndex = tokenIndex;
				selection.startCharacterIndex = characterIndex;
				if (
						   (selection.startTokenIndex > selection.endTokenIndex)
						|| (
								(selection.startTokenIndex == selection.endTokenIndex)
								&& (selection.startCharacterIndex > selection.endCharacterIndex)
						   )
				   )
				{
					selection.endTokenIndex = selection.startTokenIndex;
					selection.endCharacterIndex = selection.startCharacterIndex;
				}
			} else {
				selection.endTokenIndex = tokenIndex;
				selection.endCharacterIndex = characterIndex;
				if (
						   (selection.endTokenIndex < selection.startTokenIndex)
						|| (
								(selection.endTokenIndex == selection.startTokenIndex)
								&& (selection.endCharacterIndex < selection.startCharacterIndex)
						   )
				   )
				{
					selection.startTokenIndex = selection.endTokenIndex;
					selection.startCharacterIndex = selection.endCharacterIndex;
				}				
			}

			ReadingProcessorDemo.this.analysisPanel.setSelection(selection);

		}


		/**
		 * @param moveStart <code>true</code> to move that start of the action,
		 *                  <code>false</code> the end
		 * @param moveLeft <code>true</code> to move left, <code>false</code>
		 *                 right
		 */
		public SelectionMotionAction(boolean moveStart, boolean moveLeft) {

			this.moveStart = moveStart;
			this.moveLeft = moveLeft;

		}

	}


	/**
	 * Sets a new sentence for analysis with the given text
	 *
	 * @param text The text to set
	 */
	private void setNewSentence(String text) {

		this.readingProcessor.setText(text);
		this.readingResult = this.readingProcessor.process();
		List<Token> tokens = this.readingResult.getTokens();
		
		this.tokenDetailTableModel.setTokens(tokens);
		this.analysisPanel.setReadingResult(this.readingResult, text);
		this.morphemeListModel.setTokens(new ArrayList<Token>());
		this.morphemeList.getSelectionModel().clearSelection();
		this.manualReadingTextField.setText("");
		
		if (tokens.size() > 0) {
			this.tokenDetailTable.getSelectionModel().setSelectionInterval(0, 0);
		}

	}


	/**
	 * Update the analysed tokens of the current sentence
	 * 
	 * @param newSelectionLength The new length of the selection 
	 */
	private void updateTokens(int newSelectionLength) {

		Selection selection = this.analysisPanel.getSelection();

		int selectionStartCharIndex = this.readingResult.getTokens().get(selection.startTokenIndex).getStart() + selection.startCharacterIndex;
		int selectionEndCharIndex = this.readingResult.getTokens().get(selection.startTokenIndex).getStart() + selection.startCharacterIndex + newSelectionLength - 1;
		this.readingResult = this.readingProcessor.process();
		List<Token> tokens = this.readingResult.getTokens();

		// Update token detail table
		this.tokenDetailTableModel.setTokens(tokens);

		// Adjust current selection
		int newStartTokenIndex = selection.startTokenIndex;
		while ((newStartTokenIndex >= tokens.size()) || (tokens.get(newStartTokenIndex).getStart() > selectionStartCharIndex)) {
			newStartTokenIndex--;
		}
		while (tokens.get(newStartTokenIndex).getStart() + tokens.get(newStartTokenIndex).getLength() <= selectionStartCharIndex) {
			newStartTokenIndex++;
		}

		int newEndTokenIndex = selection.startTokenIndex;
		while ((newEndTokenIndex >= tokens.size()) || (tokens.get(newEndTokenIndex).getStart() > selectionEndCharIndex)) {
			newEndTokenIndex--;
		}
		while (tokens.get(newEndTokenIndex).getStart() + tokens.get(newEndTokenIndex).getLength() <= selectionEndCharIndex) {
			newEndTokenIndex++;
		}

		selection.startTokenIndex = newStartTokenIndex;
		selection.startCharacterIndex = selectionStartCharIndex - tokens.get(newStartTokenIndex).getStart();
		selection.endTokenIndex = newEndTokenIndex;
		selection.endCharacterIndex = selectionEndCharIndex - tokens.get(newEndTokenIndex).getStart();

		this.selectionChanging = true;
		this.tokenDetailTable.getSelectionModel().setSelectionInterval(newStartTokenIndex, newEndTokenIndex);
		this.selectionChanging = false;

		this.analysisPanel.setReadingResultAndSelection(this.readingResult, selection);

	}


	/**
	 * Gets the full reading text at a given position (excluding any compound
	 * processing)
	 *
	 * @param position The position to look at
	 * @return The reading text, if present, or ""
	 */
	private String getFullReadingTextAt(int position) {

		Reading baseReading = ReadingProcessorDemo.this.readingResult.getBaseReadings().get(position);

		// If there is a base reading, the text is that reading
		if (baseReading != null) {
			return baseReading.text;
		}

		return "";

	}


	/**
	 * Calculates the length in characters of a selection
	 *
	 * @param selection The selection to examine
	 * @return The selection's length in characters
	 */
	private int getSelectionLength(Selection selection) {

		int length = 0;
		int tokenIndex = selection.startTokenIndex;
		int baseCharIndex = selection.startCharacterIndex;

		while (tokenIndex <= selection.endTokenIndex) {
			Token token = this.readingResult.getTokens().get(tokenIndex);
			int tokenLength = (tokenIndex == selection.endTokenIndex) ? selection.endCharacterIndex + 1 : token.getLength();
			length += (tokenLength - baseCharIndex);

			baseCharIndex = 0;
			tokenIndex++;
		}

		return length;

	}


	/**
	 * Create the user interface window
	 */
	public void createUI() {

		this.morphemeListModel = new MorphemeListModel();
		this.morphemeList = new JList(this.morphemeListModel);


		// Set look and feel

		try {
			UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}


		// Create window

		JFrame frame = new JFrame("Reading Processor Demo");
		frame.setSize(800, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.tokenDetailTableModel = new TokenTableModel();


		// Analysis panel

		this.analysisPanel = new AnalysisPanel();
		this.analysisPanel.setPreferredSize(new Dimension(0, 140));
		this.analysisPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
		this.analysisPanel.setFocusable(true);
		JScrollPane analysisScrollPane = new JScrollPane(this.analysisPanel);
		analysisScrollPane.setMinimumSize(new Dimension(0, 140));
		analysisScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		analysisScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		analysisScrollPane.setBorder(new TitledBorder("Analysed Sentence"));

		this.analysisPanel.addMouseListener(new MouseAdapter() {

			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				ReadingProcessorDemo.this.analysisPanel.requestFocusInWindow();
			}
			
		});

		this.analysisPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "previousTokenWithReading");
		this.analysisPanel.getActionMap().put("previousTokenWithReading", new PreviousTokenWithReadingAction());

		this.analysisPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "nextTokenWithReading");
		this.analysisPanel.getActionMap().put("nextTokenWithReading", new NextTokenWithReadingAction());


		this.analysisPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK), "selectionStartLeft");
		this.analysisPanel.getActionMap().put("selectionStartLeft", new SelectionMotionAction(true, true));

		this.analysisPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK), "selectionStartRight");
		this.analysisPanel.getActionMap().put("selectionStartRight", new SelectionMotionAction(true, false));


		this.analysisPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK), "selectionEndLeft");
		this.analysisPanel.getActionMap().put("selectionEndLeft", new SelectionMotionAction(false, true));

		this.analysisPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK), "selectionEndRight");
		this.analysisPanel.getActionMap().put("selectionEndRight", new SelectionMotionAction(false, false));


		this.analysisPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "previousReading");
		this.analysisPanel.getActionMap().put("previousReading", new PreviousReadingAction());

		this.analysisPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "nextReading");
		this.analysisPanel.getActionMap().put("nextReading", new NextReadingAction());


		this.analysisPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "customReading");
		this.analysisPanel.getActionMap().put("customReading", new CustomReadingAction());


		// Input panel

		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new GridBagLayout());
		GridBagConstraints inputConstraints = new GridBagConstraints();

		this.inputTextField = new JTextField();
		inputConstraints.fill = GridBagConstraints.HORIZONTAL;
		inputConstraints.insets = new Insets(4,2,2,2);
		inputConstraints.gridx = 0;
		inputConstraints.weightx = 1.0;
		inputPanel.add (this.inputTextField, inputConstraints);

		JButton analyseButton = new JButton("Analyse");
		inputConstraints.fill = GridBagConstraints.NONE;
		inputConstraints.insets = new Insets(4,2,2,2);
		inputConstraints.gridx = 2;
		inputConstraints.weightx = 0.0;
		inputPanel.add(analyseButton, inputConstraints);

		JButton clearButton = new JButton("Clear");
		inputConstraints.fill = GridBagConstraints.NONE;
		inputConstraints.insets = new Insets(4,2,2,2);
		inputConstraints.gridx = 3;
		inputConstraints.weightx = 0.0;
		inputPanel.add(clearButton, inputConstraints);

		ActionListener analyseListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setNewSentence(ReadingProcessorDemo.this.inputTextField.getText());				
			}

		};

		analyseButton.addActionListener(analyseListener);
		this.inputTextField.addActionListener(analyseListener);

		clearButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setNewSentence("");
				ReadingProcessorDemo.this.inputTextField.setText("");
			}
			
		});

		inputPanel.setBorder(new TitledBorder("Input"));


		// Token detail table

		this.tokenDetailTable = new JTable(this.tokenDetailTableModel) {

			private static final long serialVersionUID = 1L;

			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);
				if (!isCellSelected(row,column)) {
					c.setBackground(Color.WHITE);
				}
				return c;
			}
			
		};
		this.tokenDetailTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		JScrollPane tokenDetailScrollPane = new JScrollPane(this.tokenDetailTable);

		tokenDetailScrollPane.setBorder(new TitledBorder("Token details"));

		this.tokenDetailTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting() && !ReadingProcessorDemo.this.selectionChanging) {
					int selectionStart = ReadingProcessorDemo.this.tokenDetailTable.getSelectionModel().getMinSelectionIndex();
					int selectionEnd = ReadingProcessorDemo.this.tokenDetailTable.getSelectionModel().getMaxSelectionIndex();

					if (selectionStart != -1) {
						Selection selection = new Selection(selectionStart, 0, selectionEnd, ReadingProcessorDemo.this.readingResult.getTokens().get(selectionEnd).getLength() - 1);
						ReadingProcessorDemo.this.analysisPanel.setSelection(selection);
					}
				}
			}
			
		});

		this.analysisPanel.addSelectionListener(new SelectionListener() {

			public void selectionChanged(Selection selection) {
				if (selection != null) {

					ReadingProcessorDemo.this.selectionChanging = true;
					JTable tokenDetailTable = ReadingProcessorDemo.this.tokenDetailTable;
					tokenDetailTable.setRowSelectionInterval(selection.startTokenIndex, selection.endTokenIndex);
					tokenDetailTable.scrollRectToVisible(tokenDetailTable.getCellRect(selection.startTokenIndex, 0, true));

					Token startToken = ReadingProcessorDemo.this.readingResult.getTokens().get(selection.startTokenIndex);
					List<Token> possibleTokens = ReadingProcessorDemo.this.readingResult.getPossibleTokens(
							startToken.getStart() + selection.startCharacterIndex
					);

					int selectionStart = startToken.getStart() + selection.startCharacterIndex;
					String readingText = getFullReadingTextAt(selectionStart); 
					ReadingProcessorDemo.this.manualReadingTextField.setText(readingText);

					int selectionLength = getSelectionLength(selection);
					ReadingProcessorDemo.this.morphemeListModel.setTokens(possibleTokens);
					int listIndex = ReadingProcessorDemo.this.morphemeListModel.getIndexByReadingAndLength(readingText, selectionLength);
					if (listIndex != -1) {
						ReadingProcessorDemo.this.morphemeList.getSelectionModel().setSelectionInterval(listIndex, listIndex);
					} else {
						ReadingProcessorDemo.this.morphemeList.getSelectionModel().clearSelection();
					}

					ReadingProcessorDemo.this.selectionChanging = false;
				}
			}
			
		});


		// Morpheme selection panel

		this.morphemeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel morphemeSelectionPanel = new JPanel();
		morphemeSelectionPanel.setBorder(new TitledBorder("Morpheme Selection"));
		morphemeSelectionPanel.setLayout(new GridBagLayout());

		this.morphemeList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				if (!e.getValueIsAdjusting()) {

					int selectedReading = ReadingProcessorDemo.this.morphemeList.getSelectedIndex();
					if (selectedReading != -1) {
						Selection selection = ReadingProcessorDemo.this.analysisPanel.getSelection();
						Token startToken = ReadingProcessorDemo.this.readingResult.getTokens().get(selection.startTokenIndex);
						int selectionStart = startToken.getStart() + selection.startCharacterIndex;
						String readingText = TextUtil.invertKanaCase(ReadingProcessorDemo.this.morphemeListModel.getReadingAt(selectedReading));
						int surfaceLength = ReadingProcessorDemo.this.morphemeListModel.getSurfaceLengthAt(selectedReading);
	
						ReadingProcessorDemo.this.readingProcessor.setReadingConstraint(new Reading(selectionStart, surfaceLength, readingText));
						updateTokens(surfaceLength);

						ReadingProcessorDemo.this.manualReadingTextField.setText(readingText);

					}

				}
				
			}

		});

		GridBagConstraints morphemeConstraints = new GridBagConstraints();

		ActionListener manualReadingActionListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				Selection selection = ReadingProcessorDemo.this.analysisPanel.getSelection();
				Token startToken = ReadingProcessorDemo.this.readingResult.getTokens().get(selection.startTokenIndex);
				Token endToken = ReadingProcessorDemo.this.readingResult.getTokens().get(selection.endTokenIndex);
				int selectionStart = startToken.getStart() + selection.startCharacterIndex;
				int selectionEnd = endToken.getStart() + selection.endCharacterIndex;
				String text = ReadingProcessorDemo.this.manualReadingTextField.getText();
				int surfaceLength = selectionEnd - selectionStart + 1;

				ReadingProcessorDemo.this.readingProcessor.setReadingConstraint(new Reading(selectionStart, surfaceLength, text));
				updateTokens(surfaceLength);

				ReadingProcessorDemo.this.analysisPanel.requestFocusInWindow();

			}
			
		};

		this.manualReadingTextField = new JTextField();
		this.manualReadingTextField.addActionListener(manualReadingActionListener);

		morphemeConstraints.gridx = 0;
		morphemeConstraints.gridy = 0;
		morphemeConstraints.weightx = 1.0;
		morphemeConstraints.weighty = 0.0;
		morphemeConstraints.fill = GridBagConstraints.BOTH;
		morphemeSelectionPanel.add(this.manualReadingTextField, morphemeConstraints);

		JButton manualReadingApplyButton = new JButton("Apply");
		manualReadingApplyButton.addActionListener(manualReadingActionListener);

		morphemeConstraints.gridx = 1;
		morphemeConstraints.gridy = 0;
		morphemeConstraints.weightx = 0.0;
		morphemeConstraints.weighty = 0.0;
		morphemeConstraints.fill = GridBagConstraints.BOTH;
		morphemeSelectionPanel.add(manualReadingApplyButton, morphemeConstraints);

		JScrollPane morphemeScrollPane = new JScrollPane(this.morphemeList);
		morphemeScrollPane.setMinimumSize(new Dimension(0, 100));

		morphemeConstraints.gridx = 0;
		morphemeConstraints.gridy = 1;
		morphemeConstraints.gridwidth = 2;
		morphemeConstraints.weightx = 1.0;
		morphemeConstraints.weighty = 1.0;
		morphemeConstraints.fill = GridBagConstraints.BOTH;
		morphemeSelectionPanel.add(morphemeScrollPane, morphemeConstraints);


		// Assemble window

		GridBagConstraints windowConstraints = new GridBagConstraints();
		frame.setLayout(new GridBagLayout());

		windowConstraints.gridy = 0;
		windowConstraints.weightx = 1.0;
		windowConstraints.weighty = 0.0;
		windowConstraints.fill = GridBagConstraints.HORIZONTAL;
		frame.add(inputPanel, windowConstraints);
		
		windowConstraints.gridy = 1;
		windowConstraints.weightx = 1.0;
		windowConstraints.weighty = 0.0;
		windowConstraints.fill = GridBagConstraints.HORIZONTAL;
		frame.add(analysisScrollPane, windowConstraints);

		windowConstraints.gridy = 2;
		windowConstraints.weightx = 1.0;
		windowConstraints.weighty = 0.0;
		windowConstraints.fill = GridBagConstraints.HORIZONTAL;
		frame.add(morphemeSelectionPanel, windowConstraints);

		windowConstraints.gridy = 3;
		windowConstraints.weightx = 1.0;
		windowConstraints.weighty = 1.0;
		windowConstraints.fill = GridBagConstraints.BOTH;
		frame.add(tokenDetailScrollPane, windowConstraints);


		// Show window

		frame.setVisible(true);
		this.analysisPanel.requestFocusInWindow();


		// Set initial content

		String initialString = "「情報スーパーハイウェイ」の真のインパクトは、情報インフラの構築により経済が従来のハードやモノづくり中心の実体経済から知識、情報、ソフトを主体とした経済に移行し、そこから生まれる新しい産業や経済活動にある。";
		setNewSentence(initialString);
		this.inputTextField.setText(initialString);

	}



	/**
	 * Constructor
	 * 
	 * @param configFilename The dictionary configuration filename to use 
	 */
	public ReadingProcessorDemo(String configFilename) {

		this.readingProcessor = SenFactory.getReadingProcessor(configFilename);
		this.readingProcessor.addFilter(0, new NumberFilter());

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {

				createUI();

			}
			
		});

	}


	/**
	 * Main method
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println ("Syntax: java ReadingProcessorDemo <config file>");
		}
		new ReadingProcessorDemo(args[0]);

	}

}
