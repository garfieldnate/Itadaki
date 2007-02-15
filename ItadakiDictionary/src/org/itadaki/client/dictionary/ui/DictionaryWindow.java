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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.itadaki.client.dictionary.settings.Settings;
import org.itadaki.client.dictionary.settings.SettingsListener;
import org.itadaki.client.dictionary.ui.dictionary.DictionaryPopupFactory;
import org.itadaki.client.dictionary.ui.dictionary.HyperlinkLabel;
import org.itadaki.client.dictionary.ui.dictionary.InformationalDocument;
import org.itadaki.client.dictionary.ui.dictionary.ResultCountLabel;
import org.itadaki.client.dictionary.ui.dictionary.ResultMarshaller;
import org.itadaki.client.dictionary.ui.dictionary.ResultSectionAction;
import org.itadaki.client.dictionary.ui.dictionary.SearchResetAction;
import org.itadaki.client.dictionary.ui.dictionary.SearchStartAction;
import org.itadaki.fasttextpane.Document;
import org.itadaki.fasttextpane.FastTextPane;
import org.itadaki.fasttextpane.FastTextView;
import org.itadaki.seashell.AsynchronousSearcher;
import org.itadaki.seashell.DictionaryException;
import org.itadaki.seashell.DictionaryManager;
import org.itadaki.seashell.SearchMode;
import org.itadaki.seashell.edict.EdictDictionary;


/**
 * Dictionary search window
 */
public class DictionaryWindow extends JFrame implements FocusListener, WindowListener, SettingsListener {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Shared DictionaryManager instance
	 */
	private static DictionaryManager dictionaryManager;

	/**
	 * Popup menu factory to create customised popups for different areas of the window
	 */
	private DictionaryPopupFactory popupFactory;

	/**
	 * Result marshaller. Connects AsynchronousSearcher with ResultPane
	 */
	private ResultMarshaller resultMarshaller;

	/**
	 * The FastTextView to hold search results and informational messages
	 */
	private FastTextView fastTextView;
	
	/**
	 * ResultEntry field for search queries
	 */
	private JTextField searchTextField;
	
	/**
	 * Button to start search
	 */
	private JButton searchButton;

	/**
	 * Button to reset and clear window
	 */
	private JButton resetButton;

	/**
	 * Link label to Exact results
	 */
	private HyperlinkLabel exactLabel;

	/**
	 * Link label to Start results
	 */
	private HyperlinkLabel startLabel;

	/**
	 * Link label to End results
	 */
	private HyperlinkLabel endLabel;

	/**
	 * Link label to Middle results
	 */
	private HyperlinkLabel otherLabel;

	/**
	 * Dictionary searcher thread
	 */
	private AsynchronousSearcher searcher;

	/**
	 * The currently displayed search query
	 */
	private String currentSearchKey = "";


	/**
	 * Retrieve the DictionaryManager singleton
	 *
	 * @return The DictionaryManager instance
	 */
	public static synchronized DictionaryManager getDictionaryManager() {

		if (dictionaryManager == null) {
			dictionaryManager = new DictionaryManager();
		}
		return dictionaryManager;

	}


	/**
	 * Perform and output a search
	 * Thread safe by conditional use of SwingUtilities.invokeLater if not already in event dispatch thread
	 *
	 * @param searchKey The search key
	 */
	public void search (String searchKey) {

		final String finalSearchKey = searchKey.trim();

		Runnable startSearchRunnable = new Runnable() {

			public void run() {

				if (!DictionaryWindow.this.searchTextField.getText().equals (finalSearchKey)) {
					DictionaryWindow.this.searchTextField.setText(finalSearchKey);
				}

				DictionaryWindow.this.currentSearchKey = finalSearchKey;
				
				if (! (DictionaryWindow.this.fastTextView.getDocument() instanceof InformationalDocument)) {
					DictionaryWindow.this.searcher.search (finalSearchKey);
				}	

			}

		};		

		if (SwingUtilities.isEventDispatchThread()) {
			startSearchRunnable.run();
		} else {
			SwingUtilities.invokeLater (startSearchRunnable);
		}

	}


	/**
	 * Determines if the window is in a reset state. The window is classed as
	 * reset if the search text field is empty and there are no current results. 
	 * NOT thread safe
	 * 
	 * @return <code>true</code> if the window is reset, <code>false</code>
	 *         otherwise
	 */
	public boolean isReset() {

		Document document = this.fastTextView.getDocument();

		if (
				this.searchTextField.getText().equals("")
				&& (
						   (document instanceof InformationalDocument)
						|| (document.getSize() == 0)
				   )
		   )
		{
			return true;
		}

		return false;

	}


	/**
	 * Discard results and reset the dictionary window,
	 * Thread safe by conditional use of SwingUtilities.invokeLater if not already in event dispatch thread
	 */
	public void resetSearch() {

		Runnable startSearchRunnable = new Runnable() {

			public void run() {

				DictionaryWindow.this.searchTextField.setText ("");
				DictionaryWindow.this.searcher.abortSearch();
				DictionaryWindow.this.currentSearchKey = "";

				DictionaryWindow.this.searchTextField.requestFocusInWindow();

			}

		};		

		if (SwingUtilities.isEventDispatchThread()) {
			startSearchRunnable.run();
		} else {
			SwingUtilities.invokeLater (startSearchRunnable);
		}

	}


