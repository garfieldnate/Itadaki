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

import net.java.sen.ReadingProcessor.ReadingResult;
import net.java.sen.dictionary.Reading;
import net.java.sen.dictionary.Token;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.*;

/**
 * Token analysis display panel
 */
public class AnalysisPanel extends JPanel implements MouseListener, MouseMotionListener {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The currently displayed reading processor result
	 */
	private ReadingResult readingResult;

	/**
	 * Text displayed by the table
	 */
	private String text;

	/**
	 * Stored text segment layouts
	 */
	private List<SegmentLayout> layout = new ArrayList<SegmentLayout>();

	/**
	 * The maximum height in pixels of the laid out text
	 */
	private int layoutHeight;

	/**
	 * Cache of the visual extents of tokens
	 */
	private List<TokenExtent> tokenExtents = new ArrayList<TokenExtent>();

	/**
	 * Width of the layout
	 */
	private int layoutWidth = 0;

	/**
	 * The current selection
	 */
	private net.java.sen.examples.ui.Selection selection = null;

	/**
	 * Length of time in milliseconds after which we are assumed to be
	 * dragging rather than clicking
	 */
	private static final int MIN_DRAG_TIME = 250;

	/**
	 * The original "mouse pressed" event while a mouse drag is in
	 * progress
	 */
	private MouseEvent pressEvent = null;

	/**
	 * Listeners for selection changes
	 */
	private Set<SelectionListener> listeners = new HashSet<SelectionListener>();


	/**
	 * The visual extent of a Token
	 */
	private static class TokenExtent {

		/**
		 * The token's x origin
		 */
		public final int x;

		/**
		 * The token's width
		 */
		public final int width;

		/**
		 * The segment index at the start of the token
		 */
		public final int startSegmentIndex;

		/**
		 * The segment character index at the start of the token
		 */
		public final int startSegmentCharIndex;

		/**
		 * The segment index at the end of the token
		 */
		public final int endSegmentIndex;

		/**
		 * The segment character index at the end of the token
		 */
		@SuppressWarnings("unused")
		public final int endSegmentCharIndex;

		/**
		 * @param x The token's x origin
		 * @param width The token's width
		 * @param startSegmentIndex The segment index at the start of the token
		 * @param startSegmentCharIndex The segment character index at the start of the token
		 * @param endSegmentIndex The segment index at the end of the token
		 * @param endSegmentCharIndex The segment character index at the end of the token
		 */
		public TokenExtent (int x, int width, int startSegmentIndex, int startSegmentCharIndex, int endSegmentIndex, int endSegmentCharIndex) {

			this.x = x;
			this.width = width;
			this.startSegmentIndex = startSegmentIndex;
			this.startSegmentCharIndex = startSegmentCharIndex;
			this.endSegmentIndex = endSegmentIndex;
			this.endSegmentCharIndex = endSegmentCharIndex;

		}

	}


	/**
	 * Calculated layout data of a single token
	 */
	private static class SegmentLayout {

		/**
		 * The X origin of the layout
		 */
		public int x = 0;

		/**
		 * The width of the layout
		 */
		public int width = 0;

		/**
		 * The main text
		 */
		public TextLayout mainLayout = null;

		/**
		 * The furigana
		 */
		public TextLayout furiganaLayout = null;

		/**
		 * The main text offset
		 */
		public Dimension mainTextOffset = null;

		/**
		 * The furigana offset
		 */
		public Dimension furiganaTextOffset = null;

	}


	/**
	 * Add a listener for selection changes
	 *
	 * @param listener The listener to add
	 */
	public void addSelectionListener (SelectionListener listener) {

		this.listeners.add(listener);

	}


	/**
	 * Signals listeners that the selection has changed
	 */
	private void signalSelectionChanged() {

		for (SelectionListener listener : this.listeners) {
			listener.selectionChanged(this.selection);
		}

	}


	/**
	 * Sets the current selection
	 *
	 * @param selection The selection to set
	 */
	public void setSelection (net.java.sen.examples.ui.Selection selection) {

		if (selection.equals(this.selection)) {
			return;
		}

		this.selection = selection.clone();

		int tokenLimit = this.readingResult.getTokens().get(selection.startTokenIndex).getLength() - 1;
		int[] startBounds = getTokenCharBounds(selection.startTokenIndex, 0);
		int[] endBounds = getTokenCharBounds(selection.startTokenIndex, tokenLimit);

		repaint();

		scrollRectToVisible(new Rectangle(startBounds[0], 0, endBounds[1] - startBounds[0] + 50, getHeight()));

		signalSelectionChanged();

	}


