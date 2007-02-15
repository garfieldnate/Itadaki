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

package org.itadaki.client.dictionary.ui.dictionary;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.itadaki.client.dictionary.settings.Settings;
import org.itadaki.client.dictionary.settings.SettingsListener;
import org.itadaki.client.dictionary.ui.DictionaryWindow;
import org.itadaki.client.dictionary.ui.OptionsWindow;
import org.itadaki.client.dictionary.utility.StringUtil;
import org.itadaki.fasttextpane.DocumentAttribute;
import org.itadaki.fasttextpane.HyperlinkListener;
import org.itadaki.fasttextpane.FastTextView;
import org.itadaki.seashell.AsynchronousSearcher;
import org.itadaki.seashell.DictionaryStatusListener;
import org.itadaki.seashell.SearchListener;
import org.itadaki.seashell.SearchMode;
import org.itadaki.seashell.edict.EdictDictionary;


/**
 * Adaptor that listens to search results from an AsyncronousSearcher and forwards
 * them to a FastTextView as a formatted Document. Also listens to settings changes
 * to cache the current dictionary highlighting configuration
 */
public class ResultMarshaller implements SearchListener, SettingsListener, DictionaryStatusListener, HyperlinkListener {

	/**
	 * The dictionary window
	 */
	private DictionaryWindow dictionaryWindow; 

	/**
	 * The FastTextView to forward results to
	 */
	private FastTextView fastTextView;

	/**
	 * The bound search thread
	 */
	private AsynchronousSearcher searcher;

	/**
	 * The current Document
	 */
	private EdictDocument edictDocument;

	/**
	 * Listeners for changes in the ResultMarshaller's state
	 * Thread safe by virtue of Collections.synchronizedMap()
	 */
	private Map<ResultMarshallerListener, Integer> listeners = Collections.synchronizedMap (new WeakHashMap<ResultMarshallerListener, Integer>());

	/**
	 * Result section positions
	 */
	private Map<SearchMode,Integer> resultPositions = new HashMap<SearchMode,Integer>();

	/**
	 * (during search) SearchMode of previously received results block 
	 */
	private SearchMode lastSearchMode = null;

	/**
	 * (during search) Running count of search results from current search
	 */
	private int resultCount = 0;

	/**
	 * (during search) Running count of paragraphs of text resulting from current search
	 */
	private int resultParagraphCount = 0;

	/**
	 * (during search) true if the Document has been set on the ResultPane
	 */
	private boolean resultsAreSet = false;

	/**
	 * Map of background colour highlights to apply to results
	 */
	private Map<String,Color> resultBackgroundColours = new HashMap<String,Color>();


	/**
	 * Add a ResultMarshallerListener
	 *
	 * @param listener The listener to add
	 */
	public void addListener (ResultMarshallerListener listener) {

		this.listeners.put (listener, 1);

	}


	/**
	 * Notify all listeners of the start of search results
	 */
	private void notifyListenersStarted() {

		for (ResultMarshallerListener listener : this.listeners.keySet()) {
			listener.resultsStarted ();
		}

	}


	/**
	 * Notify all listeners of the end of search results
	 */
	private void notifyListenersEnded() {

		for (ResultMarshallerListener listener : this.listeners.keySet()) {
			listener.resultsEnded ();
		}

	}


	/**
	 * Notify all listeners that results have been cleared
	 */
	private void notifyListenersCleared() {

		for (ResultMarshallerListener listener : this.listeners.keySet()) {
			listener.resultsCleared();
		}

	}


	/**
	 * Notify all listeners of the creation of a new result section
	 * 
	 * @param searchMode The result section created
	 * @param documentIndex The Document index of the section
	 */
	private void notifyListenersSectionCreated (SearchMode searchMode, int documentIndex) {

		for (ResultMarshallerListener listener : this.listeners.keySet()) {
			listener.resultSectionCreated (searchMode, documentIndex);
		}

	}


	/**
	 * Notify all listeners that new search results have been received
	 *
	 * @param resultCount The new search result count
	 */
	private void notifyListenersCountUpdated (int resultCount) {

		for (ResultMarshallerListener listener : this.listeners.keySet()) {
			listener.resultCountUpdate (resultCount);
		}

	}