	/**
	 * Starts a search based on the current contents of the search entry
	 * Thread safe by conditional use of SwingUtilities.invokeLater if not already in event dispatch thread
	 */
	public void startSearch() {

		Runnable startSearchRunnable = new Runnable() {

			public void run() {
				search (DictionaryWindow.this.searchTextField.getText());
			}

		};		

		if (SwingUtilities.isEventDispatchThread()) {
			startSearchRunnable.run();
		} else {
			SwingUtilities.invokeLater (startSearchRunnable);
		}

	}


	/**
	 * Reloads the currently displayed search if any.
	 * Thread safe by conditional use of SwingUtilities.invokeLater if not already in event dispatch thread
	 */
	public void reloadSearch() {

		Runnable startSearchRunnable = new Runnable() {

			public void run() {
				search (DictionaryWindow.this.currentSearchKey);
			}

		};		

		if (SwingUtilities.isEventDispatchThread()) {
			startSearchRunnable.run();
		} else {
			SwingUtilities.invokeLater (startSearchRunnable);
		}

	}


	/**
	 * Create the search entry panel
	 *
	 * @return The search entry panel
	 */
	private JPanel createSearchEntryPanel () {

		JPanel northPanel = new JPanel();
		northPanel.setLayout (new GridBagLayout());
		northPanel.setComponentPopupMenu (this.popupFactory.genericMenu());
		GridBagConstraints constraints = new GridBagConstraints();

		// Swing bug workaround to enable popup menu on blank panel areas
		northPanel.addMouseListener (new MouseAdapter() { });

		this.searchTextField = new JTextField();
		this.searchTextField.setComponentPopupMenu (this.popupFactory.menuForTextComponent (this.searchTextField));

		this.searchButton = new JButton ("Search");
		this.searchButton.setInheritsPopupMenu (true);
		this.searchButton.setFocusable (false);

		ImageIcon resetIcon = new ImageIcon (ClassLoader.getSystemResource("reset-20.png"), "Reset");
		this.resetButton = new JButton (resetIcon);
		this.resetButton.setMargin (new Insets (0,0,0,0));
		this.resetButton.setInheritsPopupMenu (true);
		this.resetButton.setFocusable (false);
		

		constraints.fill = GridBagConstraints.BOTH;

		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets (10, 6, 0, 0);
		northPanel.add (this.searchTextField, constraints);

		constraints.weightx = 0;
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.insets = new Insets (10, 10, 0, 0);
		northPanel.add (this.searchButton, constraints);

		constraints.weightx = 0;
		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.insets = new Insets (10, 10, 0, 6);
		northPanel.add (this.resetButton, constraints);

		return northPanel;

	}


	/**
	 * Create the search result panel
	 *
	 * @return The search result panel
	 */
	private JPanel createSearchResultPanel() {

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout (new GridBagLayout());
		centerPanel.setComponentPopupMenu (this.popupFactory.genericMenu());
		GridBagConstraints constraints = new GridBagConstraints();

		// Swing bug workaround to enable popup menu on blank panel areas
		centerPanel.addMouseListener (new MouseAdapter() { });

		this.fastTextView = new FastTextView();
		this.fastTextView.setComponentPopupMenu (this.popupFactory.menuForResultPane (this.fastTextView));
		this.resultMarshaller = new ResultMarshaller (this, this.fastTextView, this.searcher);
		FastTextPane fastTextPane = new FastTextPane (this.fastTextView);

		JLabel resultsCountLabel = new ResultCountLabel (this.resultMarshaller);
		resultsCountLabel.setInheritsPopupMenu (true);


		this.exactLabel = new HyperlinkLabel();
		this.startLabel = new HyperlinkLabel();
		this.endLabel = new HyperlinkLabel();
		this.otherLabel = new HyperlinkLabel();

		this.exactLabel.setInheritsPopupMenu (true);
		this.startLabel.setInheritsPopupMenu (true);
		this.endLabel.setInheritsPopupMenu (true);
		this.otherLabel.setInheritsPopupMenu (true);

		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets (12, 7, 0, 0);
		centerPanel.add (resultsCountLabel, constraints);


		FlowLayout flowLayout = new FlowLayout (FlowLayout.LEFT);
		flowLayout.setHgap (0);
		flowLayout.setVgap (0);
		JPanel countsPanel = new JPanel (flowLayout);
		countsPanel.setInheritsPopupMenu (true);

		countsPanel.add (this.exactLabel);
		countsPanel.add (new JLabel ("  |  "));
		countsPanel.add (this.startLabel);
		countsPanel.add (new JLabel ("  |  "));
		countsPanel.add (this.endLabel);
		countsPanel.add (new JLabel ("  |  "));
		countsPanel.add (this.otherLabel);

		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets (4, 7, 0, 6);
		centerPanel.add (countsPanel, constraints);

		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;

		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets (3, 6, 10, 6);

		centerPanel.add (fastTextPane, constraints);

		return centerPanel;
	}