	/**
	 * Return the current selection
	 *
	 * @return The current selection
	 */
	public net.java.sen.examples.ui.Selection getSelection() {

		if (this.selection == null) {
			return null;
		}

		return this.selection.clone();

	}


	/**
	 * Set the reading processor result to be displayed
	 *
	 * @param readingResult The reading processor result to be displayed
	 * @param text The text to be displayed
	 */
	public void setReadingResult (ReadingResult readingResult, String text) {

		this.readingResult = readingResult;
		this.text = text;
		this.selection = null;

		createLayout((Graphics2D) getGraphics());
		createTokenExtentCache();
		setPreferredSize(new Dimension(this.layoutWidth, 140));
		revalidate();
		repaint();

	}


	/**
	 * Set the tokens and selection to be displayed
	 *
	 * @param readingResult The reading processor result to be displayed
	 * @param selection The selection to be displayed
	 */
	public void setReadingResultAndSelection (ReadingResult readingResult, net.java.sen.examples.ui.Selection selection) {

		this.readingResult = readingResult;
		this.selection = selection.clone();

		createLayout((Graphics2D) getGraphics());
		createTokenExtentCache();
		setPreferredSize(new Dimension(this.layoutWidth, 140));
		revalidate();
		repaint();

	}


	/**
	 * Creates the token extent cache
	 */
	public void createTokenExtentCache() {

		List<TokenExtent> extents = new ArrayList<TokenExtent>();

		if (this.layout.size() > 0) {

			SegmentLayout segment = this.layout.get(0);
			int segmentIndex = 0;
			int segmentCharacterIndex = 0;
			int characterIndex = 0;

			for (Token token : this.readingResult.getTokens()) {

				// Advance to start of token
				while (characterIndex < token.getStart()) {
					characterIndex++;
					segmentCharacterIndex++;
					if (segmentCharacterIndex >= segment.mainLayout.getCharacterCount()) {
						segmentIndex++;
						segment = this.layout.get(segmentIndex);
						segmentCharacterIndex = 0;
					}
				}
				Rectangle startBounds = segment.mainLayout.getLogicalHighlightShape(segmentCharacterIndex, segmentCharacterIndex + 1).getBounds();
				int x = segment.x + segment.mainTextOffset.width + startBounds.x;
				int startSegmentIndex = segmentIndex;
				int startSegmentCharacterIndex = segmentCharacterIndex;

				// Find end of token
				while (characterIndex < (token.getStart() + token.getLength() - 1)) {
					characterIndex++;
					segmentCharacterIndex++;
					if (segmentCharacterIndex >= segment.mainLayout.getCharacterCount()) {
						segmentIndex++;
						if (segmentIndex < this.layout.size()) {
							segment = this.layout.get(segmentIndex);
							segmentCharacterIndex = 0;
						}
					}
				}
				Rectangle endBounds = segment.mainLayout.getLogicalHighlightShape(segmentCharacterIndex, segmentCharacterIndex + 1).getBounds();
				int width = segment.x + segment.mainTextOffset.width + endBounds.x + endBounds.width - x;
				int endSegmentIndex = segmentIndex;
				int endSegmentCharacterIndex = segmentCharacterIndex;

				TokenExtent extent = new TokenExtent(x, width, startSegmentIndex, startSegmentCharacterIndex, endSegmentIndex, endSegmentCharacterIndex);
				extents.add(extent);

			}

		}

		this.tokenExtents = extents;

	}