	/**
	 * Highlight the search term in a result
	 * 
	 * @param entryString The unattributed result entry 
	 * @param attributedString The string to highlight
	 * @param foldedSearchQuery The case-folded search query to highlight with
	 */
	public static void highlightResult (String entryString, AttributedString attributedString, String foldedSearchQuery) {

		String foldedResult = StringUtil.foldCase (entryString);

		int resultLength = foldedResult.length();
		int queryLength = foldedSearchQuery.length();
		int matchStart = 0;
		for (int i = 0; i < resultLength; i++) {
			if (matchStart >= 0) {
				if (foldedResult.charAt(i) == foldedSearchQuery.charAt (i - matchStart)) {
					if ((i - matchStart + 1) == queryLength) {
						attributedString.addAttribute (TextAttribute.FOREGROUND, Color.BLUE, matchStart, i + 1);
						matchStart = -1;
					}
				} else {
					matchStart = -1;
				}
			} else if (foldedResult.charAt(i) == foldedSearchQuery.charAt(0)) {
				matchStart = i;
				if ((i - matchStart + 1) == queryLength) {
					attributedString.addAttribute (TextAttribute.FOREGROUND, Color.BLUE, matchStart, i + 1);
					matchStart = -1;
				}
			}
		}

	}


	/* (non-Javadoc)
	 * @see org.itadaki.seashell.SearchListener#searchStarted()
	 */
	public void searchStarted (String searchQuery) {

		this.lastSearchMode = null;
		this.resultCount = 0;
		this.resultParagraphCount = 0;
		this.resultPositions.clear();

		this.edictDocument = new EdictDocument (StringUtil.foldCase (searchQuery));
		this.resultsAreSet = false;

		notifyListenersStarted();

	}


	/* (non-Javadoc)
	 * @see org.itadaki.seashell.SearchListener#searchResults(org.itadaki.seashell.SearchMode, java.util.List)
	 */
	public void searchResults (final SearchMode mode, final EdictDictionary dictionary, final List<Integer> resultIndices) {

		final Color backgroundColour = this.resultBackgroundColours.get (dictionary.getFilename());

		if (mode != this.lastSearchMode) {
			if (this.lastSearchMode != null) {
				this.edictDocument.addBlankLines (2);
				this.resultParagraphCount += 2;
			}
			this.lastSearchMode = mode;
			this.resultPositions.put (mode, this.resultParagraphCount);

			notifyListenersSectionCreated (mode, this.resultParagraphCount);

			String headerString = "";
			switch (mode) {
				case EXACT:
					headerString = "Exact matches";
					break;
				case START:
					headerString = "Start matches";
					break;
				case END:
					headerString = "End matches";
					break;
				case MIDDLE:
					headerString = "Other matches";
					break;
			}

			this.edictDocument.addHeader (headerString);
			this.edictDocument.addBlankLines (1);
			this.resultParagraphCount += 2;

		}

		this.edictDocument.addResults (dictionary, backgroundColour, resultIndices);
		this.resultCount += resultIndices.size();
		this.resultParagraphCount += resultIndices.size();

		if (this.resultParagraphCount > 250) {

			// Size change is signalled first to avoid laying out extra times if the Document is set here
			this.edictDocument.signalSizeChange();

			if (!this.resultsAreSet) {
				if (! (this.fastTextView.getDocument() instanceof InformationalDocument)) {
					this.fastTextView.setDocument (this.edictDocument);
					this.resultsAreSet = true;
				}
			}

		}

		notifyListenersCountUpdated (this.resultCount);

	}


	/* (non-Javadoc)
	 * @see org.itadaki.seashell.SearchListener#searchFinished()
	 */
	public void searchFinished() {

		// Size change is signalled first to avoid laying out extra times if the Document is set here
		this.edictDocument.signalSizeChange();

		if (!this.resultsAreSet) {
			if (! (this.fastTextView.getDocument() instanceof InformationalDocument)) {
				this.fastTextView.setDocument (this.edictDocument);
			}
		}

		notifyListenersEnded();

	}