	/**
	 * Install keyboard mappings and other actions
	 */
	private void installActions () {

		Action searchStartAction = new SearchStartAction (this);
		Action searchResetAction = new SearchResetAction (this);
		Action jumpExactAction = new ResultSectionAction (this.resultMarshaller, this.fastTextView, SearchMode.EXACT, "Exact");
		Action jumpStartAction = new ResultSectionAction (this.resultMarshaller, this.fastTextView, SearchMode.START, "Start");
		Action jumpEndAction = new ResultSectionAction (this.resultMarshaller, this.fastTextView, SearchMode.END, "End");
		Action jumpMiddleAction = new ResultSectionAction (this.resultMarshaller, this.fastTextView, SearchMode.MIDDLE, "Other");

		this.exactLabel.setAction (jumpExactAction);
		this.startLabel.setAction (jumpStartAction);
		this.endLabel.setAction (jumpEndAction);
		this.otherLabel.setAction (jumpMiddleAction);

		InputMap inputMap = this.searchTextField.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

		inputMap.put (KeyStroke.getKeyStroke("alt 1"), "jump-exact");
		inputMap.put (KeyStroke.getKeyStroke("alt 2"), "jump-start");
		inputMap.put (KeyStroke.getKeyStroke("alt 3"), "jump-end");
		inputMap.put (KeyStroke.getKeyStroke("alt 4"), "jump-middle");
		inputMap.put (KeyStroke.getKeyStroke("UP"), "results-up");
		inputMap.put (KeyStroke.getKeyStroke("DOWN"), "results-down");
		inputMap.put (KeyStroke.getKeyStroke("PAGE_UP"), "results-up-page");
		inputMap.put (KeyStroke.getKeyStroke("PAGE_DOWN"), "results-down-page");
		inputMap.put (KeyStroke.getKeyStroke("ctrl HOME"), "results-home");
		inputMap.put (KeyStroke.getKeyStroke("ctrl END"), "results-end");
		inputMap.put (KeyStroke.getKeyStroke("ESCAPE"), "reset-search");

		ActionMap actionMap = this.searchTextField.getActionMap();

		actionMap.put ("jump-exact", jumpExactAction);
		actionMap.put ("jump-start", jumpStartAction);
		actionMap.put ("jump-end", jumpEndAction);
		actionMap.put ("jump-middle", jumpMiddleAction);
		actionMap.put ("results-up", this.fastTextView.getActionMap().get ("scroll-up"));
		actionMap.put ("results-down", this.fastTextView.getActionMap().get ("scroll-down"));
		actionMap.put ("results-up-page", this.fastTextView.getActionMap().get ("scroll-up-page"));
		actionMap.put ("results-down-page", this.fastTextView.getActionMap().get ("scroll-down-page"));
		actionMap.put ("results-home", this.fastTextView.getActionMap().get ("scroll-home"));
		actionMap.put ("results-end", this.fastTextView.getActionMap().get ("scroll-end"));
		actionMap.put ("reset-search", searchResetAction);

		this.searchTextField.addActionListener (searchStartAction);
		this.searchButton.addActionListener (searchStartAction);
		this.resetButton.addActionListener (searchResetAction);

	}


	/* FocusListener interface */

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(FocusEvent e) {

		this.searchTextField.requestFocusInWindow();

	}


	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(FocusEvent e) {
		// Do nothing
	}


	/* WindowListener interface */

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
		resetSearch();
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


	/* SettingsListener interface */

	/* (non-Javadoc)
	 * @see org.takadb.itadaki.SettingsListener#settingsChanged()
	 */
	public void settingsChanged() {

		ArrayList<Settings.DictionarySettings> dictionarySettings = Settings.getInstance().getDictionarySettings();
		Set<EdictDictionary> dictionaries = new LinkedHashSet<EdictDictionary>();

		for (Settings.DictionarySettings settings : dictionarySettings) {
			try {
				EdictDictionary dictionary = new EdictDictionary (settings.getFileName());
				dictionaries.add (dictionary);
			} catch (DictionaryException e) {
				System.err.println ("Error: Failed to load dictionary " + settings.getFileName());
			}
		}

		getDictionaryManager().setDictionaries (dictionaries);

	}


	/**
	 * Default constructor
	 */
	public DictionaryWindow() {

		super ("Dictionary");

		this.popupFactory = new DictionaryPopupFactory (this);

		this.searcher = new AsynchronousSearcher();
		this.searcher.start();

		JPanel northPanel = createSearchEntryPanel();
		JPanel centerPanel = createSearchResultPanel();

		installActions();

		this.add (northPanel, BorderLayout.NORTH);
		this.add (centerPanel, BorderLayout.CENTER);
		this.pack();
		this.setSize (400,600);

		addFocusListener (this);
		addWindowListener (this);

		Settings.getInstance().addListener (this);

	}


}
