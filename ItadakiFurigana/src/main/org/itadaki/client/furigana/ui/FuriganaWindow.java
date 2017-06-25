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

package org.itadaki.client.furigana.ui;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.itadaki.client.furigana.SentenceListener;
import org.itadaki.client.furigana.SentenceProvider;

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
public class FuriganaWindow extends JFrame implements SentenceListener, WindowListener {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * ReadingProcessor used to analyse text
	 */
	private ReadingProcessor readingProcessor;

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
	 * List model used to display possible morpheme readings
	 */
	private MorphemeListModel morphemeListModel;

	/**
	 * The current SentenceProvider used to supply sentences
	 */
	private SentenceProvider sentenceProvider;

	/**
	 * Button to apply a manually entered reading to the current selection
	 */
	private JButton manualReadingApplyButton;

	/**
	 * Action to move to the previous sentence
	 */
	private AbstractAction previousSentenceAction;

	/**
	 * Action to move to the next sentence
	 */
	private AbstractAction nextSentenceAction;


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
		
			Selection selection = FuriganaWindow.this.analysisPanel.getSelection();
			List<Token> tokens = FuriganaWindow.this.readingResult.getTokens();
			BitSet visibleTokens = FuriganaWindow.this.readingResult.getVisibleTokens();
		
			if (selection != null) {
				for (int i = selection.startTokenIndex - 1; i >= 0; i--) {
					if (visibleTokens.get(i)) {
						Selection newSelection = new Selection(i, 0, i, tokens.get(i).getLength() - 1);
						FuriganaWindow.this.analysisPanel.setSelection(newSelection);
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
		
			Selection selection = FuriganaWindow.this.analysisPanel.getSelection();
			List<Token> tokens = FuriganaWindow.this.readingResult.getTokens();
			BitSet visibleTokens = FuriganaWindow.this.readingResult.getVisibleTokens();
		
			if (selection != null) {
				for (int i = selection.startTokenIndex + 1; i < tokens.size(); i++) {
					if (visibleTokens.get(i)) {
						Selection newSelection = new Selection(i, 0, i, tokens.get(i).getLength() - 1);
						FuriganaWindow.this.analysisPanel.setSelection(newSelection);
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

			int selectedIndex = FuriganaWindow.this.morphemeList.getSelectedIndex();
			int nextIndex = Math.max(0, selectedIndex - 1);

			if (selectedIndex != nextIndex) {
				FuriganaWindow.this.morphemeList.setSelectedIndex(nextIndex);
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

			int selectedIndex = FuriganaWindow.this.morphemeList.getSelectedIndex();
			int nextIndex = Math.min(FuriganaWindow.this.morphemeListModel.getSize() - 1, selectedIndex + 1);

			if (selectedIndex != nextIndex) {
				FuriganaWindow.this.morphemeList.setSelectedIndex(nextIndex);
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

			FuriganaWindow.this.manualReadingTextField.requestFocusInWindow();
			FuriganaWindow.this.manualReadingTextField.selectAll();

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

			Selection selection = FuriganaWindow.this.analysisPanel.getSelection();
			List<Token> tokens = FuriganaWindow.this.readingResult.getTokens();

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

			FuriganaWindow.this.analysisPanel.setSelection(selection);

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
	 * An Action that moves to the previous sentence
	 */
	private final class PreviousSentenceAction extends AbstractAction {

		/**
		 * Serial version UID
		 */
		private static final long serialVersionUID = 1L;


		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
		
			SentenceProvider sentenceProvider = FuriganaWindow.this.sentenceProvider;
			List<Reading> readings = new ArrayList<Reading>(FuriganaWindow.this.readingResult.getDisplayReadings().values());
			sentenceProvider.setReadings(readings);
			sentenceProvider.previous();
			updateNavigationButtons();
			setNewSentence(sentenceProvider.getText(), sentenceProvider.getReadings());
			FuriganaWindow.this.analysisPanel.requestFocusInWindow();
		
		}

		/**
		 * @param name The Action's name
		 */
		public PreviousSentenceAction(String name) {
			super(name);
		}

	}


	/**
	 * An Action that moves to the next sentence
	 */
	private final class NextSentenceAction extends AbstractAction {

		/**
		 * Serial version UID
		 */
		private static final long serialVersionUID = -1L;


		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
		
			SentenceProvider sentenceProvider = FuriganaWindow.this.sentenceProvider;
			List<Reading> readings = new ArrayList<Reading>(FuriganaWindow.this.readingResult.getDisplayReadings().values());
			sentenceProvider.setReadings(readings);
			sentenceProvider.next();
			updateNavigationButtons();
			setNewSentence(sentenceProvider.getText(), sentenceProvider.getReadings());
			FuriganaWindow.this.analysisPanel.requestFocusInWindow();
		
		}

		/**
		 * @param name The Action's name
		 */
		public NextSentenceAction(String name) {
			super(name);
		}

	}


	/**
	 * An Action that applies the currently selected readings, if any, and
	 * closes the window
	 */
	private final class FinishAction extends AbstractAction {

		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 1L;


		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed (ActionEvent e) {
		
			SentenceProvider sentenceProvider = FuriganaWindow.this.sentenceProvider;
			List<Reading> readings = new ArrayList<Reading> (FuriganaWindow.this.readingResult.getDisplayReadings().values());
			sentenceProvider.setReadings (readings);
			FuriganaWindow.this.setVisible (false);

			// Inform the sentence provider it's not needed any more
			sentenceProvider.dispose();

		}


		/**
		 * @param name The action's name
		 */
		private FinishAction(String name) {
			super(name);
		}

	}


	/**
	 * An Action that closes the window without applying readings
	 */
	private final class CancelAction extends AbstractAction {

		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 1L;


		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
		
			FuriganaWindow.this.setVisible(false);
			
		}


		/**
		 * @param name The action's name
		 */
		private CancelAction(String name) {
			super(name);
		}

	}


	/**
	 * Sets a new sentence for analysis with the given text
	 *
	 * @param text The text to set
	 * @param readings 
	 */
	private void setNewSentence(String text, List<Reading> readings) {

		this.readingProcessor.setText(text);
		for (Reading reading : readings) {
			this.readingProcessor.setReadingConstraint(reading);
		}
		this.readingResult = this.readingProcessor.process();
		
		this.morphemeListModel.setTokens(new ArrayList<Token>());
		this.morphemeList.getSelectionModel().clearSelection();
		this.manualReadingTextField.setText("");

		this.analysisPanel.scrollRectToVisible(new Rectangle(0,0));
		this.analysisPanel.setReadingResult(this.readingResult, text);
		this.analysisPanel.requestFocusInWindow();

	}


	/**
	 * Sets a new SentenceProvider for analysis
	 *
	 * @param sentenceProvider The SentenceProvider to set
	 */
	public void setSentenceProvider(SentenceProvider sentenceProvider) {

		this.sentenceProvider = sentenceProvider;
		this.sentenceProvider.setSentenceListener(this);

		updateNavigationButtons();

		setNewSentence(sentenceProvider.getText(), sentenceProvider.getReadings());

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

		this.analysisPanel.updateReadingResultAndSelection(this.readingResult, selection);

	}


	/**
	 * Gets the full reading text at a given position (excluding any compound
	 * processing)
	 *
	 * @param position The position to look at
	 * @return The reading text, if present, or ""
	 */
	private String getFullReadingTextAt(int position) {

		Reading baseReading = FuriganaWindow.this.readingResult.getBaseReadings().get(position);

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
	 * Update navigation buttons to account for changes in the SentenceProvider
	 */
	public void updateNavigationButtons() {

		this.previousSentenceAction.setEnabled(this.sentenceProvider.hasPrevious());
		this.nextSentenceAction.setEnabled(this.sentenceProvider.hasNext());

	}


	/**
	 * Create the user interface window
	 */
	public void createUI() {

		this.morphemeListModel = new MorphemeListModel();
		this.morphemeList = new JList(this.morphemeListModel);


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
				FuriganaWindow.this.analysisPanel.requestFocusInWindow();
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

		this.analysisPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_MASK), "previousSentence");
		// Action defined below

		this.analysisPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_MASK), "nextSentence");
		// Action defined below
		
		this.analysisPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK),
				"finish"
		);
		// Action defined below

		this.analysisPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		// Action defined below

		this.analysisPanel.addSelectionListener(new SelectionListener() {

			public void selectionChanged(Selection selection) {

				if (selection != null) {

					Token startToken = FuriganaWindow.this.readingResult.getTokens().get(selection.startTokenIndex);
					List<Token> possibleTokens = FuriganaWindow.this.readingResult.getPossibleTokens(
							startToken.getStart() + selection.startCharacterIndex
					);

					int selectionStart = startToken.getStart() + selection.startCharacterIndex;
					String readingText = getFullReadingTextAt(selectionStart); 
					FuriganaWindow.this.manualReadingTextField.setText(readingText);

					int selectionLength = getSelectionLength(selection);
					FuriganaWindow.this.morphemeListModel.setTokens(possibleTokens);
					int listIndex = FuriganaWindow.this.morphemeListModel.getIndexByReadingAndLength(readingText, selectionLength);
					if (listIndex != -1) {
						FuriganaWindow.this.morphemeList.getSelectionModel().setSelectionInterval(listIndex, listIndex);
					} else {
						FuriganaWindow.this.morphemeList.getSelectionModel().clearSelection();
					}

					FuriganaWindow.this.manualReadingTextField.setEnabled(true);
					FuriganaWindow.this.manualReadingApplyButton.setEnabled(true);

				} else {

					FuriganaWindow.this.manualReadingTextField.setText("");
					FuriganaWindow.this.morphemeListModel.setTokens(new ArrayList<Token>());
					FuriganaWindow.this.manualReadingTextField.setEnabled(false);
					FuriganaWindow.this.manualReadingApplyButton.setEnabled(false);

				}

			}
			
		});


		// Morpheme selection panel

		this.morphemeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel morphemeSelectionPanel = new JPanel();
		morphemeSelectionPanel.setBorder(new TitledBorder("Readings"));
		morphemeSelectionPanel.setLayout(new GridBagLayout());

		this.morphemeList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				if (!e.getValueIsAdjusting()) {

					int selectedReading = FuriganaWindow.this.morphemeList.getSelectedIndex();
					if (selectedReading != -1) {
						Selection selection = FuriganaWindow.this.analysisPanel.getSelection();
						Token startToken = FuriganaWindow.this.readingResult.getTokens().get(selection.startTokenIndex);
						int selectionStart = startToken.getStart() + selection.startCharacterIndex;
						String readingText = TextUtil.invertKanaCase(FuriganaWindow.this.morphemeListModel.getReadingAt(selectedReading));
						int surfaceLength = FuriganaWindow.this.morphemeListModel.getSurfaceLengthAt(selectedReading);
	
						FuriganaWindow.this.readingProcessor.setReadingConstraint(new Reading(selectionStart, surfaceLength, readingText));
						updateTokens(surfaceLength);

						FuriganaWindow.this.manualReadingTextField.setText(readingText);

					}

				}
				
			}

		});

		GridBagConstraints morphemeConstraints = new GridBagConstraints();

		ActionListener manualReadingActionListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				Selection selection = FuriganaWindow.this.analysisPanel.getSelection();

				if (selection != null) {
					Token startToken = FuriganaWindow.this.readingResult.getTokens().get(selection.startTokenIndex);
					Token endToken = FuriganaWindow.this.readingResult.getTokens().get(selection.endTokenIndex);
					int selectionStart = startToken.getStart() + selection.startCharacterIndex;
					int selectionEnd = endToken.getStart() + selection.endCharacterIndex;
					String text = FuriganaWindow.this.manualReadingTextField.getText();
					int surfaceLength = selectionEnd - selectionStart + 1;
	
					FuriganaWindow.this.readingProcessor.setReadingConstraint(new Reading(selectionStart, surfaceLength, text));
					updateTokens(surfaceLength);
	
					FuriganaWindow.this.analysisPanel.requestFocusInWindow();
				}

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

		this.manualReadingApplyButton = new JButton("Apply");
		this.manualReadingApplyButton.setMnemonic (KeyEvent.VK_A);
		this.manualReadingApplyButton.addActionListener(manualReadingActionListener);
		FuriganaWindow.this.manualReadingApplyButton.setEnabled(false);

		morphemeConstraints.gridx = 1;
		morphemeConstraints.gridy = 0;
		morphemeConstraints.weightx = 0.0;
		morphemeConstraints.weighty = 0.0;
		morphemeConstraints.fill = GridBagConstraints.BOTH;
		morphemeSelectionPanel.add(this.manualReadingApplyButton, morphemeConstraints);

		JScrollPane morphemeScrollPane = new JScrollPane(this.morphemeList);
		morphemeScrollPane.setMinimumSize(new Dimension(0, 100));

		morphemeConstraints.gridx = 0;
		morphemeConstraints.gridy = 1;
		morphemeConstraints.gridwidth = 2;
		morphemeConstraints.weightx = 1.0;
		morphemeConstraints.weighty = 1.0;
		morphemeConstraints.fill = GridBagConstraints.BOTH;
		morphemeSelectionPanel.add(morphemeScrollPane, morphemeConstraints);


		// Separator

		JSeparator separator = new JSeparator();


		// Navigation panel

		JPanel innerNavigationPanel = new JPanel();
		GridBagConstraints navigationConstraints = new GridBagConstraints();
		innerNavigationPanel.setLayout(new GridBagLayout());

		this.previousSentenceAction = new PreviousSentenceAction("< Previous");
		this.nextSentenceAction = new NextSentenceAction("Next >");
		Action finishAction = new FinishAction("Finish");
		Action cancelAction = new CancelAction("Cancel");

		this.previousSentenceAction.setEnabled(false);
		this.nextSentenceAction.setEnabled(false);

		JButton previousButton = new JButton (this.previousSentenceAction);
		JButton nextButton = new JButton (this.nextSentenceAction);
		JButton finishButton = new JButton (finishAction);
		JButton cancelButton = new JButton (cancelAction);

		previousButton.setMnemonic (KeyEvent.VK_P);
		nextButton.setMnemonic (KeyEvent.VK_N);
		finishButton.setMnemonic(KeyEvent.VK_F);
		cancelButton.setMnemonic(KeyEvent.VK_C);

		this.analysisPanel.getActionMap().put("previousSentence", this.previousSentenceAction);
		this.analysisPanel.getActionMap().put("nextSentence", this.nextSentenceAction);
		this.analysisPanel.getActionMap().put("finish", finishAction);
		this.analysisPanel.getActionMap().put("cancel", cancelAction);


		navigationConstraints.gridx = 0;
		navigationConstraints.weightx = 1.0;
		navigationConstraints.weighty = 1.0;
		navigationConstraints.insets = new Insets(0, 0, 0, 0);
		navigationConstraints.fill = GridBagConstraints.HORIZONTAL;
		innerNavigationPanel.add(previousButton, navigationConstraints);

		navigationConstraints.gridx = 1;
		navigationConstraints.weightx = 1.0;
		navigationConstraints.weighty = 1.0;
		navigationConstraints.insets = new Insets(0, 5, 0, 0);
		navigationConstraints.fill = GridBagConstraints.HORIZONTAL;
		innerNavigationPanel.add(nextButton, navigationConstraints);

		navigationConstraints.gridx = 2;
		navigationConstraints.weightx = 1.0;
		navigationConstraints.weighty = 1.0;
		navigationConstraints.insets = new Insets(0, 15, 0, 0);
		navigationConstraints.fill = GridBagConstraints.HORIZONTAL;
		innerNavigationPanel.add(finishButton, navigationConstraints);

		navigationConstraints.gridx = 3;
		navigationConstraints.weightx = 1.0;
		navigationConstraints.weighty = 1.0;
		navigationConstraints.insets = new Insets(0, 10, 0, 0);
		navigationConstraints.fill = GridBagConstraints.HORIZONTAL;
		innerNavigationPanel.add(cancelButton, navigationConstraints);


		JPanel navigationPanel = new JPanel();
		navigationPanel.setLayout(new GridBagLayout());

		navigationConstraints.gridx = 0;
		navigationConstraints.weightx = 0.0;
		navigationConstraints.weighty = 1.0;
		navigationConstraints.insets = new Insets(0, 10, 0, 10);
		navigationConstraints.anchor = GridBagConstraints.EAST;
		navigationConstraints.fill = GridBagConstraints.NONE;
		navigationPanel.add(innerNavigationPanel, navigationConstraints);


		// Assemble window

		GridBagConstraints windowConstraints = new GridBagConstraints();
		setLayout(new GridBagLayout());

		windowConstraints.gridy = 0;
		windowConstraints.weightx = 1.0;
		windowConstraints.weighty = 0.0;
		windowConstraints.insets = new Insets(0, 0, 0, 0);
		windowConstraints.fill = GridBagConstraints.HORIZONTAL;
		add(analysisScrollPane, windowConstraints);

		windowConstraints.gridy = 1;
		windowConstraints.weightx = 1.0;
		windowConstraints.weighty = 1.0;
		windowConstraints.insets = new Insets(0, 0, 0, 0);
		windowConstraints.fill = GridBagConstraints.BOTH;
		add(morphemeSelectionPanel, windowConstraints);

		windowConstraints.gridy = 2;
		windowConstraints.weightx = 1.0;
		windowConstraints.weighty = 0.0;
		windowConstraints.insets = new Insets(10, 0, 10, 0);
		windowConstraints.fill = GridBagConstraints.HORIZONTAL;
		add(separator, windowConstraints);

		windowConstraints.gridy = 3;
		windowConstraints.weightx = 1.0;
		windowConstraints.weighty = 0.0;
		windowConstraints.insets = new Insets(10, 0, 10, 0);
		windowConstraints.anchor = GridBagConstraints.EAST;
		windowConstraints.fill = GridBagConstraints.NONE;
		add(navigationPanel, windowConstraints);


		// Set window listener
		addWindowListener (this);


		// Show window
		setSize (750, 400);
		setVisible (true);
		this.analysisPanel.requestFocusInWindow();


	}


	/* SentenceListener interface */

	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceListener#documentClosed()
	 */
	public void documentClosed() {
		
		this.setVisible (false);
		
	}


	/**
	 * Constructor
	 * 
	 * @param configFilename The dictionary configuration filename to use 
	 */
	public FuriganaWindow (String configFilename) {

		super ("Add Furigana");

		this.readingProcessor = SenFactory.getReadingProcessor (configFilename);
		this.readingProcessor.addFilter (0, new NumberFilter());

		createUI();

	}


	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent e) {
		// Do nothing
	}


	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent e) {
		// Do nothing
	}


	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent e) {

		// Inform the sentence provider it's not needed any more
		this.sentenceProvider.dispose();
		
	}


	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	public void windowDeactivated(WindowEvent e) {
		// Do nothing
	}


	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	public void windowDeiconified(WindowEvent e) {
		// Do nothing
	}


	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent e) {
		// Do nothing
	}


	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent e) {
		// Do nothing
	}


}