	/**
	 * Creates the text layout
	 *
	 * @param g2d The graphics to render against
	 */
	public void createLayout (Graphics2D g2d) {

		List<SegmentLayout> layout = new ArrayList<SegmentLayout>();

		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		Font miniFont = new Font("Serif", Font.PLAIN, 14);
		Font mainFont = new Font("Serif", Font.PLAIN, 40);

		FontRenderContext fontRenderContext = g2d.getFontRenderContext();
		int miniFontHeight = (int)StrictMath.round(-miniFont.getMaxCharBounds(fontRenderContext).getMinY());
		int mainFontHeight = (int)StrictMath.round(-mainFont.getMaxCharBounds(fontRenderContext).getMinY());
		int furiganaY = 10 + miniFontHeight;
		int mainY = furiganaY + 2 + mainFontHeight;

		int x = 0;
		int nextTextIndex = 0;

		for (Reading reading : this.readingResult.getDisplayReadings().values()) {

			// Text without reading
			if (nextTextIndex < reading.start) {
				String mainText = this.text.substring(nextTextIndex, reading.start);
				TextLayout mainLayout = new TextLayout(mainText, mainFont, fontRenderContext);
				SegmentLayout segmentLayout = new SegmentLayout();
				segmentLayout.x = x;
				segmentLayout.width = (int) mainLayout.getAdvance();
				segmentLayout.mainLayout = mainLayout;
				segmentLayout.mainTextOffset = new Dimension(2, 63);
				layout.add(segmentLayout);

				x += segmentLayout.mainLayout.getAdvance();
			}

			// Text with reading
			SegmentLayout segmentLayout = new SegmentLayout();

			String mainText = this.text.substring(reading.start, reading.start + reading.length);
			TextLayout mainLayout = new TextLayout(mainText, mainFont, fontRenderContext);
			TextLayout furiganaLayout = new TextLayout(reading.text, miniFont, fontRenderContext);
			int width = (int) Math.max(mainLayout.getAdvance(), furiganaLayout.getAdvance());

			segmentLayout.x = x;
			segmentLayout.width = width;
			segmentLayout.mainLayout = mainLayout;
			segmentLayout.mainTextOffset = new Dimension((int) ((width - mainLayout.getAdvance()) / 2) + 2, mainY);
			segmentLayout.furiganaLayout = furiganaLayout;
			segmentLayout.furiganaTextOffset = new Dimension((int) ((width - furiganaLayout.getAdvance()) / 2) + 2, furiganaY);
			layout.add(segmentLayout);

			x += width;
			nextTextIndex = reading.start + reading.length;

		}

		// Final text without reading
		if (nextTextIndex < this.text.length()) {
			String mainText = this.text.substring(nextTextIndex, this.text.length());
			TextLayout mainLayout = new TextLayout(mainText, mainFont, fontRenderContext);
			SegmentLayout tokenLayout = new SegmentLayout();
			tokenLayout.x = x;
			tokenLayout.width = (int) mainLayout.getAdvance();
			tokenLayout.mainLayout = mainLayout;
			tokenLayout.mainTextOffset = new Dimension(2, 63);
			layout.add(tokenLayout);
			x += tokenLayout.mainLayout.getAdvance();
		}

		this.layoutWidth = x + 5;
		this.layoutHeight = mainY;
		this.layout = layout;

	}


	/**
	 * Find the token and character index at a given X coordinate
	 *
	 * @param x The X coordinate to search for
	 * @return The token and character indices
	 */
	public int[] findCharacter(int x) {

		for (int i = 0; i < this.tokenExtents.size(); i++) {
			TokenExtent extent = this.tokenExtents.get(i);
			if ((x > extent.x) && (x <= extent.x + extent.width)) {

				int segmentIndex = extent.startSegmentIndex;
				int segmentCharIndex = extent.startSegmentCharIndex;
				int charIndex = 0;

				while (segmentIndex <= extent.endSegmentIndex) {
					SegmentLayout segmentLayout = this.layout.get(segmentIndex);

					if ((x > segmentLayout.x) && (x <= segmentLayout.x + segmentLayout.width)) {
						TextHitInfo hit = segmentLayout.mainLayout.hitTestChar(x - segmentLayout.x - segmentLayout.mainTextOffset.width, 0);
						charIndex += (hit.getCharIndex() - segmentCharIndex);

						return new int[] { i, charIndex };
					}

					charIndex += (segmentLayout.mainLayout.getCharacterCount() - segmentCharIndex);
					segmentIndex++;
					segmentCharIndex = 0;
				}

			}
		}

		return null;

	}


	/**
	 * Finds the starting and ending X indices of a character within a token
	 * from the segment layouts
	 *
	 * @param tokenIndex The token's index
	 * @param tokenCharIndex The index of the character within the token
	 * @return A two member array containing the start and end X coordinates,
	 *         or null if no match was found
	 */
	private int[] getTokenCharBounds(int tokenIndex, int tokenCharIndex) {

		TokenExtent startTokenExtent = this.tokenExtents.get(tokenIndex);
		int segmentIndex = startTokenExtent.startSegmentIndex;
		int segmentCharIndex = startTokenExtent.startSegmentCharIndex;

		int charIndex = 0;

		while (charIndex < this.readingResult.getTokens().get(tokenIndex).getLength()) {

			SegmentLayout segmentLayout = this.layout.get(segmentIndex);
			int maxCharIndex = charIndex + segmentLayout.mainLayout.getCharacterCount() - segmentCharIndex - 1;

			if (tokenCharIndex <= maxCharIndex) {
				int foundIndex = segmentCharIndex + tokenCharIndex - charIndex;
				Rectangle bounds = segmentLayout.mainLayout.getBlackBoxBounds(foundIndex, foundIndex + 1).getBounds();
				bounds.translate(segmentLayout.x + segmentLayout.mainTextOffset.width, 0);
				return new int[] { bounds.x, bounds.x + bounds.width - 1 };
			}

			charIndex = maxCharIndex + 1;
			segmentIndex++;
			segmentCharIndex = 0;
		}

		return null;

	}