	/* (non-Javadoc)
	 * @see org.itadaki.seashell.SearchListener#searchCancelled()
	 */
	public void searchAborted() {

		if (!(this.fastTextView.getDocument() instanceof InformationalDocument)) {
			this.fastTextView.setDocument (null);
		}

		notifyListenersCleared();

	}


	/* (non-Javadoc)
	 * @see org.itadaki.seashell.DictionaryStatusListener#dictionariesUnavailable()
	 */
	public void dictionariesUnavailable() {

		this.searcher.setDictionaries (new HashSet<EdictDictionary>());

	}


	/* (non-Javadoc)
	 * @see org.itadaki.seashell.DictionaryStatusListener#dictionariesIndexing(org.itadaki.seashell.edict.EdictDictionary, int)
	 */
	public void dictionariesIndexing (final EdictDictionary dictionary, final int remainingDictionaries) {

		this.fastTextView.setDocument (new InformationalDocument(
				"",
				"Indexing " + dictionary.getFilename() + "...",
				"(" + remainingDictionaries + " more remaining)"
		));
		
	}


	/* (non-Javadoc)
	 * @see org.itadaki.seashell.DictionaryStatusListener#dictionariesReady()
	 */
	public void dictionariesReady() {

		final Set<EdictDictionary> dictionaries = DictionaryWindow.getDictionaryManager().getDictionaries();
		this.searcher.setDictionaries (dictionaries);

		if (dictionaries.size() == 0) {

			// Display informational message about adding dictionaries

			InformationalDocument noDictionariesDocument = new InformationalDocument();
			Hashtable<TextAttribute, Object> plainAttributes = new Hashtable<TextAttribute, Object>();
			plainAttributes.put (TextAttribute.FONT, new Font ("SansSerif", Font.PLAIN, 14));
			noDictionariesDocument.add (new AttributedString ("\n", plainAttributes));
			noDictionariesDocument.add (new AttributedString ("There are currently no dictionaries configured.", plainAttributes));
			AttributedString line = new AttributedString ("Click here to configure dictionaries", plainAttributes);

			Hashtable<Attribute, Object> linkAttributes = new Hashtable<Attribute, Object>();
			linkAttributes.put (TextAttribute.FOREGROUND, Color.BLUE);
			linkAttributes.put (TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			linkAttributes.put (DocumentAttribute.HYPERLINK, "options:dictionary");
			line.addAttributes (linkAttributes, 6, 10);
			noDictionariesDocument.add (line);
			this.fastTextView.setDocument (noDictionariesDocument);

		} else {

			// Kick dictionary window to reload its current search

			if (this.fastTextView.getDocument() instanceof InformationalDocument) {
				this.fastTextView.setDocument (null);
			}

			this.dictionaryWindow.reloadSearch();

		}

	}



	/* (non-Javadoc)
	 * @see org.takadb.itadaki.PreferencesListener#preferencesChanged()
	 */
	public void settingsChanged() {

		ArrayList<Settings.DictionarySettings> dictionarySettings = Settings.getInstance().getDictionarySettings();
		for (Settings.DictionarySettings settings : dictionarySettings) {
			this.resultBackgroundColours.put (settings.getFileName(), settings.getHighlightBackgroundColour());
		}

	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.resultpane.HyperlinkListener#hyperlinkInvoked(java.lang.Object)
	 */
	public void hyperlinkInvoked (Object value) {

		if (value.equals ("options:dictionary")) {
			new OptionsWindow (this.dictionaryWindow);
		}

	}


	/**
	 * @param dictionaryWindow The dictionary window
	 * @param fastTextView The ResultPane to feed search results to
	 * @param searcher The search thread to bind to
	 */
	public ResultMarshaller (DictionaryWindow dictionaryWindow, FastTextView fastTextView, AsynchronousSearcher searcher) {

		this.dictionaryWindow = dictionaryWindow;
		this.fastTextView = fastTextView;
		this.searcher = searcher;

		this.fastTextView.addHyperlinkListener (this);
		this.searcher.addListener (this);

		Settings.getInstance().addListener (this);
		DictionaryWindow.getDictionaryManager().addStatusListener (this);

	}


}