	/**
	 * Updates the current selection
	 *
	 * @param movedEvent The mouse event of the updated position
	 * @param complete <code>true</code> if the mouse button was released
	 */
	private void updateSelection (MouseEvent movedEvent, boolean complete) {

		if (complete && ((movedEvent.getWhen() - this.pressEvent.getWhen()) <= MIN_DRAG_TIME)) {

			// Click - select whole morpheme
			int[] characterIndex = findCharacter(this.pressEvent.getX());
			if (characterIndex != null) {

                net.java.sen.examples.ui.Selection selection = new net.java.sen.examples.ui.Selection(characterIndex[0], 0, characterIndex[0], this.readingResult.getTokens().get(characterIndex[0]).getLength() - 1);

				setSelection (selection);

			}

		} else if (complete || ((movedEvent.getWhen() - this.pressEvent.getWhen()) > MIN_DRAG_TIME)) {

			// Drag - update start or end point

			int[] startIndex;
			int[] endIndex;
			if (movedEvent.getX() >= this.pressEvent.getX()) {
				// Forward selection
				startIndex = findCharacter(this.pressEvent.getX());
				endIndex = findCharacter(movedEvent.getX());
			} else {
				// Backward selection
				startIndex = findCharacter(movedEvent.getX());
				endIndex = findCharacter(this.pressEvent.getX());
			}

			if ((startIndex != null) && (endIndex != null)) {
                net.java.sen.examples.ui.Selection selection = new net.java.sen.examples.ui.Selection(startIndex[0], startIndex[1], endIndex[0], endIndex[1]);

				setSelection (selection);
			}

		}

	}


	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Clear background
		g2d.setBackground(Color.WHITE);
		g2d.clearRect(0, 0, getWidth(), getHeight());

		// Render selection
		if (this.selection != null) {

			int[] startBounds = getTokenCharBounds (this.selection.startTokenIndex, this.selection.startCharacterIndex);
			int[] endBounds = getTokenCharBounds (this.selection.endTokenIndex, this.selection.endCharacterIndex);
			g2d.setColor(Color.GREEN);
			g2d.fillRect(startBounds[0], 0, endBounds[1] - startBounds[0], getHeight());

		}

		// Render text
		for (SegmentLayout tokenLayout : this.layout) {

			g2d.setColor(Color.BLACK);

			tokenLayout.mainLayout.draw(g2d, tokenLayout.x + tokenLayout.mainTextOffset.width, tokenLayout.mainTextOffset.height);
			if (tokenLayout.furiganaLayout != null) {
				tokenLayout.furiganaLayout.draw(g2d, tokenLayout.x + tokenLayout.furiganaTextOffset.width, tokenLayout.furiganaTextOffset.height);
			}

		}

		// Render token boundaries
		g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1.0f));
		for (int i = 0; i < this.tokenExtents.size(); i++) {

			TokenExtent extent = this.tokenExtents.get(i);

			if (this.readingResult.getVisibleTokens().get(i)) {
				g2d.setColor(Color.RED);
			} else {
				g2d.setColor(Color.LIGHT_GRAY);
			}

			g2d.drawLine(extent.x + 5, this.layoutHeight + 10, extent.x + 5, this.layoutHeight + 15);
			g2d.drawLine(extent.x + extent.width - 5, this.layoutHeight + 10, extent.x + extent.width - 5, this.layoutHeight + 15);
			g2d.drawLine(extent.x + 5, this.layoutHeight + 15, extent.x + extent.width - 5, this.layoutHeight + 15);

		}

	}


	/* MouseListener interface */

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {

		// Do nothing

	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {

		// Do nothing

	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {

		// Do nothing

	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {

		this.selection = null;
		this.pressEvent = e;

		repaint();

	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {

		updateSelection(e, true);

	}


	/* MouseMotionListener interface */

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {

		updateSelection(e, false);

	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {

		// Do nothing

	}


	/* Constructors */

	/**
	 * Default constructor
	 */
	public AnalysisPanel() {

		addMouseListener(this);
		addMouseMotionListener(this);

	}


}
